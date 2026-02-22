package com.swappy.aicalcount.data.meals

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.mealDiaryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "meal_diary"
)

private val KEY_MEALS = stringPreferencesKey("meals")

private const val SEP = "|"
private const val NEWLINE = "\n"

private fun LoggedMeal.toStorageString(): String =
    listOf(id, date, title, imagePath ?: "", calories.toString(), proteinG.toString(), carbsG.toString(), fatG.toString(), timestampMillis.toString()).joinToString(SEP)

private fun String.toLoggedMealOrNull(): LoggedMeal? {
    val parts = split(SEP)
    if (parts.size < 9) return null
    val id = parts[0]
    val date = parts[1]
    val title = parts[2]
    val imagePath = parts[3].takeIf { it.isNotEmpty() }
    val calories = parts[4].toFloatOrNull() ?: return null
    val proteinG = parts[5].toFloatOrNull() ?: return null
    val carbsG = parts[6].toFloatOrNull() ?: return null
    val fatG = parts[7].toFloatOrNull() ?: return null
    val timestampMillis = parts[8].toLongOrNull() ?: return null
    return LoggedMeal(
        id = id,
        date = date,
        title = title,
        imagePath = imagePath,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        timestampMillis = timestampMillis,
    )
}

private fun parseMealsList(str: String?): List<LoggedMeal> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split(NEWLINE).mapNotNull { line ->
        line.trim().takeIf { it.isNotEmpty() }?.toLoggedMealOrNull()
    }.sortedBy { it.timestampMillis }
}

class MealDiaryRepository(private val context: Context) {

    /** All logged meals, sorted by timestamp. */
    val allMeals: Flow<List<LoggedMeal>> = context.mealDiaryDataStore.data.map { prefs ->
        parseMealsList(prefs[KEY_MEALS])
    }

    /** Meals for a given date (yyyy-MM-dd), sorted by timestamp. */
    fun mealsForDate(date: String): Flow<List<LoggedMeal>> = allMeals.map { list ->
        list.filter { it.date == date }.sortedBy { it.timestampMillis }
    }

    suspend fun addMeal(meal: LoggedMeal) {
        context.mealDiaryDataStore.edit { prefs ->
            val current = parseMealsList(prefs[KEY_MEALS])
            val updated = (current + meal).sortedBy { it.timestampMillis }
            prefs[KEY_MEALS] = updated.joinToString(NEWLINE) { it.toStorageString() }
        }
    }

    suspend fun addMeal(
        date: String,
        title: String,
        imagePath: String?,
        calories: Float,
        proteinG: Float,
        carbsG: Float,
        fatG: Float,
    ): LoggedMeal {
        val meal = LoggedMeal(
            id = UUID.randomUUID().toString(),
            date = date,
            title = title,
            imagePath = imagePath,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
        )
        addMeal(meal)
        return meal
    }

    suspend fun deleteMeal(id: String) {
        context.mealDiaryDataStore.edit { prefs ->
            val current = parseMealsList(prefs[KEY_MEALS])
            val updated = current.filter { it.id != id }
            prefs[KEY_MEALS] = updated.joinToString(NEWLINE) { it.toStorageString() }
        }
    }
}
