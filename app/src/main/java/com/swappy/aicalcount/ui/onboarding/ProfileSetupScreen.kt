package com.swappy.aicalcount.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.data.diet.ActivityLevel
import com.swappy.aicalcount.data.diet.DietGoal
import com.swappy.aicalcount.data.diet.DietPreferences
import com.swappy.aicalcount.data.diet.DietRestriction
import androidx.compose.ui.tooling.preview.Preview
import com.swappy.aicalcount.ui.theme.AiCalCountTheme

private const val STEP_NAME = 0
private const val STEP_WEIGHT_HEIGHT_AGE = 1
private const val STEP_GOAL = 2
private const val STEP_ACTIVITY = 3
private const val STEP_RESTRICTIONS = 4
private const val TOTAL_STEPS = 5

@Composable
fun ProfileSetupScreen(
    onComplete: (displayName: String, weightKg: Float, heightCm: Float, age: Int, preferences: DietPreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    var displayName by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(DietGoal.Maintain) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.Medium) }
    var restrictions by remember { mutableStateOf(setOf<DietRestriction>()) }

    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            STEP_NAME -> {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.profile_name)) },
                    placeholder = { Text(stringResource(R.string.profile_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            STEP_WEIGHT_HEIGHT_AGE -> {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(R.string.profile_weight)) },
                    placeholder = { Text(stringResource(R.string.profile_weight_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(R.string.profile_height)) },
                    placeholder = { Text(stringResource(R.string.profile_height_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text(stringResource(R.string.profile_age)) },
                    placeholder = { Text(stringResource(R.string.profile_age_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            STEP_GOAL -> {
                Text(
                    stringResource(R.string.diet_goal),
                    style = MaterialTheme.typography.titleMedium
                )
                DietGoal.entries.forEach { g ->
                    ProfileSetupRadioRow(
                        text = when (g) {
                            DietGoal.LoseWeight -> stringResource(R.string.diet_goal_lose)
                            DietGoal.Maintain -> stringResource(R.string.diet_goal_maintain)
                            DietGoal.GainMuscle -> stringResource(R.string.diet_goal_gain)
                        },
                        selected = goal == g,
                        onClick = { goal = g }
                    )
                }
            }
            STEP_ACTIVITY -> {
                Text(
                    stringResource(R.string.diet_activity),
                    style = MaterialTheme.typography.titleMedium
                )
                ActivityLevel.entries.forEach { a ->
                    ProfileSetupRadioRow(
                        text = when (a) {
                            ActivityLevel.Low -> stringResource(R.string.diet_activity_low)
                            ActivityLevel.Medium -> stringResource(R.string.diet_activity_medium)
                            ActivityLevel.High -> stringResource(R.string.diet_activity_high)
                        },
                        selected = activityLevel == a,
                        onClick = { activityLevel = a }
                    )
                }
            }
            STEP_RESTRICTIONS -> {
                Text(
                    stringResource(R.string.diet_restrictions),
                    style = MaterialTheme.typography.titleMedium
                )
                listOf(
                    DietRestriction.None to stringResource(R.string.diet_restrictions_none),
                    DietRestriction.Vegetarian to stringResource(R.string.diet_restrictions_vegetarian),
                    DietRestriction.Vegan to stringResource(R.string.diet_restrictions_vegan),
                    DietRestriction.GlutenFree to stringResource(R.string.diet_restrictions_gluten_free)
                ).forEach { (r, label) ->
                    ProfileSetupRadioRow(
                        text = label,
                        selected = if (r == DietRestriction.None) restrictions.isEmpty()
                        else restrictions.contains(r),
                        onClick = {
                            if (r == DietRestriction.None) restrictions = emptySet()
                            else restrictions = if (restrictions.contains(r)) restrictions - r
                            else restrictions + r
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (step < TOTAL_STEPS - 1) {
                        step++
                    } else {
                        onComplete(
                            displayName.trim(),
                            weightText.toFloatOrNull() ?: 0f,
                            heightText.toFloatOrNull() ?: 0f,
                            ageText.toIntOrNull() ?: 0,
                            DietPreferences(
                                goal = goal,
                                activityLevel = activityLevel,
                                restrictions = restrictions.filter { it != DietRestriction.None }
                            )
                        )
                    }
                }
            ) {
                Text(
                    if (step < TOTAL_STEPS - 1) stringResource(R.string.onboarding_next)
                    else stringResource(R.string.profile_finish)
                )
            }
        }
    }
}

@Composable
private fun ProfileSetupRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, name = "Profile setup")
@Composable
fun ProfileSetupScreenPreview() {
    AiCalCountTheme {
        ProfileSetupScreen(onComplete = { _, _, _, _, _ -> })
    }
}
