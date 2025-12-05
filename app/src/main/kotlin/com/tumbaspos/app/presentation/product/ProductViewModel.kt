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
    val products: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val filteredProducts: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val categories: List<com.tumbaspos.app.data.local.entity.CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: com.tumbaspos.app.data.local.dao.ProductWithCategory? = null,
    val isProductDialogOpen: Boolean = false,
    val isUploadingImage: Boolean = false
)

class ProductViewModel(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val manageProductUseCase: ManageProductUseCase,
    private val manageProductImageUseCase: ManageProductImageUseCase,
    private val getCategoriesUseCase: com.tumbaspos.app.domain.usecase.warehouse.GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Load Categories
            launch {
                getCategoriesUseCase().collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
            }

            // Load Products
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

    fun onAddProductClick() {
        _uiState.update { it.copy(selectedProduct = null, isProductDialogOpen = true) }
    }

    fun onEditProductClick(product: com.tumbaspos.app.data.local.dao.ProductWithCategory) {
        _uiState.update { it.copy(selectedProduct = product, isProductDialogOpen = true) }
    }

    fun onProductDialogDismiss() {
        _uiState.update { it.copy(isProductDialogOpen = false) }
    }

    fun onSaveProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                if (product.id == 0L) {
                    manageProductUseCase.createProduct(product)
                } else {
                    manageProductUseCase.updateProduct(product)
                }
                _uiState.update { it.copy(isProductDialogOpen = false) }
            } catch (e: Exception) {
                // Log error or show snackbar? For now just log
                e.printStackTrace()
            }
        }
    }

    fun onDeleteProduct(productWithCategory: com.tumbaspos.app.data.local.dao.ProductWithCategory) {
        viewModelScope.launch {
            // Delete image if exists
            productWithCategory.product.image?.let { image ->
                manageProductImageUseCase.deleteImage(image)
            }
            manageProductUseCase.deleteProduct(productWithCategory.product)
        }
    }
    
    suspend fun uploadProductImage(imageData: ByteArray): Result<String> {
        _uiState.update { it.copy(isUploadingImage = true) }
        return try {
            // Compress image if it's too large (limit to ~500KB to stay well under 2MB SQLite limit)
            val compressedData = compressImageIfNeeded(imageData, maxSizeKb = 500)
            
            manageProductImageUseCase.uploadImage(compressedData).also {
                _uiState.update { state -> state.copy(isUploadingImage = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isUploadingImage = false) }
            Result.failure(e)
        }
    }
    
    private fun compressImageIfNeeded(imageData: ByteArray, maxSizeKb: Int): ByteArray {
        val maxSizeBytes = maxSizeKb * 1024
        
        // If already small enough, return as-is
        if (imageData.size <= maxSizeBytes) {
            return imageData
        }
        
        // Decode the image
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: return imageData // If decode fails, return original
        
        // Calculate scale factor to reduce size
        val scaleFactor = kotlin.math.sqrt(maxSizeBytes.toDouble() / imageData.size)
        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()
        
        // Scale down the bitmap
        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        // Compress to JPEG with quality adjustment
        val outputStream = java.io.ByteArrayOutputStream()
        var quality = 85
        
        do {
            outputStream.reset()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality -= 5
        } while (outputStream.size() > maxSizeBytes && quality > 10)
        
        bitmap.recycle()
        scaledBitmap.recycle()
        
        return outputStream.toByteArray()
    }
}
