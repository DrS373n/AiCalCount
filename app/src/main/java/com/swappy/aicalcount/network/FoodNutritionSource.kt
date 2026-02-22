package com.swappy.aicalcount.network

/**
 * Abstraction for text-based nutrition search. Indian local source and Spoonacular
 * both produce the same [Recipe] shape for extractAndAddMacros.
 */
interface FoodNutritionSource {
    /** Returns a [Recipe] for the query or null if no match. apiKey used only by remote sources. */
    suspend fun searchByText(query: String, apiKey: String): Recipe?
}
