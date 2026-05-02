package com.n0white.n0widgets.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressIndicator(
    progress: Float,
    color: Color,
    trackColor: Color,
    isWavy: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().height(16.dp)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val strokeWidth = 14f

        // Track
        drawLine(
            color = trackColor,
            start = androidx.compose.ui.geometry.Offset(0f, centerY),
            end = androidx.compose.ui.geometry.Offset(width, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Progress
        if (progress > 0) {
            val progressWidth = width * progress.coerceIn(0f, 1f)
            if (isWavy) {
                val path = Path()
                path.moveTo(0f, centerY)
                val waveLength = 60f
                val waveHeight = 10f
                
                var x = 0f
                while (x < progressWidth) {
                    val y = centerY + sin(x * 2 * PI / waveLength).toFloat() * (waveHeight / 2)
                    path.lineTo(x, y)
                    x += 1f
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(progressWidth, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
