package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.WasteRecordDao
import com.argminres.app.data.local.entity.WasteRecordEntity
import com.argminres.app.domain.repository.WasteRecordRepository
import kotlinx.coroutines.flow.Flow

class WasteRecordRepositoryImpl(
    private val wasteRecordDao: WasteRecordDao
) : WasteRecordRepository {
    override fun getWasteRecordsBySession(sessionId: Long): Flow<List<WasteRecordEntity>> =
        wasteRecordDao.getWasteRecordsBySession(sessionId)
    
    override fun getRecentWasteRecords(): Flow<List<WasteRecordEntity>> =
        wasteRecordDao.getRecentWasteRecords()
    
    override suspend fun getTotalWasteForSession(sessionId: Long): Double =
        wasteRecordDao.getTotalWasteForSession(sessionId) ?: 0.0
    
    override suspend fun createWasteRecord(wasteRecord: WasteRecordEntity): Long =
        wasteRecordDao.insertWasteRecord(wasteRecord)
    
    override suspend fun createWasteRecords(wasteRecords: List<WasteRecordEntity>) =
        wasteRecordDao.insertAll(wasteRecords)
    
    override suspend fun deleteWasteRecordsForSession(sessionId: Long) =
        wasteRecordDao.deleteWasteRecordsForSession(sessionId)
}
