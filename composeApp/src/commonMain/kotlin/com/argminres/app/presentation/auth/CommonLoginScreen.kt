package com.argminres.app.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argminres.app.Employee
import com.argminres.app.core.PlatformConfig
import com.argminres.app.ui.theme.*

@Composable
fun CommonLoginScreen(
    employees: List<Employee>,
    onLoginSuccess: (Employee) -> Unit
) {
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize().background(White)) {
        // ── Panel Kiri: Branding TumbasPOS (38% lebar) ─────────────────────
        Box(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
                .background(Blue600),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = White,
                    modifier = Modifier.size(92.dp),
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🍲", fontSize = 44.sp)
                    }
                }

                Text(
                    text = "TumbasPOS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Sistem Kasir Rumah Makan Padang",
                    fontSize = 15.sp,
                    color = Blue100,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Feature Highlights
                listOf(
                    "⚡ Pembayaran Cepat (Cash & QRIS)",
                    "🏬 Etalase Saji & Stok Real-Time",
                    "👥 Multi-Karyawan & Otoritas PIN",
                    "📊 Laporan Omset & Tutup Hari"
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(White, CircleShape)
                        )
                        Text(
                            feature,
                            fontSize = 13.sp,
                            color = Color(0xFFE3F2FD),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x33FFFFFF)
                ) {
                    Text(
                        text = "Platform: ${PlatformConfig.platformName}",
                        fontSize = 11.sp,
                        color = White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ── Panel Kanan: Pemilihan Karyawan & Input PIN (62% lebar) ────────
        Box(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
                .background(Color(0xFFF8F9FA))
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedEmployee == null) {
                        // 1. TAHAP PEMILIHAN KARYAWAN
                        Text(
                            text = "Selamat Datang",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Pilih akun karyawan untuk masuk ke sistem",
                            fontSize = 13.sp,
                            color = Gray600
                        )

                        Spacer(Modifier.height(24.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            employees.forEach { emp ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedEmployee = emp
                                            pin = ""
                                            errorMsg = null
                                        },
                                    color = Blue50,
                                    border = BorderStroke(1.dp, Blue100)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Blue600,
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        emp.name.take(1).uppercase(),
                                                        color = White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    emp.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Black
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (emp.role == "MANAGER") Color(0xFFE8F5E9) else Blue100
                                                ) {
                                                    Text(
                                                        text = emp.role,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (emp.role == "MANAGER") Color(0xFF2E7D32) else Blue700,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray600)
                                    }
                                }
                            }
                        }
                    } else {
                        // 2. TAHAP INPUT PIN
                        val currentEmp = selectedEmployee!!

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                selectedEmployee = null
                                pin = ""
                                errorMsg = null
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Gray600
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Masukkan PIN",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                Text(
                                    text = "${currentEmp.name} (${currentEmp.role})",
                                    fontSize = 13.sp,
                                    color = Blue600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // 4 Pin Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            repeat(4) { idx ->
                                val isFilled = idx < pin.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(if (isFilled) Blue600 else Blue50, CircleShape)
                                        .border(1.5.dp, if (isFilled) Blue700 else Blue100, CircleShape)
                                )
                            }
                        }

                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = Color(0xFFC62828),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Spacer(Modifier.height(18.dp))
                        }

                        // Keypad Numerik (1-9, 0, ⌫)
                        val keypadButtons = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "delete")
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (row in 0 until 4) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (col in 0 until 3) {
                                        val idx = row * 3 + col
                                        val key = keypadButtons[idx]
                                        if (key.isNotEmpty()) {
                                            val isDel = key == "delete"
                                            Surface(
                                                modifier = Modifier
                                                    .size(width = 68.dp, height = 48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        if (isDel) {
                                                            if (pin.isNotEmpty()) {
                                                                pin = pin.dropLast(1)
                                                                errorMsg = null
                                                            }
                                                        } else {
                                                            if (pin.length < 4) {
                                                                val nextPin = pin + key
                                                                pin = nextPin
                                                                errorMsg = null
                                                                if (nextPin.length == 4) {
                                                                    if (nextPin == currentEmp.pin) {
                                                                        onLoginSuccess(currentEmp)
                                                                    } else {
                                                                        errorMsg = "PIN salah! Silakan coba lagi."
                                                                        pin = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                color = if (isDel) Color(0xFFFFEBEE) else Blue50,
                                                border = BorderStroke(1.dp, if (isDel) Color(0xFFEF9A9A) else Blue100)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (isDel) "⌫" else key,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDel) Color(0xFFC62828) else Blue700
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(Modifier.size(width = 68.dp, height = 48.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Petunjuk PIN
                        Text(
                            text = "💡 Petunjuk PIN: Manager (Budi) = 0000 | Kasir / Koki = 1234",
                            fontSize = 11.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        TextButton(onClick = {
                            selectedEmployee = null
                            pin = ""
                            errorMsg = null
                        }) {
                            Text("Bukan ${currentEmp.name}? Ganti Pengguna", color = Blue600, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
