package com.argminres.app.presentation.ingredientmaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.dao.IngredientWithCategory
import org.koin.compose.koinInject

@Composable
fun IngredientMasterDialog(
    ingredient: IngredientWithCategory?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, Double) -> Unit
) {
    val ingredientRepository: com.argminres.app.domain.repository.IngredientRepository = koinInject()
    val categories by ingredientRepository.getAllCategories().collectAsState(initial = emptyList())
    
    var name by remember { mutableStateOf(ingredient?.ingredient?.name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(ingredient?.ingredient?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)) }
    var unit by remember { mutableStateOf(ingredient?.ingredient?.unit ?: "kg") }
    var costPerUnit by remember { mutableStateOf(ingredient?.ingredient?.costPerUnit?.toString() ?: "0") }
    var expandedCategory by remember { mutableStateOf(false) }
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
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                
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
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Base Cost per Unit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Default cost for this ingredient") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        selectedCategoryId,
                        unit,
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
