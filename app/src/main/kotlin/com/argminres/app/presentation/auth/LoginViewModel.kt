package com.argminres.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.EmployerEntity
import com.argminres.app.domain.manager.AuthenticationManager
import com.argminres.app.domain.repository.EmployerRepository
import com.argminres.app.util.PinHasher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val employers: List<EmployerEntity> = emptyList(),
    val selectedEmployer: EmployerEntity? = null,
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class LoginViewModel(
    private val authManager: AuthenticationManager,
    private val employerRepository: EmployerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        // Load employers list
        loadEmployers()
    }
    
    private fun loadEmployers() {
        viewModelScope.launch {
            val employers = employerRepository.getAll().first()
            _uiState.update { it.copy(employers = employers, selectedEmployer = employers.firstOrNull()) }
        }
    }
    
    fun onEmployerSelected(employer: EmployerEntity) {
        _uiState.update { it.copy(selectedEmployer = employer, pin = "", error = null) }
    }
    
    fun onPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _uiState.update { it.copy(pin = pin, error = null) }
            
            // Auto-login when 4 digits entered
            if (pin.length == 4) {
                login()
            }
        }
    }
    
    fun login() {
        val pin = _uiState.value.pin
        val selectedEmployer = _uiState.value.selectedEmployer
        
        if (selectedEmployer == null) {
            _uiState.update { it.copy(error = "Please select an employee") }
            return
        }
        
        if (pin.length != 4) {
            _uiState.update { it.copy(error = "PIN must be 4 digits") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Verify PIN for selected employee
            val isValid = PinHasher.verifyPin(pin, selectedEmployer.pin)
            
            if (isValid) {
                // Login with the selected employee
                authManager.login(pin).fold(
                    onSuccess = { employer ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Login failed"
                            )
                        }
                    }
                )
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Incorrect PIN for ${selectedEmployer.fullName}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
