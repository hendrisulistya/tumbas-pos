package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM dishes ORDER BY name ASC")
    fun getAllDishes(): Flow<List<DishWithCategory>>

    @Transaction
    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getDishById(id: Long): DishWithCategory?

    @Transaction
    @Query("SELECT * FROM dishes WHERE barcode = :barcode")
    suspend fun getDishByBarcode(barcode: String): DishWithCategory?

    @Transaction
    @Query("SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    fun searchDishes(query: String): Flow<List<DishWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: DishEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dishes: List<DishEntity>)

    @Update
    suspend fun updateDish(dish: DishEntity)

    @Delete
    suspend fun deleteDish(dish: DishEntity)

    @Query("UPDATE dishes SET stock = stock + :quantity WHERE id = :dishId")
    suspend fun updateStock(dishId: Long, quantity: Int)
}

data class DishWithCategory(
    @Embedded val dish: DishEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

@Dao
interface ContactDao {
    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
}

@Dao
interface PurchaseOrderDao {
    @Transaction
    @Query("SELECT * FROM purchase_orders ORDER BY orderDate DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getPurchaseOrderById(id: Long): PurchaseOrderWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(order: PurchaseOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrderItems(items: List<PurchaseOrderItemEntity>)

    @Update
    suspend fun updatePurchaseOrder(order: PurchaseOrderEntity)
    
    @Query("DELETE FROM purchase_order_items WHERE purchaseOrderId = :orderId")
    suspend fun deletePurchaseOrderItems(orderId: Long)
}

@Dao
interface SalesOrderDao {
    @Transaction
    @Query("SELECT * FROM sales_orders ORDER BY orderDate DESC")
    fun getAllSalesOrders(): Flow<List<SalesOrderWithItems>>

    @Transaction
    @Query("SELECT * FROM sales_orders WHERE id = :id")
    suspend fun getSalesOrderById(id: Long): SalesOrderWithItems?
    
    @Query("SELECT * FROM sales_orders WHERE orderDate BETWEEN :startDate AND :endDate")
    fun getSalesOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<SalesOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrder(order: SalesOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrderItems(items: List<SalesOrderItemEntity>)

    @Update
    suspend fun updateSalesOrder(order: SalesOrderEntity)
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_movements WHERE dishId = :dishId ORDER BY createdAt DESC")
    fun getStockMovementsForDish(dishId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)
}

@Dao
interface ReportingDao {
    @Query("""
        SELECT DATE(orderDate / 1000, 'unixepoch') as date,
               SUM(totalAmount) as totalSales,
               COUNT(*) as totalTransactions
        FROM sales_orders
        WHERE orderDate >= :startDate AND orderDate <= :endDate
          AND (:cashierId IS NULL OR cashierId = :cashierId)
        GROUP BY DATE(orderDate / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    fun getDailySalesSummary(startDate: Long, endDate: Long, cashierId: Long?): Flow<List<com.argminres.app.domain.model.SalesSummary>>

    @Query("""
        SELECT 
            p.id as dishId,
            p.name as productName,
            SUM(soi.quantity) as quantitySold,
            SUM(soi.subtotal) as totalRevenue
        FROM sales_order_items soi
        INNER JOIN dishes p ON soi.dishId = p.id
        INNER JOIN sales_orders so ON soi.salesOrderId = so.id
        WHERE so.orderDate >= :startDate AND so.orderDate <= :endDate
          AND (:cashierId IS NULL OR so.cashierId = :cashierId)
        GROUP BY p.id
        ORDER BY quantitySold DESC
        LIMIT :limit
    """)
    fun getTopSellingDishes(startDate: Long, endDate: Long, limit: Int, cashierId: Long?): Flow<List<com.argminres.app.domain.model.TopProduct>>

    @Query("""
        SELECT 
            id as dishId, 
            name as productName, 
            stock as currentStock, 
            :threshold as threshold 
        FROM dishes 
        WHERE stock <= :threshold 
        ORDER BY stock ASC
    """)
    fun getLowStockDishes(threshold: Int): Flow<List<com.argminres.app.domain.model.LowStockProduct>>

    @Query("""
        SELECT SUM(totalAmount)
        FROM sales_orders
        WHERE orderDate >= :startDate AND orderDate <= :endDate
          AND (:cashierId IS NULL OR cashierId = :cashierId)
    """)
    fun getTotalRevenue(startDate: Long, endDate: Long, cashierId: Long?): Flow<Double?>
}

// Relations
data class PurchaseOrderWithItems(
    @Embedded val order: PurchaseOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseOrderId"
    )
    val items: List<PurchaseOrderItemEntity>,
    val cashierName: String? = null // Will be populated manually
)

data class SalesOrderWithItems(
    @Embedded val order: SalesOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "salesOrderId"
    )
    val items: List<SalesOrderItemEntity>,
    val cashierName: String? = null // Will be populated manually
)

@Dao
interface StoreSettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 1")
    fun getStoreSettings(): Flow<StoreSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: StoreSettingsEntity)
}
