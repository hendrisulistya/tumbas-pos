package com.argomin.app.util

/**
 * Hasil parsing dan validasi data QRIS standar EMVCo / ASPI (Bank Indonesia).
 */
data class QrisValidationResult(
    val isValid: Boolean,
    val merchantName: String = "",
    val nmid: String = "",
    val terminalId: String = "A01",
    val mcc: String = "",
    val city: String = "",
    val postalCode: String = "",
    val isDynamic: Boolean = false,
    val rawPayload: String = "",
    val calculatedCrc: String = "",
    val expectedCrc: String = "",
    val errorMessage: String? = null
)

/**
 * Parser dan validator standar QRIS Merchant-Presented Mode (MPM).
 * Memverifikasi:
 * 1. Format Header Tag 00 = "01" (Payload Format Indicator)
 * 2. Checksum CRC16 (Tag 63) standar EMVCo CCITT-FALSE
 * 3. Kode Negara Tag 58 = "ID" (Indonesia)
 * 4. Tag 59 (Nama Merchant)
 * 5. Ekstraksi NMID (National Merchant ID) dari Tag 51 (sub-tag 02), Tag 26-45, atau pola ASPI
 */
fun validateAndParseQris(payload: String): QrisValidationResult {
    val clean = payload.trim()
    if (clean.length < 24) {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "Data QRIS terlalu pendek atau kosong (minimal 24 karakter)."
        )
    }

    // 1. Validasi Header Tag 00
    if (!clean.startsWith("000201")) {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "Format QRIS tidak valid: Header bukan '000201' (Payload Format Indicator)."
        )
    }

    // 2. Validasi CRC16 (Tag 63)
    val crcTagIdx = clean.lastIndexOf("6304")
    if (crcTagIdx == -1 || crcTagIdx + 8 != clean.length) {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "Struktur QRIS tidak lengkap: Tag CRC (6304) tidak ditemukan di akhir payload."
        )
    }

    val dataBeforeCrc = clean.substring(0, crcTagIdx + 4)
    val expectedCrc = clean.substring(crcTagIdx + 4).uppercase()
    val calculatedCrc = crc16Ccitt(dataBeforeCrc).uppercase()

    if (expectedCrc != calculatedCrc) {
        return QrisValidationResult(
            isValid = false,
            calculatedCrc = calculatedCrc,
            expectedCrc = expectedCrc,
            errorMessage = "Checksum CRC QRIS tidak cocok (Ditemukan: $expectedCrc, Seharusnya: $calculatedCrc). Data QRIS kemungkinan rusak/terpotong."
        )
    }

    // 3. Parsing TLV (Tag-Length-Value)
    val tags = mutableMapOf<String, String>()
    var idx = 0
    try {
        while (idx < crcTagIdx) {
            if (idx + 4 > crcTagIdx) break
            val tag = clean.substring(idx, idx + 2)
            val len = clean.substring(idx + 2, idx + 4).toIntOrNull() ?: break
            val valStart = idx + 4
            val valEnd = valStart + len
            if (valEnd > crcTagIdx) break
            val value = clean.substring(valStart, valEnd)
            tags[tag] = value
            idx = valEnd
        }
    } catch (e: Exception) {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "Gagal memproses struktur TLV QRIS: ${e.message}"
        )
    }

    // 4. Validasi Kode Negara Indonesia (Tag 58 = "ID")
    val countryCode = tags["58"]?.trim() ?: ""
    if (countryCode != "ID") {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "QRIS bukan standar Indonesia (Kode Negara Tag 58: '$countryCode', diharapkan 'ID')."
        )
    }

    // 5. Validasi Nama Merchant (Tag 59)
    val merchantName = tags["59"]?.trim() ?: ""
    if (merchantName.isEmpty()) {
        return QrisValidationResult(
            isValid = false,
            errorMessage = "Nama Merchant (Tag 59) tidak ditemukan di dalam payload QRIS."
        )
    }

    // 6. Ekstraksi NMID (National Merchant ID)
    // Tag 51: Central Repository (subtag 00=ID.CO.QRIS.WWW, subtag 02=NMID)
    var nmid = ""
    tags["51"]?.let { tag51Val ->
        var sIdx = 0
        while (sIdx + 4 <= tag51Val.length) {
            val sTag = tag51Val.substring(sIdx, sIdx + 2)
            val sLen = tag51Val.substring(sIdx + 2, sIdx + 4).toIntOrNull() ?: break
            val sStart = sIdx + 4
            val sEnd = sStart + sLen
            if (sEnd > tag51Val.length) break
            val sVal = tag51Val.substring(sStart, sEnd)
            if (sTag == "02" && (sVal.startsWith("ID") || sVal.length >= 10)) {
                nmid = sVal.trim()
                break
            }
            sIdx = sEnd
        }
    }

    // Jika belum ditemukan, periksa Tag 26-50 (Merchant Account Information Acquirer)
    if (nmid.isEmpty()) {
        for (t in 26..50) {
            val tKey = t.toString().padStart(2, '0')
            tags[tKey]?.let { valStr ->
                var sIdx = 0
                while (sIdx + 4 <= valStr.length) {
                    val sTag = valStr.substring(sIdx, sIdx + 2)
                    val sLen = valStr.substring(sIdx + 2, sIdx + 4).toIntOrNull() ?: break
                    val sStart = sIdx + 4
                    val sEnd = sStart + sLen
                    if (sEnd > valStr.length) break
                    val sVal = valStr.substring(sStart, sEnd)
                    if (sTag == "02" && (sVal.startsWith("ID") || sVal.length >= 10)) {
                        nmid = sVal.trim()
                        break
                    }
                    sIdx = sEnd
                }
            }
            if (nmid.isNotEmpty()) break
        }
    }

    // Fallback: Regex pola NMID nasional (contoh: ID1026469990109 atau ID diikuti 10-15 angka)
    if (nmid.isEmpty()) {
        val nmidRegex = Regex("ID[0-9]{10,16}")
        val match = nmidRegex.find(clean)
        if (match != null) {
            nmid = match.value
        }
    }

    // 7. Terminal ID (Tag 62 sub-tag 07)
    var terminalId = "A01"
    tags["62"]?.let { tag62Val ->
        var sIdx = 0
        while (sIdx + 4 <= tag62Val.length) {
            val sTag = tag62Val.substring(sIdx, sIdx + 2)
            val sLen = tag62Val.substring(sIdx + 2, sIdx + 4).toIntOrNull() ?: break
            val sStart = sIdx + 4
            val sEnd = sStart + sLen
            if (sEnd > tag62Val.length) break
            val sVal = tag62Val.substring(sStart, sEnd)
            if (sTag == "07") {
                terminalId = sVal.trim()
                break
            }
            sIdx = sEnd
        }
    }

    val pointOfInitiation = tags["01"] ?: "11"
    val isDynamic = pointOfInitiation == "12"

    return QrisValidationResult(
        isValid = true,
        merchantName = merchantName,
        nmid = if (nmid.isNotEmpty()) nmid else "TIDAK TERDETEKSI",
        terminalId = terminalId,
        mcc = tags["52"] ?: "",
        city = tags["60"] ?: "",
        postalCode = tags["61"] ?: "",
        isDynamic = isDynamic,
        rawPayload = clean,
        calculatedCrc = calculatedCrc,
        expectedCrc = expectedCrc
    )
}
