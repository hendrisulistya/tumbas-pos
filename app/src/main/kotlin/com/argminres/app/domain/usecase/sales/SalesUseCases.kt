package com.argminres.app.domain.usecase.sales

import com.argminres.app.data.local.entity.SalesOrderEntity
import com.argminres.app.data.local.entity.SalesOrderItemEntity
import com.argminres.app.domain.repository.DishComponentRepository
import com.argminres.app.domain.repository.DishRepository
import com.argminres.app.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.first

class CreateSalesOrderUseCase(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: DishRepository,
    private val componentRepository: DishComponentRepository
) {
    suspend operator fun invoke(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long {
        // 1. Create the order
        val orderId = salesOrderRepository.createSalesOrder(order, items)
        
        // 2. Update stock for each item
        items.forEach { item ->
            if (item.packageId != null) {
                // It's a package, deduct stock from all components
                val components = componentRepository.getComponentsForPackage(item.packageId).first()
                components.forEach { component ->
                    productRepository.updateStock(component.dish.id, -item.quantity)
                }
            } else if (item.dishId != null) {
                // Not a package, deduct stock from the dish itself
                productRepository.updateStock(item.dishId, -item.quantity)
            }
        }
        
        return orderId
    }
}

class GetSalesOrdersUseCase(
    private val salesOrderRepository: SalesOrderRepository
) {
    operator fun invoke() = salesOrderRepository.getAllSalesOrders()
    suspend fun getLastOrderByNumber(prefix: String) = salesOrderRepository.getLastOrderByNumber(prefix)
}


class SearchDishesUseCase(
    private val productRepository: DishRepository
) {
    operator fun invoke(query: String) = productRepository.searchDishes(query)
}
