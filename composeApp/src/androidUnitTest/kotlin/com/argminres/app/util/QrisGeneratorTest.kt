package com.argminres.app.util

import kotlin.test.*

class QrisGeneratorTest {

    @Test
    fun `generateDynamicQris - menghasilkan data QRIS dinamis yang valid dengan fee 1000`() {
        val baseAmount = 38500L
        val fee = 1000L
        val data = generateDynamicQris(baseAmount, fee)

        assertEquals(baseAmount, data.baseAmount)
        assertEquals(fee, data.fee)
        assertEquals(39500L, data.totalAmount)
        assertEquals(QrisConfig.NMID, data.nmid)
        assertEquals("ID1026469990109", data.nmid)
        assertEquals(QrisConfig.MERCHANT_NAME, data.merchantName)
        assertEquals(QrisConfig.TERMINAL_ID, data.terminalId)

        // Verifikasi Tag 01 berubah menjadi 12 (Dynamic QR)
        assertTrue(data.payload.contains("010212"), "Payload harus bertipe Dynamic QR (010212)")

        // Verifikasi Tag 54 menyertakan nominal total 39500 (540539500)
        assertTrue(data.payload.contains("540539500"), "Payload harus memuat Tag 54 dengan nominal 39500")

        // Verifikasi Tag 51 memuat NMID ID1026469990109
        assertTrue(data.payload.contains("ID1026469990109"), "Payload harus memuat NMID yang sesuai")

        // Verifikasi Tag 58 adalah ID (Indonesia) dan Tag 53 adalah 360 (IDR)
        assertTrue(data.payload.contains("5303360"), "Currency harus 360 (IDR)")
        assertTrue(data.payload.contains("5802ID"), "Country code harus ID")

        // Verifikasi Tag 63 (CRC16)
        val payloadWithoutCrc = data.payload.substring(0, data.payload.length - 4)
        val expectedCrc = crc16Ccitt(payloadWithoutCrc)
        val actualCrc = data.payload.takeLast(4)
        assertEquals(expectedCrc, actualCrc, "Checksum CRC-16/CCITT-FALSE harus cocok")
    }

    @Test
    fun `generateDynamicQris - menghasilkan QR matrix yang sesuai spesifikasi MPM QRIS Dasar`() {
        val data = generateDynamicQris(50000L, 1000L)
        val qr = QrCode.encodeText(data.payload, QrCode.Ecc.MEDIUM)

        // Version 11 QR: 61x61 modul
        assertEquals(61, qr.size)

        // Finder patterns harus ada di ketiga sudut
        assertTrue(qr.getModule(0, 0), "Sudut kiri atas finder pattern harus gelap")
        assertTrue(qr.getModule(qr.size - 1, 0), "Sudut kanan atas finder pattern harus gelap")
        assertTrue(qr.getModule(0, qr.size - 1), "Sudut kiri bawah finder pattern harus gelap")

        // Separator harus terang (putih)
        assertFalse(qr.getModule(7, 7), "Separator finder pattern harus terang")
    }

    @Test
    fun `MPM QRIS Dasar layout properties - NMID dan Merchant Name sesuai data asli`() {
        assertEquals("ID1026469990109", QrisConfig.NMID)
        assertEquals("RM ARGO MINANG 3 KEPEK, P", QrisConfig.MERCHANT_NAME)
        assertEquals("A02", QrisConfig.TERMINAL_ID)
        assertEquals(1000L, QrisConfig.DEFAULT_FEE)
    }
}
