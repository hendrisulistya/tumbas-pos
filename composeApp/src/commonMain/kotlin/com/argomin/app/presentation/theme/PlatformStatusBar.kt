package com.argomin.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun PlatformStatusBar(color: Color, darkIcons: Boolean)
