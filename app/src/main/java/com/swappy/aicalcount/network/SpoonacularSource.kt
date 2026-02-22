package com.swappy.aicalcount.network

/**
 * Spoonacular API adapter implementing [FoodNutritionSource].
 * Used as fallback when Indian local source has no match; consumes API quota.
 */
class SpoonacularSource(private val service: SpoonacularService) : FoodNutritionSource {

    override suspend fun searchByText(query: String, apiKey: String): Recipe? {
        val response = service.searchRecipesWithNutrition(
            apiKey = apiKey,
            query = query,
            number = 1,
            addRecipeNutrition = true
        )
        return response.toRecipe()
    }
}
