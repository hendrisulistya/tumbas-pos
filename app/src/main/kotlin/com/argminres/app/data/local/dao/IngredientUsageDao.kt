package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.IngredientUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientUsageDao {
    @Query("SELECT * FROM ingredient_usage WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun getUsageBySession(sessionId: Long): Flow<List<IngredientUsageEntity>>

    @Query("SELECT * FROM ingredient_usage ORDER BY createdAt DESC LIMIT 100")
    fun getRecentUsage(): Flow<List<IngredientUsageEntity>>

    @Query("SELECT SUM(totalCost) FROM ingredient_usage WHERE sessionId = :sessionId")
    suspend fun getTotalCostForSession(sessionId: Long): Double?

    @Insert
    suspend fun insertUsage(usage: IngredientUsageEntity): Long

    @Insert
    suspend fun insertAll(usageList: List<IngredientUsageEntity>)

    @Query("DELETE FROM ingredient_usage WHERE sessionId = :sessionId")
    suspend fun deleteUsageForSession(sessionId: Long)
}
