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
