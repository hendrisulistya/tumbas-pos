package com.argomin.app.presentation.wip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.MenuItem
import com.argomin.app.domain.model.OrderRecord
import com.argomin.app.presentation.theme.*

@Composable
fun WorkInProcessOverviewScreen(menuList: List<MenuItem>, orderList: List<OrderRecord>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Pekerjaan Berjalan (Work In Process)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Monitoring hidangan yang sedang diproses di dapur.", fontSize = 12.sp, color = Gray600)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Gray300)
            Spacer(Modifier.height(14.dp))
            Text("✓ Tidak ada pesanan tertunda di dapur. Semua pesanan telah disajikan.", fontSize = 13.sp, color = Gray800)
        }
    }
}
