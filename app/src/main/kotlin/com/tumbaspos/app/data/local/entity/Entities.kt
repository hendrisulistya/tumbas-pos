package com.tumbaspos.app.data.local.entity

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

@Entity(tableName = "products")
@Serializable
data class ProductEntity(
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
    val productId: Long,
    val quantity: Int,
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
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

@Entity(tableName = "stock_movements")
@Serializable
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val movementType: String, // IN, OUT, ADJUSTMENT
    val quantity: Int,
    val referenceId: Long?, // PurchaseOrderID or SalesOrderID
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
@Serializable
data class CategoryEntity(
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
