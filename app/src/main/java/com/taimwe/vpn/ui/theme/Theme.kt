package com.taimwe.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TileMode
import androidx.compose.foundation.layout.fillMaxSize
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VpnAppTheme(
    selectedTheme: AppTheme = RainbowMatrixTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (selectedTheme.isDark) {
        darkColorScheme(
            primary = selectedTheme.primary,
            secondary = selectedTheme.secondary,
            tertiary = selectedTheme.tertiary,
            background = selectedTheme.background,
            surface = selectedTheme.surface,
            onPrimary = selectedTheme.onPrimary,
            onSecondary = selectedTheme.onSecondary,
            onBackground = selectedTheme.onBackground,
            onSurface = selectedTheme.onSurface,
        )
    } else {
        lightColorScheme(
            primary = selectedTheme.primary,
            secondary = selectedTheme.secondary,
            tertiary = selectedTheme.tertiary,
            background = selectedTheme.background,
            surface = selectedTheme.surface,
            onPrimary = selectedTheme.onPrimary,
            onSecondary = selectedTheme.onSecondary,
            onBackground = selectedTheme.onBackground,
            onSurface = selectedTheme.onSurface,
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MatrixRainBackground(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    themeColor: Color = NeonGreen,
    reverse: Boolean = false,
    rainbow: Boolean = false
) {
    if (!enabled) return
    
    val rainbowColors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF8C00),
        Color(0xFFFFD700),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF),
        Color(0xFF9400D3)
    )
    
    var columns by remember { mutableStateOf<List<MatrixColumn>>(emptyList()) }
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            columns = columns.map { it.copy(y = it.y + it.speed * if (reverse) -1f else 1f) }
                .filter { 
                    if (reverse) it.y > -2000 else it.y < height + 2000 
                }
            
            if (columns.isEmpty() || Random.nextFloat() < 0.03f) {
                val newCols = mutableListOf<MatrixColumn>()
                val colWidth = 14
                val numColumns = maxOf(1, width / colWidth)
                for (i in 0 until numColumns) {
                    if (Random.nextFloat() < 0.7f) {
                        val color = if (rainbow) rainbowColors.random() else themeColor
                        newCols.add(
                            MatrixColumn(
                                x = i * colWidth,
                                y = if (reverse) Random.nextFloat() * (height + 3000) else Random.nextFloat() * -3000f,
                                speed = Random.nextFloat() * 2 + 0.5f,
                                length = Random.nextInt(250, 400),
                                color = color
                            )
                        )
                    }
                }
                columns = newCols
            }
        }
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        width = canvasWidth.toInt()
        height = canvasHeight.toInt()
        
        columns.forEach { col ->
            for (i in 0 until col.length) {
                val y = col.y + i * 14f
                if (y in -20f..canvasHeight + 20f) {
                    val alpha = if (i == 0) 1f else (1f - i / col.length.toFloat()) * 0.85f + 0.15f
                    val color = col.color.copy(alpha = alpha)
                    
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(col.x.toFloat(), y - 2f),
                        size = androidx.compose.ui.geometry.Size(if (i == 0) 10f else 6f, if (i == 0) 10f else 12f)
                    )
                    
                    drawRect(
                        color = color,
                        topLeft = Offset(col.x.toFloat() + 2, y + 2),
                        size = androidx.compose.ui.geometry.Size(6f, 10f)
                    )
                }
            }
        }
    }
}

private data class MatrixColumn(
    val x: Int,
    val y: Float,
    val speed: Float,
    val length: Int,
    val color: Color
)
