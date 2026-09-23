package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.StoreSettingsDao
import com.argminres.app.data.local.entity.StoreSettingsEntity
import com.argminres.app.domain.repository.StoreSettingsRepository
import kotlinx.coroutines.flow.Flow

class StoreSettingsRepositoryImpl(
    private val storeSettingsDao: StoreSettingsDao
) : StoreSettingsRepository {
    
    override fun getStoreSettings(): Flow<StoreSettingsEntity?> {
        return storeSettingsDao.getStoreSettings()
    }
    
    override suspend fun saveStoreSettings(settings: StoreSettingsEntity) {
        storeSettingsDao.insertOrUpdate(settings)
    }
}
