package com.argminres.app.domain.repository

import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    
    fun addToCart(product: DishEntity, quantity: Int)
    fun addPackageToCart(pkg: com.argminres.app.data.local.entity.PackageEntity, quantity: Int)
    fun updateQuantity(productId: Long, isPackage: Boolean, quantity: Int)
    fun removeFromCart(productId: Long, isPackage: Boolean)
    fun clearCart()
    fun getTotalAmount(): Double
}
