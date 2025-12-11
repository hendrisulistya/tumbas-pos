package com.tumbaspos.app.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.AuditLogEntity
import com.tumbaspos.app.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AuditLogUiState(
    val logs: List<AuditLogEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedEmployerId: Long? = null,
    val selectedAction: String? = null,
    val startDate: Long = getStartOfDay(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)), // 7 days ago
    val endDate: Long = getEndOfDay(System.currentTimeMillis())
)

class AuditLogViewModel(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuditLogUiState())
    val uiState: StateFlow<AuditLogUiState> = _uiState.asStateFlow()
    
    init {
        loadLogs()
    }
    
    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            auditLogRepository.getFiltered(
                employerId = _uiState.value.selectedEmployerId,
                action = _uiState.value.selectedAction,
                startDate = _uiState.value.startDate,
                endDate = _uiState.value.endDate
            ).collect { logs ->
                _uiState.update { it.copy(logs = logs, isLoading = false) }
            }
        }
    }
    
    fun filterByEmployer(employerId: Long?) {
        _uiState.update { it.copy(selectedEmployerId = employerId) }
        loadLogs()
    }
    
    fun filterByAction(action: String?) {
        _uiState.update { it.copy(selectedAction = action) }
        loadLogs()
    }
    
    fun setDateRange(startDate: Long, endDate: Long) {
        _uiState.update { 
            it.copy(
                startDate = getStartOfDay(startDate),
                endDate = getEndOfDay(endDate)
            )
        }
        loadLogs()
    }
    
    fun clearFilters() {
        _uiState.update { 
            it.copy(
                selectedEmployerId = null,
                selectedAction = null,
                startDate = getStartOfDay(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)),
                endDate = getEndOfDay(System.currentTimeMillis())
            )
        }
        loadLogs()
    }
}

private fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun getEndOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}
