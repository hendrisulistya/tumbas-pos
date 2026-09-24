package com.argomin.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.Screen
import com.argomin.app.presentation.theme.*

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
            .background(Color(0xF50F172A))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {}
        ) {
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
