package com.example.savingswidget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    thickness: Dp = 8.dp,
    waveLength: Dp = 32.dp, // Increased from 24dp for longer, smoother waves
    waveHeight: Dp = 5.dp, // Slightly reduced relative to thickness for gentler curves
    isWavy: Boolean = true
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness + (if (isWavy) waveHeight else 0.dp))
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val progressWidth = width * progress.coerceIn(0f, 1f)
        
        val strokeWidth = thickness.toPx()
        val waveLenPx = waveLength.toPx()
        val waveHeightPx = if (isWavy) waveHeight.toPx() else 0f
        val margin = strokeWidth / 2 // Add margin for rounded caps

        val gap = 8.dp.toPx() // Adjusted gap to 8dp

        // 1. Draw Track (straight line from progress + gap to end)
        if (progress < 1f) {
            val trackStart = progressWidth + margin + gap
            if (trackStart < width - margin) {
                drawLine(
                    color = trackColor,
                    start = Offset(trackStart, centerY),
                    end = Offset(width - margin, centerY), // Draw to the very end of margin
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Draw Stop Dot at the end (smaller dot)
        if (progress < 0.98f) {
            drawCircle(
                color = color,
                radius = 1.25.dp.toPx(), // Slightly larger dot for larger UI
                center = Offset(width - margin - 1.dp.toPx(), centerY) // Positioned closer to the end
            )
        }

        // 3. Draw Wavy Progress
        if (progress > 0f) {
            val path = Path()
            path.moveTo(margin, centerY)
            
            // Number of points to draw. Using higher density for extra smoothness.
            val density = 1 // 1px per point for maximum smoothness
            val points = (progressWidth / density).toInt()
            
            for (i in 1..points) {
                val x = margin + i.toFloat() * density
                // Use a sine wave
                val y = centerY + sin(i.toFloat() * density * 2 * PI.toFloat() / waveLenPx) * (waveHeightPx / 2)
                path.lineTo(x, y)
            }
            
            // Ensure we reach the exact progress width
            val finalX = margin + progressWidth
            val finalY = centerY + sin(progressWidth * 2 * PI.toFloat() / waveLenPx) * (waveHeightPx / 2)
            path.lineTo(finalX, finalY)
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WavyProgressPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            WavyProgressIndicator(progress = 0.4f)
        }
    }
}
