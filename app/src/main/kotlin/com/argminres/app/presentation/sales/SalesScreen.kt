package com.argminres.app.presentation.sales

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.CustomerEntity
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

private val Blue600 = Color(0xFF1976D2)
private val Blue50  = Color(0xFFE3F2FD)
private val Blue100 = Color(0xFFBBDEFB)
private val glass   = Color(0xFF_E3F2FD.toInt()) // unused, kept for compat

@Composable
fun SalesScreen(
    viewModel: SalesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Show error message
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (uiState.orderCompleted && uiState.lastInvoice != null) {
        InvoiceDialog(
            invoiceText = uiState.lastInvoice!!,
            pdfPath = uiState.lastPdfPath,
            onDismiss = viewModel::resetOrder,
            onPrint = viewModel::printReceipt
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Sales", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue600
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        // Tablet landscape: two-column split
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Left: Cart Items (60%) ─────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                if (uiState.cart.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Blue100
                            )
                            Text(
                                "Your cart is empty",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFF9E9E9E)
                            )
                            Text(
                                "Add products from the Home screen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(onClick = onNavigateBack) {
                                Text("Browse Products")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.cart) { item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { viewModel.updateQuantity(item.id, item.isPackage, item.quantity + 1) },
                                onDecrease = { viewModel.updateQuantity(item.id, item.isPackage, item.quantity - 1) },
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }

            VerticalDivider(color = Color(0xFF2A2A4A))

            // ── Right: Payment Panel (40%) ─────────────────────────────
            var showCustomerDialog by remember { mutableStateOf(false) }

            if (showCustomerDialog) {
                CustomerSelectionDialog(
                    customers = uiState.customers,
                    selectedCustomer = uiState.selectedCustomer,
                    onDismiss = { showCustomerDialog = false },
                    onCustomerSelected = { customer ->
                        viewModel.selectCustomer(customer)
                        showCustomerDialog = false
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Order Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Customer Selection Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Blue50)
                        .border(1.dp, Blue100, RoundedCornerShape(12.dp))
                        .then(Modifier.clickable { showCustomerDialog = true })
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Person, null, tint = Blue600)
                            Column {
                                Text(
                                    "Customer",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF616161)
                                )
                                Text(
                                    uiState.selectedCustomer?.name ?: "Select Customer",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.selectedCustomer != null) Color.Black else Color(0xFF9E9E9E)
                                )
                            }
                        }
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF9E9E9E))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    Text(
                        currencyFormatter.format(uiState.totalAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Blue600,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Gradient Checkout Button
                val canCheckout = uiState.cart.isNotEmpty() && !uiState.isLoading && uiState.selectedCustomer != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canCheckout) Blue600 else Color(0xFFE0E0E0)
                        )
                        .then(if (canCheckout) Modifier.clickable { viewModel.checkout() } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            "Checkout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (canCheckout) Color.White else Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
    } // close aurora Box
}

@Composable
fun CustomerSelectionDialog(
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onCustomerSelected: (CustomerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    // Filter customers based on search query
    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter { customer ->
                customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery, ignoreCase = true) ||
                customer.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Auto-focus search field when dialog opens
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore focus errors
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Customer") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customers...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
                
                HorizontalDivider()
                
                // Customer list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCustomers.size) { index ->
                        val customer = filteredCustomers[index]
                        Card(
                            onClick = { onCustomerSelected(customer) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (customer.id == selectedCustomer?.id) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (customer.id == selectedCustomer?.id)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Column {
                                        Text(
                                            text = customer.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (customer.id == selectedCustomer?.id) 
                                                FontWeight.Bold 
                                            else 
                                                FontWeight.Normal
                                        )
                                        if (customer.phone != "-") {
                                            Text(
                                                text = customer.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (customer.id == selectedCustomer?.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    if (filteredCustomers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No customers found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun InvoiceDialog(
    invoiceText: String,
    pdfPath: String? = null,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Payment Successful!")
        },
        text = {
            if (pdfPath != null) {
                // Render PDF
                PdfPreview(pdfPath = pdfPath)
            } else {
                // Fallback to text if PDF not available
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = invoiceText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPrint()
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Print & Close")
            }
        },
        dismissButton = {}
    )
}

@Composable
fun PdfPreview(pdfPath: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(pdfPath) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Get the PDF file from MediaStore URI or file path
                val file = if (pdfPath.startsWith("content://")) {
                    // MediaStore URI - need to copy to temp file
                    val tempFile = java.io.File(context.cacheDir, "temp_receipt.pdf")
                    context.contentResolver.openInputStream(android.net.Uri.parse(pdfPath))?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } else {
                    // Direct file path
                    java.io.File(pdfPath)
                }
                
                if (file.exists()) {
                    val fileDescriptor = android.os.ParcelFileDescriptor.open(
                        file,
                        android.os.ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    
                    val pdfRenderer = android.graphics.pdf.PdfRenderer(fileDescriptor)
                    val page = pdfRenderer.openPage(0)
                    
                    // Create bitmap with page dimensions
                    val bmp = android.graphics.Bitmap.createBitmap(
                        page.width * 2, // Scale up for better quality
                        page.height * 2,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    
                    // Render page to bitmap
                    page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    bitmap = bmp
                    
                    page.close()
                    pdfRenderer.close()
                    fileDescriptor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Receipt PDF",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: run {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    currencyFormatter: NumberFormat
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Blue50)
            .border(1.dp, Blue100, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    currencyFormatter.format(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8080B0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    currencyFormatter.format(item.subtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Blue600
                )
            }
            
            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(Blue50, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove, 
                        "Decrease",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF424242)
                    )
                }
                Text(
                    item.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.Black
                )
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        "Increase",
                        modifier = Modifier.size(18.dp),
                        tint = Blue600
                    )
                }
            }
        }
    }
}
