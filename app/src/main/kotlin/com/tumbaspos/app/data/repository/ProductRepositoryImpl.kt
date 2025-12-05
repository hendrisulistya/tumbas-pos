package com.tumbaspos.app.data.repository

import com.tumbaspos.app.data.local.dao.ProductDao
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {
    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    override suspend fun getProductById(id: Long): ProductEntity? {
        return productDao.getProductById(id)
    }

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? {
        return productDao.getProductByBarcode(barcode)
    }

    override fun searchProducts(query: String): Flow<List<ProductEntity>> {
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
