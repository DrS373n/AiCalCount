package com.swappy.aicalcount.network

import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularService {

    /** Get grocery product by UPC barcode. Returns product with nutrition when available. */
    @GET("food/products/upc/{upc}")
    suspend fun getProductByUpc(
        @Path("upc") upc: String,
        @Query("apiKey") apiKey: String
    ): ProductByUpcResponse?

    /** Analyze food image. API expects multipart/form-data with part name "file". */
    @Multipart
    @POST("food/images/analyze")
    suspend fun analyzeImage(
        @Part file: MultipartBody.Part,
        @Query("apiKey") apiKey: String
    ): ImageAnalyzeResponse

    /** Search recipes by natural language query; returns nutrition when addRecipeNutrition=true. */
    @GET("recipes/complexSearch")
    suspend fun searchRecipesWithNutrition(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int = 1,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true
    ): ComplexSearchResponse
}