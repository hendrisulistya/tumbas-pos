package com.argminres.app.data.repository

import android.content.Context
import com.argminres.app.data.s3.S3Client
import com.argminres.app.domain.model.R2Config
import com.argminres.app.domain.repository.BackupRepository
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
            val s3Client = S3Client(
                endpoint = r2Config.endpointUrl,
                accessKeyId = r2Config.accessKeyId,
                secretAccessKey = r2Config.secretAccessKey,
                region = "auto"
            )
            
            val result = s3Client.putObject(
                bucket = r2Config.bucketName,
                key = "$appId/$backupFileName",
                file = backupFile
            )
            
            // Clean up local backup file
            backupFile.delete()

            result.fold(
                onSuccess = { Result.success(backupFileName) },
                onFailure = { e -> Result.failure(e) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreDatabase(r2Config: R2Config, backupFileName: String, namespace: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val appId = namespace ?: settingsRepository.getAppId()
            val s3Client = S3Client(
                endpoint = r2Config.endpointUrl,
                accessKeyId = r2Config.accessKeyId,
                secretAccessKey = r2Config.secretAccessKey,
                region = "auto"
            )

            val tempFile = File(context.cacheDir, "restore_temp.db")
            
            val result = s3Client.getObject(
                bucket = r2Config.bucketName,
                key = "$appId/$backupFileName"
            )
            
            result.fold(
                onSuccess = { bytes ->
                    tempFile.writeBytes(bytes)
                    
                    // Validate the downloaded file (basic check)
                    if (!tempFile.exists() || tempFile.length() == 0L) {
                        throw Exception("Downloaded backup file is invalid")
                    }

                    // Replace current database
                    val dbFile = context.getDatabasePath(dbName)
                    
                    if (dbFile.exists()) {
                        dbFile.delete()
                    }
                    tempFile.copyTo(dbFile, overwrite = true)
                    tempFile.delete()

                    Result.success(Unit)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBackups(r2Config: R2Config, namespace: String?): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val appId = namespace ?: settingsRepository.getAppId()
            val s3Client = S3Client(
                endpoint = r2Config.endpointUrl,
                accessKeyId = r2Config.accessKeyId,
                secretAccessKey = r2Config.secretAccessKey,
                region = "auto"
            )
            
            val result = s3Client.listObjects(
                bucket = r2Config.bucketName,
                prefix = "$appId/"
            )
            
            result.fold(
                onSuccess = { keys ->
                    // Strip the appId prefix from the keys for display
                    val backups = keys
                        .map { it.removePrefix("$appId/") }
                        .filter { it.endsWith(".db") }
                        .sortedDescending()
                    Result.success(backups)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
