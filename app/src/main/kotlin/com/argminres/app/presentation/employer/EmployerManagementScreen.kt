package com.argminres.app.presentation.employer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.EmployerEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmployerManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Employees") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddEmployerClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Employee")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.employers) { employer ->
                        EmployerCard(
                            employer = employer,
                            onEdit = { viewModel.onEditEmployerClick(employer) },
                            onDelete = { viewModel.deleteEmployer(employer) }
                        )
                    }
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (uiState.isDialogOpen) {
        EmployerDialog(
            employer = uiState.editingEmployer,
            error = uiState.error,
            onDismiss = viewModel::onDismissDialog,
            onSave = viewModel::onSaveEmployer,
            onClearError = viewModel::clearError
        )
    }
}

@Composable
fun EmployerCard(
    employer: EmployerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { 
                Text(
                    employer.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                ) 
            },
            supportingContent = {
                Column {
                    Text("Phone: ${employer.phoneNumber}")
                    Text(
                        "Role: ${employer.role}",
                        color = if (employer.role == "MANAGER") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                }
            },
            leadingContent = {
                Icon(
                    if (employer.role == "MANAGER") Icons.Default.Star else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (employer.role == "MANAGER") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.secondary
                )
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete, 
                            "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${employer.fullName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmployerDialog(
    employer: EmployerEntity?,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (EmployerEntity) -> Unit,
    onClearError: () -> Unit
) {
    var fullName by remember { mutableStateOf(employer?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(employer?.phoneNumber ?: "") }
    var role by remember { mutableStateOf(employer?.role ?: "CASHIER") }
    var pin by remember { mutableStateOf(employer?.pin ?: "") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employer == null) "Add Employee" else "Edit Employee") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { 
                        fullName = it
                        onClearError()
                    },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        onClearError()
                    },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("MANAGER") },
                            onClick = {
                                role = "MANAGER"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("CASHIER") },
                            onClick = {
                                role = "CASHIER"
                                expanded = false
                            }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            onClearError()
                        }
                    },
                    label = { Text("PIN (4 digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = error != null,
                    supportingText = if (error != null) {
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newEmployer = EmployerEntity(
                        id = employer?.id ?: 0L,
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        role = role,
                        pin = pin
                    )
                    onSave(newEmployer)
                },
                enabled = fullName.isNotBlank() && phoneNumber.isNotBlank() && pin.length == 4
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
