package com.argomin.app.presentation.showcase

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.domain.model.MenuItem
import com.argomin.app.presentation.components.DishImage
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.formatRupiah

@Composable
fun ShowcaseStockScreen(menuList: List<MenuItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Etalase Saji - Stok Siap Santap", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuList) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Cyan50)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DishImage(
                                    imagePath = item.image,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    fallbackEmoji = item.emoji
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(formatRupiah(item.price), fontSize = 12.sp, color = Cyan700)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Tersedia di Etalase:", fontSize = 11.sp, color = Neutral600)
                                Text("${item.stock} Porsi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cyan800)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DishMasterManagementScreen(menuList: MutableList<MenuItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Master Menu Hidangan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { /* Tambah menu */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan700,
                    contentColor = White
                )
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = White)
                Spacer(Modifier.width(6.dp))
                Text("Tambah Menu Baru", color = White)
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(menuList) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Neutral200, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DishImage(
                                imagePath = item.image,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                fallbackEmoji = item.emoji
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Kategori: ${item.category} • Modal: ${formatRupiah(item.cost)}", fontSize = 12.sp, color = Neutral600)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatRupiah(item.price), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Cyan700, modifier = Modifier.padding(end = 16.dp))
                            OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("Edit", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
