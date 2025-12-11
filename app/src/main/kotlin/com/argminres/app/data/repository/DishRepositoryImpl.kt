package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.DishDao
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.Flow

class DishRepositoryImpl(
    private val productDao: DishDao,
    private val categoryDao: com.argminres.app.data.local.dao.CategoryDao
) : DishRepository {
    override fun getAllDishes(): Flow<List<DishWithCategory>> {
        return productDao.getAllDishes()
    }

    override fun getAllCategories(): Flow<List<com.argminres.app.data.local.entity.CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun getDishById(id: Long): DishWithCategory? {
        return productDao.getDishById(id)
    }

    override suspend fun getDishByBarcode(barcode: String): DishWithCategory? {
        return productDao.getDishByBarcode(barcode)
    }

    override fun searchDishes(query: String): Flow<List<DishWithCategory>> {
        return productDao.searchDishes(query)
    }

    override suspend fun insertDish(product: DishEntity): Long {
        return productDao.insertDish(product)
    }

    override suspend fun updateDish(product: DishEntity) {
        productDao.updateDish(product)
    }

    override suspend fun deleteDish(product: DishEntity) {
        productDao.deleteDish(product)
    }

    override suspend fun updateStock(productId: Long, quantity: Int) {
        productDao.updateStock(productId, quantity)
    }
}
