package com.swappy.aicalcount.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
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
import com.swappy.aicalcount.data.diet.GoalPace
import com.swappy.aicalcount.data.profile.BiologicalSex
import com.swappy.aicalcount.data.profile.UserProfileRepository
import androidx.compose.ui.tooling.preview.Preview
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val STEP_PHOTO = 0
private const val STEP_SEX = 1
private const val STEP_NAME = 2
private const val STEP_WEIGHT = 3
private const val STEP_HEIGHT = 4
private const val STEP_BIRTHDATE = 5
private const val STEP_TARGET_WEIGHT = 6
private const val STEP_GOAL_PACE = 7
private const val STEP_GOAL = 8
private const val STEP_ACTIVITY = 9
private const val STEP_RESTRICTIONS = 10
private const val STEP_GOOD_NEWS = 11
private const val STEP_LOADING = 12
private const val TOTAL_STEPS = 13

private const val WEIGHT_KG_MIN = 36f
private const val WEIGHT_KG_MAX = 181f
private const val WEIGHT_LB_MIN = 79f  // ~36 kg
private const val WEIGHT_LB_MAX = 399f // ~181 kg
private const val HEIGHT_CM_MIN = 122f
private const val HEIGHT_CM_MAX = 213f
private const val HEIGHT_IN_MIN = 48f   // ~122 cm
private const val HEIGHT_IN_MAX = 84f   // ~213 cm

private val birthdateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: (displayName: String, biologicalSex: BiologicalSex?, weightKg: Float, heightCm: Float, goalWeightKg: Float, birthdate: LocalDate?, preferences: DietPreferences, profilePhotoPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    var profilePhotoPath by remember { mutableStateOf("") }
    var biologicalSex by remember { mutableStateOf<BiologicalSex?>(null) }
    var displayName by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf(70f) }
    var useWeightLb by remember { mutableStateOf(false) }
    var heightCm by remember { mutableStateOf(170f) }
    var useHeightImperial by remember { mutableStateOf(false) }
    var goalWeightKg by remember { mutableStateOf(70f) }
    var useTargetWeightLb by remember { mutableStateOf(false) }
    var goalPace by remember { mutableStateOf(GoalPace.Steadily) }
    var selectedBirthdate by remember { mutableStateOf<LocalDate?>(null) }
    var showBirthdatePicker by remember { mutableStateOf(false) }
    var goal by remember { mutableStateOf(DietGoal.Maintain) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.ModeratelyActive) }
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
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
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
                STEP_SEX -> stringResource(R.string.profile_biological_sex_subtitle)
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
            STEP_SEX -> {
                Text(
                    text = stringResource(R.string.profile_biological_sex_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        BiologicalSex.MALE to stringResource(R.string.profile_biological_sex_male) to Icons.Filled.Male,
                        BiologicalSex.FEMALE to stringResource(R.string.profile_biological_sex_female) to Icons.Filled.Female,
                        BiologicalSex.OTHER to stringResource(R.string.profile_biological_sex_other) to Icons.Outlined.Person
                    ).forEach { (sexToLabel, icon) ->
                        val (sex, label) = sexToLabel
                        Card(
                            onClick = { biologicalSex = sex },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (biologicalSex == sex) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
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
            STEP_WEIGHT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(false to stringResource(R.string.profile_weight_kg), true to stringResource(R.string.profile_weight_lb)).forEach { (useLb, label) ->
                        FilterChip(
                            selected = useWeightLb == useLb,
                            onClick = { useWeightLb = useLb },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (useWeightLb) "${(weightKg * 2.20462f).toInt()} ${stringResource(R.string.profile_weight_lb)}" else "${weightKg.toInt()} ${stringResource(R.string.profile_weight_kg)}",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = if (useWeightLb) weightKg * 2.20462f else weightKg,
                    onValueChange = { value -> weightKg = if (useWeightLb) value / 2.20462f else value },
                    valueRange = if (useWeightLb) WEIGHT_LB_MIN..WEIGHT_LB_MAX else WEIGHT_KG_MIN..WEIGHT_KG_MAX,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            STEP_HEIGHT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(false to stringResource(R.string.profile_height_cm), true to stringResource(R.string.profile_height_ft_in)).forEach { (useImperial, label) ->
                        FilterChip(
                            selected = useHeightImperial == useImperial,
                            onClick = { useHeightImperial = useImperial },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val heightDisplay = if (useHeightImperial) {
                    val totalInches = (heightCm / 2.54f).toInt()
                    val ft = totalInches / 12
                    val ins = totalInches % 12
                    "${ft}${stringResource(R.string.profile_height_ft)} ${ins}${stringResource(R.string.profile_height_in)}"
                } else {
                    "${heightCm.toInt()} ${stringResource(R.string.profile_height_cm)}"
                }
                Text(
                    text = heightDisplay,
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = if (useHeightImperial) heightCm / 2.54f else heightCm,
                    onValueChange = { value -> heightCm = if (useHeightImperial) value * 2.54f else value },
                    valueRange = if (useHeightImperial) HEIGHT_IN_MIN..HEIGHT_IN_MAX else HEIGHT_CM_MIN..HEIGHT_CM_MAX,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            STEP_TARGET_WEIGHT -> {
                Text(
                    text = stringResource(R.string.profile_target_weight_current, weightKg.toInt(), stringResource(R.string.profile_weight_kg)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(false to stringResource(R.string.profile_weight_kg), true to stringResource(R.string.profile_weight_lb)).forEach { (useLb, label) ->
                        FilterChip(
                            selected = useTargetWeightLb == useLb,
                            onClick = { useTargetWeightLb = useLb },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (useTargetWeightLb) "${(goalWeightKg * 2.20462f).toInt()} ${stringResource(R.string.profile_weight_lb)}" else "${goalWeightKg.toInt()} ${stringResource(R.string.profile_weight_kg)}",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = if (useTargetWeightLb) goalWeightKg * 2.20462f else goalWeightKg,
                    onValueChange = { value -> goalWeightKg = if (useTargetWeightLb) value / 2.20462f else value },
                    valueRange = if (useTargetWeightLb) WEIGHT_LB_MIN..WEIGHT_LB_MAX else WEIGHT_KG_MIN..WEIGHT_KG_MAX,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            STEP_BIRTHDATE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedBirthdate?.format(birthdateFormatter) ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.profile_birthdate)) },
                        placeholder = { Text(stringResource(R.string.profile_birthdate_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Overlay so tap opens date picker (readOnly TextField consumes clicks otherwise)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showBirthdatePicker = true }
                    )
                }
            }
            STEP_GOAL_PACE -> {
                Text(
                    text = stringResource(R.string.goal_pace_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalPace.entries.forEach { pace ->
                        Card(
                            onClick = { goalPace = pace },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (goalPace == pace) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (pace) {
                                            GoalPace.Slowly -> stringResource(R.string.goal_pace_slowly)
                                            GoalPace.Steadily -> stringResource(R.string.goal_pace_steadily)
                                            GoalPace.Quickly -> stringResource(R.string.goal_pace_quickly)
                                        },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = when (pace) {
                                            GoalPace.Slowly -> stringResource(R.string.goal_pace_slowly_desc)
                                            GoalPace.Steadily -> stringResource(R.string.goal_pace_steadily_desc)
                                            GoalPace.Quickly -> stringResource(R.string.goal_pace_quickly_desc)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
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
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityLevel.entries.forEach { level ->
                        Card(
                            onClick = { activityLevel = level },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activityLevel == level) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (level) {
                                        ActivityLevel.Sedentary -> Icons.Outlined.Person
                                        ActivityLevel.LightlyActive -> Icons.Outlined.DirectionsWalk
                                        ActivityLevel.ModeratelyActive -> Icons.Outlined.DirectionsRun
                                        ActivityLevel.HighlyActive -> Icons.Outlined.FitnessCenter
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (level) {
                                            ActivityLevel.Sedentary -> stringResource(R.string.diet_activity_sedentary)
                                            ActivityLevel.LightlyActive -> stringResource(R.string.diet_activity_lightly_active)
                                            ActivityLevel.ModeratelyActive -> stringResource(R.string.diet_activity_moderately_active)
                                            ActivityLevel.HighlyActive -> stringResource(R.string.diet_activity_highly_active)
                                        },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = when (level) {
                                            ActivityLevel.Sedentary -> stringResource(R.string.diet_activity_sedentary_desc)
                                            ActivityLevel.LightlyActive -> stringResource(R.string.diet_activity_lightly_active_desc)
                                            ActivityLevel.ModeratelyActive -> stringResource(R.string.diet_activity_moderately_active_desc)
                                            ActivityLevel.HighlyActive -> stringResource(R.string.diet_activity_highly_active_desc)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            STEP_GOOD_NEWS -> {
                val lbsPerWeek = when (goalPace) {
                    GoalPace.Slowly -> 0.5f
                    GoalPace.Steadily -> 1f
                    GoalPace.Quickly -> 1.5f
                }
                val diffKg = kotlin.math.abs(weightKg - goalWeightKg)
                val weeksToGoal = if (lbsPerWeek > 0f && diffKg > 0f) (diffKg * 2.20462f / lbsPerWeek).toInt().coerceAtLeast(1) else 0
                val targetDate = LocalDate.now().plusWeeks(weeksToGoal.toLong())
                Text(
                    text = stringResource(R.string.good_news_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.good_news_weeks, weeksToGoal),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.good_news_target_date, targetDate.format(birthdateFormatter)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            STEP_LOADING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.creating_plan_loading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    onComplete(
                        displayName.trim(),
                        biologicalSex,
                        weightKg,
                        heightCm,
                        goalWeightKg,
                        selectedBirthdate,
                        DietPreferences(
                            goal = goal,
                            activityLevel = activityLevel,
                            goalPace = goalPace,
                            restrictions = restrictions.filter { it != DietRestriction.None }
                        ),
                        profilePhotoPath
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
        if (step != STEP_LOADING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (step == STEP_GOOD_NEWS) {
                            step = STEP_LOADING
                        } else {
                            step++
                        }
                    }
                ) {
                    Text(
                        if (step == STEP_GOOD_NEWS) stringResource(R.string.good_news_continue)
                        else stringResource(R.string.onboarding_next)
                    )
                }
            }
        }
        }
        if (showBirthdatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedBirthdate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                    ?: System.currentTimeMillis(),
                yearRange = (LocalDate.now().year - 120)..LocalDate.now().year
            )
            DatePickerDialog(
                onDismissRequest = { showBirthdatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedBirthdate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showBirthdatePicker = false
                        }
                    ) { Text(stringResource(R.string.onboarding_next)) }
                },
                dismissButton = {
                    TextButton(onClick = { showBirthdatePicker = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
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
        ProfileSetupScreen(onComplete = { _, _, _, _, _, _, _, _ -> })  // displayName, biologicalSex, weightKg, heightCm, goalWeightKg, birthdate, preferences, photoPath
    }
}
