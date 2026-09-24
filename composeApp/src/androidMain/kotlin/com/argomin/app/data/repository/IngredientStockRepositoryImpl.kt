package com.argomin.app.data.repository

import com.argomin.app.data.local.dao.IngredientStockDao
import com.argomin.app.data.local.entity.IngredientStockMovementEntity
import com.argomin.app.domain.repository.IngredientStockRepository
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
