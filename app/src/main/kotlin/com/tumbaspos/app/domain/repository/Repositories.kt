package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.data.local.dao.SalesOrderWithItems
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.data.local.entity.SalesOrderItemEntity
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductById(id: Long): ProductEntity?
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun updateStock(productId: Long, quantity: Int)
}

interface SalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<SalesOrderWithItems>>
    suspend fun getSalesOrderById(id: Long): SalesOrderWithItems?
    fun getSalesOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<SalesOrderEntity>>
    suspend fun createSalesOrder(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long
    suspend fun updateSalesOrder(order: SalesOrderEntity)
}

interface StockRepository {
    fun getStockMovementsForProduct(productId: Long): Flow<List<com.tumbaspos.app.data.local.entity.StockMovementEntity>>
    suspend fun insertStockMovement(movement: com.tumbaspos.app.data.local.entity.StockMovementEntity)
}

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<com.tumbaspos.app.data.local.entity.SupplierEntity>>
    suspend fun getSupplierById(id: Long): com.tumbaspos.app.data.local.entity.SupplierEntity?
    suspend fun insertSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity): Long
    suspend fun updateSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity)
    suspend fun deleteSupplier(supplier: com.tumbaspos.app.data.local.entity.SupplierEntity)
}

interface PurchaseOrderRepository {
    fun getAllPurchaseOrders(): Flow<List<com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems>>
    suspend fun getPurchaseOrderById(id: Long): com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems?
    suspend fun createPurchaseOrder(order: com.tumbaspos.app.data.local.entity.PurchaseOrderEntity, items: List<com.tumbaspos.app.data.local.entity.PurchaseOrderItemEntity>): Long
    suspend fun updatePurchaseOrder(order: com.tumbaspos.app.data.local.entity.PurchaseOrderEntity)
    suspend fun updatePurchaseOrderStatus(orderId: Long, status: String)
}

interface ReportingRepository {
    fun getDailySalesSummary(startDate: Long, endDate: Long): Flow<List<com.tumbaspos.app.domain.model.SalesSummary>>
    fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int): Flow<List<com.tumbaspos.app.domain.model.TopProduct>>
    fun getLowStockProducts(threshold: Int): Flow<List<com.tumbaspos.app.domain.model.LowStockProduct>>
    fun getTotalRevenue(startDate: Long, endDate: Long): Flow<Double>
}

interface BackupRepository {
    suspend fun backupDatabase(r2Config: com.tumbaspos.app.domain.model.R2Config): Result<String>
    suspend fun restoreDatabase(r2Config: com.tumbaspos.app.domain.model.R2Config, backupFileName: String, namespace: String? = null): Result<Unit>
    suspend fun getBackups(r2Config: com.tumbaspos.app.domain.model.R2Config, namespace: String? = null): Result<List<String>>
}
