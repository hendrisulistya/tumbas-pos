package com.tumbaspos.app.domain.usecase.sales

import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.data.local.entity.SalesOrderItemEntity
import com.tumbaspos.app.domain.repository.ProductRepository
import com.tumbaspos.app.domain.repository.SalesOrderRepository

class CreateSalesOrderUseCase(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long {
        // 1. Create the order
        val orderId = salesOrderRepository.createSalesOrder(order, items)
        
        // 2. Update stock for each item
        items.forEach { item ->
            productRepository.updateStock(item.productId, -item.quantity)
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
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String) = productRepository.getProductByBarcode(barcode)
}

class SearchProductsUseCase(
    private val productRepository: ProductRepository
) {
    operator fun invoke(query: String) = productRepository.searchProducts(query)
}
