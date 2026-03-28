package com.argminres.app.presentation.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.domain.model.LowStockProduct
import com.argminres.app.domain.model.SalesSummary
import com.argminres.app.domain.model.TopProduct
import com.argminres.app.domain.usecase.reporting.GetDashboardDataUseCase
import com.argminres.app.domain.usecase.reporting.GetLowStockReportUseCase
import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.domain.usecase.reporting.GetSalesReportUseCase
import android.content.Context
import android.widget.Toast
import com.argminres.app.domain.model.AggregatedIngredientUsage
import com.argminres.app.util.PdfReportData
import com.argminres.app.util.PdfReportExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReportingUiState(
    val isLoading: Boolean = false,
    val salesSummary: List<SalesSummary> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val totalRevenue: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalWaste: Double = 0.0,
    val unclosedSessions: List<DailySessionEntity> = emptyList(),
    val allSessions: List<DailySessionEntity> = emptyList(),
    val selectedTab: Int = 0, // 0: Dashboard, 1: Sales, 2: Session History
    val selectedSessionDetails: SessionDetailState? = null,
    val aggregatedUsage: AggregatedUsageState? = null,
    val startDate: Long = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
    val endDate: Long = System.currentTimeMillis(),
    val cashierDishSales: List<com.argminres.app.domain.model.CashierDishSales> = emptyList(),
    val isDownloadTypeDialogOpen: Boolean = false,
    val isPeriodSelectionDialogOpen: Boolean = false,
    val downloadReportType: String? = null,
    val availablePeriods: List<ReportPeriod> = emptyList(),
    val isGeneratingPdf: Boolean = false,
    val exportMessage: String? = null
)

data class ReportPeriod(
    val id: String,
    val label: String,
    val startDate: Long,
    val endDate: Long
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
    private val authManager: com.argminres.app.domain.manager.AuthenticationManager,
    private val context: Context,
    private val storeSettingsRepository: com.argminres.app.domain.repository.StoreSettingsRepository
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
            _uiState.update { state -> state.copy(isLoading = true) }
            val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                authManager.getCurrentEmployer()?.id
            } else {
                null
            }

            getSalesReportUseCase(_uiState.value.startDate, _uiState.value.endDate, cashierId).collect { summary: List<SalesSummary> ->
                _uiState.update { state -> state.copy(salesSummary = summary, isLoading = false) }
            }
        }
    }

    private fun loadAllSessions() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }
            dailySessionRepository.getAllSessions().collect { sessions: List<DailySessionEntity> ->
                _uiState.update { state -> state.copy(allSessions = sessions, isLoading = false) }
            }
        }
    }

    fun selectSession(sessionId: Long) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }
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

    // Download / Export Logic
    fun onDownloadIconClick() {
        _uiState.update { it.copy(isDownloadTypeDialogOpen = true) }
    }

    fun dismissDownloadDialogs() {
        _uiState.update { 
            it.copy(
                isDownloadTypeDialogOpen = false, 
                isPeriodSelectionDialogOpen = false,
                downloadReportType = null
            ) 
        }
    }

    fun onReportTypeSelected(type: String) {
        _uiState.update { it.copy(isDownloadTypeDialogOpen = false, downloadReportType = type) }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val periods = when (type) {
                "Harian" -> generateDailyPeriods()
                "Mingguan" -> generateWeeklyPeriods()
                "Bulanan" -> generateMonthlyPeriods()
                "Tahunan" -> generateYearlyPeriods()
                else -> emptyList()
            }
            
            _uiState.update { 
                it.copy(
                    availablePeriods = periods,
                    isPeriodSelectionDialogOpen = true,
                    isLoading = false
                ) 
            }
        }
    }

    private suspend fun generateDailyPeriods(): List<ReportPeriod> {
        val sessions = dailySessionRepository.getAllSessions().firstOrNull()?.filter { s: DailySessionEntity -> s.status == "CLOSED" } ?: emptyList()
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sessions.map { session ->
            ReportPeriod(
                id = session.id.toString(),
                label = "${formatter.format(Date(session.timestampStart))} - Sesi #${session.id}",
                startDate = session.timestampStart,
                endDate = session.timestampEnd ?: System.currentTimeMillis()
            )
        }
    }

    private suspend fun generateWeeklyPeriods(): List<ReportPeriod> {
        // Collect all closed sessions to find the range of dates
        val sessions = dailySessionRepository.getAllSessions().firstOrNull()?.filter { s: DailySessionEntity -> s.status == "CLOSED" } ?: return emptyList()
        if (sessions.isEmpty()) return emptyList()
        
        val minDate = sessions.minOf { s: DailySessionEntity -> s.timestampStart }
        val maxDate = sessions.maxOf { s: DailySessionEntity -> s.timestampStart }
        
        val periods = mutableListOf<ReportPeriod>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = maxDate
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        
        val dateFormatter = SimpleManagementFormatter("dd MMM", Locale("id", "ID"))
        
        while (cal.timeInMillis >= minDate - 7 * 24 * 60 * 60 * 1000L) {
            val start = cal.timeInMillis
            val weekEndCal = cal.clone() as Calendar
            weekEndCal.add(Calendar.DAY_OF_YEAR, 6)
            weekEndCal.set(Calendar.HOUR_OF_DAY, 23)
            weekEndCal.set(Calendar.MINUTE, 59)
            val end = weekEndCal.timeInMillis
            
            // Check if there are sessions in this week
            val hasSessions = sessions.any { s: DailySessionEntity -> s.timestampStart in start..end }
            if (hasSessions) {
                periods.add(ReportPeriod(
                    id = "week_$start",
                    label = "Minggu (${dateFormatter.format(Date(start))} - ${dateFormatter.format(Date(end))})",
                    startDate = start,
                    endDate = end
                ))
            }
            cal.add(Calendar.WEEK_OF_YEAR, -1)
            if (periods.size > 12) break // Limit to last 12 weeks
        }
        return periods
    }

    private suspend fun generateMonthlyPeriods(): List<ReportPeriod> {
        val sessions = dailySessionRepository.getAllSessions().firstOrNull()?.filter { s: DailySessionEntity -> s.status == "CLOSED" } ?: return emptyList()
        if (sessions.isEmpty()) return emptyList()
        
        val minDate = sessions.minOf { s: DailySessionEntity -> s.timestampStart }
        val maxDate = sessions.maxOf { s: DailySessionEntity -> s.timestampStart }
        
        val periods = mutableListOf<ReportPeriod>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = maxDate
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        
        while (cal.timeInMillis >= minDate - 30 * 24 * 60 * 60 * 1000L) {
            val start = cal.timeInMillis
            val monthEndCal = cal.clone() as Calendar
            monthEndCal.add(Calendar.MONTH, 1)
            monthEndCal.add(Calendar.DAY_OF_YEAR, -1)
            monthEndCal.set(Calendar.HOUR_OF_DAY, 23)
            val end = monthEndCal.timeInMillis
            
            if (sessions.any { s: DailySessionEntity -> s.timestampStart in start..end }) {
                periods.add(ReportPeriod(
                    id = "month_$start",
                    label = monthFormatter.format(Date(start)),
                    startDate = start,
                    endDate = end
                ))
            }
            cal.add(Calendar.MONTH, -1)
            if (periods.size > 24) break
        }
        return periods
    }

    private suspend fun generateYearlyPeriods(): List<ReportPeriod> {
        val sessions = dailySessionRepository.getAllSessions().firstOrNull()?.filter { s: DailySessionEntity -> s.status == "CLOSED" } ?: return emptyList()
        if (sessions.isEmpty()) return emptyList()
        
        val minDate = sessions.minOf { s: DailySessionEntity -> s.timestampStart }
        val maxDate = sessions.maxOf { s: DailySessionEntity -> s.timestampStart }
        
        val periods = mutableListOf<ReportPeriod>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = maxDate
        cal.set(Calendar.DAY_OF_YEAR, 1)
        
        while (cal.timeInMillis >= minDate - 365 * 24 * 60 * 60 * 1000L) {
            val start = cal.timeInMillis
            val yearEndCal = cal.clone() as Calendar
            yearEndCal.add(Calendar.YEAR, 1)
            yearEndCal.add(Calendar.DAY_OF_YEAR, -1)
            val end = yearEndCal.timeInMillis
            
            if (sessions.any { s: DailySessionEntity -> s.timestampStart in start..end }) {
                periods.add(ReportPeriod(
                    id = "year_$start",
                    label = "Tahun ${Calendar.getInstance().apply { timeInMillis = start }.get(Calendar.YEAR)}",
                    startDate = start,
                    endDate = end
                ))
            }
            cal.add(Calendar.YEAR, -1)
        }
        return periods
    }

    fun onPeriodSelected(period: ReportPeriod) {
        // Dismiss first to show we're doing something
        dismissDownloadDialogs()
        
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isGeneratingPdf = true, exportMessage = null) }
            
            try {
                val storeSettings = storeSettingsRepository.getStoreSettings().firstOrNull()
                val reportType = _uiState.value.downloadReportType ?: "Laporan"
                val isDetailed = reportType == "Harian"
                val reportTitle = "Laporan $reportType"
                
                // Fetch all data for the period
                val cashierId = if (authManager.getCurrentEmployer()?.role == "CASHIER") {
                    authManager.getCurrentEmployer()?.id
                } else {
                    null
                }
                
                // 1. Get Sales Summary / Total Revenue
                val salesSummary: List<SalesSummary> = getSalesReportUseCase(period.startDate, period.endDate, cashierId).firstOrNull() ?: emptyList()
                val totalRevenue = salesSummary.sumOf { s: SalesSummary -> s.totalSales }
                
                // 2. Get Products (State already has them for this period)
                val topProducts = _uiState.value.topProducts
                
                // 3. Get Details (Only if Detailed)
                var ingredientUsage = emptyList<com.argminres.app.domain.model.AggregatedIngredientUsage>()
                var dishUsage = emptyList<com.argminres.app.domain.model.AggregatedDishUsage>()
                
                if (isDetailed) {
                    val usageData = getAggregatedUsageReportUseCase(period.startDate, period.endDate)
                    ingredientUsage = usageData.ingredientUsage.firstOrNull() ?: emptyList()
                    dishUsage = usageData.dishUsage.firstOrNull() ?: emptyList()
                }
                
                // Calculate costs and waste from sessions
                val sessions = dailySessionRepository.getAllSessions().firstOrNull()?.filter { s -> s.timestampStart in period.startDate..period.endDate } ?: emptyList()
                val totalCost = sessions.sumOf { s -> s.totalIngredientCost }
                val totalWaste = sessions.sumOf { s -> s.totalDishWasteValue + s.totalIngredientWasteValue }

                val reportData = PdfReportData(
                    storeName = storeSettings?.storeName ?: "Padang POS",
                    storeAddress = storeSettings?.storeAddress ?: "",
                    reportTitle = reportTitle,
                    periodLabel = period.label,
                    totalRevenue = totalRevenue,
                    totalCost = totalCost,
                    totalWaste = totalWaste,
                    topProducts = topProducts,
                    ingredientUsage = ingredientUsage,
                    dishUsage = dishUsage,
                    isDetailed = isDetailed
                )

                val uri = PdfReportExporter(context).generateAndSaveReport(reportData)
                
                if (uri != null) {
                    _uiState.update { state -> state.copy(isGeneratingPdf = false, exportMessage = "Laporan berhasil disimpan ke folder Download") }
                } else {
                    _uiState.update { state -> state.copy(isGeneratingPdf = false, exportMessage = "Gagal membuat PDF") }
                }
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(isGeneratingPdf = false, exportMessage = "Error: ${e.message}") }
            }
        }
    }

    private fun SimpleManagementFormatter(pattern: String, locale: Locale): SimpleDateFormat {
        return SimpleDateFormat(pattern, locale)
    }
}
