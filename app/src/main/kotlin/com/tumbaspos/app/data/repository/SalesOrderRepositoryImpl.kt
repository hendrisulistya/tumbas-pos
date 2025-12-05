package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.SalesOrderDao
import com.tumbaspos.app.data.local.dao.SalesOrderWithItems
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.data.local.entity.SalesOrderItemEntity
import com.tumbaspos.app.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.Flow

class SalesOrderRepositoryImpl(
    private val salesOrderDao: SalesOrderDao
) : SalesOrderRepository {
    override fun getAllSalesOrders(): Flow<List<SalesOrderWithItems>> {
        return salesOrderDao.getAllSalesOrders()
    }

    override suspend fun getSalesOrderById(id: Long): SalesOrderWithItems? {
        return salesOrderDao.getSalesOrderById(id)
    }

    override fun getSalesOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<SalesOrderEntity>> {
        return salesOrderDao.getSalesOrdersByDateRange(startDate, endDate)
    }

    override suspend fun createSalesOrder(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long {
        val orderId = salesOrderDao.insertSalesOrder(order)
        val itemsWithOrderId = items.map { it.copy(salesOrderId = orderId) }
        salesOrderDao.insertSalesOrderItems(itemsWithOrderId)
        return orderId
    }

    override suspend fun updateSalesOrder(order: SalesOrderEntity) {
        salesOrderDao.updateSalesOrder(order)
    }
}
