package com.swappy.aicalcount.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.ui.theme.ProgressGreen
import com.swappy.aicalcount.ui.theme.StreakYellow
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import com.swappy.aicalcount.ui.theme.TextSecondary

@Composable
fun ProgressTabScreen(
    currentWeightKg: Float,
    goalWeightKg: Float,
    weightHistory: List<Pair<String, Float>>,
    streakCount: Int,
    weekDaysLogged: List<Boolean>,
    onLogWeight: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onImportFromHealthConnect: () -> Unit = {},
    healthConnectAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    var selectedTimeframe by remember { mutableStateOf(3) } // 0=90D, 1=6M, 2=1Y, 3=ALL
    val today = java.time.LocalDate.now()
    val filteredHistory = remember(weightHistory, selectedTimeframe) {
        val cutoff = when (selectedTimeframe) {
            0 -> today.minusDays(90)
            1 -> today.minusDays(180)
            2 -> today.minusDays(365)
            else -> null
        }
        if (cutoff == null) weightHistory
        else weightHistory.filter { it.first >= cutoff.toString() }
    }
    val weightDisplay = if (currentWeightKg > 0f) "%.1f kg".format(currentWeightKg) else "—"
    val goalDisplay = if (goalWeightKg > 0f) "%.1f kg".format(goalWeightKg) else "—"
    val progressFraction = when {
        goalWeightKg <= 0f -> 0f
        currentWeightKg <= 0f -> 0f
        else -> (currentWeightKg / goalWeightKg).coerceIn(0f, 1f)
    }
    val goalPercentText = when {
        goalWeightKg <= 0f || currentWeightKg <= 0f -> ""
        else -> "%.0f%% of goal".format((currentWeightKg / goalWeightKg * 100f).coerceIn(0f, 199f))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(20.dp)
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.progress_your_weight),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Text(
                        weightDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.progress_goal, goalDisplay),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(6.dp)
                                .background(ProgressGreen, RoundedCornerShape(3.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onLogWeight,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.progress_log_weight))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("→")
                    }
                    if (healthConnectAvailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onImportFromHealthConnect) {
                            Text(stringResource(R.string.progress_import_health_connect))
                        }
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            streakCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = StreakYellow
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("🔥", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        stringResource(R.string.progress_day_streak),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (weekDaysLogged + List(7) { false }).take(7).zip(dayLabels).forEach { (filled, label) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .then(
                                            if (filled) Modifier.background(StreakYellow, CircleShape)
                                            else Modifier
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (weightHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.progress_empty_weight),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.progress_empty_weight_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLogWeight) {
                        Text(stringResource(R.string.progress_log_weight))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.progress_weight_progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (goalPercentText.isNotEmpty()) {
                        Text(
                            goalPercentText,
                            style = MaterialTheme.typography.labelMedium,
                            color = ProgressGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (filteredHistory.isNotEmpty()) {
                        val minW = filteredHistory.minOf { it.second }
                        val maxW = filteredHistory.maxOf { it.second }
                        val range = (maxW - minW).coerceAtLeast(1f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val strokeWidthPx = 4.dp.toPx()
                            val pts = filteredHistory.mapIndexed { index, (_, weight) ->
                                val x = if (filteredHistory.size == 1) w / 2f
                                else (w * (index.toFloat() / (filteredHistory.size - 1).coerceAtLeast(1)))
                                val y = h - (weight - minW) / range * (h - strokeWidthPx * 2) - strokeWidthPx
                                Offset(x, y.coerceIn(strokeWidthPx, h - strokeWidthPx))
                            }
                            if (pts.size >= 2) {
                                for (i in 0 until pts.size - 1) {
                                    drawLine(
                                        color = ProgressGreen,
                                        start = pts[i],
                                        end = pts[i + 1],
                                        strokeWidth = strokeWidthPx,
                                        cap = StrokeCap.Round
                                    )
                                }
                            } else {
                                drawLine(
                                    color = ProgressGreen,
                                    start = Offset(0f, pts[0].y),
                                    end = Offset(w, pts[0].y),
                                    strokeWidth = strokeWidthPx,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.progress_log_to_see),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeframeChip(
                        modifier = Modifier.weight(1f),
                        label = "90D",
                        selected = selectedTimeframe == 0,
                        onClick = { selectedTimeframe = 0 }
                    )
                    TimeframeChip(
                        modifier = Modifier.weight(1f),
                        label = "6M",
                        selected = selectedTimeframe == 1,
                        onClick = { selectedTimeframe = 1 }
                    )
                    TimeframeChip(
                        modifier = Modifier.weight(1f),
                        label = "1Y",
                        selected = selectedTimeframe == 2,
                        onClick = { selectedTimeframe = 2 }
                    )
                    TimeframeChip(
                        modifier = Modifier.weight(1f),
                        label = "ALL",
                        selected = selectedTimeframe == 3,
                        onClick = { selectedTimeframe = 3 }
                    )
                }
                if (filteredHistory.isNotEmpty() && goalWeightKg > 0f) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.progress_keep_going),
                        style = MaterialTheme.typography.bodySmall,
                        color = ProgressGreen
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Daily Average Calories",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "2861",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                " cal  ",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Text(
                "+90%",
                style = MaterialTheme.typography.bodyMedium,
                color = ProgressGreen
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToCompare,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Compare progress photos")
        }
    }
}

@Composable
private fun TimeframeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else TextSecondary
        )
    }
}

@Preview(showBackground = true, name = "Progress tab")
@Composable
fun ProgressTabScreenPreview() {
    AiCalCountTheme {
        ProgressTabScreen(
            currentWeightKg = 72.5f,
            goalWeightKg = 68f,
            weightHistory = listOf("2025-01-01" to 75f, "2025-02-01" to 73f, "2025-02-15" to 72.5f),
            streakCount = 5,
            weekDaysLogged = listOf(true, true, false, true, true, false, false),
            onLogWeight = {},
            onNavigateToCompare = {},
        )
    }
}
