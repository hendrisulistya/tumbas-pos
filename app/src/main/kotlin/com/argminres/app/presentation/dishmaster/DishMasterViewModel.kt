package com.argminres.app.presentation.dishmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishMasterUiState(
    val dishes: List<DishWithCategory> = emptyList(),
    val filteredDishes: List<DishWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedDish: DishWithCategory? = null,
    val isUploadingImage: Boolean = false,
    val error: String? = null
)

class DishMasterViewModel(
    private val dishRepository: DishRepository,
    private val manageProductImageUseCase: com.argminres.app.domain.usecase.dish.ManageDishImageUseCase,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(DishMasterUiState())
    val uiState: StateFlow<DishMasterUiState> = _uiState.asStateFlow()

    init {
        loadDishes()
    }

    private fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            dishRepository.getAllDishes().collect { dishes ->
                val filtered = if (_uiState.value.searchQuery.isBlank()) {
                    dishes
                } else {
                    dishes.filter { 
                        it.dish.name.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }
                }
                
                _uiState.update {
                    it.copy(
                        dishes = dishes,
                        filteredDishes = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        val filtered = if (query.isBlank()) {
            _uiState.value.dishes
        } else {
            _uiState.value.dishes.filter { 
                it.dish.name.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.update { it.copy(filteredDishes = filtered) }
    }

    fun onAddDishClick() {
        _uiState.update { 
            it.copy(
                showAddEditDialog = true,
                selectedDish = null
            )
        }
    }

    fun onEditDishClick(dish: DishWithCategory) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                selectedDish = dish
            )
        }
    }

    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                selectedDish = null
            )
        }
    }

    fun onSaveDish(
        name: String,
        category: String,
        price: Double,
        image: String? = null
    ) {
        viewModelScope.launch {
            try {
                val dish = _uiState.value.selectedDish?.dish
                
                if (dish != null) {
                    // Update existing
                    dishRepository.updateDish(
                        dish.copy(
                            name = name,
                            category = category,
                            price = price,
                            image = image,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Add new - start with 0 stock (will be set daily)
                    dishRepository.insertDish(
                        DishEntity(
                            name = name,
                            description = "",
                            category = category,
                            price = price,
                            stock = 0,
                            image = image
                        )
                    )
                }
                
                onDialogDismiss()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDeleteDish(dishId: Long) {
        viewModelScope.launch {
            try {
                val dish = _uiState.value.dishes.find { it.dish.id == dishId }?.dish
                if (dish != null) {
                    // Delete image if exists
                    dish.image?.let { image ->
                        manageProductImageUseCase.deleteImage(image)
                    }
                    dishRepository.deleteDish(dish)
                    
                    auditLogger.logAsync {
                        auditLogger.logDelete("DISH", dishId, "Name: ${dish.name}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    suspend fun uploadProductImage(imageData: ByteArray): Result<String> {
        _uiState.update { it.copy(isUploadingImage = true) }
        return try {
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
        if (imageData.size <= maxSizeBytes) return imageData

        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: return imageData

        val scaleFactor = kotlin.math.sqrt(maxSizeBytes.toDouble() / imageData.size)
        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
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
