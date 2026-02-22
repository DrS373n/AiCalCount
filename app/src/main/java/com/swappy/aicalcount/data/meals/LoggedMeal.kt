package com.swappy.aicalcount.data.meals

import java.time.LocalDate

/**
 * A single logged meal entry for the diary.
 * @param id Unique id for this entry (for deletion/updates).
 * @param date Date of the meal (yyyy-MM-dd).
 * @param title Display title (e.g. recipe name or "Grilled chicken salad").
 * @param imagePath Optional path to saved image file.
 * @param calories Calories for this meal.
 * @param proteinG Protein in grams.
 * @param carbsG Carbs in grams.
 * @param fatG Fat in grams.
 * @param timestampMillis When the meal was logged (for ordering).
 */
data class LoggedMeal(
    val id: String,
    val date: String,
    val title: String,
    val imagePath: String? = null,
    val calories: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val timestampMillis: Long = System.currentTimeMillis(),
) {
    val localDate: LocalDate get() = LocalDate.parse(date)
}
