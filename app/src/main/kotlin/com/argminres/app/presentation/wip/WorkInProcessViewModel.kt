package com.argminres.app.presentation.wip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.domain.repository.DailySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WipUiState(
    val unclosedSessions: List<DailySessionEntity> = emptyList(),
    val isLoading: Boolean = false
)

class WorkInProcessViewModel(
    private val dailySessionRepository: DailySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WipUiState())
    val uiState: StateFlow<WipUiState> = _uiState.asStateFlow()

    init {
        loadUnclosedSessions()
    }

    private fun loadUnclosedSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Get only PENDING_CLOSE sessions (ACTIVE sessions are handled at login)
            val allSessions = mutableListOf<DailySessionEntity>()
            dailySessionRepository.getAllSessions().collect { sessions ->
                allSessions.clear()
                allSessions.addAll(sessions.filter { it.status == "PENDING_CLOSE" })
                _uiState.value = _uiState.value.copy(
                    unclosedSessions = allSessions,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadUnclosedSessions()
    }
}
