package com.argminres.app.presentation.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.AuditLogEntity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuditLogViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    if (uiState.selectedEmployerId != null || uiState.selectedAction != null) {
                        IconButton(onClick = { viewModel.clearFilters() }) {
                            Icon(Icons.Default.Clear, "Clear Filters")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No audit logs found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.logs) { log ->
                    AuditLogItem(log)
                }
            }
        }
    }
    
    if (showFilterDialog) {
        FilterDialog(
            currentAction = uiState.selectedAction,
            onDismiss = { showFilterDialog = false },
            onFilterByAction = { action ->
                viewModel.filterByAction(action)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun AuditLogItem(log: AuditLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (log.action) {
                            "LOGIN" -> Icons.Default.Login
                            "LOGOUT" -> Icons.Default.Logout
                            "CREATE" -> Icons.Default.Add
                            "UPDATE" -> Icons.Default.Edit
                            "DELETE" -> Icons.Default.Delete
                            else -> Icons.Default.Info
                        },
                        contentDescription = log.action,
                        tint = when (log.action) {
                            "LOGIN" -> MaterialTheme.colorScheme.primary
                            "LOGOUT" -> MaterialTheme.colorScheme.secondary
                            "CREATE" -> MaterialTheme.colorScheme.tertiary
                            "UPDATE" -> MaterialTheme.colorScheme.primary
                            "DELETE" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.titleMedium,
                        color = when (log.action) {
                            "DELETE" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Text(
                    text = formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = log.employerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (log.entityType.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${log.entityType}${log.entityId?.let { " #$it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            if (!log.details.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentAction: String?,
    onDismiss: () -> Unit,
    onFilterByAction: (String?) -> Unit
) {
    val actions = listOf("All", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Action") },
        text = {
            Column {
                actions.forEach { action ->
                    val actionValue = if (action == "All") null else action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentAction == actionValue,
                            onClick = { onFilterByAction(actionValue) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(action)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
