package com.tumbaspos.app.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.domain.usecase.product.ManageProductImageUseCase
import com.tumbaspos.app.domain.usecase.warehouse.GetInventoryUseCase
import com.tumbaspos.app.domain.usecase.warehouse.ManageProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: ProductEntity? = null,
    val isProductDialogOpen: Boolean = false,
    val isUploadingImage: Boolean = false
)

class ProductViewModel(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val manageProductUseCase: ManageProductUseCase,
    private val manageProductImageUseCase: ManageProductImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
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
            it.barcode.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
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
            // Delete image if exists
            product.image?.let { image ->
                manageProductImageUseCase.deleteImage(image)
            }
            manageProductUseCase.deleteProduct(product)
        }
    }
    
    suspend fun uploadProductImage(imageData: ByteArray): Result<String> {
        _uiState.update { it.copy(isUploadingImage = true) }
        return try {
            manageProductImageUseCase.uploadImage(imageData).also {
                _uiState.update { state -> state.copy(isUploadingImage = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isUploadingImage = false) }
            Result.failure(e)
        }
    }
}
