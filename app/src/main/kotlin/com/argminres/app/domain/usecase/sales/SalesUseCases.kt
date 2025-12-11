package com.argminres.app.domain.usecase.sales

import com.argminres.app.data.local.entity.SalesOrderEntity
import com.argminres.app.data.local.entity.SalesOrderItemEntity
import com.argminres.app.domain.repository.DishRepository
import com.argminres.app.domain.repository.SalesOrderRepository

class CreateSalesOrderUseCase(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: DishRepository
) {
    suspend operator fun invoke(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long {
        // 1. Create the order
        val orderId = salesOrderRepository.createSalesOrder(order, items)
        
        // 2. Update stock for each item
        items.forEach { item ->
            productRepository.updateStock(item.dishId, -item.quantity)
        }
        
        return orderId
    }
}

class GetSalesOrdersUseCase(
    private val salesOrderRepository: SalesOrderRepository
) {
    operator fun invoke() = salesOrderRepository.getAllSalesOrders()
}

class GetProductByBarcodeUseCase(
    private val productRepository: DishRepository
) {
    suspend operator fun invoke(barcode: String) = productRepository.getDishByBarcode(barcode)
}

class SearchDishesUseCase(
    private val productRepository: DishRepository
) {
    operator fun invoke(query: String) = productRepository.searchDishes(query)
}
