package com.argminres.app.domain.usecase.backup

import com.argminres.app.domain.repository.BackupRepository

class BackupDatabaseUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<String> {
        return backupRepository.backupDatabase()
    }
}

class RestoreDatabaseUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(backupFileName: String): Result<Unit> {
        return backupRepository.restoreDatabase(backupFileName)
    }
}

class GetBackupsUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return backupRepository.getBackups()
    }
}
