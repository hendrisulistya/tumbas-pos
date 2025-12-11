package com.argminres.app.presentation.wip

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
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkInProcessScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEndOfDay: () -> Unit,
    viewModel: WorkInProcessViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work in Process") },
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
                
                uiState.unclosedSessions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No Unclosed Sessions",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "All days have been properly closed.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            if (uiState.unclosedSessions.isNotEmpty()) {
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
                                                "${uiState.unclosedSessions.size} Unclosed Session(s)",
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
                        
                        items(uiState.unclosedSessions) { session ->
                            SessionCard(
                                session = session,
                                dateFormatter = dateFormatter,
                                onCloseClick = onNavigateToEndOfDay
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(
    session: com.argminres.app.data.local.entity.DailySessionEntity,
    dateFormatter: SimpleDateFormat,
    onCloseClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                        onClick = onCloseClick,
                        label = { Text("Close Now") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                } else {
                    OutlinedButton(
                        onClick = onCloseClick
                    ) {
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
                        Text(
                            "Sales",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(session.totalSales),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column {
                        Text(
                            "Waste",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(session.totalWaste),
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
