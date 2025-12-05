package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.ProductDao
import com.tumbaspos.app.data.local.dao.ProductWithCategory
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val categoryDao: com.tumbaspos.app.data.local.dao.CategoryDao
) : ProductRepository {
    override fun getAllProducts(): Flow<List<ProductWithCategory>> {
        return productDao.getAllProducts()
    }

    override fun getAllCategories(): Flow<List<com.tumbaspos.app.data.local.entity.CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun getProductById(id: Long): ProductWithCategory? {
        return productDao.getProductById(id)
    }

    override suspend fun getProductByBarcode(barcode: String): ProductWithCategory? {
        return productDao.getProductByBarcode(barcode)
    }

    override fun searchProducts(query: String): Flow<List<ProductWithCategory>> {
        return productDao.searchProducts(query)
    }

    override suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    override suspend fun updateStock(productId: Long, quantity: Int) {
        productDao.updateStock(productId, quantity)
    }
}
