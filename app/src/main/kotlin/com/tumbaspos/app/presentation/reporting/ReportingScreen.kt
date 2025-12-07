package com.tumbaspos.app.presentation.reporting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tumbaspos.app.domain.model.LowStockProduct
import com.tumbaspos.app.domain.model.SalesSummary
import com.tumbaspos.app.domain.model.TopProduct
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportingScreen(
    viewModel: ReportingViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
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
                    text = { Text("Low Stock") }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when (uiState.selectedTab) {
                0 -> DashboardContent(uiState, currencyFormatter)
                1 -> SalesReportContent(uiState.salesSummary, currencyFormatter)
                2 -> LowStockContent(uiState.lowStockProducts)
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
