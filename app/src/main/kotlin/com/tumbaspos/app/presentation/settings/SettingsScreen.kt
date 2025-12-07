package com.tumbaspos.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tumbaspos.app.data.repository.SettingsRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            leadingContent = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
fun HorizontalDivider() {
    Divider(color = MaterialTheme.colorScheme.outlineVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToSalesOrder: () -> Unit,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToPurchase: () -> Unit,
    onNavigateToReporting: () -> Unit,
    onNavigateToProduct: () -> Unit,
    onNavigateToAbout: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Management",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.List,
                    title = "Products",
                    subtitle = "Manage products",
                    onClick = onNavigateToProduct
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Receipt,
                    title = "Sales Orders",
                    subtitle = "View sales history",
                    onClick = onNavigateToSalesOrder
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Warehouse,
                    title = "Warehouse",
                    subtitle = "Manage inventory",
                    onClick = onNavigateToWarehouse
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.ShoppingBag,
                    title = "Purchase Orders",
                    subtitle = "Manage purchases",
                    onClick = onNavigateToPurchase
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Assessment,
                    title = "Reporting",
                    subtitle = "View reports",
                    onClick = onNavigateToReporting
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Print,
                    title = "Printer",
                    subtitle = "Manage Bluetooth/USB printers",
                    onClick = onNavigateToPrinter
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.ShoppingBag,
                    title = "Store Settings",
                    subtitle = "Configure store information",
                    onClick = onNavigateToStore
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Backup & Restore",
                    subtitle = "Manage data backups",
                    onClick = onNavigateToBackup
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App information",
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}
