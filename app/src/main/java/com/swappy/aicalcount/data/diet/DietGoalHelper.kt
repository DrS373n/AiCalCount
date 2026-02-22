package com.swappy.aicalcount.data.diet

import com.swappy.aicalcount.data.profile.BiologicalSex
import com.swappy.aicalcount.data.profile.UserProfile

/**
 * Computes daily calorie and macro goals from diet preferences.
 * Used by Home macro rings and Coach so goals are a single source of truth.
 * Uses BMR (Mifflin–St Jeor) + TDEE when profile data is available.
 */
object DietGoalHelper {

    /** BMR (Mifflin–St Jeor) in kcal/day. Uses male formula for OTHER. Returns null if inputs invalid. */
    fun computeBmr(weightKg: Float, heightCm: Float, age: Int, biologicalSex: BiologicalSex?): Float? {
        if (weightKg <= 0f || heightCm <= 0f || age <= 0) return null
        val base = 10f * weightKg + 6.25f * heightCm - 5f * age
        val sexFactor = when (biologicalSex) {
            BiologicalSex.MALE -> 5f
            BiologicalSex.FEMALE -> -161f
            BiologicalSex.OTHER, null -> -78f // average of male and female
        }
        return (base + sexFactor).coerceIn(500f, 5000f)
    }

    /** TDEE = BMR × activity multiplier (Sedentary 1.2, Lightly 1.375, Moderate 1.55, High 1.725). */
    fun computeTdee(bmr: Float, activityLevel: ActivityLevel): Float {
        val multiplier = when (activityLevel) {
            ActivityLevel.Sedentary -> 1.2f
            ActivityLevel.LightlyActive -> 1.375f
            ActivityLevel.ModeratelyActive -> 1.55f
            ActivityLevel.HighlyActive -> 1.725f
        }
        return bmr * multiplier
    }

    /**
     * Returns daily goals using BMR/TDEE when profile is available, otherwise fallback to preference-based estimate.
     * Applies deficit/surplus from goal and goal pace (~0.5/1/1.5 lb/week ≈ 250/500/750 kcal).
     */
    fun computeGoals(profile: UserProfile?, preferences: DietPreferences): MacroGoals {
        val bmr = profile?.let { computeBmr(it.weightKg, it.heightCm, it.age, it.biologicalSex) }
        val tdee = bmr?.let { computeTdee(it, preferences.activityLevel) }
        val baseCal = when {
            tdee != null -> {
                val paceKcal = when (preferences.goalPace) {
                    GoalPace.Slowly -> 250f
                    GoalPace.Steadily -> 500f
                    GoalPace.Quickly -> 750f
                }
                when (preferences.goal) {
                    DietGoal.LoseWeight -> (tdee - paceKcal).coerceAtLeast(1200f)
                    DietGoal.Maintain -> tdee
                    DietGoal.GainMuscle -> tdee + paceKcal
                }
            }
            else -> {
                val fallback = when (preferences.goal) {
                    DietGoal.LoseWeight -> 1800f
                    DietGoal.Maintain -> 2200f
                    DietGoal.GainMuscle -> 2500f
                }
                val activityMultiplier = when (preferences.activityLevel) {
                    ActivityLevel.Sedentary -> 0.95f
                    ActivityLevel.LightlyActive -> 1.0f
                    ActivityLevel.ModeratelyActive -> 1.1f
                    ActivityLevel.HighlyActive -> 1.25f
                }
                fallback * activityMultiplier
            }
        }
        val calories = baseCal.toInt().toFloat()
        val proteinG = (calories * 0.30f / 4f).toInt().toFloat()
        val carbsG = (calories * 0.40f / 4f).toInt().toFloat()
        val fatG = (calories * 0.30f / 9f).toInt().toFloat()
        return MacroGoals(calories = calories, proteinG = proteinG, carbsG = carbsG, fatG = fatG)
    }

    /** Legacy: returns goals from preferences only (no profile). Prefer computeGoals(profile, preferences). */
    fun computeGoals(preferences: DietPreferences): MacroGoals = computeGoals(null, preferences)

    data class MacroGoals(
        val calories: Float,
        val proteinG: Float,
        val carbsG: Float,
        val fatG: Float
    )
}
