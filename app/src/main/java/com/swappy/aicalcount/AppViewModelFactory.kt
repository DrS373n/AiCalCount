package com.swappy.aicalcount

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val onboardingRepository = OnboardingRepository(application)
    private val progressRepository = ProgressRepository(application)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(application, onboardingRepository, progressRepository) as T
            modelClass.isAssignableFrom(MealQaViewModel::class.java) -> MealQaViewModel(application) as T
            modelClass.isAssignableFrom(DietPlanViewModel::class.java) -> DietPlanViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
