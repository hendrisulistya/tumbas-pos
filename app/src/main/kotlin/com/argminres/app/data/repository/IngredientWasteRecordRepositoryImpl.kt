package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.IngredientWasteRecordDao
import com.argminres.app.data.local.entity.IngredientWasteRecordEntity
import com.argminres.app.domain.repository.IngredientWasteRecordRepository
import kotlinx.coroutines.flow.Flow

class IngredientWasteRecordRepositoryImpl(
    private val ingredientWasteRecordDao: IngredientWasteRecordDao
) : IngredientWasteRecordRepository {

    override fun getWasteRecordsBySession(sessionId: Long): Flow<List<IngredientWasteRecordEntity>> {
        return ingredientWasteRecordDao.getWasteRecordsBySession(sessionId)
    }

    override suspend fun insertWasteRecord(record: IngredientWasteRecordEntity): Long {
        return ingredientWasteRecordDao.insertWasteRecord(record)
    }

    override suspend fun deleteWasteRecordsBySession(sessionId: Long) {
        ingredientWasteRecordDao.deleteWasteRecordsBySession(sessionId)
    }
}
