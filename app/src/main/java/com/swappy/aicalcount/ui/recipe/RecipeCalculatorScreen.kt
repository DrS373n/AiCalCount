package com.swappy.aicalcount.ui.recipe

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
fun RecipeCalculatorScreen(
    loading: Boolean,
    recipe: Recipe?,
    onCalculate: (query: String) -> Unit,
    onAddServing: (Recipe, Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("1") }
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.recipe_calc_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.recipe_calc_name_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text(stringResource(R.string.recipe_calc_ingredients_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = servings,
            onValueChange = { servings = it.filter { c -> c.isDigit() }.ifEmpty { "1" }.take(2) },
            label = { Text(stringResource(R.string.recipe_calc_servings_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val query = buildList {
                    if (name.isNotBlank()) add(name)
                    if (ingredients.isNotBlank()) add(ingredients)
                }.joinToString(": ")
                if (query.isNotBlank()) onCalculate(query)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && (name.isNotBlank() || ingredients.isNotBlank()),
        ) {
            Text(stringResource(R.string.recipe_calc_calculate))
        }
        if (recipe != null) {
            Spacer(modifier = Modifier.height(24.dp))
            val servingsNum = servings.toIntOrNull() ?: 1
            if (servingsNum > 1) {
                Text(
                    text = stringResource(R.string.recipe_calc_per_serving) + " ($servingsNum)",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            RecipeDetails(recipe = recipe)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onAddServing(recipe, servingsNum) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recipe_calc_add_serving))
            }
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}

@Preview(showBackground = true, name = "Recipe calculator")
@Composable
fun RecipeCalculatorScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        RecipeCalculatorScreen(
            loading = false,
            recipe = null,
            onCalculate = { },
            onAddServing = { _, _ -> },
            onClear = {},
        )
    }
}
