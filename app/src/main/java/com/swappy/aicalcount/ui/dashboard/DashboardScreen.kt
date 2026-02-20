package com.swappy.aicalcount.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.tooling.preview.Preview
import com.swappy.aicalcount.R
import com.swappy.aicalcount.ui.theme.AiCalCountTheme

@Composable
fun DashboardScreen(
    navController: NavHostController = rememberNavController(),
    onFabClick: () -> Unit,
    content: @Composable (NavHostController) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomNavItems = listOf(
        BottomNavItem("Home", com.swappy.aicalcount.navigation.NavRoutes.Home, null),
        BottomNavItem("Progress", com.swappy.aicalcount.navigation.NavRoutes.Progress, null),
        BottomNavItem("Profile", com.swappy.aicalcount.navigation.NavRoutes.Profile, null)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { NavIcon(item) },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.home_scan_food))
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End
    ) { innerPadding ->
        content(navController)
        // Content is full screen; we pass padding to the NavHost in MainActivity
    }
}

private data class BottomNavItem(val label: String, val route: String, val icon: ImageVector?)

@Composable
private fun NavIcon(item: BottomNavItem) {
    val icon = when (item.label) {
        "Home" -> Icons.Outlined.Home
        "Progress" -> Icons.Outlined.BarChart
        "Profile" -> Icons.Outlined.Person
        else -> Icons.Outlined.Home
    }
    Icon(icon, contentDescription = item.label)
}

@Preview(showBackground = true, name = "Dashboard")
@Composable
fun DashboardScreenPreview() {
    AiCalCountTheme {
        DashboardScreen(
            onFabClick = {},
            content = { Box(Modifier.fillMaxSize()) }
        )
    }
}
