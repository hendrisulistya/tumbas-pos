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
    val dishes: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
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
        loadDishes()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { items ->
                android.util.Log.d("HomeViewModel", "Cart updated: ${items.size} items")
                items.forEach { item ->
                    android.util.Log.d("HomeViewModel", "  - ${item.product.name}: qty=${item.quantity}")
                }
                _uiState.update { 
                    it.copy(
                        cart = items,
                        cartItemCount = items.sumOf { item -> item.quantity }
                    ) 
                }
            }
        }
    }

    private fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            searchProductsUseCase(_uiState.value.searchQuery).collect { dishes ->
                val categories = listOf("All") + dishes.map { it.category?.name ?: "Uncategorized" }.distinct()
                
                val filteredDishes = if (_uiState.value.selectedCategory == "All") {
                    dishes
                } else {
                    dishes.filter { (it.category?.name ?: "Uncategorized") == _uiState.value.selectedCategory }
                }
                
                _uiState.update { 
                    it.copy(
                        dishes = filteredDishes,
                        categories = categories,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadDishes()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadDishes()
    }

    fun addToCart(dishWithCategory: com.argminres.app.data.local.dao.DishWithCategory) {
        android.util.Log.d("HomeViewModel", "Adding to cart: ${dishWithCategory.dish.name}")
        cartRepository.addToCart(dishWithCategory.dish, 1)
    }

    fun increaseQuantity(dishId: Long) {
        viewModelScope.launch {
            val item = _uiState.value.cart.find { it.product.id == dishId }
            if (item != null) {
                cartRepository.updateQuantity(dishId, item.quantity + 1)
            }
        }
    }

    fun decreaseQuantity(dishId: Long) {
        viewModelScope.launch {
            val item = _uiState.value.cart.find { it.product.id == dishId }
            if (item != null) {
                if (item.quantity > 1) {
                    cartRepository.updateQuantity(dishId, item.quantity - 1)
                } else {
                    cartRepository.removeFromCart(dishId)
                }
            }
        }
    }

    fun getCartQuantity(dishId: Long): Int {
        val qty = _uiState.value.cart.find { it.product.id == dishId }?.quantity ?: 0
        android.util.Log.d("HomeViewModel", "getCartQuantity for dishId=$dishId: $qty")
        return qty
    }
}
