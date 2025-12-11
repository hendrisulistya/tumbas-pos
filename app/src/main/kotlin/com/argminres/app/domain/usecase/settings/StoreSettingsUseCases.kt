package com.argminres.app.domain.usecase.settings

import com.argminres.app.data.local.entity.StoreSettingsEntity
import com.argminres.app.domain.repository.StoreSettingsRepository
import kotlinx.coroutines.flow.Flow

class GetStoreSettingsUseCase(
    private val repository: StoreSettingsRepository
) {
    operator fun invoke(): Flow<StoreSettingsEntity?> {
        return repository.getStoreSettings()
    }
}

class SaveStoreSettingsUseCase(
    private val repository: StoreSettingsRepository
) {
    suspend operator fun invoke(settings: StoreSettingsEntity) {
        repository.saveStoreSettings(settings)
    }
}
