package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HabitRepository(private val habitDao: HabitDao) {
    val habits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    val completions: Flow<List<HabitCompletion>> = habitDao.getAllCompletionsFlow()
    val wellness: Flow<List<DailyWellness>> = habitDao.getAllWellnessFlow()
    val quests: Flow<List<Quest>> = habitDao.getAllQuestsFlow()
    val onboardingState: Flow<OnboardingState?> = habitDao.getOnboardingStateFlow()

    suspend fun saveOnboardingState(state: OnboardingState) {
        habitDao.insertOnboardingState(state)
    }

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

    suspend fun addQuest(quest: Quest) {
        habitDao.insertQuest(quest)
    }

    suspend fun updateQuest(quest: Quest) {
        habitDao.updateQuest(quest)
    }

    suspend fun deleteQuest(quest: Quest) {
        habitDao.deleteQuest(quest)
    }

    suspend fun populateDefaultHabitsIfEmpty() {
        val currentHabits = habitDao.getAllHabits()
        
        // Proactively clean up any existing duplicates by name to fix the user's corrupted database
        val seenNames = mutableSetOf<String>()
        currentHabits.forEach { habit ->
            if (seenNames.contains(habit.name)) {
                habitDao.deleteHabit(habit)
            } else {
                seenNames.add(habit.name)
            }
        }

        val cleanedHabits = habitDao.getAllHabits()
        if (cleanedHabits.isEmpty()) {
            val defaults = listOf(
                Habit(name = "Exercise", emoji = "💪", order = 1, attribute = "STR"),
                Habit(name = "Walking", emoji = "🏃", order = 2, attribute = "VIT"),
                Habit(name = "Meditation", emoji = "🧘", order = 3, attribute = "MND"),
                Habit(name = "Study", emoji = "📚", order = 4, attribute = "INT"),
                Habit(name = "Wake Up 6:30...", emoji = "⏰", order = 5, attribute = "DIS")
            )
            defaults.forEach { habitDao.insertHabit(it) }
        }

        // Also populate default Quests
        val currentQuests = habitDao.getAllQuestsFlow().first()
        if (currentQuests.isEmpty()) {
            val defaultQuests = listOf(
                Quest(title = "The Mind Shield", category = "Daily", xpReward = 100, attributeToReward = "MND", isCompleted = false),
                Quest(title = "Unbroken Focus", category = "Daily", xpReward = 120, attributeToReward = "INT", isCompleted = false),
                Quest(title = "Iron Physical", category = "Daily", xpReward = 150, attributeToReward = "STR", isCompleted = false),
                Quest(title = "Sentry Rest", category = "Daily", xpReward = 100, attributeToReward = "VIT", isCompleted = false),
                Quest(title = "Apex Predator", category = "Epic", xpReward = 1000, attributeToReward = "STR", isCompleted = false),
                Quest(title = "Sovereign Sage", category = "Epic", xpReward = 1200, attributeToReward = "INT", isCompleted = false),
                Quest(title = "Unshakable Will", category = "Epic", xpReward = 2000, attributeToReward = "DIS", isCompleted = false)
            )
            defaultQuests.forEach { habitDao.insertQuest(it) }
        }
    }

    suspend fun resetHabitsToDefaults() {
        val currentHabits = habitDao.getAllHabits()
        currentHabits.forEach { habitDao.deleteHabit(it) }
        val defaults = listOf(
            Habit(name = "Exercise", emoji = "💪", order = 1, attribute = "STR"),
            Habit(name = "Walking", emoji = "🏃", order = 2, attribute = "VIT"),
            Habit(name = "Meditation", emoji = "🧘", order = 3, attribute = "MND"),
            Habit(name = "Study", emoji = "📚", order = 4, attribute = "INT"),
            Habit(name = "Wake Up 6:30...", emoji = "⏰", order = 5, attribute = "DIS")
        )
        defaults.forEach { habitDao.insertHabit(it) }
    }

    suspend fun getBackupData(): BackupData {
        val habitsList = habitDao.getAllHabits()
        val completionsList = habitDao.getAllCompletions()
        val wellnessList = habitDao.getAllWellness()
        val questsList = habitDao.getAllQuests()
        val onboarding = habitDao.getOnboardingState()
        return BackupData(habitsList, completionsList, wellnessList, questsList, onboarding)
    }

    suspend fun restoreBackupData(backupData: BackupData) {
        // Clear all
        habitDao.deleteAllHabits()
        habitDao.deleteAllCompletions()
        habitDao.deleteAllWellness()
        habitDao.deleteAllQuests()
        habitDao.deleteAllOnboarding()

        // Insert restored items
        habitDao.insertHabits(backupData.habits)
        habitDao.insertCompletions(backupData.completions)
        habitDao.insertWellnessList(backupData.wellness)
        habitDao.insertQuests(backupData.quests)
        backupData.onboarding?.let { habitDao.insertOnboardingState(it) }
    }
}
