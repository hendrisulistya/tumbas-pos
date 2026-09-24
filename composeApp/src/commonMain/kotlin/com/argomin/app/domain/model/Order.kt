package com.argomin.app.domain.model

data class PosCartItem(
    val item: MenuItem,
    var quantity: Int
)

data class OrderRecord(
    val id: String,
    val time: String,
    val cashier: String,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String,
    val itemsSummary: String
)
