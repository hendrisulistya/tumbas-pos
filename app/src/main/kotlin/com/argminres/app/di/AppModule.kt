package com.argminres.app.di

import androidx.room.Room
import com.argminres.app.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.component.get
import com.argminres.app.data.local.dao.DishDao
import com.argminres.app.data.local.dao.CustomerDao
import com.argminres.app.data.repository.SettingsRepository

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
    single { get<AppDatabase>().ingredientCategoryDao() }
    single { get<AppDatabase>().ingredientStockDao() }
    single { get<AppDatabase>().dailySessionDao() }
    single { get<AppDatabase>().wasteRecordDao() }
    single { get<AppDatabase>().ingredientUsageDao() }
    single { get<AppDatabase>().dishComponentDao() }
    single { get<AppDatabase>().dishHistoryDao() }

    // Repositories
    single<com.argminres.app.domain.repository.DishRepository> { 
        com.argminres.app.data.repository.DishRepositoryImpl(get(), get()) 
    }
    single<com.argminres.app.domain.repository.SalesOrderRepository> { 
        com.argminres.app.data.repository.SalesOrderRepositoryImpl(get()) 
    }
    single<com.argminres.app.domain.repository.IngredientStockRepository> {
        com.argminres.app.data.repository.IngredientStockRepositoryImpl(get())
    }
    
    single<com.argminres.app.domain.repository.DailySessionRepository> {
        com.argminres.app.data.repository.DailySessionRepositoryImpl(get())
    }
    
    single<com.argminres.app.domain.repository.WasteRecordRepository> {
        com.argminres.app.data.repository.WasteRecordRepositoryImpl(get())
    }
    
    single<com.argminres.app.domain.repository.IngredientUsageRepository> {
        com.argminres.app.data.repository.IngredientUsageRepositoryImpl(get())
    }
    
    single<com.argminres.app.domain.repository.StockRepository> {
        com.argminres.app.data.repository.StockRepositoryImpl(get())
    }
    single<com.argminres.app.domain.repository.SupplierRepository> { 
        com.argminres.app.data.repository.SupplierRepositoryImpl(get()) 
    }
    single<com.argminres.app.domain.repository.PurchaseOrderRepository> { 
        com.argminres.app.data.repository.PurchaseOrderRepositoryImpl(get()) 
    }
    single<com.argminres.app.domain.repository.DishComponentRepository> {
        com.argminres.app.data.repository.DishComponentRepositoryImpl(get(), get())
    }
    single<com.argminres.app.domain.repository.IngredientRepository> {
        com.argminres.app.data.repository.IngredientRepositoryImpl(get(), get())
    }
    single<com.argminres.app.domain.repository.DishHistoryRepository> {
        com.argminres.app.data.repository.DishHistoryRepositoryImpl(get())
    }
    single<com.argminres.app.domain.repository.ReportingRepository> { 
        com.argminres.app.data.repository.ReportingRepositoryImpl(get()) 
    }
    single { com.argminres.app.data.repository.SettingsRepository(get()) }
    single<com.argminres.app.domain.repository.BackupRepository> { 
        com.argminres.app.data.repository.BackupRepositoryImpl(get(), get()) 
    }
    single<com.argminres.app.domain.repository.CartRepository> {
        com.argminres.app.data.repository.CartRepositoryImpl()
    }
    single<com.argminres.app.domain.repository.CustomerRepository> {
        com.argminres.app.domain.repository.CustomerRepositoryImpl(get())
    }
    single { get<AppDatabase>().customerDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().storeSettingsDao() }
    single { get<AppDatabase>().employerDao() }
    single<com.argminres.app.domain.repository.ImageRepository> {
        com.argminres.app.data.repository.ImageRepositoryImpl()
    }
    single<com.argminres.app.domain.repository.StoreSettingsRepository> {
        com.argminres.app.data.repository.StoreSettingsRepositoryImpl(get())
    }
    single<com.argminres.app.domain.repository.EmployerRepository> {
        com.argminres.app.data.repository.EmployerRepositoryImpl(get(), androidContext())
    }
    
    single {
        com.argminres.app.domain.model.R2Config(
            accountId = com.argminres.app.core.Secrets.R2_ACCOUNT_ID,
            accessKeyId = com.argminres.app.core.Secrets.R2_ACCESS_KEY_ID,
            secretAccessKey = com.argminres.app.core.Secrets.R2_SECRET_ACCESS_KEY,
            bucketName = com.argminres.app.core.Secrets.R2_BUCKET_NAME
        )
    }

    // Use Cases
    factory { com.argminres.app.domain.usecase.sales.CreateSalesOrderUseCase(get(), get()) }
    factory { com.argminres.app.domain.usecase.sales.GetSalesOrdersUseCase(get()) }
    factory { com.argminres.app.domain.usecase.sales.GetProductByBarcodeUseCase(get()) }
    factory { com.argminres.app.domain.usecase.sales.SearchDishesUseCase(get()) }

    // Showcase Use Cases
    factory { com.argminres.app.domain.usecase.showcase.GetShowcaseInventoryUseCase(get()) }
    factory { com.argminres.app.domain.usecase.showcase.ManageShowcaseDishUseCase(get()) }
    factory { com.argminres.app.domain.usecase.showcase.AdjustShowcaseStockUseCase(get(), get()) }
    factory { com.argminres.app.domain.usecase.showcase.GetShowcaseStockHistoryUseCase(get()) }
    factory { com.argminres.app.domain.usecase.showcase.GetCategoriesUseCase(get()) }

    // Ingredient Use Cases
    factory { com.argminres.app.domain.usecase.ingredient.SearchIngredientsUseCase(get()) }
    factory { com.argminres.app.domain.usecase.ingredient.GetIngredientsUseCase(get()) }
    
    // Session Use Cases
    factory { com.argminres.app.domain.usecase.session.EndOfDayUseCase(get(), get(), get(), get(), get()) }
    factory { com.argminres.app.domain.usecase.session.StartDailySessionUseCase(get()) }
    factory { com.argminres.app.domain.usecase.session.CheckAutoDailyCloseUseCase(get()) }
    factory { com.argminres.app.domain.usecase.session.SessionCheckUseCase(get()) }

    // Dish Use Cases
    factory { com.argminres.app.domain.usecase.dish.ManageDishImageUseCase(get(), get()) }
    
    // Recipe Use Cases
    factory { com.argminres.app.domain.usecase.recipe.GetPackageComponentsUseCase(get()) }
    factory { com.argminres.app.domain.usecase.recipe.GetPackageAvailabilityUseCase(get()) }
    factory { com.argminres.app.domain.usecase.recipe.ManageRecipeUseCase(get()) }
    factory { com.argminres.app.domain.usecase.recipe.GetPackageDishesUseCase(get()) }

    // Purchase Order Use Cases
    factory { com.argminres.app.domain.usecase.purchase.GetPurchaseOrdersUseCase(get()) }
    factory { com.argminres.app.domain.usecase.purchase.CreatePurchaseOrderUseCase(get()) }
    factory { com.argminres.app.domain.usecase.purchase.ReceivePurchaseOrderUseCase(get(), get(), get()) }
    factory { com.argminres.app.domain.usecase.purchase.GetSuppliersUseCase(get()) }
    factory { com.argminres.app.domain.usecase.purchase.ManageSupplierUseCase(get()) }

    // Reporting Use Cases
    factory { com.argminres.app.domain.usecase.reporting.GetDashboardDataUseCase(get()) }
    factory { com.argminres.app.domain.usecase.reporting.GetSalesReportUseCase(get()) }
    factory { com.argminres.app.domain.usecase.reporting.GetLowStockReportUseCase(get()) }

    // Backup Use Cases
    factory { com.argminres.app.domain.usecase.backup.BackupDatabaseUseCase(get()) }
    factory { com.argminres.app.domain.usecase.backup.RestoreDatabaseUseCase(get()) }
    factory { com.argminres.app.domain.usecase.backup.GetBackupsUseCase(get()) }

    // Store Settings Use Cases
    factory { com.argminres.app.domain.usecase.settings.GetStoreSettingsUseCase(get()) }
    factory { com.argminres.app.domain.usecase.settings.SaveStoreSettingsUseCase(get()) }

    // Database Initializer
    single<com.argminres.app.data.local.DatabaseInitializer> { 
        com.argminres.app.data.local.DatabaseInitializer(
            androidContext(),
            get<DishDao>(),
            get<CustomerDao>(),
            get<com.argminres.app.data.local.dao.CategoryDao>(),
            get<com.argminres.app.data.local.dao.IngredientCategoryDao>(),
            get<com.argminres.app.data.local.dao.IngredientDao>(),
            get<com.argminres.app.data.local.dao.DishComponentDao>(),
            get<SettingsRepository>(),
            get<com.argminres.app.data.local.dao.StoreSettingsDao>(),
            get<com.argminres.app.domain.repository.EmployerRepository>()
        ) 
    }
    
    // Session & Authentication
    single { com.argminres.app.data.local.SessionManager(androidContext()) }
    single { com.argminres.app.domain.manager.AuthenticationManager(get(), get(), get()) }
    
    // Audit Trail
    single { get<AppDatabase>().auditLogDao() }
    single<com.argminres.app.domain.repository.AuditLogRepository> { 
        com.argminres.app.data.repository.AuditLogRepositoryImpl(get()) 
    }
    single { com.argminres.app.domain.manager.AuditLogger(get(), inject(), kotlinx.coroutines.GlobalScope) }
    
    single<com.argminres.app.domain.manager.PrinterManager> { com.argminres.app.data.manager.EscPosPrinterManager(androidContext()) }
    
    // ViewModels
    viewModel { com.argminres.app.presentation.auth.LoginViewModel(get(), get()) }
    viewModel { com.argminres.app.presentation.sales.SalesViewModel(get(), get(), get(), get(), get(), get(), get(), androidContext() as android.app.Application, get(), get()) }
    viewModel { com.argminres.app.presentation.home.HomeViewModel(get(), get()) }
    viewModel { com.argminres.app.presentation.showcase.ShowcaseViewModel(get(), get(), get(), get()) }
    viewModel { 
        com.argminres.app.presentation.purchase.PurchaseViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get()
        ) 
    }
    viewModel { com.argminres.app.presentation.reporting.ReportingViewModel(get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.backup.BackupViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.activation.ActivationViewModel(get(), get()) }
    viewModel { com.argminres.app.presentation.endofday.EndOfDayViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.wip.WorkInProcessViewModel(get()) }
    viewModel { com.argminres.app.presentation.startday.SessionCheckViewModel(get(), get(), get()) }
    viewModel { com.argminres.app.presentation.ingredient.IngredientManagementViewModel(get()) }
    viewModel { com.argminres.app.presentation.ingredientmaster.IngredientMasterViewModel(get()) }
    viewModel { com.argminres.app.presentation.dishmaster.DishMasterViewModel(get()) }
    viewModel { com.argminres.app.presentation.activation.PostActivationViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.activation.RestoreStoreViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.sales.SalesOrderViewModel(get(), get(), get(), get()) }
    viewModel { com.argminres.app.presentation.scan.ScanViewModel(get(), get()) }
    viewModel { com.argminres.app.presentation.settings.printer.PrinterSettingsViewModel(get(), androidContext()) }
    viewModel { com.argminres.app.presentation.sales.SalesOrderDetailViewModel(get(), get(), get(), androidContext() as android.app.Application, get(), get()) }
    viewModel { com.argminres.app.presentation.employer.EmployerManagementViewModel(get(), get(), get()) }
    viewModel { com.argminres.app.presentation.audit.AuditLogViewModel(get()) }
    viewModel { 
        com.argminres.app.presentation.dish.DishViewModel(
            get(), get(), get(), get(), get()
        ) 
    }
    viewModel { 
        com.argminres.app.presentation.settings.store.StoreSettingsViewModel(
            get(), get(), get()
        ) 
    }
}
