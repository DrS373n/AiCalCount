package com.swappy.aicalcount.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.swappy.aicalcount.BuildConfig
import com.swappy.aicalcount.data.diet.DietGoalHelper
import com.swappy.aicalcount.data.diet.DietPreferencesRepository
import com.swappy.aicalcount.data.progress.ProgressRepository
import com.swappy.aicalcount.data.profile.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatMessage(val isUser: Boolean, val text: String)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepo = UserProfileRepository(application)
    private val dietPrefsRepo = DietPreferencesRepository(application)
    private val progressRepo = ProgressRepository(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chat: com.google.ai.client.generativeai.Chat? = null

    init {
        viewModelScope.launch {
            val profile = profileRepo.profile.first()
            val preferences = dietPrefsRepo.preferences.first()
            val goals = DietGoalHelper.computeGoals(profile, preferences)
            val protein = progressRepo.todayProtein.first()
            val carbs = progressRepo.todayCarbs.first()
            val fat = progressRepo.todayFat.first()
            val contextBlock = buildContextBlock(profile, preferences, goals, protein, carbs, fat)
            chat = buildChat(contextBlock)
            if (_messages.value.isEmpty()) {
                _messages.value = listOf(
                    ChatMessage(isUser = false, "Hi! I'm here to help with nutrition and healthy habits. What would you like to know?")
                )
            }
        }
    }

    private fun buildContextBlock(
        profile: com.swappy.aicalcount.data.profile.UserProfile,
        preferences: com.swappy.aicalcount.data.diet.DietPreferences,
        goals: com.swappy.aicalcount.data.diet.DietGoalHelper.MacroGoals,
        todayProtein: Float,
        todayCarbs: Float,
        todayFat: Float
    ): List<String> {
        val lines = mutableListOf<String>()
        if (profile.displayName.isNotBlank()) {
            lines.add("User's name: ${profile.displayName}.")
        }
        lines.add("Goal: ${preferences.goal.name}; daily targets: ${goals.calories.toInt()} cal, ${goals.proteinG.toInt()}g protein, ${goals.carbsG.toInt()}g carbs, ${goals.fatG.toInt()}g fat.")
        lines.add("Today so far: ${todayProtein.toInt()}g protein, ${todayCarbs.toInt()}g carbs, ${todayFat.toInt()}g fat.")
        return lines
    }

    private fun buildChat(contextLines: List<String>): com.google.ai.client.generativeai.Chat {
        val systemText = buildString {
            append("You are a friendly nutrition and health coach. Answer briefly and supportively. ")
            append("This is for educational support only, not a substitute for medical or dietitian advice. ")
            if (contextLines.isNotEmpty()) {
                append("Context: ")
                append(contextLines.joinToString(" "))
            }
        }
        return model.startChat(
            history = listOf(
                content(role = "user") { text(systemText) },
                content(role = "model") { text("Hi! I'm here to help with nutrition and healthy habits. What would you like to know?") }
            )
        )
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage(isUser = true, trimmed)
            _loading.value = true
            val currentChat = chat
            try {
                if (currentChat != null) {
                    val response = currentChat.sendMessage(trimmed)
                    val reply = response.text?.trim() ?: "I couldn't generate a response."
                    _messages.value = _messages.value + ChatMessage(isUser = false, reply)
                } else {
                    _messages.value = _messages.value + ChatMessage(isUser = false, "Please wait a moment and try again.")
                }
            } catch (_: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    isUser = false,
                    "Something went wrong. Please try again."
                )
            } finally {
                _loading.value = false
            }
        }
    }

    /** One-shot tip based on today's macros (Coach feature). Appends result as a bot message. */
    fun loadTodayTip() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val profile = profileRepo.profile.first()
                val preferences = dietPrefsRepo.preferences.first()
                val goals = DietGoalHelper.computeGoals(profile, preferences)
                val protein = progressRepo.todayProtein.first()
                val carbs = progressRepo.todayCarbs.first()
                val fat = progressRepo.todayFat.first()
                val prompt = """
                    You are a friendly nutrition coach. In 1-2 short sentences only, give one practical tip.
                    Today's macros so far: ${protein.toInt()}g protein, ${carbs.toInt()}g carbs, ${fat.toInt()}g fat.
                    Typical goals: ${goals.proteinG.toInt()}g protein, ${goals.carbsG.toInt()}g carbs, ${goals.fatG.toInt()}g fat.
                    No greeting, just the tip.
                """.trimIndent()
                val response = model.generateContent(prompt)
                val tip = response.text?.trim() ?: "Track your meals to get a personalized tip."
                _messages.value = _messages.value + ChatMessage(isUser = false, tip)
            } catch (_: Exception) {
                _messages.value = _messages.value + ChatMessage(isUser = false, "Track your meals to get a personalized tip.")
            } finally {
                _loading.value = false
            }
        }
    }

    /** One-shot weekly encouragement (Coach feature). Appends result as a bot message. */
    fun loadWeeklySummary() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val streak = progressRepo.streakCount.first()
                val loggedDates = progressRepo.loggedDates.first()
                val thisWeek = loggedDates.count { it >= java.time.LocalDate.now().minusDays(7).toString() }
                val prompt = """
                    You are a friendly nutrition coach. In one short sentence only, give encouragement.
                    This week they logged meals on $thisWeek days. Current streak: $streak days.
                    No greeting, just one encouraging sentence.
                """.trimIndent()
                val response = model.generateContent(prompt)
                val summary = response.text?.trim() ?: "Keep logging to see your weekly summary."
                _messages.value = _messages.value + ChatMessage(isUser = false, summary)
            } catch (_: Exception) {
                _messages.value = _messages.value + ChatMessage(isUser = false, "Keep logging to see your weekly summary.")
            } finally {
                _loading.value = false
            }
        }
    }
}
