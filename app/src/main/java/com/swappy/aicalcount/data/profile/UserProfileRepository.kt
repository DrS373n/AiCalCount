package com.swappy.aicalcount.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_profile"
)

private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
private val KEY_WEIGHT_KG = floatPreferencesKey("weight_kg")
private val KEY_GOAL_WEIGHT_KG = floatPreferencesKey("goal_weight_kg")
private val KEY_HEIGHT_CM = floatPreferencesKey("height_cm")
private val KEY_AGE = intPreferencesKey("age")
private val KEY_PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")

class UserProfileRepository(private val context: Context) {

    val profile: Flow<UserProfile> = context.profileDataStore.data.map { prefs ->
        UserProfile(
            displayName = prefs[KEY_DISPLAY_NAME] ?: "",
            weightKg = prefs[KEY_WEIGHT_KG] ?: 0f,
            goalWeightKg = prefs[KEY_GOAL_WEIGHT_KG] ?: 0f,
            heightCm = prefs[KEY_HEIGHT_CM] ?: 0f,
            age = prefs[KEY_AGE] ?: 0,
            profilePhotoPath = prefs[KEY_PROFILE_PHOTO_PATH] ?: ""
        )
    }

    suspend fun save(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_DISPLAY_NAME] = profile.displayName
            prefs[KEY_WEIGHT_KG] = profile.weightKg
            prefs[KEY_GOAL_WEIGHT_KG] = profile.goalWeightKg
            prefs[KEY_HEIGHT_CM] = profile.heightCm
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
