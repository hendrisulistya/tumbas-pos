package com.tumbaspos.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
import kotlinx.coroutines.launch

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
    onNavigateToStoreSettings: () -> Unit,
    onNavigateToSalesOrder: () -> Unit,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToPurchaseOrder: () -> Unit,
    onNavigateToReporting: () -> Unit,
    onNavigateToProduct: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEmployers: () -> Unit = {},
    onNavigateToAuditLog: () -> Unit,
    onChangePinClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val authManager: com.tumbaspos.app.domain.manager.AuthenticationManager = koinInject()
    val currentEmployer by authManager.currentEmployer.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isManager = currentEmployer?.role == "MANAGER"
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        coroutineScope.launch {
                            authManager.logout()
                            onLogout()
                        }
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Current User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    ListItem(
                        headlineContent = { 
                            Text(
                                currentEmployer?.fullName ?: "Not Logged In",
                                style = MaterialTheme.typography.titleMedium
                            ) 
                        },
                        supportingContent = { 
                            Text(
                                currentEmployer?.role ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Management",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Only show for managers
            if (isManager) {
                item {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Employees",
                        subtitle = "Manage employees",
                        onClick = onNavigateToEmployers
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.History,
                        title = "Audit Log",
                        subtitle = "View activity history",
                        onClick = onNavigateToAuditLog
                    )
                }
            }
            
            // Account Section
            item {
                Text(
                    "Account",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change PIN",
                    subtitle = "Update your security PIN",
                    onClick = onChangePinClick
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.ShoppingBag,
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
                    onClick = onNavigateToPurchaseOrder
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
                    onClick = onNavigateToStoreSettings
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
