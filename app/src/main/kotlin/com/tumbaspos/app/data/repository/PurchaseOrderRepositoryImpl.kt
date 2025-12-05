package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.PurchaseOrderDao
import com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems
import com.tumbaspos.app.data.local.entity.PurchaseOrderEntity
import com.tumbaspos.app.data.local.entity.PurchaseOrderItemEntity
import com.tumbaspos.app.domain.repository.PurchaseOrderRepository
import kotlinx.coroutines.flow.Flow

class PurchaseOrderRepositoryImpl(
    private val purchaseOrderDao: PurchaseOrderDao
) : PurchaseOrderRepository {
    override fun getAllPurchaseOrders(): Flow<List<PurchaseOrderWithItems>> {
        return purchaseOrderDao.getAllPurchaseOrders()
    }

    override suspend fun getPurchaseOrderById(id: Long): PurchaseOrderWithItems? {
        return purchaseOrderDao.getPurchaseOrderById(id)
    }

    override suspend fun createPurchaseOrder(
        order: PurchaseOrderEntity,
        items: List<PurchaseOrderItemEntity>
    ): Long {
        val orderId = purchaseOrderDao.insertPurchaseOrder(order)
        val itemsWithOrderId = items.map { it.copy(purchaseOrderId = orderId) }
        purchaseOrderDao.insertPurchaseOrderItems(itemsWithOrderId)
        return orderId
    }

    override suspend fun updatePurchaseOrder(order: PurchaseOrderEntity) {
        purchaseOrderDao.updatePurchaseOrder(order)
    }

    override suspend fun updatePurchaseOrderStatus(orderId: Long, status: String) {
        val orderWithItems = purchaseOrderDao.getPurchaseOrderById(orderId)
        orderWithItems?.let {
            val updatedOrder = it.order.copy(status = status, updatedAt = System.currentTimeMillis())
            purchaseOrderDao.updatePurchaseOrder(updatedOrder)
        }
    }
}
