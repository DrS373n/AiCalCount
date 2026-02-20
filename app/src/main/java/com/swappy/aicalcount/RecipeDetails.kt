package com.swappy.aicalcount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.network.AnalyzedRecipe
import com.swappy.aicalcount.network.Nutrient
import com.swappy.aicalcount.network.Nutrition
import com.swappy.aicalcount.network.Recipe
import com.swappy.aicalcount.ui.theme.AiCalCountTheme

@Composable
fun RecipeDetails(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.recipes.firstOrNull()?.title ?: "No recipe found",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(recipe.recipes.firstOrNull()?.nutrition?.nutrients ?: emptyList()) { nutrient ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = nutrient.name, modifier = Modifier.weight(1f))
                        Text(text = "${nutrient.amount} ${nutrient.unit}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Recipe details")
@Composable
fun RecipeDetailsPreview() {
    val sampleRecipe = Recipe(
        recipes = listOf(
            AnalyzedRecipe(
                id = 0,
                title = "Preview Recipe",
                image = "",
                imageUrls = emptyList(),
                nutrition = Nutrition(
                    nutrients = listOf(
                        Nutrient("Calories", 300.0, "kcal"),
                        Nutrient("Protein", 10.0, "g"),
                        Nutrient("Carbs", 35.0, "g"),
                        Nutrient("Fat", 12.0, "g")
                    )
                )
            )
        )
    )
    AiCalCountTheme {
        RecipeDetails(recipe = sampleRecipe)
    }
}
