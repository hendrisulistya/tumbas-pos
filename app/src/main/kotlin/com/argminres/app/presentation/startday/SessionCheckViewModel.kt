package com.argminres.app.presentation.startday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.domain.manager.AuthenticationManager
import com.argminres.app.domain.usecase.session.SessionCheckResult
import com.argminres.app.domain.usecase.session.SessionCheckUseCase
import com.argminres.app.domain.usecase.session.StartDailySessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionCheckUiState(
    val isLoading: Boolean = true,
    val hasActiveSession: Boolean = false,
    val isManager: Boolean = false,
    val showStartDayDialog: Boolean = false,
    val isStartingSession: Boolean = false,
    val error: String? = null
)

class SessionCheckViewModel(
    private val sessionCheckUseCase: SessionCheckUseCase,
    private val startDailySessionUseCase: StartDailySessionUseCase,
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionCheckUiState())
    val uiState: StateFlow<SessionCheckUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val currentUser = authManager.getCurrentEmployer()
            val isManager = currentUser?.role == "MANAGER"
            
            when (val result = sessionCheckUseCase()) {
                is SessionCheckResult.SessionActive -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasActiveSession = true,
                            isManager = isManager
                        )
                    }
                }
                is SessionCheckResult.NoSession -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasActiveSession = false,
                            isManager = isManager,
                            showStartDayDialog = isManager
                        )
                    }
                }
            }
        }
    }

    fun startDay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isStartingSession = true, error = null) }
            
            try {
                val currentUser = authManager.getCurrentEmployer()
                startDailySessionUseCase(currentUser?.id)
                
                _uiState.update {
                    it.copy(
                        isStartingSession = false,
                        hasActiveSession = true,
                        showStartDayDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStartingSession = false,
                        error = e.message ?: "Failed to start day"
                    )
                }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showStartDayDialog = false) }
    }
}
