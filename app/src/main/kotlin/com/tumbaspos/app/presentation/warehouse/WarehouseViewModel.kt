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
    val products: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: ProductEntity? = null,
    val isProductDialogOpen: Boolean = false,
    val isStockAdjustmentDialogOpen: Boolean = false
)

class WarehouseViewModel(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val manageProductUseCase: ManageProductUseCase,
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

    private fun filterProducts(products: List<ProductEntity>, query: String): List<ProductEntity> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.barcode.contains(query, ignoreCase = true)
        }
    }

    fun onAddProductClick() {
        _uiState.update { it.copy(selectedProduct = null, isProductDialogOpen = true) }
    }

    fun onEditProductClick(product: ProductEntity) {
        _uiState.update { it.copy(selectedProduct = product, isProductDialogOpen = true) }
    }

    fun onProductDialogDismiss() {
        _uiState.update { it.copy(isProductDialogOpen = false) }
    }

    fun onSaveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                manageProductUseCase.createProduct(product)
            } else {
                manageProductUseCase.updateProduct(product)
            }
            _uiState.update { it.copy(isProductDialogOpen = false) }
        }
    }

    fun onDeleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            manageProductUseCase.deleteProduct(product)
        }
    }

    fun onStockAdjustmentClick(product: ProductEntity) {
        _uiState.update { it.copy(selectedProduct = product, isStockAdjustmentDialogOpen = true) }
    }

    fun onStockAdjustmentDialogDismiss() {
        _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
    }

    fun onConfirmStockAdjustment(quantityChange: Int, reason: String) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            adjustStockUseCase(
                productId = product.id,
                quantityChange = quantityChange,
                reason = reason
            )
            _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
        }
    }
}
