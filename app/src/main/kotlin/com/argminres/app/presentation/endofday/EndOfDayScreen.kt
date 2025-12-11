package com.argminres.app.presentation.endofday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndOfDayScreen(
    onNavigateBack: () -> Unit,
    viewModel: EndOfDayViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("End of Day") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                !uiState.hasActiveSession && !uiState.isComplete -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No Active Session",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "There is no active session to close.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                uiState.isComplete -> {
                    EndOfDayCompleteScreen(
                        wasteRecords = uiState.wasteRecords,
                        ingredientUsage = uiState.ingredientUsage,
                        totalWaste = uiState.totalWaste,
                        totalIngredientCost = uiState.totalIngredientCost,
                        totalSales = uiState.totalSales,
                        totalProfit = uiState.totalProfit,
                        currencyFormatter = currencyFormatter,
                        onDone = {
                            viewModel.resetState()
                            onNavigateBack()
                        }
                    )
                }
                
                else -> {
                    EndOfDayIngredientInputScreen(
                        isProcessing = uiState.isProcessing,
                        error = uiState.error,
                        ingredients = uiState.remainingIngredients,
                        onConfirm = { remainingIngredients ->
                            viewModel.processEndOfDay(remainingIngredients)
                        },
                        onCancel = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
fun EndOfDayConfirmScreen(
    isProcessing: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Close Day?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "This will:",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("• Record all unsold dishes as waste")
            Text("• Reset dish stock to zero")
            Text("• Close the current session")
            Text("• Generate daily report")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Close Day")
                }
            }
        }
    }
}

@Composable
fun EndOfDayCompleteScreen(
    wasteRecords: List<com.argminres.app.data.local.entity.WasteRecordEntity>,
    ingredientUsage: List<com.argminres.app.data.local.entity.IngredientUsageEntity>,
    totalWaste: Double,
    totalIngredientCost: Double,
    totalSales: Double,
    totalProfit: Double,
    currencyFormatter: NumberFormat,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Success Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Day Closed Successfully",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Daily Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SummaryRow("Total Sales", currencyFormatter.format(totalSales))
                SummaryRow("Dish Waste", currencyFormatter.format(totalWaste))
                if (totalIngredientCost > 0) {
                    SummaryRow("Ingredient Cost", currencyFormatter.format(totalIngredientCost))
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow(
                    "Net Profit/Loss",
                    currencyFormatter.format(totalProfit),
                    isTotal = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Waste Records
        if (wasteRecords.isNotEmpty()) {
            Text(
                "Waste Records (${wasteRecords.size} items)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wasteRecords) { record ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    record.dishName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Quantity: ${record.quantity}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                currencyFormatter.format(record.totalLoss),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "No waste recorded - all dishes were sold!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ingredient Usage
        if (ingredientUsage.isNotEmpty()) {
            Text(
                "Ingredient Usage (${ingredientUsage.size} items)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(0.5f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredientUsage) { usage ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    usage.ingredientName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row {
                                    Text(
                                        "Used: ${usage.quantityUsed} ${usage.unit}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "• Remaining: ${usage.quantityUsed} ${usage.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                currencyFormatter.format(usage.totalCost),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}
