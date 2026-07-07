package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.util.*

@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val wellness by viewModel.wellness.collectAsStateWithLifecycle()

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }

    // Dialog state for logging wellness
    var wellnessDialogDate by remember { mutableStateOf<String?>(null) }

    // Dialog state for adding/editing habit
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    // "Professional Polish" Material You light palette
    val customBackground = Color(0xFFFEF7FF)
    val cardBackground = Color(0xFFF3EDF7)
    val accentBlue = Color(0xFF6750A4)
    val accentTeal = Color(0xFF6750A4)
    val textWhite = Color(0xFF1D1B20)
    val textMuted = Color(0xFF49454F)
    val gridBorderColor = Color(0xFFCAC4D0)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = customBackground
    ) {
        BoxWithConstraints {
            val isExpanded = maxWidth > 720.dp

            Scaffold(
                topBar = {
                    HeaderSection(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onMonthYearChange = { month, year ->
                            viewModel.selectMonthAndYear(month, year)
                        },
                        onAddHabitClick = {
                            habitToEdit = null
                            showAddHabitDialog = true
                        }
                    )
                },
                bottomBar = {
                    if (!isExpanded) {
                        NavigationBar(
                            containerColor = cardBackground,
                            contentColor = textWhite,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.GridOn, contentDescription = "Grid") },
                                label = { Text("Grid Tracker") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accentBlue,
                                    selectedTextColor = accentBlue,
                                    unselectedIconColor = textMuted,
                                    unselectedTextColor = textMuted,
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                                label = { Text("Stats") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accentBlue,
                                    selectedTextColor = accentBlue,
                                    unselectedIconColor = textMuted,
                                    unselectedTextColor = textMuted,
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Manage") },
                                label = { Text("Manage") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accentBlue,
                                    selectedTextColor = accentBlue,
                                    unselectedIconColor = textMuted,
                                    unselectedTextColor = textMuted,
                                    indicatorColor = Color(0xFFEADDFF)
                                )
                            )
                        }
                    }
                },
                containerColor = customBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpanded) {
                        // Responsive Landscape / Tablet Split Layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column: Grid Tracker & Wellness (60% width)
                            Column(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = "Monthly Grid Tracker",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textWhite,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBackground)
                                        .border(1.dp, gridBorderColor, RoundedCornerShape(12.dp))
                                ) {
                                    GridTrackerView(
                                        habits = habits,
                                        completions = completions,
                                        wellness = wellness,
                                        month = selectedMonth,
                                        year = selectedYear,
                                        onToggleCompletion = { habitId, date ->
                                            viewModel.toggleCompletion(habitId, date, !completions.any { it.habitId == habitId && it.dateString == date })
                                        },
                                        onWellnessClick = { date ->
                                            wellnessDialogDate = date
                                        },
                                        accentBlue = accentBlue,
                                        accentTeal = accentTeal,
                                        textWhite = textWhite,
                                        textMuted = textMuted,
                                        gridBorderColor = gridBorderColor
                                    )
                                }
                            }

                            // Right Column: Stats & Leaders (40% width)
                            Column(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatsAndAnalysisView(
                                    habits = habits,
                                    completions = completions,
                                    wellness = wellness,
                                    month = selectedMonth,
                                    year = selectedYear,
                                    accentBlue = accentBlue,
                                    accentTeal = accentTeal,
                                    cardBackground = cardBackground,
                                    textWhite = textWhite,
                                    textMuted = textMuted,
                                    onEditHabit = { habit ->
                                        habitToEdit = habit
                                        showAddHabitDialog = true
                                    },
                                    onDeleteHabit = { habit ->
                                        viewModel.deleteHabit(habit)
                                    }
                                )
                            }
                        }
                    } else {
                        // Portrait / Compact Navigation-driven views
                        Crossfade(targetState = activeTab, label = "tabTransition") { tab ->
                            when (tab) {
                                0 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp)
                                    ) {
                                        // Let's draw the custom Daily Progress mini-chart above the grid!
                                        DailyProgressChart(
                                            habits = habits,
                                            completions = completions,
                                            month = selectedMonth,
                                            year = selectedYear,
                                            accentTeal = accentTeal,
                                            cardBackground = cardBackground,
                                            textWhite = textWhite,
                                            textMuted = textMuted
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(cardBackground)
                                                .border(1.dp, gridBorderColor, RoundedCornerShape(12.dp))
                                        ) {
                                            GridTrackerView(
                                                habits = habits,
                                                completions = completions,
                                                wellness = wellness,
                                                month = selectedMonth,
                                                year = selectedYear,
                                                onToggleCompletion = { habitId, date ->
                                                    viewModel.toggleCompletion(habitId, date, !completions.any { it.habitId == habitId && it.dateString == date })
                                                },
                                                onWellnessClick = { date ->
                                                    wellnessDialogDate = date
                                                },
                                                accentBlue = accentBlue,
                                                accentTeal = accentTeal,
                                                textWhite = textWhite,
                                                textMuted = textMuted,
                                                gridBorderColor = gridBorderColor
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        StatsAndAnalysisView(
                                            habits = habits,
                                            completions = completions,
                                            wellness = wellness,
                                            month = selectedMonth,
                                            year = selectedYear,
                                            accentBlue = accentBlue,
                                            accentTeal = accentTeal,
                                            cardBackground = cardBackground,
                                            textWhite = textWhite,
                                            textMuted = textMuted,
                                            onEditHabit = null, // edit from manage tab
                                            onDeleteHabit = null
                                        )
                                    }
                                }
                                2 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Manage Habits",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textWhite,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )

                                        Button(
                                            onClick = {
                                                habitToEdit = null
                                                showAddHabitDialog = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("add_habit_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                            Text("Add New Habit", fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            items(habits) { habit ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = BorderStroke(1.dp, gridBorderColor)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .background(Color(0xFFEADDFF), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(habit.emoji, fontSize = 20.sp)
                                                            }
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Text(
                                                                text = habit.name,
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = textWhite,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }

                                                        Row {
                                                            IconButton(
                                                                onClick = {
                                                                    habitToEdit = habit
                                                                    showAddHabitDialog = true
                                                                }
                                                            ) {
                                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentBlue)
                                                            }
                                                            IconButton(
                                                                onClick = { viewModel.deleteHabit(habit) }
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBA1A1A))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Wellness logger Dialog
    wellnessDialogDate?.let { dateStr ->
        val existingLog = wellness.find { it.dateString == dateStr }
        WellnessLogDialog(
            dateString = dateStr,
            initialMood = existingLog?.mood ?: 0,
            initialSleep = existingLog?.sleepHours ?: 0f,
            onDismiss = { wellnessDialogDate = null },
            onSave = { mood, sleep ->
                viewModel.updateWellness(dateStr, mood, sleep)
                wellnessDialogDate = null
            },
            accentTeal = accentTeal,
            cardBackground = cardBackground,
            textWhite = textWhite,
            textMuted = textMuted
        )
    }

    // Add / Edit Habit Dialog
    if (showAddHabitDialog) {
        AddEditHabitDialog(
            habit = habitToEdit,
            onDismiss = { showAddHabitDialog = false },
            onSave = { name, emoji ->
                if (habitToEdit == null) {
                    viewModel.addHabit(name, emoji)
                } else {
                    viewModel.updateHabit(habitToEdit!!.copy(name = name, emoji = emoji))
                }
                showAddHabitDialog = false
            },
            accentBlue = accentBlue,
            cardBackground = cardBackground,
            textWhite = textWhite
        )
    }
}

@Composable
fun HeaderSection(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearChange: (Int, Int) -> Unit,
    onAddHabitClick: () -> Unit
) {
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val years = (2024..2030).toList()

    val headerBg = Color(0xFFFEF7FF)
    val textWhite = Color(0xFF1D1B20)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Habit Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textWhite
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Month selector
                Box {
                    TextButton(onClick = { expandedMonth = true }) {
                        Text(months[selectedMonth - 1], color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6750A4))
                    }
                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false },
                        modifier = Modifier.background(Color(0xFFF3EDF7))
                    ) {
                        months.forEachIndexed { index, m ->
                            DropdownMenuItem(
                                text = { Text(m, color = textWhite) },
                                onClick = {
                                    onMonthYearChange(index + 1, selectedYear)
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                // Year selector
                Box {
                    TextButton(onClick = { expandedYear = true }) {
                        Text(selectedYear.toString(), color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6750A4))
                    }
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false },
                        modifier = Modifier.background(Color(0xFFF3EDF7))
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString(), color = textWhite) },
                                onClick = {
                                    onMonthYearChange(selectedMonth, y)
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onAddHabitClick,
            modifier = Modifier
                .background(Color(0xFFEADDFF), CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = Color(0xFF21005D))
        }
    }
}

@Composable
fun GridTrackerView(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    wellness: List<DailyWellness>,
    month: Int,
    year: Int,
    onToggleCompletion: (Int, String) -> Unit,
    onWellnessClick: (String) -> Unit,
    accentBlue: Color,
    accentTeal: Color,
    textWhite: Color,
    textMuted: Color,
    gridBorderColor: Color
) {
    val daysInMonth = getDaysInMonth(month, year)
    val daysList = (1..daysInMonth).toList()

    val calendar = Calendar.getInstance()
    val todayDay = calendar.get(Calendar.DATE)
    val todayMonth = calendar.get(Calendar.MONTH) + 1
    val todayYear = calendar.get(Calendar.YEAR)

    if (habits.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Habits yet. Tap the '+' button above to add some!", color = textMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        return
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sticky left column (Habit names)
        Column(
            modifier = Modifier
                .width(150.dp)
                .background(Color(0xFFF3EDF7))
                .border(BorderStroke(1.dp, gridBorderColor))
        ) {
            // Header corner block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFFEADDFF))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("My Habits", fontWeight = FontWeight.Bold, color = Color(0xFF21005D), fontSize = 14.sp)
            }

            Divider(color = gridBorderColor, thickness = 1.dp)

            // Habit Names rows
            habits.forEach { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(habit.emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = habit.name,
                        color = textWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Divider(color = gridBorderColor, thickness = 1.dp)
            }

            // Wellness names
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Mood 🎭", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Divider(color = gridBorderColor, thickness = 1.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Sleep 🛌", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Horizontally scrollable checklist grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            Row {
                daysList.forEach { d ->
                    val dayOfWeek = getDayOfWeekAbbreviation(d, month, year)
                    val isToday = d == todayDay && month == todayMonth && year == todayYear
                    val dateString = String.format("%04d-%02d-%02d", year, month, d)

                    Column(
                        modifier = Modifier
                            .width(46.dp)
                            .background(if (isToday) Color(0xFFEADDFF).copy(alpha = 0.4f) else Color.Transparent)
                            .border(BorderStroke(1.dp, gridBorderColor)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header cell
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(if (isToday) Color(0xFFEADDFF) else Color(0xFFF3EDF7)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = d.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) Color(0xFF6750A4) else textWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                text = dayOfWeek,
                                color = if (isToday) Color(0xFF6750A4) else textMuted,
                                fontSize = 11.sp
                            )
                        }

                        Divider(color = gridBorderColor, thickness = 1.dp)

                        // Checkboxes for each habit
                        habits.forEach { habit ->
                            val isCompleted = completions.any { it.habitId == habit.id && it.dateString == dateString }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCompleted) accentTeal else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isCompleted) accentTeal else Color(0xFFCAC4D0),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onToggleCompletion(habit.id, dateString) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Divider(color = gridBorderColor, thickness = 1.dp)
                        }

                        // Mood Cell
                        val moodLog = wellness.find { it.dateString == dateString }
                        val moodVal = moodLog?.mood ?: 0
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color.Transparent)
                                .clickable { onWellnessClick(dateString) },
                            contentAlignment = Alignment.Center
                        ) {
                            when (moodVal) {
                                1 -> Text("😡", fontSize = 16.sp)
                                2 -> Text("😔", fontSize = 16.sp)
                                3 -> Text("😐", fontSize = 16.sp)
                                4 -> Text("🙂", fontSize = 16.sp)
                                5 -> Text("🤩", fontSize = 16.sp)
                                else -> Text("➖", color = textMuted, fontSize = 14.sp)
                            }
                        }
                        Divider(color = gridBorderColor, thickness = 1.dp)

                        // Sleep Cell
                        val sleepHours = moodLog?.sleepHours ?: 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color.Transparent)
                                .clickable { onWellnessClick(dateString) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (sleepHours > 0) {
                                Text(
                                    text = String.format("%.1f", sleepHours),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                            } else {
                                Text("➖", color = textMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyProgressChart(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    month: Int,
    year: Int,
    accentTeal: Color,
    cardBackground: Color,
    textWhite: Color,
    textMuted: Color
) {
    val daysInMonth = getDaysInMonth(month, year)
    val daysList = (1..daysInMonth).toList()

    val dailyProgressList = daysList.map { d ->
        val dateString = String.format("%04d-%02d-%02d", year, month, d)
        val countForDay = completions.count { it.dateString == dateString }
        val percentage = if (habits.isNotEmpty()) {
            (countForDay.toFloat() / habits.size * 100).toInt()
        } else {
            0
        }
        percentage
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Daily Completion Progress",
                fontWeight = FontWeight.Bold,
                color = textWhite,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally scrolling bar chart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dailyProgressList.forEachIndexed { idx, pct ->
                    val dayNum = idx + 1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(22.dp)
                    ) {
                        // Percentage text on top of bar if > 0
                        if (pct > 0) {
                            Text(
                                text = "$pct%",
                                fontSize = 8.sp,
                                color = accentTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        // The actual bar
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(60.dp * (pct / 100f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(accentTeal, accentTeal.copy(alpha = 0.4f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum.toString(),
                            fontSize = 9.sp,
                            color = textMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsAndAnalysisView(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    wellness: List<DailyWellness>,
    month: Int,
    year: Int,
    accentBlue: Color,
    accentTeal: Color,
    cardBackground: Color,
    textWhite: Color,
    textMuted: Color,
    onEditHabit: ((Habit) -> Unit)?,
    onDeleteHabit: ((Habit) -> Unit)?
) {
    val gridBorderColor = Color(0xFFCAC4D0)
    val daysInMonth = getDaysInMonth(month, year)
    val totalPossibilities = habits.size * daysInMonth

    // Completions filtered for the selected month/year
    val monthPrefix = String.format("%04d-%02d-", year, month)
    val filteredCompletions = completions.filter { it.dateString.startsWith(monthPrefix) }
    val completedCount = filteredCompletions.size
    val leftCount = maxOf(0, totalPossibilities - completedCount)
    val successRate = if (totalPossibilities > 0) {
        (completedCount.toFloat() / totalPossibilities * 100).toInt()
    } else {
        0
    }

    // Sleep analytics
    val monthlyWellness = wellness.filter { it.dateString.startsWith(monthPrefix) }
    val sleepDays = monthlyWellness.filter { it.sleepHours > 0f }
    val avgSleep = if (sleepDays.isNotEmpty()) {
        sleepDays.map { it.sleepHours }.average().toFloat()
    } else {
        0f
    }

    // Mood analytics
    val moodDays = monthlyWellness.filter { it.mood > 0 }
    val avgMood = if (moodDays.isNotEmpty()) {
        moodDays.map { it.mood }.average().toFloat()
    } else {
        0f
    }

    // 1. Overall Circle Progress Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Overall Statistics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textWhite)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive circle progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { successRate / 100f },
                        modifier = Modifier.size(100.dp),
                        color = accentTeal,
                        strokeWidth = 10.dp,
                        trackColor = Color(0xFFEADDFF),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$successRate%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textWhite)
                        Text("Success", fontSize = 10.sp, color = textMuted)
                    }
                }

                // Grid stats details
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    StatRow("Goal", totalPossibilities.toString(), Color(0xFFFBBF24))
                    StatRow("Completed", completedCount.toString(), accentTeal)
                    StatRow("Left", leftCount.toString(), Color(0xFFEF4444))
                }
            }
        }
    }

    // 2. Wellness Quick Insights Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Wellness Analytics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textWhite)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sleep insight card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (avgSleep > 0) String.format("%.1f hrs", avgSleep) else "No logs",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            fontSize = 15.sp
                        )
                        Text("Avg Sleep", fontSize = 11.sp, color = textMuted)
                    }
                }

                // Mood insight card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EmojiEmotions, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        val moodEmoji = when (avgMood.toInt()) {
                            1 -> "😡"
                            2 -> "😔"
                            3 -> "😐"
                            4 -> "🙂"
                            5 -> "🤩"
                            else -> "➖"
                        }
                        Text(
                            text = if (avgMood > 0) String.format("%.1f %s", avgMood, moodEmoji) else "No logs",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            fontSize = 15.sp
                        )
                        Text("Avg Mood", fontSize = 11.sp, color = textMuted)
                    }
                }
            }
        }
    }

    // 3. Habit-by-habit Analysis Grid
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Habit Progress Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textWhite)
            Spacer(modifier = Modifier.height(12.dp))

            // Habit summary stats list
            habits.forEach { habit ->
                val habitComps = filteredCompletions.count { it.habitId == habit.id }
                val progressPct = if (daysInMonth > 0) (habitComps.toFloat() / daysInMonth * 100).toInt() else 0
                val remaining = maxOf(0, daysInMonth - habitComps)

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = habit.name,
                                color = textWhite,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                        }
                        Text(
                            text = "Goal: $daysInMonth | Done: $habitComps | Left: $remaining",
                            fontSize = 11.sp,
                            color = textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentBlue,
                            trackColor = Color(0xFFEADDFF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$progressPct%", color = textWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Divider(color = gridBorderColor, thickness = 0.5.dp)
            }
        }
    }

    // 4. TOP 10 HABITS Leaderboard
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top Habits Leaderboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textWhite)
            Spacer(modifier = Modifier.height(12.dp))

            val rankedHabits = habits.map { habit ->
                val count = filteredCompletions.count { it.habitId == habit.id }
                val pct = if (daysInMonth > 0) (count.toFloat() / daysInMonth * 100).toInt() else 0
                habit to pct
            }.sortedByDescending { it.second }.take(10)

            if (rankedHabits.isEmpty()) {
                Text("No data to calculate rankings.", color = textMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                rankedHabits.forEachIndexed { index, (habit, pct) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = when (index) {
                                    0 -> Color(0xFFFBBF24) // Gold
                                    1 -> Color(0xFF9CA3AF) // Silver
                                    2 -> Color(0xFFD97706) // Bronze
                                    else -> textMuted
                                },
                                modifier = Modifier.width(24.dp)
                            )
                            Text(habit.emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(habit.name, color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("$pct% Score", color = accentTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Divider(color = gridBorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, tint: Color) {
    Row(
        modifier = Modifier.width(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(tint, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color(0xFF49454F), fontSize = 13.sp)
        }
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp)
    }
}

@Composable
fun WellnessLogDialog(
    dateString: String,
    initialMood: Int,
    initialSleep: Float,
    onDismiss: () -> Unit,
    onSave: (Int, Float) -> Unit,
    accentTeal: Color,
    cardBackground: Color,
    textWhite: Color,
    textMuted: Color
) {
    var mood by remember { mutableStateOf(initialMood) }
    var sleepHours by remember { mutableStateOf(initialSleep) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Wellness",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textWhite
                )
                Text(
                    text = dateString,
                    fontSize = 12.sp,
                    color = textMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mood selector
                Text("How was your mood?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val moodOptions = listOf(
                        1 to "😡",
                        2 to "😔",
                        3 to "😐",
                        4 to "🙂",
                        5 to "🤩"
                    )
                    moodOptions.forEach { (valNum, emoji) ->
                        val isSelected = mood == valNum
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFEADDFF) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) accentTeal else Color(0xFFCAC4D0),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { mood = valNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sleep hours selector
                Text("Hours of sleep: ${String.format("%.1f", sleepHours)}h", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textWhite)
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    valueRange = 0f..12f,
                    steps = 23, // increments of 0.5 hours
                    colors = SliderDefaults.colors(
                        activeTrackColor = accentTeal,
                        thumbColor = accentTeal
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = textMuted)
                    }
                    Button(
                        onClick = { onSave(mood, sleepHours) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditHabitDialog(
    habit: Habit?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    accentBlue: Color,
    cardBackground: Color,
    textWhite: Color
) {
    var name by remember { mutableStateOf(habit?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(habit?.emoji ?: "⏰") }

    val emojisList = listOf(
        "⏰", "💪", "📚", "🗓️", "💻", "🥗", "🚫", "📵", "📝", "❄️",
        "🧠", "🧘", "🚶", "💧", "🍳", "🛌", "🧹", "💡", "🎨", "🎵",
        "❤️", "🔥", "🚲", "🍵", "💵", "🌱", "🔑", "🛁", "🍎", "🏃"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (habit == null) "Add Custom Habit" else "Edit Habit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textWhite,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name", color = Color(0xFF49454F)) },
                    placeholder = { Text("e.g., Read for 30 mins", color = Color(0xFFCAC4D0)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textWhite,
                        unfocusedTextColor = textWhite,
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Pick an Icon/Emoji", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textWhite)
                Spacer(modifier = Modifier.height(8.dp))

                // Grid scrollable selection of emojis
                Box(modifier = Modifier.height(130.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        val chunked = emojisList.chunked(5)
                        chunked.forEach { rowEmojis ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowEmojis.forEach { emoji ->
                                    val isSelected = selectedEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFEADDFF) else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isSelected) accentBlue else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedEmoji = emoji },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 22.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF6750A4))
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedEmoji)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_habit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentBlue,
                            disabledContainerColor = Color(0xFFEADDFF).copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Calendar Date & Month calculation helpers
fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun getDayOfWeekAbbreviation(day: Int, month: Int, year: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day)
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Su"
        Calendar.MONDAY -> "Mo"
        Calendar.TUESDAY -> "Tu"
        Calendar.WEDNESDAY -> "We"
        Calendar.THURSDAY -> "Th"
        Calendar.FRIDAY -> "Fr"
        Calendar.SATURDAY -> "Sa"
        else -> ""
    }
}
