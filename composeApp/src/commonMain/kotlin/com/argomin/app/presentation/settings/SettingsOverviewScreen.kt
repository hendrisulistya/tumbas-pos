package com.argomin.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.PlatformConfig
import com.argomin.app.presentation.theme.*

@Composable
fun SettingsOverviewScreen() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Pengaturan tambooPOS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp), color = Cyan50, border = androidx.compose.foundation.BorderStroke(1.dp, Cyan200), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Informasi Lisensi & Platform", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("• Target Platform Aktif: ${PlatformConfig.platformName}", fontSize = 13.sp, color = Neutral700)
                    Text("• Status Aktivasi: ${if (PlatformConfig.requiresActivation) "Wajib Diaktivasi (Mobile)" else "DI-BYPASS (Web Edition)"}", fontSize = 13.sp, color = Neutral700)
                    Text("• Versi Aplikasi: 1.0.0 (KMP Multiplatform)", fontSize = 13.sp, color = Neutral700)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Pengaturan Toko:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Nama Toko: Rumah Makan Padang tambooPOS", fontSize = 13.sp, color = Neutral700)
            Text("Pajak PB1: 10% (Aktif)", fontSize = 13.sp, color = Neutral700)
            Text("Mata Uang: Rupiah (IDR)", fontSize = 13.sp, color = Neutral700)
        }
    }
}
