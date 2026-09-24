package com.argomin.app.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.PlatformConfig
import com.argomin.app.domain.model.StoreProfile
import com.argomin.app.presentation.kasir.KasirViewModel
import com.argomin.app.presentation.theme.*

@Composable
fun SettingsOverviewScreen(
    kasirViewModel: KasirViewModel? = null,
    storeProfile: StoreProfile = StoreProfile(),
    onUpdateStoreProfile: (StoreProfile) -> Unit = {}
) {
    val state = kasirViewModel?.state

    // State untuk Pajak Resto PB1
    var isPb1Enabled by remember(state?.isPb1Active) {
        mutableStateOf(state?.isPb1Active ?: true)
    }
    var pb1Input by remember(state?.pb1Rate) {
        val initialPercent = if (state != null) (state.pb1Rate * 100).toInt().toString() else "10"
        mutableStateOf(if (initialPercent == "0") "10" else initialPercent)
    }

    // State untuk Pajak Pertambahan Nilai PPN
    var isPpnEnabled by remember(state?.isPpnActive) {
        mutableStateOf(state?.isPpnActive ?: false)
    }
    var ppnInput by remember(state?.ppnRate) {
        val initialPercent = if (state != null) (state.ppnRate * 100).toInt().toString() else "11"
        mutableStateOf(if (initialPercent == "0") "11" else initialPercent)
    }

    // State untuk Form Informasi Toko
    var storeName by remember(storeProfile.name) { mutableStateOf(storeProfile.name) }
    var storeAddress by remember(storeProfile.address) { mutableStateOf(storeProfile.address) }
    var storePhone by remember(storeProfile.phone) { mutableStateOf(storeProfile.phone) }
    var storeTaxId by remember(storeProfile.taxId) { mutableStateOf(storeProfile.taxId) }
    var storeQrisName by remember(storeProfile.qrisMerchantName) { mutableStateOf(storeProfile.qrisMerchantName) }
    var storeQrisNmid by remember(storeProfile.qrisNmid) { mutableStateOf(storeProfile.qrisNmid) }
    var showStoreSavedSuccess by remember { mutableStateOf(false) }

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

            // ── SEKSI 1: PAJAK RESTORAN (PB1) ───────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, if (isPb1Enabled) Cyan300 else Gray300),
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
                                tint = if (isPb1Enabled) Cyan700 else Gray500,
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
                                    if (isPb1Enabled) "Aktif (${pb1Input}%)" else "Non-aktif (0% / Bebas Pajak)",
                                    fontSize = 12.sp,
                                    color = if (isPb1Enabled) Cyan700 else Gray600
                                )
                            }
                        }

                        // Toggle switch PB1
                        Switch(
                            checked = isPb1Enabled,
                            onCheckedChange = { isChecked ->
                                isPb1Enabled = isChecked
                                val rateNum = if (isChecked) (pb1Input.toDoubleOrNull() ?: 10.0) else 0.0
                                kasirViewModel?.setPb1Config(isChecked, rateNum)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Cyan600
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    if (isPb1Enabled) {
                        Text(
                            "Tarif Persentase PB1:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral800
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pb1Input,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(3)
                                pb1Input = clean
                                val rateNum = clean.toDoubleOrNull() ?: 0.0
                                kasirViewModel?.setPb1Config(true, rateNum)
                            },
                            label = { Text("Persentase Pajak PB1", color = Neutral700) },
                            placeholder = { Text("10", color = Gray400) },
                            trailingIcon = {
                                Text(
                                    "%",
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan800,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.fillMaxWidth(0.5f).widthIn(min = 200.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Black,
                                unfocusedTextColor = Black,
                                focusedBorderColor = Cyan600,
                                unfocusedBorderColor = Gray300,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tarif standar PB1 restoran daerah umumnya sebesar 10%.",
                            fontSize = 11.5.sp,
                            color = Gray600
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningContainer,
                            border = BorderStroke(1.dp, WarningBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Pajak Restoran (PB1) dinonaktifkan. Baris perhitungan PB1 otomatis dihilangkan dari pesanan kasir.",
                                    fontSize = 12.sp,
                                    color = OnWarningContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 2: PAJAK PERTAMBAHAN NILAI (PPN) ──────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, if (isPpnEnabled) Cyan300 else Gray300),
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
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = if (isPpnEnabled) Cyan700 else Gray500,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Pajak Pertambahan Nilai (PPN)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Neutral900
                                )
                                Text(
                                    if (isPpnEnabled) "Aktif (${ppnInput}%)" else "Non-aktif (0% / Bebas Pajak)",
                                    fontSize = 12.sp,
                                    color = if (isPpnEnabled) Cyan700 else Gray600
                                )
                            }
                        }

                        // Toggle switch PPN
                        Switch(
                            checked = isPpnEnabled,
                            onCheckedChange = { isChecked ->
                                isPpnEnabled = isChecked
                                val rateNum = if (isChecked) (ppnInput.toDoubleOrNull() ?: 11.0) else 0.0
                                kasirViewModel?.setPpnConfig(isChecked, rateNum)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Cyan600
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    if (isPpnEnabled) {
                        Text(
                            "Tarif Persentase PPN:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral800
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ppnInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(3)
                                ppnInput = clean
                                val rateNum = clean.toDoubleOrNull() ?: 0.0
                                kasirViewModel?.setPpnConfig(true, rateNum)
                            },
                            label = { Text("Persentase PPN", color = Neutral700) },
                            placeholder = { Text("11", color = Gray400) },
                            trailingIcon = {
                                Text(
                                    "%",
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan800,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.fillMaxWidth(0.5f).widthIn(min = 200.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Black,
                                unfocusedTextColor = Black,
                                focusedBorderColor = Cyan600,
                                unfocusedBorderColor = Gray300,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tarif PPN umum nasional saat ini adalah 11%.",
                            fontSize = 11.5.sp,
                            color = Gray600
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningContainer,
                            border = BorderStroke(1.dp, WarningBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Pajak Pertambahan Nilai (PPN) dinonaktifkan. Baris perhitungan PPN otomatis dihilangkan dari pesanan kasir.",
                                    fontSize = 12.sp,
                                    color = OnWarningContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 3: INFORMASI TOKO & IDENTITAS USAHA (EDITABLE) ───────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, Gray300),
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
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Cyan700, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Informasi Toko & Usaha", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                                Text("Ubah identitas toko, alamat, kontak, dan identitas QRIS", fontSize = 12.sp, color = Gray600)
                            }
                        }

                        if (showStoreSavedSuccess) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessContainer,
                                border = BorderStroke(1.dp, SuccessBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessBase, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Perubahan Tersimpan!", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = OnSuccessContainer)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Form Fields (2-Kolom Tata Letak Responsif Tablet)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Kolom Kiri: Info Umum
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = {
                                    storeName = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("Nama Toko / Restoran", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storePhone,
                                onValueChange = {
                                    storePhone = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("No. Telepon / WhatsApp", color = Neutral700) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storeTaxId,
                                onValueChange = {
                                    storeTaxId = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("NPWPD / Tax ID", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )
                        }

                        // Kolom Kanan: Alamat & Data QRIS
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = storeAddress,
                                onValueChange = {
                                    storeAddress = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("Alamat Toko / Restoran", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storeQrisName,
                                onValueChange = {
                                    storeQrisName = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("Nama Merchant QRIS (Struk & Standee)", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storeQrisNmid,
                                onValueChange = {
                                    storeQrisNmid = it
                                    showStoreSavedSuccess = false
                                },
                                label = { Text("NMID QRIS Nasional", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black,
                                    unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600,
                                    unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tombol Simpan Toko
                    Button(
                        onClick = {
                            val updated = storeProfile.copy(
                                name = storeName.trim(),
                                address = storeAddress.trim(),
                                phone = storePhone.trim(),
                                taxId = storeTaxId.trim(),
                                qrisMerchantName = storeQrisName.trim(),
                                qrisNmid = storeQrisNmid.trim()
                            )
                            onUpdateStoreProfile(updated)
                            showStoreSavedSuccess = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan700,
                            contentColor = White
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan Informasi Toko", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 4: INFORMASI LISENSI & PLATFORM ───────────────────────────
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
