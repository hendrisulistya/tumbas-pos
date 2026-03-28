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
import androidx.compose.ui.graphics.Color
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
                title = { Text("Laporan", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2)
                ),
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
                    text = { Text("Penjualan") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = { Text("Sesi Harian") }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 800.dp)) {
                    when (uiState.selectedTab) {
                        0 -> DashboardContent(uiState, currencyFormatter)
                        1 -> SalesReportContent(uiState.salesSummary, currencyFormatter)
                        2 -> DailySessionsContent(uiState.unclosedSessions, onNavigateToEndOfDay)
                    }
                }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFBBDEFB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan Bulanan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SummaryRow(
                        label = "Total Pendapatan",
                        value = currencyFormatter.format(uiState.totalRevenue)
                    )
                    SummaryRow(
                        label = "Biaya Bahan",
                        value = "- ${currencyFormatter.format(uiState.totalCost)}",
                        color = MaterialTheme.colorScheme.error
                    )
                    SummaryRow(
                        label = "Nilai Buangan (Waste)",
                        value = "- ${currencyFormatter.format(uiState.totalWaste)}",
                        color = MaterialTheme.colorScheme.error
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val netProfit = uiState.totalRevenue - uiState.totalCost - uiState.totalWaste
                    SummaryRow(
                        label = "Laba Bersih",
                        value = currencyFormatter.format(netProfit),
                        isTotal = true,
                        color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item {
            Text("Produk Terlaris", style = MaterialTheme.typography.titleMedium)
        }

        items(uiState.topProducts) { product ->
            ListItem(
                headlineContent = { Text(product.productName) },
                supportingContent = { Text("Terjual: ${product.quantitySold}") },
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
            Text("Penjualan Harian (30 Hari Terakhir)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(salesSummary) { summary ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFBBDEFB))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(summary.date, style = MaterialTheme.typography.bodyMedium)
                        Text("${summary.totalTransactions} Transaksi", style = MaterialTheme.typography.bodySmall)
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
            Text("Peringatan Stok Rendah", style = MaterialTheme.typography.titleMedium)
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
                    supportingContent = { Text("Ambang Batas: ${product.threshold}") },
                    trailingContent = { 
                        Text(
                            "Stok: ${product.currentStock}",
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
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale("id", "ID")) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Manajemen Sesi Harian", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Lihat dan kelola hari bisnis yang belum ditutup",
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
                                "${unclosedSessions.size} Sesi Belum Ditutup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Harap tutup sesi ini untuk menjaga akurasi catatan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        items(unclosedSessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFBBDEFB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${dateFormatter.format(Date(session.timestampStart))} (${timeFormatter.format(Date(session.timestampStart))})",
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
                                label = { Text("Tutup Sekarang") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        } else {
                            OutlinedButton(onClick = onNavigateToEndOfDay) {
                                Text("Tutup Hari")
                            }
                        }
                    }
                    
                    if (session.totalSales > 0 || (session.totalDishWasteValue + session.totalIngredientWasteValue) > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Penjualan", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(session.totalSales),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Column {
                                Text("Buangan", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(session.totalDishWasteValue + session.totalIngredientWasteValue),
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
                            "Semua Sesi Telah Ditutup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Semua hari bisnis telah ditutup dengan benar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    isTotal: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
