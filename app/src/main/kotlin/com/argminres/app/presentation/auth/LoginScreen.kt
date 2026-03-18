package com.argminres.app.presentation.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.argminres.app.data.local.entity.EmployerEntity
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Login",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Padang POS",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Crossfade(targetState = uiState.selectedEmployer != null) { isSelected ->
                    if (!isSelected) {
                        EmployeeSelectionView(
                            employers = uiState.employers,
                            onEmployeeSelected = viewModel::onEmployerSelected
                        )
                    } else {
                        PinInputView(
                            employer = uiState.selectedEmployer!!,
                            pin = uiState.pin,
                            onPinChange = viewModel::onPinChange,
                            onBack = viewModel::onBackToSelection,
                            isLoading = uiState.isLoading,
                            error = uiState.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeSelectionView(
    employers: List<EmployerEntity>,
    onEmployeeSelected: (EmployerEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Employee",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "Choose an account",
                onValueChange = {},
                readOnly = true,
                label = { Text("Employee") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                prefix = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                employers.forEach { employer ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(
                                    text = employer.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = employer.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onEmployeeSelected(employer)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PinInputView(
    employer: EmployerEntity,
    pin: String,
    onPinChange: (String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = employer.fullName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "Enter your 4-digit PIN to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Visual PIN dots
        PinDots(pinLength = pin.length)
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Custom Numeric Keypad
        NumericKeypad(
            onDigitClick = { digit ->
                if (pin.length < 4) {
                    onPinChange(pin + digit)
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) {
                    onPinChange(pin.dropLast(1))
                }
            },
            enabled = !isLoading
        )
        
        TextButton(onClick = onBack) {
            Text("Not ${employer.fullName}? Change User")
        }
    }
}

@Composable
fun PinDots(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        repeat(4) { index ->
            val isFilled = index < pinLength
            Surface(
                modifier = Modifier.size(16.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = if (!isFilled) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null
            ) {}
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean = true
) {
    val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "delete")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.width(300.dp)
    ) {
        for (i in 0 until 4) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (j in 0 until 3) {
                    val index = i * 3 + j
                    if (index < digits.size) {
                        val digit = digits[index]
                        if (digit.isNotEmpty()) {
                            KeypadButton(
                                text = if (digit == "delete") "⌫" else digit,
                                onClick = {
                                    if (digit == "delete") onDeleteClick() else onDigitClick(digit)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = enabled,
                                color = if (digit == "delete") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.5f),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (color == MaterialTheme.colorScheme.errorContainer) 
                MaterialTheme.colorScheme.onErrorContainer 
            else 
                MaterialTheme.colorScheme.onSecondaryContainer
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
