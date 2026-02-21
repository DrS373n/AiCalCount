package com.swappy.aicalcount

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository
import com.swappy.aicalcount.network.AnalyzedRecipe
import com.swappy.aicalcount.network.Nutrition
import com.swappy.aicalcount.network.ProductByUpcResponse
import com.swappy.aicalcount.network.Recipe
import com.swappy.aicalcount.network.RetrofitClient
import com.swappy.aicalcount.network.toRecipe
import com.swappy.aicalcount.util.ApiUsageManager
import com.swappy.aicalcount.util.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainViewModel(
    application: Application,
    private val onboardingRepository: OnboardingRepository,
    private val progressRepository: ProgressRepository
) : AndroidViewModel(application) {

    private val apiUsageManager = ApiUsageManager(application)

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe = _recipe.asStateFlow()

    private val _showApiLimitNotification = MutableStateFlow(false)
    val showApiLimitNotification = _showApiLimitNotification.asStateFlow()

    private val _appError = MutableStateFlow<AppError?>(null)
    val appError = _appError.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /** One-shot key for Snackbar: "added_to_day" when macros were added to today. */
    private val _snackbarMessageKey = MutableStateFlow<String?>(null)
    val snackbarMessageKey = _snackbarMessageKey.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessageKey.value = null
    }

    fun analyzeImage(bitmap: Bitmap, apiKey: String) {
        if (!apiUsageManager.canMakeApiCall()) {
            _showApiLimitNotification.value = true
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val requestBody = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                val response = RetrofitClient.spoonacularService.analyzeImage(requestBody, apiKey)
                _bitmap.value = bitmap
                _recipe.value = response
                apiUsageManager.recordApiCall()
                onboardingRepository.setHasLoggedMeal()
                progressRepository.recordMealLogged()
                extractAndAddMacros(response)
            } catch (e: IOException) {
                _appError.value = AppError.Network(detail = e.message)
            } catch (e: retrofit2.HttpException) {
                _appError.value = when (e.code()) {
                    429 -> AppError.ApiLimit()
                    else -> AppError.Server(detail = e.message())
                }
            } catch (e: Exception) {
                _appError.value = AppError.Unknown(detail = e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun analyzeFromText(query: String, apiKey: String) {
        if (query.isBlank()) {
            _appError.value = AppError.InvalidInput(detail = "Please describe your meal.")
            return
        }
        if (!apiUsageManager.canMakeApiCall()) {
            _showApiLimitNotification.value = true
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                val response = RetrofitClient.spoonacularService.searchRecipesWithNutrition(
                    apiKey = apiKey,
                    query = query,
                    number = 1,
                    addRecipeNutrition = true
                )
                val recipe = response.toRecipe()
                _recipe.value = recipe
                if (recipe == null)
                    _appError.value = AppError.InvalidInput(detail = "No nutrition data found for that description.")
                else {
                    apiUsageManager.recordApiCall()
                    onboardingRepository.setHasLoggedMeal()
                    progressRepository.recordMealLogged()
                    extractAndAddMacros(recipe)
                }
            } catch (e: IOException) {
                _appError.value = AppError.Network(detail = e.message)
            } catch (e: retrofit2.HttpException) {
                _appError.value = when (e.code()) {
                    429 -> AppError.ApiLimit()
                    else -> AppError.Server(detail = e.message())
                }
            } catch (e: Exception) {
                _appError.value = AppError.Unknown(detail = e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearRecipe() {
        _bitmap.value = null
        _recipe.value = null
        _appError.value = null
    }

    fun setBitmapAndAnalyze(bitmap: Bitmap?, apiKey: String) {
        _bitmap.value = bitmap
        bitmap?.let { analyzeImage(it, apiKey) }
    }

    fun lookupBarcode(upc: String, apiKey: String) {
        val trimmed = upc.trim()
        if (trimmed.isBlank()) {
            _appError.value = AppError.InvalidInput(detail = "Enter a barcode (UPC).")
            return
        }
        if (!apiUsageManager.canMakeApiCall()) {
            _showApiLimitNotification.value = true
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                val product = RetrofitClient.spoonacularService.getProductByUpc(trimmed, apiKey)
                if (product == null || product.title == null) {
                    _appError.value = AppError.InvalidInput(detail = "Product not in database. Try photo or describe.")
                    _recipe.value = null
                } else {
                    val nutrition = product.nutrition ?: Nutrition(emptyList())
                    val recipe = Recipe(
                        recipes = listOf(
                            AnalyzedRecipe(
                                id = product.id ?: 0,
                                title = product.title,
                                image = "",
                                imageUrls = emptyList(),
                                nutrition = nutrition
                            )
                        )
                    )
                    _recipe.value = recipe
                    apiUsageManager.recordApiCall()
                    onboardingRepository.setHasLoggedMeal()
                    progressRepository.recordMealLogged()
                    extractAndAddMacros(recipe)
                }
            } catch (e: IOException) {
                _appError.value = AppError.Network(detail = e.message)
                _recipe.value = null
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _appError.value = AppError.InvalidInput(detail = "Product not in database. Try photo or describe.")
                } else {
                    _appError.value = when (e.code()) {
                        429 -> AppError.ApiLimit()
                        else -> AppError.Server(detail = e.message())
                    }
                }
                _recipe.value = null
            } catch (e: Exception) {
                _appError.value = AppError.Unknown(detail = e.message)
                _recipe.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun dismissApiLimitNotification() {
        _showApiLimitNotification.value = false
    }

    fun dismissError() {
        _appError.value = null
    }

    private suspend fun extractAndAddMacros(recipe: Recipe?) {
        recipe ?: return
        val nutrients = recipe.recipes.firstOrNull()?.nutrition?.nutrients ?: return
        fun findAmount(nameVariants: List<String>): Float =
            nutrients.firstOrNull { n -> nameVariants.any { n.name.equals(it, ignoreCase = true) } }?.amount?.toFloat() ?: 0f
        val protein = findAmount(listOf("Protein"))
        val carbs = findAmount(listOf("Carbohydrates", "Carbs"))
        val fat = findAmount(listOf("Fat"))
        if (protein > 0f || carbs > 0f || fat > 0f) {
            progressRepository.addMealMacros(protein, carbs, fat)
            _snackbarMessageKey.value = "added_to_day"
        }
    }

    /** Add one serving of a recipe to today's macros (recipe nutrition / servings). */
    suspend fun addRecipeServingToToday(recipe: Recipe?, servings: Int) {
        if (recipe == null || servings < 1) return
        val nutrients = recipe.recipes.firstOrNull()?.nutrition?.nutrients ?: return
        fun findAmount(nameVariants: List<String>): Float =
            nutrients.firstOrNull { n -> nameVariants.any { n.name.equals(it, ignoreCase = true) } }?.amount?.toFloat() ?: 0f
        val protein = findAmount(listOf("Protein")) / servings
        val carbs = findAmount(listOf("Carbohydrates", "Carbs")) / servings
        val fat = findAmount(listOf("Fat")) / servings
        if (protein > 0f || carbs > 0f || fat > 0f) {
            progressRepository.addMealMacros(protein, carbs, fat)
            _snackbarMessageKey.value = "added_to_day"
        }
    }
}
