package com.argomin.app.domain.repository

import com.argomin.app.data.local.entity.IngredientUsageEntity
import kotlinx.coroutines.flow.Flow

interface IngredientUsageRepository {
    fun getUsageBySession(sessionId: Long): Flow<List<IngredientUsageEntity>>
    fun getRecentUsage(): Flow<List<IngredientUsageEntity>>
    suspend fun getTotalCostForSession(sessionId: Long): Double?
    suspend fun createUsage(usage: IngredientUsageEntity): Long
    suspend fun createUsageRecords(usageList: List<IngredientUsageEntity>)
    suspend fun deleteUsageForSession(sessionId: Long)
}
