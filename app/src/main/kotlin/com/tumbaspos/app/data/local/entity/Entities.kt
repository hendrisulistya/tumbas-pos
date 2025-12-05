package com.tumbaspos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

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
    val category: String,
    val imageUrl: String? = null,
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
    val paymentMethod: String, // CASH, CARD, QRIS
    val totalAmount: Double,
    val discount: Double,
    val tax: Double,
    val status: String, // COMPLETED, REFUNDED, VOID
    val createdAt: Long = System.currentTimeMillis()
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
