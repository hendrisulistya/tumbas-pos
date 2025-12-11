package com.argminres.app.presentation.sales

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
import com.argminres.app.data.local.entity.CustomerEntity
import com.argminres.app.data.local.dao.SalesOrderWithItems
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun SalesOrderScreen(
    viewModel: SalesOrderViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Orders") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) { // Only show FAB for Customers tab
                FloatingActionButton(
                    onClick = viewModel::onAddCustomerClick
                ) {
                    Icon(Icons.Default.Add, "Add Customer")
                }
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
                    text = { Text("Customers") }
                )
            }

            when (selectedTab) {
                0 -> OrderHistoryList(
                    orders = uiState.orders,
                    currencyFormatter = currencyFormatter,
                    dateFormatter = dateFormatter,
                    onOrderClick = onNavigateToDetail
                )
                1 -> CustomerList(
                    customers = uiState.customers,
                    onEditCustomer = viewModel::onEditCustomerClick
                )
            }
        }
    }

    if (uiState.isAddCustomerDialogOpen) {
        CustomerDialog(
            customer = uiState.selectedCustomer,
            onDismiss = viewModel::onDismissCustomerDialog,
            onSave = viewModel::onSaveCustomer
        )
    }
}



@Composable
fun OrderHistoryList(
    orders: List<SalesOrderWithItems>,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onOrderClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(orders) { orderWithItems ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOrderClick(orderWithItems.order.id) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(orderWithItems.order.orderNumber, style = MaterialTheme.typography.titleMedium)
                        Text(
                            orderWithItems.order.status,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dateFormatter.format(Date(orderWithItems.order.orderDate)), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Show cashier name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Cashier",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Cashier: ${orderWithItems.cashierName ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total: ${currencyFormatter.format(orderWithItems.order.totalAmount)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerList(
    customers: List<CustomerEntity>,
    onEditCustomer: (CustomerEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customers) { customer ->
            Card(
                onClick = { onEditCustomer(customer) },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text(customer.name) },
                    supportingContent = { Text(customer.phone) },
                    trailingContent = { 
                        if (customer.loyaltyPoints > 0) {
                            Text("${customer.loyaltyPoints} pts") 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomerDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CustomerEntity(
                            id = customer?.id ?: 0,
                            name = name,
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
