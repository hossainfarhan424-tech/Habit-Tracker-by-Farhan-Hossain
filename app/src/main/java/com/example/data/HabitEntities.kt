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
    val createdAt: Long = System.currentTimeMillis()
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
