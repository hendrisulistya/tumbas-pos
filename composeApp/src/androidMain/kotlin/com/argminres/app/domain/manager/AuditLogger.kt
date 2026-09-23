package com.argminres.app.domain.manager

import com.argminres.app.data.local.entity.AuditLogEntity
import com.argminres.app.domain.repository.AuditLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centralized audit logging service
 */
class AuditLogger(
    private val auditLogRepository: AuditLogRepository,
    private val authManager: Lazy<AuthenticationManager>,
    private val scope: CoroutineScope
) {
    
    suspend fun logLogin(employerName: String) {
        val employer = authManager.value.getCurrentEmployer()
        if (employer != null) {
            val log = AuditLogEntity(
                employerId = employer.id,
                employerName = employerName,
                action = "LOGIN",
                entityType = "SESSION",
                entityId = null,
                details = null
            )
            auditLogRepository.insert(log)
        }
    }
    
    suspend fun logLogout(employerName: String, employerId: Long) {
        val log = AuditLogEntity(
            employerId = employerId,
            employerName = employerName,
            action = "LOGOUT",
            entityType = "SESSION",
            entityId = null,
            details = null
        )
        auditLogRepository.insert(log)
    }
    
    suspend fun logCreate(entityType: String, entityId: Long, details: String? = null) {
        val employer = authManager.value.getCurrentEmployer()
        if (employer != null) {
            val log = AuditLogEntity(
                employerId = employer.id,
                employerName = employer.fullName,
                action = "CREATE",
                entityType = entityType,
                entityId = entityId,
                details = details
            )
            auditLogRepository.insert(log)
        }
    }
    
    suspend fun logUpdate(entityType: String, entityId: Long, details: String? = null) {
        val employer = authManager.value.getCurrentEmployer()
        if (employer != null) {
            val log = AuditLogEntity(
                employerId = employer.id,
                employerName = employer.fullName,
                action = "UPDATE",
                entityType = entityType,
                entityId = entityId,
                details = details
            )
            auditLogRepository.insert(log)
        }
    }
    
    suspend fun logDelete(entityType: String, entityId: Long, details: String? = null) {
        val employer = authManager.value.getCurrentEmployer()
        if (employer != null) {
            val log = AuditLogEntity(
                employerId = employer.id,
                employerName = employer.fullName,
                action = "DELETE",
                entityType = entityType,
                entityId = entityId,
                details = details
            )
            auditLogRepository.insert(log)
        }
    }
    
    /**
     * Log action asynchronously (fire and forget)
     */
    fun logAsync(action: suspend () -> Unit) {
        scope.launch {
            try {
                action()
            } catch (e: Exception) {
                android.util.Log.e("AuditLogger", "Error logging action", e)
            }
        }
    }
}
