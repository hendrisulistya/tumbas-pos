package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Entity(tableName = "employers")
data class EmployerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val role: String, // "MANAGER" or "CASHIER"
    val pin: String // 4-digit PIN
)

@Entity(tableName = "daily_sessions")
data class DailySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionDate: Long, // Start of day timestamp
    val startedBy: Long?, // Employer ID who started session
    val status: String, // ACTIVE, PENDING_CLOSE, CLOSED
    val totalSales: Double = 0.0,
    val totalWaste: Double = 0.0,
    val totalProfit: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

@Entity(tableName = "waste_records")
data class WasteRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val dishId: Long,
    val dishName: String, // Store name for historical record
    val quantity: Int,
    val costPrice: Double, // Cost per dish
    val totalLoss: Double, // quantity * costPrice
    val reason: String = "UNSOLD", // UNSOLD, DAMAGED, EXPIRED
    val recordedBy: Long?, // Employer ID
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ingredient_usage")
data class IngredientUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val ingredientId: Long,
    val ingredientName: String, // Store name for historical record
    val quantityUsed: Double, // Amount used during the day
    val unit: String, // kg, liter, pcs, etc.
    val costPerUnit: Double,
    val totalCost: Double, // quantityUsed * costPerUnit
    val recordedBy: Long?, // Employer ID
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dishes")
@Serializable
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val name: String,
    val description: String,
    val price: Double,
    val costPrice: Double,
    val stock: Int,
    val categoryId: Long,
    val image: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
@Serializable
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
@Serializable
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val loyaltyPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_orders")
@Serializable
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val orderDate: Long,
    val cashierId: Long?, // ID of the employer/cashier who created this order
    val status: String, // DRAFT, SUBMITTED, RECEIVED, CANCELLED
    val totalAmount: Double,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_order_items")
@Serializable
data class PurchaseOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseOrderId: Long,
    val ingredientId: Long, // Changed from dishId
    val quantity: Double, // Changed to Double for fractional units
    val unitCost: Double,
    val subtotal: Double
)

@Entity(tableName = "sales_orders")
@Serializable
data class SalesOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val orderDate: Long,
    val customerId: Long?, // Nullable for guest checkout
    val cashierId: Long?, // ID of the employer/cashier who created this order
    val totalAmount: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val paymentMethod: String, // CASH, CARD, TRANSFER
    val status: String, // DRAFT, COMPLETED, CANCELLED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales_order_items")
@Serializable
data class SalesOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val salesOrderId: Long,
    val dishId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

@Entity(tableName = "stock_movements")
@Serializable
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    val movementType: String, // IN, OUT, ADJUSTMENT
    val quantity: Int,
    val referenceId: Long?, // PurchaseOrderID or SalesOrderID
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ingredient_stock_movements")
@Serializable
data class IngredientStockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientId: Long,
    val movementType: String, // PURCHASE, USAGE, ADJUSTMENT, WASTE
    val quantity: Double,
    val referenceId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
@Serializable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String
)

@Entity(tableName = "ingredients")
@Serializable
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val unit: String, // kg, liter, pcs, etc.
    val stock: Double = 0.0,
    val minimumStock: Double = 0.0,
    val costPerUnit: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ingredient_categories")
@Serializable
data class IngredientCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_settings")
@Serializable
data class StoreSettingsEntity(
    @PrimaryKey
    val id: Long = 1L, // Singleton - always 1
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeTaxId: String = "",
    val logoImage: String? = null // Base64 encoded image
)
