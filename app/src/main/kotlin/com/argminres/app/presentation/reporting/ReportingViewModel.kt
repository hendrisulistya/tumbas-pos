package com.argminres.app.presentation.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.domain.model.LowStockProduct
import com.argminres.app.domain.model.SalesSummary
import com.argminres.app.domain.model.TopProduct
import com.argminres.app.domain.usecase.reporting.GetDashboardDataUseCase
import com.argminres.app.domain.usecase.reporting.GetLowStockReportUseCase
import com.argminres.app.domain.usecase.reporting.GetSalesReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReportingUiState(
    val isLoading: Boolean = false,
    val salesSummary: List<SalesSummary> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val totalRevenue: Double = 0.0,
    val unclosedSessions: List<com.argminres.app.data.local.entity.DailySessionEntity> = emptyList(),
    val selectedTab: Int = 0 // 0: Dashboard, 1: Sales, 2: Daily Sessions
)

class ReportingViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getSalesReportUseCase: GetSalesReportUseCase,
    private val dailySessionRepository: com.argminres.app.domain.repository.DailySessionRepository,
    private val authManager: com.argminres.app.domain.manager.AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportingUiState())
    val uiState: StateFlow<ReportingUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        when (index) {
            0 -> loadDashboardData()
            1 -> loadSalesReport()
            2 -> loadDailySessions()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Get cashier ID for filtering (null for managers = see all)
            val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                authManager.getCurrentEmployer()?.id
            } else {
                null // Managers see all
            }
            
            val data = getDashboardDataUseCase(cashierId)
            
            // Collect flows concurrently
            launch {
                data.salesSummary.collect { summary ->
                    _uiState.update { it.copy(salesSummary = summary) }
                }
            }
            launch {
                data.topProducts.collect { products ->
                    _uiState.update { it.copy(topProducts = products) }
                }
            }
            launch {
                data.totalRevenue.collect { revenue ->
                    _uiState.update { it.copy(totalRevenue = revenue) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadSalesReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -30) // Last 30 days
            val startDate = calendar.timeInMillis
            
            // Get cashier ID for filtering
            val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                authManager.getCurrentEmployer()?.id
            } else {
                null // Managers see all
            }

            getSalesReportUseCase(startDate, endDate, cashierId).collect { summary ->
                _uiState.update { it.copy(salesSummary = summary, isLoading = false) }
            }
        }
    }

    private fun loadDailySessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dailySessionRepository.getAllSessions().collect { sessions ->
                val unclosed = sessions.filter { it.status != "CLOSED" }
                _uiState.update { it.copy(unclosedSessions = unclosed, isLoading = false) }
            }
        }
    }
}
