package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.DishHistoryDao
import com.argminres.app.data.local.entity.DishHistoryEntity
import com.argminres.app.domain.repository.DishHistoryRepository
import kotlinx.coroutines.flow.Flow

class DishHistoryRepositoryImpl(
    private val dishHistoryDao: DishHistoryDao
) : DishHistoryRepository {
    
    override fun getHistoryForSession(date: String): Flow<List<DishHistoryEntity>> {
        return dishHistoryDao.getHistoryForSession(date)
    }
    
    override fun getHistoryForDish(dishId: Long): Flow<List<DishHistoryEntity>> {
        return dishHistoryDao.getHistoryForDish(dishId)
    }
    
    override suspend fun insertHistory(history: DishHistoryEntity) {
        dishHistoryDao.insertHistory(history)
    }
    
    override fun getAllSessionDates(): Flow<List<String>> {
        return dishHistoryDao.getAllSessionDates()
    }
    
    override fun getRecentHistory(limit: Int): Flow<List<DishHistoryEntity>> {
        return dishHistoryDao.getRecentHistory(limit)
    }
}
