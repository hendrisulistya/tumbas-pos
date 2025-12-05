package com.tumbaspos.app.presentation.product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tumbaspos.app.data.local.entity.ProductEntity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
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
                placeholder = { Text("Search by name, barcode, or category") }
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
                    items(uiState.filteredProducts) { product ->
                        ProductItem(
                            product = product,
                            currencyFormatter = currencyFormatter,
                            onEditClick = { viewModel.onEditProductClick(product) },
                            onDeleteClick = { viewModel.onDeleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isProductDialogOpen) {
        ProductDialog(
            product = uiState.selectedProduct,
            onDismiss = viewModel::onProductDialogDismiss,
            onSave = viewModel::onSaveProduct
        )
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    currencyFormatter: NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                    Text(product.barcode, style = MaterialTheme.typography.bodySmall)
                    if (product.category.isNotBlank()) {
                        Text(
                            product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
                        "Sell: ${currencyFormatter.format(product.price)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Cost: ${currencyFormatter.format(product.costPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun ProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit,
    viewModel: ProductViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var image by remember { mutableStateOf(product?.image) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    
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

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { scannedCode ->
                barcode = scannedCode
                showBarcodeScanner = false
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Image section
                item {
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
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = { showBarcodeScanner = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Selling Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Cost Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProduct = ProductEntity(
                        id = product?.id ?: 0,
                        barcode = barcode,
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                        stock = product?.stock ?: 0,
                        category = category,
                        image = image
                    )
                    onSave(newProduct)
                },
                enabled = name.isNotBlank() && barcode.isNotBlank() && price.isNotBlank() && costPrice.isNotBlank() && !uiState.isUploadingImage
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
    if (image.startsWith("data:image")) {
        // Base64 image
        val base64Data = image.substringAfter("base64,")
        val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Product Image",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        // URL image
        androidx.compose.foundation.layout.Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                Icons.Default.Image,
                contentDescription = "Product Image",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BarcodeScannerDialog(
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hasScanned by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Scan Barcode",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    com.tumbaspos.app.presentation.scan.BarcodeScanner(
                        onBarcodeScanned = { code ->
                            if (!hasScanned) {
                                hasScanned = true
                                onBarcodeScanned(code)
                            }
                        }
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Point camera at barcode",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "The barcode will be detected automatically",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
