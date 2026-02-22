package com.swappy.aicalcount

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swappy.aicalcount.data.meals.MealDiaryRepository
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val onboardingRepository = OnboardingRepository(application)
    private val progressRepository = ProgressRepository(application)
    private val mealDiaryRepository = MealDiaryRepository(application)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, onboardingRepository, progressRepository, mealDiaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
