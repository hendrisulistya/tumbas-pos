package com.tumbaspos.app.data.local

import com.tumbaspos.app.data.local.dao.CustomerDao
import com.tumbaspos.app.data.local.dao.ProductDao
import com.tumbaspos.app.data.local.entity.CustomerEntity
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseInitializer(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val settingsRepository: SettingsRepository
) {
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsRepository.isDatabaseInitialized()) {
            return@withContext
        }

        // Insert sample products
        insertSampleProducts()
        
        // Insert default guest customer
        insertDefaultCustomer()
        
        // Mark as initialized
        settingsRepository.setDatabaseInitialized(true)
    }
    
    private suspend fun insertDefaultCustomer() {
        val guest = CustomerEntity(
            name = "Guest",
            phone = "-",
            email = "-",
            address = "-"
        )
        customerDao.insertCustomer(guest)
    }

    private suspend fun insertSampleProducts() {
        val sampleProducts = listOf(
            // Personal Care
            ProductEntity(
                barcode = "8991102100014",
                name = "Shampoo Anti-Dandruff 200ml",
                description = "Anti-dandruff shampoo for healthy hair",
                price = 25000.0,
                costPrice = 18000.0,
                stock = 50,
                category = "Personal Care"
            ),
            ProductEntity(
                barcode = "8991102100021",
                name = "Bath Soap 90g",
                description = "Moisturizing bath soap",
                price = 5000.0,
                costPrice = 3500.0,
                stock = 100,
                category = "Personal Care"
            ),
            ProductEntity(
                barcode = "8991102100038",
                name = "Toothpaste Fresh Mint 150g",
                description = "Fresh mint toothpaste with fluoride",
                price = 12000.0,
                costPrice = 8500.0,
                stock = 75,
                category = "Personal Care"
            ),

            // Food
            ProductEntity(
                barcode = "8992761111014",
                name = "Premium Rice 5kg",
                description = "Premium quality white rice",
                price = 65000.0,
                costPrice = 52000.0,
                stock = 30,
                category = "Food"
            ),
            ProductEntity(
                barcode = "8992761111021",
                name = "Instant Noodles Chicken Flavor",
                description = "Delicious instant noodles",
                price = 3000.0,
                costPrice = 2200.0,
                stock = 200,
                category = "Food"
            ),
            ProductEntity(
                barcode = "8992761111038",
                name = "Cooking Oil 2L",
                description = "Pure cooking oil",
                price = 32000.0,
                costPrice = 26000.0,
                stock = 40,
                category = "Food"
            ),

            // Drink
            ProductEntity(
                barcode = "8993675100014",
                name = "Mineral Water 600ml",
                description = "Pure mineral water",
                price = 3500.0,
                costPrice = 2500.0,
                stock = 150,
                category = "Drink"
            ),
            ProductEntity(
                barcode = "8993675100021",
                name = "Soft Drink Cola 330ml",
                description = "Refreshing cola drink",
                price = 6000.0,
                costPrice = 4200.0,
                stock = 100,
                category = "Drink"
            ),
            ProductEntity(
                barcode = "8993675100038",
                name = "Instant Coffee 3-in-1",
                description = "Coffee with sugar and creamer",
                price = 2000.0,
                costPrice = 1400.0,
                stock = 180,
                category = "Drink"
            ),

            // Home Care
            ProductEntity(
                barcode = "8994567100014",
                name = "Laundry Detergent 1kg",
                description = "Powerful cleaning detergent",
                price = 18000.0,
                costPrice = 13500.0,
                stock = 60,
                category = "Home Care"
            ),
            ProductEntity(
                barcode = "8994567100021",
                name = "Dishwashing Liquid 800ml",
                description = "Effective dishwashing liquid",
                price = 12000.0,
                costPrice = 9000.0,
                stock = 70,
                category = "Home Care"
            ),

            // Snacks
            ProductEntity(
                barcode = "8995432100014",
                name = "Potato Chips BBQ 60g",
                description = "Crispy BBQ flavored chips",
                price = 8000.0,
                costPrice = 5600.0,
                stock = 120,
                category = "Snacks"
            ),
            ProductEntity(
                barcode = "8995432100021",
                name = "Chocolate Cookies 150g",
                description = "Delicious chocolate cookies",
                price = 15000.0,
                costPrice = 11000.0,
                stock = 80,
                category = "Snacks"
            ),
            ProductEntity(
                barcode = "8995432100038",
                name = "Candy Mix 100g",
                description = "Assorted fruit candies",
                price = 5000.0,
                costPrice = 3500.0,
                stock = 150,
                category = "Snacks"
            ),

            // Electronics
            ProductEntity(
                barcode = "8996321100014",
                name = "AA Batteries 4-Pack",
                description = "Alkaline AA batteries",
                price = 20000.0,
                costPrice = 15000.0,
                stock = 50,
                category = "Electronics"
            ),
            ProductEntity(
                barcode = "8996321100021",
                name = "USB Phone Charger Cable",
                description = "Universal USB charging cable",
                price = 35000.0,
                costPrice = 25000.0,
                stock = 40,
                category = "Electronics"
            ),

            // Stationery
            ProductEntity(
                barcode = "8997654100014",
                name = "Ballpoint Pen Blue",
                description = "Smooth writing ballpoint pen",
                price = 3000.0,
                costPrice = 2000.0,
                stock = 200,
                category = "Stationery"
            ),
            ProductEntity(
                barcode = "8997654100021",
                name = "Notebook A5 80 Pages",
                description = "Quality ruled notebook",
                price = 12000.0,
                costPrice = 8500.0,
                stock = 100,
                category = "Stationery"
            ),
            ProductEntity(
                barcode = "8997654100038",
                name = "Pencil HB 12-Pack",
                description = "Standard HB pencils",
                price = 15000.0,
                costPrice = 11000.0,
                stock = 60,
                category = "Stationery"
            )
        )

        productDao.insertAll(sampleProducts)
    }
}
