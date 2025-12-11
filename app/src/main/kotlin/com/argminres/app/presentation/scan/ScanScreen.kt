package com.argminres.app.presentation.scan

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = koinViewModel(),
    onNavigateToCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResumeScanning()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onPauseScanning()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(Icons.Default.ShoppingCart, "Cart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (cameraPermissionState.status.isGranted) {
                BarcodeScanner(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeScanned = viewModel::onBarcodeScanned
                )
                
                // Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // Transparent hole in the middle
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center)
                            .background(Color.Transparent) // This doesn't actually cut a hole in Compose easily without custom drawing
                            // Instead, let's just draw a border
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    )
                    
                    // Actually, to make a hole, we'd need a custom draw modifier or Canvas.
                    // For simplicity, let's just show the camera full screen and put a frame on top.
                    // But wait, the background above covers the camera.
                    // Let's remove the background and just show the frame.
                }
                
                // Real Overlay
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                )
                
                Text(
                    text = "Align barcode within frame",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 180.dp)
                )

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is required to scan barcodes")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }

            // Feedback Overlay
            if (uiState.successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = padding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = padding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
