package com.swappy.aicalcount.ui.qa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.swappy.aicalcount.BuildConfig
import com.swappy.aicalcount.util.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenerativeMealQaViewModel : ViewModel() {

    private val _answer = MutableStateFlow<String?>(null)
    val answer = _answer.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _appError = MutableStateFlow<AppError?>(null)
    val appError = _appError.asStateFlow()

    // Free-tier model: Gemini 1.5 Flash (Meal Q&A only; photo/text nutrition uses Spoonacular)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("You are a helpful and friendly nutritionist and fitness expert. Your goal is to help me understand my meals and stay healthy. Please be concise and encouraging.") },
            content(role = "model") { text("I am ready to help you with your meals. What's on your mind?") }
        )
    )

    fun sendQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            _appError.value = null
            _answer.value = null
            try {
                val response = chat.sendMessage(question)
                _answer.value = response.text
            } catch (e: Exception) {
                _appError.value = AppError.Unknown(detail = e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun dismissError() {
        _appError.value = null
    }
}
