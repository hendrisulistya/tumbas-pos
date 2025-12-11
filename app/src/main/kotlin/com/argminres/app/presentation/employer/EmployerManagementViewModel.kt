package com.argminres.app.presentation.employer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.EmployerEntity
import com.argminres.app.domain.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmployerManagementUiState(
    val employers: List<EmployerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDialogOpen: Boolean = false,
    val editingEmployer: EmployerEntity? = null,
    val isChangePinDialogOpen: Boolean = false,
    val pinChangeSuccess: Boolean = false,
    val pinChangeError: String? = null
)

class EmployerManagementViewModel(
    private val employerRepository: EmployerRepository,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger,
    private val authManager: com.argminres.app.domain.manager.AuthenticationManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmployerManagementUiState())
    val uiState: StateFlow<EmployerManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadEmployers()
    }
    
    private fun loadEmployers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            employerRepository.getAll().collect { employers ->
                _uiState.update { it.copy(employers = employers, isLoading = false) }
            }
        }
    }
    
    fun onAddEmployerClick() {
        _uiState.update { it.copy(isDialogOpen = true, editingEmployer = null) }
    }
    
    fun onEditEmployerClick(employer: EmployerEntity) {
        _uiState.update { it.copy(isDialogOpen = true, editingEmployer = employer) }
    }
    
    fun onDismissDialog() {
        _uiState.update { it.copy(isDialogOpen = false, editingEmployer = null) }
    }
    
    fun onSaveEmployer(employer: EmployerEntity) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Validate PIN
                if (employer.pin.length != 4 || !employer.pin.all { it.isDigit() }) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "PIN must be exactly 4 digits"
                        ) 
                    }
                    return@launch
                }
                
                val editingEmployer = _uiState.value.editingEmployer
                val employerId = if (editingEmployer == null) {
                    employerRepository.insert(employer)
                } else {
                    employerRepository.update(employer)
                    employer.id
                }
                
                // Log audit trail
                auditLogger.logAsync {
                    if (editingEmployer == null) {
                        auditLogger.logCreate("EMPLOYEE", employerId, "Name: ${employer.fullName}, Role: ${employer.role}")
                    } else {
                        auditLogger.logUpdate("EMPLOYEE", employerId, "Name: ${employer.fullName}, Role: ${employer.role}")
                    }
                }
                
                _uiState.update { it.copy(isDialogOpen = false, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to save employer"
                    ) 
                }
            }
        }
    }
    
    fun deleteEmployer(employer: EmployerEntity) {
        viewModelScope.launch {
            try {
                employerRepository.delete(employer)
                
                // Log audit trail
                auditLogger.logAsync {
                    auditLogger.logDelete("EMPLOYEE", employer.id, "Name: ${employer.fullName}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete employer") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // PIN Change functionality
    fun onChangePinClick() {
        _uiState.update { it.copy(isChangePinDialogOpen = true, pinChangeError = null, pinChangeSuccess = false) }
    }

    fun onDismissChangePinDialog() {
        _uiState.update { it.copy(isChangePinDialogOpen = false, pinChangeError = null, pinChangeSuccess = false) }
    }

    fun changePin(oldPin: String, newPin: String) {
        viewModelScope.launch {
            try {
                val currentEmployer = authManager.getCurrentEmployer()
                if (currentEmployer == null) {
                    _uiState.update { it.copy(pinChangeError = "No user logged in") }
                    return@launch
                }

                // Verify old PIN
                if (!com.argminres.app.util.PinHasher.verifyPin(oldPin, currentEmployer.pin)) {
                    _uiState.update { it.copy(pinChangeError = "Current PIN is incorrect") }
                    return@launch
                }

                // Hash new PIN
                val hashedNewPin = com.argminres.app.util.PinHasher.hashPin(newPin)

                // Update employer with new PIN
                val updatedEmployer = currentEmployer.copy(pin = hashedNewPin)
                employerRepository.update(updatedEmployer)

                // Update session - re-login with new PIN
                authManager.login(newPin)

                // Log audit trail
                auditLogger.logAsync {
                    auditLogger.logUpdate(
                        "EMPLOYEE",
                        currentEmployer.id,
                        "PIN changed for: ${currentEmployer.fullName}"
                    )
                }

                _uiState.update { 
                    it.copy(
                        pinChangeSuccess = true,
                        pinChangeError = null,
                        isChangePinDialogOpen = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(pinChangeError = e.message ?: "Failed to change PIN") }
            }
        }
    }
}
