package com.swappy.aicalcount.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.ui.components.DonutChart
import com.swappy.aicalcount.ui.components.HydrationCard
import com.swappy.aicalcount.ui.components.StreakCard
import com.swappy.aicalcount.ui.theme.AiCalCountTheme

@OptIn(ExperimentalMaterial3Api::class)
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
    hydrationGlasses: Int,
    hydrationGoalGlasses: Int,
    streakCount: Int,
    onAddHydration: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToNutrition: (String) -> Unit,
) {
    val todayCalories = (todayProtein * 4) + (todayCarbs * 4) + (todayFat * 9)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(visible = !isNewUser) {
                    Column {
                        DailySummary(
                            todayCalories,
                            goalCalories,
                            todayProtein,
                            goalProtein,
                            todayCarbs,
                            goalCarbs,
                            todayFat,
                            goalFat,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HydrationCard(
                                glasses = hydrationGlasses,
                                goal = hydrationGoalGlasses,
                                onAdd = onAddHydration,
                                modifier = Modifier.weight(1f)
                            )
                            StreakCard(
                                streak = streakCount,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = isNewUser) {
                    HomeEmptyState(
                        onNavigateToScan = onNavigateToScan
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.home_empty_recent),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (isNewUser) {
                item {
                    HomeEmptyState(
                        onNavigateToScan = onNavigateToScan
                    )
                }
            } else {
                items(emptyList<String>()) {
                    // TODO: Recent items
                }
            }
        }
    }
}

@Composable
fun DailySummary(
    todayCalories: Float,
    goalCalories: Float,
    todayProtein: Float,
    goalProtein: Float,
    todayCarbs: Float,
    goalCarbs: Float,
    todayFat: Float,
    goalFat: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "%.0f / %.0f".format(todayCalories, goalCalories),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "calories",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                MacroRow(
                    label = "Protein",
                    today = todayProtein,
                    goal = goalProtein,
                    color = Color(0xFF50C878),
                )
                MacroRow(
                    label = "Carbs",
                    today = todayCarbs,
                    goal = goalCarbs,
                    color = Color(0xFF0085FF),
                )
                MacroRow(
                    label = "Fat",
                    today = todayFat,
                    goal = goalFat,
                    color = Color(0xFFFFC107),
                )
            }
            DonutChart(
                items = listOf(todayProtein, todayCarbs, todayFat),
                colors = listOf(
                    Color(0xFF50C878), // Protein
                    Color(0xFF0085FF), // Carbs
                    Color(0xFFFFC107), // Fat
                ),
                modifier = Modifier.size(120.dp),
            )
        }
    }
}

@Composable
fun MacroRow(
    label: String,
    today: Float,
    goal: Float,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_circle),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(8.dp),
        )
        Text(
            text = "$label %.0fg / %.0fg".format(today, goal),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}


@Composable
fun HomeEmptyState(
    onNavigateToScan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_food_placeholder),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_cta),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

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

@Preview(showBackground = true, name = "Home screen empty state")
@Composable
fun HomeScreenEmptyPreview() {
    AiCalCountTheme {
        HomeTabScreen(
            isNewUser = true,
            todayProtein = 0f,
            todayCarbs = 0f,
            todayFat = 0f,
            goalCalories = 2000f,
            goalProtein = 150f,
            goalCarbs = 200f,
            goalFat = 70f,
            hydrationGlasses = 0,
            hydrationGoalGlasses = 8,
            streakCount = 0,
            onAddHydration = {},
            onNavigateToScan = {},
            onNavigateToNutrition = {},
        )
    }
}
