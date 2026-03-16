package com.argminres.app.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.repository.SettingsRepository
import com.argminres.app.domain.usecase.backup.BackupDatabaseUseCase
import com.argminres.app.domain.usecase.backup.GetBackupsUseCase
import com.argminres.app.domain.usecase.backup.RestoreDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupUiState(
    val isLoading: Boolean = false,
    val backups: List<String> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val isConfigDialogOpen: Boolean = false
)

class BackupViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupDatabaseUseCase: BackupDatabaseUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val getBackupsUseCase: GetBackupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadBackups()
    }

    private fun loadBackups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getBackupsUseCase()
            result.fold(
                onSuccess = { backups ->
                    _uiState.update { it.copy(isLoading = false, backups = backups) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load backups: ${e.message}") }
                }
            )
        }
    }

    fun onBackupClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = backupDatabaseUseCase()
            result.fold(
                onSuccess = { fileName ->
                    _uiState.update { it.copy(isLoading = false, successMessage = "Backup successful: $fileName") }
                    loadBackups()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Backup failed: ${e.message}") }
                }
            )
        }
    }

    fun onRestoreClick(fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = restoreDatabaseUseCase(fileName)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Restore successful. Please restart the app.") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Restore failed: ${e.message}") }
                }
            )
        }
    }
    
    fun onDismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
