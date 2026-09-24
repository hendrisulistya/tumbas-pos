package com.argomin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.argomin.app.data.local.dao.*
import com.argomin.app.data.local.entity.*

@Database(
    entities = [
        DishEntity::class,
        com.argomin.app.data.local.entity.CategoryEntity::class,
        com.argomin.app.data.local.entity.CustomerEntity::class,
        SalesOrderEntity::class,
        SalesOrderItemEntity::class,
        com.argomin.app.data.local.entity.SupplierEntity::class,
        com.argomin.app.data.local.entity.PurchaseOrderEntity::class,
        com.argomin.app.data.local.entity.PurchaseOrderItemEntity::class,
        com.argomin.app.data.local.entity.StockMovementEntity::class,
        com.argomin.app.data.local.entity.StoreSettingsEntity::class,
        EmployerEntity::class,
        com.argomin.app.data.local.entity.AuditLogEntity::class,
        IngredientEntity::class,
        IngredientStockMovementEntity::class,
        DailySessionEntity::class,
        WasteRecordEntity::class,
        IngredientWasteRecordEntity::class,
        IngredientUsageEntity::class,
        DishComponentEntity::class,
        DishHistoryEntity::class,
        IngredientHistoryEntity::class,
        PackageEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dishDao(): DishDao
    abstract fun contactDao(): ContactDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun stockDao(): StockDao
    abstract fun reportingDao(): ReportingDao
    abstract fun customerDao(): com.argomin.app.data.local.dao.CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storeSettingsDao(): com.argomin.app.data.local.dao.StoreSettingsDao
    abstract fun employerDao(): EmployerDao
    abstract fun auditLogDao(): com.argomin.app.data.local.dao.AuditLogDao
    abstract fun ingredientDao(): com.argomin.app.data.local.dao.IngredientDao
    abstract fun ingredientStockDao(): com.argomin.app.data.local.dao.IngredientStockDao
    abstract fun dailySessionDao(): com.argomin.app.data.local.dao.DailySessionDao
    abstract fun wasteRecordDao(): com.argomin.app.data.local.dao.WasteRecordDao
    abstract fun ingredientUsageDao(): com.argomin.app.data.local.dao.IngredientUsageDao
    abstract fun ingredientWasteRecordDao(): com.argomin.app.data.local.dao.IngredientWasteRecordDao
    abstract fun dishComponentDao(): DishComponentDao
    abstract fun dishHistoryDao(): DishHistoryDao
    abstract fun ingredientHistoryDao(): IngredientHistoryDao
    abstract fun packageDao(): PackageDao
}
