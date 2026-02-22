package com.swappy.aicalcount

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swappy.aicalcount.data.indian.IndianLocalSource
import com.swappy.aicalcount.data.meals.MealDiaryRepository
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository
import com.swappy.aicalcount.network.AnalyzedRecipe
import com.swappy.aicalcount.network.Nutrition
import com.swappy.aicalcount.network.ProductByUpcResponse
import com.swappy.aicalcount.network.Recipe
import com.swappy.aicalcount.network.RetrofitClient
import com.swappy.aicalcount.network.SpoonacularSource
import com.swappy.aicalcount.network.toRecipe
import com.swappy.aicalcount.util.ApiUsageManager
import com.swappy.aicalcount.util.AppError
import com.swappy.aicalcount.util.GeminiImageToDish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainViewModel(
    application: Application,
    private val onboardingRepository: OnboardingRepository,
    private val progressRepository: ProgressRepository,
    private val mealDiaryRepository: MealDiaryRepository,
) : AndroidViewModel(application) {

    private val apiUsageManager = ApiUsageManager(application)
    private val indianLocalSource = IndianLocalSource(application)
    private val spoonacularSource = SpoonacularSource(RetrofitClient.spoonacularService)

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

    /** True when the current recipe came from Indian local data (for optional UI badge). */
    private val _lastRecipeFromIndian = MutableStateFlow(false)
    val lastRecipeFromIndian = _lastRecipeFromIndian.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessageKey.value = null
    }

    /** When true, try Gemini → dish name → Indian local data first for image scan; else use Spoonacular only. */
    private val preferIndianImage = true

    fun analyzeImage(bitmap: Bitmap, apiKey: String) {
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                if (preferIndianImage) {
                    val dishName = withContext(Dispatchers.IO) { GeminiImageToDish.getDishName(bitmap) }
                    val indianRecipe = dishName?.let { indianLocalSource.searchByText(it) }
                    if (indianRecipe != null) {
                        _lastRecipeFromIndian.value = true
                        _bitmap.value = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false) ?: bitmap
                        _recipe.value = indianRecipe
                        onboardingRepository.setHasLoggedMeal()
                        progressRepository.recordMealLogged()
                        extractAndAddMacros(indianRecipe)
                        return@launch
                    }
                }
                _lastRecipeFromIndian.value = false
                if (!apiUsageManager.canMakeApiCall()) {
                    _showApiLimitNotification.value = true
                    return@launch
                }
                val stream = ByteArrayOutputStream()
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                    _appError.value = AppError.Unknown(detail = "Could not encode image")
                    return@launch
                }
                val body = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", "image.jpg", body)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.spoonacularService.analyzeImage(filePart, apiKey)
                }
                val recipe = response.toRecipe()
                _bitmap.value = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false) ?: bitmap
                _recipe.value = recipe
                apiUsageManager.recordApiCall()
                onboardingRepository.setHasLoggedMeal()
                progressRepository.recordMealLogged()
                extractAndAddMacros(recipe)
            } catch (e: IOException) {
                _appError.value = AppError.Network(detail = e.message)
            } catch (e: retrofit2.HttpException) {
                _appError.value = when (e.code()) {
                    429 -> AppError.ApiLimit()
                    else -> AppError.Server(detail = e.message())
                }
            } catch (e: Throwable) {
                _appError.value = AppError.Unknown(detail = e.message ?: e.javaClass.simpleName)
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
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                val indianRecipe = indianLocalSource.searchByText(query)
                if (indianRecipe != null) {
                    _lastRecipeFromIndian.value = true
                    _recipe.value = indianRecipe
                    onboardingRepository.setHasLoggedMeal()
                    progressRepository.recordMealLogged()
                    extractAndAddMacros(indianRecipe)
                } else {
                    _lastRecipeFromIndian.value = false
                    if (!apiUsageManager.canMakeApiCall()) {
                        _showApiLimitNotification.value = true
                    } else {
                        val recipe = spoonacularSource.searchByText(query, apiKey)
                        _lastRecipeFromIndian.value = false
                        _recipe.value = recipe
                        if (recipe == null)
                            _appError.value = AppError.InvalidInput(detail = "No nutrition data found for that description.")
                        else {
                            apiUsageManager.recordApiCall()
                            onboardingRepository.setHasLoggedMeal()
                            progressRepository.recordMealLogged()
                            extractAndAddMacros(recipe)
                        }
                    }
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
        _lastRecipeFromIndian.value = false
    }

    fun setBitmapAndAnalyze(bitmap: Bitmap?, apiKey: String) {
        // Store a copy so we own the pixels; original may be recycled by activity/picker
        _bitmap.value = bitmap?.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false) ?: bitmap
        bitmap?.let { analyzeImage(it, apiKey) }
    }

    /** Re-send current bitmap to Spoonacular (e.g. after an error). */
    fun retryAnalyze(apiKey: String) {
        _bitmap.value?.let { analyzeImage(it, apiKey) }
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
        val first = recipe.recipes.firstOrNull() ?: return
        val nutrients = first.nutrition?.nutrients ?: return
        fun findAmount(nameVariants: List<String>): Float =
            nutrients.firstOrNull { n -> nameVariants.any { n.name.equals(it, ignoreCase = true) } }?.amount?.toFloat() ?: 0f
        val protein = findAmount(listOf("Protein"))
        val carbs = findAmount(listOf("Carbohydrates", "Carbs"))
        val fat = findAmount(listOf("Fat"))
        val fiber = findAmount(listOf("Fiber"))
        val sugar = findAmount(listOf("Sugar"))
        val sodium = findAmount(listOf("Sodium"))
        val calories = findAmount(listOf("Calories"))
        if (protein > 0f || carbs > 0f || fat > 0f) {
            progressRepository.addMealMacros(protein, carbs, fat, fiber, sugar, sodium)
            mealDiaryRepository.addMeal(
                date = java.time.LocalDate.now().toString(),
                title = first.title ?: "Meal",
                imagePath = null,
                calories = calories,
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
            )
            _snackbarMessageKey.value = "added_to_day"
        }
    }

    /** Subtract a recipe's macros from today's totals (e.g. before replacing with corrected identification). */
    private suspend fun subtractMacros(recipe: Recipe?) {
        recipe ?: return
        val nutrients = recipe.recipes.firstOrNull()?.nutrition?.nutrients ?: return
        fun findAmount(nameVariants: List<String>): Float =
            nutrients.firstOrNull { n -> nameVariants.any { n.name.equals(it, ignoreCase = true) } }?.amount?.toFloat() ?: 0f
        val protein = findAmount(listOf("Protein"))
        val carbs = findAmount(listOf("Carbohydrates", "Carbs"))
        val fat = findAmount(listOf("Fat"))
        val fiber = findAmount(listOf("Fiber"))
        val sugar = findAmount(listOf("Sugar"))
        val sodium = findAmount(listOf("Sodium"))
        if (protein > 0f || carbs > 0f || fat > 0f) {
            progressRepository.addMealMacros(-protein, -carbs, -fat, -fiber, -sugar, -sodium)
        }
    }

    /** Correct a wrong identification: subtract current recipe macros, then look up corrected text and add new macros. */
    fun correctIdentification(correctedText: String, apiKey: String) {
        val trimmed = correctedText.trim()
        if (trimmed.isBlank()) {
            _appError.value = AppError.InvalidInput(detail = "Please enter what this food is.")
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            try {
                subtractMacros(_recipe.value)
                val indianRecipe = indianLocalSource.searchByText(trimmed)
                if (indianRecipe != null) {
                    _lastRecipeFromIndian.value = true
                    _recipe.value = indianRecipe
                    extractAndAddMacros(indianRecipe)
                    _snackbarMessageKey.value = "added_to_day"
                } else {
                    _lastRecipeFromIndian.value = false
                    if (!apiUsageManager.canMakeApiCall()) {
                        _showApiLimitNotification.value = true
                    } else {
                        val recipe = spoonacularSource.searchByText(trimmed, apiKey)
                        _lastRecipeFromIndian.value = false
                        _recipe.value = recipe
                        if (recipe == null) {
                            _appError.value = AppError.InvalidInput(detail = "No nutrition data found for that. Try a different description.")
                        } else {
                            apiUsageManager.recordApiCall()
                            extractAndAddMacros(recipe)
                            _snackbarMessageKey.value = "added_to_day"
                        }
                    }
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

    /** Add one serving of a recipe to today's macros (recipe nutrition / servings). */
    suspend fun addRecipeServingToToday(recipe: Recipe?, servings: Int) {
        if (recipe == null || servings < 1) return
        val first = recipe.recipes.firstOrNull() ?: return
        val nutrients = first.nutrition?.nutrients ?: return
        fun findAmount(nameVariants: List<String>): Float =
            nutrients.firstOrNull { n -> nameVariants.any { n.name.equals(it, ignoreCase = true) } }?.amount?.toFloat() ?: 0f
        val div = servings.toFloat()
        val protein = findAmount(listOf("Protein")) / div
        val carbs = findAmount(listOf("Carbohydrates", "Carbs")) / div
        val fat = findAmount(listOf("Fat")) / div
        val fiber = findAmount(listOf("Fiber")) / div
        val sugar = findAmount(listOf("Sugar")) / div
        val sodium = findAmount(listOf("Sodium")) / div
        val calories = findAmount(listOf("Calories")) / div
        if (protein > 0f || carbs > 0f || fat > 0f) {
            progressRepository.addMealMacros(protein, carbs, fat, fiber, sugar, sodium)
            mealDiaryRepository.addMeal(
                date = java.time.LocalDate.now().toString(),
                title = first.title ?: "Meal",
                imagePath = null,
                calories = calories,
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
            )
            _snackbarMessageKey.value = "added_to_day"
        }
    }
}
