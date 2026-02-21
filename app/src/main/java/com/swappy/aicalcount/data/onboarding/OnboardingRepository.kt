package com.swappy.aicalcount.data.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "onboarding_preferences"
)

private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
private val KEY_HAS_LOGGED_MEAL = booleanPreferencesKey("has_logged_meal")
private val KEY_PROFILE_SETUP_COMPLETE = booleanPreferencesKey("profile_setup_complete")
private val KEY_HAS_SEEN_FAB_TOOLTIP = booleanPreferencesKey("has_seen_fab_tooltip")

class OnboardingRepository(private val context: Context) {

    val isOnboardingComplete: Flow<Boolean> = context.onboardingDataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    /** True once the user has logged or analyzed at least one meal. New users see a blank home until then. */
    val hasLoggedMeal: Flow<Boolean> = context.onboardingDataStore.data.map { prefs ->
        prefs[KEY_HAS_LOGGED_MEAL] ?: false
    }

    /** True once the user has completed the post-onboarding profile setup. */
    val isProfileSetupComplete: Flow<Boolean> = context.onboardingDataStore.data.map { prefs ->
        prefs[KEY_PROFILE_SETUP_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = true
        }
    }

    suspend fun setHasLoggedMeal() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_HAS_LOGGED_MEAL] = true
        }
    }

    /** Reset so home shows blank state; call when user completes onboarding. */
    suspend fun clearHasLoggedMeal() {
        context.onboardingDataStore.edit { prefs ->
            prefs.remove(KEY_HAS_LOGGED_MEAL)
        }
    }

    suspend fun setProfileSetupComplete() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_PROFILE_SETUP_COMPLETE] = true
        }
    }

    suspend fun getHasSeenFabTooltip(): Boolean =
        context.onboardingDataStore.data.first()[KEY_HAS_SEEN_FAB_TOOLTIP] ?: false

    suspend fun setHasSeenFabTooltip() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_FAB_TOOLTIP] = true
        }
    }
}
