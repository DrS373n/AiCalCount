package com.swappy.aicalcount.data.diet

/**
 * Computes daily calorie and macro goals from diet preferences.
 * Used by Home macro rings and Coach so goals are a single source of truth.
 */
object DietGoalHelper {

    /** Returns (dailyCalories, proteinG, carbsG, fatG) for the given preferences. */
    fun computeGoals(preferences: DietPreferences): MacroGoals {
        val baseCal = when (preferences.goal) {
            DietGoal.LoseWeight -> 1800f
            DietGoal.Maintain -> 2200f
            DietGoal.GainMuscle -> 2500f
        }
        val activityMultiplier = when (preferences.activityLevel) {
            ActivityLevel.Low -> 0.95f
            ActivityLevel.Medium -> 1f
            ActivityLevel.High -> 1.1f
        }
        val calories = (baseCal * activityMultiplier).toInt().toFloat()
        // Split: ~30% protein, 40% carbs, 30% fat
        val proteinG = (calories * 0.30f / 4f).toInt().toFloat()
        val carbsG = (calories * 0.40f / 4f).toInt().toFloat()
        val fatG = (calories * 0.30f / 9f).toInt().toFloat()
        return MacroGoals(calories = calories, proteinG = proteinG, carbsG = carbsG, fatG = fatG)
    }

    data class MacroGoals(
        val calories: Float,
        val proteinG: Float,
        val carbsG: Float,
        val fatG: Float
    )
}
