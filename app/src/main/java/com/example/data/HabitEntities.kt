package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val attribute: String = "DIS" // "STR", "INT", "MND", "DIS", "VIT"
)

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateString"], unique = true)]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String, // format "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_wellness")
data class DailyWellness(
    @PrimaryKey val dateString: String, // format "YYYY-MM-DD"
    val mood: Int = 0, // 1 to 5 scale (0 for unrecorded)
    val sleepHours: Float = 0f // 0 for unrecorded
)

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Daily", "Epic", "Workout", "Study"
    val xpReward: Int,
    val attributeToReward: String, // "STR", "INT", "MND", "DIS", "VIT"
    val isCompleted: Boolean = false,
    val dateString: String = "", // e.g. "2026-07-08" or empty for non-daily
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey val id: Int = 1,
    val completed: Boolean = false,
    val gender: String = "",
    val goal: String = "",
    val motivations: String = "",
    val focusAreas: String = "",
    val fitnessLevel: String = "",
    val activityLevel: String = "",
    val age: Int = 18,
    val height: String = "",
    val currentWeight: Float = 0f,
    val targetWeight: Float = 0f,
    val healthIssues: String = "",
    val equipment: String = "",
    val workoutFrequency: Int = 4,
    val workoutDays: String = "",
    val reminderEnabled: Boolean = true
)
