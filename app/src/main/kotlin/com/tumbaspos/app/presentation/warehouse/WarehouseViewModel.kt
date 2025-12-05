package com.tumbaspos.app.presentation.warehouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.domain.usecase.warehouse.AdjustStockUseCase
import com.tumbaspos.app.domain.usecase.warehouse.GetInventoryUseCase
import com.tumbaspos.app.domain.usecase.warehouse.ManageProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WarehouseUiState(
    val products: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val filteredProducts: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: com.tumbaspos.app.data.local.dao.ProductWithCategory? = null,
    val isStockAdjustmentDialogOpen: Boolean = false
)

class WarehouseViewModel(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val adjustStockUseCase: AdjustStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WarehouseUiState())
    val uiState: StateFlow<WarehouseUiState> = _uiState.asStateFlow()

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

    private fun filterProducts(products: List<com.tumbaspos.app.data.local.dao.ProductWithCategory>, query: String): List<com.tumbaspos.app.data.local.dao.ProductWithCategory> {
        if (query.isBlank()) return products
        return products.filter {
            it.product.name.contains(query, ignoreCase = true) ||
            it.product.barcode.contains(query, ignoreCase = true) ||
            (it.category?.name?.contains(query, ignoreCase = true) == true)
        }
    }



    fun onStockAdjustmentClick(product: com.tumbaspos.app.data.local.dao.ProductWithCategory) {
        _uiState.update { it.copy(selectedProduct = product, isStockAdjustmentDialogOpen = true) }
    }

    fun onStockAdjustmentDialogDismiss() {
        _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
    }

    fun onConfirmStockAdjustment(quantityChange: Int, reason: String) {
        val productWithCategory = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            adjustStockUseCase(
                productId = productWithCategory.product.id,
                quantityChange = quantityChange,
                reason = reason
            )
            _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
        }
    }
}
