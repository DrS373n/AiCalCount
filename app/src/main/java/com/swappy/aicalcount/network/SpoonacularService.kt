package com.swappy.aicalcount.network

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SpoonacularService {

    @POST("food/images/analyze")
    suspend fun analyzeImage(
        @Body image: RequestBody,
        @Query("apiKey") apiKey: String
    ): Recipe

    /** Search recipes by natural language query; returns nutrition when addRecipeNutrition=true. */
    @GET("recipes/complexSearch")
    suspend fun searchRecipesWithNutrition(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int = 1,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true
    ): ComplexSearchResponse
}