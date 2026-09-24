package com.argomin.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.PlatformConfig
import com.argomin.app.core.Screen
import com.argomin.app.domain.model.*
import com.argomin.app.generated.resources.Res
import com.argomin.app.generated.resources.logo
import com.argomin.app.presentation.audit.AuditLogOverviewScreen
import com.argomin.app.presentation.auth.CommonLoginScreen
import com.argomin.app.presentation.employer.EmployerListScreen
import com.argomin.app.presentation.ingredient.IngredientMasterManagementScreen
import com.argomin.app.presentation.ingredient.IngredientStockScreen
import com.argomin.app.presentation.kasir.KasirViewModel
import com.argomin.app.presentation.kasir.PosCashierScreen
import com.argomin.app.presentation.navigation.MetroTileMenuOverlay
import com.argomin.app.presentation.reporting.ReportingOverviewScreen
import com.argomin.app.presentation.sales.SalesOrderHistoryScreen
import com.argomin.app.presentation.session.EndOfDayScreen
import com.argomin.app.presentation.session.SessionControlScreen
import com.argomin.app.presentation.settings.SettingsOverviewScreen
import com.argomin.app.presentation.showcase.DishMasterManagementScreen
import com.argomin.app.presentation.showcase.ShowcaseStockScreen
import com.argomin.app.presentation.theme.*
import com.argomin.app.presentation.wip.WorkInProcessOverviewScreen
import org.jetbrains.compose.resources.painterResource

@Composable
fun CommonApp() {
        TambooPosTheme {
                var loggedInEmployee by remember { mutableStateOf<Employee?>(null) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var showMetroMenu by remember { mutableStateOf(false) }

                // Master menu data dari dishes.csv & dish_package.json
                val menuList = remember {
                        mutableStateListOf(
                                MenuItem(
                                        "1001",
                                        "Rendang Daging",
                                        "Makanan",
                                        35000,
                                        22000,
                                        35,
                                        "dish_image/1001.png",
                                        "🥩"
                                ),
                                MenuItem(
                                        "1002",
                                        "Gulai Ayam",
                                        "Makanan",
                                        28000,
                                        18000,
                                        28,
                                        "dish_image/1002.png",
                                        "🍗"
                                ),
                                MenuItem(
                                        "1003",
                                        "Gulai Ikan",
                                        "Makanan",
                                        32000,
                                        20000,
                                        20,
                                        "dish_image/1003.png",
                                        "🐟"
                                ),
                                MenuItem(
                                        "1004",
                                        "Ayam Pop",
                                        "Makanan",
                                        30000,
                                        19000,
                                        30,
                                        "dish_image/1004.png",
                                        "🍗"
                                ),
                                MenuItem(
                                        "1005",
                                        "Dendeng Balado",
                                        "Makanan",
                                        38000,
                                        24000,
                                        22,
                                        "dish_image/1005.png",
                                        "🥓"
                                ),
                                MenuItem(
                                        "1006",
                                        "Gulai Otak",
                                        "Makanan",
                                        25000,
                                        15000,
                                        15,
                                        "dish_image/1006.png",
                                        "🍲"
                                ),
                                MenuItem(
                                        "1007",
                                        "Gulai Limpa",
                                        "Makanan",
                                        24000,
                                        14000,
                                        18,
                                        "dish_image/1007.png",
                                        "🥣"
                                ),
                                MenuItem(
                                        "1008",
                                        "Gulai Usus",
                                        "Makanan",
                                        24000,
                                        14000,
                                        20,
                                        "dish_image/1008.png",
                                        "🥣"
                                ),
                                MenuItem(
                                        "1009",
                                        "Terong Balado",
                                        "Makanan",
                                        14000,
                                        8000,
                                        25,
                                        "dish_image/1009.png",
                                        "🍆"
                                ),
                                MenuItem(
                                        "1010",
                                        "Kacang Panjang Balado",
                                        "Makanan",
                                        14000,
                                        7000,
                                        25,
                                        "dish_image/1010.png",
                                        "🥗"
                                ),
                                MenuItem(
                                        "1011",
                                        "Nasi Putih",
                                        "Makanan",
                                        5000,
                                        2000,
                                        100,
                                        "dish_image/1011.png",
                                        "🍚"
                                ),
                                MenuItem(
                                        "1012",
                                        "Nasi Kuning",
                                        "Makanan",
                                        8000,
                                        3500,
                                        40,
                                        "dish_image/1012.png",
                                        "🍚"
                                ),
                                MenuItem(
                                        "1013",
                                        "Sambal Ijo",
                                        "Makanan",
                                        5000,
                                        1500,
                                        50,
                                        "dish_image/1013.png",
                                        "🌶️"
                                ),
                                MenuItem(
                                        "1014",
                                        "Sambal Merah",
                                        "Makanan",
                                        5000,
                                        1500,
                                        50,
                                        "dish_image/1014.png",
                                        "🌶️"
                                ),
                                MenuItem(
                                        "1015",
                                        "Sambal Balado",
                                        "Makanan",
                                        5000,
                                        1500,
                                        50,
                                        "dish_image/1015.png",
                                        "🌶️"
                                ),
                                MenuItem(
                                        "1016",
                                        "Es Teh Manis",
                                        "Minuman",
                                        5000,
                                        1200,
                                        80,
                                        "dish_image/1016.png",
                                        "🧋"
                                ),
                                MenuItem(
                                        "1017",
                                        "Es Jeruk",
                                        "Minuman",
                                        8000,
                                        3000,
                                        40,
                                        "dish_image/1017.png",
                                        "🍊"
                                ),
                                MenuItem(
                                        "1018",
                                        "Teh Tawar Panas",
                                        "Minuman",
                                        3000,
                                        800,
                                        60,
                                        "dish_image/1018.png",
                                        "🍵"
                                ),
                                MenuItem(
                                        "1019",
                                        "Kopi Hitam",
                                        "Minuman",
                                        8000,
                                        3000,
                                        35,
                                        "dish_image/1019.png",
                                        "☕"
                                ),
                                MenuItem(
                                        "1020",
                                        "Air Mineral",
                                        "Minuman",
                                        3000,
                                        1500,
                                        90,
                                        "dish_image/1020.png",
                                        "💧"
                                ),
                                MenuItem(
                                        "1021",
                                        "Kerupuk Jangek",
                                        "Lain-lain",
                                        8000,
                                        4000,
                                        45,
                                        "dish_image/1021.png",
                                        "🍘"
                                ),
                                MenuItem(
                                        "1022",
                                        "Kerupuk Sanjai",
                                        "Lain-lain",
                                        10000,
                                        5000,
                                        30,
                                        "dish_image/1022.png",
                                        "🍘"
                                ),
                                MenuItem(
                                        "1023",
                                        "Perkedel Kentang",
                                        "Lain-lain",
                                        8000,
                                        3500,
                                        35,
                                        "dish_image/1023.png",
                                        "🥔"
                                ),
                                MenuItem(
                                        "1024",
                                        "Perkedel Jagung",
                                        "Lain-lain",
                                        8000,
                                        3500,
                                        35,
                                        "dish_image/1024.png",
                                        "🌽"
                                ),
                                MenuItem(
                                        "1025",
                                        "Paket Komplit",
                                        "Paket",
                                        45000,
                                        27000,
                                        25,
                                        "dish_image/1025.png",
                                        "🍱"
                                ),
                                MenuItem(
                                        "1026",
                                        "Paket Berdua",
                                        "Paket",
                                        85000,
                                        52000,
                                        15,
                                        "dish_image/1026.png",
                                        "🍱"
                                ),
                                MenuItem(
                                        "1027",
                                        "Paket Keluarga",
                                        "Paket",
                                        165000,
                                        100000,
                                        10,
                                        "dish_image/1027.png",
                                        "🍱"
                                ),
                                MenuItem(
                                        "1028",
                                        "Paket Hemat",
                                        "Paket",
                                        32000,
                                        19000,
                                        30,
                                        "dish_image/1028.png",
                                        "🍱"
                                )
                        )
                }

                val ingredientList = remember {
                        mutableStateListOf(
                                IngredientItem(
                                        "1",
                                        "Daging Sapi Has Luar",
                                        "Kg",
                                        24.5,
                                        10.0,
                                        130000
                                ),
                                IngredientItem("2", "Ayam Segar Potong", "Ekor", 30.0, 15.0, 38000),
                                IngredientItem("3", "Telur Ayam Negeri", "Kg", 18.0, 5.0, 28000),
                                IngredientItem(
                                        "4",
                                        "Santan Kelapa Murni",
                                        "Liter",
                                        20.0,
                                        8.0,
                                        16000
                                ),
                                IngredientItem(
                                        "5",
                                        "Beras Solok Premium",
                                        "Karung",
                                        6.0,
                                        2.0,
                                        310000
                                ),
                                IngredientItem("6", "Cabai Merah Keriting", "Kg", 8.0, 3.0, 45000),
                                IngredientItem("7", "Cabai Rawit Hijau", "Kg", 5.0, 2.0, 50000),
                                IngredientItem("8", "Bawang Merah & Putih", "Kg", 12.0, 4.0, 36000)
                        )
                }

                val orderList = remember {
                        mutableStateListOf(
                                OrderRecord(
                                        "#ORD-1001",
                                        "12:15",
                                        "Rahmat Hidayat",
                                        76000,
                                        "QRIS",
                                        "Selesai",
                                        "2x Rendang, 1x Teh Talua, 1x Es Jeruk"
                                ),
                                OrderRecord(
                                        "#ORD-1002",
                                        "12:40",
                                        "Rahmat Hidayat",
                                        44000,
                                        "Cash",
                                        "Selesai",
                                        "1x Ayam Pop, 1x Telur Dadar, 1x Es Teh"
                                ),
                                OrderRecord(
                                        "#ORD-1003",
                                        "13:05",
                                        "Siti Rahma",
                                        112000,
                                        "Cash",
                                        "Selesai",
                                        "3x Gulai Tunjang, 2x Perkedel, 2x Es Jeruk"
                                ),
                                OrderRecord(
                                        "#ORD-1004",
                                        "13:30",
                                        "Siti Rahma",
                                        53000,
                                        "QRIS",
                                        "Selesai",
                                        "2x Dendeng Batokok, 1x Sambal Ijo"
                                )
                        )
                }

                val employeeList = remember {
                        mutableStateListOf(
                                Employee(
                                        "EMP-01",
                                        "Budi Santoso",
                                        "MANAGER",
                                        "081234567890",
                                        "Aktif",
                                        pin = "0000"
                                ),
                                Employee(
                                        "EMP-02",
                                        "Rahmat Hidayat",
                                        "CASHIER",
                                        "081398765432",
                                        "Aktif",
                                        pin = "1234"
                                ),
                                Employee(
                                        "EMP-03",
                                        "Siti Rahma",
                                        "CASHIER",
                                        "082156781234",
                                        "Aktif",
                                        pin = "1234"
                                ),
                                Employee(
                                        "EMP-04",
                                        "Ahmad Fauzi",
                                        "KOKI",
                                        "085211223344",
                                        "Aktif",
                                        pin = "1234"
                                )
                        )
                }

                val auditList = remember {
                        mutableStateListOf(
                                AuditEntry(
                                        "13:35",
                                        "Rahmat (Kasir)",
                                        "TRANSAKSI_SELESAI",
                                        "Mencatat transaksi #ORD-1004 Rp 53.000 (QRIS)"
                                ),
                                AuditEntry(
                                        "12:00",
                                        "Budi (Manager)",
                                        "BUKA_SESI",
                                        "Membuka sesi kasir harian dengan modal awal Rp 300.000"
                                ),
                                AuditEntry(
                                        "11:30",
                                        "Budi (Manager)",
                                        "UPDATE_STOK",
                                        "Menambah stok etalase Rendang Daging +15 porsi"
                                ),
                                AuditEntry(
                                        "08:00",
                                        "Sistem",
                                        "APLIKASI_DIMULAI",
                                        "Aplikasi POS dijalankan pada platform ${PlatformConfig.platformName}"
                                )
                        )
                }

                val kasirViewModel = remember { KasirViewModel() }

                // ── GERBANG AUTENTIKASI: Jika belum ada karyawan login, tampilkan LoginScreen ──
                if (loggedInEmployee == null) {
                        CommonLoginScreen(
                                employees = employeeList,
                                onLoginSuccess = { emp ->
                                        loggedInEmployee = emp
                                        currentScreen = Screen.Home
                                        auditList.add(
                                                0,
                                                AuditEntry(
                                                        "Sekarang",
                                                        "${emp.name} (${emp.role})",
                                                        "LOGIN",
                                                        "Berhasil login dengan autentikasi PIN"
                                                )
                                        )
                                }
                        )
                } else {
                        val activeEmployee = loggedInEmployee!!

                        // FULL SCREEN ROOT CONTAINER (NO FIXED SIDEBAR - OPTIMIZED FOR TABLET)
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(MaterialTheme.colorScheme.background)
                        ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                        // TOP BAR WITH HAMBURGER BUTTON (MATERIAL 3 PROPORTIONAL
                                        // 64DP)
                                        Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shadowElevation = 1.dp,
                                                modifier = Modifier.fillMaxWidth().height(64.dp)
                                        ) {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                horizontal = 16.dp
                                                                        ),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        // Left: Hamburger Menu Button + Logo &
                                                        // Branding + Active Screen Badge
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                FilledTonalIconButton(
                                                                        onClick = {
                                                                                showMetroMenu = true
                                                                        },
                                                                        colors =
                                                                                IconButtonDefaults
                                                                                        .filledTonalIconButtonColors(
                                                                                                containerColor =
                                                                                                        Cyan50,
                                                                                                contentColor =
                                                                                                        Cyan700
                                                                                        ),
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        12.dp
                                                                                ),
                                                                        modifier =
                                                                                Modifier.size(44.dp)
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Default
                                                                                                .Menu,
                                                                                contentDescription =
                                                                                        "Menu Navigasi",
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                24.dp
                                                                                        )
                                                                        )
                                                                }

                                                                Spacer(Modifier.width(16.dp))

                                                                // Logo & Branding (SVG from
                                                                // composeResources)
                                                                Row(
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        modifier =
                                                                                Modifier.clickable {
                                                                                        currentScreen =
                                                                                                Screen.Home
                                                                                }
                                                                ) {
                                                                        Image(
                                                                                painter =
                                                                                        painterResource(
                                                                                                Res.drawable
                                                                                                        .logo
                                                                                        ),
                                                                                contentDescription =
                                                                                        "tambooPOS Logo",
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                34.dp
                                                                                        )
                                                                        )
                                                                        Spacer(
                                                                                Modifier.width(
                                                                                        10.dp
                                                                                )
                                                                        )
                                                                        Column {
                                                                                Text(
                                                                                        text =
                                                                                                "tambooPOS",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleMedium
                                                                                                        .copy(
                                                                                                                fontWeight =
                                                                                                                        FontWeight
                                                                                                                                .Black
                                                                                                        ),
                                                                                        color =
                                                                                                Cyan700,
                                                                                        letterSpacing =
                                                                                                0.5.sp
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Resto Edition",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodySmall,
                                                                                        color =
                                                                                                Gray600
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        // Right: Active Employee Info & Logout
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {

                                                                // Employee Profile Capsule
                                                                Surface(
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        24.dp
                                                                                ),
                                                                        color = Cyan50,
                                                                        border =
                                                                                BorderStroke(
                                                                                        1.dp,
                                                                                        Cyan100
                                                                                )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                horizontal =
                                                                                                        10.dp,
                                                                                                vertical =
                                                                                                        5.dp
                                                                                        ),
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Surface(
                                                                                        shape =
                                                                                                CircleShape,
                                                                                        color =
                                                                                                Cyan700,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        28.dp
                                                                                                )
                                                                                ) {
                                                                                        Box(
                                                                                                contentAlignment =
                                                                                                        Alignment
                                                                                                                .Center
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                activeEmployee
                                                                                                                        .name
                                                                                                                        .take(
                                                                                                                                1
                                                                                                                        )
                                                                                                                        .uppercase(),
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Bold,
                                                                                                        fontSize =
                                                                                                                12.sp,
                                                                                                        color =
                                                                                                                White
                                                                                                )
                                                                                        }
                                                                                }
                                                                                Spacer(
                                                                                        Modifier.width(
                                                                                                8.dp
                                                                                        )
                                                                                )
                                                                                Column {
                                                                                        Text(
                                                                                                text =
                                                                                                        activeEmployee
                                                                                                                .name,
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .labelMedium
                                                                                                                .copy(
                                                                                                                        fontWeight =
                                                                                                                                FontWeight
                                                                                                                                        .Bold
                                                                                                                ),
                                                                                                color =
                                                                                                        Cyan900
                                                                                        )
                                                                                        Text(
                                                                                                text =
                                                                                                        activeEmployee
                                                                                                                .role,
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .labelSmall,
                                                                                                color =
                                                                                                        Cyan600
                                                                                        )
                                                                                }
                                                                        }
                                                                }

                                                                Spacer(Modifier.width(8.dp))

                                                                // Logout / Switch Employee Button
                                                                IconButton(
                                                                        onClick = {
                                                                                auditList.add(
                                                                                        0,
                                                                                        AuditEntry(
                                                                                                "Sekarang",
                                                                                                "${activeEmployee.name} (${activeEmployee.role})",
                                                                                                "LOGOUT",
                                                                                                "Keluar dari sesi kasir"
                                                                                        )
                                                                                )
                                                                                loggedInEmployee =
                                                                                        null
                                                                        },
                                                                        modifier =
                                                                                Modifier.size(36.dp)
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.AutoMirrored
                                                                                                .Filled
                                                                                                .Logout,
                                                                                contentDescription =
                                                                                        "Keluar / Ganti Akun",
                                                                                tint =
                                                                                        Color(
                                                                                                0xFFDC2626
                                                                                        ),
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                18.dp
                                                                                        )
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        // SCREEN CONTENT AREA (FULL TABLET WIDTH)
                                        Box(
                                                modifier =
                                                        Modifier.weight(1f)
                                                                .fillMaxWidth()
                                                                .padding(14.dp)
                                        ) {
                                                when (currentScreen) {
                                                        Screen.Home ->
                                                                PosCashierScreen(
                                                                        menuList,
                                                                        kasirViewModel,
                                                                        onOrderPaid = { ord ->
                                                                                orderList.add(
                                                                                        0,
                                                                                        ord.copy(
                                                                                                cashier =
                                                                                                        activeEmployee
                                                                                                                .name
                                                                                        )
                                                                                )
                                                                        }
                                                                )
                                                        Screen.SalesOrder ->
                                                                SalesOrderHistoryScreen(orderList)
                                                        Screen.Showcase ->
                                                                ShowcaseStockScreen(menuList)
                                                        Screen.DishMaster ->
                                                                DishMasterManagementScreen(menuList)
                                                        Screen.Ingredient ->
                                                                IngredientStockScreen(
                                                                        ingredientList
                                                                )
                                                        Screen.IngredientMaster ->
                                                                IngredientMasterManagementScreen(
                                                                        ingredientList
                                                                )
                                                        Screen.SessionCheck ->
                                                                SessionControlScreen()
                                                        Screen.EndOfDay ->
                                                                EndOfDayScreen(menuList, orderList)
                                                        Screen.EmployerManagement ->
                                                                EmployerListScreen(employeeList)
                                                        Screen.Reporting ->
                                                                ReportingOverviewScreen(orderList)
                                                        Screen.WorkInProcess ->
                                                                WorkInProcessOverviewScreen(
                                                                        menuList,
                                                                        orderList
                                                                )
                                                        Screen.AuditLog ->
                                                                AuditLogOverviewScreen(auditList)
                                                        Screen.Settings -> SettingsOverviewScreen()
                                                        else ->
                                                                PosCashierScreen(
                                                                        menuList,
                                                                        kasirViewModel,
                                                                        onOrderPaid = { ord ->
                                                                                orderList.add(
                                                                                        0,
                                                                                        ord.copy(
                                                                                                cashier =
                                                                                                        activeEmployee
                                                                                                                .name
                                                                                        )
                                                                                )
                                                                        }
                                                                )
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
