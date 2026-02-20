package com.swappy.aicalcount.data.progress

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "progress_preferences"
)

private val KEY_STREAK_COUNT = intPreferencesKey("streak_count")
private val KEY_LAST_STREAK_DATE = stringPreferencesKey("last_streak_date")
private val KEY_LOGGED_DATES = stringSetPreferencesKey("logged_dates")
private val KEY_WEIGHT_HISTORY = stringPreferencesKey("weight_history")
private val KEY_TODAY_MACROS_DATE = stringPreferencesKey("today_macros_date")
private val KEY_TODAY_PROTEIN = floatPreferencesKey("today_protein")
private val KEY_TODAY_CARBS = floatPreferencesKey("today_carbs")
private val KEY_TODAY_FAT = floatPreferencesKey("today_fat")

private fun parseWeightHistory(str: String?): List<Pair<String, Float>> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split(";").mapNotNull { entry ->
        val parts = entry.split(",")
        if (parts.size != 2) return@mapNotNull null
        val w = parts[1].toFloatOrNull() ?: return@mapNotNull null
        parts[0] to w
    }.sortedBy { it.first }
}

class ProgressRepository(private val context: Context) {

    val streakCount: Flow<Int> = context.progressDataStore.data.map { prefs ->
        prefs[KEY_STREAK_COUNT] ?: 0
    }

    val lastStreakDate: Flow<String> = context.progressDataStore.data.map { prefs ->
        prefs[KEY_LAST_STREAK_DATE] ?: ""
    }

    /** Set of dates "yyyy-MM-dd" when the user logged a meal (goal-related activity). */
    val loggedDates: Flow<Set<String>> = context.progressDataStore.data.map { prefs ->
        prefs[KEY_LOGGED_DATES] ?: emptySet()
    }

    /** Weight history: list of (date "yyyy-MM-dd", weightKg) sorted by date ascending. */
    val weightHistory: Flow<List<Pair<String, Float>>> = context.progressDataStore.data.map { prefs ->
        parseWeightHistory(prefs[KEY_WEIGHT_HISTORY])
    }

    /** Today's macros (protein, carbs, fat in grams). Reset each day. */
    val todayProtein: Flow<Float> = context.progressDataStore.data.map { prefs ->
        if (prefs[KEY_TODAY_MACROS_DATE] == LocalDate.now().toString()) prefs[KEY_TODAY_PROTEIN] ?: 0f else 0f
    }
    val todayCarbs: Flow<Float> = context.progressDataStore.data.map { prefs ->
        if (prefs[KEY_TODAY_MACROS_DATE] == LocalDate.now().toString()) prefs[KEY_TODAY_CARBS] ?: 0f else 0f
    }
    val todayFat: Flow<Float> = context.progressDataStore.data.map { prefs ->
        if (prefs[KEY_TODAY_MACROS_DATE] == LocalDate.now().toString()) prefs[KEY_TODAY_FAT] ?: 0f else 0f
    }

    /** Add a meal's macros to today's totals. Resets to this meal if it's a new day. */
    suspend fun addMealMacros(proteinG: Float, carbsG: Float, fatG: Float) {
        val todayStr = LocalDate.now().toString()
        context.progressDataStore.edit { prefs ->
            val storedDate = prefs[KEY_TODAY_MACROS_DATE]
            if (storedDate == todayStr) {
                prefs[KEY_TODAY_PROTEIN] = (prefs[KEY_TODAY_PROTEIN] ?: 0f) + proteinG
                prefs[KEY_TODAY_CARBS] = (prefs[KEY_TODAY_CARBS] ?: 0f) + carbsG
                prefs[KEY_TODAY_FAT] = (prefs[KEY_TODAY_FAT] ?: 0f) + fatG
            } else {
                prefs[KEY_TODAY_MACROS_DATE] = todayStr
                prefs[KEY_TODAY_PROTEIN] = proteinG
                prefs[KEY_TODAY_CARBS] = carbsG
                prefs[KEY_TODAY_FAT] = fatG
            }
        }
    }

    /** Add a weight entry for the given date. Replaces entry if date already exists. */
    suspend fun addWeightEntry(dateStr: String, weightKg: Float) {
        context.progressDataStore.edit { prefs ->
            val current = parseWeightHistory(prefs[KEY_WEIGHT_HISTORY]).toMutableList()
            current.removeAll { it.first == dateStr }
            current.add(dateStr to weightKg)
            current.sortBy { it.first }
            prefs[KEY_WEIGHT_HISTORY] = current.joinToString(";") { "${it.first},${it.second}" }
        }
    }

    /** Call when the user logs a meal / meets goal for today. Updates streak and adds today to logged dates. */
    suspend fun recordMealLogged() {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        context.progressDataStore.edit { prefs ->
            val currentStreak = prefs[KEY_STREAK_COUNT] ?: 0
            val lastDate = prefs[KEY_LAST_STREAK_DATE] ?: ""
            val dates = (prefs[KEY_LOGGED_DATES] ?: emptySet()).toMutableSet()
            dates.add(todayStr)
            // Keep only last 14 days to avoid unbounded growth
            val cutoff = today.minusDays(14).toString()
            dates.removeAll { it < cutoff }
            prefs[KEY_LOGGED_DATES] = dates
            val newStreak = when {
                lastDate == yesterdayStr -> currentStreak + 1
                lastDate == todayStr -> currentStreak
                else -> 1
            }
            prefs[KEY_STREAK_COUNT] = newStreak
            prefs[KEY_LAST_STREAK_DATE] = todayStr
        }
    }
}
