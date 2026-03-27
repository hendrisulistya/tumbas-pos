package com.argminres.app.presentation.dishmaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.dao.DishWithCategory
import org.koin.compose.koinInject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
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
    isUploadingImage: Boolean,
    initialComponents: List<Long>,
    allDishes: List<DishWithCategory>,
    onUploadImage: suspend (ByteArray) -> Result<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String?, List<Long>) -> Unit
) {
    val dishRepository: com.argminres.app.domain.repository.DishRepository = koinInject()
    val categories by dishRepository.getAllCategories().collectAsState(initial = emptyList())
    
    var name by remember { mutableStateOf(dish?.dish?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(dish?.dish?.category ?: (categories.find { it.name == "Paket" && dish?.dish?.category == "Paket" }?.name ?: categories.firstOrNull()?.name ?: "")) }
    var price by remember { mutableStateOf(dish?.dish?.price?.toString() ?: "0") }
    var image by remember { mutableStateOf(dish?.dish?.image ?: "") }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedComponents by remember { mutableStateOf(initialComponents) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    onUploadImage(bytes).onSuccess { imageUrl ->
                        image = imageUrl
                    }
                }
            }
        }
    }
    
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Foto Hidangan", style = MaterialTheme.typography.labelLarge)
                        if (image.isNotBlank()) {
                            Text(
                                text = image.substringAfterLast("/"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        } else {
                            Text("Belum ada foto", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Button(
                        onClick = { 
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isUploadingImage,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Pilih Foto")
                        }
                    }
                }

                // Package Components Section
                if (selectedCategory == "Paket") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Komponen Paket", style = MaterialTheme.typography.titleSmall)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5))
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            items(allDishes.filter { it.dish.category != "Paket" }) { componentDish ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedComponents = if (selectedComponents.contains(componentDish.dish.id)) {
                                                selectedComponents - componentDish.dish.id
                                            } else {
                                                selectedComponents + componentDish.dish.id
                                            }
                                        }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedComponents.contains(componentDish.dish.id),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(componentDish.dish.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        selectedCategory,
                        price.toDoubleOrNull() ?: 0.0,
                        image.ifBlank { null },
                        selectedComponents
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
