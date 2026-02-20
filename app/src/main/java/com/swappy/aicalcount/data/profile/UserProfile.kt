package com.swappy.aicalcount.data.profile

data class UserProfile(
    val displayName: String = "",
    val weightKg: Float = 0f,
    val goalWeightKg: Float = 0f,
    val heightCm: Float = 0f,
    val age: Int = 0,
    val profilePhotoPath: String = ""
)
