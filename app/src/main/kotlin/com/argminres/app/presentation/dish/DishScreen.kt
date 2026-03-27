package com.argminres.app.presentation.dish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.argminres.app.data.local.entity.DishEntity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: DishViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddProductClick) {
                Icon(Icons.Default.Add, "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search Products") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search by name or category") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.filteredProducts,
                        key = { it.dish.id },
                        contentType = { "product" }
                    ) { productWithCategory ->
                        ProductItem(
                            productWithCategory = productWithCategory,
                            currencyFormatter = currencyFormatter,
                            onEditClick = { viewModel.onEditProductClick(productWithCategory) },
                            onDeleteClick = { viewModel.onDeleteProduct(productWithCategory) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isProductDialogOpen) {
        ProductDialog(
            productWithCategory = uiState.selectedProduct,
            categories = uiState.categories,
            onDismiss = viewModel::onProductDialogDismiss,
            onSave = viewModel::onSaveProduct
        )
    }
}

@Composable
fun ProductItem(
    productWithCategory: com.argminres.app.data.local.dao.DishWithCategory,
    currencyFormatter: NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val product = productWithCategory.dish
    val categoryName = productWithCategory.category?.name ?: "Uncategorized"
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text("ID: ${product.id}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "Stock: ${product.stock}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (product.stock < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Price: ${currencyFormatter.format(product.price)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    productWithCategory: com.argminres.app.data.local.dao.DishWithCategory?,
    categories: List<com.argminres.app.data.local.entity.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (DishEntity) -> Unit,
    viewModel: DishViewModel = koinViewModel()
) {
    val product = productWithCategory?.dish
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var selectedCategory by remember(productWithCategory) { 
        mutableStateOf(productWithCategory?.category) 
    }
    
    // Select first category if nothing is selected and categories are available
    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.firstOrNull()
        }
    }
    
    var image by remember { mutableStateOf(product?.image) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val imageData = inputStream?.readBytes()
                inputStream?.close()
                
                if (imageData != null) {
                    coroutineScope.launch {
                        val result = viewModel.uploadProductImage(imageData)
                        result.onSuccess { url ->
                            image = url
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (image != null) {
                        ProductImageDisplay(
                            image = image!!,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !uiState.isUploadingImage
                            ) {
                                Icon(Icons.Default.Edit, "Change Image", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change")
                            }
                            OutlinedButton(
                                onClick = { image = null },
                                enabled = !uiState.isUploadingImage
                            ) {
                                Icon(Icons.Default.Delete, "Remove Image", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove")
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isUploadingImage
                        ) {
                            if (uiState.isUploadingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, "Add Image")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uiState.isUploadingImage) "Uploading..." else "Add Product Image")
                        }
                    }
                }
                
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Selling Price *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProduct = DishEntity(
                        id = product?.id ?: 0,
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        stock = product?.stock ?: 0,
                        category = selectedCategory?.name ?: "",
                        image = image
                    )
                    onSave(newProduct)
                },
                enabled = name.isNotBlank() && price.isNotBlank() && selectedCategory != null && !uiState.isUploadingImage
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

@Composable
fun ProductImageDisplay(image: String, modifier: Modifier = Modifier) {
    // Optimization: Use Coil's AsyncImage for efficient background decoding and memory management
    val imageData = remember(image) {
        if (image.startsWith("data:image")) {
            try {
                val base64Data = image.substringAfter("base64,")
                android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        } else {
            if (image.startsWith("http") || image.startsWith("content://") || image.startsWith("file://")) {
                image
            } else {
                "file:///android_asset/$image"
            }
        }
    }

    AsyncImage(
        model = imageData,
        contentDescription = "Product Image",
        modifier = modifier,
        contentScale = ContentScale.Crop,
        error = rememberVectorPainter(Icons.Default.BrokenImage),
        placeholder = rememberVectorPainter(Icons.Default.Image)
    )
}
