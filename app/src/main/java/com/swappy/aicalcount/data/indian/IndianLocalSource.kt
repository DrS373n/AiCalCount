package com.swappy.aicalcount.data.indian

import android.content.Context
import com.swappy.aicalcount.network.AnalyzedRecipe
import com.swappy.aicalcount.network.FoodNutritionSource
import com.swappy.aicalcount.network.Nutrient
import com.swappy.aicalcount.network.Nutrition
import com.swappy.aicalcount.network.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * Free Indian food nutrition source: loads bundled IFCT-style JSON from assets,
 * searches by name/alias (normalized), returns [Recipe] compatible with extractAndAddMacros.
 * No network; no API key; does not consume Spoonacular quota.
 */
class IndianLocalSource(private val context: Context) : FoodNutritionSource {

    override suspend fun searchByText(query: String, apiKey: String): Recipe? = doSearchByText(query)

    private val entries: List<IndianFoodEntry> by lazy { loadEntries() }

    private fun loadEntries(): List<IndianFoodEntry> {
        return try {
            context.assets.open(ASSET_FILE).use { input ->
                val type = object : TypeToken<List<IndianFoodEntry>>() {}.type
                Gson().fromJson<List<IndianFoodEntry>>(InputStreamReader(input), type) ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Search by text (e.g. "idli", "palak paneer"). Normalizes query (trim, lowercase),
     * matches name or any alias (exact or contains). Returns first match as [Recipe] or null.
     */
    fun searchByText(query: String): Recipe? = doSearchByText(query)

    private fun doSearchByText(query: String): Recipe? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        val entry = entries.firstOrNull { entry ->
            entry.name.lowercase() == q ||
                entry.name.lowercase().contains(q) ||
                entry.aliases.any { it.lowercase() == q || it.lowercase().contains(q) }
        } ?: entries.firstOrNull { entry ->
            q.contains(entry.name.lowercase()) || entry.aliases.any { q.contains(it.lowercase()) }
        } ?: return null
        return entry.toRecipe()
    }

    private fun IndianFoodEntry.toRecipe(): Recipe {
        val nutrients = listOf(
            Nutrient("Calories", calories, "kcal"),
            Nutrient("Protein", protein, "g"),
            Nutrient("Carbohydrates", carbs, "g"),
            Nutrient("Fat", fat, "g"),
            Nutrient("Fiber", fiber, "g"),
            Nutrient("Sugar", sugar, "g"),
            Nutrient("Sodium", sodium, "mg"),
        )
        val analyzed = AnalyzedRecipe(
            id = id,
            title = name.replaceFirstChar { it.uppercase() },
            image = "",
            imageUrls = emptyList(),
            nutrition = Nutrition(nutrients),
        )
        return Recipe(recipes = listOf(analyzed))
    }

    companion object {
        private const val ASSET_FILE = "indian_food_ifct.json"
    }
}
