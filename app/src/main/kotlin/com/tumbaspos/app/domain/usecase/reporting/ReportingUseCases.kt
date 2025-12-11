package com.tumbaspos.app.domain.usecase.reporting

import com.tumbaspos.app.domain.repository.ReportingRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow

data class DashboardData(
    val salesSummary: Flow<List<com.tumbaspos.app.domain.model.SalesSummary>>,
    val topProducts: Flow<List<com.tumbaspos.app.domain.model.TopProduct>>,
    val lowStock: Flow<List<com.tumbaspos.app.domain.model.LowStockProduct>>,
    val totalRevenue: Flow<Double>
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
            topProducts = getTopSellingProducts(startDate, endDate, 5, cashierId),
            lowStock = getLowStockProducts(10),
            totalRevenue = getTotalRevenue(startDate, endDate, cashierId)
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
    operator fun invoke(threshold: Int = 10) = reportingRepository.getLowStockProducts(threshold)
}
