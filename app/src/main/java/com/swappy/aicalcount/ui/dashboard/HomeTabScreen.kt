package com.swappy.aicalcount.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import com.swappy.aicalcount.ui.theme.CarbsOrange
import com.swappy.aicalcount.ui.theme.FatBlue
import com.swappy.aicalcount.ui.theme.ProgressGreen
import com.swappy.aicalcount.ui.theme.ProteinRed
import com.swappy.aicalcount.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeTabScreen(
    isNewUser: Boolean,
    todayProtein: Float = 0f,
    todayCarbs: Float = 0f,
    todayFat: Float = 0f,
    goalProtein: Float = 150f,
    goalCarbs: Float = 275f,
    goalFat: Float = 70f,
    onNavigateToScan: () -> Unit,
    onNavigateToNutrition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val today = LocalDate.now()
    // Today on the left, then next 6 days (7 days total)
    val weekDays = (0..6).map { today.plusDays(it.toLong()) }
    val selectedIndex = 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (!isNewUser) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("15", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(weekDays.size) { i ->
                val date = weekDays[i]
                val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1)
                val dayNum = date.dayOfMonth.toString()
                val selected = i == selectedIndex
                val isToday = date == today
                // Progress for this day (0..1). Today uses main card ratio when not new user; other days 0 until we have per-day data.
                val dayProgress = when {
                    date == today && !isNewUser -> 0.5f
                    date == today && isNewUser -> 0f
                    else -> 0f
                }
                val strokeWidth = 3.dp
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val progressColor = when {
                    selected -> MaterialTheme.colorScheme.primary
                    isToday -> ProgressGreen
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                }
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = strokeWidth.toPx()
                        val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                        val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                        // Track (full circle)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        // Progress arc
                        if (dayProgress > 0f) {
                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = 360f * dayProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    selected -> MaterialTheme.colorScheme.primary
                                    isToday -> ProgressGreen
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected || isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                dayNum,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected || isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isNewUser) "0/2500" else "1250/2500",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Calories eaten",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                val progress = if (isNewUser) 0f else 0.5f
                val strokeWidth = 12.dp
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = strokeWidth.toPx()
                        val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                        val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                        // Track
                        drawArc(
                            color = surfaceVariantColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        // Progress
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                    Text("🔥", fontSize = 24.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MacroCard(
                modifier = Modifier.weight(1f),
                current = todayProtein,
                goal = goalProtein,
                label = "Protein eaten",
                color = ProteinRed
            )
            MacroCard(
                modifier = Modifier.weight(1f),
                current = todayCarbs,
                goal = goalCarbs,
                label = "Carbs eaten",
                color = CarbsOrange
            )
            MacroCard(
                modifier = Modifier.weight(1f),
                current = todayFat,
                goal = goalFat,
                label = "Fat eaten",
                color = FatBlue
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.home_empty_recent),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isNewUser) {
            Card(
                onClick = onNavigateToScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = stringResource(R.string.home_empty_cta),
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            MealUploadCard(
                title = "Grilled Salmon",
                calories = "550",
                protein = "35g",
                carbs = "40g",
                fat = "28g",
                time = "12:37pm",
                onClick = { onNavigateToNutrition("1") }
            )
        }
    }
}

@Composable
private fun MacroCard(
    modifier: Modifier = Modifier,
    current: Float,
    goal: Float,
    label: String,
    color: Color
) {
    val currentInt = current.toInt()
    val goalInt = goal.toInt().coerceAtLeast(1)
    val progressFraction = (current / goal).coerceIn(0f, 1f)
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$currentInt/${goalInt}g",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 5.dp.toPx()
                    val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                    val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                    // Track (full circle) — use outline so it's visible on any card
                    val trackColor = surfaceVariantColor
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                    // Progress arc (full circle sweep, clockwise from top)
                    if (progressFraction > 0f) {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 360f * progressFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealUploadCard(
    title: String,
    calories: String,
    protein: String,
    carbs: String,
    fat: String,
    time: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🍽", fontSize = 32.sp)
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "🔥 $calories Calories",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "🥩 $protein  🌾 $carbs  💧 $fat",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Preview(showBackground = true, name = "Home tab - new user")
@Composable
fun HomeTabScreenPreview() {
    AiCalCountTheme {
        HomeTabScreen(
            isNewUser = true,
            onNavigateToScan = {},
            onNavigateToNutrition = {},
        )
    }
}

@Preview(showBackground = true, name = "Home tab - with data")
@Composable
fun HomeTabScreenWithDataPreview() {
    AiCalCountTheme {
        HomeTabScreen(
            isNewUser = false,
            onNavigateToScan = {},
            onNavigateToNutrition = {},
        )
    }
}
