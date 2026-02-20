package com.swappy.aicalcount

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swappy.aicalcount.data.diet.ActivityLevel
import com.swappy.aicalcount.data.diet.DietGoal
import com.swappy.aicalcount.data.diet.DietPreferences
import com.swappy.aicalcount.data.diet.DietPreferencesRepository
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository
import com.swappy.aicalcount.data.profile.UserProfile
import com.swappy.aicalcount.data.profile.UserProfileRepository
import com.swappy.aicalcount.navigation.NavRoutes
import com.swappy.aicalcount.ui.compare.CompareScreen
import com.swappy.aicalcount.ui.dashboard.HomeTabScreen
import com.swappy.aicalcount.ui.dashboard.ProgressTabScreen
import com.swappy.aicalcount.ui.dashboard.ProfileTabScreen
import com.swappy.aicalcount.ui.describe.DescribeScreen
import com.swappy.aicalcount.ui.diet.DietPlanScreen
import com.swappy.aicalcount.ui.nutrition.NutritionDetailScreen
import com.swappy.aicalcount.ui.onboarding.OnboardingScreen
import com.swappy.aicalcount.ui.onboarding.ProfileSetupScreen
import com.swappy.aicalcount.ui.qa.GenerativeMealQaViewModel
import com.swappy.aicalcount.ui.qa.MealQaScreen
import com.swappy.aicalcount.ui.scan.ScanScreen
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import com.swappy.aicalcount.util.SPOONACULAR_API_KEY
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiCalCountTheme {
                OnboardingGate()
            }
        }
    }
}

@Composable
private fun HomeRoute(
    onNavigateToScan: () -> Unit,
    onNavigateToNutrition: (String) -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val onboardingRepo = remember(appContext) { OnboardingRepository(appContext) }
    val progressRepo = remember(appContext) { ProgressRepository(appContext) }
    val hasLoggedMeal by onboardingRepo.hasLoggedMeal.collectAsState(initial = false)
    val todayProtein by progressRepo.todayProtein.collectAsState(initial = 0f)
    val todayCarbs by progressRepo.todayCarbs.collectAsState(initial = 0f)
    val todayFat by progressRepo.todayFat.collectAsState(initial = 0f)
    HomeTabScreen(
        isNewUser = !hasLoggedMeal,
        todayProtein = todayProtein,
        todayCarbs = todayCarbs,
        todayFat = todayFat,
        goalProtein = 150f,
        goalCarbs = 275f,
        goalFat = 70f,
        onNavigateToScan = onNavigateToScan,
        onNavigateToNutrition = onNavigateToNutrition,
    )
}

@Composable
private fun OnboardingGate() {
    val context = LocalContext.current
    val repository = remember { OnboardingRepository(context) }
    val profileRepo = remember { UserProfileRepository(context) }
    val dietPrefsRepo = remember { DietPreferencesRepository(context) }
    val completed by repository.isOnboardingComplete.collectAsState(initial = false)
    val profileSetupComplete by repository.isProfileSetupComplete.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    when {
        !completed -> OnboardingScreen(
            onComplete = {
                scope.launch {
                    repository.setOnboardingComplete()
                    repository.clearHasLoggedMeal()
                }
            },
            onSkip = {
                scope.launch {
                    repository.setOnboardingComplete()
                    repository.clearHasLoggedMeal()
                }
            },
        )
        !profileSetupComplete -> ProfileSetupScreen(
            onComplete = { displayName, weightKg, heightCm, age, preferences ->
                scope.launch {
                    profileRepo.save(
                        UserProfile(
                            displayName = displayName,
                            weightKg = weightKg,
                            heightCm = heightCm,
                            age = age,
                        ),
                    )
                    dietPrefsRepo.save(preferences)
                    repository.setProfileSetupComplete()
                }
            },
        )
        else -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppNavHost(modifier = Modifier.padding(innerPadding))
        }
    }
}

private val tabRoutes = setOf(
    NavRoutes.Home,
    NavRoutes.Progress,
    NavRoutes.Profile,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val mainViewModel: MainViewModel = viewModel(factory = AppViewModelFactory(application))
    val showApiLimit by mainViewModel.showApiLimitNotification.collectAsState()
    val appError by mainViewModel.appError.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in tabRoutes
    val showBack = currentRoute != null && currentRoute !in tabRoutes

    if (showApiLimit) {
        AlertDialog(
            onDismissRequest = { mainViewModel.dismissApiLimitNotification() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.error_api_limit)) },
            confirmButton = {
                TextButton(onClick = { mainViewModel.dismissApiLimitNotification() }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    appError?.let { error ->
        val message = stringResource(error.messageRes) + (error.detail?.let { "\n$it" } ?: "")
        AlertDialog(
            onDismissRequest = { mainViewModel.dismissError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { mainViewModel.dismissError() }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showBack) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.home_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 8.dp) {
                    listOf(
                        "Home" to NavRoutes.Home,
                        "Progress" to NavRoutes.Progress,
                        "Profile" to NavRoutes.Profile,
                    ).forEach { (label, route) ->
                        val selected = currentRoute == route
                        val navIcon = when (label) {
                            "Home" -> Icons.Outlined.Home
                            "Progress" -> Icons.Outlined.BarChart
                            "Profile" -> Icons.Outlined.Person
                            else -> Icons.Outlined.Home
                        }
                        NavigationBarItem(
                            icon = { Icon(navIcon, contentDescription = label) },
                            label = { Text(label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.Scan) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.home_scan_food),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { topPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier
                .padding(topPadding)
                .then(
                    // When there's no TopAppBar (Home), reserve space for status bar so content isn't clipped
                    if (!showBack) Modifier.statusBarsPadding() else Modifier
                ),
        ) {
            composable(NavRoutes.Home) {
                HomeRoute(
                    onNavigateToScan = { navController.navigate(NavRoutes.Scan) },
                    onNavigateToNutrition = { navController.navigate(NavRoutes.NutritionDetail) },
                )
            }
            composable(NavRoutes.Progress) {
                val appContext = LocalContext.current.applicationContext
                val profileRepo = remember(appContext) { UserProfileRepository(appContext) }
                val progressRepo = remember(appContext) { ProgressRepository(appContext) }
                val profile by profileRepo.profile.collectAsState(initial = UserProfile())
                val streakCount by progressRepo.streakCount.collectAsState(initial = 0)
                val loggedDates by progressRepo.loggedDates.collectAsState(initial = emptySet())
                val weightHistory by progressRepo.weightHistory.collectAsState(initial = emptyList())
                var showLogWeightDialog by remember { mutableStateOf(false) }
                var logWeightInput by remember { mutableStateOf(profile.weightKg.toString().takeIf { it != "0.0" } ?: "") }
                val scope = rememberCoroutineScope()
                val today = java.time.LocalDate.now()
                val startOfWeek = today.with(java.time.DayOfWeek.SUNDAY)
                val weekDaysLogged = (0..6).map { startOfWeek.plusDays(it.toLong()).toString() in loggedDates }
                if (showLogWeightDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogWeightDialog = false },
                        title = { Text(stringResource(R.string.progress_log_weight_dialog)) },
                        text = {
                            OutlinedTextField(
                                value = logWeightInput,
                                onValueChange = { logWeightInput = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text(stringResource(R.string.progress_log_weight_hint)) },
                                singleLine = true,
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val kg = logWeightInput.toFloatOrNull()
                                if (kg != null && kg > 0f && kg < 500f) {
                                    scope.launch {
                                        progressRepo.addWeightEntry(today.toString(), kg)
                                        profileRepo.save(profile.copy(weightKg = kg))
                                    }
                                    showLogWeightDialog = false
                                    logWeightInput = ""
                                }
                            }) { Text(stringResource(R.string.ok)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogWeightDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                    )
                }
                ProgressTabScreen(
                    currentWeightKg = profile.weightKg,
                    goalWeightKg = profile.goalWeightKg,
                    weightHistory = weightHistory,
                    streakCount = streakCount,
                    weekDaysLogged = weekDaysLogged,
                    onLogWeight = {
                        logWeightInput = profile.weightKg.toString().takeIf { it != "0.0" } ?: ""
                        showLogWeightDialog = true
                    },
                    onNavigateToCompare = { navController.navigate(NavRoutes.Compare) },
                )
            }
            composable(NavRoutes.Profile) {
                val ctx = LocalContext.current
                val appContext = ctx.applicationContext
                val profileRepo = remember(appContext) { UserProfileRepository(appContext) }
                val profile by profileRepo.profile.collectAsState(initial = UserProfile())
                var showPhotoSourceDialog by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val takeProfilePicture = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview(),
                ) { bmp ->
                    bmp?.let { bitmap ->
                        scope.launch {
                            val path = profileRepo.saveProfilePhoto(bitmap)
                            profileRepo.save(profile.copy(profilePhotoPath = path))
                        }
                    }
                }
                val requestProfileCameraPermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) takeProfilePicture.launch(null)
                }
                val pickProfileImage = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                ) { uri ->
                    uri?.let { u ->
                        profileRepo.saveProfilePhotoFromUri(u)?.let { path ->
                            scope.launch {
                                profileRepo.save(profile.copy(profilePhotoPath = path))
                            }
                        }
                    }
                }
                if (showPhotoSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showPhotoSourceDialog = false },
                        title = { Text(stringResource(R.string.profile_photo_edit)) },
                        text = {
                            Column {
                                TextButton(onClick = {
                                    showPhotoSourceDialog = false
                                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        takeProfilePicture.launch(null)
                                    } else {
                                        requestProfileCameraPermission.launch(Manifest.permission.CAMERA)
                                    }
                                }) { Text(stringResource(R.string.profile_photo_selfie)) }
                                TextButton(onClick = {
                                    showPhotoSourceDialog = false
                                    pickProfileImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) { Text(stringResource(R.string.profile_photo_gallery)) }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showPhotoSourceDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                    )
                }
                ProfileTabScreen(
                    profilePhotoPath = profile.profilePhotoPath.takeIf { it.isNotBlank() },
                    displayName = profile.displayName,
                    onChangePhotoClick = { showPhotoSourceDialog = true },
                    onNavigateToDietPlan = { navController.navigate(NavRoutes.DietPlan) },
                    onNavigateToCompare = { navController.navigate(NavRoutes.Compare) },
                    onNavigateToScan = { navController.navigate(NavRoutes.Scan) },
                    onNavigateToDescribe = { navController.navigate(NavRoutes.Describe) },
                    onNavigateToLabelUpload = { navController.navigate(NavRoutes.LabelUpload) },
                    onNavigateToMealQa = { navController.navigate(NavRoutes.MealQa) },
                )
            }
            composable(NavRoutes.Scan) {
                val ctx = LocalContext.current
                val bitmap by mainViewModel.bitmap.collectAsState()
                val recipe by mainViewModel.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val takePicture = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview(),
                ) { bmp ->
                    mainViewModel.setBitmapAndAnalyze(bmp, SPOONACULAR_API_KEY)
                }
                val requestCameraPermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) takePicture.launch(null)
                }
                val pickImage = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                ) { uri ->
                    uri?.let { u ->
                        ctx.contentResolver.openInputStream(u)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            mainViewModel.setBitmapAndAnalyze(bmp, SPOONACULAR_API_KEY)
                        }
                    }
                }
                val onTakePhoto = {
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        takePicture.launch(null)
                    } else {
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
                ScanScreen(
                    bitmap = bitmap,
                    recipe = recipe,
                    loading = loading,
                    isLabelMode = false,
                    onTakePhoto = onTakePhoto,
                    onPickImage = { pickImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.Describe) {
                val recipe by mainViewModel.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                DescribeScreen(
                    loading = loading,
                    recipe = recipe,
                    onDescribeSubmit = { mainViewModel.analyzeFromText(it, SPOONACULAR_API_KEY) },
                )
            }
            composable(NavRoutes.LabelUpload) {
                val ctx = LocalContext.current
                val bitmap by mainViewModel.bitmap.collectAsState()
                val recipe by mainViewModel.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val takePicture = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview(),
                ) { bmp ->
                    mainViewModel.setBitmapAndAnalyze(bmp, SPOONACULAR_API_KEY)
                }
                val requestCameraPermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) takePicture.launch(null)
                }
                val pickImage = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                ) { uri ->
                    uri?.let { u ->
                        ctx.contentResolver.openInputStream(u)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            mainViewModel.setBitmapAndAnalyze(bmp, SPOONACULAR_API_KEY)
                        }
                    }
                }
                val onTakePhoto = {
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        takePicture.launch(null)
                    } else {
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
                ScanScreen(
                    bitmap = bitmap,
                    recipe = recipe,
                    loading = loading,
                    isLabelMode = true,
                    onTakePhoto = onTakePhoto,
                    onPickImage = { pickImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.MealQa) {
                val qaViewModel: GenerativeMealQaViewModel = viewModel()
                val answer by qaViewModel.answer.collectAsState()
                val loading by qaViewModel.loading.collectAsState()
                val qaError by qaViewModel.appError.collectAsState()
                qaError?.let { err ->
                    val msg = stringResource(err.messageRes) + (err.detail?.let { "\n$it" } ?: "")
                    AlertDialog(
                        onDismissRequest = { qaViewModel.dismissError() },
                        title = { Text(stringResource(R.string.error)) },
                        text = { Text(msg) },
                        confirmButton = { TextButton(onClick = { qaViewModel.dismissError() }) { Text(stringResource(R.string.ok)) } },
                    )
                }
                MealQaScreen(
                    loading = loading,
                    answer = answer,
                    onSendQuestion = qaViewModel::sendQuestion,
                    onPredefinedClick = qaViewModel::sendQuestion,
                )
            }
            composable(NavRoutes.DietPlan) {
                val dietViewModel: DietPlanViewModel = viewModel(factory = AppViewModelFactory(application))
                val step by dietViewModel.step.collectAsState()
                val preferences by dietViewModel.preferences.collectAsState()
                val planComplete by dietViewModel.planComplete.collectAsState()
                if (planComplete) {
                    DietPlanSummaryScreen(
                        preferences = preferences,
                        onStartOver = { dietViewModel.resetPlanView() },
                    )
                } else {
                    DietPlanScreen(
                        step = step,
                        preferences = preferences,
                        onGoalSelect = dietViewModel::onGoalSelect,
                        onActivitySelect = dietViewModel::onActivitySelect,
                        onRestrictionToggle = dietViewModel::onRestrictionToggle,
                        onNext = dietViewModel::nextStep,
                        onDone = dietViewModel::completePlan,
                    )
                }
            }
            composable(NavRoutes.NutritionDetail) {
                NutritionDetailScreen(
                    onFixResults = { },
                    onDone = { navController.navigateUp() },
                )
            }
            composable(NavRoutes.Compare) {
                CompareScreen(onShare = { })
            }
        }
    }
}

@Composable
fun DietPlanSummaryScreen(
    preferences: DietPreferences,
    onStartOver: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.diet_plan_ready),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.diet_plan_summary),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Goal: ${preferences.goal.name}, Activity: ${preferences.activityLevel.name}, Restrictions: ${preferences.restrictions.joinToString { it.name }}",
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStartOver) {
            Text(stringResource(R.string.diet_start_over))
        }
    }
}

@Preview(showBackground = true, name = "Diet plan summary")
@Composable
fun DietPlanSummaryScreenPreview() {
    AiCalCountTheme {
        DietPlanSummaryScreen(
            preferences = DietPreferences(
                goal = DietGoal.LoseWeight,
                activityLevel = ActivityLevel.Medium,
                restrictions = emptyList()
            ),
            onStartOver = {}
        )
    }
}
