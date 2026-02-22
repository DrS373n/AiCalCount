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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
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
import com.swappy.aicalcount.data.health.HealthConnectRepository
import com.swappy.aicalcount.data.meals.LoggedMeal
import com.swappy.aicalcount.data.meals.MealDiaryRepository
import com.swappy.aicalcount.data.profile.UserProfile
import com.swappy.aicalcount.data.profile.UserProfileRepository
import com.swappy.aicalcount.navigation.NavRoutes
import com.swappy.aicalcount.util.ApiUsageManager
import com.swappy.aicalcount.ui.barcode.BarcodeScanScreen
import com.swappy.aicalcount.ui.compare.CompareScreen
import com.swappy.aicalcount.ui.dashboard.DiaryTabScreen
import com.swappy.aicalcount.ui.dashboard.HomeTabScreen
import com.swappy.aicalcount.ui.dashboard.ProgressTabScreen
import com.swappy.aicalcount.ui.dashboard.ProfileTabScreen
import com.swappy.aicalcount.ui.describe.DescribeScreen
import com.swappy.aicalcount.ui.diet.DietPlanScreen
import com.swappy.aicalcount.ui.nutrition.NutritionDetailScreen
import com.swappy.aicalcount.ui.onboarding.OnboardingScreen
import com.swappy.aicalcount.ui.onboarding.ProfileSetupScreen
import com.swappy.aicalcount.ui.qa.GenerativeMealQaViewModel
import com.swappy.aicalcount.ui.chat.ChatScreen
import com.swappy.aicalcount.ui.chat.ChatViewModel
import com.swappy.aicalcount.ui.coach.CoachScreen
import com.swappy.aicalcount.ui.coach.CoachViewModel
import com.swappy.aicalcount.ui.qa.MealQaScreen
import com.swappy.aicalcount.ui.recipe.RecipeCalculatorScreen
import com.swappy.aicalcount.ui.scan.ScanScreen
import com.swappy.aicalcount.ui.theme.AiCalCountTheme
import com.swappy.aicalcount.util.AppError
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
    todayMeals: List<LoggedMeal>,
    lastUploadedRecipe: com.swappy.aicalcount.network.Recipe?,
    lastUploadedImage: android.graphics.Bitmap?,
    onNavigateToScan: () -> Unit,
    onNavigateToNutrition: (String) -> Unit,
    onAddHydration: () -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val onboardingRepo = remember(appContext) { OnboardingRepository(appContext) }
    val progressRepo = remember(appContext) { ProgressRepository(appContext) }
    val dietPrefsRepo = remember(appContext) { DietPreferencesRepository(appContext) }
    val hasLoggedMeal by onboardingRepo.hasLoggedMeal.collectAsState(initial = false)
    val todayProtein by progressRepo.todayProtein.collectAsState(initial = 0f)
    val todayCarbs by progressRepo.todayCarbs.collectAsState(initial = 0f)
    val todayFat by progressRepo.todayFat.collectAsState(initial = 0f)
    val todayFiber by progressRepo.todayFiber.collectAsState(initial = 0f)
    val todaySugar by progressRepo.todaySugar.collectAsState(initial = 0f)
    val todaySodium by progressRepo.todaySodium.collectAsState(initial = 0f)
    val hydrationGlasses by progressRepo.hydrationGlasses.collectAsState(initial = 0)
    val hydrationGoalGlasses by progressRepo.hydrationGoalGlasses.collectAsState(initial = 8)
    val streakCount by progressRepo.streakCount.collectAsState(initial = 0)
    val loggedDates by progressRepo.loggedDates.collectAsState(initial = emptySet())
    val preferences by dietPrefsRepo.preferences.collectAsState(initial = DietPreferences())
    val profileRepo = remember(appContext) { UserProfileRepository(appContext) }
    val profile by profileRepo.profile.collectAsState(initial = UserProfile())
    val goals = remember(profile, preferences) { com.swappy.aicalcount.data.diet.DietGoalHelper.computeGoals(profile, preferences) }
    val todayCalories = todayProtein * 4f + todayCarbs * 4f + todayFat * 9f
    val todayGoalMet = goals.calories > 0f && todayCalories >= goals.calories
    val todayStr = java.time.LocalDate.now().toString()
    val datesWithGoalsMet = remember(loggedDates, todayGoalMet, todayStr) {
        if (todayGoalMet) loggedDates + todayStr else loggedDates
    }
    HomeTabScreen(
        isNewUser = !hasLoggedMeal,
        todayProtein = todayProtein,
        todayCarbs = todayCarbs,
        todayFat = todayFat,
        goalCalories = goals.calories,
        goalProtein = goals.proteinG,
        goalCarbs = goals.carbsG,
        goalFat = goals.fatG,
        todayFiber = todayFiber,
        todaySugar = todaySugar,
        todaySodium = todaySodium,
        goalFiber = 25f,
        goalSugar = 50f,
        goalSodium = 2300f,
        hydrationGlasses = hydrationGlasses,
        hydrationGoalGlasses = hydrationGoalGlasses,
        streakCount = streakCount,
        datesWithGoalsMet = datesWithGoalsMet,
        todayMeals = todayMeals,
        lastUploadedRecipe = lastUploadedRecipe,
        lastUploadedImage = lastUploadedImage,
        onAddHydration = onAddHydration,
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
    val pendingDietSummary by repository.pendingDietSummary.collectAsState(initial = false)
    val preferences by dietPrefsRepo.preferences.collectAsState(initial = DietPreferences())
    val profile by profileRepo.profile.collectAsState(initial = UserProfile())
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
        !profileSetupComplete && pendingDietSummary -> DietPlanSummaryScreen(
            profile = profile,
            preferences = preferences,
            onStartOver = {
                scope.launch {
                    repository.setPendingDietSummary(false)
                }
            },
            onNext = {
                scope.launch {
                    repository.setProfileSetupComplete()
                }
            },
        )
        !profileSetupComplete -> Box(modifier = Modifier.fillMaxSize().padding(top = 84.dp)) {
            ProfileSetupScreen(
                onComplete = { displayName, biologicalSex, weightKg, heightCm, goalWeightKg, birthdate, preferences, photoPath ->
                    scope.launch {
                        val age = birthdate?.let { java.time.temporal.ChronoUnit.YEARS.between(it, java.time.LocalDate.now()).toInt() } ?: 0
                        val inferredGoal = when {
                            goalWeightKg < weightKg -> com.swappy.aicalcount.data.diet.DietGoal.LoseWeight
                            goalWeightKg > weightKg -> com.swappy.aicalcount.data.diet.DietGoal.GainMuscle
                            else -> com.swappy.aicalcount.data.diet.DietGoal.Maintain
                        }
                        profileRepo.save(
                            UserProfile(
                                displayName = displayName,
                                biologicalSex = biologicalSex,
                                weightKg = weightKg,
                                goalWeightKg = goalWeightKg,
                                heightCm = heightCm,
                                birthdateEpochDay = birthdate?.toEpochDay(),
                                age = age,
                                profilePhotoPath = photoPath,
                            ),
                        )
                        dietPrefsRepo.save(preferences.copy(goal = inferredGoal))
                        repository.setPendingDietSummary(true)
                    }
                },
            )
        }
        else -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppNavHost(modifier = Modifier.padding(innerPadding))
        }
    }
}

private val tabRoutes = setOf(
    NavRoutes.Home,
    NavRoutes.Diary,
    NavRoutes.Progress,
    NavRoutes.Profile,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as Application
    val mainViewModel: MainViewModel = viewModel(factory = AppViewModelFactory(application))
    val showApiLimit by mainViewModel.showApiLimitNotification.collectAsState()
    val appError by mainViewModel.appError.collectAsState()
    val snackbarKey by mainViewModel.snackbarMessageKey.collectAsState()

    AppNavHostStateless(
        modifier = modifier,
        showApiLimit = showApiLimit,
        appError = appError,
        snackbarKey = snackbarKey,
        onDismissApiLimitNotification = mainViewModel::dismissApiLimitNotification,
        onDismissError = mainViewModel::dismissError,
        onClearSnackbarMessage = mainViewModel::clearSnackbarMessage,
        mainViewModel = mainViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHostStateless(
    modifier: Modifier = Modifier,
    showApiLimit: Boolean,
    appError: AppError?,
    snackbarKey: String?,
    onDismissApiLimitNotification: () -> Unit,
    onDismissError: () -> Unit,
    onClearSnackbarMessage: () -> Unit,
    mainViewModel: MainViewModel? = null
) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in tabRoutes
    val showBack = currentRoute != null && currentRoute !in tabRoutes
    val snackbarHostState = remember { SnackbarHostState() }
    var showFabSheet by remember { mutableStateOf(false) }
    val fabSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val apiUsageManager = remember(application) { ApiUsageManager(application) }
    var showNearLimitWarning by remember { mutableStateOf(false) }
    var pendingNavAfterWarning by remember { mutableStateOf<(() -> Unit)?>(null) }

    val snackbarAddedMessage = stringResource(R.string.snackbar_added_to_day)
    LaunchedEffect(snackbarKey) {
        snackbarKey?.let { key ->
            if (key == "added_to_day") {
                snackbarHostState.showSnackbar(
                    message = snackbarAddedMessage,
                    duration = SnackbarDuration.Short
                )
                onClearSnackbarMessage()
            }
        }
    }

    if (showApiLimit) {
        AlertDialog(
            onDismissRequest = { onDismissApiLimitNotification() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.error_api_limit)) },
            confirmButton = {
                TextButton(onClick = { onDismissApiLimitNotification() }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    appError?.let { error ->
        val message = stringResource(error.messageRes) + (error.detail?.let { "\n$it" } ?: "")
        AlertDialog(
            onDismissRequest = { onDismissError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { onDismissError() }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    if (showNearLimitWarning) {
        val remaining = apiUsageManager.getRemainingCalls()
        AlertDialog(
            onDismissRequest = {
                showNearLimitWarning = false
                pendingNavAfterWarning = null
            },
            title = { Text(stringResource(R.string.api_near_limit_title)) },
            text = { Text(stringResource(R.string.api_near_limit_message, remaining)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingNavAfterWarning?.invoke()
                    showNearLimitWarning = false
                    pendingNavAfterWarning = null
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNearLimitWarning = false
                    pendingNavAfterWarning = null
                }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 8.dp) {
                    listOf(
                        "Home" to NavRoutes.Home,
                        "Diary" to NavRoutes.Diary,
                        "Progress" to NavRoutes.Progress,
                        "Profile" to NavRoutes.Profile,
                    ).forEach { (label, route) ->
                        val selected = currentRoute == route
                        val navIcon = when (label) {
                            "Home" -> Icons.Outlined.Home
                            "Diary" -> Icons.Outlined.Edit
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
                    onClick = { showFabSheet = true },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { topPadding ->
        if (showFabSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFabSheet = false },
                sheetState = fabSheetState,
            ) {
                Column(Modifier.padding(vertical = 16.dp)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.home_scan_food)) },
                        leadingContent = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                        modifier = Modifier
                            .clickable {
                                val remaining = apiUsageManager.getRemainingCalls()
                                if (remaining in 1..20) {
                                    pendingNavAfterWarning = { navController.navigate(NavRoutes.Scan); showFabSheet = false }
                                    showNearLimitWarning = true
                                } else {
                                    showFabSheet = false
                                    navController.navigate(NavRoutes.Scan)
                                }
                            }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.home_describe_meal)) },
                        leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        modifier = Modifier
                            .clickable {
                                val remaining = apiUsageManager.getRemainingCalls()
                                if (remaining in 1..20) {
                                    pendingNavAfterWarning = { navController.navigate(NavRoutes.Describe); showFabSheet = false }
                                    showNearLimitWarning = true
                                } else {
                                    showFabSheet = false
                                    navController.navigate(NavRoutes.Describe)
                                }
                            }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.barcode_title)) },
                        leadingContent = { Icon(Icons.Outlined.DocumentScanner, contentDescription = null) },
                        modifier = Modifier
                            .clickable {
                                val remaining = apiUsageManager.getRemainingCalls()
                                if (remaining in 1..20) {
                                    pendingNavAfterWarning = { navController.navigate(NavRoutes.BarcodeScan); showFabSheet = false }
                                    showNearLimitWarning = true
                                } else {
                                    showFabSheet = false
                                    navController.navigate(NavRoutes.BarcodeScan)
                                }
                            }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.home_voice_meal)) },
                        leadingContent = { Icon(Icons.Outlined.Mic, contentDescription = null) },
                        modifier = Modifier
                            .clickable {
                                val remaining = apiUsageManager.getRemainingCalls()
                                if (remaining in 1..20) {
                                    pendingNavAfterWarning = { navController.navigate(NavRoutes.Describe); showFabSheet = false }
                                    showNearLimitWarning = true
                                } else {
                                    showFabSheet = false
                                    navController.navigate(NavRoutes.Describe)
                                }
                            }
                    )
                }
            }
        }
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier
                .padding(topPadding)
                .then(Modifier.statusBarsPadding())
                .then(
                    // Push main-tab content (Home, etc.) below the app bar
                    if (showBottomBar) Modifier.padding(top = 84.dp) else Modifier
                ),
        ) {
            composable(NavRoutes.Home) {
                val scope = rememberCoroutineScope()
                val appContext = LocalContext.current.applicationContext
                val onboardingRepo = remember(application) { OnboardingRepository(application) }
                val progressRepo = remember(appContext) { ProgressRepository(appContext) }
                val mealDiaryRepo = remember(appContext) { MealDiaryRepository(appContext) }
                val todayStr = java.time.LocalDate.now().toString()
                val todayMeals by mealDiaryRepo.mealsForDate(todayStr).collectAsState(initial = emptyList())
                val firstRunTooltipMessage = stringResource(R.string.first_run_fab_tooltip)
                val lastUploadedRecipe by mainViewModel?.recipe?.collectAsState(initial = null) ?: remember { mutableStateOf<com.swappy.aicalcount.network.Recipe?>(null) }
                val lastUploadedImage by mainViewModel?.bitmap?.collectAsState(initial = null) ?: remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                LaunchedEffect(Unit) {
                    if (!onboardingRepo.getHasSeenFabTooltip()) {
                        snackbarHostState.showSnackbar(
                            message = firstRunTooltipMessage,
                            duration = SnackbarDuration.Long
                        )
                        onboardingRepo.setHasSeenFabTooltip()
                    }
                }
                HomeRoute(
                    todayMeals = todayMeals,
                    lastUploadedRecipe = lastUploadedRecipe,
                    lastUploadedImage = lastUploadedImage,
                    onNavigateToScan = { navController.navigate(NavRoutes.Scan) },
                    onNavigateToNutrition = { navController.navigate(NavRoutes.NutritionDetail) },
                    onAddHydration = { scope.launch { progressRepo.addHydrationGlass() } },
                )
            }
            composable(NavRoutes.Diary) {
                val appContext = LocalContext.current.applicationContext
                val mealDiaryRepo = remember(appContext) { MealDiaryRepository(appContext) }
                val allMeals by mealDiaryRepo.allMeals.collectAsState(initial = emptyList())
                DiaryTabScreen(
                    meals = allMeals,
                    onTapToLog = { navController.navigate(NavRoutes.Scan) },
                )
            }
            composable(NavRoutes.Progress) {
                val appContext = LocalContext.current.applicationContext
                val profileRepo = remember(appContext) { UserProfileRepository(appContext) }
                val progressRepo = remember(appContext) { ProgressRepository(appContext) }
                val mealDiaryRepo = remember(appContext) { MealDiaryRepository(appContext) }
                val dietPrefsRepo = remember(appContext) { DietPreferencesRepository(appContext) }
                val profile by profileRepo.profile.collectAsState(initial = UserProfile())
                val preferences by dietPrefsRepo.preferences.collectAsState(initial = DietPreferences())
                val goals = remember(profile, preferences) { com.swappy.aicalcount.data.diet.DietGoalHelper.computeGoals(profile, preferences) }
                val allMeals by mealDiaryRepo.allMeals.collectAsState(initial = emptyList())
                val weeklyCalories = remember(allMeals) {
                    val todayRef = java.time.LocalDate.now()
                    (0..6).map { offset ->
                        val d = todayRef.minusDays(offset.toLong())
                        val dateStr = d.toString()
                        val total = allMeals.filter { it.date == dateStr }.sumOf { it.calories.toDouble() }.toFloat()
                        dateStr to total
                    }.reversed()
                }
                val streakCount by progressRepo.streakCount.collectAsState(initial = 0)
                val loggedDates by progressRepo.loggedDates.collectAsState(initial = emptySet())
                val weightHistory by progressRepo.weightHistory.collectAsState(initial = emptyList())
                var showLogWeightDialog by remember { mutableStateOf(false) }
                var logWeightInput by remember { mutableStateOf(profile.weightKg.toString().takeIf { it != "0.0" } ?: "") }
                val scope = rememberCoroutineScope()
                val progressToday = java.time.LocalDate.now()
                val startOfWeek = progressToday.with(java.time.DayOfWeek.SUNDAY)
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
                                        progressRepo.addWeightEntry(progressToday.toString(), kg)
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
                val healthRepo = remember(appContext) { HealthConnectRepository(appContext) }
                val progressImportedMessage = stringResource(R.string.progress_imported_weight)
                val progressImportFailedMessage = stringResource(R.string.progress_import_failed)
                ProgressTabScreen(
                    currentWeightKg = profile.weightKg,
                    goalWeightKg = profile.goalWeightKg,
                    weightHistory = weightHistory,
                    streakCount = streakCount,
                    weekDaysLogged = weekDaysLogged,
                    weeklyCalories = weeklyCalories,
                    goalCalories = goals.calories,
                    onLogWeight = {
                        logWeightInput = profile.weightKg.toString().takeIf { it != "0.0" } ?: ""
                        showLogWeightDialog = true
                    },
                    onNavigateToCompare = { navController.navigate(NavRoutes.Compare) },
                    onImportFromHealthConnect = {
                        scope.launch {
                            try {
                                val latest = healthRepo.getLatestWeight()
                                if (latest != null) {
                                    val (kg, date) = latest
                                    progressRepo.addWeightEntry(date.toString(), kg.toFloat())
                                    profileRepo.save(profile.copy(weightKg = kg.toFloat()))
                                    snackbarHostState.showSnackbar(progressImportedMessage)
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar(progressImportFailedMessage)
                            }
                        }
                    },
                    healthConnectAvailable = healthRepo.isAvailable,
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
                    onNavigateToRecipeCalculator = { navController.navigate(NavRoutes.RecipeCalculator) },
                    onNavigateToCoach = { navController.navigate(NavRoutes.Coach) },
                    onNavigateToChat = { navController.navigate(NavRoutes.Chat) },
                    onNavigateToWeightTracker = { navController.navigate(NavRoutes.Progress) },
                    apiRemainingCalls = apiUsageManager.getRemainingCalls(),
                )
            }
            composable(NavRoutes.Scan) {
                val ctx = LocalContext.current
                val bitmap by mainViewModel!!.bitmap.collectAsState()
                val recipe by mainViewModel.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val appError by mainViewModel.appError.collectAsState()
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
                    appError = appError,
                    onTakePhoto = onTakePhoto,
                    onPickImage = { pickImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRetry = { mainViewModel.retryAnalyze(SPOONACULAR_API_KEY) },
                    onCorrectIdentification = { mainViewModel.correctIdentification(it, SPOONACULAR_API_KEY) },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.Describe) {
                val ctx = LocalContext.current
                val recipe by mainViewModel!!.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val hasVoicePermission = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                val requestVoicePermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { }
                DescribeScreen(
                    loading = loading,
                    recipe = recipe,
                    onDescribeSubmit = { mainViewModel.analyzeFromText(it, SPOONACULAR_API_KEY) },
                    hasVoicePermission = hasVoicePermission,
                    onRequestVoicePermission = { requestVoicePermission.launch(Manifest.permission.RECORD_AUDIO) },
                )
            }
            composable(NavRoutes.LabelUpload) {
                val ctx = LocalContext.current
                val bitmap by mainViewModel!!.bitmap.collectAsState()
                val recipe by mainViewModel.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val appError by mainViewModel.appError.collectAsState()
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
                    appError = appError,
                    onTakePhoto = onTakePhoto,
                    onPickImage = { pickImage.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRetry = { mainViewModel.retryAnalyze(SPOONACULAR_API_KEY) },
                    onCorrectIdentification = { mainViewModel.correctIdentification(it, SPOONACULAR_API_KEY) },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.BarcodeScan) {
                val recipe by mainViewModel!!.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                BarcodeScanScreen(
                    loading = loading,
                    recipe = recipe,
                    onLookup = { mainViewModel.lookupBarcode(it, SPOONACULAR_API_KEY) },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.Coach) {
                val coachViewModel: CoachViewModel = viewModel(factory = AppViewModelFactory(application))
                val todayTip by coachViewModel.todayTip.collectAsState()
                val weeklySummary by coachViewModel.weeklySummary.collectAsState()
                val coachLoading by coachViewModel.loading.collectAsState()
                CoachScreen(
                    todayTip = todayTip,
                    weeklySummary = weeklySummary,
                    loading = coachLoading,
                    onGetTip = coachViewModel::loadTodayTip,
                    onGetSummary = coachViewModel::loadWeeklySummary,
                )
            }
            composable(NavRoutes.Chat) {
                val chatViewModel: ChatViewModel = viewModel(factory = AppViewModelFactory(application))
                val chatMessages by chatViewModel.messages.collectAsState()
                val chatLoading by chatViewModel.loading.collectAsState()
                ChatScreen(
                    messages = chatMessages,
                    loading = chatLoading,
                    onSendMessage = chatViewModel::sendMessage,
                )
            }
            composable(NavRoutes.RecipeCalculator) {
                val recipe by mainViewModel!!.recipe.collectAsState()
                val loading by mainViewModel.loading.collectAsState()
                val scope = rememberCoroutineScope()
                RecipeCalculatorScreen(
                    loading = loading,
                    recipe = recipe,
                    onCalculate = { mainViewModel.analyzeFromText(it, SPOONACULAR_API_KEY) },
                    onAddServing = { r, s -> scope.launch { mainViewModel.addRecipeServingToToday(r, s) } },
                    onClear = mainViewModel::clearRecipe,
                )
            }
            composable(NavRoutes.MealQa) {
                val qaViewModel: GenerativeMealQaViewModel = viewModel()
                val answer by qaViewModel.answer.collectAsState()
                val conversationHistory by qaViewModel.conversationHistory.collectAsState()
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
                    conversationHistory = conversationHistory,
                    onSendQuestion = qaViewModel::sendQuestion,
                    onPredefinedClick = qaViewModel::sendQuestion,
                )
            }
            composable(NavRoutes.DietPlan) {
                val dietViewModel: DietPlanViewModel = viewModel(factory = AppViewModelFactory(application))
                val step by dietViewModel.step.collectAsState()
                val preferences by dietViewModel.preferences.collectAsState()
                val planComplete by dietViewModel.planComplete.collectAsState()
                val profileRepo = remember(application) { UserProfileRepository(application) }
                val profile by profileRepo.profile.collectAsState(initial = UserProfile())
                if (planComplete) {
                    DietPlanSummaryScreen(
                        profile = profile,
                        preferences = preferences,
                        onStartOver = { dietViewModel.resetPlanView() },
                        onNext = {
                            navController.navigate(NavRoutes.Home) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
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
    profile: UserProfile,
    preferences: DietPreferences,
    onStartOver: () -> Unit,
    onNext: () -> Unit,
) {
    val goals = remember(profile, preferences) {
        com.swappy.aicalcount.data.diet.DietGoalHelper.computeGoals(profile, preferences)
    }
    val bmr = remember(profile) {
        com.swappy.aicalcount.data.diet.DietGoalHelper.computeBmr(
            profile.weightKg, profile.heightCm, profile.age, profile.biologicalSex
        )
    }
    val tdee = remember(bmr, preferences) {
        bmr?.let { com.swappy.aicalcount.data.diet.DietGoalHelper.computeTdee(it, preferences.activityLevel) }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.nutrition_plan_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.nutrition_plan_goal_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = when (preferences.goal) {
                com.swappy.aicalcount.data.diet.DietGoal.LoseWeight -> stringResource(R.string.diet_goal_lose)
                com.swappy.aicalcount.data.diet.DietGoal.Maintain -> stringResource(R.string.diet_goal_maintain)
                com.swappy.aicalcount.data.diet.DietGoal.GainMuscle -> stringResource(R.string.diet_goal_gain)
            } + " • " + when (preferences.goalPace) {
                com.swappy.aicalcount.data.diet.GoalPace.Slowly -> stringResource(R.string.goal_pace_slowly)
                com.swappy.aicalcount.data.diet.GoalPace.Steadily -> stringResource(R.string.goal_pace_steadily)
                com.swappy.aicalcount.data.diet.GoalPace.Quickly -> stringResource(R.string.goal_pace_quickly)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nutrition_plan_daily_targets),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.nutrition_plan_targets_value,
                goals.calories.toInt(),
                goals.proteinG.toInt(),
                goals.fatG.toInt(),
                goals.carbsG.toInt(),
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nutrition_plan_details),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.nutrition_plan_details_value,
                profile.age,
                profile.heightCm.toInt(),
                profile.weightKg.toInt(),
                profile.goalWeightKg.toInt(),
                when (preferences.activityLevel) {
                    com.swappy.aicalcount.data.diet.ActivityLevel.Sedentary -> stringResource(R.string.diet_activity_sedentary)
                    com.swappy.aicalcount.data.diet.ActivityLevel.LightlyActive -> stringResource(R.string.diet_activity_lightly_active)
                    com.swappy.aicalcount.data.diet.ActivityLevel.ModeratelyActive -> stringResource(R.string.diet_activity_moderately_active)
                    com.swappy.aicalcount.data.diet.ActivityLevel.HighlyActive -> stringResource(R.string.diet_activity_highly_active)
                },
                (bmr?.toInt() ?: 0).toString(),
                (tdee?.toInt() ?: 0).toString(),
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nutrition_plan_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.nutrition_plan_lets_go))
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onStartOver, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.diet_start_over))
        }
    }
}

@Preview(showBackground = true, name = "Diet plan summary")
@Composable
fun DietPlanSummaryScreenPreview() {
    AiCalCountTheme {
        DietPlanSummaryScreen(
            profile = UserProfile(age = 30, weightKg = 70f, goalWeightKg = 65f, heightCm = 170f),
            preferences = DietPreferences(
                goal = DietGoal.LoseWeight,
                activityLevel = ActivityLevel.ModeratelyActive,
                restrictions = emptyList()
            ),
            onStartOver = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeRoutePreview() {
    AiCalCountTheme {
        HomeRoute(
            todayMeals = emptyList(),
            lastUploadedRecipe = null,
            lastUploadedImage = null,
            onNavigateToScan = {},
            onNavigateToNutrition = {},
            onAddHydration = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostStatelessPreview() {
    AiCalCountTheme {
        AppNavHostStateless(
            showApiLimit = false,
            appError = null,
            snackbarKey = null,
            onDismissApiLimitNotification = {},
            onDismissError = {},
            onClearSnackbarMessage = {}
        )
    }
}
