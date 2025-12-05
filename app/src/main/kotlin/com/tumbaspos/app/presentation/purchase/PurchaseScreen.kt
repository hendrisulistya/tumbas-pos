package com.tumbaspos.app.presentation.purchase

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
import com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems
import com.tumbaspos.app.data.local.entity.SupplierEntity
import com.tumbaspos.app.presentation.warehouse.ProductItem
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Orders") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (selectedTab == 0) viewModel.onCreateOrderClick() 
                    else viewModel.onAddSupplierClick() 
                }
            ) {
                Icon(Icons.Default.Add, if (selectedTab == 0) "New Order" else "New Supplier")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Orders") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Suppliers") }
                )
            }

            when (selectedTab) {
                0 -> PurchaseOrderList(
                    orders = uiState.orders,
                    currencyFormatter = currencyFormatter,
                    onReceiveOrder = viewModel::onReceiveOrder
                )
                1 -> SupplierList(
                    suppliers = uiState.suppliers,
                    onEditSupplier = viewModel::onEditSupplierClick
                )
            }
        }
    }

    if (uiState.isCreateOrderDialogOpen) {
        CreateOrderDialog(
            uiState = uiState,
            onDismiss = viewModel::onDismissCreateOrderDialog,
            onSelectSupplier = viewModel::onSelectSupplier,
            onSearchProduct = viewModel::onProductSearch,
            onAddProduct = viewModel::onAddProductToOrder,
            onUpdateQuantity = viewModel::onUpdateOrderItemQuantity,
            onSubmit = viewModel::onSubmitOrder,
            currencyFormatter = currencyFormatter
        )
    }

    if (uiState.isSupplierDialogOpen) {
        SupplierDialog(
            supplier = uiState.selectedSupplier,
            onDismiss = viewModel::onDismissSupplierDialog,
            onSave = viewModel::onSaveSupplier
        )
    }
}

@Composable
fun PurchaseOrderList(
    orders: List<PurchaseOrderWithItems>,
    currencyFormatter: NumberFormat,
    onReceiveOrder: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(orders) { orderWithItems ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order #${orderWithItems.order.id}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            orderWithItems.order.status,
                            color = if (orderWithItems.order.status == "RECEIVED") 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total: ${currencyFormatter.format(orderWithItems.order.totalAmount)}")
                    Text("Items: ${orderWithItems.items.size}")
                    
                    if (orderWithItems.order.status == "SUBMITTED") {
                        Button(
                            onClick = { onReceiveOrder(orderWithItems.order.id) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Receive Stock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierList(
    suppliers: List<SupplierEntity>,
    onEditSupplier: (SupplierEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suppliers) { supplier ->
            Card(
                onClick = { onEditSupplier(supplier) },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(supplier.name) },
                    supportingContent = { Text(supplier.contactPerson) },
                    trailingContent = { Text(supplier.phone) }
                )
            }
        }
    }
}

@Composable
fun CreateOrderDialog(
    uiState: PurchaseUiState,
    onDismiss: () -> Unit,
    onSelectSupplier: (SupplierEntity) -> Unit,
    onSearchProduct: (String) -> Unit,
    onAddProduct: (com.tumbaspos.app.data.local.dao.ProductWithCategory) -> Unit,
    onUpdateQuantity: (com.tumbaspos.app.data.local.entity.ProductEntity, Int) -> Unit,
    onSubmit: () -> Unit,
    currencyFormatter: NumberFormat
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Purchase Order") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.newOrderSupplier == null) {
                    Text("Select Supplier:")
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(uiState.suppliers) { supplier ->
                            TextButton(
                                onClick = { onSelectSupplier(supplier) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(supplier.name)
                            }
                        }
                    }
                } else {
                    Text("Supplier: ${uiState.newOrderSupplier.name}")
                    Divider()
                    
                    OutlinedTextField(
                        value = uiState.productSearchQuery,
                        onValueChange = onSearchProduct,
                        label = { Text("Search Product to Add") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (uiState.searchResults.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.height(100.dp)) {
                            items(uiState.searchResults) { productWithCategory ->
                                TextButton(
                                    onClick = { onAddProduct(productWithCategory) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(productWithCategory.product.name)
                                }
                            }
                        }
                    }
                    
                    Divider()
                    Text("Order Items:")
                    LazyColumn(modifier = Modifier.height(150.dp)) {
                        items(uiState.newOrderItems) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name)
                                    Text(currencyFormatter.format(item.costPrice))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onUpdateQuantity(item.product, item.quantity - 1) }) {
                                        Icon(Icons.Default.Remove, "Remove")
                                    }
                                    Text("${item.quantity}")
                                    IconButton(onClick = { onUpdateQuantity(item.product, item.quantity + 1) }) {
                                        Icon(Icons.Default.Add, "Add")
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        "Total: ${currencyFormatter.format(uiState.newOrderItems.sumOf { it.subtotal })}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = uiState.newOrderSupplier != null && uiState.newOrderItems.isNotEmpty()
            ) {
                Text("Submit Order")
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
fun SupplierDialog(
    supplier: SupplierEntity?,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var contact by remember { mutableStateOf(supplier?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supplier == null) "Add Supplier" else "Edit Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") })
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SupplierEntity(
                            id = supplier?.id ?: 0,
                            name = name,
                            contactPerson = contact,
                            phone = phone,
                            email = email,
                            address = address
                        )
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
