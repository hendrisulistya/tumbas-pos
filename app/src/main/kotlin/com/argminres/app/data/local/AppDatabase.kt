package com.argminres.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.argminres.app.data.local.dao.*
import com.argminres.app.data.local.entity.*

@Database(
    entities = [
        DishEntity::class,
        com.argminres.app.data.local.entity.CategoryEntity::class,
        com.argminres.app.data.local.entity.CustomerEntity::class,
        SalesOrderEntity::class,
        SalesOrderItemEntity::class,
        com.argminres.app.data.local.entity.SupplierEntity::class,
        com.argminres.app.data.local.entity.PurchaseOrderEntity::class,
        com.argminres.app.data.local.entity.PurchaseOrderItemEntity::class,
        com.argminres.app.data.local.entity.StockMovementEntity::class,
        com.argminres.app.data.local.entity.StoreSettingsEntity::class,
        EmployerEntity::class,
        com.argminres.app.data.local.entity.AuditLogEntity::class,
        IngredientEntity::class,
        IngredientCategoryEntity::class,
        IngredientStockMovementEntity::class,
        DailySessionEntity::class,
        WasteRecordEntity::class,
        IngredientUsageEntity::class,
        DishComponentEntity::class,
        DishHistoryEntity::class,
        IngredientHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dishDao(): DishDao
    abstract fun contactDao(): ContactDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun stockDao(): StockDao
    abstract fun reportingDao(): ReportingDao
    abstract fun customerDao(): com.argminres.app.data.local.dao.CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storeSettingsDao(): com.argminres.app.data.local.dao.StoreSettingsDao
    abstract fun employerDao(): EmployerDao
    abstract fun auditLogDao(): com.argminres.app.data.local.dao.AuditLogDao
    abstract fun ingredientDao(): com.argminres.app.data.local.dao.IngredientDao
    abstract fun ingredientCategoryDao(): com.argminres.app.data.local.dao.IngredientCategoryDao
    abstract fun ingredientStockDao(): com.argminres.app.data.local.dao.IngredientStockDao
    abstract fun dailySessionDao(): com.argminres.app.data.local.dao.DailySessionDao
    abstract fun wasteRecordDao(): com.argminres.app.data.local.dao.WasteRecordDao
    abstract fun ingredientUsageDao(): com.argminres.app.data.local.dao.IngredientUsageDao
    abstract fun dishComponentDao(): DishComponentDao
    abstract fun dishHistoryDao(): DishHistoryDao
    abstract fun ingredientHistoryDao(): IngredientHistoryDao
}
