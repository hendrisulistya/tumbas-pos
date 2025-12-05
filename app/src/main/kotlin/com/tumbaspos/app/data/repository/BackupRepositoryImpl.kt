package com.tumbaspos.app.data.repository

import android.content.Context
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.net.url.Url
import com.tumbaspos.app.domain.model.R2Config
import com.tumbaspos.app.domain.repository.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRepositoryImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val dbName: String = "tumbas_pos.db"
) : BackupRepository {

    override suspend fun backupDatabase(r2Config: R2Config): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // Create a backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "backup_$timestamp.db"
            val backupFile = File(context.cacheDir, backupFileName)
            dbFile.copyTo(backupFile, overwrite = true)

            // Upload to R2 with App ID namespace
            val appId = settingsRepository.getAppId()
            val s3Client = getS3Client(r2Config)
            val request = PutObjectRequest {
                bucket = r2Config.bucketName
                key = "$appId/$backupFileName"
                body = ByteStream.fromFile(backupFile)
            }
            s3Client.putObject(request)
            
            // Clean up local backup file
            backupFile.delete()

            Result.success(backupFileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreDatabase(r2Config: R2Config, backupFileName: String, namespace: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val appId = namespace ?: settingsRepository.getAppId()
            val s3Client = getS3Client(r2Config)
            val request = GetObjectRequest {
                bucket = r2Config.bucketName
                key = "$appId/$backupFileName"
            }

            val tempFile = File(context.cacheDir, "restore_temp.db")
            
            s3Client.getObject(request) { response ->
                val body = response.body
                if (body != null) {
                    body.writeToFile(tempFile)
                } else {
                    throw Exception("Empty response body")
                }
            }

            // Validate the downloaded file (basic check)
            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw Exception("Downloaded backup file is invalid")
            }

            // Replace current database
            val dbFile = context.getDatabasePath(dbName)
            
            // Close database connections if possible (in a real app, we might need to close the AppDatabase instance first)
            // For this implementation, we assume the user accepts a restart or we handle it via the ViewModel
            
            if (dbFile.exists()) {
                dbFile.delete()
            }
            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBackups(r2Config: R2Config, namespace: String?): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val appId = namespace ?: settingsRepository.getAppId()
            val s3Client = getS3Client(r2Config)
            val request = ListObjectsV2Request {
                bucket = r2Config.bucketName
                prefix = "$appId/"
            }
            
            val response = s3Client.listObjectsV2(request)
            // Strip the appId prefix from the keys for display
            val backups = response.contents?.mapNotNull { obj ->
                obj.key?.removePrefix("$appId/")
            }?.filter { it.endsWith(".db") }?.sortedDescending() ?: emptyList()
            
            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
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
