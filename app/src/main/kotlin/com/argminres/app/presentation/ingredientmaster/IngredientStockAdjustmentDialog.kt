package com.argminres.app.presentation.ingredientmaster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.IngredientEntity

@Composable
fun IngredientStockAdjustmentDialog(
    ingredient: IngredientEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Stock: ${ingredient.stock} ${ingredient.unit}")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isAddition, onClick = { isAddition = true })
                    Text("Add")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isAddition, onClick = { isAddition = false })
                    Text("Remove")
                }
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (${ingredient.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 0.0
                    val finalQty = if (isAddition) qty else -qty
                    onConfirm(finalQty)
                },
                enabled = quantity.isNotBlank() && (quantity.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
