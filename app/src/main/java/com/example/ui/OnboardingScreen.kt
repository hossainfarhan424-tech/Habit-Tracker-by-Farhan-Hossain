package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OnboardingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: HabitViewModel,
    onComplete: (OnboardingState) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    
    // Core onboarding data state
    var gender by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    val motivations = remember { mutableStateListOf<String>() }
    val focusAreas = remember { mutableStateListOf<String>() }
    var fitnessLevel by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(18) }
    var isCm by remember { mutableStateOf(false) }
    var heightFeet by remember { mutableStateOf(5) }
    var heightInches by remember { mutableStateOf(8) }
    var heightCm by remember { mutableStateOf(170) }
    var isLbs by remember { mutableStateOf(true) }
    var weightLbs by remember { mutableStateOf(160) }
    var weightKg by remember { mutableStateOf(73) }
    var targetWeightLbs by remember { mutableStateOf(155) }
    var targetWeightKg by remember { mutableStateOf(70) }
    var healthIssues by remember { mutableStateOf("None") }
    var equipment by remember { mutableStateOf("None (Bodyweight)") }
    var workoutFrequency by remember { mutableStateOf(4) }
    val workoutDays = remember { mutableStateListOf<String>() }
    var smartReminders by remember { mutableStateOf(true) }

    // Dialog state
    var showMoreDaysNeededDialog by remember { mutableStateOf(false) }

    // Theme palette
    val customBackground = Color(0xFF07090E)
    val cardBackground = Color(0xFF11141E)
    val borderStrokeColor = Color(0xFF1C2230)
    val neonBlue = Color(0xFF00C3FF)
    val neonPurple = Color(0xFF8B5CF6)
    val neonGreen = Color(0xFF10B981)
    val neonOrange = Color(0xFFFF9E00)
    val neonRed = Color(0xFFEF4444)
    val textWhite = Color(0xFFF1F5F9)
    val textMuted = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customBackground)
    ) {
        // Aesthetic Background Glow (The Arise Blue Aura Halo)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonBlue.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width / 2, size.height * 0.3f),
                    radius = size.width * 0.8f
                )
            )
        }

        // Phase Layouts
        when {
            step == 0 -> {
                // Accept Player Qualification Dialog (Opening Cinematic)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "ARISE",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = textWhite,
                            style = TextStyle(
                                shadow = Shadow(color = neonBlue, offset = Offset(0f, 0f), blurRadius = 16f)
                            )
                        )
                        Text(
                            text = "Level Up In Real Life",
                            color = textMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.5.dp, neonBlue), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBackground),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = neonBlue)
                                    Text(
                                        text = "NOTIFICATION",
                                        color = neonBlue,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TextStyle(shadow = Shadow(color = neonBlue, blurRadius = 8f))
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "You have acquired the qualifications to be a Player.\nWill you accept?",
                                    color = textWhite,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = { step = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("onboarding_accept_button")
                                ) {
                                    Text(
                                        text = "Accept",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            step in 1..15 -> {
                // Questionnaire Steps
                OnboardingQuestionLayout(
                    currentStep = step,
                    totalSteps = 15,
                    onBackClick = { if (step > 1) step-- },
                    content = {
                        when (step) {
                            1 -> ChooseGenderScreen(selected = gender, onSelect = { gender = it; step++ })
                            2 -> ChooseGoalScreen(selected = goal, onSelect = { goal = it; step++ })
                            3 -> HearAboutUsScreen(selected = source, onSelect = { source = it; step++ })
                            4 -> MotivationsScreen(
                                selected = motivations,
                                onContinue = { step++ }
                            )
                            5 -> FocusAreasScreen(
                                selected = focusAreas,
                                onContinue = { step++ }
                            )
                            6 -> ChooseFitnessLevelScreen(selected = fitnessLevel, onSelect = { fitnessLevel = it; step++ })
                            7 -> ChooseActivityLevelScreen(selected = activityLevel, onSelect = { activityLevel = it; step++ })
                            8 -> AgeScreen(age = age, onAgeChange = { age = it }, onContinue = { step++ })
                            9 -> HeightScreen(
                                isCm = isCm,
                                onUnitChange = { isCm = it },
                                feet = heightFeet,
                                inches = heightInches,
                                cm = heightCm,
                                onFeetChange = { heightFeet = it },
                                onInchesChange = { heightInches = it },
                                onCmChange = { heightCm = it },
                                onContinue = { step++ }
                            )
                            10 -> WeightScreen(
                                isLbs = isLbs,
                                onUnitChange = { isLbs = it },
                                weightLbs = weightLbs,
                                weightKg = weightKg,
                                onLbsChange = { weightLbs = it },
                                onKgChange = { weightKg = it },
                                title = "What is your current weight?",
                                onContinue = { step++ }
                            )
                            11 -> WeightScreen(
                                isLbs = isLbs,
                                onUnitChange = { isLbs = it },
                                weightLbs = targetWeightLbs,
                                weightKg = targetWeightKg,
                                onLbsChange = { targetWeightLbs = it },
                                onKgChange = { targetWeightKg = it },
                                title = "What is your target weight?",
                                onContinue = { step++ }
                            )
                            12 -> HealthIssuesScreen(selected = healthIssues, onSelect = { healthIssues = it; step++ })
                            13 -> EquipmentScreen(selected = equipment, onSelect = { equipment = it; step++ })
                            14 -> WorkoutFrequencyScreen(
                                frequency = workoutFrequency,
                                onFrequencyChange = { workoutFrequency = it },
                                onContinue = { step++ }
                            )
                            15 -> WorkoutDaysScreen(
                                selectedDays = workoutDays,
                                frequency = workoutFrequency,
                                smartReminders = smartReminders,
                                onSmartRemindersChange = { smartReminders = it },
                                onContinue = {
                                    if (workoutDays.size < workoutFrequency) {
                                        showMoreDaysNeededDialog = true
                                    } else {
                                        step++
                                    }
                                }
                            )
                        }
                    }
                )
            }

            step == 16 -> {
                // Calibration / Processing Screen
                CalibrationScreen(onFinished = { step++ })
            }

            step == 17 -> {
                // Analysis Complete Slide
                AnalysisCompleteScreen(
                    age = age,
                    gender = gender,
                    onContinue = { step++ }
                )
            }

            step == 18 -> {
                // Red Danger Warning Slides
                WarningCarouselScreen(onFinished = { step++ })
            }

            step == 19 -> {
                // Your Arise Stats
                AriseStatsScreen(onContinue = { step++ })
            }

            step == 20 -> {
                // Your Potential Stats
                PotentialStatsScreen(onContinue = { step++ })
            }

            step == 21 -> {
                // Give yourself just 90 days graph + list
                NinetyDaysScreen(onContinue = { step++ })
            }

            step == 22 -> {
                // Solo Leveling heavy questions cinematic
                HeavyQuestionsScreen(onFinished = { step++ })
            }

            step == 23 -> {
                // Eclipse greeting slides
                LunarEclipseGreetingScreen(onFinished = { step++ })
            }

            step == 24 -> {
                // Account Sync Mock screen
                AccountSyncMockScreen(onFinished = { step++ })
            }

            step == 25 -> {
                // Final Plan Preview & Kickstart!
                val onboardingData = OnboardingState(
                    gender = gender,
                    goal = goal,
                    motivations = motivations.joinToString(","),
                    focusAreas = focusAreas.joinToString(","),
                    fitnessLevel = fitnessLevel,
                    activityLevel = activityLevel,
                    age = age,
                    height = if (isCm) "$heightCm cm" else "$heightFeet'$heightInches\"",
                    currentWeight = if (isLbs) weightLbs.toFloat() else weightKg.toFloat(),
                    targetWeight = if (isLbs) targetWeightLbs.toFloat() else targetWeightKg.toFloat(),
                    healthIssues = healthIssues,
                    equipment = equipment,
                    workoutFrequency = workoutFrequency,
                    workoutDays = workoutDays.joinToString(","),
                    reminderEnabled = smartReminders
                )
                FinalPlanPreviewScreen(
                    state = onboardingData,
                    onKickstart = {
                        onComplete(onboardingData)
                    }
                )
            }
        }
    }

    if (showMoreDaysNeededDialog) {
        Dialog(onDismissRequest = { showMoreDaysNeededDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "More Days Needed",
                        color = textWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please select at least ${workoutFrequency - workoutDays.size} more workout days to match your goal of working out $workoutFrequency times per week.",
                        color = textMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showMoreDaysNeededDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
                    ) {
                        Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== BASE LAYOUTS ====================
@Composable
fun OnboardingQuestionLayout(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val progress = currentStep.toFloat() / totalSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = currentStep > 1,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (currentStep > 1) Color.White else Color.Transparent
                )
            }

            // Simple progress percentage indicator
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color(0xFF00C3FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B2330))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Color(0xFF00C3FF))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

// ==================== CHOOSE GENDER ====================
@Composable
fun ChooseGenderScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Choose your gender",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val options = listOf(
            Triple("Male", "Male", "♂"),
            Triple("Female", "Female", "♀"),
            Triple("Other", "Other", "👤")
        )

        options.forEach { (id, label, icon) ->
            val isSelected = selected == id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(id) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = icon, color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}

// ==================== CHOOSE GOAL ====================
@Composable
fun ChooseGoalScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Choose your goal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val goals = listOf("Build Muscle", "Lose Weight", "Look Better", "Stay In Shape")

        goals.forEach { item ->
            val isSelected = selected == item
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(item) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = item, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==================== HEAR ABOUT US ====================
@Composable
fun HearAboutUsScreen(selected: String, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Where did you hear about us?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val sources = listOf("TikTok", "Instagram", "Facebook", "YouTube", "Google", "Friend or family", "Other")

        sources.forEach { src ->
            val isSelected = selected == src
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(src) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = src, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==================== MOTIVATIONS ====================
@Composable
fun MotivationsScreen(
    selected: List<String>,
    onContinue: () -> Unit
) {
    val items = listOf("Health", "Weight Loss", "Appearance", "Stress Relief", "Social Support", "Enjoyment")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What motivates you to work out?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            items.forEach { mot ->
                val isSelected = selected.contains(mot)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                (selected as MutableList).remove(mot)
                            } else {
                                (selected as MutableList).add(mot)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                .border(BorderStroke(1.5.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF64748B)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(text = mot, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== FOCUS AREAS ====================
@Composable
fun FocusAreasScreen(
    selected: List<String>,
    onContinue: () -> Unit
) {
    val options = listOf(
        Pair("Full Body", "🧍"),
        Pair("Chest", "🥩"),
        Pair("Back", "🛡️"),
        Pair("Arms", "💪"),
        Pair("Shoulders", "🦾"),
        Pair("Abs", "🌀"),
        Pair("Legs", "🦵"),
        Pair("Glutes", "🍑")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Choose your focus areas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { (area, emoji) ->
                val isSelected = selected.contains(area)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                (selected as MutableList).remove(area)
                            } else {
                                (selected as MutableList).add(area)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                    .border(BorderStroke(1.5.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF64748B)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(text = area, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== FITNESS LEVEL ====================
@Composable
fun ChooseFitnessLevelScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Choose your fitness level",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val levels = listOf(
            Pair("Beginner", "I'm new or have only tried it for a bit"),
            Pair("Intermediate", "I've lifted weights before"),
            Pair("Advanced", "I've been lifting weights for a while")
        )

        levels.forEach { (lvl, desc) ->
            val isSelected = selected == lvl
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(lvl) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = lvl, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = desc, color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        }
    }
}

// ==================== ACTIVITY LEVEL ====================
@Composable
fun ChooseActivityLevelScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Choose your activity level",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val items = listOf(
            Pair("Sedentary", "Little to no exercise"),
            Pair("Lightly Active", "Light exercise 1-3 days a week"),
            Pair("Moderately Active", "Moderate exercise 4-6 days a week"),
            Pair("Very Active", "Hard exercise every day")
        )

        items.forEach { (lvl, desc) ->
            val isSelected = selected == lvl
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(lvl) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = lvl, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = desc, color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        }
    }
}

// ==================== AGE SCREEN ====================
@Composable
fun AgeScreen(
    age: Int,
    onAgeChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "How old are you?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$age",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00C3FF)
                )
                Text(text = "years old", color = Color(0xFF64748B), fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (age > 12) onAgeChange(age - 1) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF11141E), CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFF1C2230)), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                    }

                    IconButton(
                        onClick = { if (age < 120) onAgeChange(age + 1) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF11141E), CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFF1C2230)), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== HEIGHT SCREEN ====================
@Composable
fun HeightScreen(
    isCm: Boolean,
    onUnitChange: (Boolean) -> Unit,
    feet: Int,
    inches: Int,
    cm: Int,
    onFeetChange: (Int) -> Unit,
    onInchesChange: (Int) -> Unit,
    onCmChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "How tall are you?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Toggle cm/ft
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF11141E))
                    .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Row {
                    Button(
                        onClick = { onUnitChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCm) Color(0xFF00C3FF) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("cm", color = if (isCm) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUnitChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isCm) Color(0xFF00C3FF) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("ft", color = if (!isCm) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isCm) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$cm cm",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00C3FF)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(
                            onClick = { if (cm > 100) onCmChange(cm - 1) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF11141E), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                        }
                        IconButton(
                            onClick = { if (cm < 250) onCmChange(cm + 1) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF11141E), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$feet' $inches\"",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00C3FF)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Feet", color = Color(0xFF64748B), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (feet > 3) onFeetChange(feet - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                                }
                                Text("$feet", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { if (feet < 8) onFeetChange(feet + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Inches", color = Color(0xFF64748B), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (inches > 0) onInchesChange(inches - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                                }
                                Text("$inches", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { if (inches < 11) onInchesChange(inches + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== WEIGHT SCREEN ====================
@Composable
fun WeightScreen(
    isLbs: Boolean,
    onUnitChange: (Boolean) -> Unit,
    weightLbs: Int,
    weightKg: Int,
    onLbsChange: (Int) -> Unit,
    onKgChange: (Int) -> Unit,
    title: String,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF11141E))
                    .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Row {
                    Button(
                        onClick = { onUnitChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isLbs) Color(0xFF00C3FF) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("kg", color = if (!isLbs) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onUnitChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLbs) Color(0xFF00C3FF) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("lbs", color = if (isLbs) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val displayVal = if (isLbs) "$weightLbs lbs" else "$weightKg kg"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayVal,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00C3FF)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(
                        onClick = {
                            if (isLbs) {
                                if (weightLbs > 50) onLbsChange(weightLbs - 2)
                            } else {
                                if (weightKg > 20) onKgChange(weightKg - 1)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF11141E), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            if (isLbs) {
                                if (weightLbs < 600) onLbsChange(weightLbs + 2)
                            } else {
                                if (weightKg < 300) onKgChange(weightKg + 1)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF11141E), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== HEALTH ISSUES ====================
@Composable
fun HealthIssuesScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Any health issues?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val items = listOf("None", "Knee", "Hip Joints", "Back or Hernia", "Arms and Shoulders", "Cant Do Jumps")

        items.forEach { issue ->
            val isSelected = selected == issue
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(issue) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                            .border(BorderStroke(1.5.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF64748B)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(text = issue, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==================== EQUIPMENT ====================
@Composable
fun EquipmentScreen(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What equipment do you have access to?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val items = listOf("None (Bodyweight)", "Full gym", "Barbells", "Dumbbells", "Kettlebells", "Machines")

        items.forEach { equip ->
            val isSelected = selected == equip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1C2230)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(equip) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                            .border(BorderStroke(1.5.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF64748B)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(text = equip, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==================== FREQUENCY SCREEN ====================
@Composable
fun WorkoutFrequencyScreen(
    frequency: Int,
    onFrequencyChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "How often would you like to work out?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${frequency}x",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00C3FF)
                )
                Text(
                    text = "$frequency workouts a week",
                    color = Color(0xFF64748B),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", color = Color(0xFF64748B), fontSize = 14.sp)

                    Slider(
                        value = frequency.toFloat(),
                        onValueChange = { onFrequencyChange(it.toInt()) },
                        valueRange = 1f..7f,
                        steps = 5,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00C3FF),
                            thumbColor = Color(0xFF00C3FF)
                        )
                    )

                    Text("More", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== WORKOUT DAYS SCREEN ====================
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WorkoutDaysScreen(
    selectedDays: List<String>,
    frequency: Int,
    smartReminders: Boolean,
    onSmartRemindersChange: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Set your workout days",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Select $frequency days matching your weekly workout goal.",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Staggered Days Layout
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            days.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF00C3FF).copy(alpha = 0.2f) else Color(0xFF11141E))
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF00C3FF) else Color(0xFF1C2230)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                (selectedDays as MutableList).remove(day)
                            } else {
                                if (selectedDays.size < frequency) {
                                    (selectedDays as MutableList).add(day)
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) Color(0xFF00C3FF) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Reminders section
        Divider(color = Color(0xFF1C2230), modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Reminder", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Helps to achieve a goal", color = Color(0xFF64748B), fontSize = 12.sp)
            }
            Switch(
                checked = smartReminders,
                onCheckedChange = onSmartRemindersChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00C3FF),
                    checkedTrackColor = Color(0xFF00C3FF).copy(alpha = 0.4f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF00C3FF))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Smart Reminders", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "90% of our users who use smart reminders reach their goals faster.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== CALIBRATION / PROGRESS LOADING SCREEN ====================
@Composable
fun CalibrationScreen(onFinished: () -> Unit) {
    var pct by remember { mutableStateOf(1) }
    var currentSubText by remember { mutableStateOf("Calculating your ability...") }

    LaunchedEffect(Unit) {
        val statuses = listOf(
            "Calculating your ability...",
            "Assessing your potential...",
            "Evaluating your rank...",
            "Generating your workout plan..."
        )
        for (i in 1..100) {
            delay(35)
            pct = i
            when (i) {
                25 -> currentSubText = statuses[1]
                55 -> currentSubText = statuses[2]
                85 -> currentSubText = statuses[3]
            }
        }
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "$pct%",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "We're creating a personal plan for you",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "[$currentSubText]",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
            )

            // Dynamic Progress ticks
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val list = listOf(
                        Pair("Physical Attributes", 20),
                        Pair("Fitness Level", 40),
                        Pair("Power Analysis", 60),
                        Pair("Rank Calibration", 80),
                        Pair("Workout Generation", 100)
                    )

                    list.forEach { (title, limit) ->
                        val isChecked = pct >= limit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, color = if (isChecked) Color.White else Color(0xFF64748B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (isChecked) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(BorderStroke(1.5.dp, Color(0xFF1C2230)), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== ANALYSIS SCREEN ====================
@Composable
fun AnalysisCompleteScreen(
    age: Int,
    gender: String,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Analysis complete", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // You bar
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(t_16 = 16.dp, t_4 = 4.dp))
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("72%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Average bar
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(t_16 = 16.dp, t_4 = 4.dp))
                            .background(Color(0xFF1C2230)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("41%", color = Color(0xFF64748B), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Average", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Based on our data, you're wasting 31% more potential",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "than the average $age year old ${gender.lowercase()}.",
                color = Color(0xFFEF4444),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🧍", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "You're only using 28% of your physical capacity. Most people your age unlock at least 60%.",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

// Helper extension for Rounded Corner Clip compatibility
private fun RoundedCornerShape(t_16: androidx.compose.ui.unit.Dp, t_4: androidx.compose.ui.unit.Dp) = RoundedCornerShape(
    topStart = t_16,
    topEnd = t_16,
    bottomStart = t_4,
    bottomEnd = t_4
)

// ==================== WARNING CAROUSEL SCREEN (RED WARNING SLIDES) ====================
@Composable
fun WarningCarouselScreen(onFinished: () -> Unit) {
    var activePage by remember { mutableStateOf(0) }

    val slides = listOf(
        Triple("Lost Potential", "Your ideal physique and confidence await behind inaction.", "🕵️"),
        Triple("Chronic Fatigue", "Your untapped energy potential fades with each inactive day.", "🧠"),
        Triple("Heart Health Risk", "Cardiovascular disease silently develops in sedentary lifestyles.", "❤️"),
        Triple("Mental Decline", "Depression and anxiety thrive in physical stagnation.", "🎈"),
        Triple("Time for Change", "Every journey begins with a single step. Take yours now.", "⏰")
    )

    val currentSlide = slides[activePage]
    val isLast = activePage == slides.size - 1
    val slideBg = if (isLast) Color(0xFF0E1E38) else Color(0xFFEF4444) // last slide blue/dark, others red

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(slideBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentSlide.third,
                fontSize = 90.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = currentSlide.first,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = currentSlide.second,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 48.dp),
                lineHeight = 24.sp
            )

            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                slides.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == activePage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (idx == activePage) Color.White else Color.White.copy(alpha = 0.4f))
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) {
                        onFinished()
                    } else {
                        activePage++
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isLast) "Let's Get Started" else "Next",
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ==================== ARISE STATS SCREEN ====================
@Composable
fun AriseStatsScreen(onContinue: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Your Arise Stats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Based on your answers, this is your current Arise stats, which reflects your lifestyle and training habits.",
            color = Color(0xFF64748B),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 stats
        val statsList = listOf(
            Triple("Strength", "STR", 12),
            Triple("Vitality", "VIT", 12),
            Triple("Agility", "AGI", 14),
            Triple("Recovery", "REC", 12)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            statsList.forEach { (name, label, value) ->
                val barProgress by animateFloatAsState(
                    targetValue = if (animationStarted) value / 100f else 0.05f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "barProg"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$value", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF1C2230))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Next", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== POTENTIAL STATS SCREEN ====================
@Composable
fun PotentialStatsScreen(onContinue: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Your Potential Stats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Based on your information, we believe you could improve your stats in 3 months by completing a customized workout program.",
            color = Color(0xFF64748B),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 potential stats
        val statsList = listOf(
            Triple("Strength", "STR", 85),
            Triple("Vitality", "VIT", 80),
            Triple("Agility", "AGI", 78),
            Triple("Recovery", "REC", 82)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            statsList.forEach { (name, label, value) ->
                val barProgress by animateFloatAsState(
                    targetValue = if (animationStarted) value / 100f else 0.10f,
                    animationSpec = tween(1400, easing = FastOutSlowInEasing),
                    label = "potentialBar"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$value", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF1C2230))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF10B981))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Next", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== 90 DAYS CHART SCREEN ====================
@Composable
fun NinetyDaysScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Give yourself just 90 days",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Line graph Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF11141E), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // grid lines
                drawLine(Color(0xFF1C2230), Offset(0f, h * 0.8f), Offset(w, h * 0.8f))
                drawLine(Color(0xFF1C2230), Offset(0f, h * 0.5f), Offset(w, h * 0.5f))
                drawLine(Color(0xFF1C2230), Offset(0f, h * 0.2f), Offset(w, h * 0.2f))

                // Red line "without Arise"
                val pathWithout = Path().apply {
                    moveTo(0f, h * 0.7f)
                    quadraticTo(w * 0.5f, h * 0.65f, w, h * 0.8f)
                }
                drawPath(pathWithout, Color(0xFFEF4444), style = Stroke(width = 3.dp.toPx()))

                // Green line "with Arise"
                val pathWith = Path().apply {
                    moveTo(0f, h * 0.7f)
                    cubicTo(w * 0.3f, h * 0.5f, w * 0.7f, h * 0.2f, w, h * 0.1f)
                }
                drawPath(pathWith, Color(0xFF10B981), style = Stroke(width = 4.dp.toPx()))

                // Labels
                drawCircle(Color(0xFF10B981), radius = 6.dp.toPx(), center = Offset(w, h * 0.1f))
            }
            
            // Labels positioned absolutely
            Text("with Arise", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp))
            Text("without Arise", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 28.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Checklists
        val items = listOf(
            Pair("Your strength will increase significantly", "Progressive overload training will help you lift heavier weights and build lean muscle."),
            Pair("You'll have more energy", "Better conditioning means you won't get tired during daily activities."),
            Pair("Your confidence will drastically improve", "As you transform your body, you'll feel empowered and unstoppable.")
        )

        items.forEach { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(desc, color = Color(0xFF64748B), fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Next", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

// ==================== HEAVY QUESTIONS CINEMATIC SCREEN ====================
@Composable
fun HeavyQuestionsScreen(onFinished: () -> Unit) {
    var subStep by remember { mutableStateOf(0) }

    val heavyQuestions = listOf(
        "Is there a version of yourself you dream of becoming?",
        "Are you willing to challenge your own limits to grow?",
        "Do you believe that small changes can lead to big transformations?",
        "Would you invest in yourself if you knew it could change your life?",
        "Are you ready to lock in?\n\nWARNING - You've seen the path ahead. Choose to walk it, or remain where you are."
    )

    val currentText = heavyQuestions[subStep]
    val isLast = subStep == heavyQuestions.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cool mystical overlay graphics
            Text(
                text = "⚡",
                fontSize = 44.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AnimatedContent(
                targetState = currentText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "textAnim"
            ) { targetText ->
                Text(
                    text = targetText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            if (!isLast) {
                Button(
                    onClick = { subStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp)
                ) {
                    Text("Yes", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else {
                // LOCK IN pulsating fingerprint button!
                val infiniteTransition = rememberInfiniteTransition(label = "fingerprintPulsate")
                val pulsateScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutLinearInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                IconButton(
                    onClick = onFinished,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulsateScale)
                        .background(Color(0xFF00C3FF).copy(alpha = 0.15f), CircleShape)
                        .border(BorderStroke(2.dp, Color(0xFF00C3FF)), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "LOCK IN",
                        tint = Color(0xFF00C3FF),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "LOCK IN",
                    color = Color(0xFF00C3FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    style = TextStyle(shadow = Shadow(color = Color(0xFF00C3FF), blurRadius = 8f))
                )
            }
        }
    }
}

// ==================== LUNAR ECLIPSE GREETING SLIDES ====================
@Composable
fun LunarEclipseGreetingScreen(onFinished: () -> Unit) {
    var subStep by remember { mutableStateOf(0) }

    val greetings = listOf(
        "Welcome to Arise!",
        "Your determination to level up in real life has been acknowledged.",
        "Your journey to become the best version of yourself starts here.",
        "Time to unlock your hidden potential.",
        "Let's do a final review and begin your dream physique journey."
    )

    LaunchedEffect(Unit) {
        for (i in greetings.indices) {
            subStep = i
            delay(2000)
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Crescent Moon Canvas Drawing (Eclipse style)
            Canvas(modifier = Modifier.size(120.dp)) {
                val r = size.width / 2
                drawCircle(
                    color = Color(0xFF00C3FF).copy(alpha = 0.2f),
                    radius = r,
                    center = Offset(r, r)
                )
                drawCircle(
                    color = Color(0xFF07090E),
                    radius = r * 0.9f,
                    center = Offset(r * 1.3f, r * 0.8f) // Offset to make crescent
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = greetings[subStep],
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "eclipseText"
            ) { targetText ->
                Text(
                    text = targetText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ==================== ACCOUNT SYNC MOCK SCREEN ====================
@Composable
fun AccountSyncMockScreen(onFinished: () -> Unit) {
    var syncing by remember { mutableStateOf(false) }

    LaunchedEffect(syncing) {
        if (syncing) {
            delay(1500)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!syncing) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign in to save your data",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Or skip for now",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // Hologram illustration mockup
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color(0xFF11141E), RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤖", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SAVING DATA...", color = Color(0xFF00C3FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { syncing = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { onFinished() }) {
                    Text("Skip for now", color = Color(0xFF00C3FF), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Loading Spinner Dialog
            Dialog(onDismissRequest = {}) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(200.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00C3FF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Syncing your data...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== FINAL PLAN PREVIEW & KICKSTART SCREEN ====================
@Composable
fun FinalPlanPreviewScreen(
    state: OnboardingState,
    onKickstart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Custom Workout Plan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0xFF00C3FF).copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THE SYSTEM",
                    color = Color(0xFF00C3FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    style = TextStyle(shadow = Shadow(color = Color(0xFF00C3FF), blurRadius = 4f))
                )
                Text(
                    text = "A customized 90-day plan based on your answers has been engineered. Achieve a complete physical turnaround by: Oct 6, 2026.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 20.sp
                )
            }
        }

        // Suggestions array list
        Text("HOW TO REACH YOUR GOAL", color = Color(0xFF00C3FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)

        val workoutDrills = when (state.goal) {
            "Build Muscle" -> listOf(
                Triple("Upper Body Hypertrophy", "Focuses on building power in chest, back, and arms using bodyweight or equipment.", "💪 STR"),
                Triple("Lower Body Power", "Explosive squats and calf drills to strengthen legs.", "🏋️ STR"),
                Triple("Core Sculpting", "Tightening the core abdominal wall daily.", "🧘 DIS")
            )
            "Lose Weight" -> listOf(
                Triple("High Intensity Cardio Drill", "Elevates heart rate rapidly to shred visceral fat.", "🏃 VIT"),
                Triple("Full Body Calisthenics", "Total muscle engagement using rapid physical loops.", "⚡ STR"),
                Triple("Core Crusher", "High performance endurance intervals.", "🔥 DIS")
            )
            else -> listOf(
                Triple("Balanced Toning", "Sculpting muscular definitions uniformly.", "💪 STR"),
                Triple("Cardio Flow Session", "Aerobic stamina flow to build energy storage.", "🏃 VIT"),
                Triple("Abdominal Tightening", "Targeted core workouts for postural control.", "🧘 DIS")
            )
        }

        workoutDrills.forEach { (title, desc, tag) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF1C2230)), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1C2230), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(tag, color = Color(0xFF00C3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, color = Color(0xFF64748B), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onKickstart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C3FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("kickstart_journey_button")
        ) {
            Text(
                "Kickstart My Journey",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}
