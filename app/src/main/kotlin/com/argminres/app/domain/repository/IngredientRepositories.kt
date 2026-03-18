package com.argminres.app.domain.repository

import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.data.local.entity.IngredientStockMovementEntity
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<IngredientEntity>>
    fun getIngredientsWithStock(): Flow<List<IngredientEntity>>
    suspend fun getIngredientById(id: Long): IngredientEntity?
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>
    suspend fun insertIngredient(ingredient: IngredientEntity): Long
    suspend fun updateIngredient(ingredient: IngredientEntity)
    suspend fun deleteIngredient(ingredient: IngredientEntity)
    suspend fun updateStock(ingredientId: Long, quantity: Double)
}

interface IngredientStockRepository {
    fun getStockMovementsForIngredient(ingredientId: Long): Flow<List<IngredientStockMovementEntity>>
    fun getRecentStockMovements(): Flow<List<IngredientStockMovementEntity>>
    suspend fun insertStockMovement(movement: IngredientStockMovementEntity): Long
}
