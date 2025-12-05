package com.tumbaspos.app.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.DatabaseInitializer
import com.tumbaspos.app.domain.model.R2Config
import com.tumbaspos.app.domain.repository.BackupRepository
import com.tumbaspos.app.data.repository.SettingsRepository
import com.tumbaspos.app.domain.usecase.backup.GetBackupsUseCase
import com.tumbaspos.app.domain.usecase.backup.RestoreDatabaseUseCase
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
    private val getBackupsUseCase: GetBackupsUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val settingsRepository: SettingsRepository,
    private val r2Config: R2Config
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostActivationUiState())
    val uiState: StateFlow<PostActivationUiState> = _uiState.asStateFlow()

    init {
        checkAndPromptRestore()
    }

    private fun checkAndPromptRestore() {
        viewModelScope.launch {
            try {
                // Check if backups exist for this App ID
                _uiState.update { it.copy(statusMessage = "Checking for backups...") }
                val backupsResult = getBackupsUseCase(r2Config)
                
                backupsResult.fold(
                    onSuccess = { backupList ->
                        if (backupList.isNotEmpty()) {
                            // Backups exist for this App ID, show restore dialog
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    showRestoreDialog = true, 
                                    backups = backupList
                                ) 
                            }
                        } else {
                            // No backups for this App ID, initialize fresh
                            initializeFreshDatabase()
                        }
                    },
                    onFailure = {
                        // Failed to check backups (maybe network issue), initialize fresh
                        initializeFreshDatabase()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // On any error, initialize fresh
                initializeFreshDatabase()
            }
        }
    }

    fun onRestorePointSelected(fileName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { 
                    it.copy(
                        showRestoreDialog = false,
                        isLoading = true, 
                        statusMessage = "Restoring from backup..."
                    ) 
                }
                val result = restoreDatabaseUseCase(r2Config, fileName)
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isComplete = true,
                                statusMessage = "Restored successfully!"
                            ) 
                        }
                    },
                    onFailure = { error ->
                        // If restore fails, fall back to fresh initialization
                        _uiState.update { 
                            it.copy(statusMessage = "Restore failed, initializing fresh...") 
                        }
                        initializeFreshDatabase()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // If restore fails, fall back to fresh initialization
                initializeFreshDatabase()
            }
        }
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
