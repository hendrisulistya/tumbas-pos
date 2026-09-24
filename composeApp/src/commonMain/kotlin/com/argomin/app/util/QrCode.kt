package com.argomin.app.util

import kotlin.math.max
import kotlin.math.min

/**
 * Pure Kotlin QR Code generator (zero external dependencies).
 * Based on Project Nayuki QR Code generator (MIT License).
 * Compatible with all Kotlin Multiplatform targets (Android, WasmJs, iOS, Desktop).
 */

class BitBuffer {
    private val data = mutableListOf<Boolean>()

    val bitLength: Int
        get() = data.size

    fun getBit(index: Int): Int {
        if (index !in 0 until data.size) throw IndexOutOfBoundsException("Index: $index, Size: ${data.size}")
        return if (data[index]) 1 else 0
    }

    fun appendBits(value: Int, len: Int) {
        require(len in 0..31 && (value ushr len) == 0) { "Value out of range" }
        for (i in len - 1 downTo 0) {
            data.add(((value ushr i) and 1) != 0)
        }
    }

    fun appendData(bb: BitBuffer) {
        data.addAll(bb.data)
    }

    fun getBytes(): ByteArray {
        val res = ByteArray((data.size + 7) / 8)
        for (i in data.indices) {
            if (data[i]) {
                res[i / 8] = (res[i / 8].toInt() or (1 shl (7 - (i % 8)))).toByte()
            }
        }
        return res
    }

    fun copy(): BitBuffer {
        val res = BitBuffer()
        res.data.addAll(this.data)
        return res
    }
}

enum class QrMode(val modeBits: Int, private val numBitsCharCount: IntArray) {
    NUMERIC(0x1, intArrayOf(10, 12, 14)),
    ALPHANUMERIC(0x2, intArrayOf(9, 11, 13)),
    BYTE(0x4, intArrayOf(8, 16, 16)),
    KANJI(0x8, intArrayOf(8, 10, 12)),
    ECI(0x7, intArrayOf(0, 0, 0));

    fun numCharCountBits(version: Int): Int {
        val i = when {
            version <= 9 -> 0
            version <= 26 -> 1
            else -> 2
        }
        return numBitsCharCount[i]
    }
}

class QrSegment(
    val mode: QrMode,
    val numChars: Int,
    val data: BitBuffer
) {
    init {
        require(numChars >= 0) { "Invalid number of characters" }
    }

    companion object {
        private const val ALPHANUMERIC_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

        fun makeBytes(data: ByteArray): QrSegment {
            val bb = BitBuffer()
            for (b in data) {
                bb.appendBits(b.toInt() and 0xFF, 8)
            }
            return QrSegment(QrMode.BYTE, data.size, bb)
        }

        fun makeNumeric(digits: String): QrSegment {
            val bb = BitBuffer()
            var i = 0
            while (i < digits.length) {
                val n = min(digits.length - i, 3)
                bb.appendBits(digits.substring(i, i + n).toInt(), n * 3 + 1)
                i += n
            }
            return QrSegment(QrMode.NUMERIC, digits.length, bb)
        }

        fun makeAlphanumeric(text: String): QrSegment {
            val bb = BitBuffer()
            var i = 0
            while (i <= text.length - 2) {
                val temp = ALPHANUMERIC_CHARSET.indexOf(text[i]) * 45 + ALPHANUMERIC_CHARSET.indexOf(text[i + 1])
                bb.appendBits(temp, 11)
                i += 2
            }
            if (i < text.length) {
                bb.appendBits(ALPHANUMERIC_CHARSET.indexOf(text[i]), 6)
            }
            return QrSegment(QrMode.ALPHANUMERIC, text.length, bb)
        }

        fun makeSegments(text: String): List<QrSegment> {
            return when {
                text.all { it in '0'..'9' } -> listOf(makeNumeric(text))
                text.all { it in ALPHANUMERIC_CHARSET } -> listOf(makeAlphanumeric(text))
                else -> listOf(makeBytes(text.encodeToByteArray()))
            }
        }

        fun getTotalBits(segs: List<QrSegment>, version: Int): Int {
            var result = 0
            for (seg in segs) {
                val ccbits = seg.mode.numCharCountBits(version)
                if (seg.numChars >= (1 shl ccbits)) return -1
                result += 4 + ccbits + seg.data.bitLength
            }
            return result
        }
    }
}

class QrCode(
    val version: Int,
    val errorCorrectionLevel: Ecc,
    dataCodewords: ByteArray,
    msk: Int
) {
    enum class Ecc(val formatBits: Int) {
        LOW(1),
        MEDIUM(0),
        QUARTILE(3),
        HIGH(2)
    }

    val size: Int = version * 4 + 17
    val mask: Int
    private val modules: Array<BooleanArray> = Array(size) { BooleanArray(size) }
    private var isFunction: Array<BooleanArray>? = Array(size) { BooleanArray(size) }

    init {
        require(version in MIN_VERSION..MAX_VERSION) { "Version out of range" }
        require(msk in -1..7) { "Mask value out of range" }

        drawFunctionPatterns()
        val allCodewords = addEccAndInterleave(dataCodewords)
        drawCodewords(allCodewords)

        var finalMask = msk
        if (finalMask == -1) {
            var minPenalty = Int.MAX_VALUE
            for (i in 0 until 8) {
                applyMask(i)
                drawFormatBits(i)
                val penalty = getPenaltyScore()
                if (penalty < minPenalty) {
                    finalMask = i
                    minPenalty = penalty
                }
                applyMask(i)
            }
        }
        mask = finalMask
        applyMask(finalMask)
        drawFormatBits(finalMask)
        isFunction = null
    }

    fun getModule(x: Int, y: Int): Boolean {
        return x in 0 until size && y in 0 until size && modules[y][x]
    }

    private fun drawFunctionPatterns() {
        for (i in 0 until size) {
            setFunctionModule(6, i, i % 2 == 0)
            setFunctionModule(i, 6, i % 2 == 0)
        }

        drawFinderPattern(3, 3)
        drawFinderPattern(size - 4, 3)
        drawFinderPattern(3, size - 4)

        val alignPatPos = getAlignmentPatternPositions()
        val numAlign = alignPatPos.size
        for (i in 0 until numAlign) {
            for (j in 0 until numAlign) {
                if (!(i == 0 && j == 0 || i == 0 && j == numAlign - 1 || i == numAlign - 1 && j == 0)) {
                    drawAlignmentPattern(alignPatPos[i], alignPatPos[j])
                }
            }
        }

        drawFormatBits(0)
        drawVersion()
    }

    private fun drawFormatBits(msk: Int) {
        val data = (errorCorrectionLevel.formatBits shl 3) or msk
        var rem = data
        for (i in 0 until 10) {
            rem = (rem shl 1) xor ((rem ushr 9) * 0x537)
        }
        val bits = ((data shl 10) or rem) xor 0x5412

        for (i in 0..5) {
            setFunctionModule(8, i, getBit(bits, i))
        }
        setFunctionModule(8, 7, getBit(bits, 6))
        setFunctionModule(8, 8, getBit(bits, 7))
        setFunctionModule(7, 8, getBit(bits, 8))
        for (i in 9..14) {
            setFunctionModule(14 - i, 8, getBit(bits, i))
        }

        for (i in 0..7) {
            setFunctionModule(size - 1 - i, 8, getBit(bits, i))
        }
        for (i in 8..14) {
            setFunctionModule(8, size - 15 + i, getBit(bits, i))
        }
        setFunctionModule(8, size - 8, true)
    }

    private fun drawVersion() {
        if (version < 7) return
        var rem = version
        for (i in 0 until 12) {
            rem = (rem shl 1) xor ((rem ushr 11) * 0x1F25)
        }
        val bits = (version shl 12) or rem
        for (i in 0 until 18) {
            val a = size - 11 + i % 3
            val b = i / 3
            val bit = getBit(bits, i)
            setFunctionModule(a, b, bit)
            setFunctionModule(b, a, bit)
        }
    }

    private fun drawFinderPattern(x: Int, y: Int) {
        for (dy in -4..4) {
            for (dx in -4..4) {
                val dist = max(kotlin.math.abs(dx), kotlin.math.abs(dy))
                val xx = x + dx
                val yy = y + dy
                if (xx in 0 until size && yy in 0 until size) {
                    setFunctionModule(xx, yy, dist != 2 && dist != 4)
                }
            }
        }
    }

    private fun drawAlignmentPattern(x: Int, y: Int) {
        for (dy in -2..2) {
            for (dx in -2..2) {
                setFunctionModule(x + dx, y + dy, max(kotlin.math.abs(dx), kotlin.math.abs(dy)) != 1)
            }
        }
    }

    private fun setFunctionModule(x: Int, y: Int, isDark: Boolean) {
        modules[y][x] = isDark
        isFunction!![y][x] = true
    }

    private fun addEccAndInterleave(data: ByteArray): ByteArray {
        val numBlocks = NUM_ERROR_CORRECTION_BLOCKS[errorCorrectionLevel.ordinal][version]
        val blockEccLen = ECC_CODEWORDS_PER_BLOCK[errorCorrectionLevel.ordinal][version]
        val rawCodewords = getNumRawDataModules(version) / 8
        val numShortBlocks = numBlocks - rawCodewords % numBlocks
        val shortBlockLen = rawCodewords / numBlocks

        val blocks = Array(numBlocks) { ByteArray(shortBlockLen + 1) }
        val rsDiv = reedSolomonComputeDivisor(blockEccLen)
        var k = 0
        for (i in 0 until numBlocks) {
            val datLen = shortBlockLen - blockEccLen + (if (i < numShortBlocks) 0 else 1)
            val dat = data.copyOfRange(k, k + datLen)
            k += datLen
            val block = blocks[i]
            for (m in 0 until datLen) {
                block[m] = dat[m]
            }
            val ecc = reedSolomonComputeRemainder(dat, rsDiv)
            for (m in 0 until ecc.size) {
                block[block.size - blockEccLen + m] = ecc[m]
            }
        }

        val result = ByteArray(rawCodewords)
        var p = 0
        for (i in 0 until blocks[0].size) {
            for (j in 0 until numBlocks) {
                // Skip the padding byte in short blocks
                if (i != shortBlockLen - blockEccLen || j >= numShortBlocks) {
                    result[p++] = blocks[j][i]
                }
            }
        }
        return result
    }

    private fun drawCodewords(data: ByteArray) {
        var i = 0
        var right = size - 1
        while (right > 0) {
            if (right == 6) right = 5
            for (vert in 0 until size) {
                for (j in 0 until 2) {
                    val x = right - j
                    val upward = ((right + 1) and 2) == 0
                    val y = if (upward) size - 1 - vert else vert
                    if (!isFunction!![y][x] && i < data.size * 8) {
                        modules[y][x] = getBit(data[i ushr 3].toInt(), 7 - (i and 7))
                        i++
                    }
                }
            }
            right -= 2
        }
    }

    private fun applyMask(msk: Int) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val invert = when (msk) {
                    0 -> (x + y) % 2 == 0
                    1 -> y % 2 == 0
                    2 -> x % 3 == 0
                    3 -> (x + y) % 3 == 0
                    4 -> (x / 3 + y / 2) % 2 == 0
                    5 -> x * y % 2 + x * y % 3 == 0
                    6 -> (x * y % 2 + x * y % 3) % 2 == 0
                    7 -> ((x + y) % 2 + x * y % 3) % 2 == 0
                    else -> error("Invalid mask")
                }
                val isFunc = if (isFunction != null) isFunction!![y][x] else false
                if (!isFunc && invert) {
                    modules[y][x] = !modules[y][x]
                }
            }
        }
    }

    private fun getPenaltyScore(): Int {
        var result = 0
        for (y in 0 until size) {
            var runColor = false
            var runX = 0
            val runHistory = IntArray(7)
            for (x in 0 until size) {
                if (modules[y][x] == runColor) {
                    runX++
                    if (runX == 5) result += PENALTY_N1
                    else if (runX > 5) result++
                } else {
                    finderPenaltyAddHistory(runX, runHistory)
                    if (!runColor) {
                        result += finderPenaltyCountPatterns(runHistory) * PENALTY_N3
                    }
                    runColor = modules[y][x]
                    runX = 1
                }
            }
            result += finderPenaltyTerminateAndCount(runColor, runX, runHistory) * PENALTY_N3
        }

        for (x in 0 until size) {
            var runColor = false
            var runY = 0
            val runHistory = IntArray(7)
            for (y in 0 until size) {
                if (modules[y][x] == runColor) {
                    runY++
                    if (runY == 5) result += PENALTY_N1
                    else if (runY > 5) result++
                } else {
                    finderPenaltyAddHistory(runY, runHistory)
                    if (!runColor) {
                        result += finderPenaltyCountPatterns(runHistory) * PENALTY_N3
                    }
                    runColor = modules[y][x]
                    runY = 1
                }
            }
            result += finderPenaltyTerminateAndCount(runColor, runY, runHistory) * PENALTY_N3
        }

        for (y in 0 until size - 1) {
            for (x in 0 until size - 1) {
                val color = modules[y][x]
                if (color == modules[y][x + 1] && color == modules[y + 1][x] && color == modules[y + 1][x + 1]) {
                    result += PENALTY_N2
                }
            }
        }

        var dark = 0
        for (row in modules) {
            for (color in row) {
                if (color) dark++
            }
        }
        val total = size * size
        val k = (kotlin.math.abs(dark * 20 - total * 10) + total - 1) / total - 1
        result += k * PENALTY_N4
        return result
    }

    private fun getAlignmentPatternPositions(): IntArray {
        if (version == 1) return intArrayOf()
        val num = version / 7 + 2
        val step = if (version == 32) 26 else (version * 4 + num * 2 + 1) / (num * 2 - 2) * 2
        val result = IntArray(num)
        result[0] = 6
        var pos = size - 7
        for (i in num - 1 downTo 1) {
            result[i] = pos
            pos -= step
        }
        return result
    }

    private fun finderPenaltyCountPatterns(runHistory: IntArray): Int {
        val n = runHistory[1]
        val core = n > 0 && runHistory[2] == n && runHistory[3] == n * 3 && runHistory[4] == n && runHistory[5] == n
        return (if (core && runHistory[0] >= n * 4 && runHistory[6] >= n) 1 else 0) +
                (if (core && runHistory[6] >= n * 4 && runHistory[0] >= n) 1 else 0)
    }

    private fun finderPenaltyTerminateAndCount(currentRunColor: Boolean, currentRunLength: Int, runHistory: IntArray): Int {
        var len = currentRunLength
        if (currentRunColor) {
            finderPenaltyAddHistory(len, runHistory)
            len = 0
        }
        len += size
        finderPenaltyAddHistory(len, runHistory)
        return finderPenaltyCountPatterns(runHistory)
    }

    private fun finderPenaltyAddHistory(currentRunLength: Int, runHistory: IntArray): Unit {
        if (runHistory[0] == 0) {
            runHistory[0] = currentRunLength
            return
        }
        for (k in runHistory.size - 1 downTo 1) runHistory[k] = runHistory[k - 1]
        runHistory[0] = currentRunLength
    }

    companion object {
        const val MIN_VERSION = 1
        const val MAX_VERSION = 40

        private const val PENALTY_N1 = 3
        private const val PENALTY_N2 = 3
        private const val PENALTY_N3 = 40
        private const val PENALTY_N4 = 10

        private val ECC_CODEWORDS_PER_BLOCK = arrayOf(
            intArrayOf(-1,  7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30),
            intArrayOf(-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28),
            intArrayOf(-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30),
            intArrayOf(-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
        )

        private val NUM_ERROR_CORRECTION_BLOCKS = arrayOf(
            intArrayOf(-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4,  4,  4,  4,  4,  6,  6,  6,  6,  7,  8,  8,  9,  9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25),
            intArrayOf(-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5,  5,  8,  9,  9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49),
            intArrayOf(-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8,  8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68),
            intArrayOf(-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81)
        )

        fun encodeText(text: String, ecl: Ecc = Ecc.MEDIUM): QrCode {
            val segs = QrSegment.makeSegments(text)
            return encodeSegments(segs, ecl)
        }

        fun encodeSegments(
            segs: List<QrSegment>,
            ecl: Ecc,
            minVersion: Int = 1,
            maxVersion: Int = 40,
            mask: Int = -1,
            boostEcl: Boolean = true
        ): QrCode {
            var version = minVersion
            var dataUsedBits = -1
            while (true) {
                val dataCapacityBits = getNumDataCodewords(version, ecl) * 8
                val used = QrSegment.getTotalBits(segs, version)
                if (used != -1 && used <= dataCapacityBits) {
                    dataUsedBits = used
                    break
                }
                if (version >= maxVersion) throw IllegalArgumentException("Data too long for QR Code")
                version++
            }

            var finalEcl = ecl
            if (boostEcl) {
                for (newEcl in listOf(Ecc.MEDIUM, Ecc.QUARTILE, Ecc.HIGH)) {
                    if (dataUsedBits <= getNumDataCodewords(version, newEcl) * 8) {
                        finalEcl = newEcl
                    }
                }
            }

            val bb = BitBuffer()
            for (seg in segs) {
                bb.appendBits(seg.mode.modeBits, 4)
                bb.appendBits(seg.numChars, seg.mode.numCharCountBits(version))
                bb.appendData(seg.data)
            }

            val dataCapacityBits = getNumDataCodewords(version, finalEcl) * 8
            bb.appendBits(0, min(4, dataCapacityBits - bb.bitLength))
            bb.appendBits(0, (8 - bb.bitLength % 8) % 8)

            var padByte = 0xEC
            while (bb.bitLength < dataCapacityBits) {
                bb.appendBits(padByte, 8)
                padByte = padByte xor (0xEC xor 0x11)
            }

            return QrCode(version, finalEcl, bb.getBytes(), mask)
        }

        fun getNumDataCodewords(ver: Int, ecl: Ecc): Int {
            return getNumRawDataModules(ver) / 8 -
                    ECC_CODEWORDS_PER_BLOCK[ecl.ordinal][ver] *
                    NUM_ERROR_CORRECTION_BLOCKS[ecl.ordinal][ver]
        }

        fun getNumRawDataModules(ver: Int): Int {
            var result = (16 * ver + 128) * ver + 64
            if (ver >= 2) {
                val numAlign = ver / 7 + 2
                result -= (25 * numAlign - 10) * numAlign - 55
                if (ver >= 7) result -= 36
            }
            return result
        }

        private fun reedSolomonComputeDivisor(degree: Int): ByteArray {
            val result = ByteArray(degree)
            result[degree - 1] = 1
            var root = 1
            for (i in 0 until degree) {
                for (j in 0 until degree) {
                    result[j] = reedSolomonMultiply(result[j].toInt() and 0xFF, root).toByte()
                    if (j + 1 < degree) {
                        result[j] = (result[j].toInt() xor result[j + 1].toInt()).toByte()
                    }
                }
                root = reedSolomonMultiply(root, 0x02)
            }
            return result
        }

        private fun reedSolomonComputeRemainder(data: ByteArray, divisor: ByteArray): ByteArray {
            val result = ByteArray(divisor.size)
            for (b in data) {
                val factor = (b.toInt() xor result[0].toInt()) and 0xFF
                for (k in 0 until result.size - 1) result[k] = result[k + 1]
                result[result.size - 1] = 0
                for (i in result.indices) {
                    result[i] = (result[i].toInt() xor reedSolomonMultiply(divisor[i].toInt() and 0xFF, factor)).toByte()
                }
            }
            return result
        }

        private fun reedSolomonMultiply(x: Int, y: Int): Int {
            var z = 0
            for (i in 7 downTo 0) {
                z = (z shl 1) xor ((z ushr 7) * 0x11D)
                z = z xor (((y ushr i) and 1) * x)
            }
            return z
        }

        private fun getBit(x: Int, i: Int): Boolean {
            return ((x ushr i) and 1) != 0
        }
    }
}
