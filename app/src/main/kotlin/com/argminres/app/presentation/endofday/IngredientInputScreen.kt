package com.argminres.app.presentation.endofday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.domain.usecase.session.RemainingIngredient
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EndOfDayIngredientInputScreen(
    isProcessing: Boolean,
    error: String?,
    ingredients: List<RemainingIngredient>,
    onConfirm: (List<RemainingIngredient>) -> Unit,
    onCancel: () -> Unit
) {
    // Mutable state for ingredient quantities
    var ingredientInputs by remember(ingredients) {
        mutableStateOf(ingredients.map { it.copy() })
    }
    var showSkipDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "End of Day - Ingredient Check",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ingredient Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Enter remaining ingredient quantities to calculate daily usage.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Usage = Starting - Remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ingredient list
        if (ingredients.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Ingredients Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add ingredients via Purchase Orders first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredientInputs.size) { index ->
                    IngredientInputCard(
                        ingredient = ingredientInputs[index],
                        onQuantityChange = { newQuantity ->
                            ingredientInputs = ingredientInputs.toMutableList().apply {
                                this[index] = this[index].copy(remainingQuantity = newQuantity)
                            }
                        },
                        onPriceChange = { newPrice ->
                            ingredientInputs = ingredientInputs.toMutableList().apply {
                                this[index] = this[index].copy(costPerUnit = newPrice)
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
            
            if (ingredients.isEmpty()) {
                Button(
                    onClick = { showSkipDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    Text("Skip & Continue")
                }
            } else {
                Button(
                    onClick = { onConfirm(ingredientInputs) },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirm & Close Day")
                    }
                }
            }
        }
    }
    
    // Skip confirmation dialog
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Skip Ingredient Tracking?") },
            text = {
                Text("You can close the day without ingredient data. Ingredient usage will not be tracked for this session.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = false
                        onConfirm(emptyList())
                    }
                ) {
                    Text("Skip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun IngredientInputCard(
    ingredient: RemainingIngredient,
    onQuantityChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit
) {
    var quantityText by remember(ingredient.remainingQuantity) {
        mutableStateOf(ingredient.remainingQuantity.toString())
    }
    var priceText by remember(ingredient.costPerUnit) {
        mutableStateOf(ingredient.costPerUnit.toString())
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ingredient.ingredientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Starting: ${ingredient.startingQuantity} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Remaining Quantity Input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newValue ->
                        quantityText = newValue
                        newValue.toDoubleOrNull()?.let { qty ->
                            if (qty >= 0 && qty <= ingredient.startingQuantity) {
                                onQuantityChange(qty)
                            }
                        }
                    },
                    label = { Text("Remaining") },
                    suffix = { Text(ingredient.unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                // Actual Price Input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        priceText = newValue
                        newValue.toDoubleOrNull()?.let { price ->
                            if (price >= 0) {
                                onPriceChange(price)
                            }
                        }
                    },
                    label = { Text("Price/Unit") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Show calculated usage
            val usage = ingredient.startingQuantity - ingredient.remainingQuantity
            if (usage > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Used: $usage ${ingredient.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            .format(usage * ingredient.costPerUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
