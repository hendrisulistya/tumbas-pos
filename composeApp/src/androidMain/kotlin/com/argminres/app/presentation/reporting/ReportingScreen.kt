package com.argminres.app.presentation.reporting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportingScreen(
    viewModel: ReportingViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEndOfDay: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
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
                actions = {
                    IconButton(onClick = viewModel::onDownloadIconClick) {
                        Icon(Icons.Default.Download, "Download Report", tint = androidx.compose.ui.graphics.Color.White)
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
                    text = { Text("Statistik") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = { Text("Riwayat Sesi") }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Date Range Picker (Simplified for now)
            DateRangeHeader(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateRangeSelected = { start, end -> viewModel.setDateRange(start, end) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 1000.dp)) {
                    when (uiState.selectedTab) {
                        0 -> DashboardContent(uiState, currencyFormatter)
                        1 -> AggregatedUsageContent(uiState.aggregatedUsage, uiState.cashierDishSales, currencyFormatter)
                        2 -> SessionHistoryContent(
                            uiState.allSessions, 
                            onSessionClick = { viewModel.selectSession(it.id) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog for Session
    uiState.selectedSessionDetails?.let { details ->
        SessionDetailDialog(
            details = details,
            onDismiss = { viewModel.dismissSessionDetails() },
            currencyFormatter = currencyFormatter
        )
    }

    // Download Type Selection Dialog
    if (uiState.isDownloadTypeDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDownloadDialogs,
            title = { Text("Pilih Jenis Laporan") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportTypeItem("Harian", "Berdasarkan sesi yang sudah ditutup", onClick = { viewModel.onReportTypeSelected("Harian") })
                    ReportTypeItem("Mingguan", "Rangkuman penjualan per minggu", onClick = { viewModel.onReportTypeSelected("Mingguan") })
                    ReportTypeItem("Bulanan", "Rangkuman penjualan per bulan", onClick = { viewModel.onReportTypeSelected("Bulanan") })
                    ReportTypeItem("Tahunan", "Rangkuman penjualan per tahun", onClick = { viewModel.onReportTypeSelected("Tahunan") })
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissDownloadDialogs) { Text("Batal") }
            }
        )
    }

    // Period Selection Dialog
    if (uiState.isPeriodSelectionDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDownloadDialogs,
            title = { Text("Pilih Periode ${uiState.downloadReportType}") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (uiState.availablePeriods.isEmpty()) {
                        Text("Tidak ada data tersedia untuk periode ini.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn {
                            items(uiState.availablePeriods) { period ->
                                ListItem(
                                    headlineContent = { Text(period.label) },
                                    modifier = Modifier.clickable { viewModel.onPeriodSelected(period) }
                                )
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissDownloadDialogs) { Text("Batal") }
            }
        )
    }

    // PDF Generation Loading Overlay
    if (uiState.isGeneratingPdf) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Menyusun Laporan PDF...")
                }
            }
        }
    }
}

@Composable
fun ReportTypeItem(title: String, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DateRangeHeader(
    startDate: Long,
    endDate: Long,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    
    // Quick Range Options
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        AssistChip(
            onClick = {
                val cal = Calendar.getInstance()
                val end = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onDateRangeSelected(cal.timeInMillis, end)
            },
            label = { Text("Hari") }
        )
        AssistChip(
            onClick = {
                val cal = Calendar.getInstance()
                val end = cal.timeInMillis
                // Set to start of week (Sunday or Monday depending on locale)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onDateRangeSelected(cal.timeInMillis, end)
            },
            label = { Text("Minggu") }
        )
        AssistChip(
            onClick = {
                val cal = Calendar.getInstance()
                val end = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onDateRangeSelected(cal.timeInMillis, end)
            },
            label = { Text("Bulan") }
        )
        AssistChip(
            onClick = {
                val cal = Calendar.getInstance()
                val end = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onDateRangeSelected(cal.timeInMillis, end)
            },
            label = { Text("Tahun") }
        )
    }
    
    // Date Range Display
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            "${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan Operasional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SummaryRow("Total Pendapatan", currencyFormatter.format(uiState.totalRevenue))
                    SummaryRow("Biaya Bahan", "- ${currencyFormatter.format(uiState.totalCost)}", color = MaterialTheme.colorScheme.error)
                    SummaryRow("Nilai Buangan", "- ${currencyFormatter.format(uiState.totalWaste)}", color = MaterialTheme.colorScheme.error)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val netProfit = uiState.totalRevenue - uiState.totalCost - uiState.totalWaste
                    SummaryRow(
                        "Laba Bersih",
                        currencyFormatter.format(netProfit),
                        isTotal = true,
                        color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        item {
            Text("Produk Terlaris", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
fun AggregatedUsageContent(
    state: AggregatedUsageState?,
    cashierPerformance: List<com.argminres.app.domain.model.CashierDishSales>,
    currencyFormatter: NumberFormat
) {
    if (state == null) return

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ringkasan Penggunaan Bahan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        items(state.ingredientUsage) { usage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(usage.ingredientName, fontWeight = FontWeight.Bold)
                        Text(currencyFormatter.format(usage.totalCost), color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        "Awal: ${usage.startingQuantity} ${usage.unit} | Sisa: ${usage.remainingQuantity} ${usage.unit} | Terpakai: ${usage.quantityUsed} ${usage.unit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ringkasan Statistik Hidangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(state.dishUsage) { dish ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(dish.dishName, fontWeight = FontWeight.Bold)
                    Text(
                        "Produksi: ${dish.producedQuantity} | Terjual: ${dish.soldQuantity} | Sisa: ${dish.remainingQuantity} | Buang: ${dish.wasteQuantity}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    val breakdown = cashierPerformance.filter { it.dishName == dish.dishName }
                    if (breakdown.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                        Text("Penjualan per Kasir:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        breakdown.forEach { bp ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(bp.cashierName, style = MaterialTheme.typography.bodySmall)
                                Text("${bp.quantitySold} porsi", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHistoryContent(
    sessions: List<com.argminres.app.data.local.entity.DailySessionEntity>,
    onSessionClick: (com.argminres.app.data.local.entity.DailySessionEntity) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions.sortedByDescending { it.timestampStart }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSessionClick(session) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dateFormatter.format(Date(session.timestampStart)), fontWeight = FontWeight.Bold)
                        StatusBadge(session.status)
                    }
                    if (session.status == "CLOSED") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Penjualan", style = MaterialTheme.typography.bodySmall)
                                Text(currencyFormatter.format(session.totalSales))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Profit", style = MaterialTheme.typography.bodySmall)
                                Text(currencyFormatter.format(session.totalProfit), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    } else {
                        Text("Sesi Masih Berjalan", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "CLOSED" -> Color(0xFF2E7D32)
        "ACTIVE" -> Color(0xFF1976D2)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun SessionDetailDialog(
    details: SessionDetailState,
    onDismiss: () -> Unit,
    currencyFormatter: NumberFormat
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Sesi #${details.sessionId}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("Bahan Baku", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Divider(Modifier.padding(vertical = 4.dp))
                }
                items(details.ingredientUsage) { usage ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(usage.ingredientName, style = MaterialTheme.typography.bodyMedium)
                            Text(currencyFormatter.format(usage.totalCost), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "Awal: ${usage.startingQuantity} | Sisa: ${usage.remainingQuantity} | Pakai: ${usage.quantityUsed} ${usage.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Hidangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Divider(Modifier.padding(vertical = 4.dp))
                }
                items(details.dishUsage) { dish ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(dish.dishName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Prod: ${dish.producedQuantity} | Jual: ${dish.soldQuantity} | Sisa: ${dish.remainingQuantity} | Buang: ${dish.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
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
