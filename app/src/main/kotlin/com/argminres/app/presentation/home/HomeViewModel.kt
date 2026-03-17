package com.argminres.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.domain.usecase.sales.SearchDishesUseCase
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val allDishes: List<DishWithCategory> = emptyList(),
    val dishes: List<DishWithCategory> = emptyList(),
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

            searchProductsUseCase("").collect { allDishes ->
                val categoryOrder = listOf("Paket", "Makanan", "Minuman", "Lain-lain")
                val categories = listOf("All") + allDishes
                    .map { it.category?.name ?: "Uncategorized" }
                    .distinct()
                    .sortedWith(compareBy { name ->
                        val idx = categoryOrder.indexOf(name)
                        if (idx >= 0) idx else Int.MAX_VALUE
                    })

                val filteredDishes = filterDishes(allDishes, _uiState.value.selectedCategory, _uiState.value.searchQuery)

                _uiState.update {
                    it.copy(
                        allDishes = allDishes,
                        dishes = filteredDishes,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterDishes(allDishes: List<DishWithCategory>, category: String, query: String): List<DishWithCategory> {
        return allDishes
            .filter { category == "All" || (it.category?.name ?: "Uncategorized") == category }
            .filter { query.isBlank() || it.dish.name.contains(query, ignoreCase = true) }
    }

    fun onCategorySelected(category: String) {
        val filtered = filterDishes(_uiState.value.allDishes, category, _uiState.value.searchQuery)
        _uiState.update { it.copy(selectedCategory = category, dishes = filtered) }
    }

    fun onSearchQueryChange(query: String) {
        val filtered = filterDishes(_uiState.value.allDishes, _uiState.value.selectedCategory, query)
        _uiState.update { it.copy(searchQuery = query, dishes = filtered) }
    }

    fun addToCart(dishWithCategory: DishWithCategory) {
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
        return _uiState.value.cart.find { it.product.id == dishId }?.quantity ?: 0
    }
}
