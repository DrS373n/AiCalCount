package com.swappy.aicalcount.ui.describe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R
import com.swappy.aicalcount.RecipeDetails
import com.swappy.aicalcount.network.Recipe

@Composable
fun DescribeScreen(
    loading: Boolean,
    recipe: Recipe?,
    lastRecipeFromIndian: Boolean = false,
    onDescribeSubmit: (String) -> Unit,
    hasVoicePermission: Boolean = true,
    onRequestVoicePermission: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
    ) {
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.home_describe_meal)) },
                placeholder = { Text(stringResource(R.string.describe_placeholder_example)) },
                modifier = Modifier.weight(1f),
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = false,
            )
            VoiceInputButton(
                enabled = !loading,
                hasVoicePermission = hasVoicePermission,
                onResult = { text = it },
                onRequestVoicePermission = onRequestVoicePermission,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onDescribeSubmit(text.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && text.isNotBlank(),
        ) {
            Text(stringResource(R.string.describe_analyze))
        }
        if (recipe != null) {
            Spacer(modifier = Modifier.height(24.dp))
            if (lastRecipeFromIndian) {
                Text(
                    text = stringResource(R.string.indian_source_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )
            }
            RecipeDetails(recipe = recipe)
        }
    }
}

@Preview(showBackground = true, name = "Describe meal")
@Composable
fun DescribeScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        DescribeScreen(
            loading = false,
            recipe = null,
            onDescribeSubmit = {},
            hasVoicePermission = true,
        )
    }
}
