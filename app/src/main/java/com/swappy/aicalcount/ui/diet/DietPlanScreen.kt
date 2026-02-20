package com.swappy.aicalcount.ui.diet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.data.diet.ActivityLevel
import com.swappy.aicalcount.data.diet.DietGoal
import com.swappy.aicalcount.data.diet.DietPreferences
import com.swappy.aicalcount.data.diet.DietRestriction

@Composable
fun DietPlanScreen(
    step: Int,
    preferences: DietPreferences,
    onGoalSelect: (DietGoal) -> Unit,
    onActivitySelect: (ActivityLevel) -> Unit,
    onRestrictionToggle: (DietRestriction) -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.diet_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = stringResource(R.string.diet_intro),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        when (step) {
            0 -> {
                Text(stringResource(R.string.diet_goal), style = MaterialTheme.typography.titleMedium)
                DietGoal.entries.forEach { goal ->
                    RadioButtonRow(
                        text = when (goal) {
                            DietGoal.LoseWeight -> stringResource(R.string.diet_goal_lose)
                            DietGoal.Maintain -> stringResource(R.string.diet_goal_maintain)
                            DietGoal.GainMuscle -> stringResource(R.string.diet_goal_gain)
                        },
                        selected = preferences.goal == goal,
                        onClick = { onGoalSelect(goal) },
                    )
                }
            }
            1 -> {
                Text(stringResource(R.string.diet_activity), style = MaterialTheme.typography.titleMedium)
                ActivityLevel.entries.forEach { level ->
                    RadioButtonRow(
                        text = when (level) {
                            ActivityLevel.Low -> stringResource(R.string.diet_activity_low)
                            ActivityLevel.Medium -> stringResource(R.string.diet_activity_medium)
                            ActivityLevel.High -> stringResource(R.string.diet_activity_high)
                        },
                        selected = preferences.activityLevel == level,
                        onClick = { onActivitySelect(level) },
                    )
                }
            }
            2 -> {
                Text(stringResource(R.string.diet_restrictions), style = MaterialTheme.typography.titleMedium)
                RadioButtonRow(
                    text = stringResource(R.string.diet_restrictions_none),
                    selected = preferences.restrictions.isEmpty(),
                    onClick = { onRestrictionToggle(DietRestriction.None) },
                )
                listOf(DietRestriction.Vegetarian, DietRestriction.Vegan, DietRestriction.GlutenFree).forEach { rest ->
                    RadioButtonRow(
                        text = when (rest) {
                            DietRestriction.Vegetarian -> stringResource(R.string.diet_restrictions_vegetarian)
                            DietRestriction.Vegan -> stringResource(R.string.diet_restrictions_vegan)
                            DietRestriction.GlutenFree -> stringResource(R.string.diet_restrictions_gluten_free)
                            else -> rest.name
                        },
                        selected = preferences.restrictions.contains(rest),
                        onClick = { onRestrictionToggle(rest) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (step < 2) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.diet_next))
            }
        } else {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.diet_done))
            }
        }
    }
}

@Composable
private fun RadioButtonRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, name = "Diet plan")
@Composable
fun DietPlanScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        DietPlanScreen(
            step = 0,
            preferences = DietPreferences(goal = DietGoal.LoseWeight),
            onGoalSelect = {},
            onActivitySelect = {},
            onRestrictionToggle = {},
            onNext = {},
            onDone = {},
        )
    }
}
