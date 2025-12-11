package com.argminres.app.presentation.scan

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.domain.repository.CartRepository
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val lastScannedBarcode: String? = null,
    val lastScannedProductName: String? = null,
    val isScanning: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

class ScanViewModel(
    private val dishRepository: DishRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private var isProcessing = false

    fun onBarcodeScanned(barcode: String) {
        if (isProcessing) return
        if (barcode == _uiState.value.lastScannedBarcode) return // Prevent duplicate scans of same item immediately

        isProcessing = true
        viewModelScope.launch {
            try {
                val productWithCategory = dishRepository.getDishByBarcode(barcode)
                if (productWithCategory != null) {
                    // Beep sound
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
                    
                    // Add to cart
                    cartRepository.addToCart(productWithCategory.dish, 1)
                    
                    _uiState.update { 
                        it.copy(
                            lastScannedBarcode = barcode,
                            lastScannedProductName = productWithCategory.dish.name,
                            successMessage = "Added ${productWithCategory.dish.name} to cart",
                            error = null
                        ) 
                    }
                    
                    // Reset success message after delay
                    delay(2000)
                    _uiState.update { it.copy(successMessage = null) }
                } else {
                    _uiState.update { 
                        it.copy(
                            lastScannedBarcode = barcode,
                            error = "Product not found: $barcode",
                            successMessage = null
                        ) 
                    }
                    // Reset error message after delay
                    delay(2000)
                    _uiState.update { it.copy(error = null) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        successMessage = null
                    ) 
                }
            } finally {
                isProcessing = false
                // Allow rescanning same item after a short delay
                delay(1000)
                _uiState.update { it.copy(lastScannedBarcode = null) }
            }
        }
    }
    
    fun onResumeScanning() {
        _uiState.update { it.copy(isScanning = true) }
    }
    
    fun onPauseScanning() {
        _uiState.update { it.copy(isScanning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator.release()
    }
}
