package com.argminres.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.domain.usecase.sales.SearchDishesUseCase
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    val isLoading: Boolean = false,
    val componentEntities: List<com.argminres.app.data.local.entity.DishComponentEntity> = emptyList()
)

class HomeViewModel(
    private val searchProductsUseCase: SearchDishesUseCase,
    private val cartRepository: com.argminres.app.domain.repository.CartRepository,
    private val dishComponentRepository: com.argminres.app.domain.repository.DishComponentRepository
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

            // Use explicit flow variables to help the compiler with type inference
            val dishesFlow: kotlinx.coroutines.flow.Flow<List<DishWithCategory>> = searchProductsUseCase("")
            val componentsFlow: kotlinx.coroutines.flow.Flow<List<com.argminres.app.data.local.entity.DishComponentEntity>> = 
                dishComponentRepository.getAllComponentEntities()

            dishesFlow.combine(componentsFlow) { allDishes, components ->
                allDishes to components
            }.collect { (allDishesFromFlow, componentsFromFlow) ->
                val categoryOrder = listOf("Paket", "Makanan", "Minuman", "Lain-lain")
                val categories = listOf("All") + allDishesFromFlow
                    .map { dwc: DishWithCategory -> dwc.category?.name ?: "Uncategorized" }
                    .distinct()
                    .sortedWith(compareBy<String> { name: String ->
                        val idx = categoryOrder.indexOf(name)
                        if (idx >= 0) idx else Int.MAX_VALUE
                    })

                val dishesWithVirtualStock = allDishesFromFlow.map { dwc: DishWithCategory ->
                    val virtualStock = calculateVirtualStock(dwc, allDishesFromFlow, componentsFromFlow)
                    dwc.copy(
                        dish = dwc.dish.copy(stock = virtualStock)
                    )
                }

                val filteredDishes = filterDishes(dishesWithVirtualStock, _uiState.value.selectedCategory, _uiState.value.searchQuery)

                _uiState.update { state ->
                    state.copy(
                        allDishes = dishesWithVirtualStock,
                        dishes = filteredDishes,
                        categories = categories,
                        componentEntities = componentsFromFlow,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateVirtualStock(
        dishWithCat: DishWithCategory,
        allDishes: List<DishWithCategory>,
        components: List<com.argminres.app.data.local.entity.DishComponentEntity>
    ): Int {
        val dishComponents = components.filter { it.packageDishId == dishWithCat.dish.id }
        if (dishComponents.isEmpty()) return dishWithCat.dish.stock

        // For packages, stock is the minimum stock of its components
        var minStock = Int.MAX_VALUE
        dishComponents.forEach { component ->
            val compDish = allDishes.find { it.dish.id == component.componentDishId }
            val compStock = compDish?.dish?.stock ?: 0
            if (compStock < minStock) {
                minStock = compStock
            }
        }
        return if (minStock == Int.MAX_VALUE) 0 else minStock
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
