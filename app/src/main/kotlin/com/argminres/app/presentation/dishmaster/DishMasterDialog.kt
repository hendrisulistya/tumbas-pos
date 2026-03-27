package com.argminres.app.presentation.dishmaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.dao.DishWithCategory
import org.koin.compose.koinInject
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.argminres.app.presentation.dish.ProductImageDisplay

@Composable
fun DishMasterDialog(
    dish: DishWithCategory?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String?) -> Unit
) {
    val dishRepository: com.argminres.app.domain.repository.DishRepository = koinInject()
    val categories by dishRepository.getAllCategories().collectAsState(initial = emptyList())
    
    var name by remember { mutableStateOf(dish?.dish?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(dish?.dish?.category ?: (categories.firstOrNull()?.name ?: "")) }
    var price by remember { mutableStateOf(dish?.dish?.price?.toString() ?: "0") }
    var image by remember { mutableStateOf(dish?.dish?.image ?: "") }
    var expandedCategory by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dish == null) "Add Dish" else "Edit Dish") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image Preview
                if (image.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        ProductImageDisplay(
                            image = image,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

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
                        value = if (selectedCategory.isBlank()) "Select Category" else selectedCategory,
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
                                    selectedCategory = category.name
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = image,
                    onValueChange = { image = it },
                    label = { Text("Image Path/URL/Base64") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. dish_image/1001.png") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        selectedCategory,
                        price.toDoubleOrNull() ?: 0.0,
                        image.ifBlank { null }
                    )
                },
                enabled = name.isNotBlank() && selectedCategory.isNotBlank()
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
