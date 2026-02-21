package com.swappy.aicalcount.util

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate

class ApiUsageManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("api_usage", Context.MODE_PRIVATE)

    private val dailyLimit = 150 // Spoonacular's free plan

    private fun ensureDateAndGetCalls(): Int {
        val today = LocalDate.now().toString()
        val storedDate = sharedPreferences.getString("api_calls_date", null)
        if (storedDate != today) {
            sharedPreferences.edit {
                putString("api_calls_date", today)
                putInt("api_calls", 0)
            }
            return 0
        }
        return sharedPreferences.getInt("api_calls", 0)
    }

    fun canMakeApiCall(): Boolean = getCurrentCalls() < dailyLimit

    fun getCurrentCalls(): Int = ensureDateAndGetCalls()

    fun getRemainingCalls(): Int = (dailyLimit - getCurrentCalls()).coerceAtLeast(0)

    fun recordApiCall() {
        val today = LocalDate.now().toString()
        val storedDate = sharedPreferences.getString("api_calls_date", null)
        val current = sharedPreferences.getInt("api_calls", 0)
        sharedPreferences.edit {
            putString("api_calls_date", today)
            putInt("api_calls", if (storedDate == today) current + 1 else 1)
        }
    }

    fun resetApiCallCount() {
        sharedPreferences.edit {
            putString("api_calls_date", LocalDate.now().toString())
            putInt("api_calls", 0)
        }
    }
}
