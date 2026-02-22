package com.swappy.aicalcount.network

data class Recipe(
    val recipes: List<AnalyzedRecipe>
)

data class AnalyzedRecipe(
    val id: Int,
    val title: String,
    val image: String,
    val imageUrls: List<String>,
    val nutrition: Nutrition
)

data class Nutrition(
    val nutrients: List<Nutrient>
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)

// Response from recipes/complexSearch (with addRecipeNutrition=true)
data class ComplexSearchResponse(
    val results: List<SearchRecipeResult>?,
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0
)

data class SearchRecipeResult(
    val id: Int,
    val title: String,
    val image: String? = "",
    val imageType: String? = null,
    val nutrition: Nutrition? = null
)

/** Response from Spoonacular GET food/products/upc/{upc} */
data class ProductByUpcResponse(
    val id: Int? = null,
    val title: String? = null,
    val upc: String? = null,
    val nutrition: Nutrition? = null
)

/** Response from Spoonacular POST food/images/analyze (structure differs from Recipe). */
data class ImageAnalyzeResponse(
    val nutrition: ImageAnalyzeNutrition? = null,
    val category: ImageAnalyzeCategory? = null,
    val recipes: List<ImageAnalyzeRecipe>? = null
)

data class ImageAnalyzeNutrition(
    val calories: ImageAnalyzeNutrientValue? = null,
    val fat: ImageAnalyzeNutrientValue? = null,
    val protein: ImageAnalyzeNutrientValue? = null,
    val carbs: ImageAnalyzeNutrientValue? = null
)

data class ImageAnalyzeNutrientValue(
    val value: Double? = null,
    val unit: String? = null
)

data class ImageAnalyzeCategory(
    val name: String? = null,
    val probability: Double? = null
)

data class ImageAnalyzeRecipe(
    val id: Int? = null,
    val title: String? = null,
    val imageType: String? = null,
    val sourceUrl: String? = null
)

/** Maps ImageAnalyzeResponse to Recipe so existing UI and extractAndAddMacros work. */
fun ImageAnalyzeResponse.toRecipe(): Recipe {
    fun safeDouble(v: Double?): Double = when {
        v == null -> 0.0
        v.isNaN() || v.isInfinite() -> 0.0
        else -> v
    }
    val nut = nutrition
    val nutrients = mutableListOf<Nutrient>()
    nut?.calories?.value?.let { v -> nutrients.add(Nutrient("Calories", safeDouble(v), nut.calories?.unit ?: "calories")) }
    nut?.fat?.value?.let { v -> nutrients.add(Nutrient("Fat", safeDouble(v), nut.fat?.unit ?: "g")) }
    nut?.protein?.value?.let { v -> nutrients.add(Nutrient("Protein", safeDouble(v), nut.protein?.unit ?: "g")) }
    nut?.carbs?.value?.let { v -> nutrients.add(Nutrient("Carbohydrates", safeDouble(v), nut.carbs?.unit ?: "g")) }
    val title = (category?.name?.takeIf { it.isNotEmpty() }?.replaceFirstChar { it.uppercase() }
        ?: recipes?.firstOrNull()?.title?.takeIf { !it.isNullOrBlank() }
        ?: "Analyzed dish")
    val single = AnalyzedRecipe(
        id = recipes?.firstOrNull()?.id ?: 0,
        title = title,
        image = "",
        imageUrls = emptyList(),
        nutrition = Nutrition(nutrients)
    )
    return Recipe(recipes = listOf(single))
}
