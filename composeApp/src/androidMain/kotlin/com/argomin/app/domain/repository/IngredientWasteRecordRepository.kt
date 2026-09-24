package com.argomin.app.domain.repository

import com.argomin.app.data.local.entity.IngredientWasteRecordEntity
import kotlinx.coroutines.flow.Flow

interface IngredientWasteRecordRepository {
    fun getWasteRecordsBySession(sessionId: Long): Flow<List<IngredientWasteRecordEntity>>
    suspend fun insertWasteRecord(record: IngredientWasteRecordEntity): Long
    suspend fun deleteWasteRecordsBySession(sessionId: Long)
}
