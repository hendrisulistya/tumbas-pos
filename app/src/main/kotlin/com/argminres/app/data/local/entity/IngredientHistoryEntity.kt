package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredient_history",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientId"), Index("sessionDate")]
)
data class IngredientHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingredientId: Long,
    val ingredientName: String,
    val stockAdded: Double,
    val unit: String,
    val sessionDate: String, // Format: "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "ADDED_TO_BAHAN"
)
