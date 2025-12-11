package com.argminres.app.data.repository

import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.repository.CartRepository
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepositoryImpl : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override fun addToCart(product: DishEntity, quantity: Int) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.product.id == product.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                currentItems + CartItem(product, quantity)
            }
        }
    }

    override fun updateQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { currentItems ->
            currentItems.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    override fun removeFromCart(productId: Long) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.product.id != productId }
        }
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getTotalAmount(): Double {
        return _cartItems.value.sumOf { it.subtotal }
    }
}
