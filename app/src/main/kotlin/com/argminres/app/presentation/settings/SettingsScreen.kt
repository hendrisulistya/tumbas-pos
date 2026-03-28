package com.argminres.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    onNavigateToStoreSettings: () -> Unit,
    onNavigateToSalesOrder: () -> Unit,
    onNavigateToShowcase: () -> Unit,
    onNavigateToIngredient: () -> Unit,
    onNavigateToIngredientMaster: () -> Unit,
    onNavigateToDishMaster: () -> Unit,
    onNavigateToReporting: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEmployers: () -> Unit = {},
    onNavigateToAuditLog: () -> Unit,
    onNavigateToEndOfDay: () -> Unit,
    onNavigateToWorkInProcess: () -> Unit,
    onChangePinClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val authManager: com.argminres.app.domain.manager.AuthenticationManager = koinInject()
    val currentEmployer by authManager.currentEmployer.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isManager = currentEmployer?.role == "MANAGER"
    val settingsRepository: com.argminres.app.data.repository.SettingsRepository = koinInject()
    val themeMode by settingsRepository.themeMode.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
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
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Pilih Tema") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.argminres.app.data.repository.SettingsRepository.ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepository.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    settingsRepository.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = when (mode) {
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.LIGHT -> "Terang"
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.DARK -> "Gelap"
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.SYSTEM -> "Default sistem"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (mode) {
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.LIGHT -> "Gunakan tema terang"
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.DARK -> "Gunakan tema gelap"
                                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.SYSTEM -> "Ikuti pengaturan sistem"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2)
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxHeight(),
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
                                currentEmployer?.fullName ?: "Belum Masuk",
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
            
            // Appearance Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tampilan",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Tema",
                    subtitle = when (themeMode) {
                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.LIGHT -> "Terang"
                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.DARK -> "Gelap"
                        com.argminres.app.data.repository.SettingsRepository.ThemeMode.SYSTEM -> "Default sistem"
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            
            // --- Manager Only: Management ---
            if (isManager) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Manajemen",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Karyawan",
                        subtitle = "Kelola data karyawan",
                        onClick = onNavigateToEmployers
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.History,
                        title = "Log Audit",
                        subtitle = "Lihat aktivitas sistem",
                        onClick = onNavigateToAuditLog
                    )

                    SettingsItem(
                        icon = Icons.Default.Pending,
                        title = "Pekerjaan Berjalan",
                        subtitle = "Lihat sesi yang belum tutup",
                        onClick = onNavigateToWorkInProcess
                    )

                    SettingsItem(
                        icon = Icons.Default.EventNote,
                        title = "Tutup Hari",
                        subtitle = "Tutup hari dan catat sisa/buangan",
                        onClick = onNavigateToEndOfDay
                    )
                }

                // --- Manager Only: Master Data ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Data Master",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "Kelola Bahan",
                        subtitle = "Kelola katalog bahan baku",
                        onClick = onNavigateToIngredientMaster
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Warehouse,
                        title = "Kelola Etalase",
                        subtitle = "Kelola katalog hidangan",
                        onClick = onNavigateToDishMaster
                    )
                }

                // --- Manager Only: Daily Operations ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Operasional Harian",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Warehouse,
                        title = "Etalase",
                        subtitle = "Kelola stok hidangan",
                        onClick = onNavigateToShowcase
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "Bahan",
                        subtitle = "Kelola bahan baku",
                        onClick = onNavigateToIngredient
                    )
                }
            }

            // --- General Sections (All Users) ---

            // Account Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Akun",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Ganti PIN",
                    subtitle = "Perbarui PIN keamanan Anda",
                    onClick = onChangePinClick
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Receipt,
                    title = "Riwayat Penjualan",
                    subtitle = "Lihat pesanan penjualan",
                    onClick = onNavigateToSalesOrder
                )
            }

            // Reporting & Hardware
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Laporan & Perangkat",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Assessment,
                    title = "Laporan",
                    subtitle = "Lihat laporan analisis",
                    onClick = onNavigateToReporting
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Print,
                    title = "Printer",
                    subtitle = "Kelola printer Bluetooth/USB",
                    onClick = onNavigateToPrinter
                )
            }

            // --- Manager Only: Advanced ---
            if (isManager) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lanjutan",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "Pengaturan Toko",
                        subtitle = "Konfigurasi informasi toko",
                        onClick = onNavigateToStoreSettings
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "Cadangan & Pulihkan",
                        subtitle = "Kelola cadangan data",
                        onClick = onNavigateToBackup
                    )
                }
            }

            // Information Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Informasi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Tentang",
                    subtitle = "Informasi aplikasi",
                    onClick = onNavigateToAbout
                )
            }
        }
        } // end Box
    }
}
