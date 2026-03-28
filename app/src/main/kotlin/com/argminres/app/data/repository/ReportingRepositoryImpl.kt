package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.ReportingDao
import com.argminres.app.domain.model.LowStockProduct
import com.argminres.app.domain.model.SalesSummary
import com.argminres.app.domain.model.TopProduct
import com.argminres.app.domain.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ReportingRepositoryImpl(
    private val reportingDao: ReportingDao
) : ReportingRepository {
    override fun getDailySalesSummary(startDate: Long, endDate: Long, cashierId: Long?): Flow<List<SalesSummary>> {
        return reportingDao.getDailySalesSummary(startDate, endDate, cashierId)
    }

    override fun getTopSellingDishes(startDate: Long, endDate: Long, limit: Int, cashierId: Long?): Flow<List<TopProduct>> {
        return reportingDao.getTopSellingDishes(startDate, endDate, limit, cashierId)
    }

    override fun getTopSellingPackages(startDate: Long, endDate: Long, limit: Int, cashierId: Long?): Flow<List<TopProduct>> {
        return reportingDao.getTopSellingPackages(startDate, endDate, limit, cashierId)
    }

    override fun getLowStockDishes(threshold: Int): Flow<List<LowStockProduct>> {
        return reportingDao.getLowStockDishes(threshold)
    }

    override fun getTotalRevenue(startDate: Long, endDate: Long, cashierId: Long?): Flow<Double> {
        return reportingDao.getTotalRevenue(startDate, endDate, cashierId).map { it ?: 0.0 }
    }

    override fun getTotalIngredientCost(startDate: Long, endDate: Long, cashierId: Long?): Flow<Double> {
        return reportingDao.getTotalIngredientCost(startDate, endDate, cashierId).map { it ?: 0.0 }
    }

    override fun getTotalWasteValue(startDate: Long, endDate: Long, cashierId: Long?): Flow<Double> {
        return reportingDao.getTotalWasteValue(startDate, endDate, cashierId).map { it ?: 0.0 }
    }

    override fun getIngredientUsageBySession(sessionId: Long): Flow<List<com.argminres.app.data.local.entity.IngredientUsageEntity>> {
        return reportingDao.getIngredientUsageBySession(sessionId)
    }

    override fun getDishUsageBySession(sessionId: Long): Flow<List<com.argminres.app.data.local.entity.WasteRecordEntity>> {
        return reportingDao.getDishUsageBySession(sessionId)
    }

    override fun getAggregatedIngredientUsage(startDate: Long, endDate: Long): Flow<List<com.argminres.app.domain.model.AggregatedIngredientUsage>> {
        return reportingDao.getAggregatedIngredientUsage(startDate, endDate)
    }

    override fun getAggregatedDishUsage(startDate: Long, endDate: Long): Flow<List<com.argminres.app.domain.model.AggregatedDishUsage>> {
        return reportingDao.getAggregatedDishUsage(startDate, endDate)
    }

    override fun getAggregatedUsageReport(startDate: Long, endDate: Long): Flow<com.argminres.app.presentation.reporting.AggregatedUsageState> {
        return combine(
            getAggregatedIngredientUsage(startDate, endDate),
            getAggregatedDishUsage(startDate, endDate)
        ) { ingredients, dishes ->
            com.argminres.app.presentation.reporting.AggregatedUsageState(ingredients, dishes)
        }
    }

    override fun getDishSalesByCashier(startDate: Long, endDate: Long): Flow<List<com.argminres.app.domain.model.CashierDishSales>> {
        return reportingDao.getDishSalesByCashier(startDate, endDate)
    }
}
