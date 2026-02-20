package com.swappy.aicalcount.network

/**
 * Maps a search result (e.g. from text query) to the same Recipe shape used by image analysis.
 */
fun ComplexSearchResponse.toRecipe(): Recipe? {
    val first = results?.firstOrNull() ?: return null
    val nutrition = first.nutrition ?: return null
    return Recipe(
        recipes = listOf(
            AnalyzedRecipe(
                id = first.id,
                title = first.title,
                image = first.image ?: "",
                imageUrls = listOfNotNull(first.image),
                nutrition = nutrition
            )
        )
    )
}
