package com.swappy.aicalcount.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.swappy.aicalcount.R
import com.swappy.aicalcount.data.diet.ActivityLevel
import com.swappy.aicalcount.data.diet.DietGoal
import com.swappy.aicalcount.data.diet.DietPreferences
import com.swappy.aicalcount.data.diet.DietRestriction
import com.swappy.aicalcount.data.profile.UserProfileRepository
import androidx.compose.ui.tooling.preview.Preview
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import kotlinx.coroutines.launch

private const val STEP_PHOTO = 0
private const val STEP_NAME = 1
private const val STEP_WEIGHT_HEIGHT_AGE = 2
private const val STEP_GOAL = 3
private const val STEP_ACTIVITY = 4
private const val STEP_RESTRICTIONS = 5
private const val TOTAL_STEPS = 6

@Composable
fun ProfileSetupScreen(
    onComplete: (displayName: String, weightKg: Float, heightCm: Float, age: Int, preferences: DietPreferences, profilePhotoPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    var profilePhotoPath by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(DietGoal.Maintain) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.Medium) }
    var restrictions by remember { mutableStateOf(setOf<DietRestriction>()) }

    val ctx = LocalContext.current
    val appContext = ctx.applicationContext
    val profileRepo = remember(appContext) { UserProfileRepository(appContext) }
    val scope = rememberCoroutineScope()

    val takeProfilePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bmp ->
        bmp?.let { bitmap ->
            scope.launch {
                val path = profileRepo.saveProfilePhoto(bitmap)
                profilePhotoPath = path
            }
        }
    }
    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) takeProfilePicture.launch(null)
    }
    val pickProfileImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { u ->
            profileRepo.saveProfilePhotoFromUri(u)?.let { path ->
                profilePhotoPath = path
            }
        }
    }

    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(24.dp)
    ) {
        if (step == STEP_PHOTO) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { step++ }) {
                    Text(stringResource(R.string.profile_photo_skip))
                }
            }
        }

        Text(
            text = stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (step) {
                STEP_PHOTO -> stringResource(R.string.profile_setup_photo_subtitle)
                else -> stringResource(R.string.profile_setup_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            STEP_PHOTO -> {
                Text(
                    text = stringResource(R.string.profile_setup_photo_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (profilePhotoPath.isNotEmpty()) {
                    AsyncImage(
                        model = profilePhotoPath,
                        contentDescription = stringResource(R.string.profile_photo_desc),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takeProfilePicture.launch(null)
                            } else {
                                requestCameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.profile_photo_selfie), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Card(
                        onClick = {
                            pickProfileImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.profile_photo_gallery), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
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
                            ),
                            profilePhotoPath
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
        ProfileSetupScreen(onComplete = { _, _, _, _, _, _ -> })
    }
}
