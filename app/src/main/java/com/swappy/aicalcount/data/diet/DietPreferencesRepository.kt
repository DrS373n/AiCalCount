package com.swappy.aicalcount.data.diet

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dietDataStore: DataStore<Preferences> by preferencesDataStore(name = "diet_preferences")

private val KEY_PREFERENCES = stringPreferencesKey("diet_preferences")

// Simple JSON serialization for the small model (avoid adding kotlinx.serialization dependency by using a simple format)
private fun DietPreferences.toStorageString(): String = buildString {
    append(goal.name).append("|")
    append(activityLevel.name).append("|")
    append(restrictions.map { it.name }.joinToString(","))
}

private fun String.toDietPreferences(): DietPreferences {
    val parts = split("|")
    if (parts.size < 3) return DietPreferences()
    val goal = DietGoal.entries.find { it.name == parts[0] } ?: DietGoal.Maintain
    val activity = ActivityLevel.entries.find { it.name == parts[1] } ?: ActivityLevel.Medium
    val rest = parts[2].split(",").mapNotNull { s ->
        if (s.isBlank()) null else DietRestriction.entries.find { it.name == s }
    }.filter { it != DietRestriction.None }
    return DietPreferences(goal = goal, activityLevel = activity, restrictions = rest)
}

class DietPreferencesRepository(private val context: Context) {

    val preferences: Flow<DietPreferences> = context.dietDataStore.data.map { prefs ->
        prefs[KEY_PREFERENCES]?.toDietPreferences() ?: DietPreferences()
    }

    suspend fun save(preferences: DietPreferences) {
        context.dietDataStore.edit { it[KEY_PREFERENCES] = preferences.toStorageString() }
    }
}
