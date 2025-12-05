package com.tumbaspos.app.domain.usecase.purchase

import com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems
import com.tumbaspos.app.data.local.entity.PurchaseOrderEntity
import com.tumbaspos.app.data.local.entity.PurchaseOrderItemEntity
import com.tumbaspos.app.data.local.entity.StockMovementEntity
import com.tumbaspos.app.domain.repository.ProductRepository
import com.tumbaspos.app.domain.repository.PurchaseOrderRepository
import com.tumbaspos.app.domain.repository.StockRepository
import com.tumbaspos.app.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow

class GetPurchaseOrdersUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository
) {
    operator fun invoke(): Flow<List<PurchaseOrderWithItems>> = purchaseOrderRepository.getAllPurchaseOrders()
}

class CreatePurchaseOrderUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository
) {
    suspend operator fun invoke(order: PurchaseOrderEntity, items: List<PurchaseOrderItemEntity>): Long {
        return purchaseOrderRepository.createPurchaseOrder(order, items)
    }
}

class ReceivePurchaseOrderUseCase(
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(orderId: Long) {
        val orderWithItems = purchaseOrderRepository.getPurchaseOrderById(orderId) ?: return
        
        if (orderWithItems.order.status == "RECEIVED") return

        // 1. Update order status
        purchaseOrderRepository.updatePurchaseOrderStatus(orderId, "RECEIVED")

        // 2. Update stock and record movements
        orderWithItems.items.forEach { item ->
            productRepository.updateStock(item.productId, item.quantity)
            
            stockRepository.insertStockMovement(
                StockMovementEntity(
                    productId = item.productId,
                    movementType = "IN",
                    quantity = item.quantity,
                    referenceId = orderId,
                    notes = "Purchase Order #${orderId}"
                )
            )
        }
    }
}

class GetSuppliersUseCase(
    private val supplierRepository: SupplierRepository
) {
    operator fun invoke() = supplierRepository.getAllSuppliers()
}

class ManageSupplierUseCase(
    private val supplierRepository: SupplierRepository
) {
    suspend fun createSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity): Long {
        return supplierRepository.insertSupplier(supplier)
    }
    
    suspend fun updateSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity) {
        supplierRepository.updateSupplier(supplier)
    }
    
    suspend fun deleteSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity) {
        supplierRepository.deleteSupplier(supplier)
    }
}
