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
