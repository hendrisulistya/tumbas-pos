package com.argminres.app.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.DatabaseInitializer
import com.argminres.app.data.repository.SettingsRepository
import com.argminres.app.domain.model.R2Config
import com.argminres.app.domain.usecase.backup.GetBackupsUseCase
import com.argminres.app.domain.usecase.backup.RestoreDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class RestoreStoreUiState(
    val appId: String = "",
    val activationCode: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = false,
    val backups: List<String> = emptyList(),
    val showBackupList: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

class RestoreStoreViewModel(
    private val r2Config: R2Config,
    private val getBackupsUseCase: GetBackupsUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val settingsRepository: SettingsRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreStoreUiState())
    val uiState: StateFlow<RestoreStoreUiState> = _uiState.asStateFlow()

    fun onAppIdChange(appId: String) {
        _uiState.update { it.copy(appId = appId.uppercase(), error = null) }
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
        val oldText = newValue.text
        val oldCursor = newValue.selection.start
        val charsBeforeCursor = oldText.take(oldCursor).count { it != '-' }
        
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
        
        if (newCursor < formatted.length && formatted[newCursor] == '-') {
            newCursor++
        }
        
        val newTextFieldValue = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor.coerceIn(0, formatted.length))
        )
        
        _uiState.update { it.copy(activationCode = newTextFieldValue, error = null) }
    }

    fun onCheckBackups() {
        val appId = _uiState.value.appId.trim()
        val activationCode = _uiState.value.activationCode.text.trim()
        
        if (appId.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an App ID") }
            return
        }
        
        if (activationCode.isBlank()) {
            _uiState.update { it.copy(error = "Please enter activation code") }
            return
        }

        // Verify activation code
        if (!verifyActivation(appId, activationCode)) {
            _uiState.update { it.copy(error = "Invalid activation code for this App ID") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null, showBackupList = false) }
                
                // Check for backups in the specified App ID namespace
                val result = getBackupsUseCase(r2Config, appId)
                
                result.fold(
                    onSuccess = { backupList ->
                        if (backupList.isNotEmpty()) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    backups = backupList,
                                    showBackupList = true
                                ) 
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "No backups found for App ID: $appId"
                                ) 
                            }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to check backups: ${e.message}"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun verifyActivation(appId: String, code: String): Boolean {
        return try {
            val secret = com.argminres.app.core.Secrets.ACTIVATION_SECRET
            val cleanCode = code.replace("-", "")
            
            if (secret.isBlank()) {
                // Fallback for development if secret is not set
                return cleanCode == "123456"
            }
            
            val expectedHmac = calculateHmac(appId, secret)
            cleanCode == expectedHmac
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateHmac(data: String, key: String): String {
        val algorithm = "HmacSHA256"
        val secretKeySpec = javax.crypto.spec.SecretKeySpec(key.toByteArray(), algorithm)
        val mac = javax.crypto.Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        val bytes = mac.doFinal(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.substring(0, 16).uppercase()
    }

    fun onRestoreSelected(fileName: String) {
        viewModelScope.launch {
            try {
                val appId = _uiState.value.appId.trim()
                _uiState.update { 
                    it.copy(
                        isLoading = true,
                        showBackupList = false,
                        error = null
                    ) 
                }
                
                val result = restoreDatabaseUseCase(r2Config, fileName, appId)
                
                result.fold(
                    onSuccess = {
                        // Update App ID and set activated
                        settingsRepository.saveStoreId(appId)
                        settingsRepository.setActivated(true)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isComplete = true
                            ) 
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to restore: ${e.message}"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
