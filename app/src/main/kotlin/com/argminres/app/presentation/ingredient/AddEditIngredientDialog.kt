package com.argminres.app.presentation.ingredient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.IngredientEntity
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIngredientDialog(
    ingredient: IngredientEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "kg") }
    var stock by remember { mutableStateOf(ingredient?.stock?.toString() ?: "0") }
    var minimumStock by remember { mutableStateOf(ingredient?.minimumStock?.toString() ?: "0") }
    var costPerUnit by remember { mutableStateOf(ingredient?.costPerUnit?.toString() ?: "0") }
    var expandedUnit by remember { mutableStateOf(false) }
    
    val units = listOf("kg", "liter", "pcs", "gram", "ml")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ingredient == null) "Add Ingredient" else "Edit Ingredient") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Unit Dropdown
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
                
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
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
            }
        },
        confirmButton = {
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
