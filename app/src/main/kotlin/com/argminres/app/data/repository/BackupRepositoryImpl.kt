package com.argminres.app.data.repository

import android.content.Context
import com.argminres.app.BuildConfig
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
    private val dbName: String = "padang_pos_db"
) : BackupRepository {

    private val backupDir: File by lazy {
        File(context.filesDir, "backups").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    override suspend fun backupDatabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // Create a backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "backup_$timestamp.db"
            val backupFile = File(backupDir, backupFileName)
            
            dbFile.copyTo(backupFile, overwrite = true)

            if (backupFile.exists()) {
                Result.success(backupFileName)
            } else {
                Result.failure(Exception("Failed to create local backup file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreDatabase(backupFileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupDir, backupFileName)
            
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("Backup file not found: $backupFileName"))
            }

            // Replace current database
            val dbFile = context.getDatabasePath(dbName)
            
            // It's safer to close the database before replacing, 
            // but usually Room handles it or the app requires a restart anyway.
            // For simplicity and matching previous logic, we just copy.
            if (dbFile.exists()) {
                dbFile.delete()
            }
            backupFile.copyTo(dbFile, overwrite = true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBackups(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val backups = backupDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".db") }
                ?.map { it.name }
                ?.sortedDescending()
                ?: emptyList()
            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
