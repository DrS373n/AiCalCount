package com.swappy.aicalcount.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.spoonacular.com/"

    /** Gson with lenient adapter for image-analyze "value" (number or string) to avoid parse crashes. */
    private val gson = createImageAnalyzeGson()

    val spoonacularService: SpoonacularService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SpoonacularService::class.java)
    }
}
