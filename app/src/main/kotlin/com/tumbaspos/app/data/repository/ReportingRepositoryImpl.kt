package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.ReportingDao
import com.tumbaspos.app.domain.model.LowStockProduct
import com.tumbaspos.app.domain.model.SalesSummary
import com.tumbaspos.app.domain.model.TopProduct
import com.tumbaspos.app.domain.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportingRepositoryImpl(
    private val reportingDao: ReportingDao
) : ReportingRepository {
    override fun getDailySalesSummary(startDate: Long, endDate: Long): Flow<List<SalesSummary>> {
        return reportingDao.getDailySalesSummary(startDate, endDate)
    }

    override fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int): Flow<List<TopProduct>> {
        return reportingDao.getTopSellingProducts(startDate, endDate, limit)
    }

    override fun getLowStockProducts(threshold: Int): Flow<List<LowStockProduct>> {
        return reportingDao.getLowStockProducts(threshold)
    }

    override fun getTotalRevenue(startDate: Long, endDate: Long): Flow<Double> {
        return reportingDao.getTotalRevenue(startDate, endDate).map { it ?: 0.0 }
    }
}
