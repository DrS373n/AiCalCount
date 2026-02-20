package com.swappy.aicalcount

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swappy.aicalcount.data.onboarding.OnboardingRepository
import com.swappy.aicalcount.data.progress.ProgressRepository

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val onboardingRepository = OnboardingRepository(application)
    private val progressRepository = ProgressRepository(application)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, onboardingRepository, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
