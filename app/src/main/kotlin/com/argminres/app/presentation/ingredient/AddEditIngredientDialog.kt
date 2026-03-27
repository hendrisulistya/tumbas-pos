package com.argminres.app.presentation.ingredient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.IngredientEntity
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIngredientDialog(
    ingredient: IngredientEntity?,
    masterIngredients: List<IngredientEntity>,
    successMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double) -> Unit,
    onClearSuccessMessage: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "kg") }
    var stock by remember { mutableStateOf(ingredient?.stock?.toString() ?: "0") }
    var minimumStock by remember { mutableStateOf(ingredient?.minimumStock?.toString() ?: "0") }
    var costPerUnit by remember { mutableStateOf(ingredient?.costPerUnit?.toString() ?: "0") }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedMaster by remember { mutableStateOf(false) }
    var isFromMaster by remember { mutableStateOf(false) }
    var hasAddedSomething by remember { mutableStateOf(false) }
    
    // Reset fields when successMessage changes (for multi-add)
    LaunchedEffect(successMessage) {
        if (successMessage != null && ingredient == null) {
            hasAddedSomething = true
            // We'll clear the fields when they click "Tambah Lagi" or pick a new master
        }
    }
    val canEditNameAndUnit = ingredient == null && !isFromMaster
    
    val units = listOf("kg", "liter", "pcs", "gram", "ml")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ingredient == null) "Add Ingredient" else "Edit Ingredient") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (ingredient == null) {
                    // Selection from Master Data
                    ExposedDropdownMenuBox(
                        expanded = expandedMaster,
                        onExpandedChange = { expandedMaster = it }
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih dari Master Bahan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMaster) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMaster,
                            onDismissRequest = { expandedMaster = false }
                        ) {
                            masterIngredients.forEach { master ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(master.name)
                                            Text(
                                                "${master.unit} - Rp ${master.costPerUnit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        name = master.name
                                        unit = master.unit
                                        costPerUnit = master.costPerUnit.toString()
                                        isFromMaster = true
                                        expandedMaster = false
                                        onClearSuccessMessage()
                                    }
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (canEditNameAndUnit) name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = !canEditNameAndUnit,
                    enabled = canEditNameAndUnit || ingredient != null // Allow non-null for editing, but read-only if it was master-based? 
                    // Actually, if editing existing session item, we should probably keep it read-only for unit/name too.
                )
                
                // Unit Selection
                if (canEditNameAndUnit) {
                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = it }
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Unit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                OutlinedTextField(
                    value = stock,
                    onValueChange = { 
                        stock = it
                        onClearSuccessMessage()
                    },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = minimumStock,
                    onValueChange = { minimumStock = it },
                    label = { Text("Minimum Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost per Unit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (successMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = successMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null && ingredient == null) {
                Button(
                    onClick = {
                        name = ""
                        unit = "kg"
                        stock = "0"
                        costPerUnit = "0"
                        isFromMaster = false
                        onClearSuccessMessage()
                    }
                ) {
                    Text("Tambah Lagi")
                }
            } else {
                Button(
                    onClick = {
                        onSave(
                            name,
                            unit,
                            stock.toDoubleOrNull() ?: 0.0,
                            minimumStock.toDoubleOrNull() ?: 0.0,
                            costPerUnit.toDoubleOrNull() ?: 0.0
                        )
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text(if (ingredient == null) "Tambah" else "Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (hasAddedSomething) "Tutup" else "Batal")
            }
        }
    )
}
