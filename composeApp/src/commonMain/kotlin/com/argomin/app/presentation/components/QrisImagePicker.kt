package com.argomin.app.presentation.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific launcher to pick and decode QR code from an image file.
 */
@Composable
expect fun rememberQrisImagePickerLauncher(
    onQrDecoded: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit
