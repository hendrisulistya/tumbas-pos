package com.argomin.app.domain.repository.pos

import com.argomin.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PosDishRepository {
    fun getAllDishes(): Flow<List<MenuItem>>
    suspend fun getDishById(id: String): MenuItem?
    suspend fun updateStock(dishId: String, newStock: Int)
    suspend fun saveDish(item: MenuItem)
    suspend fun deleteDish(id: String)
}

interface PosSalesOrderRepository {
    fun getAllSalesOrders(): Flow<List<OrderRecord>>
    suspend fun getSalesOrderById(id: String): OrderRecord?
    suspend fun createSalesOrder(order: OrderRecord): Boolean
}

interface PosIngredientRepository {
    fun getAllIngredients(): Flow<List<IngredientItem>>
    suspend fun updateIngredientStock(id: String, newStock: Double)
    suspend fun saveIngredient(item: IngredientItem)
    suspend fun deleteIngredient(id: String)
}

interface PosEmployeeRepository {
    fun getAllEmployees(): Flow<List<Employee>>
    suspend fun getEmployeeById(id: String): Employee?
    suspend fun authenticatePin(pin: String): Employee?
    suspend fun saveEmployee(employee: Employee)
}

interface PosAuditLogRepository {
    fun getAllLogs(): Flow<List<AuditEntry>>
    suspend fun addLog(entry: AuditEntry)
}
