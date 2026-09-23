package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_logs WHERE employerId = :employerId ORDER BY timestamp DESC")
    fun getByEmployer(employerId: Long): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_logs WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY timestamp DESC")
    fun getByAction(action: String): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:employerId IS NULL OR employerId = :employerId)
          AND (:action IS NULL OR action = :action)
          AND timestamp >= :startDate 
          AND timestamp <= :endDate
        ORDER BY timestamp DESC
    """)
    fun getFiltered(
        employerId: Long?,
        action: String?,
        startDate: Long,
        endDate: Long
    ): Flow<List<AuditLogEntity>>
    
    @Insert
    suspend fun insert(log: AuditLogEntity): Long
    
    @Query("DELETE FROM audit_logs WHERE timestamp < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: Long)
}
