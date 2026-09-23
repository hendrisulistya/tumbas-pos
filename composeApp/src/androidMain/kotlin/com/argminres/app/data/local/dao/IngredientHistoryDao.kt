package com.argminres.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.argminres.app.data.local.entity.IngredientHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientHistoryDao {
    @Query("SELECT * FROM ingredient_history WHERE sessionDate = :date ORDER BY timestamp DESC")
    fun getHistoryForSession(date: String): Flow<List<IngredientHistoryEntity>>
    
    @Query("SELECT * FROM ingredient_history WHERE ingredientId = :ingredientId ORDER BY timestamp DESC")
    fun getHistoryForIngredient(ingredientId: Long): Flow<List<IngredientHistoryEntity>>
    
    @Insert
    suspend fun insertHistory(history: IngredientHistoryEntity)
    
    @Query("SELECT DISTINCT sessionDate FROM ingredient_history ORDER BY sessionDate DESC")
    fun getAllSessionDates(): Flow<List<String>>
    
    @Query("SELECT * FROM ingredient_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<IngredientHistoryEntity>>
}
