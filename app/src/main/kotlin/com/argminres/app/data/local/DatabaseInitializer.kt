package com.argminres.app.data.local

import com.argminres.app.data.local.dao.CustomerDao
import com.argminres.app.data.local.dao.DishDao
import com.argminres.app.data.local.entity.CustomerEntity
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DatabaseInitializer(
    private val context: android.content.Context,
    private val productDao: DishDao,
    private val customerDao: CustomerDao,
    private val categoryDao: com.argminres.app.data.local.dao.CategoryDao,
    private val ingredientCategoryDao: com.argminres.app.data.local.dao.IngredientCategoryDao,
    private val ingredientDao: com.argminres.app.data.local.dao.IngredientDao,
    private val settingsRepository: SettingsRepository,
    private val storeSettingsDao: com.argminres.app.data.local.dao.StoreSettingsDao,
    private val employerRepository: com.argminres.app.domain.repository.EmployerRepository
) {
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsRepository.isDatabaseInitialized()) {
            return@withContext
        }

        // Insert categories from CSV
        insertCategoriesFromCsv()

        // Insert products from CSV
        insertProductsFromCsv()
        
        // Insert ingredient categories from CSV
        insertIngredientCategoriesFromCsv()
        
        // Insert ingredients from CSV
        insertIngredientsFromCsv()
        
        // Insert customers from CSV
        insertCustomersFromCsv()
        
        // Initialize employers from CSV
        employerRepository.initializeFromCsv()
        
        // Initialize default store settings
        insertDefaultStoreSettings()
        
        // Mark as initialized
        settingsRepository.setDatabaseInitialized(true)
    }
    
    private suspend fun insertDefaultStoreSettings() {
        try {
            // Read store settings from CSV
            var storeName = "Tumbas POS"
            var storeAddress = "Jl. Example No. 123"
            var storePhone = "+62 812-3456-7890"
            var storeTaxId = "01.234.567.8-901.000"
            
            try {
                context.assets.open("store.csv").bufferedReader().use { reader ->
                    reader.readLine() // Skip header
                    reader.readLine()?.let { line ->
                        val tokens = parseCsvLine(line)
                        if (tokens.size >= 4) {
                            storeName = tokens[0]
                            storeAddress = tokens[1]
                            storePhone = tokens[2]
                            storeTaxId = tokens[3]
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Use default values if CSV reading fails
            }
            
            // Load and convert logo to BW bitmap for thermal printer
            val logoBase64 = try {
                context.assets.open("logo.png").use { inputStream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    val bwBitmap = convertToBWBitmap(bitmap)
                    val outputStream = java.io.ByteArrayOutputStream()
                    bwBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                    android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null // If logo.png doesn't exist or conversion fails, use null
            }
            
            val defaultSettings = com.argminres.app.data.local.entity.StoreSettingsEntity(
                id = 1L,
                storeName = storeName,
                storeAddress = storeAddress,
                storePhone = storePhone,
                storeTaxId = storeTaxId,
                logoImage = logoBase64
            )
            storeSettingsDao.insertOrUpdate(defaultSettings)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Convert bitmap to Black & White for thermal printer compatibility
     */
    private fun convertToBWBitmap(original: android.graphics.Bitmap): android.graphics.Bitmap {
        val width = original.width
        val height = original.height
        val bwBitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(bwBitmap)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        
        // Convert to grayscale
        colorMatrix.setSaturation(0f)
        
        val filter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(original, 0f, 0f, paint)
        
        // Apply threshold to make it pure black and white
        val pixels = IntArray(width * height)
        bwBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val gray = (android.graphics.Color.red(pixel) + 
                       android.graphics.Color.green(pixel) + 
                       android.graphics.Color.blue(pixel)) / 3
            
            // Threshold at 128 - lighter becomes white, darker becomes black
            pixels[i] = if (gray > 128) {
                android.graphics.Color.WHITE
            } else {
                android.graphics.Color.BLACK
            }
        }
        
        bwBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bwBitmap
    }
    
    private suspend fun insertCategoriesFromCsv() {
        try {
            val categories = mutableListOf<com.argminres.app.data.local.entity.CategoryEntity>()
            context.assets.open("categories.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 1) {
                        categories.add(
                            com.argminres.app.data.local.entity.CategoryEntity(
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
            // Always insert Guest customer first
            insertDefaultCustomer()
            
            // Then insert customers from CSV
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
                customers.forEach { customerDao.insertCustomer(it) }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "Error inserting customers", e)
            // Guest already inserted above, so no fallback needed
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
            val products = mutableListOf<DishEntity>()
            context.assets.open("dishes.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val tokens = parseCsvLine(line)
                    // tokens: barcode, name, description, price, costPrice, stock, categoryId, image
                    if (tokens.size >= 7) {
                        products.add(
                            DishEntity(
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
    
    private suspend fun insertIngredientCategoriesFromCsv() {
        try {
            val categories = mutableListOf<com.argminres.app.data.local.entity.IngredientCategoryEntity>()
            context.assets.open("ingredient_categories.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        categories.add(
                            com.argminres.app.data.local.entity.IngredientCategoryEntity(
                                id = parts[0].toLongOrNull() ?: 0,
                                name = parts[1],
                                description = parts[2],
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            categories.forEach { ingredientCategoryDao.insertCategory(it) }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "Error inserting ingredient categories", e)
        }
    }
    
    private suspend fun insertIngredientsFromCsv() {
        try {
            val ingredients = mutableListOf<com.argminres.app.data.local.entity.IngredientEntity>()
            context.assets.open("ingredients.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size >= 7) {
                        ingredients.add(
                            com.argminres.app.data.local.entity.IngredientEntity(
                                id = parts[0].toLongOrNull() ?: 0,
                                name = parts[1],
                                categoryId = parts[2].toLongOrNull() ?: 0,
                                unit = parts[3],
                                stock = parts[4].toDoubleOrNull() ?: 0.0,
                                minimumStock = parts[5].toDoubleOrNull() ?: 0.0,
                                costPerUnit = parts[6].toDoubleOrNull() ?: 0.0,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            ingredients.forEach { ingredientDao.insertIngredient(it) }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "Error inserting ingredients", e)
        }
    }
}
