package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.IngredientStockDao
import com.argminres.app.data.local.entity.IngredientStockMovementEntity
import com.argminres.app.domain.repository.IngredientStockRepository
import kotlinx.coroutines.flow.Flow

class IngredientStockRepositoryImpl(
    private val ingredientStockDao: IngredientStockDao
) : IngredientStockRepository {
    override fun getStockMovementsForIngredient(ingredientId: Long): Flow<List<IngredientStockMovementEntity>> =
        ingredientStockDao.getStockMovementsForIngredient(ingredientId)
    
    override fun getRecentStockMovements(): Flow<List<IngredientStockMovementEntity>> =
        ingredientStockDao.getRecentStockMovements()
    
    override suspend fun insertStockMovement(movement: IngredientStockMovementEntity): Long =
        ingredientStockDao.insertStockMovement(movement)
}
