package com.swappy.aicalcount.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.swappy.aicalcount.R
import com.swappy.aicalcount.ui.theme.AiCalCountTheme

@Composable
fun ProfileTabScreen(
    profilePhotoPath: String?,
    displayName: String,
    onChangePhotoClick: () -> Unit,
    onNavigateToDietPlan: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToDescribe: () -> Unit,
    onNavigateToLabelUpload: () -> Unit,
    onNavigateToMealQa: () -> Unit,
    onNavigateToRecipeCalculator: () -> Unit = {},
    onNavigateToCoach: () -> Unit = {},
    apiRemainingCalls: Int? = null,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onChangePhotoClick),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (!profilePhotoPath.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePhotoPath,
                        contentDescription = stringResource(R.string.profile_photo_desc),
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.profile_photo_edit),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = displayName.ifBlank { stringResource(R.string.profile_title) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (apiRemainingCalls != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.api_remaining, apiRemainingCalls),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuItem(stringResource(R.string.home_my_diet_plan), onNavigateToDietPlan)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.recipe_calc_menu), onNavigateToRecipeCalculator)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.coach_menu), onNavigateToCoach)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem("Compare progress photos", onNavigateToCompare)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.home_scan_food), onNavigateToScan)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.home_describe_meal), onNavigateToDescribe)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.home_upload_label), onNavigateToLabelUpload)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(stringResource(R.string.home_ask_meals), onNavigateToMealQa)
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            title,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true, name = "Profile tab")
@Composable
fun ProfileTabScreenPreview() {
    AiCalCountTheme {
        ProfileTabScreen(
            profilePhotoPath = null,
            displayName = "Preview User",
            onChangePhotoClick = {},
            onNavigateToDietPlan = {},
            onNavigateToCompare = {},
            onNavigateToScan = {},
            onNavigateToDescribe = {},
            onNavigateToLabelUpload = {},
            onNavigateToMealQa = {},
            onNavigateToRecipeCalculator = {},
            onNavigateToCoach = {},
        )
    }
}
