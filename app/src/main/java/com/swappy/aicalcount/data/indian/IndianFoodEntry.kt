package com.swappy.aicalcount.data.indian

import com.google.gson.annotations.SerializedName

/**
 * Single entry from bundled Indian food JSON (IFCT-style or Kaggle).
 * Used by IndianLocalSource to build Recipe for extractAndAddMacros.
 */
data class IndianFoodEntry(
    val id: Int,
    val name: String,
    val aliases: List<String> = emptyList(),
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    @SerializedName("fiber") val fiber: Double = 0.0,
    @SerializedName("sugar") val sugar: Double = 0.0,
    @SerializedName("sodium") val sodium: Double = 0.0,
)
