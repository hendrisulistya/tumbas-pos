package com.argminres.app.domain.repository

import com.argminres.app.data.local.entity.DishHistoryEntity
import kotlinx.coroutines.flow.Flow

interface DishHistoryRepository {
    fun getHistoryForSession(date: String): Flow<List<DishHistoryEntity>>
    fun getHistoryForDish(dishId: Long): Flow<List<DishHistoryEntity>>
    suspend fun insertHistory(history: DishHistoryEntity)
    fun getAllSessionDates(): Flow<List<String>>
    fun getRecentHistory(limit: Int = 50): Flow<List<DishHistoryEntity>>
}
