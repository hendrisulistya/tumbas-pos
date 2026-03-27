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
    private val ingredientDao: com.argminres.app.data.local.dao.IngredientDao,
    private val dishComponentDao: com.argminres.app.data.local.dao.DishComponentDao,
    private val settingsRepository: SettingsRepository,
    private val storeSettingsDao: com.argminres.app.data.local.dao.StoreSettingsDao,
    private val employerRepository: com.argminres.app.domain.repository.EmployerRepository
) {
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsRepository.isDatabaseInitialized()) {
            android.util.Log.d("DatabaseInitializer", "Database already initialized, skipping.")
            return@withContext
        }

        android.util.Log.d("DatabaseInitializer", "Starting database initialization...")


        // Insert categories from CSV
        insertCategoriesFromCsv()

        // Insert dishes from CSV as MASTER DATA (stock = 0)
        // These dishes populate Kelola Etalase for managers to select from
        // Managers then add selected dishes to daily Etalase with stock
        insertProductsFromCsv()
        
        // Insert dish packages (package recipes) from JSON
        insertDishPackagesFromJson()
        
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
        android.util.Log.d("DatabaseInitializer", "Database initialization completed successfully.")
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
        android.util.Log.d("DatabaseInitializer", "Inserting categories...")
        // Categories are now static (matches HomeScreen hardcoded categories)
        val categories = listOf(
            com.argminres.app.data.local.entity.CategoryEntity(id = 1L, name = "Paket", description = "Paket makanan"),
            com.argminres.app.data.local.entity.CategoryEntity(id = 2L, name = "Makanan", description = "Makanan"),
            com.argminres.app.data.local.entity.CategoryEntity(id = 3L, name = "Minuman", description = "Minuman"),
            com.argminres.app.data.local.entity.CategoryEntity(id = 4L, name = "Lain-lain", description = "Lain-lain")
        )
        categoryDao.insertAll(categories)
        android.util.Log.d("DatabaseInitializer", "Inserted ${categories.size} categories.")
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
                    // New simplified format: id, name, description, price, category, image
                    // Stock is always 0 for master data
                    if (tokens.size >= 5) {
                        products.add(
                            DishEntity(
                                id = tokens[0].toLongOrNull() ?: 0L,
                                name = tokens[1],
                                description = tokens[2],
                                price = tokens[3].toDoubleOrNull() ?: 0.0,
                                stock = 0,
                                category = tokens[4],
                                image = if (tokens.size > 5 && tokens[5].isNotBlank()) tokens[5] else null
                            )
                        )
                    }
                }
            }
            if (products.isNotEmpty()) {
                productDao.insertAll(products)
                android.util.Log.d("DatabaseInitializer", "Inserted ${products.size} products from dishes.csv.")
            } else {
                android.util.Log.w("DatabaseInitializer", "No products found in dishes.csv!")
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "Error inserting products from CSV", e)
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
    
    private suspend fun insertIngredientsFromCsv() {
        try {
            val ingredients = mutableListOf<com.argminres.app.data.local.entity.IngredientEntity>()
            context.assets.open("ingredients.csv").bufferedReader().use { reader ->
                reader.readLine() // Skip header
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    // New format: id,name,unit,costPerUnit
                    // Stock and minimumStock are always 0 for master data
                    if (parts.size >= 4) {
                        ingredients.add(
                            com.argminres.app.data.local.entity.IngredientEntity(
                                id = parts[0].toLongOrNull() ?: 0,
                                name = parts[1],
                                unit = parts[2],
                                stock = 0.0, // Always 0 for master data
                                minimumStock = 0.0, // Not used for master data
                                costPerUnit = parts[3].toDoubleOrNull() ?: 0.0,
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
    
    private suspend fun insertDishPackagesFromJson() {
        try {
            val components = mutableListOf<com.argminres.app.data.local.entity.DishComponentEntity>()
            val packages = mutableListOf<DishEntity>()
            
            val jsonString = context.assets.open("dish_package.json").bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val packageObj = jsonArray.getJSONObject(i)
                val packageId = packageObj.getLong("package_id")
                val packageName = packageObj.getString("package_name")
                val description = packageObj.getString("description")
                val price = packageObj.getDouble("price")
                val componentsArray = packageObj.getJSONArray("components")
                val image = if (packageObj.has("image")) packageObj.getString("image") else null
                
                packages.add(
                    DishEntity(
                        id = packageId,
                        name = packageName,
                        description = description,
                        price = price,
                        stock = 0,
                        category = "Paket",
                        image = image
                    )
                )
                
                for (j in 0 until componentsArray.length()) {
                    val componentId = componentsArray.getLong(j)
                    components.add(
                        com.argminres.app.data.local.entity.DishComponentEntity(
                            packageDishId = packageId,
                            componentDishId = componentId,
                            quantity = 1
                        )
                    )
                }
            }
            if (packages.isNotEmpty()) {
                productDao.insertAll(packages)
            }
            components.forEach { dishComponentDao.insertComponent(it) }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseInitializer", "Error inserting dish packages", e)
        }
    }
}
