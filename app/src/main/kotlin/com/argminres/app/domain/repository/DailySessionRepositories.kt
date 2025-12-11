package com.argminres.app.domain.repository

import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.data.local.entity.WasteRecordEntity
import kotlinx.coroutines.flow.Flow

interface DailySessionRepository {
    fun getAllSessions(): Flow<List<DailySessionEntity>>
    suspend fun getActiveSession(): DailySessionEntity?
    suspend fun getSessionById(id: Long): DailySessionEntity?
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<DailySessionEntity>>
    suspend fun createSession(session: DailySessionEntity): Long
    suspend fun updateSession(session: DailySessionEntity)
    suspend fun closeSession(sessionId: Long, closedAt: Long, totalSales: Double, totalWaste: Double, totalProfit: Double)
}

interface WasteRecordRepository {
    fun getWasteRecordsBySession(sessionId: Long): Flow<List<WasteRecordEntity>>
    fun getRecentWasteRecords(): Flow<List<WasteRecordEntity>>
    suspend fun getTotalWasteForSession(sessionId: Long): Double
    suspend fun createWasteRecord(wasteRecord: WasteRecordEntity): Long
    suspend fun createWasteRecords(wasteRecords: List<WasteRecordEntity>)
    suspend fun deleteWasteRecordsForSession(sessionId: Long)
}
