package com.argminres.app.domain.repository

import com.argminres.app.data.local.entity.IngredientHistoryEntity
import kotlinx.coroutines.flow.Flow

interface IngredientHistoryRepository {
    fun getHistoryForSession(date: String): Flow<List<IngredientHistoryEntity>>
    fun getHistoryForIngredient(ingredientId: Long): Flow<List<IngredientHistoryEntity>>
    suspend fun insertHistory(history: IngredientHistoryEntity)
    fun getAllSessionDates(): Flow<List<String>>
    fun getRecentHistory(limit: Int = 50): Flow<List<IngredientHistoryEntity>>
}
