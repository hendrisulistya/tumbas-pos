package com.argminres.app.presentation.reporting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.argminres.app.domain.model.LowStockProduct
import com.argminres.app.domain.model.SalesSummary
import com.argminres.app.domain.model.TopProduct
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportingScreen(
    viewModel: ReportingViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEndOfDay: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Dashboard") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Sales") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = { Text("Daily Sessions") }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when (uiState.selectedTab) {
                0 -> DashboardContent(uiState, currencyFormatter)
                1 -> SalesReportContent(uiState.salesSummary, currencyFormatter)
                2 -> DailySessionsContent(uiState.unclosedSessions, onNavigateToEndOfDay)
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: ReportingUiState,
    currencyFormatter: NumberFormat
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Revenue (This Month)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        currencyFormatter.format(uiState.totalRevenue),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text("Top Selling Products", style = MaterialTheme.typography.titleMedium)
        }

        items(uiState.topProducts) { product ->
            ListItem(
                headlineContent = { Text(product.productName) },
                supportingContent = { Text("Sold: ${product.quantitySold}") },
                trailingContent = { Text(currencyFormatter.format(product.totalRevenue)) }
            )
            Divider()
        }
    }
}

@Composable
fun SalesReportContent(
    salesSummary: List<SalesSummary>,
    currencyFormatter: NumberFormat
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Daily Sales (Last 30 Days)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(salesSummary) { summary ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(summary.date, style = MaterialTheme.typography.bodyMedium)
                        Text("${summary.totalTransactions} Transactions", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        currencyFormatter.format(summary.totalSales),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun LowStockContent(
    lowStockProducts: List<LowStockProduct>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Low Stock Alerts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(lowStockProducts) { product ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                ListItem(
                    headlineContent = { Text(product.productName) },
                    supportingContent = { Text("Threshold: ${product.threshold}") },
                    trailingContent = { 
                        Text(
                            "Stock: ${product.currentStock}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun DailySessionsContent(
    unclosedSessions: List<com.argminres.app.data.local.entity.DailySessionEntity>,
    onNavigateToEndOfDay: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID")) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Daily Sessions Management", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "View and manage unclosed business days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (unclosedSessions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "${unclosedSessions.size} Unclosed Session(s)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Please close these sessions to maintain accurate records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        items(unclosedSessions) { session ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                dateFormatter.format(Date(session.sessionDate)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Status: ${session.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (session.status) {
                                    "ACTIVE" -> MaterialTheme.colorScheme.primary
                                    "PENDING_CLOSE" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        
                        if (session.status == "PENDING_CLOSE") {
                            AssistChip(
                                onClick = onNavigateToEndOfDay,
                                label = { Text("Close Now") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        } else {
                            OutlinedButton(onClick = onNavigateToEndOfDay) {
                                Text("Close Day")
                            }
                        }
                    }
                    
                    if (session.totalSales > 0 || session.totalWaste > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sales", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(session.totalSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Column {
                                Text("Waste", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(session.totalWaste),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (unclosedSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "All Sessions Closed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "All business days have been properly closed.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
