package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.AuditLogDao
import com.argminres.app.data.local.entity.AuditLogEntity
import com.argminres.app.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow

class AuditLogRepositoryImpl(
    private val auditLogDao: AuditLogDao
) : AuditLogRepository {
    
    override fun getAll(): Flow<List<AuditLogEntity>> = auditLogDao.getAll()
    
    override fun getByEmployer(employerId: Long): Flow<List<AuditLogEntity>> = 
        auditLogDao.getByEmployer(employerId)
    
    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLogEntity>> = 
        auditLogDao.getByDateRange(startDate, endDate)
    
    override fun getByAction(action: String): Flow<List<AuditLogEntity>> = 
        auditLogDao.getByAction(action)
    
    override fun getFiltered(
        employerId: Long?,
        action: String?,
        startDate: Long,
        endDate: Long
    ): Flow<List<AuditLogEntity>> = 
        auditLogDao.getFiltered(employerId, action, startDate, endDate)
    
    override suspend fun insert(log: AuditLogEntity): Long = auditLogDao.insert(log)
    
    override suspend fun deleteOlderThan(beforeDate: Long) = 
        auditLogDao.deleteOlderThan(beforeDate)
}
