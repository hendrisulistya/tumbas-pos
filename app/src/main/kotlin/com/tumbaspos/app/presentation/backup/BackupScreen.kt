package com.tumbaspos.app.presentation.backup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = viewModel::onBackupClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Database Now")
                }

                Text("Available Backups", style = MaterialTheme.typography.titleMedium)
                
                if (uiState.backups.isEmpty()) {
                    Text("No backups found.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.backups) { fileName ->
                            BackupItem(
                                fileName = fileName,
                                onRestore = { viewModel.onRestoreClick(fileName) },
                                enabled = !uiState.isLoading
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (uiState.error != null) {
                AlertDialog(
                    onDismissRequest = viewModel::onDismissMessage,
                    title = { Text("Error") },
                    text = { Text(uiState.error!!) },
                    confirmButton = {
                        TextButton(onClick = viewModel::onDismissMessage) { Text("OK") }
                    }
                )
            }
            
            if (uiState.successMessage != null) {
                AlertDialog(
                    onDismissRequest = viewModel::onDismissMessage,
                    title = { Text("Success") },
                    text = { Text(uiState.successMessage!!) },
                    confirmButton = {
                        TextButton(onClick = viewModel::onDismissMessage) { Text("OK") }
                    }
                )
            }
        }
    }
}

@Composable
fun BackupItem(
    fileName: String,
    onRestore: () -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fileName, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRestore, enabled = enabled) {
                Icon(Icons.Default.CloudDownload, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restore")
            }
        }
    }
}


