package com.swappy.aicalcount.ui.scan

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.RecipeDetails
import com.swappy.aicalcount.network.Recipe
import com.swappy.aicalcount.util.AppError

@Composable
fun ScanScreen(
    bitmap: Bitmap?,
    recipe: Recipe?,
    loading: Boolean,
    isLabelMode: Boolean,
    appError: AppError?,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onRetry: () -> Unit,
    onCorrectIdentification: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCorrectDialog by remember { mutableStateOf(false) }
    var correctText by remember(recipe) {
        mutableStateOf(recipe?.recipes?.firstOrNull()?.title ?: "")
    }
    if (showCorrectDialog) {
        AlertDialog(
            onDismissRequest = { showCorrectDialog = false },
            title = { Text(stringResource(R.string.scan_correct_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = correctText,
                    onValueChange = { correctText = it },
                    label = { Text(stringResource(R.string.scan_correct_dialog_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = correctText.trim()
                        if (trimmed.isNotEmpty()) {
                            onCorrectIdentification(trimmed)
                            showCorrectDialog = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.scan_correct_update))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    // Do not use verticalScroll here: RecipeDetails contains LazyColumn, which needs bounded height.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
        when {
            bitmap != null -> {
                // Safe conversion: recycled/invalid bitmap would throw in asImageBitmap()
                val imageBitmap = remember(bitmap) {
                    runCatching { bitmap.asImageBitmap() }.getOrNull()
                }
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = if (isLabelMode) "Product label" else "Scanned food",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.error_image_load),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (loading) {
                    Text(
                        text = stringResource(R.string.scan_analyzing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (recipe != null) {
                    RecipeDetails(recipe = recipe)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            correctText = recipe.recipes.firstOrNull()?.title ?: ""
                            showCorrectDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.scan_correct_identification))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (recipe == null && !loading && appError != null) {
                    Text(
                        text = stringResource(appError.messageRes) + (appError.detail?.let { "\n$it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.retry))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.clear))
                }
            }
            else -> {
                Text(
                    text = if (isLabelMode) stringResource(R.string.label_upload_hint) else stringResource(R.string.home_scan_food),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_take_photo))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_upload_image))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Scan")
@Composable
fun ScanScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        ScanScreen(
            bitmap = null,
            recipe = null,
            loading = false,
            isLabelMode = false,
            appError = null,
            onTakePhoto = {},
            onPickImage = {},
            onRetry = {},
            onCorrectIdentification = {},
            onClear = {},
        )
    }
}
