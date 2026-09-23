package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.IngredientUsageDao
import com.argminres.app.data.local.entity.IngredientUsageEntity
import com.argminres.app.domain.repository.IngredientUsageRepository
import kotlinx.coroutines.flow.Flow

class IngredientUsageRepositoryImpl(
    private val ingredientUsageDao: IngredientUsageDao
) : IngredientUsageRepository {
    
    override fun getUsageBySession(sessionId: Long): Flow<List<IngredientUsageEntity>> =
        ingredientUsageDao.getUsageBySession(sessionId)
    
    override fun getRecentUsage(): Flow<List<IngredientUsageEntity>> =
        ingredientUsageDao.getRecentUsage()
    
    override suspend fun getTotalCostForSession(sessionId: Long): Double? =
        ingredientUsageDao.getTotalCostForSession(sessionId)
    
    override suspend fun createUsage(usage: IngredientUsageEntity): Long =
        ingredientUsageDao.insertUsage(usage)
    
    override suspend fun createUsageRecords(usageList: List<IngredientUsageEntity>) =
        ingredientUsageDao.insertAll(usageList)
    
    override suspend fun deleteUsageForSession(sessionId: Long) =
        ingredientUsageDao.deleteUsageForSession(sessionId)
}
