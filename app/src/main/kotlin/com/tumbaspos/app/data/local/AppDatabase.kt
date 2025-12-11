package com.tumbaspos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tumbaspos.app.data.local.dao.*
import com.tumbaspos.app.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        com.tumbaspos.app.data.local.entity.CategoryEntity::class,
        com.tumbaspos.app.data.local.entity.CustomerEntity::class,
        SalesOrderEntity::class,
        SalesOrderItemEntity::class,
        com.tumbaspos.app.data.local.entity.SupplierEntity::class,
        com.tumbaspos.app.data.local.entity.PurchaseOrderEntity::class,
        com.tumbaspos.app.data.local.entity.PurchaseOrderItemEntity::class,
        com.tumbaspos.app.data.local.entity.StockMovementEntity::class,
        com.tumbaspos.app.data.local.entity.StoreSettingsEntity::class,
        EmployerEntity::class,
        com.tumbaspos.app.data.local.entity.AuditLogEntity::class
    ],
    version = 2,
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
    abstract fun storeSettingsDao(): com.tumbaspos.app.data.local.dao.StoreSettingsDao
    abstract fun employerDao(): EmployerDao
    abstract fun auditLogDao(): com.tumbaspos.app.data.local.dao.AuditLogDao
}
