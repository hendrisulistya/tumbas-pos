package com.argomin.app.presentation.audit

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.AuditEntry
import com.argomin.app.presentation.theme.*

@Composable
fun AuditLogOverviewScreen(audits: List<AuditEntry>) {
    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Log Audit Aktivitas Sistem", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(audits) { a ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(a.time, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue600, modifier = Modifier.width(60.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${a.user} • ${a.details}", fontSize = 11.sp, color = Gray600)
                        }
                    }
                }
            }
        }
    }
}
