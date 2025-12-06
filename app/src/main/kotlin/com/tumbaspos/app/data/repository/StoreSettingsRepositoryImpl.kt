package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.StoreSettingsDao
import com.tumbaspos.app.data.local.entity.StoreSettingsEntity
import com.tumbaspos.app.domain.repository.StoreSettingsRepository
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
