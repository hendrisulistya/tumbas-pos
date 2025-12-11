package com.argminres.app.presentation.settings.store

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.StoreSettingsEntity
import com.argminres.app.domain.usecase.dish.ManageDishImageUseCase
import com.argminres.app.domain.usecase.settings.GetStoreSettingsUseCase
import com.argminres.app.domain.usecase.settings.SaveStoreSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreSettingsUiState(
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeTaxId: String = "",
    val logoImage: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class StoreSettingsViewModel(
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase,
    private val saveStoreSettingsUseCase: SaveStoreSettingsUseCase,
    private val manageProductImageUseCase: ManageDishImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSettingsUiState())
    val uiState: StateFlow<StoreSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getStoreSettingsUseCase().collect { settings ->
                if (settings != null) {
                    _uiState.update {
                        it.copy(
                            storeName = settings.storeName,
                            storeAddress = settings.storeAddress,
                            storePhone = settings.storePhone,
                            storeTaxId = settings.storeTaxId,
                            logoImage = settings.logoImage
                        )
                    }
                }
            }
        }
    }

    fun onStoreNameChange(name: String) {
        _uiState.update { it.copy(storeName = name, isSaved = false) }
    }

    fun onStoreAddressChange(address: String) {
        _uiState.update { it.copy(storeAddress = address, isSaved = false) }
    }

    fun onStorePhoneChange(phone: String) {
        _uiState.update { it.copy(storePhone = phone, isSaved = false) }
    }

    fun onStoreTaxIdChange(taxId: String) {
        _uiState.update { it.copy(storeTaxId = taxId, isSaved = false) }
    }

    fun onLogoImageSelected(imageUri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Convert Uri to ByteArray
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageData = inputStream?.readBytes()
                inputStream?.close()
                
                if (imageData != null) {
                    val result = manageProductImageUseCase.uploadImage(imageData)
                    result.onSuccess { base64Image ->
                        _uiState.update { 
                            it.copy(
                                logoImage = base64Image,
                                isLoading = false,
                                isSaved = false
                            )
                        }
                    }.onFailure { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message
                            )
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to read image"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onSaveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val settings = StoreSettingsEntity(
                    id = 1L,
                    storeName = _uiState.value.storeName,
                    storeAddress = _uiState.value.storeAddress,
                    storePhone = _uiState.value.storePhone,
                    storeTaxId = _uiState.value.storeTaxId,
                    logoImage = _uiState.value.logoImage
                )
                saveStoreSettingsUseCase(settings)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSavedStatus() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
