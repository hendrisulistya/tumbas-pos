package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAll(): Flow<List<AuditLogEntity>>
    fun getByEmployer(employerId: Long): Flow<List<AuditLogEntity>>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLogEntity>>
    fun getByAction(action: String): Flow<List<AuditLogEntity>>
    fun getFiltered(
        employerId: Long?,
        action: String?,
        startDate: Long,
        endDate: Long
    ): Flow<List<AuditLogEntity>>
    suspend fun insert(log: AuditLogEntity): Long
    suspend fun deleteOlderThan(beforeDate: Long)
}
