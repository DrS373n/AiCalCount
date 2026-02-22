package com.swappy.aicalcount.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.data.meals.LoggedMeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@Composable
fun DiaryTabScreen(
    meals: List<LoggedMeal>,
    onTapToLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedByDate = meals.groupBy { it.date }.toSortedMap(reverseOrder())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (groupedByDate.isEmpty()) {
            item {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.diary_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.diary_empty_cta),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            groupedByDate.forEach { (dateStr, dayMeals) ->
                item(key = "header_$dateStr") {
                    Text(
                        text = try {
                            LocalDate.parse(dateStr).format(dateHeaderFormatter)
                        } catch (_: Exception) { dateStr },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(dayMeals, key = { it.id }) { meal ->
                    DiaryMealCard(meal = meal)
                }
            }
        }
    }
}

@Composable
private fun DiaryMealCard(meal: LoggedMeal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = meal.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${meal.calories.toInt()} cal • P ${meal.proteinG.toInt()}g / C ${meal.carbsG.toInt()}g / F ${meal.fatG.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
