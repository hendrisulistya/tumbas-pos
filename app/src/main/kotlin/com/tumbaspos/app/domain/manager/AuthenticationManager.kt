package com.tumbaspos.app.domain.manager

import com.tumbaspos.app.data.local.entity.EmployerEntity
import com.tumbaspos.app.domain.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class AuthenticationManager(
    private val employerRepository: EmployerRepository,
    private val sessionManager: com.tumbaspos.app.data.local.SessionManager,
    private val auditLogger: com.tumbaspos.app.domain.manager.AuditLogger
) {
    private val _currentEmployer = MutableStateFlow<EmployerEntity?>(null)
    val currentEmployer: StateFlow<EmployerEntity?> = _currentEmployer.asStateFlow()
    
    val isLoggedIn: Boolean
        get() = _currentEmployer.value != null
    
    suspend fun login(pin: String): Result<EmployerEntity> {
        return try {
            // Get all employers and find one with matching PIN
            val employers = employerRepository.getAll().first()
            val employer = employers.find { employer ->
                com.tumbaspos.app.util.PinHasher.verifyPin(pin, employer.pin)
            }
            
            if (employer != null) {
                _currentEmployer.value = employer
                // Save session
                sessionManager.saveEmployerId(employer.id)
                // Log login
                auditLogger.logAsync { auditLogger.logLogin(employer.fullName) }
                Result.success(employer)
            } else {
                Result.failure(Exception("Invalid PIN"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        val employer = _currentEmployer.value
        _currentEmployer.value = null
        // Clear session
        sessionManager.clearSession()
        // Log logout
        if (employer != null) {
            auditLogger.logAsync { auditLogger.logLogout(employer.fullName, employer.id) }
        }
    }
    
    /**
     * Restore session from saved employer ID
     */
    suspend fun restoreSession(): Boolean {
        return try {
            val savedEmployerId = sessionManager.getEmployerId().first()
            if (savedEmployerId != null) {
                val employer = employerRepository.getById(savedEmployerId)
                if (employer != null) {
                    _currentEmployer.value = employer
                    true
                } else {
                    // Employer not found, clear invalid session
                    sessionManager.clearSession()
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getCurrentEmployer(): EmployerEntity? = _currentEmployer.value
    
    fun isManager(): Boolean = _currentEmployer.value?.role == "MANAGER"
    
    fun isCashier(): Boolean = _currentEmployer.value?.role == "CASHIER"
}
