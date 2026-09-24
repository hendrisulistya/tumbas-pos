package com.argomin.app.util

import org.junit.Assert.*
import org.junit.Test

class QrisParserTest {

    @Test
    fun testValidStaticQris() {
        val staticPayload = QrisConfig.STATIC_PAYLOAD
        val result = validateAndParseQris(staticPayload)

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
        assertEquals("RM ARGO MINANG 3 KEPEK, P", result.merchantName)
        assertEquals("ID1026469990109", result.nmid)
        assertEquals("KULON PROGO", result.city)
        assertEquals("A02", result.terminalId)
        assertFalse(result.isDynamic)
        assertEquals("1DE8", result.calculatedCrc)
    }

    @Test
    fun testInvalidCrcQris() {
        // Corrupt last 4 characters of CRC
        val corrupted = QrisConfig.STATIC_PAYLOAD.dropLast(4) + "0000"
        val result = validateAndParseQris(corrupted)

        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("CRC"))
    }

    @Test
    fun testInvalidHeader() {
        val invalid = "990201010211" + QrisConfig.STATIC_PAYLOAD.substring(12)
        val result = validateAndParseQris(invalid)

        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("000201"))
    }

    @Test
    fun testDynamicQrisGeneratedValidation() {
        val dynamic = generateDynamicQris(50000L, fee = 1000L)
        val result = validateAndParseQris(dynamic.payload)

        assertTrue(result.isValid)
        assertTrue(result.isDynamic)
        assertEquals("RM ARGO MINANG 3 KEPEK, P", result.merchantName)
        assertEquals("ID1026469990109", result.nmid)
    }
}
