package com.argminres.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.argminres.app.core.PlatformConfig

enum class WebScreen {
    LANDING,
    DASHBOARD
}

data class ProductItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Long,
    val emoji: String
)

data class CartItem(
    val product: ProductItem,
    var quantity: Int
)

data class CompletedOrder(
    val orderId: String,
    val totalAmount: Long,
    val itemCount: Int,
    val paymentMethod: String,
    val time: String
)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "webApp") {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                background = Color(0xFF141218),
                surface = Color(0xFF1D1B20),
                surfaceVariant = Color(0xFF2B2831),
                onSurface = Color(0xFFE6E0E9),
                onSurfaceVariant = Color(0xFFCAC4D0)
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                var currentScreen by remember { mutableStateOf(WebScreen.LANDING) }

                when (currentScreen) {
                    WebScreen.LANDING -> WebPosLanding(
                        onOpenDashboard = { currentScreen = WebScreen.DASHBOARD }
                    )
                    WebScreen.DASHBOARD -> WebPosDashboard(
                        onBackToLanding = { currentScreen = WebScreen.LANDING }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. LANDING SCREEN
// -------------------------------------------------------------
@Composable
fun WebPosLanding(onOpenDashboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "WEB EDITION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = "Tumbas POS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Platform: ${PlatformConfig.platformName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = Color(0xFF332D41))
                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF231F2A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (PlatformConfig.requiresActivation) Color(0xFF5D3F00) else Color(0xFF1B4D2E),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (PlatformConfig.requiresActivation) "!" else "✓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (PlatformConfig.requiresActivation) Color(0xFFFFD54F) else Color(0xFF81C784)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Mekanisme Aktivasi Toko",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (PlatformConfig.requiresActivation) {
                                        "Wajib Aktivasi (Platform Mobile)"
                                    } else {
                                        "DI-BYPASS (Web Edition tidak memerlukan aktivasi)"
                                    },
                                    fontSize = 13.sp,
                                    color = if (PlatformConfig.requiresActivation) Color(0xFFFFD54F) else Color(0xFF81C784),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF332D41))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF372E50),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "→",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFFD0BCFF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Alur Masuk Web",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Langsung ke Menu Kasir & Dashboard POS",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onOpenDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Buka Dashboard POS →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. DASHBOARD & POS INTERACTIVE SCREEN
// -------------------------------------------------------------
@Composable
fun WebPosDashboard(onBackToLanding: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Semua") }
    val cart = remember { mutableStateListOf<CartItem>() }
    val orders = remember {
        mutableStateListOf(
            CompletedOrder("#ORD-101", 62000, 3, "Cash", "14:20"),
            CompletedOrder("#ORD-102", 48000, 2, "QRIS", "15:05")
        )
    }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var lastPaidAmount by remember { mutableLongStateOf(0L) }

    val sampleProducts = remember {
        listOf(
            ProductItem("1", "Rendang Daging", "Makanan", 25000, "🥩"),
            ProductItem("2", "Ayam Pop", "Makanan", 22000, "🍗"),
            ProductItem("3", "Gulai Tunjang", "Makanan", 30000, "🍲"),
            ProductItem("4", "Dendeng Batokok", "Makanan", 26000, "🍖"),
            ProductItem("5", "Telur Dadar Padang", "Makanan", 12000, "🍳"),
            ProductItem("6", "Sayur Daun Singkong", "Tambahan", 8000, "🥗"),
            ProductItem("7", "Perkedel Kentang", "Tambahan", 6000, "🥔"),
            ProductItem("8", "Sambal Ijo / Merah", "Tambahan", 5000, "🌶️"),
            ProductItem("9", "Teh Talua", "Minuman", 15000, "🍵"),
            ProductItem("10", "Es Teh Manis", "Minuman", 6000, "🧋"),
            ProductItem("11", "Es Jeruk Peras", "Minuman", 8000, "🍊"),
            ProductItem("12", "Jus Alpukat", "Minuman", 14000, "🥑")
        )
    }

    val categories = listOf("Semua", "Makanan", "Minuman", "Tambahan")

    val filteredProducts = remember(selectedCategory) {
        if (selectedCategory == "Semua") sampleProducts
        else sampleProducts.filter { it.category == selectedCategory }
    }

    val subtotal = cart.sumOf { it.product.price * it.quantity }
    val tax = (subtotal * 0.1).toLong()
    val totalAmount = subtotal + tax

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP APP BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🍲 TumbasPOS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B4D2E)
                    ) {
                        Text(
                            text = "✓ Tanpa Aktivasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2B2831)
                    ) {
                        Text(
                            text = "Web Wasm",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCAC4D0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kasir: Web Admin",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = onBackToLanding,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Info Aktivasi", fontSize = 12.sp)
                    }
                }
            }
        }

        // NAVIGATION TABS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🛒 Kasir & Penjualan", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📋 Riwayat Pesanan (${orders.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("📊 Ringkasan Penjualan", fontWeight = FontWeight.SemiBold) }
            )
        }

        // TAB CONTENTS
        when (selectedTab) {
            0 -> {
                // POS SCREEN (PRODUCT GRID + CART)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // LEFT COLUMN: CATEGORIES + PRODUCTS
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 16.dp)
                    ) {
                        // Category Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // Product Grid
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProducts) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val existing = cart.find { it.product.id == item.id }
                                            if (existing != null) {
                                                val index = cart.indexOf(existing)
                                                cart[index] = existing.copy(quantity = existing.quantity + 1)
                                            } else {
                                                cart.add(CartItem(item, 1))
                                            }
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = item.emoji,
                                            fontSize = 36.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Rp ${formatRupiah(item.price)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN: CART & PAYMENT
                    Card(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Keranjang Pesanan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF332D41))

                            if (cart.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🛒", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Keranjang Kosong",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Klik menu untuk menambahkan",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(cart) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${item.product.emoji} ${item.product.name}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Rp ${formatRupiah(item.product.price * item.quantity)}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF3F3B4A),
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clickable {
                                                            val index = cart.indexOf(item)
                                                            if (item.quantity > 1) {
                                                                cart[index] = item.copy(quantity = item.quantity - 1)
                                                            } else {
                                                                cart.remove(item)
                                                            }
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("-", fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                Text(
                                                    text = "${item.quantity}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )

                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF3F3B4A),
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clickable {
                                                            val index = cart.indexOf(item)
                                                            cart[index] = item.copy(quantity = item.quantity + 1)
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF332D41))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Total calculation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(subtotal)}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pajak PB1 (10%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(tax)}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Tagihan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Rp ${formatRupiah(totalAmount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (cart.isNotEmpty()) {
                                        lastPaidAmount = totalAmount
                                        orders.add(
                                            0,
                                            CompletedOrder(
                                                orderId = "#ORD-${(100..999).random()}",
                                                totalAmount = totalAmount,
                                                itemCount = cart.sumOf { it.quantity },
                                                paymentMethod = "Cash/QRIS",
                                                time = "Baru Saja"
                                            )
                                        )
                                        cart.clear()
                                        showPaymentSuccessDialog = true
                                    }
                                },
                                enabled = cart.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // ORDER HISTORY SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Riwayat Transaksi Pesanan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(orders) { ord ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(ord.orderId, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            "${ord.itemCount} items • ${ord.paymentMethod} • ${ord.time}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Rp ${formatRupiah(ord.totalAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF1B4D2E),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                "Sukses",
                                                fontSize = 11.sp,
                                                color = Color(0xFF81C784),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // REPORT SUMMARY SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Ringkasan Penjualan Hari Ini",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val totalOmset = orders.sumOf { it.totalAmount }
                    val totalTx = orders.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Total Omset", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Rp ${formatRupiah(totalOmset)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Total Transaksi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$totalTx Transaksi",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Metode Bayar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Cash & QRIS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Success Dialog
    if (showPaymentSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = false },
            title = {
                Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Pembayaran sebesar Rp ${formatRupiah(lastPaidAmount)} telah diterima.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Struk telah dicatat di sistem tanpa aktivasi perangkat.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPaymentSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val str = amount.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        sb.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return sb.reverse().toString()
}
