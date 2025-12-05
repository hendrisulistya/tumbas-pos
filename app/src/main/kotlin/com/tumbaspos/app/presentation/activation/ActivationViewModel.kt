package com.tumbaspos.app.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.core.Secrets
import com.tumbaspos.app.data.local.DatabaseInitializer
import com.tumbaspos.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class ActivationUiState(
    val storeId: String = "",
    val activationCode: TextFieldValue = TextFieldValue(""),
    val error: String? = null,
    val isInitializing: Boolean = false,
    val isSuccess: Boolean = false
)

class ActivationViewModel(
    private val settingsRepository: SettingsRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        val appId = settingsRepository.getAppId()
        _uiState.update { it.copy(storeId = appId) }
    }

    fun onActivationCodeChange(newValue: TextFieldValue) {
        // Remove all hyphens and non-alphanumeric characters
        val cleanCode = newValue.text.replace("-", "").filter { it.isLetterOrDigit() }.uppercase()
        
        // Format with hyphens after every 4 characters
        val formatted = buildString {
            cleanCode.take(16).forEachIndexed { index, char ->
                if (index > 0 && index % 4 == 0) {
                    append('-')
                }
                append(char)
            }
        }
        
        // Calculate new cursor position
        // Count how many characters (excluding dashes) are before the cursor
        val oldText = newValue.text
        val oldCursor = newValue.selection.start
        val charsBeforeCursor = oldText.take(oldCursor).count { it != '-' }
        
        // Find the new cursor position in the formatted text
        var newCursor = 0
        var charCount = 0
        for (i in formatted.indices) {
            if (formatted[i] != '-') {
                charCount++
            }
            if (charCount >= charsBeforeCursor) {
                newCursor = i + 1
                break
            }
        }
        
        // Ensure cursor is after any dash at the current position
        if (newCursor < formatted.length && formatted[newCursor] == '-') {
            newCursor++
        }
        
        val newTextFieldValue = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor.coerceIn(0, formatted.length))
        )
        
        _uiState.update { it.copy(activationCode = newTextFieldValue, error = null) }
    }

    fun onActivateClick() {
        val appId = uiState.value.storeId
        val code = uiState.value.activationCode.text.trim()

        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Please enter activation code") }
            return
        }

        if (verifyActivation(appId, code)) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isInitializing = true, error = null) }
                    
                    settingsRepository.saveStoreId(appId)
                    settingsRepository.setActivated(true)
                    
                    _uiState.update { it.copy(isInitializing = false, isSuccess = true) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { 
                        it.copy(
                            isInitializing = false, 
                            error = "Failed to initialize database: ${e.message}"
                        ) 
                    }
                }
            }
        } else {
            _uiState.update { it.copy(error = "Invalid activation code") }
        }
    }

    private fun verifyActivation(appId: String, code: String): Boolean {
        try {
            val secret = Secrets.ACTIVATION_SECRET
            val cleanCode = code.replace("-", "")
            
            if (secret.isBlank()) {
                // Fallback for development if secret is not set
                return cleanCode == "123456" 
            }
            
            val expectedHmac = calculateHmac(appId, secret)
            return cleanCode == expectedHmac
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun calculateHmac(data: String, key: String): String {
        val algorithm = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        val bytes = mac.doFinal(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.substring(0, 16).uppercase()
    }
}
