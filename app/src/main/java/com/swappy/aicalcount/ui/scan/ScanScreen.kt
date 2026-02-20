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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.RecipeDetails
import com.swappy.aicalcount.network.Recipe

@Composable
fun ScanScreen(
    bitmap: Bitmap?,
    recipe: Recipe?,
    loading: Boolean,
    isLabelMode: Boolean,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = if (isLabelMode) "Product label" else "Scanned food",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (recipe != null) {
                    RecipeDetails(recipe = recipe)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(onClick = onClear) {
                    Text(stringResource(R.string.clear))
                }
            }
            else -> {
                Text(
                    text = if (isLabelMode) stringResource(R.string.label_upload_hint) else stringResource(R.string.home_scan_food),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_take_photo))
                }
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.OutlinedButton(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
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
            onTakePhoto = {},
            onPickImage = {},
            onClear = {},
        )
    }
}
