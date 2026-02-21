package com.swappy.aicalcount.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.navigation.NavRoutes

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeActionCard(
            title = stringResource(R.string.home_scan_food),
            onClick = { onNavigate(NavRoutes.Scan) },
        )
        HomeActionCard(
            title = stringResource(R.string.home_describe_meal),
            onClick = { onNavigate(NavRoutes.Describe) },
        )
        HomeActionCard(
            title = stringResource(R.string.home_upload_label),
            onClick = { onNavigate(NavRoutes.LabelUpload) },
        )
        HomeActionCard(
            title = stringResource(R.string.home_ask_meals),
            onClick = { onNavigate(NavRoutes.MealQa) },
        )
        HomeActionCard(
            title = stringResource(R.string.home_my_diet_plan),
            onClick = { onNavigate(NavRoutes.DietPlan) },
        )
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview(showBackground = true, name = "Home")
@Composable
fun HomeScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        HomeScreen(onNavigate = {})
    }
}
