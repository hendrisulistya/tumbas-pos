package com.argomin.app.presentation.kasir

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.MenuItem
import com.argomin.app.domain.model.OrderRecord
import com.argomin.app.domain.model.PosCartItem
import com.argomin.app.presentation.components.DishImage
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.DynamicQrisCard
import com.argomin.app.util.formatNumber
import com.argomin.app.util.formatRupiah
import com.argomin.app.util.generateDynamicQris

@Composable
fun PosCashierScreen(
    menuList: List<MenuItem>,
    viewModel: KasirViewModel = remember { KasirViewModel() },
    onOrderPaid: (OrderRecord) -> Unit
) {
    val state = viewModel.state
    val categories = listOf("Semua", "Makanan", "Minuman", "Paket", "Lain-lain")
    val filteredList = viewModel.filterMenu(menuList)

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Catalog
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp)) {
            // Header Bar: Search input + Category Filter Chips in a neat 2-tier arrangement
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                // Tier 1: Search Input with clear icon & count badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari hidangan masakan Padang...", style = MaterialTheme.typography.bodyMedium, color = Gray600) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = Cyan600,
                            unfocusedBorderColor = Gray300
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Cyan700, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, "Hapus pencarian", tint = Gray600, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )

                    Spacer(Modifier.width(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Cyan50,
                        border = BorderStroke(1.dp, Cyan100)
                    ) {
                        Text(
                            text = "${filteredList.size} Menu",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Cyan800,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Tier 2: Category Filter Chips with horizontal scroll support
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories) { cat ->
                        val isSel = state.selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) Cyan600 else White,
                            border = BorderStroke(1.dp, if (isSel) Cyan600 else Gray300),
                            shadowElevation = if (isSel) 2.dp else 0.dp,
                            modifier = Modifier.clickable { viewModel.setSelectedCategory(cat) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSel) White else Gray800
                                )
                            }
                        }
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
                            viewModel.addToCart(item)
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
                            Text(formatRupiah(item.price), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cyan700, modifier = Modifier.padding(top = 4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (item.stock > 10) Cyan50 else ErrorContainer,
                                border = BorderStroke(1.dp, if (item.stock > 10) Cyan200 else ErrorBorder),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text("Stok: ${item.stock}", fontSize = 11.sp, color = if (item.stock > 10) Cyan800 else ErrorBase, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Cyan50,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ShoppingCart, null, tint = Cyan700, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Pesanan Baru", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "${viewModel.cartItemCount} item dipilih",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }
                    }

                    if (state.cart.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearCart() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Kosongkan", style = MaterialTheme.typography.labelSmall, color = Error)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Gray300)

                if (state.cart.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = Neutral400
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Keranjang Kosong", color = Gray600, fontSize = 13.sp)
                            Text("Pilih hidangan di samping", color = Gray600, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.cart) { ci ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Cyan50, RoundedCornerShape(8.dp))
                                    .border(1.dp, Cyan200, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
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
                                        Text(formatRupiah(ci.item.price * ci.quantity), fontSize = 11.sp, color = Cyan700)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = White, modifier = Modifier.size(24.dp).clickable {
                                        viewModel.removeFromCart(ci.item)
                                    }) { Box(contentAlignment = Alignment.Center) { Text("-", fontWeight = FontWeight.Bold) } }
                                    Text("${ci.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    Surface(shape = CircleShape, color = White, modifier = Modifier.size(24.dp).clickable { viewModel.addToCart(ci.item) }) {
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
                    Text(formatRupiah(state.subtotal), fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pajak PB1 (10%)", fontSize = 12.sp, color = Gray600)
                    Text(formatRupiah(state.pajak), fontSize = 12.sp)
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(formatRupiah(state.totalTagihan), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Cyan700)
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        viewModel.openPaymentDialog()
                    },
                    enabled = viewModel.canPay,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan700,
                        contentColor = White
                    )
                ) {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold, color = White)
                }
            }
        }
    }

    // ── Dialog: Pilihan Metode Pembayaran (Tunai / QRIS) ──────────────────────
    if (state.isPaymentDialogOpen) {
        val isTunai = state.paymentMethod is PaymentMethod.Tunai

        AlertDialog(
            onDismissRequest = { viewModel.closePaymentDialog() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pilih Metode Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Cyan900
                    )
                    IconButton(
                        onClick = { viewModel.closePaymentDialog() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Gray600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 360.dp, max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Segmented Selector: Tunai vs QRIS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Opsi Tunai
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectPaymentMethod(PaymentMethod.Tunai) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTunai) Cyan50 else White,
                            border = BorderStroke(
                                width = if (isTunai) 2.dp else 1.dp,
                                color = if (isTunai) Cyan600 else Gray300
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (isTunai) Cyan700 else Gray600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Tunai",
                                    fontWeight = if (isTunai) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTunai) Cyan900 else Gray800,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Opsi QRIS
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectPaymentMethod(PaymentMethod.Qris) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isTunai) Cyan50 else White,
                            border = BorderStroke(
                                width = if (!isTunai) 2.dp else 1.dp,
                                color = if (!isTunai) Cyan600 else Gray300
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (!isTunai) Cyan700 else Gray600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "QRIS",
                                    fontWeight = if (!isTunai) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isTunai) Cyan900 else Gray800,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (!isTunai) {
                        // Tampilan QRIS Dinamis (Nominal Pesanan + Fee Rp 1.000)
                        val dynamicQris = remember(state.totalTagihan) { generateDynamicQris(state.totalTagihan, fee = KasirViewModel.QRIS_FEE) }
                        DynamicQrisCard(qrisData = dynamicQris)
                    } else {
                        // Tampilan Tunai
                        // 1. Total Pesanan Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Cyan50,
                            border = BorderStroke(1.dp, Cyan200)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "TOTAL PESANAN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gray600,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        formatRupiah(state.totalTagihan),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Cyan900
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Cyan100
                                ) {
                                    Text(
                                        "${viewModel.cartItemCount} Porsi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Cyan800,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // 2. Input Dibayarkan
                        OutlinedTextField(
                            value = state.cashPaidInput,
                            onValueChange = { viewModel.setCashPaidInput(it) },
                            label = { Text("Nominal Dibayarkan") },
                            placeholder = { Text("0") },
                            leadingIcon = {
                                Text(
                                    "Rp",
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan700,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            trailingIcon = {
                                if (state.cashPaidInput.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearCashInput() }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Hapus",
                                            tint = Gray600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedBorderColor = Cyan600,
                                unfocusedBorderColor = Gray400
                            )
                        )

                        // 3. Tombol Shortcut Pecahan Uang (1.000, 2.000, 5.000, 10.000, 20.000, 50.000, 100.000, Uang Pas)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Shortcut Pecahan Uang:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray600
                                )
                                if (state.cashPaidInput.isNotEmpty() && state.cashPaidInput != "0") {
                                    Text(
                                        "Reset (C)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorBase,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { viewModel.clearCashInput() }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))

                            val denominationsRow1 = listOf(1_000L, 2_000L, 5_000L, 10_000L)
                            val denominationsRow2 = listOf(20_000L, 50_000L, 100_000L)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                denominationsRow1.forEach { denom ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.addDenomination(denom) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gray100,
                                        border = BorderStroke(1.dp, Gray300)
                                    ) {
                                        Text(
                                            text = formatNumber(denom),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Cyan900,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                denominationsRow2.forEach { denom ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.addDenomination(denom) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gray100,
                                        border = BorderStroke(1.dp, Gray300)
                                    ) {
                                        Text(
                                            text = formatNumber(denom),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Cyan900,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                                // Uang Pas Button
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setUangPas() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Cyan100,
                                    border = BorderStroke(1.dp, Cyan300)
                                ) {
                                    Text(
                                        text = "Uang Pas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cyan800,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        // 4. Nominal Kembalian Card
                        if (state.dibayarkan > 0L) {
                            if (!viewModel.isCashSufficient) {
                                val deficit = state.totalTagihan - state.dibayarkan
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = ErrorContainer,
                                    border = BorderStroke(1.dp, ErrorBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "Uang Kurang:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = OnErrorContainer
                                            )
                                        }
                                        Text(
                                            formatRupiah(deficit),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorBase
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = SuccessContainer,
                                    border = BorderStroke(1.dp, SuccessBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Success,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "Kembalian:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OnSuccessContainer
                                            )
                                        }
                                        Text(
                                            formatRupiah(state.kembalian),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SuccessBase
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Gray100,
                                border = BorderStroke(1.dp, Gray300)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Nominal Kembalian:",
                                        fontSize = 12.sp,
                                        color = Gray600
                                    )
                                    Text(
                                        "Rp 0",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Gray600
                                    )
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.closePaymentDialog() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal")
                }
            },
            confirmButton = {
                if (isTunai) {
                    Button(
                        onClick = {
                            if (viewModel.isCashSufficient) {
                                val summary = state.cart.joinToString(", ") { "${it.quantity}x ${it.item.name}" }
                                val orderId = "#ORD-${(1000..9999).random()}"
                                onOrderPaid(
                                    OrderRecord(
                                        id = orderId,
                                        time = "14:50",
                                        cashier = "Rahmat Hidayat",
                                        totalAmount = state.totalTagihan,
                                        paymentMethod = "Tunai",
                                        status = "Selesai",
                                        itemsSummary = summary
                                    )
                                )
                                viewModel.completePayment(orderId)
                            }
                        },
                        enabled = viewModel.isCashSufficient,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan700,
                            contentColor = White
                        )
                    ) {
                        Text("Konfirmasi & Bayar", fontWeight = FontWeight.Bold, color = White)
                    }
                } else {
                    val dynamicQris = remember(state.totalTagihan) { generateDynamicQris(state.totalTagihan, fee = KasirViewModel.QRIS_FEE) }
                    Button(
                        onClick = {
                            val summary = state.cart.joinToString(", ") { "${it.quantity}x ${it.item.name}" }
                            val orderId = "#ORD-${(1000..9999).random()}"
                            onOrderPaid(
                                OrderRecord(
                                    id = orderId,
                                    time = "14:50",
                                    cashier = "Rahmat Hidayat",
                                    totalAmount = dynamicQris.totalAmount,
                                    paymentMethod = "QRIS Dinamis",
                                    status = "Selesai",
                                    itemsSummary = summary
                                )
                            )
                            viewModel.completePayment(orderId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan700,
                            contentColor = White
                        )
                    ) {
                        Text("Sudah Bayar & Selesai", fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
        )
    }

    // ── Dialog: Sukses Pembayaran ──────────────────────────────────────────────
    if (state.isSuccessDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                viewModel.closeSuccessDialog()
                viewModel.resetAfterTransaction()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(SuccessContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "Transaksi Berhasil!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Pembayaran telah selesai dan pesanan telah dicatat.",
                        fontSize = 12.sp,
                        color = Neutral600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Neutral50,
                        border = BorderStroke(1.dp, Neutral200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Tagihan", fontSize = 12.sp, color = Neutral600)
                                Text(formatRupiah(state.lastOrderTotal), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Metode Bayar", fontSize = 12.sp, color = Neutral600)
                                Text(state.lastPaymentMethod, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (state.lastPaymentMethod == "Tunai") {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Uang Diterima", fontSize = 12.sp, color = Neutral600)
                                    Text(formatRupiah(state.lastPaidAmount), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Neutral200)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kembalian", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Cyan900)
                                    Text(
                                        formatRupiah(state.lastChangeAmount),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessBase
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fee Transaksi QRIS", fontSize = 12.sp, color = Neutral600)
                                    Text("Rp 1.000", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cyan700)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Neutral200)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Cyan900)
                                    Text(
                                        "LUNAS (Terverifikasi)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessBase
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeSuccessDialog()
                        viewModel.resetAfterTransaction()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan700,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cetak Struk & Selesai", fontWeight = FontWeight.Bold, color = White)
                }
            }
        )
    }
}
