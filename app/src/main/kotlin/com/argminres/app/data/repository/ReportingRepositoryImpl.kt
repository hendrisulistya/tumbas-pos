package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.ReportingDao
import com.argminres.app.domain.model.LowStockProduct
import com.argminres.app.domain.model.SalesSummary
import com.argminres.app.domain.model.TopProduct
import com.argminres.app.domain.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
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

    override fun getLowStockDishes(threshold: Int): Flow<List<LowStockProduct>> {
        return reportingDao.getLowStockDishes(threshold)
    }

    override fun getTotalRevenue(startDate: Long, endDate: Long, cashierId: Long?): Flow<Double> {
        return reportingDao.getTotalRevenue(startDate, endDate, cashierId).map { it ?: 0.0 }
    }
}
