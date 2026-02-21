package com.swappy.aicalcount.ui.barcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.RecipeDetails
import com.swappy.aicalcount.network.Recipe

@Composable
fun BarcodeScanScreen(
    loading: Boolean,
    recipe: Recipe?,
    onLookup: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var upc by rememberSaveable { mutableStateOf("") }
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.barcode_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = upc,
            onValueChange = { upc = it.filter { c -> c.isDigit() } },
            label = { Text(stringResource(R.string.barcode_enter_manually)) },
            placeholder = { Text(stringResource(R.string.barcode_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onLookup(upc) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && upc.isNotBlank(),
        ) {
            Text(stringResource(R.string.barcode_lookup))
        }
        if (recipe != null) {
            Spacer(modifier = Modifier.height(24.dp))
            RecipeDetails(recipe = recipe)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClear) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}

@Preview(showBackground = true, name = "Barcode scan")
@Composable
fun BarcodeScanScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        BarcodeScanScreen(
            loading = false,
            recipe = null,
            onLookup = {},
            onClear = {},
        )
    }
}
