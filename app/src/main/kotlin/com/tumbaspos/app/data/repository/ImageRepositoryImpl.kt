package com.tumbaspos.app.data.repository

import android.util.Base64
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import com.tumbaspos.app.domain.model.R2Config
import com.tumbaspos.app.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepositoryImpl : ImageRepository {
    
    override suspend fun uploadProductImage(
        imageData: ByteArray,
        fileName: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // If R2 config is available, upload to cloud
            if (r2Config != null) {
                val s3Client = getS3Client(r2Config)
                val key = "$namespace/product/$fileName"
                
                val request = PutObjectRequest {
                    bucket = r2Config.bucketName
                    this.key = key
                    body = ByteStream.fromBytes(imageData)
                    contentType = "image/jpeg"
                }
                
                s3Client.putObject(request)
                
                // Return the R2 URL
                val url = "${r2Config.endpointUrl}/${r2Config.bucketName}/$key"
                Result.success(url)
            } else {
                // Store as base64 if no R2 config
                val base64 = "data:image/jpeg;base64," + Base64.encodeToString(imageData, Base64.NO_WRAP)
                Result.success(base64)
            }
        } catch (e: Exception) {
            // Fallback to base64 on error
            try {
                val base64 = "data:image/jpeg;base64," + Base64.encodeToString(imageData, Base64.NO_WRAP)
                Result.success(base64)
            } catch (e2: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun deleteProductImage(
        image: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Only delete from R2 if it's a URL (not base64)
            if (r2Config != null && !image.startsWith("data:")) {
                val s3Client = getS3Client(r2Config)
                
                // Extract key from URL
                val key = image.substringAfter("${r2Config.bucketName}/")
                
                val request = DeleteObjectRequest {
                    bucket = r2Config.bucketName
                    this.key = key
                }
                
                s3Client.deleteObject(request)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Ignore deletion errors for base64 images
            Result.success(Unit)
        }
    }
    
    private fun getS3Client(r2Config: R2Config): S3Client {
        return S3Client {
            region = "auto"
            endpointUrl = Url.parse(r2Config.endpointUrl)
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = r2Config.accessKeyId
                secretAccessKey = r2Config.secretAccessKey
            }
        }
    }
}
