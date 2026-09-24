package com.argomin.app.presentation.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.argomin.app.domain.model.IngredientItem
import com.argomin.app.presentation.theme.*
import com.argomin.app.util.formatRupiah

@Composable
fun IngredientStockScreen(ingredients: List<IngredientItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Stok Bahan Baku Harian", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients) { ing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Neutral50, RoundedCornerShape(8.dp))
                            .border(1.dp, Neutral200, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ing.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Batas Minimum: ${ing.minStock} ${ing.unit} • Harga: ${formatRupiah(ing.costPerUnit)}/${ing.unit}", fontSize = 11.sp, color = Neutral600)
                        }
                        Text("${ing.stock} ${ing.unit}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (ing.stock <= ing.minStock) ErrorBase else Cyan700)
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientMasterManagementScreen(ingredients: MutableList<IngredientItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Master Katalog Bahan Baku", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Text("Tambah Bahan", color = White)
            }
        }
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients) { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Gray300, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ing.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Satuan: ${ing.unit} • Estimasi Harga Beli: ${formatRupiah(ing.costPerUnit)}", fontSize = 12.sp, color = Gray600)
                        }
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Edit", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
