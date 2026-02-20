package com.swappy.aicalcount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swappy.aicalcount.data.diet.ActivityLevel
import com.swappy.aicalcount.data.diet.DietPreferences
import com.swappy.aicalcount.data.diet.DietPreferencesRepository
import com.swappy.aicalcount.data.diet.DietGoal
import com.swappy.aicalcount.data.diet.DietRestriction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DietPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DietPreferencesRepository(application)

    private val _step = MutableStateFlow(0)
    val step = _step.asStateFlow()

    private val _preferences = MutableStateFlow(DietPreferences())
    val preferences = _preferences.asStateFlow()

    private val _planComplete = MutableStateFlow(false)
    val planComplete = _planComplete.asStateFlow()

    init {
        viewModelScope.launch {
            repo.preferences.collect { _preferences.value = it }
        }
    }

    fun onGoalSelect(goal: DietGoal) {
        _preferences.value = _preferences.value.copy(goal = goal)
    }

    fun onActivitySelect(level: ActivityLevel) {
        _preferences.value = _preferences.value.copy(activityLevel = level)
    }

    fun onRestrictionToggle(restriction: DietRestriction) {
        if (restriction == DietRestriction.None) {
            _preferences.value = _preferences.value.copy(restrictions = emptyList())
            return
        }
        val current = _preferences.value.restrictions
        val next = if (current.contains(restriction)) current - restriction else current + restriction
        _preferences.value = _preferences.value.copy(restrictions = next)
    }

    fun nextStep() {
        if (_step.value < 2) _step.value = _step.value + 1
    }

    fun completePlan() {
        viewModelScope.launch {
            repo.save(_preferences.value)
            _planComplete.value = true
        }
    }

    fun resetPlanView() {
        _planComplete.value = false
        _step.value = 0
    }
}
