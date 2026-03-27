package com.argminres.app.presentation.dishmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishMasterUiState(
    val dishes: List<DishWithCategory> = emptyList(),
    val filteredDishes: List<DishWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedDish: DishWithCategory? = null,
    val selectedPackageComponents: List<Long> = emptyList(),
    val selectedTab: Int = 0, // 0 = Hidangan, 1 = Paket
    val isUploadingImage: Boolean = false,
    val error: String? = null
)

class DishMasterViewModel(
    private val dishRepository: DishRepository,
    private val manageProductImageUseCase: com.argminres.app.domain.usecase.dish.ManageDishImageUseCase,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger,
    private val dishComponentRepository: com.argminres.app.domain.repository.DishComponentRepository
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
                updateFilteredDishes(dishes, _uiState.value.searchQuery, _uiState.value.selectedTab)
            }
        }
    }

    private fun updateFilteredDishes(dishes: List<DishWithCategory>, query: String, tab: Int) {
        val filtered = dishes.filter { 
            val matchesQuery = it.dish.name.contains(query, ignoreCase = true) || 
                               it.dish.id.toString().contains(query)
            val matchesTab = if (tab == 0) {
                it.dish.category != "Paket"
            } else {
                it.dish.category == "Paket"
            }
            matchesQuery && matchesTab
        }
        
        _uiState.update {
            it.copy(
                dishes = dishes,
                filteredDishes = filtered,
                isLoading = false
            )
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        updateFilteredDishes(_uiState.value.dishes, _uiState.value.searchQuery, index)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredDishes(_uiState.value.dishes, query, _uiState.value.selectedTab)
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
        viewModelScope.launch {
            val components = if (dish.dish.category == "Paket") {
                dishComponentRepository.getComponentEntities(dish.dish.id).first().map { it.componentDishId }
            } else {
                emptyList<Long>()
            }
            
            _uiState.update {
                it.copy(
                    showAddEditDialog = true,
                    selectedDish = dish,
                    selectedPackageComponents = components
                )
            }
        }
    }

    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                selectedDish = null,
                selectedPackageComponents = emptyList()
            )
        }
    }

    fun onSaveDish(
        name: String,
        category: String,
        price: Double,
        image: String? = null,
        components: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val existingDish = _uiState.value.selectedDish?.dish
                val savedId: Long
                
                if (existingDish != null) {
                    savedId = existingDish.id
                    dishRepository.updateDish(
                        existingDish.copy(
                            name = name,
                            category = category,
                            price = price,
                            image = image,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    savedId = dishRepository.insertDish(
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

                // Sync components if it's a Paket
                if (category == "Paket") {
                    dishComponentRepository.removeAllComponents(savedId)
                    components.forEach { compId ->
                        dishComponentRepository.addComponent(savedId, compId)
                    }
                }
                
                onDialogDismiss()
                
                auditLogger.logAsync {
                    if (existingDish != null) auditLogger.logUpdate("DISH", savedId, "Name: $name")
                    else auditLogger.logCreate("DISH", savedId, "Name: $name")
                }
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
                    // Clean up components if it's a Paket
                    if (dish.category == "Paket") {
                        dishComponentRepository.removeAllComponents(dish.id)
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
