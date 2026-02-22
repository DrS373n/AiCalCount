package com.swappy.aicalcount.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_profile"
)

private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
private val KEY_BIOLOGICAL_SEX = stringPreferencesKey("biological_sex")
private val KEY_WEIGHT_KG = floatPreferencesKey("weight_kg")
private val KEY_GOAL_WEIGHT_KG = floatPreferencesKey("goal_weight_kg")
private val KEY_HEIGHT_CM = floatPreferencesKey("height_cm")
private val KEY_BIRTHDATE_EPOCH_DAY = longPreferencesKey("birthdate_epoch_day")
private val KEY_AGE = intPreferencesKey("age")
private val KEY_PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")

class UserProfileRepository(private val context: Context) {

    val profile: Flow<UserProfile> = context.profileDataStore.data.map { prefs ->
        val sexString = prefs[KEY_BIOLOGICAL_SEX]
        val birthdateEpochDay = prefs[KEY_BIRTHDATE_EPOCH_DAY]
        val age = if (birthdateEpochDay != null) {
            ChronoUnit.YEARS.between(LocalDate.ofEpochDay(birthdateEpochDay), LocalDate.now()).toInt()
        } else {
            prefs[KEY_AGE] ?: 0
        }
        UserProfile(
            displayName = prefs[KEY_DISPLAY_NAME] ?: "",
            biologicalSex = sexString?.let { BiologicalSex.entries.find { e -> e.name == it } },
            weightKg = prefs[KEY_WEIGHT_KG] ?: 0f,
            goalWeightKg = prefs[KEY_GOAL_WEIGHT_KG] ?: 0f,
            heightCm = prefs[KEY_HEIGHT_CM] ?: 0f,
            birthdateEpochDay = birthdateEpochDay,
            age = age,
            profilePhotoPath = prefs[KEY_PROFILE_PHOTO_PATH] ?: ""
        )
    }

    suspend fun save(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_DISPLAY_NAME] = profile.displayName
            profile.biologicalSex?.name?.let { prefs[KEY_BIOLOGICAL_SEX] = it }
                ?: run { prefs.remove(KEY_BIOLOGICAL_SEX) }
            prefs[KEY_WEIGHT_KG] = profile.weightKg
            prefs[KEY_GOAL_WEIGHT_KG] = profile.goalWeightKg
            prefs[KEY_HEIGHT_CM] = profile.heightCm
            profile.birthdateEpochDay?.let { prefs[KEY_BIRTHDATE_EPOCH_DAY] = it }
                ?: run { prefs.remove(KEY_BIRTHDATE_EPOCH_DAY) }
            prefs[KEY_AGE] = profile.age
            prefs[KEY_PROFILE_PHOTO_PATH] = profile.profilePhotoPath
        }
    }

    /** Saves a bitmap as the profile photo and returns the file path. */
    fun saveProfilePhoto(bitmap: Bitmap): String {
        val file = File(context.filesDir, "profile_photo.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /** Saves profile photo from a content URI (e.g. from gallery). Returns the saved file path. */
    fun saveProfilePhotoFromUri(uri: Uri): String? {
        val bitmap = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) { null } ?: return null
        return saveProfilePhoto(bitmap)
    }
}
