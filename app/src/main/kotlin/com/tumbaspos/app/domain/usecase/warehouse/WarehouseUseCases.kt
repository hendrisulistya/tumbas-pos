package com.tumbaspos.app.domain.usecase.warehouse

import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.local.entity.StockMovementEntity
import com.tumbaspos.app.domain.repository.ProductRepository
import com.tumbaspos.app.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow

class GetInventoryUseCase(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<com.tumbaspos.app.data.local.dao.ProductWithCategory>> = productRepository.getAllProducts()
}

class ManageProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend fun createProduct(product: ProductEntity): Long {
        return productRepository.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productRepository.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productRepository.deleteProduct(product)
    }
    
    suspend fun getProductById(id: Long): com.tumbaspos.app.data.local.dao.ProductWithCategory? {
        return productRepository.getProductById(id)
    }
}

class AdjustStockUseCase(
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(
        productId: Long,
        quantityChange: Int,
        reason: String,
        movementType: String = "ADJUSTMENT"
    ) {
        // 1. Update product stock
        productRepository.updateStock(productId, quantityChange)

        // 2. Record movement
        val movement = StockMovementEntity(
            productId = productId,
            movementType = movementType,
            quantity = quantityChange,
            referenceId = null, // Manual adjustment has no order reference
            notes = reason
        )
        stockRepository.insertStockMovement(movement)
    }
}

class GetStockHistoryUseCase(
    private val stockRepository: StockRepository
) {
    operator fun invoke(productId: Long) = stockRepository.getStockMovementsForProduct(productId)
}

class GetCategoriesUseCase(
    private val productRepository: ProductRepository
) {
    operator fun invoke() = productRepository.getAllCategories()
}
