package com.argminres.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.argminres.app.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap

// In-memory cache for fast instant rendering on subsequent visits / category filters
private val imageBitmapCache = mutableMapOf<String, ImageBitmap>()

@Composable
fun DishImage(
    imagePath: String,
    modifier: Modifier = Modifier,
    fallbackEmoji: String = ""
) {
    var bitmap by remember(imagePath) { mutableStateOf(imageBitmapCache[imagePath]) }
    var loadFailed by remember(imagePath) { mutableStateOf(false) }

    LaunchedEffect(imagePath) {
        if (bitmap != null) return@LaunchedEffect

        if (imagePath.isNotBlank()) {
            try {
                val cleanPath = imagePath.removePrefix("files/").removePrefix("/")
                val bytes = Res.readBytes("files/$cleanPath")
                val decoded = bytes.decodeToImageBitmap()
                imageBitmapCache[imagePath] = decoded
                bitmap = decoded
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
    } else if (loadFailed) {
        // Subtle neutral placeholder if image failed to load
        Box(
            modifier = modifier.background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        // Modern Skeleton Shimmer Loading (replacing emoji flash)
        val infiniteTransition = rememberInfiniteTransition(label = "dishSkeleton")
        val shimmerTranslate by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE2E8F0),
                Color(0xFFF8FAFC),
                Color(0xFFE2E8F0)
            ),
            start = Offset(shimmerTranslate - 300f, shimmerTranslate - 300f),
            end = Offset(shimmerTranslate, shimmerTranslate)
        )

        Box(
            modifier = modifier.background(shimmerBrush)
        )
    }
}
