package com.argomin.app.domain.model

data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val cost: Long,
    var stock: Int,
    val image: String = "",
    val emoji: String = "🍲",
    val unit: String = "Porsi"
)
