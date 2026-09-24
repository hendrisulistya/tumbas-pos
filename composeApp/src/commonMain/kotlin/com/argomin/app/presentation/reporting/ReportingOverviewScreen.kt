package com.argomin.app.presentation.reporting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.OrderRecord
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.formatRupiah

@Composable
fun ReportingOverviewScreen(orderList: List<OrderRecord>) {
    val totalOmset = orderList.sumOf { it.totalAmount }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Laporan Penjualan & Performa Toko", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Total Penjualan", fontSize = 12.sp, color = Neutral600)
                    Text(formatRupiah(totalOmset), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cyan700, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Transaksi Selesai", fontSize = 12.sp, color = Gray600)
                    Text("${orderList.size} Pesanan", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Success, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Rata-rata Keranjang", fontSize = 12.sp, color = Gray600)
                    val avg = if (orderList.isNotEmpty()) totalOmset / orderList.size else 0L
                    Text(formatRupiah(avg), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Warning, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
