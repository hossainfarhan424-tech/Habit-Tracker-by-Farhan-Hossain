package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY `order` ASC, id ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY `order` ASC, id ASC")
    suspend fun getAllHabits(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Completions
    @Query("SELECT * FROM habit_completions")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    suspend fun getAllCompletions(): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE dateString = :dateString")
    suspend fun getCompletionsForDate(dateString: String): List<HabitCompletion>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteCompletion(habitId: Int, dateString: String)

    // Wellness
    @Query("SELECT * FROM daily_wellness")
    fun getAllWellnessFlow(): Flow<List<DailyWellness>>

    @Query("SELECT * FROM daily_wellness")
    suspend fun getAllWellness(): List<DailyWellness>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWellness(wellness: DailyWellness)

    // Quests
    @Query("SELECT * FROM quests ORDER BY timestamp DESC")
    fun getAllQuestsFlow(): Flow<List<Quest>>

    @Query("SELECT * FROM quests")
    suspend fun getAllQuests(): List<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest): Long

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    // Onboarding
    @Query("SELECT * FROM onboarding_state WHERE id = 1")
    fun getOnboardingStateFlow(): Flow<OnboardingState?>

    @Query("SELECT * FROM onboarding_state WHERE id = 1")
    suspend fun getOnboardingState(): OnboardingState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingState(state: OnboardingState)

    // Bulk delete and insert for Backup/Restore
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()

    @Query("DELETE FROM daily_wellness")
    suspend fun deleteAllWellness()

    @Query("DELETE FROM quests")
    suspend fun deleteAllQuests()

    @Query("DELETE FROM onboarding_state")
    suspend fun deleteAllOnboarding()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletion>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessList(wellnessList: List<DailyWellness>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<Quest>)
}
