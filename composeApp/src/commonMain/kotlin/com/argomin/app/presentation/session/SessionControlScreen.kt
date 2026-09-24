package com.argomin.app.presentation.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.MenuItem
import com.argomin.app.domain.model.OrderRecord
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.formatRupiah

@Composable
fun SessionControlScreen() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Kontrol Sesi Kasir Harian", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(10.dp), color = Blue50, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Sesi Operasional Sedang Berjalan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Dibuka pukul 08:00 oleh Budi Santoso (Modal Awal: Rp 300.000)", fontSize = 12.sp, color = Gray600)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Informasi Sesi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("• Total Transaksi Sesi Ini: 4 Transaksi", fontSize = 13.sp, color = Gray800)
            Text("• Total Pembayaran Diterima: Rp 285.000", fontSize = 13.sp, color = Gray800)
            Text("• Kasir yang Bertugas: Rahmat Hidayat & Siti Rahma", fontSize = 13.sp, color = Gray800)
        }
    }
}

@Composable
fun EndOfDayScreen(menuList: List<MenuItem>, orderList: List<OrderRecord>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Prosedur Tutup Hari (End of Day)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Rekapitulasi total omset kasir dan pencatatan sisa hidangan etalase.", fontSize = 12.sp, color = Gray600)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(16.dp))

            val omset = orderList.sumOf { it.totalAmount }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Blue50) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Omset Hari Ini", fontSize = 12.sp, color = Gray600)
                        Text(formatRupiah(omset), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Blue600)
                    }
                }
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFE8F5E9)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Transaksi", fontSize = 12.sp, color = Gray600)
                        Text("${orderList.size} Pesanan Selesai", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Success)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tutup Hari & Finalisasi Sesi", fontWeight = FontWeight.Bold)
            }
        }
    }
}
