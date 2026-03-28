package com.argminres.app.presentation.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argminres.app.data.local.entity.EmployerEntity
import com.argminres.app.R
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
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
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
                    text = "Sistem Kasir",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFBBDEFB),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Feature pills
                listOf("Pembayaran Cepat", "Stok Real-time", "Multi-karyawan").forEach { feature ->
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
            text = "Selamat Datang Kembali",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Pilih akun Anda untuk melanjutkan",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF757575)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Dropdown for better reliability and control
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, if (expanded) Blue600 else Color(0xFFE0E0E0)),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Klik untuk memilih akun",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(416.dp) // Match max width - padding
                    .background(Color.White)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
            ) {
                if (employers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Memuat data karyawan...", color = Color.Gray) },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    employers.forEach { employer ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Blue50, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = employer.fullName.take(1).uppercase(),
                                            color = Blue600,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = employer.fullName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = employer.role,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Blue600
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onEmployeeSelected(employer)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        
        if (employers.isEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                color = Blue600,
                trackColor = Blue50
            )
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
                    text = "Masukkan PIN",
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
            Text("Bukan ${employer.fullName}? Ganti Pengguna", color = Color(0xFF9E9E9E))
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
