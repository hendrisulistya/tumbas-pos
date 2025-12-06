package com.tumbaspos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tumbaspos.app.data.local.dao.*
import com.tumbaspos.app.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        SalesOrderEntity::class,
        SalesOrderItemEntity::class,
        StockMovementEntity::class,
        SupplierEntity::class,
        PurchaseOrderEntity::class,
        PurchaseOrderItemEntity::class,
        com.tumbaspos.app.data.local.entity.CustomerEntity::class,
        CategoryEntity::class,
        StoreSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun contactDao(): ContactDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun stockDao(): StockDao
    abstract fun reportingDao(): ReportingDao
    abstract fun customerDao(): com.tumbaspos.app.data.local.dao.CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storeSettingsDao(): StoreSettingsDao
}
