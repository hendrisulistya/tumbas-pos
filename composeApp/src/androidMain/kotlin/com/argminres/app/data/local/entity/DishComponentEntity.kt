package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dish_components",
    foreignKeys = [
        ForeignKey(
            entity = PackageEntity::class,
            parentColumns = ["id"],
            childColumns = ["packageId"],
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
    val packageId: Long,          // The package (from packages table)
    val componentDishId: Long,    // Component dish (from dishes table)
    val quantity: Int = 1         // Always 1 for simple system
)
