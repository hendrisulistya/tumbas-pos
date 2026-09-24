package com.argomin.app.domain.usecase.purchase

import com.argomin.app.data.local.dao.PurchaseOrderWithItems
import com.argomin.app.data.local.entity.PurchaseOrderEntity
import com.argomin.app.data.local.entity.PurchaseOrderItemEntity
import com.argomin.app.data.local.entity.StockMovementEntity
import com.argomin.app.domain.repository.DishRepository
import com.argomin.app.domain.repository.PurchaseOrderRepository
import com.argomin.app.domain.repository.StockRepository
import com.argomin.app.domain.repository.SupplierRepository
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
    private val ingredientRepository: com.argomin.app.domain.repository.IngredientRepository,
    private val ingredientStockRepository: com.argomin.app.domain.repository.IngredientStockRepository
) {
    suspend operator fun invoke(orderId: Long) {
        val orderWithItems = purchaseOrderRepository.getPurchaseOrderById(orderId)
        
        if (orderWithItems != null && orderWithItems.order.status == "SUBMITTED") {
            // Update ingredient stock
            orderWithItems.items.forEach { item ->
                ingredientRepository.updateStock(item.ingredientId, item.quantity)
                
                // Record stock movement
                ingredientStockRepository.insertStockMovement(
                    com.argomin.app.data.local.entity.IngredientStockMovementEntity(
                        ingredientId = item.ingredientId,
                        movementType = "PURCHASE",
                        quantity = item.quantity,
                        referenceId = orderId,
                        notes = "Purchase order #$orderId received"
                    )
                )
            }
            
            // Update order status
            purchaseOrderRepository.updatePurchaseOrder(
                orderWithItems.order.copy(status = "RECEIVED")
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
    suspend fun createSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity): Long {
        return supplierRepository.insertSupplier(supplier)
    }
    
    suspend fun updateSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity) {
        supplierRepository.updateSupplier(supplier)
    }
    
    suspend fun deleteSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity) {
        supplierRepository.deleteSupplier(supplier)
    }
}
