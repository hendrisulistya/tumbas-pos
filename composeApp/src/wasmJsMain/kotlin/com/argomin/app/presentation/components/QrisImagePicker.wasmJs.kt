package com.argomin.app.presentation.components

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

@Composable
actual fun rememberQrisImagePickerLauncher(
    onQrDecoded: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    return {
        try {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"
            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val file = files.item(0)
                    if (file != null) {
                        onError("File '${file.name}' dipilih. Gunakan dialog konfirmasi atau tempel payload untuk memproses.")
                    }
                }
            }
            input.click()
        } catch (e: Exception) {
            onError("Gagal membuka pemilih file di peramban web: ${e.message}")
        }
    }
}
