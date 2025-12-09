package com.tumbaspos.app.di

import androidx.room.Room
import com.tumbaspos.app.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.component.get
import com.tumbaspos.app.data.local.dao.ProductDao
import com.tumbaspos.app.data.local.dao.CustomerDao
import com.tumbaspos.app.data.repository.SettingsRepository

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "tumbas_pos.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // DAOs
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().purchaseOrderDao() }
    single { get<AppDatabase>().salesOrderDao() }
    single { get<AppDatabase>().stockDao() }
    single { get<AppDatabase>().reportingDao() }

    // Repositories
    single<com.tumbaspos.app.domain.repository.ProductRepository> { 
        com.tumbaspos.app.data.repository.ProductRepositoryImpl(get(), get()) 
    }
    single<com.tumbaspos.app.domain.repository.SalesOrderRepository> { 
        com.tumbaspos.app.data.repository.SalesOrderRepositoryImpl(get()) 
    }
    single<com.tumbaspos.app.domain.repository.StockRepository> { 
        com.tumbaspos.app.data.repository.StockRepositoryImpl(get()) 
    }
    single<com.tumbaspos.app.domain.repository.SupplierRepository> { 
        com.tumbaspos.app.data.repository.SupplierRepositoryImpl(get()) 
    }
    single<com.tumbaspos.app.domain.repository.PurchaseOrderRepository> { 
        com.tumbaspos.app.data.repository.PurchaseOrderRepositoryImpl(get()) 
    }
    single<com.tumbaspos.app.domain.repository.ReportingRepository> { 
        com.tumbaspos.app.data.repository.ReportingRepositoryImpl(get()) 
    }
    single { com.tumbaspos.app.data.repository.SettingsRepository(get()) }
    single<com.tumbaspos.app.domain.repository.BackupRepository> { 
        com.tumbaspos.app.data.repository.BackupRepositoryImpl(get(), get()) 
    }
    single<com.tumbaspos.app.domain.repository.CartRepository> {
        com.tumbaspos.app.data.repository.CartRepositoryImpl()
    }
    single<com.tumbaspos.app.domain.repository.CustomerRepository> {
        com.tumbaspos.app.domain.repository.CustomerRepositoryImpl(get())
    }
    single { get<AppDatabase>().customerDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().storeSettingsDao() }
    single<com.tumbaspos.app.domain.repository.ImageRepository> {
        com.tumbaspos.app.data.repository.ImageRepositoryImpl()
    }
    single<com.tumbaspos.app.domain.repository.StoreSettingsRepository> {
        com.tumbaspos.app.data.repository.StoreSettingsRepositoryImpl(get())
    }
    
    single {
        com.tumbaspos.app.domain.model.R2Config(
            accountId = com.tumbaspos.app.core.Secrets.R2_ACCOUNT_ID,
            accessKeyId = com.tumbaspos.app.core.Secrets.R2_ACCESS_KEY_ID,
            secretAccessKey = com.tumbaspos.app.core.Secrets.R2_SECRET_ACCESS_KEY,
            bucketName = com.tumbaspos.app.core.Secrets.R2_BUCKET_NAME
        )
    }

    // Use Cases
    factory { com.tumbaspos.app.domain.usecase.sales.CreateSalesOrderUseCase(get(), get()) }
    factory { com.tumbaspos.app.domain.usecase.sales.GetSalesOrdersUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.sales.GetProductByBarcodeUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.sales.SearchProductsUseCase(get()) }

    // Warehouse Use Cases
    factory { com.tumbaspos.app.domain.usecase.warehouse.GetInventoryUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.warehouse.ManageProductUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.warehouse.AdjustStockUseCase(get(), get()) }
    factory { com.tumbaspos.app.domain.usecase.warehouse.GetStockHistoryUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.warehouse.GetCategoriesUseCase(get()) }

    // Product Use Cases
    factory { com.tumbaspos.app.domain.usecase.product.ManageProductImageUseCase(get(), get()) }

    // Purchase Order Use Cases
    factory { com.tumbaspos.app.domain.usecase.purchase.GetPurchaseOrdersUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.purchase.CreatePurchaseOrderUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.purchase.ReceivePurchaseOrderUseCase(get(), get(), get()) }
    factory { com.tumbaspos.app.domain.usecase.purchase.GetSuppliersUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.purchase.ManageSupplierUseCase(get()) }

    // Reporting Use Cases
    factory { com.tumbaspos.app.domain.usecase.reporting.GetDashboardDataUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.reporting.GetSalesReportUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.reporting.GetLowStockReportUseCase(get()) }

    // Backup Use Cases
    factory { com.tumbaspos.app.domain.usecase.backup.BackupDatabaseUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.backup.RestoreDatabaseUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.backup.GetBackupsUseCase(get()) }

    // Store Settings Use Cases
    factory { com.tumbaspos.app.domain.usecase.settings.GetStoreSettingsUseCase(get()) }
    factory { com.tumbaspos.app.domain.usecase.settings.SaveStoreSettingsUseCase(get()) }

    // Database Initializer
    single<com.tumbaspos.app.data.local.DatabaseInitializer> { 
        com.tumbaspos.app.data.local.DatabaseInitializer(
            androidContext(),
            get<ProductDao>(), 
            get<CustomerDao>(),
            get<com.tumbaspos.app.data.local.dao.CategoryDao>(),
            get<SettingsRepository>(),
            get<com.tumbaspos.app.data.local.dao.StoreSettingsDao>()
        ) 
    }

    single<com.tumbaspos.app.domain.manager.PrinterManager> { com.tumbaspos.app.data.manager.EscPosPrinterManager(androidContext()) }
    
    // ViewModels
    viewModel { com.tumbaspos.app.presentation.sales.SalesViewModel(get(), get(), get(), get(), get(), get(), get(), androidContext() as android.app.Application) }
    viewModel { com.tumbaspos.app.presentation.home.HomeViewModel(get(), get()) }
    viewModel { com.tumbaspos.app.presentation.warehouse.WarehouseViewModel(get(), get()) }
    viewModel { com.tumbaspos.app.presentation.purchase.PurchaseViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { com.tumbaspos.app.presentation.reporting.ReportingViewModel(get(), get(), get()) }
    viewModel { com.tumbaspos.app.presentation.backup.BackupViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.tumbaspos.app.presentation.activation.ActivationViewModel(get(), get()) }
    viewModel { com.tumbaspos.app.presentation.activation.PostActivationViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.tumbaspos.app.presentation.activation.RestoreStoreViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.tumbaspos.app.presentation.sales.SalesOrderViewModel(get(), get()) }
    viewModel { com.tumbaspos.app.presentation.scan.ScanViewModel(get(), get()) }
    viewModel { com.tumbaspos.app.presentation.settings.printer.PrinterSettingsViewModel(get(), androidContext()) }
    viewModel { com.tumbaspos.app.presentation.sales.SalesOrderDetailViewModel(get(), get(), get()) }
    viewModel { 
        com.tumbaspos.app.presentation.product.ProductViewModel(
            get(), get(), get(), get()
        ) 
    }
    viewModel { 
        com.tumbaspos.app.presentation.settings.store.StoreSettingsViewModel(
            get(), get(), get()
        ) 
    }
}
