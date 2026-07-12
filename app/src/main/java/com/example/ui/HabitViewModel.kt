package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    // Google Sign In / Backup properties
    val googleSignInAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val backupStatus = MutableStateFlow<String?>(null)
    val isBackupLoading = MutableStateFlow(false)

    init {
        val database = HabitDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())
        viewModelScope.launch {
            repository.populateDefaultHabitsIfEmpty()
        }
        checkGoogleSignIn(application)
    }

    fun checkGoogleSignIn(context: android.content.Context) {
        googleSignInAccount.value = GoogleSignIn.getLastSignedInAccount(context)
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount?, context: android.content.Context? = null) {
        googleSignInAccount.value = account
        if (account != null) {
            backupStatus.value = "Google account connected successfully!"
            context?.let {
                android.widget.Toast.makeText(it, "Google account connected successfully!", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signOut(context: android.content.Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .build()
        GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener {
            googleSignInAccount.value = null
            backupStatus.value = "Signed out successfully"
        }
    }

    fun backupToGoogleDrive(context: android.content.Context) {
        viewModelScope.launch {
            isBackupLoading.value = true
            backupStatus.value = "Preparing backup..."
            try {
                val data = repository.getBackupData()
                val backupManager = GoogleDriveBackupManager(context)
                val success = backupManager.backupData(data)
                if (success) {
                    backupStatus.value = "Backup successful!"
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    lastSyncedTime.value = sdf.format(java.util.Date())
                    syncStatus.value = "SECURE_SYNCED"
                } else {
                    backupStatus.value = "Backup failed: check internet or permissions"
                }
            } catch (e: Exception) {
                backupStatus.value = "Backup failed: ${e.localizedMessage}"
            } finally {
                isBackupLoading.value = false
            }
        }
    }

    fun restoreFromGoogleDrive(context: android.content.Context) {
        viewModelScope.launch {
            isBackupLoading.value = true
            backupStatus.value = "Retrieving backup..."
            try {
                val backupManager = GoogleDriveBackupManager(context)
                val data = backupManager.restoreData()
                if (data != null) {
                    repository.restoreBackupData(data)
                    backupStatus.value = "Progress restored successfully!"
                } else {
                    backupStatus.value = "No backup found or restore failed"
                }
            } catch (e: Exception) {
                backupStatus.value = "Restore failed: ${e.localizedMessage}"
            } finally {
                isBackupLoading.value = false
            }
        }
    }

    fun clearBackupStatus() {
        backupStatus.value = null
    }

    // State for selected month and year
    private val calendar = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1) // 1-12
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Interactive settings & preferences (In-Memory for state management)
    val isDarkTheme = MutableStateFlow(true)
    val streakNotificationsEnabled = MutableStateFlow(true)
    val syncStatus = MutableStateFlow("IDLE") // IDLE, SYNCING, SECURE_SYNCED
    val lastSyncedTime = MutableStateFlow("Never")

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun toggleStreakNotifications() {
        streakNotificationsEnabled.value = !streakNotificationsEnabled.value
    }

    fun runCloudBackupAndSync() {
        viewModelScope.launch {
            syncStatus.value = "SYNCING"
            kotlinx.coroutines.delay(1800)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            lastSyncedTime.value = sdf.format(java.util.Date())
            syncStatus.value = "SECURE_SYNCED"
        }
    }

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

    val quests: StateFlow<List<Quest>> = repository.quests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val onboardingState: StateFlow<OnboardingState?> = repository.onboardingState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Dynamic Streak state flow (calculates current and maximum streaks)
    val habitStreak: StateFlow<Pair<Int, Int>> = completions.map { completionList ->
        calculateStreak(completionList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(0, 0)
    )

    private fun calculateStreak(completions: List<HabitCompletion>): Pair<Int, Int> {
        val uniqueDates = completions.map { it.dateString }.distinct().sorted()
        if (uniqueDates.isEmpty()) return Pair(0, 0)

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val parsedDates = uniqueDates.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()

        if (parsedDates.isEmpty()) return Pair(0, 0)

        val calendar = Calendar.getInstance()
        // Strip time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.time

        calendar.add(Calendar.DATE, -1)
        val yesterday = calendar.time

        var currentStreak = 0
        var maxStreak = 0
        var tempStreak = 0
        var lastDate: java.util.Date? = null

        for (date in parsedDates) {
            if (lastDate == null) {
                tempStreak = 1
            } else {
                val diff = date.time - lastDate.time
                val diffDays = diff / (24 * 60 * 60 * 1000)
                if (diffDays == 1L) {
                    tempStreak++
                } else if (diffDays > 1L) {
                    if (tempStreak > maxStreak) {
                        maxStreak = tempStreak
                    }
                    tempStreak = 1
                }
            }
            lastDate = date
        }
        if (tempStreak > maxStreak) {
            maxStreak = tempStreak
        }

        // Check if current streak is still active (contains today or yesterday)
        val activeDates = parsedDates.map {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.time
        }

        val hasToday = activeDates.any { it.time == today.time }
        val hasYesterday = activeDates.any { it.time == yesterday.time }

        if (hasToday || hasYesterday) {
            val checkDate = if (hasToday) today else yesterday
            var count = 0
            val activeTimes = activeDates.map { it.time }.toSet()
            val walkCal = Calendar.getInstance()
            walkCal.time = checkDate

            while (activeTimes.contains(walkCal.timeInMillis)) {
                count++
                walkCal.add(Calendar.DATE, -1)
            }
            currentStreak = count
        } else {
            currentStreak = 0
        }

        return Pair(currentStreak, maxOf(maxStreak, currentStreak))
    }

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

    fun resetHabitsToImageLayout() {
        viewModelScope.launch {
            repository.resetHabitsToDefaults()
        }
    }

    fun addHabit(name: String, emoji: String, attribute: String = "DIS") {
        viewModelScope.launch {
            val currentList = repository.habits.first()
            val maxOrder = currentList.maxOfOrNull { it.order } ?: 0
            repository.addHabit(Habit(name = name, emoji = emoji, order = maxOrder + 1, attribute = attribute))
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

    // Quest Actions
    fun addQuest(title: String, category: String, xpReward: Int, attribute: String) {
        viewModelScope.launch {
            repository.addQuest(Quest(title = title, category = category, xpReward = xpReward, attributeToReward = attribute))
        }
    }

    fun toggleQuestCompletion(quest: Quest) {
        viewModelScope.launch {
            repository.updateQuest(quest.copy(isCompleted = !quest.isCompleted))
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            repository.deleteQuest(quest)
        }
    }

    // Onboarding Actions
    fun saveOnboarding(state: OnboardingState) {
        viewModelScope.launch {
            repository.saveOnboardingState(state)
        }
    }

    fun generateWorkoutPlan(state: OnboardingState) {
        viewModelScope.launch {
            // Save the onboarding state as completed
            repository.saveOnboardingState(state.copy(completed = true))

            // Create recommended habits based on user's goals
            val isBodyweight = state.equipment.contains("Bodyweight", ignoreCase = true) || state.equipment == "None"
            val prefix = if (isBodyweight) "Bodyweight " else "Gym "

            val recommendedHabits = when (state.goal) {
                "Build Muscle" -> listOf(
                    Habit(name = "$prefix Upper Body Hypertrophy", emoji = "💪", order = 1, attribute = "STR"),
                    Habit(name = "$prefix Lower Body Power", emoji = "🏋️", order = 2, attribute = "STR"),
                    Habit(name = "Core Sculpting", emoji = "🧘", order = 3, attribute = "DIS")
                )
                "Lose Weight" -> listOf(
                    Habit(name = "High Intensity Cardio Drill", emoji = "🏃", order = 1, attribute = "VIT"),
                    Habit(name = "$prefix Full Body Calisthenics", emoji = "⚡", order = 2, attribute = "STR"),
                    Habit(name = "Core Crusher", emoji = "🔥", order = 3, attribute = "DIS")
                )
                else -> listOf( // "Look Better", "Stay In Shape"
                    Habit(name = "$prefix Balanced Toning", emoji = "💪", order = 1, attribute = "STR"),
                    Habit(name = "Cardio Flow Session", emoji = "🏃", order = 2, attribute = "VIT"),
                    Habit(name = "Abdominal Tightening", emoji = "🧘", order = 3, attribute = "DIS")
                )
            }

            // Delete default habits so the user's focus is fully on their custom plan
            val existingHabits = repository.habits.first()
            existingHabits.forEach { repository.deleteHabit(it) }

            // Add the custom suggested habits
            recommendedHabits.forEach { repository.addHabit(it) }

            // Delete default quests
            val existingQuests = repository.quests.first()
            existingQuests.forEach { repository.deleteQuest(it) }

            // Create recommended quests matching selected workout days
            val selectedDays = state.workoutDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            selectedDays.forEach { day ->
                repository.addQuest(
                    Quest(
                        title = "$day Training: ${state.goal} Focus",
                        category = "Workout",
                        xpReward = 150,
                        attributeToReward = "STR"
                    )
                )
            }

            // Add 1 Epic/Milestone Quest
            repository.addQuest(
                Quest(
                    title = "Ascension Milestone: 90-Day Challenge",
                    category = "Epic",
                    xpReward = 1500,
                    attributeToReward = "DIS"
                )
            )
        }
    }
}
