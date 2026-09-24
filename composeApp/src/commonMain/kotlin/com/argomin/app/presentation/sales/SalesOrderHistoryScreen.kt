package com.argomin.app.presentation.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.OrderRecord
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.formatRupiah

@Composable
fun SalesOrderHistoryScreen(orders: List<OrderRecord>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Daftar Struk & Pesanan Penjualan", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { ord ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Neutral50, RoundedCornerShape(10.dp))
                            .border(1.dp, Neutral200, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ord.id, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.width(10.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = SuccessContainer, border = BorderStroke(1.dp, SuccessBorder)) {
                                    Text(ord.status, fontSize = 11.sp, color = SuccessBase, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(ord.itemsSummary, fontSize = 12.sp, color = Neutral600, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                            Text("Kasir: ${ord.cashier} • Jam: ${ord.time} • Metode: ${ord.paymentMethod}", fontSize = 11.sp, color = Neutral600, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(formatRupiah(ord.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cyan700)
                    }
                }
            }
        }
    }
}
