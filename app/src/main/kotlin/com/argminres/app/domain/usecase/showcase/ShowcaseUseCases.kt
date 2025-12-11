package com.argminres.app.domain.usecase.showcase

import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.data.local.entity.StockMovementEntity
import com.argminres.app.domain.repository.DishRepository
import com.argminres.app.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow

class GetShowcaseInventoryUseCase(
    private val dishRepository: DishRepository
) {
    operator fun invoke(): Flow<List<com.argminres.app.data.local.dao.DishWithCategory>> = dishRepository.getAllDishes()
}

class ManageShowcaseDishUseCase(
    private val dishRepository: DishRepository
) {
    suspend fun createDish(product: DishEntity): Long {
        return dishRepository.insertDish(product)
    }

    suspend fun updateDish(product: DishEntity) {
        dishRepository.updateDish(product)
    }

    suspend fun deleteDish(product: DishEntity) {
        dishRepository.deleteDish(product)
    }
    
    suspend fun getDishById(id: Long): com.argminres.app.data.local.dao.DishWithCategory? {
        return dishRepository.getDishById(id)
    }
}

class AdjustShowcaseStockUseCase(
    private val dishRepository: DishRepository,
    private val stockRepository: StockRepository
) {
    suspend operator fun invoke(
        dishId: Long,
        quantityChange: Int,
        reason: String,
        movementType: String = "ADJUSTMENT"
    ) {
        // 1. Update dish stock in showcase
        dishRepository.updateStock(dishId, quantityChange)

        // 2. Record movement
        val movement = StockMovementEntity(
            dishId = dishId,
            movementType = movementType,
            quantity = quantityChange,
            referenceId = null, // Manual adjustment has no order reference
            notes = reason
        )
        stockRepository.insertStockMovement(movement)
    }
}

class GetShowcaseStockHistoryUseCase(
    private val stockRepository: StockRepository
) {
    operator fun invoke(dishId: Long) = stockRepository.getStockMovementsForDish(dishId)
}

class GetCategoriesUseCase(
    private val dishRepository: DishRepository
) {
    operator fun invoke() = dishRepository.getAllCategories()
}
