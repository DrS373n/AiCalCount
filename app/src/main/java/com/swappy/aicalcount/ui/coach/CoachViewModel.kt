package com.swappy.aicalcount.ui.coach

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.swappy.aicalcount.BuildConfig
import com.swappy.aicalcount.data.diet.DietGoalHelper
import com.swappy.aicalcount.data.diet.DietPreferencesRepository
import com.swappy.aicalcount.data.progress.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CoachViewModel(application: Application) : AndroidViewModel(application) {

    private val progressRepo = ProgressRepository(application)
    private val dietPrefsRepo = DietPreferencesRepository(application)

    private val _todayTip = MutableStateFlow<String?>(null)
    val todayTip = _todayTip.asStateFlow()

    private val _weeklySummary = MutableStateFlow<String?>(null)
    val weeklySummary = _weeklySummary.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun loadTodayTip() {
        viewModelScope.launch {
            _loading.value = true
            _todayTip.value = null
            try {
                val protein = progressRepo.todayProtein.first()
                val carbs = progressRepo.todayCarbs.first()
                val fat = progressRepo.todayFat.first()
                val prefs = dietPrefsRepo.preferences.first()
                val goals = DietGoalHelper.computeGoals(prefs)
                val prompt = """
                    You are a friendly nutrition coach. In 1-2 short sentences only, give one practical tip.
                    Today's macros so far: ${protein.toInt()}g protein, ${carbs.toInt()}g carbs, ${fat.toInt()}g fat.
                    Typical goals: ${goals.proteinG.toInt()}g protein, ${goals.carbsG.toInt()}g carbs, ${goals.fatG.toInt()}g fat.
                    No greeting, just the tip.
                """.trimIndent()
                val response = model.generateContent(prompt)
                _todayTip.value = response.text?.trim()
            } catch (_: Exception) {
                _todayTip.value = "Track your meals to get a personalized tip."
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadWeeklySummary() {
        viewModelScope.launch {
            _loading.value = true
            _weeklySummary.value = null
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
                _weeklySummary.value = response.text?.trim()
            } catch (_: Exception) {
                _weeklySummary.value = "Keep logging to see your weekly summary."
            } finally {
                _loading.value = false
            }
        }
    }
}
