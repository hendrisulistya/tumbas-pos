package com.argminres.app.domain.repository

import com.argminres.app.data.local.dao.SalesOrderWithItems
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.data.local.entity.SalesOrderEntity
import com.argminres.app.data.local.entity.SalesOrderItemEntity
import com.argminres.app.data.local.entity.EmployerEntity
import kotlinx.coroutines.flow.Flow

interface DishRepository {
    fun getAllDishes(): Flow<List<com.argminres.app.data.local.dao.DishWithCategory>>
    fun getDishesWithStock(): Flow<List<com.argminres.app.data.local.dao.DishWithCategory>>
    fun getAllCategories(): Flow<List<com.argminres.app.data.local.entity.CategoryEntity>>
    suspend fun getDishById(id: Long): com.argminres.app.data.local.dao.DishWithCategory?
    suspend fun getDishByBarcode(barcode: String): com.argminres.app.data.local.dao.DishWithCategory?
    fun searchDishes(query: String): Flow<List<com.argminres.app.data.local.dao.DishWithCategory>>
    suspend fun insertDish(product: DishEntity): Long
    suspend fun updateDish(product: DishEntity)
    suspend fun deleteDish(product: DishEntity)
    suspend fun updateStock(dishId: Long, quantity: Int)
}

interface SalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<SalesOrderWithItems>>
    suspend fun getSalesOrderById(id: Long): SalesOrderWithItems?
    fun getSalesOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<SalesOrderEntity>>
    suspend fun createSalesOrder(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long
    suspend fun updateSalesOrder(order: SalesOrderEntity)
}

interface StockRepository {
    fun getStockMovementsForDish(dishId: Long): Flow<List<com.argminres.app.data.local.entity.StockMovementEntity>>
    suspend fun insertStockMovement(movement: com.argminres.app.data.local.entity.StockMovementEntity)
}

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<com.argminres.app.data.local.entity.SupplierEntity>>
    suspend fun getSupplierById(id: Long): com.argminres.app.data.local.entity.SupplierEntity?
    suspend fun insertSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity): Long
    suspend fun updateSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity)
    suspend fun deleteSupplier(supplier: com.argminres.app.data.local.entity.SupplierEntity)
}

interface PurchaseOrderRepository {
    fun getAllPurchaseOrders(): Flow<List<com.argminres.app.data.local.dao.PurchaseOrderWithItems>>
    suspend fun getPurchaseOrderById(id: Long): com.argminres.app.data.local.dao.PurchaseOrderWithItems?
    suspend fun createPurchaseOrder(order: com.argminres.app.data.local.entity.PurchaseOrderEntity, items: List<com.argminres.app.data.local.entity.PurchaseOrderItemEntity>): Long
    suspend fun updatePurchaseOrder(order: com.argminres.app.data.local.entity.PurchaseOrderEntity)
    suspend fun updatePurchaseOrderStatus(orderId: Long, status: String)
}

interface ReportingRepository {
    fun getDailySalesSummary(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<List<com.argminres.app.domain.model.SalesSummary>>
    fun getTopSellingDishes(startDate: Long, endDate: Long, limit: Int, cashierId: Long? = null): Flow<List<com.argminres.app.domain.model.TopProduct>>
    fun getLowStockDishes(threshold: Int): Flow<List<com.argminres.app.domain.model.LowStockProduct>>
    fun getTotalRevenue(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<Double>
}

interface BackupRepository {
    suspend fun backupDatabase(r2Config: com.argminres.app.domain.model.R2Config): Result<String>
    suspend fun restoreDatabase(r2Config: com.argminres.app.domain.model.R2Config, backupFileName: String, namespace: String? = null): Result<Unit>
    suspend fun getBackups(r2Config: com.argminres.app.domain.model.R2Config, namespace: String? = null): Result<List<String>>
}

interface StoreSettingsRepository {
    fun getStoreSettings(): Flow<com.argminres.app.data.local.entity.StoreSettingsEntity?>
    suspend fun saveStoreSettings(settings: com.argminres.app.data.local.entity.StoreSettingsEntity)
}
