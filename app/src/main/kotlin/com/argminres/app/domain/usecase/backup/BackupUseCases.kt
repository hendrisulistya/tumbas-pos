package com.argminres.app.domain.usecase.backup

import com.argminres.app.domain.model.R2Config
import com.argminres.app.domain.repository.BackupRepository

class BackupDatabaseUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(r2Config: R2Config): Result<String> {
        return backupRepository.backupDatabase(r2Config)
    }
}

class RestoreDatabaseUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(r2Config: R2Config, backupFileName: String, namespace: String? = null): Result<Unit> {
        return backupRepository.restoreDatabase(r2Config, backupFileName, namespace)
    }
}

class GetBackupsUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(r2Config: R2Config, namespace: String? = null): Result<List<String>> {
        return backupRepository.getBackups(r2Config, namespace)
    }
}
