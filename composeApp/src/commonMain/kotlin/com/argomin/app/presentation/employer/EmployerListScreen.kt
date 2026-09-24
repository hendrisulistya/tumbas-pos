package com.argomin.app.presentation.employer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.Employee
import com.argomin.app.presentation.theme.*

@Composable
fun EmployerListScreen(employees: List<Employee>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Data Karyawan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = {},
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan700,
                    contentColor = White
                )
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = White)
                Spacer(Modifier.width(6.dp))
                Text("Tambah Karyawan", color = White)
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(employees) { emp ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Neutral200, RoundedCornerShape(8.dp)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Cyan100, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(emp.name.take(1), fontWeight = FontWeight.Bold, color = Cyan800) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Role: ${emp.role} • No HP: ${emp.phone}", fontSize = 12.sp, color = Gray600)
                            }
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = SuccessContainer, border = BorderStroke(1.dp, SuccessBorder)) {
                            Text(emp.status, fontSize = 11.sp, color = SuccessBase, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
