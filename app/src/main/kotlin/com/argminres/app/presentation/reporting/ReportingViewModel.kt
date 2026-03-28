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
    val totalCost: Double = 0.0,
    val totalWaste: Double = 0.0,
    val unclosedSessions: List<com.argminres.app.data.local.entity.DailySessionEntity> = emptyList(),
    val allSessions: List<com.argminres.app.data.local.entity.DailySessionEntity> = emptyList(),
    val selectedTab: Int = 0, // 0: Dashboard, 1: Sales, 2: Session History
    val selectedSessionDetails: SessionDetailState? = null,
    val aggregatedUsage: AggregatedUsageState? = null,
    val startDate: Long = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
    val endDate: Long = System.currentTimeMillis(),
    val cashierDishSales: List<com.argminres.app.domain.model.CashierDishSales> = emptyList()
)

data class SessionDetailState(
    val sessionId: Long,
    val ingredientUsage: List<com.argminres.app.data.local.entity.IngredientUsageEntity> = emptyList(),
    val dishUsage: List<com.argminres.app.data.local.entity.WasteRecordEntity> = emptyList()
)

data class AggregatedUsageState(
    val ingredientUsage: List<com.argminres.app.domain.model.AggregatedIngredientUsage> = emptyList(),
    val dishUsage: List<com.argminres.app.domain.model.AggregatedDishUsage> = emptyList()
)

class ReportingViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getSalesReportUseCase: GetSalesReportUseCase,
    private val getSessionReportUseCase: com.argminres.app.domain.usecase.reporting.GetSessionReportUseCase,
    private val getAggregatedUsageReportUseCase: com.argminres.app.domain.usecase.reporting.GetAggregatedUsageReportUseCase,
    private val getCashierPerformanceUseCase: com.argminres.app.domain.usecase.reporting.GetCashierPerformanceUseCase,
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
            1 -> {
                loadSalesReport()
                loadAggregatedUsage(_uiState.value.startDate, _uiState.value.endDate)
                loadCashierPerformance(_uiState.value.startDate, _uiState.value.endDate)
            }
            2 -> loadAllSessions()
        }
    }

    fun setDateRange(start: Long, end: Long) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        loadSalesReport()
        loadAggregatedUsage(start, end)
        loadCashierPerformance(start, end)
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                authManager.getCurrentEmployer()?.id
            } else {
                null
            }
            
            val data = getDashboardDataUseCase(cashierId)
            
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
            launch {
                data.totalCost.collect { cost ->
                    _uiState.update { it.copy(totalCost = cost) }
                }
            }
            launch {
                data.totalWaste.collect { waste ->
                    _uiState.update { it.copy(totalWaste = waste) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadSalesReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                authManager.getCurrentEmployer()?.id
            } else {
                null
            }

            getSalesReportUseCase(_uiState.value.startDate, _uiState.value.endDate, cashierId).collect { summary ->
                _uiState.update { it.copy(salesSummary = summary, isLoading = false) }
            }
        }
    }

    private fun loadAllSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dailySessionRepository.getAllSessions().collect { sessions ->
                _uiState.update { it.copy(allSessions = sessions, isLoading = false) }
            }
        }
    }

    fun selectSession(sessionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = getSessionReportUseCase(sessionId)
            
            launch {
                data.ingredientUsage.collect { usage ->
                    _uiState.update { state ->
                        state.copy(selectedSessionDetails = (state.selectedSessionDetails ?: SessionDetailState(sessionId)).copy(ingredientUsage = usage))
                    }
                }
            }
            launch {
                data.dishUsage.collect { usage ->
                    _uiState.update { state ->
                        state.copy(selectedSessionDetails = (state.selectedSessionDetails ?: SessionDetailState(sessionId)).copy(dishUsage = usage))
                    }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun dismissSessionDetails() {
        _uiState.update { it.copy(selectedSessionDetails = null) }
    }

    private fun loadAggregatedUsage(start: Long, end: Long) {
        viewModelScope.launch {
            val data = getAggregatedUsageReportUseCase(start, end)
            launch {
                data.ingredientUsage.collect { usage ->
                    _uiState.update { state ->
                        state.copy(aggregatedUsage = (state.aggregatedUsage ?: AggregatedUsageState()).copy(ingredientUsage = usage))
                    }
                }
            }
            launch {
                data.dishUsage.collect { usage ->
                    _uiState.update { state ->
                        state.copy(aggregatedUsage = (state.aggregatedUsage ?: AggregatedUsageState()).copy(dishUsage = usage))
                    }
                }
            }
        }
    }

    private fun loadCashierPerformance(start: Long, end: Long) {
        viewModelScope.launch {
            getCashierPerformanceUseCase(start, end).collect { performance ->
                _uiState.update { it.copy(cashierDishSales = performance) }
            }
        }
    }
}
