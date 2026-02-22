package com.swappy.aicalcount.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A simple donut chart showing proportional segments.
 * @param items Values for each segment (e.g. protein, carbs, fat in grams).
 * @param colors Colors for each segment, same order as items.
 * @param modifier Modifier for layout.
 */
@Composable
fun DonutChart(
    items: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val total = items.sum()
    if (total <= 0f) return
    Canvas(modifier = modifier) {
        val stroke = size.minDimension / 6f
        val radius = (size.minDimension / 2f) - stroke / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f // start from top
        items.forEachIndexed { index, value ->
            val sweep = (value / total) * 360f
            val color = colors.getOrElse(index) { Color.Gray }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
            startAngle += sweep
        }
    }
}
