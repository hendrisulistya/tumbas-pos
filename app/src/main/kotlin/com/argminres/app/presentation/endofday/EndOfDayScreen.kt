package com.argminres.app.presentation.endofday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndOfDayScreen(
    onNavigateBack: () -> Unit,
    viewModel: EndOfDayViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val title = when(uiState.currentStep) {
                        1 -> "Tutup Hari: Bahan Sisa"
                        2 -> "Tutup Hari: Hidangan Sisa"
                        3 -> "Tutup Hari: Rekap"
                        else -> "Tutup Hari"
                    }
                    Text(title, color = Color.White) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) {
                            viewModel.previousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                !uiState.isManager -> {
                    RestrictedAccess(onBack = onNavigateBack)
                }
                
                !uiState.hasActiveSession && !uiState.isComplete -> {
                    NoActiveSession(onBack = onNavigateBack)
                }
                
                uiState.isComplete -> {
                    EndOfDayCompleteScreen(
                        recap = uiState.recap,
                        totalWaste = uiState.totalDishWasteValue,
                        totalIngredientCost = uiState.totalIngredientCost,
                        totalSales = uiState.totalSales,
                        totalProfit = uiState.totalProfit,
                        currencyFormatter = currencyFormatter,
                        onDone = {
                            viewModel.resetState()
                            onNavigateBack()
                        }
                    )
                }
                
                uiState.currentStep == 1 -> {
                    IngredientInputStep(
                        ingredients = uiState.remainingIngredients,
                        onUpdateRemaining = viewModel::updateIngredientRemaining,
                        onNext = viewModel::nextStep,
                        onCancel = onNavigateBack
                    )
                }
                
                uiState.currentStep == 2 -> {
                    DishInputStep(
                        dishes = uiState.remainingDishes,
                        onUpdateRemaining = viewModel::updateDishRemaining,
                        onNext = viewModel::nextStep,
                        onBack = viewModel::previousStep
                    )
                }
                
                uiState.currentStep == 3 -> {
                    RecapStep(
                        ingredients = uiState.remainingIngredients,
                        dishes = uiState.remainingDishes,
                        totalSales = uiState.totalSales,
                        totalProfit = uiState.totalProfit,
                        isProcessing = uiState.isProcessing,
                        error = uiState.error,
                        currencyFormatter = currencyFormatter,
                        onConfirm = viewModel::processEndOfDay,
                        onBack = viewModel::previousStep
                    )
                }
            }
        }
    }
}

@Composable
fun NoActiveSession(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NotInterested,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tidak Ada Sesi Aktif",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tidak ada sesi operasional harian yang sedang berjalan.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Kembali")
        }
    }
}

@Composable
fun IngredientInputStep(
    ingredients: List<com.argminres.app.domain.usecase.session.EndOfDayIngredientInput>,
    onUpdateRemaining: (Long, Double) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Langkah 1: Sisa Bahan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Masukkan jumlah sisa bahan di gudang/dapur.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ingredients) { ingredient ->
                IngredientInputItem(ingredient, onUpdateRemaining)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Batal")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Lanjut")
            }
        }
    }
}

@Composable
fun IngredientInputItem(
    ingredient: com.argminres.app.domain.usecase.session.EndOfDayIngredientInput,
    onUpdateRemaining: (Long, Double) -> Unit
) {
    var textValue by remember(ingredient.remainingQuantity) { 
        mutableStateOf(if (ingredient.remainingQuantity % 1.0 == 0.0) ingredient.remainingQuantity.toInt().toString() else ingredient.remainingQuantity.toString()) 
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ingredient.ingredientName, fontWeight = FontWeight.Bold)
                Text("Awal: ${ingredient.startingQuantity} ${ingredient.unit}", style = MaterialTheme.typography.bodySmall)
            }
            
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    it.toDoubleOrNull()?.let { valDouble ->
                        onUpdateRemaining(ingredient.ingredientId, valDouble)
                    }
                },
                label = { Text("Sisa") },
                suffix = { Text(ingredient.unit) },
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

@Composable
fun DishInputStep(
    dishes: List<com.argminres.app.domain.usecase.session.EndOfDayDishInput>,
    onUpdateRemaining: (Long, Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Langkah 2: Sisa Hidangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Masukkan jumlah porsi hidangan yang tersisa di etalase.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dishes) { dish ->
                DishInputItem(dish, onUpdateRemaining)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Kembali")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Lihat Rekap")
            }
        }
    }
}

@Composable
fun DishInputItem(
    dish: com.argminres.app.domain.usecase.session.EndOfDayDishInput,
    onUpdateRemaining: (Long, Int) -> Unit
) {
    var textValue by remember(dish.remaining) { mutableStateOf(dish.remaining.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dish.dishName, fontWeight = FontWeight.Bold)
                Text("Produksi: ${dish.produced} porsi", style = MaterialTheme.typography.bodySmall)
            }
            
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    it.toIntOrNull()?.let { valInt ->
                        onUpdateRemaining(dish.dishId, valInt)
                    }
                },
                label = { Text("Sisa") },
                suffix = { Text("porsi") },
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
fun RecapStep(
    ingredients: List<com.argminres.app.domain.usecase.session.EndOfDayIngredientInput>,
    dishes: List<com.argminres.app.domain.usecase.session.EndOfDayDishInput>,
    totalSales: Double,
    totalProfit: Double,
    isProcessing: Boolean,
    error: String?,
    currencyFormatter: NumberFormat,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Langkah 3: Rekap Operasional",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                RecapTableSection(
                    title = "Bahan Masak",
                    headers = listOf("Nama", "Awal", "Sisa", "Pakai"),
                    rows = ingredients.map {
                        listOf(
                            it.ingredientName,
                            it.startingQuantity.toString(),
                            it.remainingQuantity.toString(),
                            (it.startingQuantity - it.remainingQuantity).toString()
                        )
                    }
                )
            }
            
            item {
                RecapTableSection(
                    title = "Hidangan",
                    headers = listOf("Nama", "Prod", "Sisa"),
                    rows = dishes.map {
                        listOf(
                            it.dishName,
                            it.produced.toString(),
                            it.remaining.toString()
                        )
                    }
                )
            }
        }
        
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), enabled = !isProcessing) {
                Text("Kembali")
            }
            Button(
                onClick = onConfirm, 
                modifier = Modifier.weight(1.5f),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Konfirmasi & Tutup Sesi")
                    }
                }
            }
        }
    }
}

@Composable
fun RecapTableSection(
    title: String,
    headers: List<String>,
    rows: List<List<String>>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            headers.forEachIndexed { index, header ->
                Text(
                    header,
                    modifier = Modifier.weight(if (index == 0) 2f else 1f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Rows
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(bottom = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                row.forEachIndexed { index, cell ->
                    Text(
                        cell,
                        modifier = Modifier.weight(if (index == 0) 2f else 1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun Modifier.border(bottom: androidx.compose.ui.unit.Dp, color: Color): Modifier = this

@Composable
fun EndOfDayCompleteScreen(
    recap: com.argminres.app.domain.usecase.session.EndOfDayRecap?,
    totalWaste: Double,
    totalIngredientCost: Double,
    totalSales: Double,
    totalProfit: Double,
    currencyFormatter: NumberFormat,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF43A047),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sesi Berhasil Ditutup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Data operasional telah disimpan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ringkasan Keuangan", fontWeight = FontWeight.Bold)
                SummaryLine("Total Penjualan", currencyFormatter.format(totalSales))
                SummaryLine("Biaya Bahan", currencyFormatter.format(totalIngredientCost))
                SummaryLine("Nilai Waste", currencyFormatter.format(totalWaste))
                Divider()
                SummaryLine("Laba Bersih", currencyFormatter.format(totalProfit), isBold = true)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Selesai")
        }
    }
}

@Composable
fun SummaryLine(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium)
        Text(value, style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RestrictedAccess(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Hanya Untuk Manajer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Hanya akun MANAJER yang dapat melakukan proses Tutup Hari.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Kembali")
        }
    }
}
