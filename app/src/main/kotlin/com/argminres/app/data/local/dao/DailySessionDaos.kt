package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.data.local.entity.WasteRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySessionDao {
    @Query("SELECT * FROM daily_sessions ORDER BY sessionDate DESC")
    fun getAllSessions(): Flow<List<DailySessionEntity>>

    @Query("SELECT * FROM daily_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): DailySessionEntity?

    @Query("SELECT * FROM daily_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): DailySessionEntity?

    @Query("SELECT * FROM daily_sessions WHERE sessionDate >= :startDate AND sessionDate <= :endDate ORDER BY sessionDate DESC")
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<DailySessionEntity>>

    @Insert
    suspend fun insertSession(session: DailySessionEntity): Long

    @Update
    suspend fun updateSession(session: DailySessionEntity)

    @Query("UPDATE daily_sessions SET status = 'CLOSED', closedAt = :closedAt, totalSales = :totalSales, totalWaste = :totalWaste, totalProfit = :totalProfit WHERE id = :sessionId")
    suspend fun closeSession(sessionId: Long, closedAt: Long, totalSales: Double, totalWaste: Double, totalProfit: Double)
}

@Dao
interface WasteRecordDao {
    @Query("SELECT * FROM waste_records WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun getWasteRecordsBySession(sessionId: Long): Flow<List<WasteRecordEntity>>

    @Query("SELECT * FROM waste_records ORDER BY createdAt DESC LIMIT 100")
    fun getRecentWasteRecords(): Flow<List<WasteRecordEntity>>

    @Query("SELECT SUM(totalLoss) FROM waste_records WHERE sessionId = :sessionId")
    suspend fun getTotalWasteForSession(sessionId: Long): Double?

    @Insert
    suspend fun insertWasteRecord(wasteRecord: WasteRecordEntity): Long

    @Insert
    suspend fun insertAll(wasteRecords: List<WasteRecordEntity>)

    @Query("DELETE FROM waste_records WHERE sessionId = :sessionId")
    suspend fun deleteWasteRecordsForSession(sessionId: Long)
}
