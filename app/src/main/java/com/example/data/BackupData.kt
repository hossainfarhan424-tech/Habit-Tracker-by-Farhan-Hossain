package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val habits: List<Habit>,
    val completions: List<HabitCompletion>,
    val wellness: List<DailyWellness>,
    val quests: List<Quest>,
    val onboarding: OnboardingState?
)
