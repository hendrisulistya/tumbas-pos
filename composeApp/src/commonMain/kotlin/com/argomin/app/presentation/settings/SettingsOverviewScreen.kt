package com.argomin.app.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.PlatformConfig
import com.argomin.app.presentation.kasir.KasirViewModel
import com.argomin.app.presentation.theme.*

@Composable
fun SettingsOverviewScreen(kasirViewModel: KasirViewModel? = null) {
    // Ambil rate dari viewModel jika tersedia (misal 0.10 -> 10.0), default 10.0
    val currentRate = kasirViewModel?.state?.taxRate ?: 0.10
    var activeTaxPercent by remember(currentRate) { mutableStateOf((currentRate * 100).toInt()) }
    var customTaxInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Text("Pengaturan Sistem & Toko", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Neutral900)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(18.dp))

            // ── SEKSI 1: KONFIGURASI PAJAK RESTORAN (PB1) ──────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, if (activeTaxPercent > 0) Cyan300 else Gray300),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Percent,
                                contentDescription = null,
                                tint = if (activeTaxPercent > 0) Cyan700 else Gray500,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Pajak Restoran (PB1)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Neutral900
                                )
                                Text(
                                    if (activeTaxPercent > 0) "Status: Aktif ($activeTaxPercent%)" else "Status: Non-aktif (0% / Bebas Pajak)",
                                    fontSize = 12.sp,
                                    color = if (activeTaxPercent > 0) Cyan700 else Gray600
                                )
                            }
                        }

                        // Switch toggle aktifkan / non-aktifkan PB1
                        Switch(
                            checked = activeTaxPercent > 0,
                            onCheckedChange = { isChecked ->
                                val newPercent = if (isChecked) 10 else 0
                                activeTaxPercent = newPercent
                                kasirViewModel?.setTaxRatePercent(newPercent.toDouble())
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Cyan600
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Pilih Tarif Pajak PB1:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Neutral800)
                    Spacer(Modifier.height(8.dp))

                    // Preset buttons
                    val presets = listOf(
                        0 to "0% (Bebas Pajak)",
                        5 to "5%",
                        10 to "10% (Standar PB1)",
                        11 to "11% (PPN)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { (rate, label) ->
                            val isSelected = activeTaxPercent == rate
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        activeTaxPercent = rate
                                        customTaxInput = ""
                                        kasirViewModel?.setTaxRatePercent(rate.toDouble())
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Cyan700 else Neutral100,
                                border = BorderStroke(1.dp, if (isSelected) Cyan700 else Gray300)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) White else Neutral800,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Keterangan aturan 0%
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTaxPercent == 0) WarningContainer else Cyan50,
                        border = BorderStroke(1.dp, if (activeTaxPercent == 0) WarningBorder else Cyan200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = if (activeTaxPercent == 0) Warning else Cyan700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (activeTaxPercent == 0) {
                                    "Pajak PB1 bernilai 0%. Baris perhitungan pajak telah otomatis dihilangkan dari rincian pesanan kasir dan struk transaksi."
                                } else {
                                    "Pajak PB1 sebesar $activeTaxPercent% diterapkan pada setiap transaksi kasir dihitung dari subtotal pesanan."
                                },
                                fontSize = 12.sp,
                                color = if (activeTaxPercent == 0) OnWarningContainer else Cyan900,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 2: INFORMASI TOKO ────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, Gray300),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Cyan700, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Informasi Toko", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("• Nama Toko: Rumah Makan Padang tambooPOS", fontSize = 13.sp, color = Neutral700)
                    Text("• NPWPD / Tax ID: 01.234.567.8-901.000", fontSize = 13.sp, color = Neutral700)
                    Text("• Mata Uang: Rupiah (IDR)", fontSize = 13.sp, color = Neutral700)
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 3: INFORMASI LISENSI & PLATFORM ───────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Cyan50,
                border = BorderStroke(1.dp, Cyan200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Informasi Lisensi & Platform", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Cyan950)
                    Spacer(Modifier.height(6.dp))
                    Text("• Target Platform Aktif: ${PlatformConfig.platformName}", fontSize = 13.sp, color = Neutral700)
                    Text("• Status Aktivasi: ${if (PlatformConfig.requiresActivation) "Wajib Diaktivasi (Mobile)" else "DI-BYPASS (Web Edition)"}", fontSize = 13.sp, color = Neutral700)
                    Text("• Versi Aplikasi: 1.0.0 (KMP Multiplatform)", fontSize = 13.sp, color = Neutral700)
                }
            }
        }
    }
}
