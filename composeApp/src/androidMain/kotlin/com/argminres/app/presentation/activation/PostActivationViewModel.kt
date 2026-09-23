package com.argminres.app.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.DatabaseInitializer
import com.argminres.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostActivationUiState(
    val isLoading: Boolean = true,
    val statusMessage: String = "Checking for backups...",
    val showRestoreDialog: Boolean = false,
    val backups: List<String> = emptyList(),
    val error: String? = null,
    val isComplete: Boolean = false
)

class PostActivationViewModel(
    private val databaseInitializer: DatabaseInitializer,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostActivationUiState())
    val uiState: StateFlow<PostActivationUiState> = _uiState.asStateFlow()

    init {
        checkAndPromptRestore()
    }

    private fun checkAndPromptRestore() {
        viewModelScope.launch {
            initializeFreshDatabase()
        }
    }

    fun onRestorePointSelected(fileName: String) {
        // Obsolete with online backup removal
    }

    fun onSkipRestore() {
        _uiState.update { it.copy(showRestoreDialog = false, isLoading = true) }
        viewModelScope.launch {
            initializeFreshDatabase()
        }
    }

    private suspend fun initializeFreshDatabase() {
        try {
            _uiState.update { it.copy(statusMessage = "Initializing database...") }
            databaseInitializer.initializeIfNeeded()
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    isComplete = true,
                    statusMessage = "Ready!"
                ) 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = "Failed to initialize database: ${e.message}"
                ) 
            }
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onRetry() {
        _uiState.update { PostActivationUiState() }
        checkAndPromptRestore()
    }
}
