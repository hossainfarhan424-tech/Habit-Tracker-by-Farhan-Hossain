package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val habits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    val completions: Flow<List<HabitCompletion>> = habitDao.getAllCompletionsFlow()
    val wellness: Flow<List<DailyWellness>> = habitDao.getAllWellnessFlow()

    suspend fun addHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleCompletion(habitId: Int, dateString: String, complete: Boolean) {
        if (complete) {
            habitDao.insertCompletion(HabitCompletion(habitId = habitId, dateString = dateString))
        } else {
            habitDao.deleteCompletion(habitId, dateString)
        }
    }

    suspend fun updateWellness(wellness: DailyWellness) {
        habitDao.insertOrUpdateWellness(wellness)
    }

    suspend fun populateDefaultHabitsIfEmpty() {
        val currentHabits = habitDao.getAllHabits()
        if (currentHabits.isEmpty()) {
            val defaults = listOf(
                Habit(name = "Wake up at 05:00", emoji = "⏰", order = 1),
                Habit(name = "Gym", emoji = "💪", order = 2),
                Habit(name = "Reading / Learning", emoji = "📚", order = 3),
                Habit(name = "Day Planning", emoji = "🗓️", order = 4),
                Habit(name = "Deep Work", emoji = "💻", order = 5),
                Habit(name = "Healthy Eating", emoji = "🥗", order = 6),
                Habit(name = "No Junk Food", emoji = "🚫", order = 7),
                Habit(name = "Social Media Detox", emoji = "📵", order = 8),
                Habit(name = "Goal Journaling", emoji = "📝", order = 9),
                Habit(name = "Cold Shower", emoji = "❄️", order = 10),
                Habit(name = "Learn a Skill", emoji = "🧠", order = 11),
                Habit(name = "Meditate", emoji = "🧘", order = 12)
            )
            defaults.forEach { habitDao.insertHabit(it) }
        }
    }
}
