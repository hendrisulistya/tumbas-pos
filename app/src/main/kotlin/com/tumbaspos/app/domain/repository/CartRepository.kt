package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    
    fun addToCart(product: ProductEntity, quantity: Int)
    fun updateQuantity(productId: Long, quantity: Int)
    fun removeFromCart(productId: Long)
    fun clearCart()
    fun getTotalAmount(): Double
}
