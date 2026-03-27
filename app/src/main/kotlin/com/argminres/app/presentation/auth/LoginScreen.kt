package com.argminres.app.presentation.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argminres.app.data.local.entity.EmployerEntity
import org.koin.androidx.compose.koinViewModel

private val Blue600 = Color(0xFF1976D2)
private val Blue50  = Color(0xFFE3F2FD)
private val Blue100 = Color(0xFFBBDEFB)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    // White background
    Row(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ── Left: Branding Panel (38%) — solid blue ──────────────────────
        Box(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
                .background(Blue600),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = Blue600
                    )
                }

                Text(
                    text = "Padang POS",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Point of Sale System",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFBBDEFB),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Feature pills
                listOf("Fast Checkout", "Real-time Stock", "Multi-employee").forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE3F2FD)
                        )
                    }
                }
            }
        }

        // ── Right: Login Form (62%) — white ─────────────────────────────
        Box(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BackHandler(enabled = uiState.selectedEmployer != null) {
                        viewModel.onBackToSelection()
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
}

@OptIn(ExperimentalMaterial3Api::class)
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
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Select your account to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Blue600)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue600,
                    unfocusedBorderColor = Color(0xFFBDBDBD),
                    focusedLabelColor = Blue600
                )
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
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = employer.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Blue600
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF616161))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Enter PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = employer.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Blue600,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        PinDots(pinLength = pin.length)

        if (error != null) {
            Text(
                text = error,
                color = Color(0xFFC62828),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Blue600)
        }

        NumericKeypad(
            onDigitClick = { digit -> if (pin.length < 4) onPinChange(pin + digit) },
            onDeleteClick = { if (pin.isNotEmpty()) onPinChange(pin.dropLast(1)) },
            enabled = !isLoading
        )

        TextButton(onClick = onBack) {
            Text("Not ${employer.fullName}? Change User", color = Color(0xFF9E9E9E))
        }
    }
}

@Composable
fun PinDots(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        repeat(4) { index ->
            val isFilled = index < pinLength
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isFilled) Blue600 else Blue50,
                        CircleShape
                    )
                    .border(1.5.dp, Blue100, CircleShape)
            )
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth()
    ) {
        for (i in 0 until 4) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                enabled = enabled,
                                isDelete = digit == "delete"
                            )
                        } else {
                            // empty spacer matching button size
                            Spacer(modifier = Modifier.size(width = 64.dp, height = 48.dp))
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
    isDelete: Boolean = false
) {
    Box(
        modifier = modifier
            .size(width = 64.dp, height = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDelete) Color(0xFFFFEBEE) else Blue50)
            .border(1.dp, if (isDelete) Color(0xFFEF9A9A) else Blue100, RoundedCornerShape(10.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (!enabled) Color(0xFFBDBDBD)
                    else if (isDelete) Color(0xFFC62828)
                    else Blue600
        )
    }
}
