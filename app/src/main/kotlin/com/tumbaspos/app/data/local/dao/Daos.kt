package com.tumbaspos.app.data.local.dao

import androidx.room.*
import com.tumbaspos.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun updateStock(productId: Long, quantity: Int)
}

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
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)
}

@Dao
interface ReportingDao {
    @Query("""
        SELECT 
            date(orderDate / 1000, 'unixepoch', 'localtime') as date, 
            SUM(totalAmount) as totalSales, 
            COUNT(*) as totalTransactions 
        FROM sales_orders 
        WHERE orderDate BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date DESC
    """)
    fun getDailySalesSummary(startDate: Long, endDate: Long): Flow<List<com.tumbaspos.app.domain.model.SalesSummary>>

    @Query("""
        SELECT 
            p.id as productId, 
            p.name as productName, 
            SUM(i.quantity) as quantitySold, 
            SUM(i.subtotal) as totalRevenue 
        FROM sales_order_items i 
        JOIN products p ON i.productId = p.id 
        JOIN sales_orders o ON i.salesOrderId = o.id 
        WHERE o.orderDate BETWEEN :startDate AND :endDate 
        GROUP BY p.id 
        ORDER BY quantitySold DESC 
        LIMIT :limit
    """)
    fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int): Flow<List<com.tumbaspos.app.domain.model.TopProduct>>

    @Query("""
        SELECT 
            id as productId, 
            name as productName, 
            stock as currentStock, 
            :threshold as threshold 
        FROM products 
        WHERE stock <= :threshold 
        ORDER BY stock ASC
    """)
    fun getLowStockProducts(threshold: Int): Flow<List<com.tumbaspos.app.domain.model.LowStockProduct>>

    @Query("SELECT SUM(totalAmount) FROM sales_orders WHERE orderDate BETWEEN :startDate AND :endDate")
    fun getTotalRevenue(startDate: Long, endDate: Long): Flow<Double?>
}

// Relations
data class PurchaseOrderWithItems(
    @Embedded val order: PurchaseOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseOrderId"
    )
    val items: List<PurchaseOrderItemEntity>
)

data class SalesOrderWithItems(
    @Embedded val order: SalesOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "salesOrderId"
    )
    val items: List<SalesOrderItemEntity>
)
