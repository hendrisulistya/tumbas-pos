package com.argminres.app.domain.usecase.reporting

import com.argminres.app.domain.repository.ReportingRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow

data class DashboardData(
    val salesSummary: Flow<List<com.argminres.app.domain.model.SalesSummary>>,
    val topProducts: Flow<List<com.argminres.app.domain.model.TopProduct>>,
    val lowStock: Flow<List<com.argminres.app.domain.model.LowStockProduct>>,
    val totalRevenue: Flow<Double>,
    val totalCost: Flow<Double>,
    val totalWaste: Flow<Double>
)

class GetDashboardDataUseCase(
    private val reportingRepository: ReportingRepository
) {
    operator fun invoke(cashierId: Long? = null) = reportingRepository.run {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.timeInMillis

        DashboardData(
            salesSummary = getDailySalesSummary(startDate, endDate, cashierId),
            topProducts = getTopSellingDishes(startDate, endDate, 5, cashierId),
            lowStock = getLowStockDishes(10),
            totalRevenue = getTotalRevenue(startDate, endDate, cashierId),
            totalCost = getTotalIngredientCost(startDate, endDate, cashierId),
            totalWaste = getTotalWasteValue(startDate, endDate, cashierId)
        )
    }
}

class GetSalesReportUseCase(
    private val reportingRepository: ReportingRepository
) {
    operator fun invoke(startDate: Long, endDate: Long, cashierId: Long? = null) = 
        reportingRepository.getDailySalesSummary(startDate, endDate, cashierId)
}

class GetLowStockReportUseCase(
    private val reportingRepository: ReportingRepository
) {
    operator fun invoke(threshold: Int = 10) = reportingRepository.getLowStockDishes(threshold)
}

data class SessionReportData(
    val ingredientUsage: Flow<List<com.argminres.app.data.local.entity.IngredientUsageEntity>>,
    val dishUsage: Flow<List<com.argminres.app.data.local.entity.WasteRecordEntity>>
)

class GetSessionReportUseCase(
    private val reportingRepository: ReportingRepository
) {
    operator fun invoke(sessionId: Long) = SessionReportData(
        ingredientUsage = reportingRepository.getIngredientUsageBySession(sessionId),
        dishUsage = reportingRepository.getDishUsageBySession(sessionId)
    )
}

data class AggregatedUsageData(
    val ingredientUsage: Flow<List<com.argminres.app.domain.model.AggregatedIngredientUsage>>,
    val dishUsage: Flow<List<com.argminres.app.domain.model.AggregatedDishUsage>>
)

class GetAggregatedUsageReportUseCase(
    private val reportingRepository: ReportingRepository
) {
    operator fun invoke(startDate: Long, endDate: Long) = AggregatedUsageData(
        ingredientUsage = reportingRepository.getAggregatedIngredientUsage(startDate, endDate),
        dishUsage = reportingRepository.getAggregatedDishUsage(startDate, endDate)
    )
}

class GetCashierPerformanceUseCase(private val reportingRepository: ReportingRepository) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<com.argminres.app.domain.model.CashierDishSales>> {
        return reportingRepository.getDishSalesByCashier(startDate, endDate)
    }
}
