package com.argminres.app.domain.usecase.purchase

import com.argminres.app.data.local.dao.PurchaseOrderWithItems
import com.argminres.app.data.local.entity.PurchaseOrderEntity
import com.argminres.app.data.local.entity.PurchaseOrderItemEntity
import com.argminres.app.data.local.entity.StockMovementEntity
import com.argminres.app.domain.repository.DishRepository
import com.argminres.app.domain.repository.PurchaseOrderRepository
import com.argminres.app.domain.repository.StockRepository
import com.argminres.app.domain.repository.SupplierRepository
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
    private val ingredientRepository: com.argminres.app.domain.repository.IngredientRepository,
    private val ingredientStockRepository: com.argminres.app.domain.repository.IngredientStockRepository
) {
    suspend operator fun invoke(orderId: Long) {
        val orderWithItems = purchaseOrderRepository.getPurchaseOrderById(orderId)
        
        if (orderWithItems != null && orderWithItems.order.status == "SUBMITTED") {
            // Update ingredient stock
            orderWithItems.items.forEach { item ->
                ingredientRepository.updateStock(item.ingredientId, item.quantity)
                
                // Record stock movement
                ingredientStockRepository.insertStockMovement(
                    com.argminres.app.data.local.entity.IngredientStockMovementEntity(
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
    suspend fun createSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity): Long {
        return supplierRepository.insertSupplier(supplier)
    }
    
    suspend fun updateSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity) {
        supplierRepository.updateSupplier(supplier)
    }
    
    suspend fun deleteSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity) {
        supplierRepository.deleteSupplier(supplier)
    }
}
