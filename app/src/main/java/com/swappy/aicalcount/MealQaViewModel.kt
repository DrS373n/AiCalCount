package com.swappy.aicalcount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swappy.aicalcount.network.RetrofitClient
import com.swappy.aicalcount.network.toRecipe
import com.swappy.aicalcount.util.ApiUsageManager
import com.swappy.aicalcount.util.AppError
import com.swappy.aicalcount.util.SPOONACULAR_API_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MealQaViewModel(application: Application) : AndroidViewModel(application) {

    private val apiUsageManager = ApiUsageManager(application)

    private val _answer = MutableStateFlow<String?>(null)
    val answer = _answer.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _appError = MutableStateFlow<AppError?>(null)
    val appError = _appError.asStateFlow()

    fun sendQuestion(question: String) {
        if (question.isBlank()) return
        if (!apiUsageManager.canMakeApiCall()) {
            _appError.value = AppError.ApiLimit()
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            _answer.value = null
            try {
                val response = RetrofitClient.spoonacularService.searchRecipesWithNutrition(
                    apiKey = SPOONACULAR_API_KEY,
                    query = question,
                    number = 1,
                    addRecipeNutrition = true
                )
                val recipe = response.toRecipe()
                apiUsageManager.recordApiCall()
                _answer.value = formatAnswer(question, recipe)
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

    private fun formatAnswer(question: String, recipe: com.swappy.aicalcount.network.Recipe?): String {
        if (recipe == null) return "I couldn't find nutrition data for that. Try rephrasing (e.g. \"calories in grilled chicken\")."
        val r = recipe.recipes.firstOrNull() ?: return "No data."
        val nutrients = r.nutrition.nutrients
        val calories = nutrients.find { it.name.equals("Calories", ignoreCase = true) }
        val protein = nutrients.find { it.name.equals("Protein", ignoreCase = true) }
        val fat = nutrients.find { it.name.equals("Fat", ignoreCase = true) }
        val carbs = nutrients.find { it.name.equals("Carbohydrates", ignoreCase = true) }
        return buildString {
            append("For \"${r.title}\":\n\n")
            calories?.let { append("• Calories: ${it.amount.toInt()} ${it.unit}\n") }
            protein?.let { append("• Protein: ${it.amount} ${it.unit}\n") }
            fat?.let { append("• Fat: ${it.amount} ${it.unit}\n") }
            carbs?.let { append("• Carbs: ${it.amount} ${it.unit}\n") }
            val others = nutrients - setOfNotNull(calories, protein, fat, carbs)
            if (others.isNotEmpty()) {
                append("\nOther: ")
                append(others.take(5).joinToString(", ") { "${it.name} ${it.amount} ${it.unit}" })
            }
        }
    }

    fun dismissError() {
        _appError.value = null
    }
}
