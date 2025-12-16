package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.IngredientDao
import com.argminres.app.data.local.dao.IngredientWithCategory
import com.argminres.app.data.local.dao.IngredientCategoryDao
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.data.local.entity.IngredientCategoryEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao,
    private val categoryDao: IngredientCategoryDao
) : IngredientRepository {
    override fun getAllIngredients(): Flow<List<IngredientWithCategory>> = ingredientDao.getAllIngredients()
    
    override fun getIngredientsWithStock(): Flow<List<IngredientWithCategory>> = ingredientDao.getIngredientsWithStock()
    
    override suspend fun getIngredientById(id: Long): IngredientWithCategory? = ingredientDao.getIngredientById(id)
    
    override fun searchIngredients(query: String): Flow<List<IngredientWithCategory>> = ingredientDao.searchIngredients(query)
    
    override suspend fun insertIngredient(ingredient: IngredientEntity): Long = ingredientDao.insertIngredient(ingredient)
    
    override suspend fun updateIngredient(ingredient: IngredientEntity) = ingredientDao.updateIngredient(ingredient)
    
    override suspend fun deleteIngredient(ingredient: IngredientEntity) = ingredientDao.deleteIngredient(ingredient)
    
    override suspend fun updateStock(ingredientId: Long, quantity: Double) = ingredientDao.updateStock(ingredientId, quantity)
    
    override fun getAllCategories(): Flow<List<IngredientCategoryEntity>> = categoryDao.getAllCategories()
    
    override suspend fun insertCategory(category: IngredientCategoryEntity): Long = categoryDao.insertCategory(category)
}
