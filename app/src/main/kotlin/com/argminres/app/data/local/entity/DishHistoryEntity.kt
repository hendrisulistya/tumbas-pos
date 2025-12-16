package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dish_history",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId"), Index("sessionDate")]
)
data class DishHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dishId: Long,
    val dishName: String,
    val stockAdded: Int,
    val sessionDate: String, // Format: "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "ADDED_TO_ETALASE" // Future: STOCK_ADJUSTED, REMOVED, etc.
)
