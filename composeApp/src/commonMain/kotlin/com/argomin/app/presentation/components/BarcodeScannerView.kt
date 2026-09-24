package com.argomin.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BarcodeScannerView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
)
