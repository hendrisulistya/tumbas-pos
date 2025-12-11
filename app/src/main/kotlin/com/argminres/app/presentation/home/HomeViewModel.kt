package com.argminres.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.usecase.sales.SearchDishesUseCase
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val products: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val cart: List<CartItem> = emptyList(),
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val searchProductsUseCase: SearchDishesUseCase,
    private val cartRepository: com.argminres.app.domain.repository.CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { items ->
                _uiState.update { 
                    it.copy(
                        cart = items,
                        cartItemCount = items.sumOf { item -> item.quantity }
                    ) 
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            searchProductsUseCase("").collect { products: List<com.argminres.app.data.local.dao.DishWithCategory> ->
                val categories = listOf("All") + products.mapNotNull { product: com.argminres.app.data.local.dao.DishWithCategory -> product.category?.name }.distinct().sorted()
                _uiState.update {
                    it.copy(
                        products = products,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }
        filterProducts()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterProducts()
    }

    private fun filterProducts() {
        viewModelScope.launch {
            val state = _uiState.value
            val query = state.searchQuery
            
            searchProductsUseCase(query).collect { allProducts: List<com.argminres.app.data.local.dao.DishWithCategory> ->
                val filtered = if (state.selectedCategory == "All") {
                    allProducts
                } else {
                    allProducts.filter { product: com.argminres.app.data.local.dao.DishWithCategory -> product.category?.name == state.selectedCategory }
                }
                
                _uiState.update { it.copy(products = filtered) }
            }
        }
    }

    fun addToCart(productWithCategory: com.argminres.app.data.local.dao.DishWithCategory) {
        cartRepository.addToCart(productWithCategory.dish, 1)
    }
    
    fun increaseQuantity(productId: Long) {
        viewModelScope.launch {
            val currentQty = cartRepository.cartItems.value.find { it.product.id == productId }?.quantity ?: 0
            if (currentQty > 0) {
                cartRepository.updateQuantity(productId, currentQty + 1)
            }
        }
    }
    
    fun decreaseQuantity(productId: Long) {
        val cartItem = _uiState.value.cart.find { it.product.id == productId }
        if (cartItem != null) {
            if (cartItem.quantity > 1) {
                cartRepository.updateQuantity(productId, cartItem.quantity - 1)
            } else {
                cartRepository.removeFromCart(productId)
            }
        }
    }
    
    fun getCartQuantity(productId: Long): Int {
        return _uiState.value.cart.find { it.product.id == productId }?.quantity ?: 0
    }
}
