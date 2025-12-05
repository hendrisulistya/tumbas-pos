package com.tumbaspos.app.data.local

import com.tumbaspos.app.data.local.dao.CustomerDao
import com.tumbaspos.app.data.local.dao.ProductDao
import com.tumbaspos.app.data.local.entity.CustomerEntity
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DatabaseInitializer(
    private val context: android.content.Context,
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val categoryDao: com.tumbaspos.app.data.local.dao.CategoryDao,
    private val settingsRepository: SettingsRepository
) {
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsRepository.isDatabaseInitialized()) {
            return@withContext
        }

        // Insert categories from CSV
        insertCategoriesFromCsv()

        // Insert products from CSV
        insertProductsFromCsv()
        
        // Insert customers from CSV
        insertCustomersFromCsv()
        
        // Mark as initialized
        settingsRepository.setDatabaseInitialized(true)
    }
    
    private suspend fun insertCategoriesFromCsv() {
        try {
            val categories = mutableListOf<com.tumbaspos.app.data.local.entity.CategoryEntity>()
            context.assets.open("categories.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 1) {
                        categories.add(
                            com.tumbaspos.app.data.local.entity.CategoryEntity(
                                id = tokens[0].toLongOrNull() ?: 0L,
                                name = if (tokens.size > 1) tokens[1] else "Unknown",
                                description = if (tokens.size > 2) tokens[2] else ""
                            )
                        )
                    }
                }
            }
            if (categories.isNotEmpty()) {
                categoryDao.insertAll(categories)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun insertCustomersFromCsv() {
        try {
            val customers = mutableListOf<CustomerEntity>()
            context.assets.open("customers.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 4) {
                        customers.add(
                            CustomerEntity(
                                name = tokens[0],
                                phone = tokens[1],
                                email = tokens[2],
                                address = tokens[3]
                            )
                        )
                    }
                }
            }
            if (customers.isNotEmpty()) {
                customerDao.insertCustomer(customers.first()) // Insert first individually if needed or loop
                // The Dao might not have insertAll for customers, let's check. 
                // Creating a loop for safety as insertAll might not exist on CustomerDao
                customers.forEach { customerDao.insertCustomer(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default if CSV fails
            insertDefaultCustomer()
        }
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

    private suspend fun insertProductsFromCsv() {
        try {
            val products = mutableListOf<ProductEntity>()
            context.assets.open("products.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val tokens = parseCsvLine(line)
                    // tokens: barcode, name, description, price, costPrice, stock, categoryId, image
                    if (tokens.size >= 7) {
                        products.add(
                            ProductEntity(
                                barcode = tokens[0],
                                name = tokens[1],
                                description = tokens[2],
                                price = tokens[3].toDoubleOrNull() ?: 0.0,
                                costPrice = tokens[4].toDoubleOrNull() ?: 0.0,
                                stock = tokens[5].toIntOrNull() ?: 0,
                                categoryId = tokens[6].toLongOrNull() ?: 0L,
                                image = if (tokens.size > 7 && tokens[7].isNotBlank()) tokens[7] else null
                            )
                        )
                    }
                }
            }
            if (products.isNotEmpty()) {
                productDao.insertAll(products)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var start = 0
        var inQuotes = false
        for (i in line.indices) {
            if (line[i] == '\"') {
                inQuotes = !inQuotes
            } else if (line[i] == ',' && !inQuotes) {
                tokens.add(line.substring(start, i).trim().removeSurrounding("\""))
                start = i + 1
            }
        }
        tokens.add(line.substring(start).trim().removeSurrounding("\""))
        return tokens
    }
}
