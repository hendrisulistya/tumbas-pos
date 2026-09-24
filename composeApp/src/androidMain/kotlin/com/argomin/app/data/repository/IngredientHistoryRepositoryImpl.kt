package com.argomin.app.data.repository

import com.argomin.app.data.local.dao.IngredientHistoryDao
import com.argomin.app.data.local.entity.IngredientHistoryEntity
import com.argomin.app.domain.repository.IngredientHistoryRepository
import kotlinx.coroutines.flow.Flow

class IngredientHistoryRepositoryImpl(
    private val ingredientHistoryDao: IngredientHistoryDao
) : IngredientHistoryRepository {
    
    override fun getHistoryForSession(date: String): Flow<List<IngredientHistoryEntity>> {
        return ingredientHistoryDao.getHistoryForSession(date)
    }
    
    override fun getHistoryForIngredient(ingredientId: Long): Flow<List<IngredientHistoryEntity>> {
        return ingredientHistoryDao.getHistoryForIngredient(ingredientId)
    }
    
    override suspend fun insertHistory(history: IngredientHistoryEntity) {
        ingredientHistoryDao.insertHistory(history)
    }
    
    override fun getAllSessionDates(): Flow<List<String>> {
        return ingredientHistoryDao.getAllSessionDates()
    }
    
    override fun getRecentHistory(limit: Int): Flow<List<IngredientHistoryEntity>> {
        return ingredientHistoryDao.getRecentHistory(limit)
    }
}
