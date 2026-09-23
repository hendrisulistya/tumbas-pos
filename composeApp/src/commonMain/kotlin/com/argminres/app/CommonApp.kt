package com.argminres.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.argminres.app.core.PlatformConfig
import com.argminres.app.core.Screen
import com.argminres.app.ui.theme.*
import com.argminres.app.util.formatRupiah

// ── Models ──────────────────────────────────────────────────────────────────
data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val cost: Long,
    var stock: Int,
    val emoji: String,
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
    val status: String
)

data class AuditEntry(
    val time: String,
    val user: String,
    val action: String,
    val details: String
)

// ── Navigation Menu Entry ───────────────────────────────────────────────────
data class NavMenuItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val section: String
)

@Composable
fun CommonApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue600,
            onPrimary = White,
            primaryContainer = Blue50,
            onPrimaryContainer = Blue700,
            secondary = Blue500,
            background = Color(0xFFF8F9FA),
            surface = White,
            surfaceVariant = Blue50,
            onSurface = Black,
            onSurfaceVariant = Gray600
        )
    ) {
        PlatformStatusBar(color = Blue600, darkIcons = false)

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

        // Master sample data
        val menuList = remember {
            mutableStateListOf(
                MenuItem("1", "Rendang Daging Sapi", "Makanan", 25000, 16000, 35, "🥩"),
                MenuItem("2", "Ayam Pop Gurih", "Makanan", 22000, 13000, 28, "🍗"),
                MenuItem("3", "Gulai Tunjang Kikil", "Makanan", 30000, 19000, 18, "🍲"),
                MenuItem("4", "Dendeng Batokok Balado", "Makanan", 26000, 17000, 22, "🍖"),
                MenuItem("5", "Telur Dadar Khas Padang", "Makanan", 12000, 5000, 45, "🍳"),
                MenuItem("6", "Ayam Bakar Padang", "Makanan", 23000, 14000, 20, "🍗"),
                MenuItem("7", "Sayur Nangka & Singkong", "Tambahan", 8000, 2500, 50, "🥗"),
                MenuItem("8", "Perkedel Kentang", "Tambahan", 6000, 2000, 30, "🥔"),
                MenuItem("9", "Sambal Ijo & Merah", "Tambahan", 5000, 1500, 60, "🌶️"),
                MenuItem("10", "Teh Talua (Teh Telur)", "Minuman", 15000, 6000, 40, "🍵"),
                MenuItem("11", "Es Teh Manis", "Minuman", 6000, 1200, 80, "🧋"),
                MenuItem("12", "Es Jeruk Murni", "Minuman", 8000, 3000, 40, "🍊"),
                MenuItem("13", "Jus Alpukat Kental", "Minuman", 14000, 6000, 25, "🥑")
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
                OrderRecord("#ORD-1001", "12:15", "Rahmat", 76000, "QRIS", "Selesai", "2x Rendang, 1x Teh Talua, 1x Es Jeruk"),
                OrderRecord("#ORD-1002", "12:40", "Rahmat", 44000, "Cash", "Selesai", "1x Ayam Pop, 1x Telur Dadar, 1x Es Teh"),
                OrderRecord("#ORD-1003", "13:05", "Siti", 112000, "Cash", "Selesai", "3x Gulai Tunjang, 2x Perkedel, 2x Es Jeruk"),
                OrderRecord("#ORD-1004", "13:30", "Siti", 53000, "QRIS", "Selesai", "2x Dendeng Batokok, 1x Sambal Ijo")
            )
        }

        val employeeList = remember {
            mutableStateListOf(
                Employee("EMP-01", "Budi Santoso", "MANAGER", "081234567890", "Aktif"),
                Employee("EMP-02", "Rahmat Hidayat", "CASHIER", "081398765432", "Aktif"),
                Employee("EMP-03", "Siti Rahma", "CASHIER", "082156781234", "Aktif"),
                Employee("EMP-04", "Ahmad Fauzi", "KOKI", "085211223344", "Aktif")
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

        val menuItems = listOf(
            NavMenuItem(Screen.Home, "Kasir (POS)", Icons.Default.PointOfSale, "Operasional"),
            NavMenuItem(Screen.SalesOrder, "Pesanan Penjualan", Icons.Default.Receipt, "Operasional"),
            NavMenuItem(Screen.SessionCheck, "Sesi Kasir", Icons.Default.Timer, "Operasional"),
            NavMenuItem(Screen.Showcase, "Etalase Saji", Icons.Default.Storefront, "Harian"),
            NavMenuItem(Screen.Ingredient, "Bahan Baku", Icons.Default.Kitchen, "Harian"),
            NavMenuItem(Screen.EndOfDay, "Tutup Hari", Icons.Default.EventNote, "Harian"),
            NavMenuItem(Screen.DishMaster, "Kelola Menu", Icons.Default.RestaurantMenu, "Data Master"),
            NavMenuItem(Screen.IngredientMaster, "Kelola Bahan", Icons.Default.Inventory2, "Data Master"),
            NavMenuItem(Screen.EmployerManagement, "Karyawan", Icons.Default.People, "Manajemen"),
            NavMenuItem(Screen.Reporting, "Laporan Penjualan", Icons.Default.Assessment, "Manajemen"),
            NavMenuItem(Screen.WorkInProcess, "Pekerjaan Berjalan", Icons.Default.Pending, "Manajemen"),
            NavMenuItem(Screen.AuditLog, "Log Audit", Icons.Default.History, "Manajemen"),
            NavMenuItem(Screen.Settings, "Pengaturan", Icons.Default.Settings, "Sistem")
        )

        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // SIDEBAR NAVIGATION
            Surface(
                modifier = Modifier.width(260.dp).fillMaxHeight(),
                color = White,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Brand
                    Surface(
                        color = Blue600,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "🍲 TumbasPOS",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Rumah Makan Padang",
                                color = Blue100,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (PlatformConfig.requiresActivation) Color(0xFF5D3F00) else Color(0xFF1B4D2E)
                            ) {
                                Text(
                                    text = if (PlatformConfig.requiresActivation) "Mobile (Perlu Aktivasi)" else "Web (Bypass Aktivasi)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (PlatformConfig.requiresActivation) Color(0xFFFFD54F) else Color(0xFF81C784),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Navigation List
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        var lastSection = ""
                        menuItems.forEach { item ->
                            if (item.section != lastSection) {
                                lastSection = item.section
                                item {
                                    Text(
                                        text = item.section.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gray600,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 4.dp)
                                    )
                                }
                            }
                            item {
                                val isSelected = currentScreen == item.screen
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { currentScreen = item.screen },
                                    color = if (isSelected) Blue50 else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (isSelected) Blue600 else Gray600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Blue700 else Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // User Profile Footer
                    HorizontalDivider(color = Gray300)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Blue100,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("B", fontWeight = FontWeight.Bold, color = Blue700)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Budi Santoso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("MANAGER", fontSize = 11.sp, color = Gray600)
                        }
                    }
                }
            }

            // MAIN CONTENT VIEW AREA
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // TOP BAR
                Surface(
                    color = White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = menuItems.find { it.screen == currentScreen }?.label ?: "Menu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue700
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Success))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Sesi Kasir: Aktif", fontSize = 12.sp, color = Success, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text("Kasir: Rahmat Hidayat", fontSize = 13.sp, color = Gray800)
                        }
                    }
                }

                // SCREEN CONTENT ROUTING
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    when (currentScreen) {
                        Screen.Home -> PosCashierScreen(menuList, cart, onOrderPaid = { ord -> orderList.add(0, ord) })
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
                        else -> PosCashierScreen(menuList, cart, onOrderPaid = { ord -> orderList.add(0, ord) })
                    }
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

    val categories = listOf("Semua", "Makanan", "Minuman", "Tambahan")

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
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(item.emoji, fontSize = 38.sp)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ci.item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(formatRupiah(ci.item.price * ci.quantity), fontSize = 11.sp, color = Blue700)
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
                            Text(item.emoji, fontSize = 28.sp)
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
                                Text(item.emoji, fontSize = 28.sp)
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
            Text("Pengaturan TumbasPOS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            Text("Nama Toko: Rumah Makan Padang TumbasPOS", fontSize = 13.sp, color = Gray800)
            Text("Pajak PB1: 10% (Aktif)", fontSize = 13.sp, color = Gray800)
            Text("Mata Uang: Rupiah (IDR)", fontSize = 13.sp, color = Gray800)
        }
    }
}
