package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.IngredientDao
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {
    override fun getAllIngredients(): Flow<List<IngredientEntity>> = ingredientDao.getAllIngredients()
    
    override fun getIngredientsWithStock(): Flow<List<IngredientEntity>> = ingredientDao.getIngredientsWithStock()
    
    override suspend fun getIngredientById(id: Long): IngredientEntity? = ingredientDao.getIngredientById(id)
    
    override fun searchIngredients(query: String): Flow<List<IngredientEntity>> = ingredientDao.searchIngredients(query)
    
    override suspend fun insertIngredient(ingredient: IngredientEntity): Long = ingredientDao.insertIngredient(ingredient)
    
    override suspend fun updateIngredient(ingredient: IngredientEntity) = ingredientDao.updateIngredient(ingredient)
    
    override suspend fun deleteIngredient(ingredient: IngredientEntity) = ingredientDao.deleteIngredient(ingredient)
    
    override suspend fun updateStock(ingredientId: Long, quantity: Double) = ingredientDao.updateStock(ingredientId, quantity)
}
