package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.DailySessionDao
import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.domain.repository.DailySessionRepository
import kotlinx.coroutines.flow.Flow

class DailySessionRepositoryImpl(
    private val dailySessionDao: DailySessionDao
) : DailySessionRepository {
    override fun getAllSessions(): Flow<List<DailySessionEntity>> = dailySessionDao.getAllSessions()
    
    override suspend fun getActiveSession(): DailySessionEntity? = dailySessionDao.getActiveSession()
    
    override suspend fun getSessionById(id: Long): DailySessionEntity? = dailySessionDao.getSessionById(id)
    
    override fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<DailySessionEntity>> =
        dailySessionDao.getSessionsByDateRange(startDate, endDate)
    
    override suspend fun createSession(session: DailySessionEntity): Long = dailySessionDao.insertSession(session)
    
    override suspend fun updateSession(session: DailySessionEntity) = dailySessionDao.updateSession(session)
    
    override suspend fun closeSession(sessionId: Long, closedAt: Long, totalSales: Double, totalWaste: Double, totalProfit: Double) =
        dailySessionDao.closeSession(sessionId, closedAt, totalSales, totalWaste, totalProfit)
}
