package com.tumbaspos.app.data.s3

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import java.util.*

/**
 * AWS Signature Version 4 signing helper for S3 requests
 */
object AwsSignatureV4 {
    
    private const val ALGORITHM = "AWS4-HMAC-SHA256"
    private const val SERVICE = "s3"
    
    fun sign(
        method: String,
        url: String,
        headers: Map<String, String>,
        payload: ByteArray,
        accessKeyId: String,
        secretAccessKey: String,
        region: String
    ): String {
        val now = Date()
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(now)
        val amzDate = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(now)
        
        // Parse URL to get host and path
        val uri = java.net.URI(url)
        val host = uri.host
        val path = uri.path.ifEmpty { "/" }
        val query = uri.query ?: ""
        
        // Create canonical request
        val payloadHash = sha256Hex(payload)
        val canonicalHeaders = "host:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$amzDate\n"
        val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
        
        val canonicalRequest = buildString {
            append(method)
            append("\n")
            append(path)
            append("\n")
            append(query)
            append("\n")
            append(canonicalHeaders)
            append("\n")
            append(signedHeaders)
            append("\n")
            append(payloadHash)
        }
        
        // Create string to sign
        val credentialScope = "$dateStamp/$region/$SERVICE/aws4_request"
        val stringToSign = buildString {
            append(ALGORITHM)
            append("\n")
            append(amzDate)
            append("\n")
            append(credentialScope)
            append("\n")
            append(sha256Hex(canonicalRequest.toByteArray(StandardCharsets.UTF_8)))
        }
        
        // Calculate signature
        val signingKey = getSignatureKey(secretAccessKey, dateStamp, region, SERVICE)
        val signature = hmacSha256Hex(stringToSign, signingKey)
        
        // Build authorization header
        return "$ALGORITHM Credential=$accessKeyId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
    }
    
    fun getAmzDate(): String {
        return SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }
    
    private fun getSignatureKey(key: String, dateStamp: String, region: String, service: String): ByteArray {
        val kDate = hmacSha256("AWS4$key".toByteArray(StandardCharsets.UTF_8), dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }
    
    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }
    
    private fun hmacSha256Hex(data: String, key: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return bytesToHex(mac.doFinal(data.toByteArray(StandardCharsets.UTF_8)))
    }
    
    fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return bytesToHex(digest.digest(data))
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
