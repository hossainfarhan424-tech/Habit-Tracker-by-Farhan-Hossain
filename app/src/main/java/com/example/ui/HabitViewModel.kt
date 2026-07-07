package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    init {
        val database = HabitDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())
        viewModelScope.launch {
            repository.populateDefaultHabitsIfEmpty()
        }
    }

    // State for selected month and year
    private val calendar = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1) // 1-12
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Expose flows from repository
    val habits: StateFlow<List<Habit>> = repository.habits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completions: StateFlow<List<HabitCompletion>> = repository.completions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val wellness: StateFlow<List<DailyWellness>> = repository.wellness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun toggleCompletion(habitId: Int, dateString: String, complete: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, dateString, complete)
        }
    }

    fun updateWellness(dateString: String, mood: Int, sleepHours: Float) {
        viewModelScope.launch {
            repository.updateWellness(DailyWellness(dateString, mood, sleepHours))
        }
    }

    fun addHabit(name: String, emoji: String) {
        viewModelScope.launch {
            val currentList = habits.value
            val maxOrder = currentList.maxOfOrNull { it.order } ?: 0
            repository.addHabit(Habit(name = name, emoji = emoji, order = maxOrder + 1))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }
}
