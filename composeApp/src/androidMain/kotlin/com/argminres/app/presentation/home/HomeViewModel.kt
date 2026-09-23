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
    val allItems: List<ProductItem> = emptyList(),
    val filteredItems: List<ProductItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val cart: List<com.argminres.app.presentation.sales.CartItem> = emptyList(),
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val searchProductsUseCase: SearchDishesUseCase,
    private val packageRepository: com.argminres.app.domain.repository.PackageRepository,
    private val cartRepository: com.argminres.app.domain.repository.CartRepository,
    private val dishComponentRepository: com.argminres.app.domain.repository.DishComponentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
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

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val dishesFlow = searchProductsUseCase("")
            val packagesFlow = packageRepository.getAllPackages()
            val componentsFlow = dishComponentRepository.getAllComponentEntities()

            combine(dishesFlow, packagesFlow, componentsFlow) { dishes, packages, components ->
                Triple(dishes, packages, components)
            }.collect { (allDishes, allPackages, allComponents) ->
                
                val productItems = mutableListOf<ProductItem>()
                
                // Add Dishes
                allDishes.filter { it.dish.category != "Paket" }.forEach { 
                    productItems.add(ProductItem.Dish(it))
                }
                
                // Add Packages with dynamic stock
                allPackages.forEach { pkg ->
                    val virtualStock = calculatePackageVirtualStock(pkg, allDishes, allComponents)
                    productItems.add(ProductItem.Package(pkg, virtualStock))
                }

                val categoryOrder = listOf("Paket", "Makanan", "Minuman", "Lain-lain")
                val categories = listOf("All") + productItems
                    .map { it.categoryName }
                    .distinct()
                    .sortedWith(compareBy<String> { name ->
                        val idx = categoryOrder.indexOf(name)
                        if (idx >= 0) idx else Int.MAX_VALUE
                    })

                val filteredItems = filterItems(productItems, _uiState.value.selectedCategory, _uiState.value.searchQuery)

                _uiState.update { state ->
                    state.copy(
                        allItems = productItems,
                        filteredItems = filteredItems,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculatePackageVirtualStock(
        pkg: com.argminres.app.data.local.entity.PackageEntity,
        allDishes: List<DishWithCategory>,
        allComponents: List<com.argminres.app.data.local.entity.DishComponentEntity>
    ): Int {
        val components = allComponents.filter { it.packageId == pkg.id }
        if (components.isEmpty()) return 0

        var minStock = Int.MAX_VALUE
        components.forEach { component ->
            val compDish = allDishes.find { it.dish.id == component.componentDishId }
            val compStock = compDish?.dish?.stock ?: 0
            if (compStock < minStock) {
                minStock = compStock
            }
        }
        return if (minStock == Int.MAX_VALUE) 0 else minStock
    }

    private fun filterItems(items: List<ProductItem>, category: String, query: String): List<ProductItem> {
        return items
            .filter { category == "All" || it.categoryName == category }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    }

    fun onCategorySelected(category: String) {
        val filtered = filterItems(_uiState.value.allItems, category, _uiState.value.searchQuery)
        _uiState.update { it.copy(selectedCategory = category, filteredItems = filtered) }
    }

    fun onSearchQueryChange(query: String) {
        val filtered = filterItems(_uiState.value.allItems, _uiState.value.selectedCategory, query)
        _uiState.update { it.copy(searchQuery = query, filteredItems = filtered) }
    }

    fun addToCart(item: ProductItem) {
        when (item) {
            is ProductItem.Dish -> cartRepository.addToCart(item.dishWithCategory.dish, 1)
            is ProductItem.Package -> cartRepository.addPackageToCart(item.pkg, 1)
        }
    }

    fun increaseQuantity(itemId: Long, isPackage: Boolean) {
        viewModelScope.launch {
            val item = _uiState.value.cart.find { it.id == itemId && it.isPackage == isPackage }
            if (item != null) {
                cartRepository.updateQuantity(itemId, isPackage, item.quantity + 1)
            }
        }
    }

    fun decreaseQuantity(itemId: Long, isPackage: Boolean) {
        viewModelScope.launch {
            val item = _uiState.value.cart.find { it.id == itemId && it.isPackage == isPackage }
            if (item != null) {
                if (item.quantity > 1) {
                    cartRepository.updateQuantity(itemId, isPackage, item.quantity - 1)
                } else {
                    cartRepository.removeFromCart(itemId, isPackage)
                }
            }
        }
    }

    fun getCartQuantity(itemId: Long, isPackage: Boolean): Int {
        return _uiState.value.cart.find { it.id == itemId && it.isPackage == isPackage }?.quantity ?: 0
    }
}
