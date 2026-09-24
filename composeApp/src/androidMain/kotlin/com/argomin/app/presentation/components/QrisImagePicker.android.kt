package com.argomin.app.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
actual fun rememberQrisImagePickerLauncher(
    onQrDecoded: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                            ?: barcodes.firstOrNull()
                        val rawValue = qrCode?.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            onQrDecoded(rawValue)
                        } else {
                            onError("Gambar berhasil dimuat, namun tidak ditemukan kode QR di dalamnya.")
                        }
                    }
                    .addOnFailureListener { exc ->
                        onError("Gagal memproses gambar QR: ${exc.localizedMessage}")
                    }
            } catch (e: Exception) {
                onError("Gagal membaca file gambar: ${e.message}")
            }
        }
    }

    return { launcher.launch("image/*") }
}
