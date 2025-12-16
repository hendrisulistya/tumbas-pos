package com.argminres.app.presentation.showcase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.DishEntity
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShowcaseScreen(
    viewModel: ShowcaseViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etalase") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddDishClick
            ) {
                Icon(Icons.Default.Add, "Add Dish")
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
                label = { Text("Cari Hidangan") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) }
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
                    items(uiState.filteredProducts) { productWithCategory ->
                        ProductItem(
                            productWithCategory = productWithCategory,
                            currencyFormatter = currencyFormatter,
                            onStockClick = { viewModel.onStockAdjustmentClick(productWithCategory) }
                        )
                    }
                }
            }
        }
        if (uiState.isStockAdjustmentDialogOpen && uiState.selectedProduct != null) {
            val selectedProduct = uiState.selectedProduct
            selectedProduct?.let {
                StockAdjustmentDialog(
                    product = it.dish,
                    onDismiss = viewModel::onStockAdjustmentDialogDismiss,
                    onConfirm = viewModel::onConfirmStockAdjustment
                )
            }
        }

        if (uiState.isAddDishDialogOpen) {
            AddDishFromMasterDialog(
                masterDishes = uiState.masterDishes,
                onDismiss = viewModel::onAddDishDialogDismiss,
                onConfirm = viewModel::onConfirmAddDish
            )
        }
    }
}

@Composable
fun ProductItem(
    productWithCategory: com.argminres.app.data.local.dao.DishWithCategory,
    currencyFormatter: NumberFormat,
    onStockClick: () -> Unit
) {
    val product = productWithCategory.dish
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
                Text(currencyFormatter.format(product.price))
                
                Row {
                    IconButton(onClick = onStockClick) {
                        Icon(Icons.Default.Inventory, "Adjust Stock")
                    }
                }
            }
        }
    }
}


@Composable
fun StockAdjustmentDialog(
    product: DishEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Stock: ${product.stock}")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isAddition, onClick = { isAddition = true })
                    Text("Add")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isAddition, onClick = { isAddition = false })
                    Text("Remove")
                }
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    val finalQty = if (isAddition) qty else -qty
                    onConfirm(finalQty, reason)
                },
                enabled = quantity.isNotBlank() && reason.isNotBlank()
            ) {
                Text("Confirm")
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
fun AddDishFromMasterDialog(
    masterDishes: List<com.argminres.app.data.local.dao.DishWithCategory>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Int) -> Unit
) {
    var selectedDishId by remember { mutableStateOf<Long?>(null) }
    var stockText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Dish to Etalase") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = masterDishes.find { it.dish.id == selectedDishId }?.dish?.name ?: "Select Dish",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dish from Master") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        masterDishes.forEach { dishWithCat ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(dishWithCat.dish.name)
                                        Text(
                                            dishWithCat.category?.name ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedDishId = dishWithCat.dish.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Initial Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Stock quantity for today") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dishId = selectedDishId
                    val stock = stockText.toIntOrNull()
                    if (dishId != null && stock != null && stock > 0) {
                        onConfirm(dishId, stock)
                    }
                },
                enabled = selectedDishId != null && stockText.toIntOrNull() != null && stockText.toIntOrNull()!! > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
