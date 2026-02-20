package com.swappy.aicalcount.data.diet

data class DietPreferences(
    val goal: DietGoal = DietGoal.Maintain,
    val activityLevel: ActivityLevel = ActivityLevel.Medium,
    val restrictions: List<DietRestriction> = emptyList()
)

enum class DietGoal { LoseWeight, Maintain, GainMuscle }
enum class ActivityLevel { Low, Medium, High }
enum class DietRestriction { None, Vegetarian, Vegan, GlutenFree }
