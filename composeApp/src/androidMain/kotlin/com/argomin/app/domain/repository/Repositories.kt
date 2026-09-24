package com.argomin.app.domain.repository

import com.argomin.app.data.local.dao.SalesOrderWithItems
import com.argomin.app.data.local.entity.DishEntity
import com.argomin.app.data.local.entity.SalesOrderEntity
import com.argomin.app.data.local.entity.SalesOrderItemEntity
import com.argomin.app.data.local.entity.EmployerEntity
import kotlinx.coroutines.flow.Flow

interface DishRepository {
    fun getAllDishes(): Flow<List<com.argomin.app.data.local.dao.DishWithCategory>>
    fun getDishesWithStock(): Flow<List<com.argomin.app.data.local.dao.DishWithCategory>>
    fun getAllCategories(): Flow<List<com.argomin.app.data.local.entity.CategoryEntity>>
    suspend fun getDishById(id: Long): com.argomin.app.data.local.dao.DishWithCategory?
    fun searchDishes(query: String): Flow<List<com.argomin.app.data.local.dao.DishWithCategory>>
    suspend fun insertDish(product: DishEntity): Long
    suspend fun updateDish(product: DishEntity)
    suspend fun deleteDish(product: DishEntity)
    suspend fun updateStock(dishId: Long, quantity: Int)
}

interface PackageRepository {
    fun getAllPackages(): Flow<List<com.argomin.app.data.local.entity.PackageEntity>>
    suspend fun getPackageById(id: Long): com.argomin.app.data.local.entity.PackageEntity?
    fun searchPackages(query: String): Flow<List<com.argomin.app.data.local.entity.PackageEntity>>
    suspend fun insertPackage(pkg: com.argomin.app.data.local.entity.PackageEntity): Long
    suspend fun updatePackage(pkg: com.argomin.app.data.local.entity.PackageEntity)
    suspend fun deletePackage(pkg: com.argomin.app.data.local.entity.PackageEntity)
}

interface SalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<SalesOrderWithItems>>
    suspend fun getSalesOrderById(id: Long): SalesOrderWithItems?
    fun getSalesOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<SalesOrderEntity>>
    suspend fun createSalesOrder(order: SalesOrderEntity, items: List<SalesOrderItemEntity>): Long
    suspend fun updateSalesOrder(order: SalesOrderEntity)
    suspend fun getLastOrderByNumber(prefix: String): SalesOrderEntity?
    suspend fun getSoldQuantitiesByDish(startDate: Long, endDate: Long): List<com.argomin.app.data.local.dao.DishSoldQuantity>
}

interface StockRepository {
    fun getStockMovementsForDish(dishId: Long): Flow<List<com.argomin.app.data.local.entity.StockMovementEntity>>
    suspend fun insertStockMovement(movement: com.argomin.app.data.local.entity.StockMovementEntity)
}

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<com.argomin.app.data.local.entity.SupplierEntity>>
    suspend fun getSupplierById(id: Long): com.argomin.app.data.local.entity.SupplierEntity?
    suspend fun insertSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity): Long
    suspend fun updateSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity)
    suspend fun deleteSupplier(supplier: com.argomin.app.data.local.entity.SupplierEntity)
}

interface PurchaseOrderRepository {
    fun getAllPurchaseOrders(): Flow<List<com.argomin.app.data.local.dao.PurchaseOrderWithItems>>
    suspend fun getPurchaseOrderById(id: Long): com.argomin.app.data.local.dao.PurchaseOrderWithItems?
    suspend fun createPurchaseOrder(order: com.argomin.app.data.local.entity.PurchaseOrderEntity, items: List<com.argomin.app.data.local.entity.PurchaseOrderItemEntity>): Long
    suspend fun updatePurchaseOrder(order: com.argomin.app.data.local.entity.PurchaseOrderEntity)
    suspend fun updatePurchaseOrderStatus(orderId: Long, status: String)
}

interface ReportingRepository {
    fun getDailySalesSummary(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<List<com.argomin.app.domain.model.SalesSummary>>
    fun getTopSellingDishes(startDate: Long, endDate: Long, limit: Int, cashierId: Long? = null): Flow<List<com.argomin.app.domain.model.TopProduct>>
    fun getTopSellingPackages(startDate: Long, endDate: Long, limit: Int, cashierId: Long? = null): Flow<List<com.argomin.app.domain.model.TopProduct>>
    fun getLowStockDishes(threshold: Int): Flow<List<com.argomin.app.domain.model.LowStockProduct>>
    fun getTotalRevenue(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<Double>
    fun getTotalIngredientCost(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<Double>
    fun getTotalWasteValue(startDate: Long, endDate: Long, cashierId: Long? = null): Flow<Double>
    
    fun getIngredientUsageBySession(sessionId: Long): Flow<List<com.argomin.app.data.local.entity.IngredientUsageEntity>>
    fun getDishUsageBySession(sessionId: Long): Flow<List<com.argomin.app.data.local.entity.WasteRecordEntity>>
    fun getAggregatedIngredientUsage(startDate: Long, endDate: Long): Flow<List<com.argomin.app.domain.model.AggregatedIngredientUsage>>
    fun getAggregatedDishUsage(startDate: Long, endDate: Long): Flow<List<com.argomin.app.domain.model.AggregatedDishUsage>>
    fun getAggregatedUsageReport(startDate: Long, endDate: Long): Flow<com.argomin.app.domain.model.AggregatedUsageState>
    fun getDishSalesByCashier(startDate: Long, endDate: Long): Flow<List<com.argomin.app.domain.model.CashierDishSales>>
}

interface BackupRepository {
    suspend fun backupDatabase(): Result<String>
    suspend fun restoreDatabase(backupFileName: String): Result<Unit>
    suspend fun getBackups(): Result<List<String>>
}

interface StoreSettingsRepository {
    fun getStoreSettings(): Flow<com.argomin.app.data.local.entity.StoreSettingsEntity?>
    suspend fun saveStoreSettings(settings: com.argomin.app.data.local.entity.StoreSettingsEntity)
}
