package com.argminres.app.presentation.dish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.usecase.dish.ManageDishImageUseCase
import com.argminres.app.domain.usecase.dish.SearchDishesUseCase
import com.argminres.app.domain.usecase.showcase.GetCategoriesUseCase
import com.argminres.app.domain.usecase.showcase.ManageShowcaseDishUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val filteredProducts: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val categories: List<com.argminres.app.data.local.entity.CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: com.argminres.app.data.local.dao.DishWithCategory? = null,
    val isProductDialogOpen: Boolean = false,
    val isUploadingImage: Boolean = false
)

class DishViewModel(
    private val searchDishesUseCase: SearchDishesUseCase,
    private val manageProductUseCase: ManageShowcaseDishUseCase,
    private val manageProductImageUseCase: ManageDishImageUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            searchDishesUseCase("").collect { products: List<com.argminres.app.data.local.dao.DishWithCategory> ->
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

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
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

    fun onAddProductClick() {
        _uiState.update { it.copy(selectedProduct = null, isProductDialogOpen = true) }
    }

    fun onEditProductClick(product: com.argminres.app.data.local.dao.DishWithCategory) {
        _uiState.update { it.copy(selectedProduct = product, isProductDialogOpen = true) }
    }

    fun onProductDialogDismiss() {
        _uiState.update { it.copy(isProductDialogOpen = false) }
    }

    fun onSaveProduct(product: DishEntity) {
        viewModelScope.launch {
            try {
                val isNewProduct = product.id == 0L
                val savedProductId: Long

                if (isNewProduct) {
                    savedProductId = manageProductUseCase.createDish(product)
                } else {
                    manageProductUseCase.updateDish(product)
                    savedProductId = product.id
                }
                
                // Log audit trail
                auditLogger.logAsync {
                    if (isNewProduct) {
                        auditLogger.logCreate("PRODUCT", savedProductId, "Name: ${product.name}")
                    } else {
                        auditLogger.logUpdate("PRODUCT", savedProductId, "Name: ${product.name}")
                    }
                }
                
                _uiState.update { it.copy(isProductDialogOpen = false) }
            } catch (e: Exception) {
                // Log error or show snackbar? For now just log
                e.printStackTrace()
            }
        }
    }

    fun onDeleteProduct(productWithCategory: com.argminres.app.data.local.dao.DishWithCategory) {
        viewModelScope.launch {
            // Delete image if exists
            productWithCategory.dish.image?.let { image ->
                manageProductImageUseCase.deleteImage(image)
            }
            manageProductUseCase.deleteDish(productWithCategory.dish)
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
