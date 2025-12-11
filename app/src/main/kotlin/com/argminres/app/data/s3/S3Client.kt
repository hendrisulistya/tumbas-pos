package com.argminres.app.data.s3

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

/**
 * Lightweight S3 client for Cloudflare R2 / AWS S3 compatible storage
 * Uses OkHttp and AWS Signature V4 for authentication
 */
class S3Client(
    private val endpoint: String,
    private val accessKeyId: String,
    private val secretAccessKey: String,
    private val region: String = "auto"
) {
    private val client = OkHttpClient()
    
    /**
     * Upload an object to S3
     */
    suspend fun putObject(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String = "application/octet-stream"
    ): Result<Unit> {
        return try {
            val url = "$endpoint/$bucket/$key"
            val amzDate = AwsSignatureV4.getAmzDate()
            val payloadHash = AwsSignatureV4.sha256Hex(data)
            
            val headers = mapOf(
                "Host" to endpoint.removePrefix("https://").removePrefix("http://"),
                "x-amz-content-sha256" to payloadHash,
                "x-amz-date" to amzDate
            )
            
            val authorization = AwsSignatureV4.sign(
                method = "PUT",
                url = url,
                headers = headers,
                payload = data,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = region
            )
            
            val request = Request.Builder()
                .url(url)
                .put(data.toRequestBody(contentType.toMediaType()))
                .header("Authorization", authorization)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .header("Content-Type", contentType)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("S3 PUT failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload a file to S3
     */
    suspend fun putObject(
        bucket: String,
        key: String,
        file: File,
        contentType: String = "application/octet-stream"
    ): Result<Unit> {
        return putObject(bucket, key, file.readBytes(), contentType)
    }
    
    /**
     * Download an object from S3
     */
    suspend fun getObject(
        bucket: String,
        key: String
    ): Result<ByteArray> {
        return try {
            val url = "$endpoint/$bucket/$key"
            val amzDate = AwsSignatureV4.getAmzDate()
            val emptyPayload = ByteArray(0)
            val payloadHash = AwsSignatureV4.sha256Hex(emptyPayload)
            
            val headers = mapOf(
                "Host" to endpoint.removePrefix("https://").removePrefix("http://"),
                "x-amz-content-sha256" to payloadHash,
                "x-amz-date" to amzDate
            )
            
            val authorization = AwsSignatureV4.sign(
                method = "GET",
                url = url,
                headers = headers,
                payload = emptyPayload,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = region
            )
            
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null) {
                    Result.success(bytes)
                } else {
                    Result.failure(IOException("Empty response body"))
                }
            } else {
                Result.failure(IOException("S3 GET failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete an object from S3
     */
    suspend fun deleteObject(
        bucket: String,
        key: String
    ): Result<Unit> {
        return try {
            val url = "$endpoint/$bucket/$key"
            val amzDate = AwsSignatureV4.getAmzDate()
            val emptyPayload = ByteArray(0)
            val payloadHash = AwsSignatureV4.sha256Hex(emptyPayload)
            
            val headers = mapOf(
                "Host" to endpoint.removePrefix("https://").removePrefix("http://"),
                "x-amz-content-sha256" to payloadHash,
                "x-amz-date" to amzDate
            )
            
            val authorization = AwsSignatureV4.sign(
                method = "DELETE",
                url = url,
                headers = headers,
                payload = emptyPayload,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = region
            )
            
            val request = Request.Builder()
                .url(url)
                .delete()
                .header("Authorization", authorization)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 204) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("S3 DELETE failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * List objects in S3 with a prefix
     */
    suspend fun listObjects(
        bucket: String,
        prefix: String = ""
    ): Result<List<String>> {
        return try {
            val queryParams = if (prefix.isNotEmpty()) "?prefix=$prefix&list-type=2" else "?list-type=2"
            val url = "$endpoint/$bucket/$queryParams"
            val amzDate = AwsSignatureV4.getAmzDate()
            val emptyPayload = ByteArray(0)
            val payloadHash = AwsSignatureV4.sha256Hex(emptyPayload)
            
            val headers = mapOf(
                "Host" to endpoint.removePrefix("https://").removePrefix("http://"),
                "x-amz-content-sha256" to payloadHash,
                "x-amz-date" to amzDate
            )
            
            val authorization = AwsSignatureV4.sign(
                method = "GET",
                url = url,
                headers = headers,
                payload = emptyPayload,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = region
            )
            
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", authorization)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val xml = response.body?.string() ?: ""
                val keys = parseListObjectsResponse(xml)
                Result.success(keys)
            } else {
                Result.failure(IOException("S3 LIST failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Simple XML parser for ListObjectsV2 response
     */
    private fun parseListObjectsResponse(xml: String): List<String> {
        val keys = mutableListOf<String>()
        val keyPattern = Regex("<Key>([^<]+)</Key>")
        keyPattern.findAll(xml).forEach { match ->
            keys.add(match.groupValues[1])
        }
        return keys
    }
}
