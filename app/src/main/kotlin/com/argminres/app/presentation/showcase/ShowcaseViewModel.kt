package com.argminres.app.presentation.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.usecase.showcase.AdjustShowcaseStockUseCase
import com.argminres.app.domain.usecase.showcase.GetShowcaseInventoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShowcaseUiState(
    val products: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val filteredProducts: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: com.argminres.app.data.local.dao.DishWithCategory? = null,
    val isStockAdjustmentDialogOpen: Boolean = false
)

class ShowcaseViewModel(
    private val getInventoryUseCase: GetShowcaseInventoryUseCase,
    private val adjustStockUseCase: AdjustShowcaseStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowcaseUiState())
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getInventoryUseCase().collect { products ->
                _uiState.update { state ->
                    state.copy(
                        products = products,
                        filteredProducts = filterProducts(products, state.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredProducts = filterProducts(state.products, query)
            )
        }
    }

    private fun filterProducts(products: List<com.argminres.app.data.local.dao.DishWithCategory>, query: String): List<com.argminres.app.data.local.dao.DishWithCategory> {
        if (query.isBlank()) return products
        return products.filter {
            it.dish.name.contains(query, ignoreCase = true) ||
            it.dish.barcode.contains(query, ignoreCase = true) ||
            (it.category?.name?.contains(query, ignoreCase = true) == true)
        }
    }



    fun onStockAdjustmentClick(product: com.argminres.app.data.local.dao.DishWithCategory) {
        _uiState.update { it.copy(selectedProduct = product, isStockAdjustmentDialogOpen = true) }
    }

    fun onStockAdjustmentDialogDismiss() {
        _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
    }

    fun onConfirmStockAdjustment(quantityChange: Int, reason: String) {
        val productWithCategory = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            adjustStockUseCase(
                dishId = productWithCategory.dish.id,
                quantityChange = quantityChange,
                reason = reason
            )
            _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
        }
    }
}
