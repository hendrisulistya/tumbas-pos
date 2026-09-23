package com.argminres.app.domain.model

data class SalesSummary(
    val date: String,
    val totalSales: Double,
    val totalTransactions: Int
)

data class TopProduct(
    val dishId: Long,
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class LowStockProduct(
    val dishId: Long,
    val productName: String,
    val currentStock: Int,
    val threshold: Int = 10 // Default threshold
)
data class AggregatedIngredientUsage(
    val ingredientName: String,
    val unit: String,
    val startingQuantity: Double,
    val remainingQuantity: Double,
    val quantityUsed: Double,
    val totalCost: Double
)

data class AggregatedDishUsage(
    val dishName: String,
    val producedQuantity: Int,
    val remainingQuantity: Int,
    val soldQuantity: Int,
    val wasteQuantity: Int
)

data class CashierDishSales(
    val dishName: String,
    val cashierName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)
