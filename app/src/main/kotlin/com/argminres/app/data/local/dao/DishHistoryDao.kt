package com.argminres.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.argminres.app.data.local.entity.DishHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishHistoryDao {
    @Query("SELECT * FROM dish_history WHERE sessionDate = :date ORDER BY timestamp DESC")
    fun getHistoryForSession(date: String): Flow<List<DishHistoryEntity>>
    
    @Query("SELECT * FROM dish_history WHERE dishId = :dishId ORDER BY timestamp DESC")
    fun getHistoryForDish(dishId: Long): Flow<List<DishHistoryEntity>>
    
    @Insert
    suspend fun insertHistory(history: DishHistoryEntity)
    
    @Query("SELECT DISTINCT sessionDate FROM dish_history ORDER BY sessionDate DESC")
    fun getAllSessionDates(): Flow<List<String>>
    
    @Query("SELECT * FROM dish_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<DishHistoryEntity>>
}
