package com.swappy.aicalcount.data.diet

data class DietPreferences(
    val goal: DietGoal = DietGoal.Maintain,
    val activityLevel: ActivityLevel = ActivityLevel.ModeratelyActive,
    val goalPace: GoalPace = GoalPace.Steadily,
    val restrictions: List<DietRestriction> = emptyList()
)

enum class DietGoal { LoseWeight, Maintain, GainMuscle }

/** How quickly the user wants to reach their goal (e.g. ±0.5, ±1, ±1.5 lbs/week). */
enum class GoalPace { Slowly, Steadily, Quickly }
enum class ActivityLevel {
    Sedentary,       // Little or no exercise
    LightlyActive,   // Light exercise 1–3 days/week
    ModeratelyActive, // Moderate exercise 3–5 days/week
    HighlyActive     // Hard exercise 6–7 days/week
}
enum class DietRestriction { None, Vegetarian, Vegan, GlutenFree }
