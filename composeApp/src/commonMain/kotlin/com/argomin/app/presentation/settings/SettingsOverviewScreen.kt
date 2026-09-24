package com.argomin.app.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import com.argomin.app.core.PlatformConfig
import com.argomin.app.domain.model.StoreProfile
import com.argomin.app.presentation.components.BarcodeScannerView
import com.argomin.app.presentation.components.rememberQrisImagePickerLauncher
import com.argomin.app.presentation.kasir.KasirViewModel
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.validateAndParseQris

@Composable
fun SettingsOverviewScreen(
    kasirViewModel: KasirViewModel? = null,
    storeProfile: StoreProfile = StoreProfile(),
    canEdit: Boolean = true,
    userRole: String = "MANAGER",
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

    // State untuk QRIS Merchant (Base: Valid Image QRIS)
    var storeQrisName by remember(storeProfile.qrisMerchantName) { mutableStateOf(storeProfile.qrisMerchantName) }
    var storeQrisNmid by remember(storeProfile.qrisNmid) { mutableStateOf(storeProfile.qrisNmid) }
    var storeQrisCity by remember(storeProfile.qrisCity) { mutableStateOf(storeProfile.qrisCity) }
    var storeQrisPayload by remember(storeProfile.qrisPayload) { mutableStateOf(storeProfile.qrisPayload) }
    var isQrisVerified by remember(storeProfile.isQrisVerified) { mutableStateOf(storeProfile.isQrisVerified) }

    // Dialog & Feedback state
    var showCameraScanner by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var validationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    var showStoreSavedSuccess by remember { mutableStateOf(false) }

    // Fungsi pemroses dan validasi payload QRIS
    fun processQris(raw: String) {
        showStoreSavedSuccess = false
        val result = validateAndParseQris(raw)
        if (result.isValid) {
            storeQrisName = result.merchantName
            storeQrisNmid = result.nmid
            storeQrisCity = result.city.ifBlank { "INDONESIA" }
            storeQrisPayload = result.rawPayload
            isQrisVerified = true
            validationSuccessMessage = "QRIS Valid & Berhasil Diverifikasi! Merchant: '${result.merchantName}', NMID: '${result.nmid}', Tipe: ${if (result.isDynamic) "Dinamis" else "Statis"}."
            validationErrorMessage = null
        } else {
            validationErrorMessage = result.errorMessage ?: "Format QRIS tidak memenuhi standar Bank Indonesia."
            validationSuccessMessage = null
        }
    }

    // Launcher untuk pilih file gambar QRIS
    val launchImagePicker = rememberQrisImagePickerLauncher(
        onQrDecoded = { decodedString ->
            processQris(decodedString)
        },
        onError = { err ->
            validationErrorMessage = err
            validationSuccessMessage = null
        }
    )

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
            // Header Bar & Access Role Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pengaturan Sistem & Toko", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                    Text("Konfigurasi perpajakan, profil operasional, dan integrasi QRIS", fontSize = 12.5.sp, color = Gray600)
                }

                if (canEdit) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Cyan50,
                        border = BorderStroke(1.dp, Cyan200)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Cyan700, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Akses Manager ($userRole)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan900)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = WarningContainer,
                        border = BorderStroke(1.dp, WarningBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mode Baca Saja (Kasir)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnWarningContainer)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(18.dp))

            // JIKA KASIR LOGIN: Banner Peringatan Hak Akses
            if (!canEdit) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarningContainer,
                    border = BorderStroke(1.dp, WarningBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Warning, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Akses Dibatasi: Hanya Manager yang dapat mengubah konfigurasi",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnWarningContainer
                            )
                            Text(
                                "Anda sedang login sebagai kasir ($userRole). Semua tombol ubah dan simpan dinonaktifkan untuk mencegah perubahan data tanpa otorisasi.",
                                fontSize = 12.sp,
                                color = OnWarningContainer
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

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
                                Text("Pajak Restoran (PB1)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                                Text(
                                    if (isPb1Enabled) "Aktif (${pb1Input}%)" else "Non-aktif (0% / Bebas Pajak)",
                                    fontSize = 12.sp,
                                    color = if (isPb1Enabled) Cyan700 else Gray600
                                )
                            }
                        }

                        Switch(
                            checked = isPb1Enabled,
                            enabled = canEdit,
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
                        Text("Tarif Persentase PB1:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Neutral800)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pb1Input,
                            enabled = canEdit,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(3)
                                pb1Input = clean
                                val rateNum = clean.toDoubleOrNull() ?: 0.0
                                kasirViewModel?.setPb1Config(true, rateNum)
                            },
                            label = { Text("Persentase Pajak PB1", color = Neutral700) },
                            placeholder = { Text("10", color = Gray400) },
                            trailingIcon = {
                                Text("%", fontWeight = FontWeight.Bold, color = Cyan800, fontSize = 15.sp, modifier = Modifier.padding(end = 12.dp))
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
                        Text("Tarif standar PB1 restoran daerah umumnya sebesar 10%.", fontSize = 11.5.sp, color = Gray600)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningContainer,
                            border = BorderStroke(1.dp, WarningBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                Text("Pajak Pertambahan Nilai (PPN)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                                Text(
                                    if (isPpnEnabled) "Aktif (${ppnInput}%)" else "Non-aktif (0% / Bebas Pajak)",
                                    fontSize = 12.sp,
                                    color = if (isPpnEnabled) Cyan700 else Gray600
                                )
                            }
                        }

                        Switch(
                            checked = isPpnEnabled,
                            enabled = canEdit,
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
                        Text("Tarif Persentase PPN:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Neutral800)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ppnInput,
                            enabled = canEdit,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(3)
                                ppnInput = clean
                                val rateNum = clean.toDoubleOrNull() ?: 0.0
                                kasirViewModel?.setPpnConfig(true, rateNum)
                            },
                            label = { Text("Persentase PPN", color = Neutral700) },
                            placeholder = { Text("11", color = Gray400) },
                            trailingIcon = {
                                Text("%", fontWeight = FontWeight.Bold, color = Cyan800, fontSize = 15.sp, modifier = Modifier.padding(end = 12.dp))
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
                        Text("Tarif PPN umum nasional saat ini adalah 11%.", fontSize = 11.5.sp, color = Gray600)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningContainer,
                            border = BorderStroke(1.dp, WarningBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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

            // ── SEKSI 3: INFORMASI UMUM TOKO (NAMA, KONTAK, ALAMAT) ─────────────
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
                                Text("Informasi Operasional Toko", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                                Text("Nama usaha, nomor telepon kontak, alamat, dan NPWPD", fontSize = 12.sp, color = Gray600)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Kolom Kiri
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = storeName,
                                enabled = canEdit,
                                onValueChange = { storeName = it; showStoreSavedSuccess = false },
                                label = { Text("Nama Toko / Restoran", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black, unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White, unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storePhone,
                                enabled = canEdit,
                                onValueChange = { storePhone = it; showStoreSavedSuccess = false },
                                label = { Text("No. Telepon / WhatsApp", color = Neutral700) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black, unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White, unfocusedContainerColor = White
                                )
                            )
                        }

                        // Kolom Kanan
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = storeAddress,
                                enabled = canEdit,
                                onValueChange = { storeAddress = it; showStoreSavedSuccess = false },
                                label = { Text("Alamat Toko / Restoran", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black, unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White, unfocusedContainerColor = White
                                )
                            )

                            OutlinedTextField(
                                value = storeTaxId,
                                enabled = canEdit,
                                onValueChange = { storeTaxId = it; showStoreSavedSuccess = false },
                                label = { Text("NPWPD / Tax ID", color = Neutral700) },
                                singleLine = true,
                                textStyle = TextStyle(color = Black, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Black, unfocusedTextColor = Black,
                                    focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                    focusedContainerColor = White, unfocusedContainerColor = White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 4: INTEGRASI QRIS BERBASIS IMAGE QRIS VALID (ANTI-SALAH INPUT) ──
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, if (isQrisVerified) Cyan300 else Gray300),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header QRIS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Cyan700, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Data Merchant QRIS Nasional", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Neutral900)
                                Text("Identitas merchant dan NMID diekstrak otomatis dari QR Code valid", fontSize = 12.sp, color = Gray600)
                            }
                        }

                        if (isQrisVerified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessContainer,
                                border = BorderStroke(1.dp, SuccessBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessBase, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("QRIS Terverifikasi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = OnSuccessContainer)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Feedback Pesan Validasi Sukses / Error
                    if (validationSuccessMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessContainer,
                            border = BorderStroke(1.dp, SuccessBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessBase, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(validationSuccessMessage!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSuccessContainer)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (validationErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorContainer,
                            border = BorderStroke(1.dp, ErrorBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = ErrorBase, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(validationErrorMessage!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnErrorContainer)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Card Fields Read-Only (Terkunci dari Kesalahan Input Manual)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Nama Merchant (Read-Only)
                        OutlinedTextField(
                            value = storeQrisName,
                            onValueChange = {},
                            readOnly = true,
                            enabled = canEdit,
                            label = { Text("Nama Merchant QRIS (Otomatis)", color = Neutral700) },
                            trailingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = Cyan700, modifier = Modifier.size(18.dp))
                            },
                            supportingText = {
                                Text("🔒 Diekstrak dari Tag 59 QRIS valid", fontSize = 11.sp, color = Gray600)
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Black, unfocusedTextColor = Black,
                                disabledTextColor = Black,
                                focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                focusedContainerColor = Cyan50.copy(alpha = 0.4f),
                                unfocusedContainerColor = Cyan50.copy(alpha = 0.4f)
                            )
                        )

                        // NMID Nasional (Read-Only)
                        OutlinedTextField(
                            value = storeQrisNmid,
                            onValueChange = {},
                            readOnly = true,
                            enabled = canEdit,
                            label = { Text("NMID Nasional (Otomatis)", color = Neutral700) },
                            trailingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = Cyan700, modifier = Modifier.size(18.dp))
                            },
                            supportingText = {
                                Text("🔒 Diekstrak dari Tag 51 QRIS valid", fontSize = 11.sp, color = Gray600)
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Black, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Black, unfocusedTextColor = Black,
                                disabledTextColor = Black,
                                focusedBorderColor = Cyan600, unfocusedBorderColor = Gray300,
                                focusedContainerColor = Cyan50.copy(alpha = 0.4f),
                                unfocusedContainerColor = Cyan50.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Kotak Aksi Upload / Scan QRIS
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Gray100,
                        border = BorderStroke(1.dp, Gray300),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Perbarui Data Merchant dari Gambar / Scan QRIS:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral800
                            )
                            Text(
                                "Unggah gambar QR code QRIS resmi toko Anda (JPG/PNG). Sistem akan memvalidasi header '000201', checksum CRC16 EMVCo, dan mencocokkan kode negara Indonesia sebelum memperbarui data.",
                                fontSize = 11.5.sp,
                                color = Gray600,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Tombol Unggah File Gambar
                                Button(
                                    onClick = { launchImagePicker() },
                                    enabled = canEdit,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Cyan700, contentColor = White)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Unggah Gambar QRIS", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }

                                // 2. Tombol Scan Kamera
                                OutlinedButton(
                                    onClick = { showCameraScanner = true },
                                    enabled = canEdit,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan800)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Scan Kamera", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // 3. Tombol Tempel Payload String
                                OutlinedButton(
                                    onClick = { showPasteDialog = true },
                                    enabled = canEdit,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Neutral700)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Tempel Payload", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── TOMBOL SIMPAN PENGATURAN TOKO ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showStoreSavedSuccess) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessContainer,
                        border = BorderStroke(1.dp, SuccessBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessBase, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Informasi Toko Berhasil Disimpan!", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = OnSuccessContainer)
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        val updated = storeProfile.copy(
                            name = storeName.trim(),
                            address = storeAddress.trim(),
                            phone = storePhone.trim(),
                            taxId = storeTaxId.trim(),
                            qrisMerchantName = storeQrisName.trim(),
                            qrisNmid = storeQrisNmid.trim(),
                            qrisPayload = storeQrisPayload.trim(),
                            qrisCity = storeQrisCity.trim(),
                            isQrisVerified = isQrisVerified
                        )
                        onUpdateStoreProfile(updated)
                        showStoreSavedSuccess = true
                    },
                    enabled = canEdit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan700,
                        contentColor = White,
                        disabledContainerColor = Gray300,
                        disabledContentColor = Gray500
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Simpan Informasi Toko", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── SEKSI 5: INFORMASI LISENSI & PLATFORM ───────────────────────────
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

    // Modal Dialog Pemindai Kamera Barcode
    if (showCameraScanner) {
        Dialog(onDismissRequest = { showCameraScanner = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = White,
                modifier = Modifier.size(width = 460.dp, height = 480.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pindai QRIS dengan Kamera", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Neutral900)
                        IconButton(onClick = { showCameraScanner = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        BarcodeScannerView(
                            modifier = Modifier.fillMaxSize(),
                            onBarcodeScanned = { scanned ->
                                showCameraScanner = false
                                processQris(scanned)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog Tempel Payload String QRIS
    if (showPasteDialog) {
        var pasteText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPasteDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = White,
                modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Tempel Payload QRIS (Statis / Dinamis)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Neutral900)
                    Text(
                        "Masukkan payload teks string QRIS (EMVCo) dari hasil scan atau screenshot QR:",
                        fontSize = 12.5.sp,
                        color = Gray600
                    )
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        placeholder = { Text("Contoh: 00020101021126610014COM.GO-JEK...", fontSize = 12.sp, color = Gray400) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5,
                        textStyle = TextStyle(fontSize = 12.sp, color = Black)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        OutlinedButton(onClick = { showPasteDialog = false }) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                showPasteDialog = false
                                processQris(pasteText)
                            },
                            enabled = pasteText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan700, contentColor = White)
                        ) {
                            Text("Validasi & Terapkan")
                        }
                    }
                }
            }
        }
    }
}
