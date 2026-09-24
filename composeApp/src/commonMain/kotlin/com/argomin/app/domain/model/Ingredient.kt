package com.argomin.app.domain.model

data class IngredientItem(
    val id: String,
    val name: String,
    val unit: String,
    var stock: Double,
    val minStock: Double,
    val costPerUnit: Long
)
