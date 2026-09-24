package com.argomin.app.data.repository

import com.argomin.app.domain.model.*
import com.argomin.app.domain.repository.pos.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultPosDishRepository(
    initialDishes: List<MenuItem> = defaultDishes
) : PosDishRepository {
    private val dishes = MutableStateFlow(initialDishes)

    override fun getAllDishes(): Flow<List<MenuItem>> = dishes.asStateFlow()

    override suspend fun getDishById(id: String): MenuItem? =
        dishes.value.find { it.id == id }

    override suspend fun updateStock(dishId: String, newStock: Int) {
        dishes.update { list ->
            list.map { if (it.id == dishId) it.copy(stock = newStock) else it }
        }
    }

    override suspend fun saveDish(item: MenuItem) {
        dishes.update { list ->
            val idx = list.indexOfFirst { it.id == item.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, item) }
            else list + item
        }
    }

    override suspend fun deleteDish(id: String) {
        dishes.update { list -> list.filterNot { it.id == id } }
    }

    companion object {
        val defaultDishes = listOf(
            MenuItem("1001", "Rendang Daging", "Makanan", 35000, 22000, 35, "dish_image/1001.png", "🥩"),
            MenuItem("1002", "Gulai Ayam", "Makanan", 28000, 18000, 28, "dish_image/1002.png", "🍗"),
            MenuItem("1003", "Gulai Ikan", "Makanan", 32000, 20000, 20, "dish_image/1003.png", "🐟"),
            MenuItem("1004", "Ayam Pop", "Makanan", 30000, 19000, 30, "dish_image/1004.png", "🍗"),
            MenuItem("1005", "Dendeng Balado", "Makanan", 34000, 21000, 25, "dish_image/1005.png", "🥩"),
            MenuItem("1006", "Dendeng Batokok", "Makanan", 34000, 21000, 22, "dish_image/1006.png", "🥩"),
            MenuItem("1007", "Gulai Tunjang", "Makanan", 38000, 24000, 18, "dish_image/1007.png", "🍲"),
            MenuItem("1008", "Gulai Kikil", "Makanan", 36000, 23000, 15, "dish_image/1008.png", "🍲"),
            MenuItem("1009", "Telur Dadar", "Makanan", 15000, 8000, 45, "dish_image/1009.png", "🍳"),
            MenuItem("1010", "Telur Balado", "Makanan", 14000, 7500, 40, "dish_image/1010.png", "🥚"),
            MenuItem("1011", "Nasi Putih", "Makanan", 8000, 3500, 100, "dish_image/1011.png", "🍚"),
            MenuItem("1012", "Sayur Singkong", "Makanan", 10000, 4000, 50, "dish_image/1012.png", "🥬"),
            MenuItem("1013", "Sambal Ijo", "Makanan", 6000, 2500, 60, "dish_image/1013.png", "🌶️"),
            MenuItem("1014", "Sambal Merah", "Makanan", 6000, 2500, 60, "dish_image/1014.png", "🌶️"),
            MenuItem("1015", "Perkedel Kentang", "Makanan", 12000, 6000, 35, "dish_image/1015.png", "🥔"),
            MenuItem("1016", "Es Teh Manis", "Minuman", 7000, 2000, 80, "dish_image/1016.png", "🍹"),
            MenuItem("1017", "Teh Hangat", "Minuman", 5000, 1500, 80, "dish_image/1017.png", "🍵"),
            MenuItem("1018", "Es Jeruk", "Minuman", 12000, 5000, 40, "dish_image/1018.png", "🍊"),
            MenuItem("1019", "Jeruk Hangat", "Minuman", 10000, 4500, 40, "dish_image/1019.png", "🍊"),
            MenuItem("1020", "Jus Alpukat", "Minuman", 18000, 8000, 25, "dish_image/1020.png", "🥑"),
            MenuItem("1021", "Jus Mangga", "Minuman", 16000, 7000, 25, "dish_image/1021.png", "🥭"),
            MenuItem("1022", "Kopi Hitam", "Minuman", 10000, 3000, 50, "dish_image/1022.png", "☕"),
            MenuItem("1023", "Kopi Susu", "Minuman", 14000, 5500, 45, "dish_image/1023.png", "☕"),
            MenuItem("1024", "Air Mineral", "Minuman", 6000, 2500, 100, "dish_image/1024.png", "💧")
        )
    }
}

class DefaultPosSalesOrderRepository(
    initialOrders: List<OrderRecord> = defaultOrders
) : PosSalesOrderRepository {
    private val orders = MutableStateFlow(initialOrders)

    override fun getAllSalesOrders(): Flow<List<OrderRecord>> = orders.asStateFlow()

    override suspend fun getSalesOrderById(id: String): OrderRecord? =
        orders.value.find { it.id == id }

    override suspend fun createSalesOrder(order: OrderRecord): Boolean {
        orders.update { listOf(order) + it }
        return true
    }

    companion object {
        val defaultOrders = listOf(
            OrderRecord("ORD-001", "12:15", "Rahmat Hidayat", 63000, "Tunai", "Selesai", "1x Rendang Daging, 1x Es Teh Manis"),
            OrderRecord("ORD-002", "12:28", "Rahmat Hidayat", 94000, "QRIS", "Selesai", "2x Gulai Ayam, 1x Es Jeruk"),
            OrderRecord("ORD-003", "13:05", "Rahmat Hidayat", 40000, "Tunai", "Selesai", "1x Ayam Pop, 1x Nasi Putih")
        )
    }
}

class DefaultPosIngredientRepository(
    initialIngredients: List<IngredientItem> = defaultIngredients
) : PosIngredientRepository {
    private val ingredients = MutableStateFlow(initialIngredients)

    override fun getAllIngredients(): Flow<List<IngredientItem>> = ingredients.asStateFlow()

    override suspend fun updateIngredientStock(id: String, newStock: Double) {
        ingredients.update { list ->
            list.map { if (it.id == id) it.copy(stock = newStock) else it }
        }
    }

    override suspend fun saveIngredient(item: IngredientItem) {
        ingredients.update { list ->
            val idx = list.indexOfFirst { it.id == item.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, item) }
            else list + item
        }
    }

    override suspend fun deleteIngredient(id: String) {
        ingredients.update { list -> list.filterNot { it.id == id } }
    }

    companion object {
        val defaultIngredients = listOf(
            IngredientItem("ING-001", "Daging Sapi", "kg", 15.0, 5.0, 120000),
            IngredientItem("ING-002", "Ayam Potong", "ekor", 25.0, 8.0, 35000),
            IngredientItem("ING-003", "Beras Putih", "kg", 80.0, 20.0, 14000),
            IngredientItem("ING-004", "Santan Kelapa", "liter", 30.0, 10.0, 18000),
            IngredientItem("ING-005", "Cabai Merah", "kg", 12.0, 4.0, 45000),
            IngredientItem("ING-006", "Bawang Merah", "kg", 8.0, 3.0, 38000),
            IngredientItem("ING-007", "Minyak Goreng", "liter", 40.0, 15.0, 16000)
        )
    }
}

class DefaultPosEmployeeRepository(
    initialEmployees: List<Employee> = defaultEmployees
) : PosEmployeeRepository {
    private val employees = MutableStateFlow(initialEmployees)

    override fun getAllEmployees(): Flow<List<Employee>> = employees.asStateFlow()

    override suspend fun getEmployeeById(id: String): Employee? =
        employees.value.find { it.id == id }

    override suspend fun authenticatePin(pin: String): Employee? =
        employees.value.find { it.pin == pin }

    override suspend fun saveEmployee(employee: Employee) {
        employees.update { list ->
            val idx = list.indexOfFirst { it.id == employee.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, employee) }
            else list + employee
        }
    }

    companion object {
        val defaultEmployees = listOf(
            Employee("1", "Rahmat Hidayat", "Kasir Utama", "081234567890", "Aktif", "1234"),
            Employee("2", "Siti Nurhaliza", "Kasir Shift Siang", "081234567891", "Aktif", "5678"),
            Employee("3", "Budi Santoso", "Kepala Dapur", "081234567892", "Aktif", "9999"),
            Employee("4", "Dewi Lestari", "Supervisor", "081234567893", "Aktif", "0000")
        )
    }
}

class DefaultPosAuditLogRepository(
    initialLogs: List<AuditEntry> = defaultLogs
) : PosAuditLogRepository {
    private val logs = MutableStateFlow(initialLogs)

    override fun getAllLogs(): Flow<List<AuditEntry>> = logs.asStateFlow()

    override suspend fun addLog(entry: AuditEntry) {
        logs.update { listOf(entry) + it }
    }

    companion object {
        val defaultLogs = listOf(
            AuditEntry("08:00", "Rahmat Hidayat", "BUKA SESI", "Sesi kasir pagi dibuka dengan kas awal Rp 500.000"),
            AuditEntry("09:30", "Budi Santoso", "UPDATE STOK", "Menambah stok Rendang Daging sebanyak 20 porsi"),
            AuditEntry("11:15", "Rahmat Hidayat", "LOGIN", "Login berhasil ke sistem POS")
        )
    }
}
