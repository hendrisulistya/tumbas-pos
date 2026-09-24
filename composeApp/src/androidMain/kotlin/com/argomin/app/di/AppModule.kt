package com.argomin.app.di

import androidx.room.Room
import com.argomin.app.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.component.get
import com.argomin.app.data.local.dao.DishDao
import com.argomin.app.data.local.dao.CustomerDao
import com.argomin.app.data.repository.SettingsRepository

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "padang_pos_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().dishDao() }
    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().purchaseOrderDao() }
    single { get<AppDatabase>().salesOrderDao() }
    single { get<AppDatabase>().stockDao() }
    single { get<AppDatabase>().reportingDao() }
    single { get<AppDatabase>().customerDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().storeSettingsDao() }
    single { get<AppDatabase>().employerDao() }
    single { get<AppDatabase>().auditLogDao() }
    single { get<AppDatabase>().ingredientDao() }
    single { get<AppDatabase>().ingredientStockDao() }
    single { get<AppDatabase>().dailySessionDao() }
    single { get<AppDatabase>().wasteRecordDao() }
    single { get<AppDatabase>().ingredientUsageDao() }
    single { get<AppDatabase>().ingredientWasteRecordDao() }
    single { get<AppDatabase>().dishComponentDao() }
    single { get<AppDatabase>().dishHistoryDao() }
    single { get<AppDatabase>().ingredientHistoryDao() }
    single { get<AppDatabase>().packageDao() }

    // Repositories
    single<com.argomin.app.domain.repository.DishRepository> { 
        com.argomin.app.data.repository.DishRepositoryImpl(get(), get()) 
    }
    single<com.argomin.app.domain.repository.SalesOrderRepository> { 
        com.argomin.app.data.repository.SalesOrderRepositoryImpl(get()) 
    }
    single<com.argomin.app.domain.repository.PackageRepository> {
        com.argomin.app.data.repository.PackageRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.IngredientStockRepository> {
        com.argomin.app.data.repository.IngredientStockRepositoryImpl(get())
    }
    
    single<com.argomin.app.domain.repository.DailySessionRepository> {
        com.argomin.app.data.repository.DailySessionRepositoryImpl(get())
    }
    
    single<com.argomin.app.domain.repository.WasteRecordRepository> {
        com.argomin.app.data.repository.WasteRecordRepositoryImpl(get())
    }
    
    single<com.argomin.app.domain.repository.IngredientUsageRepository> {
        com.argomin.app.data.repository.IngredientUsageRepositoryImpl(get())
    }
    
    single<com.argomin.app.domain.repository.IngredientWasteRecordRepository> {
        com.argomin.app.data.repository.IngredientWasteRecordRepositoryImpl(get())
    }
    
    single<com.argomin.app.domain.repository.StockRepository> {
        com.argomin.app.data.repository.StockRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.SupplierRepository> { 
        com.argomin.app.data.repository.SupplierRepositoryImpl(get()) 
    }
    single<com.argomin.app.domain.repository.PurchaseOrderRepository> { 
        com.argomin.app.data.repository.PurchaseOrderRepositoryImpl(get()) 
    }
    single<com.argomin.app.domain.repository.DishComponentRepository> {
        com.argomin.app.data.repository.DishComponentRepositoryImpl(get(), get())
    }
    single<com.argomin.app.domain.repository.IngredientRepository> {
        com.argomin.app.data.repository.IngredientRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.DishHistoryRepository> {
        com.argomin.app.data.repository.DishHistoryRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.IngredientHistoryRepository> {
        com.argomin.app.data.repository.IngredientHistoryRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.ReportingRepository> { 
        com.argomin.app.data.repository.ReportingRepositoryImpl(get()) 
    }
    single { com.argomin.app.data.repository.SettingsRepository(get()) }
    single<com.argomin.app.domain.repository.BackupRepository> { 
        com.argomin.app.data.repository.BackupRepositoryImpl(get(), get()) 
    }
    single<com.argomin.app.domain.repository.CustomerRepository> {
        com.argomin.app.domain.repository.CustomerRepositoryImpl(get())
    }
    single { get<AppDatabase>().customerDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().storeSettingsDao() }
    single { get<AppDatabase>().employerDao() }
    single<com.argomin.app.domain.repository.ImageRepository> {
        com.argomin.app.data.repository.ImageRepositoryImpl()
    }
    single<com.argomin.app.domain.repository.StoreSettingsRepository> {
        com.argomin.app.data.repository.StoreSettingsRepositoryImpl(get())
    }
    single<com.argomin.app.domain.repository.EmployerRepository> {
        com.argomin.app.data.repository.EmployerRepositoryImpl(get(), androidContext())
    }
    

    // Use Cases
    factory { com.argomin.app.domain.usecase.sales.CreateSalesOrderUseCase(get(), get(), get()) }
    factory { com.argomin.app.domain.usecase.sales.GetSalesOrdersUseCase(get()) }
    factory { com.argomin.app.domain.usecase.sales.SearchDishesUseCase(get()) }

    // Showcase Use Cases
    factory { com.argomin.app.domain.usecase.showcase.GetShowcaseInventoryUseCase(get()) }
    factory { com.argomin.app.domain.usecase.showcase.ManageShowcaseDishUseCase(get()) }
    factory { com.argomin.app.domain.usecase.showcase.AdjustShowcaseStockUseCase(get(), get()) }
    factory { com.argomin.app.domain.usecase.showcase.GetShowcaseStockHistoryUseCase(get()) }
    factory { com.argomin.app.domain.usecase.showcase.GetCategoriesUseCase(get()) }

    // Ingredient Use Cases
    factory { com.argomin.app.domain.usecase.ingredient.SearchIngredientsUseCase(get()) }
    factory { com.argomin.app.domain.usecase.ingredient.GetIngredientsUseCase(get()) }
    
    // Session Use Cases
    factory { com.argomin.app.domain.usecase.session.EndOfDayUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { com.argomin.app.domain.usecase.session.StartDailySessionUseCase(get()) }
    factory { com.argomin.app.domain.usecase.session.CheckAutoDailyCloseUseCase(get()) }
    factory { com.argomin.app.domain.usecase.session.SessionCheckUseCase(get()) }

    // Dish Use Cases
    factory { com.argomin.app.domain.usecase.dish.ManageDishImageUseCase(get(), get()) }
    
    // Recipe Use Cases
    factory { com.argomin.app.domain.usecase.recipe.GetPackageComponentsUseCase(get()) }
    factory { com.argomin.app.domain.usecase.recipe.GetPackageAvailabilityUseCase(get()) }
    factory { com.argomin.app.domain.usecase.recipe.ManageRecipeUseCase(get()) }
    factory { com.argomin.app.domain.usecase.recipe.GetPackageDishesUseCase(get()) }

    // Purchase Order Use Cases
    factory { com.argomin.app.domain.usecase.purchase.GetPurchaseOrdersUseCase(get()) }
    factory { com.argomin.app.domain.usecase.purchase.CreatePurchaseOrderUseCase(get()) }
    factory { com.argomin.app.domain.usecase.purchase.ReceivePurchaseOrderUseCase(get(), get(), get()) }
    factory { com.argomin.app.domain.usecase.purchase.GetSuppliersUseCase(get()) }
    factory { com.argomin.app.domain.usecase.purchase.ManageSupplierUseCase(get()) }

    // Reporting Use Cases
    factory { com.argomin.app.domain.usecase.reporting.GetDashboardDataUseCase(get()) }
    factory { com.argomin.app.domain.usecase.reporting.GetSalesReportUseCase(get()) }
    factory { com.argomin.app.domain.usecase.reporting.GetLowStockReportUseCase(get()) }
    factory { com.argomin.app.domain.usecase.reporting.GetSessionReportUseCase(get()) }
    factory { com.argomin.app.domain.usecase.reporting.GetAggregatedUsageReportUseCase(get()) }
    factory { com.argomin.app.domain.usecase.reporting.GetCashierPerformanceUseCase(get()) }

    // Backup Use Cases
    factory { com.argomin.app.domain.usecase.backup.BackupDatabaseUseCase(get()) }
    factory { com.argomin.app.domain.usecase.backup.RestoreDatabaseUseCase(get()) }
    factory { com.argomin.app.domain.usecase.backup.GetBackupsUseCase(get()) }

    // Store Settings Use Cases
    factory { com.argomin.app.domain.usecase.settings.GetStoreSettingsUseCase(get()) }
    factory { com.argomin.app.domain.usecase.settings.SaveStoreSettingsUseCase(get()) }

    // Database Initializer
    single<com.argomin.app.data.local.DatabaseInitializer> { 
        com.argomin.app.data.local.DatabaseInitializer(
            get<com.argomin.app.data.local.AppDatabase>(),
            androidContext(),
            get<DishDao>(),
            get<CustomerDao>(),
            get<com.argomin.app.data.local.dao.CategoryDao>(),
            get<com.argomin.app.data.local.dao.IngredientDao>(),
            get<com.argomin.app.data.local.dao.DishComponentDao>(),
            get<com.argomin.app.data.local.dao.PackageDao>(),
            get<SettingsRepository>(),
            get<com.argomin.app.data.local.dao.StoreSettingsDao>(),
            get<com.argomin.app.domain.repository.EmployerRepository>()
        ) 
    }
    
    // Session & Authentication
    single { com.argomin.app.data.local.SessionManager(androidContext()) }
    single { com.argomin.app.domain.manager.AuthenticationManager(get(), get(), get()) }
    
    // Audit Trail
    single { get<AppDatabase>().auditLogDao() }
    single<com.argomin.app.domain.repository.AuditLogRepository> { 
        com.argomin.app.data.repository.AuditLogRepositoryImpl(get()) 
    }
    single { com.argomin.app.domain.manager.AuditLogger(get(), inject(), kotlinx.coroutines.GlobalScope) }
    
    single<com.argomin.app.domain.manager.PrinterManager> { com.argomin.app.data.manager.AndroidPrinterManager(androidContext(), get()) }
}

