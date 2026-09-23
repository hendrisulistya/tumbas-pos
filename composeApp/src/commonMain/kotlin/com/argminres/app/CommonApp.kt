package com.argminres.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.argminres.app.core.PlatformConfig
import com.argminres.app.core.Screen
import com.argminres.app.presentation.auth.CommonLoginScreen
import com.argminres.app.ui.theme.*
import com.argminres.app.util.formatRupiah
import org.jetbrains.compose.resources.painterResource
import com.argminres.app.generated.resources.Res
import com.argminres.app.generated.resources.logo
import com.argminres.app.ui.components.DishImage

// ── Models ──────────────────────────────────────────────────────────────────
data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val cost: Long,
    var stock: Int,
    val image: String = "",
    val emoji: String = "🍲",
    val unit: String = "Porsi"
)

data class IngredientItem(
    val id: String,
    val name: String,
    val unit: String,
    var stock: Double,
    val minStock: Double,
    val costPerUnit: Long
)

data class PosCartItem(
    val item: MenuItem,
    var quantity: Int
)

data class OrderRecord(
    val id: String,
    val time: String,
    val cashier: String,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String,
    val itemsSummary: String
)

data class Employee(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val status: String,
    val pin: String = "1234"
)

data class AuditEntry(
    val time: String,
    val user: String,
    val action: String,
    val details: String
)

// ── Metro Tile Models ────────────────────────────────────────────────────────
data class MetroTile(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null
)

data class MetroCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val tiles: List<MetroTile>
)

@Composable
fun CommonApp() {
    TambooPosTheme {

        var loggedInEmployee by remember { mutableStateOf<Employee?>(null) }
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        var showMetroMenu by remember { mutableStateOf(false) }

        // Master menu data dari dishes.csv & dish_package.json
        val menuList = remember {
            mutableStateListOf(
                MenuItem("1001", "Rendang Daging", "Makanan", 35000, 22000, 35, "dish_image/1001.png", "🥩"),
                MenuItem("1002", "Gulai Ayam", "Makanan", 28000, 18000, 28, "dish_image/1002.png", "🍗"),
                MenuItem("1003", "Gulai Ikan", "Makanan", 32000, 20000, 20, "dish_image/1003.png", "🐟"),
                MenuItem("1004", "Ayam Pop", "Makanan", 30000, 19000, 30, "dish_image/1004.png", "🍗"),
                MenuItem("1005", "Dendeng Balado", "Makanan", 38000, 24000, 22, "dish_image/1005.png", "🥓"),
                MenuItem("1006", "Gulai Otak", "Makanan", 25000, 15000, 15, "dish_image/1006.png", "🍲"),
                MenuItem("1007", "Gulai Limpa", "Makanan", 24000, 14000, 18, "dish_image/1007.png", "🥣"),
                MenuItem("1008", "Gulai Usus", "Makanan", 24000, 14000, 20, "dish_image/1008.png", "🥣"),
                MenuItem("1009", "Terong Balado", "Makanan", 14000, 8000, 25, "dish_image/1009.png", "🍆"),
                MenuItem("1010", "Kacang Panjang Balado", "Makanan", 14000, 7000, 25, "dish_image/1010.png", "🥗"),
                MenuItem("1011", "Nasi Putih", "Makanan", 5000, 2000, 100, "dish_image/1011.png", "🍚"),
                MenuItem("1012", "Nasi Kuning", "Makanan", 8000, 3500, 40, "dish_image/1012.png", "🍚"),
                MenuItem("1013", "Sambal Ijo", "Makanan", 5000, 1500, 50, "dish_image/1013.png", "🌶️"),
                MenuItem("1014", "Sambal Merah", "Makanan", 5000, 1500, 50, "dish_image/1014.png", "🌶️"),
                MenuItem("1015", "Sambal Balado", "Makanan", 5000, 1500, 50, "dish_image/1015.png", "🌶️"),
                MenuItem("1016", "Es Teh Manis", "Minuman", 5000, 1200, 80, "dish_image/1016.png", "🧋"),
                MenuItem("1017", "Es Jeruk", "Minuman", 8000, 3000, 40, "dish_image/1017.png", "🍊"),
                MenuItem("1018", "Teh Tawar Panas", "Minuman", 3000, 800, 60, "dish_image/1018.png", "🍵"),
                MenuItem("1019", "Kopi Hitam", "Minuman", 8000, 3000, 35, "dish_image/1019.png", "☕"),
                MenuItem("1020", "Air Mineral", "Minuman", 3000, 1500, 90, "dish_image/1020.png", "💧"),
                MenuItem("1021", "Kerupuk Jangek", "Lain-lain", 8000, 4000, 45, "dish_image/1021.png", "🍘"),
                MenuItem("1022", "Kerupuk Sanjai", "Lain-lain", 10000, 5000, 30, "dish_image/1022.png", "🍘"),
                MenuItem("1023", "Perkedel Kentang", "Lain-lain", 8000, 3500, 35, "dish_image/1023.png", "🥔"),
                MenuItem("1024", "Perkedel Jagung", "Lain-lain", 8000, 3500, 35, "dish_image/1024.png", "🌽"),
                MenuItem("1025", "Paket Komplit", "Paket", 45000, 27000, 25, "dish_image/1025.png", "🍱"),
                MenuItem("1026", "Paket Berdua", "Paket", 85000, 52000, 15, "dish_image/1026.png", "🍱"),
                MenuItem("1027", "Paket Keluarga", "Paket", 165000, 100000, 10, "dish_image/1027.png", "🍱"),
                MenuItem("1028", "Paket Hemat", "Paket", 32000, 19000, 30, "dish_image/1028.png", "🍱")
            )
        }

        val ingredientList = remember {
            mutableStateListOf(
                IngredientItem("1", "Daging Sapi Has Luar", "Kg", 24.5, 10.0, 130000),
                IngredientItem("2", "Ayam Segar Potong", "Ekor", 30.0, 15.0, 38000),
                IngredientItem("3", "Telur Ayam Negeri", "Kg", 18.0, 5.0, 28000),
                IngredientItem("4", "Santan Kelapa Murni", "Liter", 20.0, 8.0, 16000),
                IngredientItem("5", "Beras Solok Premium", "Karung", 6.0, 2.0, 310000),
                IngredientItem("6", "Cabai Merah Keriting", "Kg", 8.0, 3.0, 45000),
                IngredientItem("7", "Cabai Rawit Hijau", "Kg", 5.0, 2.0, 50000),
                IngredientItem("8", "Bawang Merah & Putih", "Kg", 12.0, 4.0, 36000)
            )
        }

        val orderList = remember {
            mutableStateListOf(
                OrderRecord("#ORD-1001", "12:15", "Rahmat Hidayat", 76000, "QRIS", "Selesai", "2x Rendang, 1x Teh Talua, 1x Es Jeruk"),
                OrderRecord("#ORD-1002", "12:40", "Rahmat Hidayat", 44000, "Cash", "Selesai", "1x Ayam Pop, 1x Telur Dadar, 1x Es Teh"),
                OrderRecord("#ORD-1003", "13:05", "Siti Rahma", 112000, "Cash", "Selesai", "3x Gulai Tunjang, 2x Perkedel, 2x Es Jeruk"),
                OrderRecord("#ORD-1004", "13:30", "Siti Rahma", 53000, "QRIS", "Selesai", "2x Dendeng Batokok, 1x Sambal Ijo")
            )
        }

        val employeeList = remember {
            mutableStateListOf(
                Employee("EMP-01", "Budi Santoso", "MANAGER", "081234567890", "Aktif", pin = "0000"),
                Employee("EMP-02", "Rahmat Hidayat", "CASHIER", "081398765432", "Aktif", pin = "1234"),
                Employee("EMP-03", "Siti Rahma", "CASHIER", "082156781234", "Aktif", pin = "1234"),
                Employee("EMP-04", "Ahmad Fauzi", "KOKI", "085211223344", "Aktif", pin = "1234")
            )
        }

        val auditList = remember {
            mutableStateListOf(
                AuditEntry("13:35", "Rahmat (Kasir)", "TRANSAKSI_SELESAI", "Mencatat transaksi #ORD-1004 Rp 53.000 (QRIS)"),
                AuditEntry("12:00", "Budi (Manager)", "BUKA_SESI", "Membuka sesi kasir harian dengan modal awal Rp 300.000"),
                AuditEntry("11:30", "Budi (Manager)", "UPDATE_STOK", "Menambah stok etalase Rendang Daging +15 porsi"),
                AuditEntry("08:00", "Sistem", "APLIKASI_DIMULAI", "Aplikasi POS dijalankan pada platform ${PlatformConfig.platformName}")
            )
        }

        val cart = remember { mutableStateListOf<PosCartItem>() }

        val currentScreenTitle = when (currentScreen) {
            Screen.Home -> "Kasir (POS)"
            Screen.SalesOrder -> "Pesanan Penjualan"
            Screen.SessionCheck -> "Sesi Kasir"
            Screen.Showcase -> "Etalase Saji"
            Screen.Ingredient -> "Bahan Baku"
            Screen.EndOfDay -> "Tutup Hari"
            Screen.DishMaster -> "Kelola Menu"
            Screen.IngredientMaster -> "Kelola Bahan"
            Screen.EmployerManagement -> "Karyawan"
            Screen.Reporting -> "Laporan Penjualan"
            Screen.WorkInProcess -> "Pekerjaan Berjalan"
            Screen.AuditLog -> "Log Audit"
            Screen.Settings -> "Pengaturan Toko"
            else -> "Kasir (POS)"
        }

        // ── GERBANG AUTENTIKASI: Jika belum ada karyawan login, tampilkan LoginScreen ──
        if (loggedInEmployee == null) {
            CommonLoginScreen(
                employees = employeeList,
                onLoginSuccess = { emp ->
                    loggedInEmployee = emp
                    currentScreen = Screen.Home
                    auditList.add(0, AuditEntry("Sekarang", "${emp.name} (${emp.role})", "LOGIN", "Berhasil login dengan autentikasi PIN"))
                }
            )
        } else {
            val activeEmployee = loggedInEmployee!!

            // FULL SCREEN ROOT CONTAINER (NO FIXED SIDEBAR - OPTIMIZED FOR TABLET)
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // TOP BAR WITH HAMBURGER BUTTON
                    Surface(
                        color = White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Hamburger Menu Button + Branding & Current Screen
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { showMetroMenu = !showMetroMenu },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (showMetroMenu) Blue700 else Blue50)
                                ) {
                                    Icon(
                                        imageVector = if (showMetroMenu) Icons.Default.Close else Icons.Default.Menu,
                                        contentDescription = "Buka Menu Metro",
                                        tint = if (showMetroMenu) White else Blue700,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(Res.drawable.logo),
                                            contentDescription = "tambooPOS Logo",
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "tambooPOS",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Cyan900
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (PlatformConfig.requiresActivation) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = if (PlatformConfig.requiresActivation) "Mobile (Aktivasi)" else "Web (Bypass)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (PlatformConfig.requiresActivation) Color(0xFFE65100) else Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "RM Padang",
                                            fontSize = 12.sp,
                                            color = Gray600
                                        )
                                        Text(
                                            text = "  ❯  ",
                                            fontSize = 11.sp,
                                            color = Gray400
                                        )
                                        Text(
                                            text = currentScreenTitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Blue700
                                        )
                                    }
                                }
                            }

                            // Right: Session Status, Logged-in Employee Info & Logout Button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Success))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Sesi: Aktif", fontSize = 12.sp, color = Success, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Blue100,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeEmployee.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Blue700
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(activeEmployee.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(activeEmployee.role, fontSize = 10.sp, color = Gray600)
                                }
                                Spacer(Modifier.width(10.dp))
                                // Logout / Ganti Karyawan Button
                                IconButton(
                                    onClick = {
                                        auditList.add(0, AuditEntry("Sekarang", "${activeEmployee.name} (${activeEmployee.role})", "LOGOUT", "Keluar dari sesi POS"))
                                        loggedInEmployee = null
                                        showMetroMenu = false
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEBEE))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Keluar / Ganti Akun",
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // SCREEN CONTENT AREA (FULL TABLET WIDTH)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(14.dp)) {
                        when (currentScreen) {
                            Screen.Home -> PosCashierScreen(menuList, cart, onOrderPaid = { ord -> orderList.add(0, ord.copy(cashier = activeEmployee.name)) })
                            Screen.SalesOrder -> SalesOrderHistoryScreen(orderList)
                            Screen.Showcase -> ShowcaseStockScreen(menuList)
                            Screen.DishMaster -> DishMasterManagementScreen(menuList)
                            Screen.Ingredient -> IngredientStockScreen(ingredientList)
                            Screen.IngredientMaster -> IngredientMasterManagementScreen(ingredientList)
                            Screen.SessionCheck -> SessionControlScreen()
                            Screen.EndOfDay -> EndOfDayScreen(menuList, orderList)
                            Screen.EmployerManagement -> EmployerListScreen(employeeList)
                            Screen.Reporting -> ReportingOverviewScreen(orderList)
                            Screen.WorkInProcess -> WorkInProcessOverviewScreen(menuList, orderList)
                            Screen.AuditLog -> AuditLogOverviewScreen(auditList)
                            Screen.Settings -> SettingsOverviewScreen()
                            else -> PosCashierScreen(menuList, cart, onOrderPaid = { ord -> orderList.add(0, ord.copy(cashier = activeEmployee.name)) })
                        }
                    }
                }

                // METRO TILE NAVIGATION OVERLAY
                if (showMetroMenu) {
                    MetroTileMenuOverlay(
                        currentScreen = currentScreen,
                        onSelectScreen = { screen ->
                            currentScreen = screen
                            showMetroMenu = false
                        },
                        onDismiss = { showMetroMenu = false }
                    )
                }
            }
        }
    }
}

// ── Metro Tile Menu Overlay Component ────────────────────────────────────────
@Composable
fun MetroTileMenuOverlay(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    onDismiss: () -> Unit
) {
    val metroCategories = remember {
        listOf(
            MetroCategory(
                name = "Operasional",
                icon = Icons.Default.PointOfSale,
                color = Color(0xFFD97706),
                tiles = listOf(
                    MetroTile(Screen.Home, "Kasir (POS)", "Transaksi cepat & cetak struk", Icons.Default.PointOfSale, Color(0xFFD97706), badge = "Utama"),
                    MetroTile(Screen.SalesOrder, "Pesanan Penjualan", "Riwayat order & status bayar", Icons.Default.Receipt, Color(0xFFEAB308)),
                    MetroTile(Screen.SessionCheck, "Sesi Kasir", "Buka/tutup shift kas & modal awal", Icons.Default.Timer, Color(0xFFB45309))
                )
            ),
            MetroCategory(
                name = "Harian",
                icon = Icons.Default.Storefront,
                color = Color(0xFF43A047),
                tiles = listOf(
                    MetroTile(Screen.Showcase, "Etalase Saji", "Pantau stok hidangan meja saji", Icons.Default.Storefront, Color(0xFF2E7D32)),
                    MetroTile(Screen.Ingredient, "Bahan Baku", "Stok bahan dapur & inventaris", Icons.Default.Kitchen, Color(0xFF00796B)),
                    MetroTile(Screen.EndOfDay, "Tutup Hari", "Rekapitulasi harian & setoran kasir", Icons.Default.EventNote, Color(0xFF388E3C))
                )
            ),
            MetroCategory(
                name = "Data Master",
                icon = Icons.Default.Inventory2,
                color = Color(0xFF8E24AA),
                tiles = listOf(
                    MetroTile(Screen.DishMaster, "Kelola Menu", "Katalog hidangan, harga jual & HPP", Icons.Default.RestaurantMenu, Color(0xFF6A1B9A)),
                    MetroTile(Screen.IngredientMaster, "Kelola Bahan", "Master bahan baku, satuan & biaya", Icons.Default.Inventory2, Color(0xFF512DA8))
                )
            ),
            MetroCategory(
                name = "Manajemen",
                icon = Icons.Default.Assessment,
                color = Color(0xFFFB8C00),
                tiles = listOf(
                    MetroTile(Screen.EmployerManagement, "Karyawan", "Kelola staf, kasir & hak akses", Icons.Default.People, Color(0xFFAD1457)),
                    MetroTile(Screen.Reporting, "Laporan Penjualan", "Analisis omset, grafik & laba kotor", Icons.Default.Assessment, Color(0xFFE65100)),
                    MetroTile(Screen.WorkInProcess, "Pekerjaan Berjalan", "Status antrian masak & pesanan dapur", Icons.Default.Pending, Color(0xFFD84315)),
                    MetroTile(Screen.AuditLog, "Log Audit", "Rekam jejak aktivitas operasional", Icons.Default.History, Color(0xFF455A64)),
                    MetroTile(Screen.Settings, "Pengaturan Toko", "Profil resto, konfigurasi printer & pajak", Icons.Default.Settings, Color(0xFF263238))
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50F172A)) // Dark slate backdrop (96% opacity)
            .clickable(onClick = onDismiss) // Click outside to dismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {} // Prevent click-through from children
        ) {
            // Metro Header Bar
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Cyan600,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "TAMBOOPOS (METRO TILES)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Pilih modul tujuan di bawah untuk berpindah layar seketika",
                                fontSize = 12.sp,
                                color = Gray400
                            )
                        }
                    }

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tutup (✕)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Metro Tiles Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 230.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                metroCategories.forEach { category ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(category.color)
                            )
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = category.name.uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    items(category.tiles) { tile ->
                        MetroTileCard(
                            tile = tile,
                            isSelected = currentScreen == tile.screen,
                            onClick = { onSelectScreen(tile.screen) }
                        )
                    }
                }
            }
        }
    }
}

// ── Metro Tile Card ─────────────────────────────────────────────────────────
@Composable
fun MetroTileCard(
    tile: MetroTile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = tile.color,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.5.dp, Color.White) else null,
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Watermark Accent Icon in Bottom-Right
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tile.icon,
                            contentDescription = tile.title,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "✓ AKTIF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tile.color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (tile.badge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = tile.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = tile.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tile.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── Screen: Kasir & POS ─────────────────────────────────────────────────────
@Composable
fun PosCashierScreen(
    menuList: List<MenuItem>,
    cart: MutableList<PosCartItem>,
    onOrderPaid: (OrderRecord) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastPaidAmount by remember { mutableLongStateOf(0L) }

    val categories = listOf("Semua", "Makanan", "Minuman", "Paket", "Lain-lain")

    val filteredList = menuList.filter {
        (selectedCategory == "Semua" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }

    val subtotal = cart.sumOf { it.item.price * it.quantity }
    val tax = (subtotal * 0.1).toLong()
    val grandTotal = subtotal + tax

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Catalog
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp)) {
            // Search & Category Filters
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari hidangan...") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Gray600) }
                )
                Spacer(Modifier.width(12.dp))
                categories.forEach { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Blue600 else White,
                        border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                        modifier = Modifier.padding(start = 6.dp).clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) White else Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(170.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val exist = cart.find { it.item.id == item.id }
                            if (exist != null) exist.quantity++
                            else cart.add(PosCartItem(item, 1))
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            DishImage(
                                imagePath = item.image,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(105.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                fallbackEmoji = item.emoji
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatRupiah(item.price), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Blue600, modifier = Modifier.padding(top = 4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (item.stock > 10) Blue50 else Color(0xFFFFEBEE),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text("Stok: ${item.stock}", fontSize = 11.sp, color = if (item.stock > 10) Blue700 else Error, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Cart Panel
        Card(
            modifier = Modifier.width(360.dp).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Text("Keranjang Pesanan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Gray300)

                if (cart.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛒", fontSize = 36.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Keranjang Kosong", color = Gray600, fontSize = 13.sp)
                            Text("Pilih hidangan di samping", color = Gray600, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cart) { ci ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Blue50, RoundedCornerShape(8.dp)).padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    DishImage(
                                        imagePath = ci.item.image,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        fallbackEmoji = ci.item.emoji
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(ci.item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(formatRupiah(ci.item.price * ci.quantity), fontSize = 11.sp, color = Blue700)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = White, modifier = Modifier.size(24.dp).clickable {
                                        if (ci.quantity > 1) ci.quantity-- else cart.remove(ci)
                                    }) { Box(contentAlignment = Alignment.Center) { Text("-", fontWeight = FontWeight.Bold) } }
                                    Text("${ci.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    Surface(shape = CircleShape, color = White, modifier = Modifier.size(24.dp).clickable { ci.quantity++ }) {
                                        Box(contentAlignment = Alignment.Center) { Text("+", fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Gray300)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = 12.sp, color = Gray600)
                    Text(formatRupiah(subtotal), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pajak PB1 (10%)", fontSize = 12.sp, color = Gray600)
                    Text(formatRupiah(tax), fontSize = 12.sp)
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(formatRupiah(grandTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blue600)
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (cart.isNotEmpty()) {
                            lastPaidAmount = grandTotal
                            val summary = cart.joinToString(", ") { "${it.quantity}x ${it.item.name}" }
                            onOrderPaid(
                                OrderRecord(
                                    id = "#ORD-${(1000..9999).random()}",
                                    time = "14:50",
                                    cashier = "Rahmat Hidayat",
                                    totalAmount = grandTotal,
                                    paymentMethod = "Cash / QRIS",
                                    status = "Selesai",
                                    itemsSummary = summary
                                )
                            )
                            cart.clear()
                            showSuccessDialog = true
                        }
                    },
                    enabled = cart.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold) },
            text = { Text("Pembayaran sebesar ${formatRupiah(lastPaidAmount)} telah dicatat.") },
            confirmButton = { Button(onClick = { showSuccessDialog = false }) { Text("Cetak Struk & Selesai") } }
        )
    }
}

// ── Screen: Pesanan Penjualan (Sales Orders) ─────────────────────────────────
@Composable
fun SalesOrderHistoryScreen(orders: List<OrderRecord>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Daftar Struk & Pesanan Penjualan", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { ord ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ord.id, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.width(10.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE8F5E9)) {
                                    Text(ord.status, fontSize = 11.sp, color = Success, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(ord.itemsSummary, fontSize = 12.sp, color = Gray600, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                            Text("Kasir: ${ord.cashier} • Jam: ${ord.time} • Metode: ${ord.paymentMethod}", fontSize = 11.sp, color = Gray600, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(formatRupiah(ord.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Blue600)
                    }
                }
            }
        }
    }
}

// ── Screen: Kelola Etalase (Dish Master) ─────────────────────────────────────
@Composable
fun DishMasterManagementScreen(menuList: MutableList<MenuItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Master Menu Hidangan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { /* Tambah menu */ }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Menu Baru")
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(menuList) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Gray300, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DishImage(
                                imagePath = item.image,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                fallbackEmoji = item.emoji
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Kategori: ${item.category} • Modal: ${formatRupiah(item.cost)}", fontSize = 12.sp, color = Gray600)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatRupiah(item.price), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Blue700, modifier = Modifier.padding(end = 16.dp))
                            OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("Edit", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Screen: Etalase Stok Harian (Showcase) ──────────────────────────────────
@Composable
fun ShowcaseStockScreen(menuList: List<MenuItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Etalase Saji - Stok Siap Santap", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuList) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Blue50)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DishImage(
                                    imagePath = item.image,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    fallbackEmoji = item.emoji
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(formatRupiah(item.price), fontSize = 12.sp, color = Blue600)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Tersedia di Etalase:", fontSize = 11.sp, color = Gray600)
                                Text("${item.stock} Porsi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Blue700)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Screen: Bahan Baku Harian (Ingredient) ──────────────────────────────────
@Composable
fun IngredientStockScreen(ingredients: List<IngredientItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Stok Bahan Baku Harian", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients) { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ing.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Batas Minimum: ${ing.minStock} ${ing.unit} • Harga: ${formatRupiah(ing.costPerUnit)}/${ing.unit}", fontSize = 11.sp, color = Gray600)
                        }
                        Text("${ing.stock} ${ing.unit}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (ing.stock <= ing.minStock) Error else Blue700)
                    }
                }
            }
        }
    }
}

// ── Screen: Kelola Bahan Baku (Ingredient Master) ───────────────────────────
@Composable
fun IngredientMasterManagementScreen(ingredients: MutableList<IngredientItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Master Katalog Bahan Baku", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = {}, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Bahan")
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients) { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Gray300, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ing.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Satuan: ${ing.unit} • Estimasi Harga Beli: ${formatRupiah(ing.costPerUnit)}", fontSize = 12.sp, color = Gray600)
                        }
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Edit", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Screen: Sesi Kasir (Session Check) ───────────────────────────────────────
@Composable
fun SessionControlScreen() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Kontrol Sesi Kasir Harian", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp), color = Blue50, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Sesi Operasional Sedang Berjalan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Dibuka pukul 08:00 oleh Budi Santoso (Modal Awal: Rp 300.000)", fontSize = 12.sp, color = Gray600)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Informasi Sesi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("• Total Transaksi Sesi Ini: 4 Transaksi", fontSize = 13.sp, color = Gray800)
            Text("• Total Pembayaran Diterima: Rp 285.000", fontSize = 13.sp, color = Gray800)
            Text("• Kasir yang Bertugas: Rahmat Hidayat & Siti Rahma", fontSize = 13.sp, color = Gray800)
        }
    }
}

// ── Screen: Tutup Hari (End of Day) ─────────────────────────────────────────
@Composable
fun EndOfDayScreen(menuList: List<MenuItem>, orderList: List<OrderRecord>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Prosedur Tutup Hari (End of Day)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Rekapitulasi total omset kasir dan pencatatan sisa hidangan etalase.", fontSize = 12.sp, color = Gray600)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            val omset = orderList.sumOf { it.totalAmount }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Blue50) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Omset Hari Ini", fontSize = 12.sp, color = Gray600)
                        Text(formatRupiah(omset), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Blue600)
                    }
                }
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFE8F5E9)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Transaksi", fontSize = 12.sp, color = Gray600)
                        Text("${orderList.size} Pesanan Selesai", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Success)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tutup Hari & Finalisasi Sesi", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Screen: Kelola Karyawan (Employer Management) ───────────────────────────
@Composable
fun EmployerListScreen(employees: List<Employee>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Data Karyawan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = {}, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Karyawan")
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(employees) { emp ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Gray300, RoundedCornerShape(8.dp)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Blue100, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(emp.name.take(1), fontWeight = FontWeight.Bold, color = Blue700) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Role: ${emp.role} • No HP: ${emp.phone}", fontSize = 12.sp, color = Gray600)
                            }
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE8F5E9)) {
                            Text(emp.status, fontSize = 11.sp, color = Success, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Screen: Laporan Penjualan (Reporting) ────────────────────────────────────
@Composable
fun ReportingOverviewScreen(orderList: List<OrderRecord>) {
    val totalOmset = orderList.sumOf { it.totalAmount }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Laporan Penjualan & Performa Toko", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Total Penjualan", fontSize = 12.sp, color = Gray600)
                    Text(formatRupiah(totalOmset), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Blue600, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Transaksi Selesai", fontSize = 12.sp, color = Gray600)
                    Text("${orderList.size} Pesanan", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Success, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Rata-rata Keranjang", fontSize = 12.sp, color = Gray600)
                    val avg = if (orderList.isNotEmpty()) totalOmset / orderList.size else 0L
                    Text(formatRupiah(avg), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Warning, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// ── Screen: Pekerjaan Berjalan (Work In Process) ─────────────────────────────
@Composable
fun WorkInProcessOverviewScreen(menuList: List<MenuItem>, orderList: List<OrderRecord>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Pekerjaan Berjalan (Work In Process)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Monitoring hidangan yang sedang diproses di dapur.", fontSize = 12.sp, color = Gray600)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(14.dp))
            Text("✓ Tidak ada pesanan tertunda di dapur. Semua pesanan telah disajikan.", fontSize = 13.sp, color = Gray800)
        }
    }
}

// ── Screen: Log Audit ───────────────────────────────────────────────────────
@Composable
fun AuditLogOverviewScreen(audits: List<AuditEntry>) {
    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Log Audit Aktivitas Sistem", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(audits) { a ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(a.time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue600, modifier = Modifier.width(60.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${a.user} • ${a.details}", fontSize = 11.sp, color = Gray600)
                        }
                    }
                }
            }
        }
    }
}

// ── Screen: Pengaturan (Settings) ───────────────────────────────────────────
@Composable
fun SettingsOverviewScreen() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Pengaturan tambooPOS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp), color = Blue50, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Informasi Lisensi & Platform", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("• Target Platform Aktif: ${PlatformConfig.platformName}", fontSize = 13.sp, color = Gray800)
                    Text("• Status Aktivasi: ${if (PlatformConfig.requiresActivation) "Wajib Diaktivasi (Mobile)" else "DI-BYPASS (Web Edition)"}", fontSize = 13.sp, color = Gray800)
                    Text("• Versi Aplikasi: 1.0.0 (KMP Multiplatform)", fontSize = 13.sp, color = Gray800)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Pengaturan Toko:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Nama Toko: Rumah Makan Padang tambooPOS", fontSize = 13.sp, color = Gray800)
            Text("Pajak PB1: 10% (Aktif)", fontSize = 13.sp, color = Gray800)
            Text("Mata Uang: Rupiah (IDR)", fontSize = 13.sp, color = Gray800)
        }
    }
}
