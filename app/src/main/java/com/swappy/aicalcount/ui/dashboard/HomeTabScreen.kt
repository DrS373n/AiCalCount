package com.swappy.aicalcount.ui.dashboard

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.swappy.aicalcount.R
import com.swappy.aicalcount.data.meals.LoggedMeal
import com.swappy.aicalcount.network.Recipe
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeTabScreen(
    isNewUser: Boolean,
    todayProtein: Float,
    todayCarbs: Float,
    todayFat: Float,
    goalCalories: Float,
    goalProtein: Float,
    goalCarbs: Float,
    goalFat: Float,
    todayFiber: Float = 0f,
    todaySugar: Float = 0f,
    todaySodium: Float = 0f,
    goalFiber: Float = 25f,
    goalSugar: Float = 50f,
    goalSodium: Float = 2300f,
    hydrationGlasses: Int,
    hydrationGoalGlasses: Int,
    streakCount: Int,
    datesWithGoalsMet: Set<String> = emptySet(),
    todayMeals: List<LoggedMeal> = emptyList(),
    lastUploadedRecipe: Recipe? = null,
    lastUploadedImage: Bitmap? = null,
    onAddHydration: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToNutrition: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                DateSelector(datesWithGoalsMet = datesWithGoalsMet)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            if (isNewUser) {
                item {
                    NewUserEmptyState(
                        onLogFirstMeal = onNavigateToScan,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    AddMealCard()
                }
            } else {
                item {
                    val todayCalories = todayProtein * 4f + todayCarbs * 4f + todayFat * 9f
                    CalorieSummaryCard(
                        caloriesEaten = todayCalories,
                        goalCalories = goalCalories,
                        todayProtein = todayProtein,
                        goalProtein = goalProtein,
                        todayCarbs = todayCarbs,
                        goalCarbs = goalCarbs,
                        todayFat = todayFat,
                        goalFat = goalFat,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    TodaysMealsSection(
                        meals = todayMeals,
                        onTapToLog = onNavigateToScan,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    WaterCard(
                        glasses = hydrationGlasses,
                        goal = hydrationGoalGlasses,
                        onAddHydration = onAddHydration,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniMacroCard(
                            label = "Fiber",
                            current = todayFiber,
                            goal = goalFiber,
                            unit = "g",
                            modifier = Modifier.weight(1f)
                        )
                        MiniMacroCard(
                            label = "Sugar",
                            current = todaySugar,
                            goal = goalSugar,
                            unit = "g",
                            modifier = Modifier.weight(1f)
                        )
                        MiniMacroCard(
                            label = "Sodium",
                            current = todaySodium,
                            goal = goalSodium,
                            unit = "mg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    Text(
                        text = "Recently uploaded",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                if (lastUploadedRecipe != null) {
                    item {
                        RecentlyUploadedCard(
                            recipe = lastUploadedRecipe,
                            image = lastUploadedImage,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
                item {
                    AddMealCard()
                }
            }
        }
    }
}

@Composable
fun DateSelector(datesWithGoalsMet: Set<String> = emptySet()) {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("E", Locale.getDefault())
    val dayFormat = SimpleDateFormat("d", Locale.getDefault())
    // Current week: 7 days starting from locale's first day of week (e.g. Sunday or Monday)
    val firstDayOfWeek = today.clone() as Calendar
    firstDayOfWeek.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek())
    firstDayOfWeek.set(Calendar.HOUR_OF_DAY, 0)
    firstDayOfWeek.set(Calendar.MINUTE, 0)
    firstDayOfWeek.set(Calendar.SECOND, 0)
    firstDayOfWeek.set(Calendar.MILLISECOND, 0)
    val weekDates = (0..6).map { offset ->
        val c = firstDayOfWeek.clone() as Calendar
        c.add(Calendar.DATE, offset)
        c
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDates.forEach { cal ->
            val dayOfWeek = dateFormat.format(cal.time).first().toString()
            val dayOfMonth = dayFormat.format(cal.time)
            val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            val dateKey = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
            val goalMet = dateKey in datesWithGoalsMet
            DateItem(
                dayOfWeek = dayOfWeek,
                dayOfMonth = dayOfMonth,
                isSelected = isToday,
                goalMet = goalMet,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DateItem(
    dayOfWeek: String,
    dayOfMonth: String,
    isSelected: Boolean,
    goalMet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        goalMet -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        goalMet -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = dayOfWeek,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
            )
            Text(
                text = dayOfMonth,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}

private fun calorieEmojiForProgress(progress: Float): String = when {
    progress <= 0f -> "🍽️"
    progress < 0.25f -> "🔥"
    progress < 0.5f -> "🔥"
    progress < 0.75f -> "💪"
    progress < 1f -> "⚡"
    else -> "🎉"
}

private fun waterEmojiForProgress(progress: Float): String = when {
    progress <= 0f -> "💧"
    progress < 0.25f -> "💧"
    progress < 0.5f -> "🥤"
    progress < 0.75f -> "💦"
    progress < 1f -> "🌊"
    else -> "🎉"
}

@Composable
fun CalorieSummaryCard(
    caloriesEaten: Float,
    goalCalories: Float,
    todayProtein: Float = 0f,
    goalProtein: Float = 0f,
    todayCarbs: Float = 0f,
    goalCarbs: Float = 0f,
    todayFat: Float = 0f,
    goalFat: Float = 0f,
) {
    val remaining = (goalCalories - caloriesEaten).coerceAtLeast(0f).toInt()
    val progress = if (goalCalories > 0f) (caloriesEaten / goalCalories).coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$remaining",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.home_calories_remaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InlineMacroChip(
                    label = stringResource(R.string.home_macro_protein),
                    current = todayProtein.toInt(),
                    goal = goalProtein.toInt()
                )
                InlineMacroChip(
                    label = stringResource(R.string.home_macro_carbs),
                    current = todayCarbs.toInt(),
                    goal = goalCarbs.toInt()
                )
                InlineMacroChip(
                    label = stringResource(R.string.home_macro_fat),
                    current = todayFat.toInt(),
                    goal = goalFat.toInt()
                )
            }
        }
    }
}

@Composable
fun TodaysMealsSection(
    meals: List<LoggedMeal>,
    onTapToLog: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.todays_meals_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (meals.isEmpty()) {
            Card(
                onClick = onTapToLog,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.todays_meals_empty_cta),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            meals.forEach { meal ->
                LoggedMealCard(meal = meal, modifier = Modifier.padding(vertical = 4.dp))
            }
            Card(
                onClick = onTapToLog,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.todays_meals_empty_cta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedMealCard(meal: LoggedMeal, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${meal.calories.toInt()} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InlineMacroChip(label: String, current: Int, goal: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$current/$goal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WaterCard(
    glasses: Int,
    goal: Int,
    onAddHydration: () -> Unit,
) {
    val progress = if (goal > 0) (glasses.toFloat() / goal).coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Water", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$glasses/$goal", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = " glasses",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = waterEmojiForProgress(progress),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.size(32.dp),
                )
                Button(
                    onClick = onAddHydration,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "+1", fontSize = 16.sp)
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}


@Composable
fun MacroCard(
    label: String,
    current: Float,
    goal: Float,
    modifier: Modifier = Modifier,
) {
    val progress = if (goal > 0f) (current / goal).coerceIn(0f, 1f) else 0f
    Card(
        modifier = modifier.height(168.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp),
            ) {
                // Background circle (track) - always visible full circle
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    trackColor = Color.Transparent,
                )
                // Progress circle on top - shows current/goal
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                )
                Text(
                    text = "${current.toInt()}/${goal.toInt()}g",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                )
        }
    }
}

@Composable
fun MiniMacroCard(
    label: String,
    current: Float,
    goal: Float,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val progress = if (goal > 0f) (current / goal).coerceIn(0f, 1f) else 0f
    val isLongUnit = unit == "mg" // sodium: show two lines so "500/2300mg" fits
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    trackColor = Color.Transparent,
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                )
                if (isLongUnit) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            text = "${current.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "${goal.toInt()}$unit",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = "${current.toInt()}/${goal.toInt()}$unit",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun NewUserEmptyState(onLogFirstMeal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Welcome! 👋",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Log your first meal to unlock your daily summary, calories, and macros.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onLogFirstMeal,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Log first meal")
            }
        }
    }
}

@Composable
fun RecentlyUploadedCard(
    recipe: Recipe,
    image: Bitmap?,
) {
    val title = recipe.recipes.firstOrNull()?.title ?: "Meal"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (image != null) {
                val imageBitmap = runCatching { image.asImageBitmap() }.getOrNull()
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun AddMealCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Snap or describe your first meal",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tap + to log your first meal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AiCalCountTheme {
        HomeTabScreen(
            isNewUser = false,
            todayProtein = 80f,
            todayCarbs = 120f,
            todayFat = 50f,
            goalCalories = 2000f,
            goalProtein = 150f,
            goalCarbs = 200f,
            goalFat = 70f,
            hydrationGlasses = 3,
            hydrationGoalGlasses = 8,
            streakCount = 5,
            onAddHydration = {},
            onNavigateToScan = {},
            onNavigateToNutrition = {},
        )
    }
}
