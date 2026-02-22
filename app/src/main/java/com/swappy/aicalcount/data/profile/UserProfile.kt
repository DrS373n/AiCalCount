package com.swappy.aicalcount.data.profile

data class UserProfile(
    val displayName: String = "",
    val biologicalSex: BiologicalSex? = null,
    val weightKg: Float = 0f,
    val goalWeightKg: Float = 0f,
    val heightCm: Float = 0f,
    val birthdateEpochDay: Long? = null,
    val age: Int = 0,
    val profilePhotoPath: String = ""
)
