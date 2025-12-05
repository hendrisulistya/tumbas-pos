package com.tumbaspos.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.domain.usecase.sales.SearchProductsUseCase
import com.tumbaspos.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val cart: List<CartItem> = emptyList(),
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val cartRepository: com.tumbaspos.app.domain.repository.CartRepository
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
            searchProductsUseCase("").collect { products ->
                val categories = listOf("All") + products.map { it.category }.distinct().sorted()
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
            
            searchProductsUseCase(query).collect { allProducts ->
                val filtered = if (state.selectedCategory == "All") {
                    allProducts
                } else {
                    allProducts.filter { it.category == state.selectedCategory }
                }
                
                _uiState.update { it.copy(products = filtered) }
            }
        }
    }

    fun addToCart(product: ProductEntity) {
        cartRepository.addToCart(product, 1)
    }
}
