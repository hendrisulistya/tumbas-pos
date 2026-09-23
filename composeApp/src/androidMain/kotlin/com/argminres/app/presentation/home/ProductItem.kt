package com.argminres.app.presentation.home

import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.PackageEntity

sealed class ProductItem {
    data class Dish(val dishWithCategory: DishWithCategory) : ProductItem()
    data class Package(val pkg: PackageEntity, val virtualStock: Int) : ProductItem()
    
    val id: Long get() = when(this) {
        is Dish -> dishWithCategory.dish.id
        is Package -> pkg.id
    }
    
    val name: String get() = when(this) {
        is Dish -> dishWithCategory.dish.name
        is Package -> pkg.name
    }
    
    val price: Double get() = when(this) {
        is Dish -> dishWithCategory.dish.price
        is Package -> pkg.price
    }
    
    val image: String? get() = when(this) {
        is Dish -> dishWithCategory.dish.image
        is Package -> pkg.image
    }
    
    val categoryName: String get() = when(this) {
        is Dish -> dishWithCategory.category?.name ?: "Uncategorized"
        is Package -> "Paket"
    }
    
    val stock: Int get() = when(this) {
        is Dish -> dishWithCategory.dish.stock
        is Package -> virtualStock
    }

    val isPackage: Boolean get() = this is Package
}
