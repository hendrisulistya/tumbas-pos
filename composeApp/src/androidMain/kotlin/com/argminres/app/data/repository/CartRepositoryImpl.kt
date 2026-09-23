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
            val existingItem = currentItems.find { it.dish?.id == product.id && !it.isPackage }
            if (existingItem != null) {
                currentItems.map {
                    if (it.dish?.id == product.id && !it.isPackage) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                currentItems + CartItem(dish = product, quantity = quantity)
            }
        }
    }

    override fun addPackageToCart(pkg: com.argminres.app.data.local.entity.PackageEntity, quantity: Int) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.pkg?.id == pkg.id && it.isPackage }
            if (existingItem != null) {
                currentItems.map {
                    if (it.pkg?.id == pkg.id && it.isPackage) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                currentItems + CartItem(pkg = pkg, quantity = quantity)
            }
        }
    }

    override fun updateQuantity(productId: Long, isPackage: Boolean, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId, isPackage)
            return
        }
        _cartItems.update { currentItems ->
            currentItems.map {
                val matches = if (isPackage) it.pkg?.id == productId else it.dish?.id == productId
                if (matches && it.isPackage == isPackage) it.copy(quantity = quantity) else it
            }
        }
    }

    override fun removeFromCart(productId: Long, isPackage: Boolean) {
        _cartItems.update { currentItems ->
            currentItems.filter { 
                val matches = if (isPackage) it.pkg?.id == productId else it.dish?.id == productId
                !(matches && it.isPackage == isPackage)
            }
        }
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getTotalAmount(): Double {
        return _cartItems.value.sumOf { it.subtotal }
    }
}
