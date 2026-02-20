package com.swappy.aicalcount.util

import android.content.Context
import androidx.core.content.edit

class ApiUsageManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("api_usage", Context.MODE_PRIVATE)

    fun canMakeApiCall(): Boolean {
        val currentCalls = sharedPreferences.getInt("api_calls", 0)
        return currentCalls < 150 // Spoonacular's free plan has a limit of 150 points per day
    }

    fun recordApiCall() {
        sharedPreferences.edit {
            val currentCalls = sharedPreferences.getInt("api_calls", 0)
            putInt("api_calls", currentCalls + 1)
        }
    }

    fun resetApiCallCount() {
        sharedPreferences.edit {
            putInt("api_calls", 0)
        }
    }
}
