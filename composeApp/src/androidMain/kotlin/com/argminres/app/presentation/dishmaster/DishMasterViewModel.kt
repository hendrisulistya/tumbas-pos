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
    val packages: List<com.argminres.app.data.local.entity.PackageEntity> = emptyList(),
    val filteredPackages: List<com.argminres.app.data.local.entity.PackageEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedDish: DishWithCategory? = null,
    val selectedPackage: com.argminres.app.data.local.entity.PackageEntity? = null,
    val selectedPackageComponents: List<Long> = emptyList(),
    val selectedTab: Int = 0, // 0 = Hidangan, 1 = Paket
    val isUploadingImage: Boolean = false,
    val error: String? = null
)

class DishMasterViewModel(
    private val dishRepository: DishRepository,
    private val packageRepository: com.argminres.app.domain.repository.PackageRepository,
    private val manageProductImageUseCase: com.argminres.app.domain.usecase.dish.ManageDishImageUseCase,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger,
    private val dishComponentRepository: com.argminres.app.domain.repository.DishComponentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DishMasterUiState())
    val uiState: StateFlow<DishMasterUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            launch {
                dishRepository.getAllDishes().collect { dishes ->
                    _uiState.update { it.copy(dishes = dishes) }
                    updateFilteredData()
                }
            }
            
            launch {
                packageRepository.getAllPackages().collect { packages ->
                    _uiState.update { it.copy(packages = packages) }
                    updateFilteredData()
                }
            }
        }
    }

    private fun updateFilteredData() {
        val state = _uiState.value
        val query = state.searchQuery
        
        val filteredDishes = state.dishes.filter { 
            it.dish.name.contains(query, ignoreCase = true) || 
            it.dish.id.toString().contains(query)
        }.filter { it.dish.category != "Paket" }
        
        val filteredPackages = state.packages.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.id.toString().contains(query)
        }
        
        _uiState.update {
            it.copy(
                filteredDishes = filteredDishes,
                filteredPackages = filteredPackages,
                isLoading = false
            )
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        updateFilteredData()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredData()
    }

    fun onAddDishClick() {
        _uiState.update { 
            it.copy(
                showAddEditDialog = true,
                selectedDish = null,
                selectedPackage = null,
                selectedPackageComponents = emptyList()
            )
        }
    }

    fun onEditDishClick(dish: DishWithCategory) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                selectedDish = dish,
                selectedPackage = null,
                selectedPackageComponents = emptyList()
            )
        }
    }

    fun onEditPackageClick(pkg: com.argminres.app.data.local.entity.PackageEntity) {
        viewModelScope.launch {
            val components = dishComponentRepository.getComponentEntities(pkg.id).first().map { it.componentDishId }
            _uiState.update {
                it.copy(
                    showAddEditDialog = true,
                    selectedDish = null,
                    selectedPackage = pkg,
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
                selectedPackage = null,
                selectedPackageComponents = emptyList()
            )
        }
    }

    fun onSaveDish(
        name: String,
        category: String,
        price: Double,
        image: String? = null,
        components: List<Long> = emptyList(),
        isPackage: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (isPackage) {
                    savePackage(name, price, image, components)
                } else {
                    saveDish(name, category, price, image)
                }
                onDialogDismiss()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private suspend fun saveDish(name: String, category: String, price: Double, image: String?) {
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
            auditLogger.logUpdate("DISH", savedId, "Name: $name")
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
            auditLogger.logCreate("DISH", savedId, "Name: $name")
        }
    }

    private suspend fun savePackage(name: String, price: Double, image: String?, components: List<Long>) {
        val existingPackage = _uiState.value.selectedPackage
        val savedId: Long
        
        if (existingPackage != null) {
            savedId = existingPackage.id
            packageRepository.updatePackage(
                existingPackage.copy(
                    name = name,
                    price = price,
                    image = image,
                    updatedAt = System.currentTimeMillis()
                )
            )
            auditLogger.logUpdate("PACKAGE", savedId, "Name: $name")
        } else {
            savedId = packageRepository.insertPackage(
                com.argminres.app.data.local.entity.PackageEntity(
                    name = name,
                    description = "",
                    price = price,
                    image = image
                )
            )
            auditLogger.logCreate("PACKAGE", savedId, "Name: $name")
        }

        // Sync components
        dishComponentRepository.removeAllComponents(savedId)
        components.forEach { compId ->
            dishComponentRepository.addComponent(savedId, compId)
        }
    }

    fun onDeleteDish(dishId: Long) {
        viewModelScope.launch {
            try {
                val dish = _uiState.value.dishes.find { it.dish.id == dishId }?.dish
                if (dish != null) {
                    dish.image?.let { manageProductImageUseCase.deleteImage(it) }
                    dishRepository.deleteDish(dish)
                    auditLogger.logAsync { auditLogger.logDelete("DISH", dishId, "Name: ${dish.name}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDeletePackage(packageId: Long) {
        viewModelScope.launch {
            try {
                val pkg = _uiState.value.packages.find { it.id == packageId }
                if (pkg != null) {
                    pkg.image?.let { manageProductImageUseCase.deleteImage(it) }
                    dishComponentRepository.removeAllComponents(pkg.id)
                    packageRepository.deletePackage(pkg)
                    auditLogger.logAsync { auditLogger.logDelete("PACKAGE", packageId, "Name: ${pkg.name}") }
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
