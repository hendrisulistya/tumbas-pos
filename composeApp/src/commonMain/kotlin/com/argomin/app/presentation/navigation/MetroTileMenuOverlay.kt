package com.argomin.app.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
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
    val badge: String? = null
)

data class MetroCategory(
    val name: String,
    val icon: ImageVector,
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
                tiles = listOf(
                    MetroTile(
                        Screen.Home,
                        "Kasir (POS)",
                        "Transaksi cepat & cetak struk",
                        Icons.Default.PointOfSale,
                        badge = "Utama"
                    ),
                    MetroTile(
                        Screen.SalesOrder,
                        "Pesanan Penjualan",
                        "Riwayat order & status bayar",
                        Icons.Default.Receipt
                    ),
                    MetroTile(
                        Screen.SessionCheck,
                        "Sesi Kasir",
                        "Buka/tutup shift kas & modal awal",
                        Icons.Default.Timer
                    )
                )
            ),
            MetroCategory(
                name = "Harian",
                icon = Icons.Default.Storefront,
                tiles = listOf(
                    MetroTile(
                        Screen.Showcase,
                        "Etalase Saji",
                        "Pantau stok hidangan meja saji",
                        Icons.Default.Storefront
                    ),
                    MetroTile(
                        Screen.Ingredient,
                        "Bahan Baku",
                        "Stok bahan dapur & inventaris",
                        Icons.Default.Kitchen
                    ),
                    MetroTile(
                        Screen.EndOfDay,
                        "Tutup Hari",
                        "Rekapitulasi harian & setoran kasir",
                        Icons.AutoMirrored.Filled.EventNote
                    )
                )
            ),
            MetroCategory(
                name = "Data Master",
                icon = Icons.Default.Inventory2,
                tiles = listOf(
                    MetroTile(
                        Screen.DishMaster,
                        "Kelola Menu",
                        "Katalog hidangan, harga & HPP",
                        Icons.Default.RestaurantMenu
                    ),
                    MetroTile(
                        Screen.IngredientMaster,
                        "Kelola Bahan",
                        "Master bahan baku, satuan & biaya",
                        Icons.Default.Inventory2
                    )
                )
            ),
            MetroCategory(
                name = "Manajemen",
                icon = Icons.Default.Assessment,
                tiles = listOf(
                    MetroTile(
                        Screen.EmployerManagement,
                        "Karyawan",
                        "Kelola staf, kasir & hak akses",
                        Icons.Default.People
                    ),
                    MetroTile(
                        Screen.Reporting,
                        "Laporan Penjualan",
                        "Analisis omset, grafik & laba kotor",
                        Icons.Default.Assessment
                    ),
                    MetroTile(
                        Screen.WorkInProcess,
                        "Pekerjaan Berjalan",
                        "Status antrian masak & pesanan",
                        Icons.Default.Pending
                    ),
                    MetroTile(
                        Screen.AuditLog,
                        "Log Audit",
                        "Rekam jejak aktivitas operasional",
                        Icons.Default.History
                    ),
                    MetroTile(
                        Screen.Settings,
                        "Pengaturan Toko",
                        "Profil resto & printer thermal",
                        Icons.Default.Settings
                    )
                )
            )
        )
    }

    // Full-Screen Solid Dimmed Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral950.copy(alpha = 0.96f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {} // Prevent click-through dismissal
        ) {
            // Solid Top Header Bar
            Surface(
                color = Neutral900,
                border = BorderStroke(1.dp, Neutral800),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Cyan700,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "TAMBOOPOS (METRO TILES)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Pilih modul tujuan di bawah untuk berpindah layar seketika",
                                fontSize = 11.5.sp,
                                color = Neutral400
                            )
                        }
                    }

                    // Solid Close Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Neutral800,
                        border = BorderStroke(1.dp, Neutral700),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Tutup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = White
                            )
                        }
                    }
                }
            }

            // Solid Grid Content Area
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                metroCategories.forEach { category ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Cyan400)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = Cyan300,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = category.name.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral200,
                                letterSpacing = 1.sp
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
    // Single solid unified brand color for every tile
    val tileColor = Cyan700

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = tileColor,
        border = if (isSelected) {
            BorderStroke(2.dp, White)
        } else {
            BorderStroke(1.dp, Cyan600)
        },
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Cyan800,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = tile.title,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Module Title, Badge & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tile.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isSelected) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = White
                        ) {
                            Text(
                                text = "AKTIF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Cyan900,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    } else if (tile.badge != null) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Cyan900
                        ) {
                            Text(
                                text = tile.badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = tile.subtitle,
                    fontSize = 11.sp,
                    color = Cyan100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
