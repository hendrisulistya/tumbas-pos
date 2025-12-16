package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dish_components",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["packageDishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["componentDishId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DishComponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageDishId: Long,      // The package dish (e.g., Paket Rendang)
    val componentDishId: Long,    // Component dish (e.g., Nasi Putih)
    val quantity: Int = 1         // Always 1 for simple system
)
