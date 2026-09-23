package com.argminres.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.argminres.app.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun DishImage(
    imagePath: String,
    modifier: Modifier = Modifier,
    fallbackEmoji: String = "🍲"
) {
    var bitmap by remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(imagePath) { mutableStateOf(false) }

    LaunchedEffect(imagePath) {
        if (imagePath.isNotBlank()) {
            try {
                val cleanPath = imagePath.removePrefix("files/").removePrefix("/")
                val bytes = Res.readBytes("files/$cleanPath")
                bitmap = bytes.decodeToImageBitmap()
            } catch (e: Exception) {
                loadFailed = true
            }
        } else {
            loadFailed = true
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(fallbackEmoji, fontSize = 32.sp)
        }
    }
}
