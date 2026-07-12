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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val wellness by viewModel.wellness.collectAsStateWithLifecycle()

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    // Dialog & overlay states
    var wellnessDialogDate by remember { mutableStateOf<String?>(null) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showGoogleBackupDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val googleAccount by viewModel.googleSignInAccount.collectAsStateWithLifecycle()
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    val isBackupLoading by viewModel.isBackupLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            viewModel.handleGoogleSignInResult(account, null, context)
            viewModel.backupToGoogleDrive(context)
        } catch (e: Exception) {
            viewModel.handleGoogleSignInResult(null, e, context)
        }
    }



    // Elegant colors (Blue & Slate theme)
    val customBackground = if (isDarkTheme) Color(0xFF07090E) else Color(0xFFF8FAFC)
    val cardBackground = if (isDarkTheme) Color(0xFF11141E) else Color(0xFFFFFFFF)
    val borderStrokeColor = if (isDarkTheme) Color(0xFF1C2230) else Color(0xFFE2E8F0)
    val neonBlue = if (isDarkTheme) Color(0xFF00C3FF) else Color(0xFF0284C7)
    val neonPurple = if (isDarkTheme) Color(0xFF8B5CF6) else Color(0xFF7C3AED)
    val neonGreen = if (isDarkTheme) Color(0xFF10B981) else Color(0xFF059669)
    val neonOrange = if (isDarkTheme) Color(0xFFFF9E00) else Color(0xFFD97706)
    val neonRed = if (isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
    val textWhite = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF475569)

    // Excel letters dictionary
    val columnLetters = remember {
        val list = mutableListOf<String>()
        for (c in 'A'..'Z') {
            list.add(c.toString())
        }
        for (c in 'A'..'Z') {
            list.add("A$c")
        }
        list
    }

    val monthsMap = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var showSettingsDropdown by remember { mutableStateOf(false) }

    val daysInMonth = getDaysInMonth(selectedMonth, selectedYear)
    val daysList = (1..daysInMonth).toList()

    // Horizontal and vertical spreadsheet scroll states
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Calculate real-time daily progress for chart and analysis
    val daysProgress = remember(completions, habits, selectedMonth, selectedYear) {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val activeHabitsCount = habits.size.coerceAtLeast(1)
        (1..daysInMonth).map { d ->
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, selectedYear)
            c.set(Calendar.MONTH, selectedMonth - 1)
            c.set(Calendar.DATE, d)
            val dateStr = format.format(c.time)
            val dayOfWeek = when (c.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Su"
                Calendar.MONDAY -> "Mo"
                Calendar.TUESDAY -> "Tu"
                Calendar.WEDNESDAY -> "We"
                Calendar.THURSDAY -> "Th"
                Calendar.FRIDAY -> "Fr"
                Calendar.SATURDAY -> "Sa"
                else -> ""
            }
            val completedCount = completions.count { it.dateString == dateStr }
            val progressPct = completedCount.toFloat() / activeHabitsCount
            Triple(d, dayOfWeek, progressPct)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = customBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. TOP CONTROL BAR (Month/Year & Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11141E))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Tracker OS",
                    color = textWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Month Selector
                    Box {
                        TextButton(
                            onClick = { showMonthDropdown = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = neonBlue),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val monthName = monthsMap.getOrNull(selectedMonth - 1) ?: "March"
                            Text(monthName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false },
                            modifier = Modifier.background(Color(0xFF11141E)).border(1.dp, borderStrokeColor)
                        ) {
                            monthsMap.forEachIndexed { idx, mName ->
                                DropdownMenuItem(
                                    text = { Text(mName, color = textWhite, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.selectMonthAndYear(idx + 1, selectedYear)
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Selector
                    Box {
                        TextButton(
                            onClick = { showYearDropdown = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = neonBlue),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("$selectedYear", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showYearDropdown,
                            onDismissRequest = { showYearDropdown = false },
                            modifier = Modifier.background(Color(0xFF11141E)).border(1.dp, borderStrokeColor)
                        ) {
                            listOf(2025, 2026, 2027, 2028).forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text("$yr", color = textWhite, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.selectMonthAndYear(selectedMonth, yr)
                                        showYearDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Settings Selector
                    Box {
                        IconButton(
                            onClick = { showSettingsDropdown = true },
                            modifier = Modifier
                                .background(neonBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = neonBlue, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showSettingsDropdown,
                            onDismissRequest = { showSettingsDropdown = false },
                            modifier = Modifier.background(Color(0xFF11141E)).border(1.dp, borderStrokeColor)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = neonBlue, modifier = Modifier.size(16.dp)) },
                                text = { Text("Add New Habit", color = textWhite, fontSize = 12.sp) },
                                onClick = {
                                    habitToEdit = null
                                    showAddHabitDialog = true
                                    showSettingsDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = neonGreen, modifier = Modifier.size(16.dp)) },
                                text = { Text("Reset to Default Habits", color = textWhite, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.resetHabitsToImageLayout()
                                    showSettingsDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = "Cloud", tint = if (googleAccount != null) neonGreen else neonBlue, modifier = Modifier.size(16.dp)) },
                                text = { Text(if (googleAccount != null) "Google Sync and Backup (Gmail Connected)" else "Google Sync & Backup", color = textWhite, fontSize = 12.sp) },
                                onClick = {
                                    showGoogleBackupDialog = true
                                    showSettingsDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.BrightnessMedium, contentDescription = "Theme", tint = neonOrange, modifier = Modifier.size(16.dp)) },
                                text = { Text("Toggle Theme", color = textWhite, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.toggleTheme()
                                    showSettingsDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. TAB SELECTION ROW
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF11141E),
                contentColor = neonBlue
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTabIndex == 0) neonBlue else textMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Habit Grid",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 0) textWhite else textMuted
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTabIndex == 1) neonBlue else textMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Stats & Insights",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 1) textWhite else textMuted
                            )
                        }
                    }
                )
            }

            // 3. TAB CONTENT AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTabIndex == 0) {
                    // TAB 0: HABIT GRID
                    GridTrackerView(
                        habits = habits,
                        completions = completions,
                        wellness = wellness,
                        month = selectedMonth,
                        year = selectedYear,
                        onToggleCompletion = { habitId, dateString ->
                            val exists = completions.any { it.habitId == habitId && it.dateString == dateString }
                            viewModel.toggleCompletion(habitId, dateString, !exists)
                        },
                        onWellnessClick = { dateStr ->
                            wellnessDialogDate = dateStr
                        },
                        onEditHabit = { habit ->
                            habitToEdit = habit
                            showAddHabitDialog = true
                        },
                        onDeleteHabit = { habit ->
                            viewModel.deleteHabit(habit)
                        },
                        onResetMatrix = {
                            viewModel.resetHabitsToImageLayout()
                        },
                        accentBlue = neonBlue,
                        accentTeal = neonGreen,
                        textWhite = textWhite,
                        textMuted = textMuted,
                        gridBorderColor = borderStrokeColor
                    )
                } else {
                    // TAB 1: STATS & INSIGHTS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // WIDGET 1: Daily Progress
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF000000))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Daily Progress",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFD8DBE0)) // Authentic Excel grey BG
                                            .padding(top = 16.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .height(100.dp)
                                                .padding(end = 4.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text("100%", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            Text("75%", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            Text("50%", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            Text("25%", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            Text("0%", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .padding(bottom = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                daysProgress.forEach { (_, _, progress) ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight(progress.coerceIn(0.02f, 1f))
                                                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                            .background(Color(0xFF0F172A)) // Solid dark black bars
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.5.dp)
                                                    .background(Color.Black)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(28.dp)
                                                    .padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                daysProgress.forEach { (_, dayOfWeek, _) ->
                                                    Box(
                                                        modifier = Modifier.weight(1f),
                                                        contentAlignment = Alignment.TopCenter
                                                    ) {
                                                        Text(
                                                            text = dayOfWeek,
                                                            fontSize = 7.sp,
                                                            color = Color.Black,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.graphicsLayer {
                                                                rotationZ = -45f
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // WIDGET 2: Overall Stats
                            val totalGoal = habits.size * daysInMonth
                            val totalCompleted = remember(completions, habits, selectedMonth, selectedYear) {
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                val validDates = (1..daysInMonth).map { d ->
                                    val c = Calendar.getInstance()
                                    c.set(Calendar.YEAR, selectedYear)
                                    c.set(Calendar.MONTH, selectedMonth - 1)
                                    c.set(Calendar.DATE, d)
                                    format.format(c.time)
                                }.toSet()
                                completions.count { validDates.contains(it.dateString) }
                            }
                            val totalLeft = (totalGoal - totalCompleted).coerceAtLeast(0)
                            val globalProgressPct = if (totalGoal > 0) totalCompleted.toFloat() / totalGoal else 0f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF000000))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Overall Stats",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Column {
                                                Text("Goal", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("$totalGoal", color = textWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                            }
                                            Column {
                                                Text("Completed", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("$totalCompleted", color = neonBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                            }
                                            Column {
                                                Text("Left", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("$totalLeft", color = neonOrange, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                            }
                                        }

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(end = 12.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { globalProgressPct },
                                                modifier = Modifier.size(90.dp),
                                                color = neonBlue,
                                                strokeWidth = 10.dp,
                                                trackColor = Color(0xFF1C2230)
                                            )
                                            Text(
                                                text = String.format("%d%%", (globalProgressPct * 100).toInt()),
                                                color = textWhite,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }

                            // WIDGET 3: Analysis
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF000000))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Analysis",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF151926))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("Day", modifier = Modifier.weight(1.2f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("Goal", modifier = Modifier.weight(1f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("Act", modifier = Modifier.weight(1f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("Left", modifier = Modifier.weight(1f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("Prog", modifier = Modifier.weight(2.5f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("%", modifier = Modifier.weight(1.2f), color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                    }

                                    Box(modifier = Modifier.height(200.dp)) {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(daysProgress) { (d, _, progress) ->
                                                val dayGoal = habits.size
                                                val dayAct = (progress * dayGoal).toInt()
                                                val dayLeft = (dayGoal - dayAct).coerceAtLeast(0)
                                                val dayPct = (progress * 100).toInt()

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("$d", modifier = Modifier.weight(1.2f), color = textWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("$dayGoal", modifier = Modifier.weight(1f), color = textMuted, fontSize = 11.sp)
                                                    Text("$dayAct", modifier = Modifier.weight(1f), color = neonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("$dayLeft", modifier = Modifier.weight(1f), color = neonOrange, fontSize = 11.sp)

                                                    Box(modifier = Modifier.weight(2.5f).padding(end = 4.dp)) {
                                                        LinearProgressIndicator(
                                                            progress = { progress },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(5.dp)
                                                                .clip(RoundedCornerShape(2.5.dp)),
                                                            color = neonBlue,
                                                            trackColor = Color(0xFF1C2230)
                                                        )
                                                    }

                                                    Text("$dayPct%", modifier = Modifier.weight(1.2f), color = textWhite, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
                                                }
                                                Divider(color = borderStrokeColor.copy(alpha = 0.4f), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }

                            // WIDGET 3.5: Average Sleep Time Analysis
                            val sleepLogs = remember(wellness, selectedMonth, selectedYear, daysInMonth) {
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                val validDates = (1..daysInMonth).map { d ->
                                    val c = Calendar.getInstance()
                                    c.set(Calendar.YEAR, selectedYear)
                                    c.set(Calendar.MONTH, selectedMonth - 1)
                                    c.set(Calendar.DATE, d)
                                    format.format(c.time)
                                }.toSet()
                                wellness.filter { validDates.contains(it.dateString) && it.sleepHours > 0f }
                            }

                            val avgSleep = remember(sleepLogs) {
                                if (sleepLogs.isNotEmpty()) {
                                    sleepLogs.map { it.sleepHours }.average().toFloat()
                                } else {
                                    0f
                                }
                            }

                            val sleepTarget = 8.0f
                            val targetProgress = if (avgSleep > 0f) (avgSleep / sleepTarget).coerceIn(0f, 1.5f) else 0f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF000000))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "AVERAGE SLEEP ANALYSIS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Average Sleep",
                                                    color = textMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        text = String.format(java.util.Locale.US, "%.1f", avgSleep),
                                                        color = neonPurple,
                                                        fontSize = 28.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = " hrs/night",
                                                        color = textWhite,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                }
                                            }

                                            val (qualityText, qualityColor) = remember(avgSleep) {
                                                when {
                                                    avgSleep == 0f -> Pair("No Logs", textMuted)
                                                    avgSleep < 6.0f -> Pair("Sleep Deprived", neonRed)
                                                    avgSleep < 7.0f -> Pair("Suboptimal", neonOrange)
                                                    avgSleep <= 9.0f -> Pair("Optimal Sleep", neonGreen)
                                                    else -> Pair("Excessive Sleep", neonBlue)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(qualityColor.copy(alpha = 0.15f))
                                                    .border(1.dp, qualityColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = qualityText,
                                                    color = qualityColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Progress to 8h Goal",
                                                    color = textMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = String.format(java.util.Locale.US, "%d%%", (targetProgress * 100).toInt()),
                                                    color = textWhite,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                LinearProgressIndicator(
                                                    progress = { targetProgress.coerceAtMost(1f) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp)),
                                                    color = neonPurple,
                                                    trackColor = Color(0xFF1C2230)
                                                )
                                            }
                                        }

                                        val maxSleep = remember(sleepLogs) {
                                            if (sleepLogs.isNotEmpty()) sleepLogs.maxOf { it.sleepHours } else 0f
                                        }
                                        val minSleep = remember(sleepLogs) {
                                            if (sleepLogs.isNotEmpty()) sleepLogs.minOf { it.sleepHours } else 0f
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF151926), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                                Text("LOGGED DAYS", color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("${sleepLogs.size} nights", color = textWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(borderStrokeColor))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                                Text("MAX SLEEP", color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(String.format(java.util.Locale.US, "%.1f hrs", maxSleep), color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(borderStrokeColor))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                                Text("MIN SLEEP", color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(String.format(java.util.Locale.US, "%.1f hrs", minSleep), color = neonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // WIDGET 4: Top 10 Habits
                            val topHabits = remember(completions, habits, selectedMonth, selectedYear) {
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                val validDates = (1..daysInMonth).map { d ->
                                    val c = Calendar.getInstance()
                                    c.set(Calendar.YEAR, selectedYear)
                                    c.set(Calendar.MONTH, selectedMonth - 1)
                                    c.set(Calendar.DATE, d)
                                    format.format(c.time)
                                }
                                habits.map { habit ->
                                    val completedCount = completions.count { it.habitId == habit.id && validDates.contains(it.dateString) }
                                    val rate = if (daysInMonth > 0) completedCount.toFloat() / daysInMonth else 0f
                                    Pair(habit, rate)
                                }.sortedByDescending { it.second }.take(10)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF000000))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "TOP 10 HABITS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Column(modifier = Modifier.padding(12.dp)) {
                                        topHabits.forEachIndexed { idx, (habit, rate) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${idx + 1}",
                                                        color = neonBlue,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.width(18.dp)
                                                    )
                                                    Text(habit.emoji, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = habit.name,
                                                        color = textWhite,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Text(
                                                    text = String.format("%d%%", (rate * 100).toInt()),
                                                    color = neonGreen,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (idx < topHabits.size - 1) {
                                                Divider(color = borderStrokeColor.copy(alpha = 0.3f), thickness = 0.5.dp)
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

    // Wellness logger dialog
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
            accentTeal = neonGreen,
            cardBackground = cardBackground,
            textWhite = textWhite,
            textMuted = textMuted
        )
    }

    // Add/Edit Habit Dialog
    if (showAddHabitDialog) {
        AddEditHabitDialog(
            habit = habitToEdit,
            onDismiss = { showAddHabitDialog = false },
            onSave = { name, emoji, attribute ->
                if (habitToEdit == null) {
                    viewModel.addHabit(name, emoji, attribute)
                } else {
                    viewModel.updateHabit(habitToEdit!!.copy(name = name, emoji = emoji, attribute = attribute))
                }
                showAddHabitDialog = false
            },
            accentBlue = neonBlue,
            cardBackground = cardBackground,
            textWhite = textWhite
        )
    }

    // Google Backup Dialog
    if (showGoogleBackupDialog) {
        Dialog(onDismissRequest = { showGoogleBackupDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon and Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Cloud Backup", tint = neonBlue, modifier = Modifier.size(24.dp))
                        Text("Google Sync & Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (googleAccount == null) {
                        // Non-connected state
                        Text(
                            text = "Connect your Google account to securely back up your habit list, completions, wellness logs, and quests to Google Drive. Keep your progress safe if you delete or reinstall the app.",
                            color = textMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = {
                                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                )
                                    .requestEmail()
                                    .requestIdToken("907853246347-uijpoit137emmtp4rqnmrkgpg3651h32.apps.googleusercontent.com")
                                    .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.appdata"))
                                    .build()
                                val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect Google Account", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        // Connected state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F121C), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // User info
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = neonPurple, modifier = Modifier.size(40.dp))
                            Text(googleAccount!!.displayName ?: "Google User", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textWhite)
                            Text(googleAccount!!.email ?: "", fontSize = 12.sp, color = textMuted)
                        }

                        Text(
                            text = "Your progress is linked to this account. You can manually back up your current progress to Google Drive, or restore your previous progress back to this device.",
                            color = textMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.backupToGoogleDrive(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !isBackupLoading
                            ) {
                                Text("Backup Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.restoreFromGoogleDrive(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !isBackupLoading
                            ) {
                                Text("Restore", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        TextButton(
                            onClick = { viewModel.signOut(context) },
                            colors = ButtonDefaults.textButtonColors(contentColor = neonRed)
                        ) {
                            Text("Disconnect Account", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }

                    if (isBackupLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = neonBlue, modifier = Modifier.size(24.dp))
                        }
                    }

                    // Show status message if any
                    backupStatus?.let { status ->
                        val statusColor = when {
                            status.contains("successful", ignoreCase = true) || status.contains("restored", ignoreCase = true) -> neonGreen
                            status.contains("failed", ignoreCase = true) || status.contains("No backup", ignoreCase = true) -> neonOrange
                            else -> textWhite
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { viewModel.clearBackupStatus() },
                                colors = ButtonDefaults.textButtonColors(contentColor = textMuted)
                            ) {
                                Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = borderStrokeColor, modifier = Modifier.padding(vertical = 8.dp))

                    // Offline Manual Backup Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = "Offline Backup", tint = neonPurple, modifier = Modifier.size(20.dp))
                        Text("Offline / Manual Backup", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textWhite)
                    }

                    Text(
                        text = "If you cannot connect your Google account in this environment, you can copy your progress as text, or paste a previously saved backup string to restore.",
                        color = textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    var importText by remember { mutableStateOf("") }
                    var showImportInput by remember { mutableStateOf(false) }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackupToClipboard { json ->
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(json))
                                    android.widget.Toast.makeText(context, "Backup copied to clipboard!", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f).border(1.dp, borderStrokeColor, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = neonBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Text", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showImportInput = !showImportInput },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).border(1.dp, borderStrokeColor, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = neonPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showImportInput) "Hide Import" else "Import Text", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (showImportInput) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = importText,
                                onValueChange = { importText = it },
                                label = { Text("Paste backup code here...", color = textMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = textWhite),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonPurple,
                                    unfocusedBorderColor = borderStrokeColor,
                                    focusedContainerColor = Color(0xFF0F121C),
                                    unfocusedContainerColor = Color(0xFF0F121C)
                                )
                            )

                            Button(
                                onClick = {
                                    if (importText.isNotBlank()) {
                                        viewModel.importBackupFromString(importText) { success ->
                                            if (success) {
                                                importText = ""
                                                showImportInput = false
                                                android.widget.Toast.makeText(context, "Progress restored successfully!", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to restore backup. Invalid code.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Backup Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Close dialog button
                    Button(
                        onClick = { showGoogleBackupDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = borderStrokeColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ==================== DASHBOARD SCREEN ====================
@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    cardBg: Color,
    borderCol: Color,
    neonBlue: Color,
    neonPurple: Color,
    neonGreen: Color,
    neonOrange: Color,
    neonRed: Color,
    textWhite: Color,
    textMuted: Color
) {
    val streakState by viewModel.habitStreak.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.streakNotificationsEnabled.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncedTime by viewModel.lastSyncedTime.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var testNotificationMessage by remember { mutableStateOf<String?>(null) }

    val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US) }
    
    // 7 Days Weekly Completion Data calculation
    val last7Days = remember(completions) {
        (0..6).map { i ->
            val c = Calendar.getInstance()
            c.add(Calendar.DATE, -i)
            sdf.format(c.time)
        }.reversed()
    }
    val weeklyCompletions = remember(completions, last7Days) {
        last7Days.map { date ->
            completions.count { it.dateString == date }
        }
    }
    val dayLabels = remember(last7Days) {
        last7Days.map { dateStr ->
            try {
                val parsed = sdf.parse(dateStr)
                val c = Calendar.getInstance()
                c.time = parsed
                val dayOfWeek = when (c.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "Su"
                    Calendar.MONDAY -> "Mo"
                    Calendar.TUESDAY -> "Tu"
                    Calendar.WEDNESDAY -> "We"
                    Calendar.THURSDAY -> "Th"
                    Calendar.FRIDAY -> "Fr"
                    Calendar.SATURDAY -> "Sa"
                    else -> ""
                }
                dayOfWeek
            } catch (e: Exception) {
                ""
            }
        }
    }

    // 4 Months Monthly Completion Data calculation
    val monthlyCompletions = remember(completions) {
        (0..3).map { i ->
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            val monthNum = c.get(Calendar.MONTH) + 1
            val yearNum = c.get(Calendar.YEAR)
            val monthLabel = when (monthNum) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> ""
            }
            val count = completions.count {
                try {
                    val pDate = sdf.parse(it.dateString)
                    val pCal = Calendar.getInstance()
                    pCal.time = pDate
                    pCal.get(Calendar.MONTH) + 1 == monthNum && pCal.get(Calendar.YEAR) == yearNum
                } catch (e: Exception) {
                    false
                }
            }
            monthLabel to count
        }.reversed()
    }

    // 24 Days Daily Progress Data calculation
    val last24Days = remember(completions, habits) {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val activeHabitsCount = habits.size.coerceAtLeast(1)
        (0..23).map { i ->
            val c = Calendar.getInstance()
            c.add(Calendar.DATE, -i)
            val dateStr = format.format(c.time)
            val dayOfWeek = when (c.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Su"
                Calendar.MONDAY -> "Mo"
                Calendar.TUESDAY -> "Tu"
                Calendar.WEDNESDAY -> "We"
                Calendar.THURSDAY -> "Th"
                Calendar.FRIDAY -> "Fr"
                Calendar.SATURDAY -> "Sa"
                else -> ""
            }
            val completedCount = completions.count { it.dateString == dateStr }
            val progressPct = (completedCount.toFloat() / activeHabitsCount).coerceIn(0f, 1f)
            Triple(dateStr, dayOfWeek, progressPct)
        }.reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==================== BENTO TILE 1: DAILY PROGRESS CHART ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column {
                // Header Bar (Edge-to-edge, black background with white text)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Progress",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Chart Panel (Light gray/silver background with dark bars as in the image)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD8DBE0)) // Elegant silver-gray background
                        .padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-Axis Labels
                    Column(
                        modifier = Modifier
                            .height(110.dp)
                            .padding(end = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("100%", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text("75%", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text("50%", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text("25%", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text("0%", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Chart Bars and X-Axis
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bars Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last24Days.forEach { (_, dayOfWeek, progress) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(progress.coerceAtLeast(0.02f))
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(Color(0xFF0F172A)) // Sharp dark-black bars
                                )
                            }
                        }

                        // X-Axis line (Black)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .background(Color.Black)
                        )

                        // X-Axis rotated labels Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            last24Days.forEach { (_, dayOfWeek, _) ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = dayOfWeek,
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = -45f
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== BENTO ROW 2: STREAK MATRIX (LEFT) & SECURE SHIELD (RIGHT) ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak Bento Tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(235.dp)
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 24.sp)
                            Box(
                                modifier = Modifier
                                    .background(neonOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("MAX: ${streakState.second}d", color = neonOrange, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("STREAK MATRIX", color = neonOrange, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("${streakState.first} Day Streak", color = textWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sustain daily habit consistency", color = textMuted, fontSize = 9.sp, lineHeight = 12.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alert Reminder", color = textWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.toggleStreakNotifications() },
                                modifier = Modifier.scale(0.75f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = neonOrange,
                                    checkedTrackColor = neonOrange.copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        Button(
                            onClick = {
                                testNotificationMessage = "⚡ [Arise Life-OS] Daily Reminder: Keep your ${streakState.first}-day streak alive! Access your matrix to complete your habits now."
                            },
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF1E2638) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = textWhite, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Alert", color = textWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Secure Shield Bento Tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(235.dp)
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Security Shield", tint = neonBlue, modifier = Modifier.size(20.dp))
                            Box(
                                modifier = Modifier
                                    .background(neonBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("AES-GCM", color = neonBlue, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("SECURE SHIELD", color = neonBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Encrypted Backup", color = textWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = when (syncStatus) {
                                "SYNCING" -> "Backup pending..."
                                "SECURE_SYNCED" -> "Secured Vault"
                                else -> "Local-first storage"
                            },
                            color = when (syncStatus) {
                                "SYNCING" -> neonOrange
                                "SECURE_SYNCED" -> neonGreen
                                else -> textMuted
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Last: $lastSyncedTime", color = textMuted, fontSize = 8.sp, maxLines = 1)
                    }

                    if (syncStatus == "SYNCING") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                color = neonBlue,
                                trackColor = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Encrypting...", color = textMuted, fontSize = 8.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.runCloudBackupAndSync() },
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonBlue.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, neonBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = neonBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Vault", color = neonBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==================== BENTO ROW 3: WEEKLY CHARTS (LEFT) & MONTHLY CHARTS (RIGHT) ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Weekly Chart Bento Tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(190.dp)
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("WEEKLY TREND", color = neonGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Activity Grid", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyCompletions.forEachIndexed { index, count ->
                            val dayLabel = dayLabels.getOrNull(index) ?: ""
                            val maxWeekly = weeklyCompletions.maxOrNull()?.coerceAtLeast(1) ?: 1
                            val barHeightFactor = count.toFloat() / maxWeekly

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 8.sp,
                                    color = if (count > 0) neonGreen else textMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight(barHeightFactor.coerceIn(0.15f, 1f))
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(neonGreen, neonGreen.copy(alpha = 0.3f))
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = dayLabel, fontSize = 8.sp, color = textMuted, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Monthly Chart Bento Tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(190.dp)
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MONTHLY TREND", color = neonPurple, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Activity Logs", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlyCompletions.forEach { (monthLabel, count) ->
                            val maxMonthly = monthlyCompletions.map { it.second }.maxOrNull()?.coerceAtLeast(1) ?: 1
                            val barHeightFactor = count.toFloat() / maxMonthly

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 8.sp,
                                    color = if (count > 0) neonPurple else textMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .fillMaxHeight(barHeightFactor.coerceIn(0.15f, 1f))
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(neonPurple, neonPurple.copy(alpha = 0.3f))
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = monthLabel, fontSize = 8.sp, color = textMuted, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==================== BENTO ROW 4: DATA EXPORT TILE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DATA PORTABILITY", color = neonPurple, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Export Ledger", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Download, contentDescription = null, tint = neonPurple, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Securely download habit metrics as portable structures", color = textMuted, fontSize = 10.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val csvBuilder = java.lang.StringBuilder()
                            csvBuilder.append("Habit ID,Habit Name,Attribute,Date Completed,Timestamp\n")
                            val habitMap = habits.associateBy { it.id }
                            completions.forEach { completion ->
                                val h = habitMap[completion.habitId]
                                val hName = h?.name ?: "Unknown"
                                val attr = h?.attribute ?: "DIS"
                                csvBuilder.append("${completion.habitId},\"${hName}\",${attr},${completion.dateString},${completion.timestamp}\n")
                            }
                            
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_TEXT, csvBuilder.toString())
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Arise_Life_OS_Completions.csv")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Export CSV"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Export CSV", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val jsonBuilder = java.lang.StringBuilder()
                            jsonBuilder.append("[\n")
                            val habitMap = habits.associateBy { it.id }
                            completions.forEachIndexed { idx, completion ->
                                val h = habitMap[completion.habitId]
                                val hName = h?.name ?: "Unknown"
                                val attr = h?.attribute ?: "DIS"
                                jsonBuilder.append("  {\n")
                                jsonBuilder.append("    \"habitId\": ${completion.habitId},\n")
                                jsonBuilder.append("    \"habitName\": \"$hName\",\n")
                                jsonBuilder.append("    \"attribute\": \"$attr\",\n")
                                jsonBuilder.append("    \"dateString\": \"${completion.dateString}\",\n")
                                jsonBuilder.append("    \"timestamp\": ${completion.timestamp}\n")
                                jsonBuilder.append(if (idx == completions.size - 1) "  }\n" else "  },\n")
                            }
                            jsonBuilder.append("]")
                            
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "application/json"
                                putExtra(android.content.Intent.EXTRA_TEXT, jsonBuilder.toString())
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Export JSON"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Export JSON", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom spacer to ensure scrolling list padding
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dynamic Test Signal alert overlay
    testNotificationMessage?.let { msg ->
        Dialog(onDismissRequest = { testNotificationMessage = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, neonOrange), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔔 STREAK NOTIFICATION 🔔", color = neonOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🔥", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(msg, color = textWhite, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { testNotificationMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = neonOrange)
                    ) {
                        Text("Acknowledge Signal", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ==================== QUESTS SCREEN ====================
@Composable
fun QuestsScreen(
    quests: List<Quest>,
    onToggleQuest: (Quest) -> Unit,
    onAddQuestClick: () -> Unit,
    onDeleteQuest: (Quest) -> Unit,
    cardBg: Color,
    borderCol: Color,
    neonBlue: Color,
    textWhite: Color,
    textMuted: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MISSION CONTROL", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textWhite)
            
            Button(
                onClick = onAddQuestClick,
                colors = ButtonDefaults.buttonColors(containerColor = neonBlue.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, neonBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Quest", tint = neonBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Quest", color = neonBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quests.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No active missions. Click 'New Quest' to spawn quests!", color = textMuted, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quests) { quest ->
                    val attributeColor = when (quest.attributeToReward) {
                        "STR" -> Color(0xFFEF4444)
                        "INT" -> Color(0xFF8B5CF6)
                        "MND" -> Color(0xFF00C3FF)
                        "DIS" -> Color(0xFFFF9E00)
                        else -> Color(0xFF10B981)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, if (quest.isCompleted) borderCol else borderCol.copy(alpha = 0.8f)), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                              ) {
                                // Completion checkbox with neat transition
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (quest.isCompleted) neonBlue else Color.Transparent)
                                        .border(BorderStroke(1.5.dp, if (quest.isCompleted) neonBlue else textMuted), CircleShape)
                                        .clickable { onToggleQuest(quest) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (quest.isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quest.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (quest.isCompleted) textMuted else textWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Category Chip
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF1B2330), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(quest.category, color = textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Reward attribute tag
                                        Box(
                                            modifier = Modifier
                                                .background(attributeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(BorderStroke(0.5.dp, attributeColor.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(quest.attributeToReward, color = attributeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("+${quest.xpReward} XP", color = neonBlue, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteQuest(quest) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete quest", tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== MATRIX GRID SCREEN ====================
@Composable
fun MatrixGridScreen(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    wellness: List<DailyWellness>,
    month: Int,
    year: Int,
    onToggleCompletion: (Int, String) -> Unit,
    onWellnessClick: (String) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onResetMatrix: () -> Unit,
    cardBg: Color,
    borderCol: Color,
    neonBlue: Color,
    neonTeal: Color,
    textWhite: Color,
    textMuted: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Daily Completion Progress Visualizer (styled in Arise neon)
        DailyProgressChart(
            habits = habits,
            completions = completions,
            month = month,
            year = year,
            accentTeal = neonBlue,
            cardBackground = cardBg,
            textWhite = textWhite,
            textMuted = textMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, borderCol, RoundedCornerShape(16.dp))
        ) {
            GridTrackerView(
                habits = habits,
                completions = completions,
                wellness = wellness,
                month = month,
                year = year,
                onToggleCompletion = onToggleCompletion,
                onWellnessClick = onWellnessClick,
                onEditHabit = onEditHabit,
                onDeleteHabit = onDeleteHabit,
                onResetMatrix = onResetMatrix,
                accentBlue = neonBlue,
                accentTeal = neonTeal,
                textWhite = textWhite,
                textMuted = textMuted,
                gridBorderColor = borderCol
            )
        }
    }
}

// ==================== DOJO (TIMER GAME DRILLS) SCREEN ====================
@Composable
fun DojoScreen(
    viewModel: HabitViewModel,
    cardBg: Color,
    borderCol: Color,
    neonBlue: Color,
    neonPurple: Color,
    neonRed: Color,
    textWhite: Color,
    textMuted: Color
) {
    var timerType by remember { mutableStateOf<String?>(null) } // "PHYSICAL" or "MIND"
    var secondsLeft by remember { mutableStateOf(15) }
    var isTimerActive by remember { mutableStateOf(false) }
    var showDojoCompleteEffect by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(isTimerActive, secondsLeft) {
        if (isTimerActive && secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        } else if (isTimerActive && secondsLeft == 0) {
            isTimerActive = false
            // Add Quest Completed XP! (Dojo drill adds 50 XP to user directly)
            val title = if (timerType == "PHYSICAL") "Physical Drill Executed" else "Mind Palace Meditation"
            val category = "Dojo Session"
            val xpReward = 50
            val attr = if (timerType == "PHYSICAL") "STR" else "INT"
            viewModel.addQuest(title, category, xpReward, attr)
            // Complete it in the background by finding the last added dojo quest
            scope.launch {
                delay(200)
                val activeQuests = viewModel.quests.value
                val questToComplete = activeQuests.find { it.title == title && !it.isCompleted }
                if (questToComplete != null) {
                    viewModel.toggleQuestCompletion(questToComplete)
                }
            }
            showDojoCompleteEffect = true
            timerType = null
        }
    }

    if (showDojoCompleteEffect) {
        Dialog(onDismissRequest = { showDojoCompleteEffect = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, neonBlue), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡ DRILL SUCCESSFUL ⚡", color = neonBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("🌟", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("You completed your dojo training session!", color = textWhite, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Text("+50 XP directly awarded!", color = neonBlue, fontWeight = FontWeight.Black, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showDojoCompleteEffect = false },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
                    ) {
                        Text("Acknowledge", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CHRONOS TRAINING DOJO", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textWhite, modifier = Modifier.fillMaxWidth())
        Text("Execute hyper-focus exercises to level up your Attributes actively. Doing training builds instant Strength and Mind points.", color = textMuted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())

        if (timerType == null) {
            // 1. PHYSICAL DRILL START CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🏋️ PHYSICAL HIIT DRILL", color = neonRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Complete a high-intensity physical body exercise (pushups, squats, or stretching) for 15 seconds.", color = textMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            timerType = "PHYSICAL"
                            secondsLeft = 15
                            isTimerActive = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonRed)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Drill (+50 STR XP)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. STUDY DRILL START CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("🧘 CHRONOS STUDY DRILL", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("A 15-second deep meditation or visual reading flashcard drill to build intellect points.", color = textMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            timerType = "MIND"
                            secondsLeft = 15
                            isTimerActive = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Drill (+50 INT XP)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Active countdown mode with pulsating neon animations!
            val activeColor = if (timerType == "PHYSICAL") neonRed else neonPurple
            val exerciseText = if (timerType == "PHYSICAL") "Perform Pushups / Squats now!" else "Deep Mind Focus. Keep breathing."

            val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
            val scalePulse by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scalePulse)
                        .background(Brush.radialGradient(listOf(activeColor.copy(alpha = 0.25f), Color.Transparent)), CircleShape)
                        .border(BorderStroke(4.dp, activeColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$secondsLeft", fontSize = 54.sp, fontWeight = FontWeight.Black, color = textWhite)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(exerciseText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Dojo Mode Active", color = textMuted, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        timerType = null
                        isTimerActive = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Abort Mission", color = Color.White)
                }
            }
        }
    }
}

// ==================== MASTER HEADER SECTION ====================
@Composable
fun HeaderSection(
    selectedMonth: Int,
    selectedYear: Int,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
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

    val headerBg = if (isDarkTheme) Color(0xFF07090E) else Color(0xFFF8FAFC)
    val textWhite = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val neonBlue = if (isDarkTheme) Color(0xFF00C3FF) else Color(0xFF0284C7)

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
                text = "HABIT BOARD",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = textWhite,
                style = TextStyle(shadow = Shadow(color = neonBlue, blurRadius = 6f))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Month selector
                Box {
                    TextButton(onClick = { expandedMonth = true }, contentPadding = PaddingValues(0.dp)) {
                        Text(months[selectedMonth - 1], color = neonBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = neonBlue, modifier = Modifier.size(14.dp))
                    }
                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false },
                        modifier = Modifier.background(if (isDarkTheme) Color(0xFF11141E) else Color.White)
                    ) {
                        months.forEachIndexed { index, m ->
                            DropdownMenuItem(
                                text = { Text(m, color = if (isDarkTheme) Color.White else Color.Black, fontSize = 12.sp) },
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
                    TextButton(onClick = { expandedYear = true }, contentPadding = PaddingValues(0.dp)) {
                        Text(selectedYear.toString(), color = neonBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = neonBlue, modifier = Modifier.size(14.dp))
                    }
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false },
                        modifier = Modifier.background(if (isDarkTheme) Color(0xFF11141E) else Color.White)
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString(), color = if (isDarkTheme) Color.White else Color.Black, fontSize = 12.sp) },
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

        // Action additions
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Theme toggle button
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .background(if (isDarkTheme) Color(0xFF1E2638) else Color(0xFFE2E8F0), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.Nightlight,
                    contentDescription = "Toggle Theme",
                    tint = if (isDarkTheme) Color(0xFFFFD700) else Color(0xFF475569),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onAddHabitClick,
                modifier = Modifier
                    .background(neonBlue, CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = if (isDarkTheme) Color.Black else Color.White)
            }
        }
    }
}

// ==================== DETAILED SYNCHRONIZED MATRIX VIEW ====================
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GridTrackerView(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    wellness: List<DailyWellness>,
    month: Int,
    year: Int,
    onToggleCompletion: (Int, String) -> Unit,
    onWellnessClick: (String) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onResetMatrix: () -> Unit,
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
            Text("No habits initialized. Trigger the '+' action at the top bar to register habits!", color = textMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        return
    }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Title Bar matching the image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habits Grid Board",
                fontWeight = FontWeight.Black,
                color = textWhite,
                fontSize = 15.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Scroll horizontally →",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onResetMatrix,
                    modifier = Modifier
                        .background(accentBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, accentBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Grid to Default Image Layout",
                        tint = accentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 1. STICKY HEADER ROW (Corner and Horizontal day numbers with vertical completion bars)
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(86.dp)
                    .background(Color(0xFF11141E))
                    .border(BorderStroke(1.dp, gridBorderColor))
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Active Habits", fontWeight = FontWeight.Black, color = textWhite, fontSize = 12.sp)
            }

            // Days scroller (Sharing horizontal scroller instance)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Row {
                    daysList.forEach { d ->
                        val dayOfWeek = getDayOfWeekAbbreviation(d, month, year)
                        val isToday = d == todayDay && month == todayMonth && year == todayYear

                        val dateString = String.format("%04d-%02d-%02d", year, month, d)
                        val dayCompletions = completions.filter { it.dateString == dateString }
                        val completionRate = if (habits.isNotEmpty()) {
                            dayCompletions.size.toFloat() / habits.size
                        } else {
                            0f
                        }

                        Column(
                            modifier = Modifier
                                .width(42.dp)
                                .height(86.dp)
                                .background(if (isToday) accentBlue.copy(alpha = 0.25f) else Color(0xFF131722))
                                .border(BorderStroke(1.dp, gridBorderColor)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Vertical progress bar at the top of the day header column
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF1A1F2C))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(completionRate)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(accentBlue, accentBlue.copy(alpha = 0.4f))
                                            )
                                        )
                                )
                            }

                            Divider(color = gridBorderColor, thickness = 0.5.dp)

                            Text(
                                text = d.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) accentBlue else textWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = dayOfWeek,
                                color = if (isToday) accentBlue else textMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. SCROLLABLE MATRIX GRID BODY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(verticalScrollState)
        ) {
            // Habit identifiers column
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .background(Color(0xFF11141E))
                    .border(BorderStroke(1.dp, gridBorderColor))
            ) {
                habits.forEach { habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 6.dp)
                            .combinedClickable(
                                onClick = { onWellnessClick(String.format("%04d-%02d-01", year, month)) }, // generic fallback click
                                onLongClick = { onEditHabit(habit) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(habit.emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.name,
                                color = textWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Option menu dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onEditHabit(habit) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⋮", color = textMuted, fontSize = 12.sp)
                        }
                    }
                    Divider(color = gridBorderColor, thickness = 1.dp)
                }

                // Mood and Sleep labels
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Mood 🎭", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = gridBorderColor, thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Sleep 🛌", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sync matrix scroll checklist container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Row {
                    daysList.forEach { d ->
                        val isToday = d == todayDay && month == todayMonth && year == todayYear
                        val dateString = String.format("%04d-%02d-%02d", year, month, d)

                        Column(
                            modifier = Modifier
                                .width(42.dp)
                                .background(if (isToday) accentBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .border(BorderStroke(1.dp, gridBorderColor)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Habits checkboxes
                            habits.forEach { habit ->
                                val isCompleted = completions.any { it.habitId == habit.id && it.dateString == dateString }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCompleted) accentBlue else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isCompleted) accentBlue else Color(0xFF1C2230),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onToggleCompletion(habit.id, dateString) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Divider(color = gridBorderColor, thickness = 1.dp)
                            }

                            // Mood cell
                            val moodLog = wellness.find { it.dateString == dateString }
                            val moodVal = moodLog?.mood ?: 0
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(Color.Transparent)
                                    .clickable { onWellnessClick(dateString) },
                                contentAlignment = Alignment.Center
                            ) {
                                when (moodVal) {
                                    1 -> Text("😡", fontSize = 14.sp)
                                    2 -> Text("😔", fontSize = 14.sp)
                                    3 -> Text("😐", fontSize = 14.sp)
                                    4 -> Text("🙂", fontSize = 14.sp)
                                    5 -> Text("🤩", fontSize = 14.sp)
                                    else -> Text("➖", color = textMuted, fontSize = 12.sp)
                                }
                            }
                            Divider(color = gridBorderColor, thickness = 1.dp)

                            // Sleep cell
                            val sleepHours = moodLog?.sleepHours ?: 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(Color.Transparent)
                                    .clickable { onWellnessClick(dateString) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (sleepHours > 0) {
                                    Text(
                                        text = String.format("%.1f", sleepHours),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentTeal
                                    )
                                } else {
                                    Text("➖", color = textMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== CHOSEN ATTRIBUTE INTERNALS / INTEL ====================
@Composable
fun AttributeIntelDialog(
    attribute: String,
    onDismiss: () -> Unit,
    cardBg: Color,
    borderCol: Color,
    neonBlue: Color,
    textWhite: Color,
    textMuted: Color
) {
    val title = when (attribute) {
        "STR" -> "🏋️ Strength Mastery Intel"
        "INT" -> "🧠 Intellect Codex Intel"
        "MND" -> "🧘 Mindfulness Sage Intel"
        "DIS" -> "🛡️ Absolute Discipline Intel"
        else -> "⚡ Vitality Emerald Intel"
    }

    val lore = when (attribute) {
        "STR" -> "Strength defines your physical capacity, muscular activation, and raw power output. Increase your STR level by executing intense workouts, going to the gym, and completing active HIIT dojo drills daily."
        "INT" -> "Intellect scales your deep analytical skill, reading volume, and memory recall. Level up INT by studying complex engineering topics, reading historical books, and initiating focused Mind Palace drills."
        "MND" -> "Mind dictates emotional mastery, mental clarity, and focus longevity. Grow your MND points by logging consistent wellness indexes, daily mood journaling, and meditating without distraction."
        "DIS" -> "Discipline represents the sovereign monarch's shield of willpower. It is the core multiplier of all stats. Gain DIS points by completing default routine habits, planners, and waking up at early intervals without failure."
        else -> "Vitality determines your cardiovascular integrity, recovery speed, and longevity. Level up VIT by drinking pristine amounts of water, sleeping 7-8 hours continuously, and eating complete organic foods."
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, neonBlue), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textWhite)
                Spacer(modifier = Modifier.height(12.dp))
                Text(lore, fontSize = 13.sp, color = textMuted, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close Intel", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== REAL TIME CELEBRATION LEVEL UP OVERLAY ====================
@Composable
fun LevelUpOverlay(
    level: Int,
    message: String,
    onDismiss: () -> Unit,
    neonBlue: Color,
    neonPurple: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Brush.horizontalGradient(listOf(neonBlue, neonPurple))), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090B11)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "levelupRotate")
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .rotate(rotationAngle)
                        .background(Brush.sweepGradient(listOf(neonBlue, neonPurple, neonBlue)), CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF090B11)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨",
                        fontSize = 48.sp,
                        modifier = Modifier.rotate(-rotationAngle) // counter rotate to keep emoji upright
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "LEVEL ASCENDED",
                    color = neonBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(shadow = Shadow(color = neonBlue, blurRadius = 8f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUE QUEST", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ==================== NEW QUEST DIALOG ====================
@Composable
fun AddQuestDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String) -> Unit,
    accentBlue: Color,
    cardBackground: Color,
    textWhite: Color
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Daily") }
    var selectedAttribute by remember { mutableStateOf("DIS") }
    var xpVal by remember { mutableStateOf("100") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, accentBlue), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Spawn New Quest", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textWhite)
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quest Mission Title", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textWhite,
                        unfocusedTextColor = textWhite,
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select Category
                Text("Quest Rank/Type", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Epic").forEach { cat ->
                        val active = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) accentBlue.copy(alpha = 0.2f) else Color.Transparent)
                                .border(BorderStroke(1.dp, if (active) accentBlue else Color(0xFF1C2230)), RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (active) accentBlue else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select Target Attribute
                Text("Core Target Attribute", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("STR", "INT", "MND", "DIS", "VIT").forEach { attr ->
                        val active = selectedAttribute == attr
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) accentBlue.copy(alpha = 0.2f) else Color.Transparent)
                                .border(BorderStroke(1.dp, if (active) accentBlue else Color(0xFF1C2230)), RoundedCornerShape(6.dp))
                                .clickable { selectedAttribute = attr },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(attr, color = if (active) accentBlue else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select XP
                OutlinedTextField(
                    value = xpVal,
                    onValueChange = { xpVal = it },
                    label = { Text("XP Bounty reward", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textWhite,
                        unfocusedTextColor = textWhite,
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color(0xFFCAC4D0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = {
                            val xpInt = xpVal.toIntOrNull() ?: 100
                            onSave(title, selectedCategory, xpInt, selectedAttribute)
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deploy Quest", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== DYNAMIC GRAPH CHART PROGRESS ====================
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
                "Matrix Completion Rate",
                fontWeight = FontWeight.Bold,
                color = textWhite,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

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
                        if (pct > 0) {
                            Text(
                                text = "$pct%",
                                fontSize = 8.sp,
                                color = accentTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(60.dp * (pct / 100f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(accentTeal, accentTeal.copy(alpha = 0.3f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum.toString(),
                            fontSize = 8.sp,
                            color = textMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== WELLNESS LOGGING DIALOG ====================
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
                .border(BorderStroke(1.dp, accentTeal), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Daily Wellness Matrix",
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Log Mood Indicator", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textWhite)
                    if (mood > 0) {
                        Text(
                            text = "Clear",
                            color = accentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { mood = 0 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
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
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentTeal.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) accentTeal else Color(0xFF1C2230),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { mood = if (mood == valNum) 0 else valNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Sleep Duration: ${String.format("%.1f", sleepHours)}h", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textWhite)
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    valueRange = 0f..12f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        activeTrackColor = accentTeal,
                        thumbColor = accentTeal
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
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
                        Text("Deploy Logs", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

// ==================== ADD / EDIT HABITS DIALOG ====================
@Composable
fun AddEditHabitDialog(
    habit: Habit?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    accentBlue: Color,
    cardBackground: Color,
    textWhite: Color
) {
    var name by remember { mutableStateOf(habit?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(habit?.emoji ?: "⏰") }
    var selectedAttribute by remember { mutableStateOf(habit?.attribute ?: "DIS") }

    val emojisList = listOf(
        "⏰", "💪", "📚", "🗓️", "💻", "🥗", "🚫", "📵", "📝", "❄️",
        "🧠", "🧘", "🚶", "💧", "🍳", "🛌", "🧹", "💡", "🎨", "🎵",
        "❤️", "🔥", "🚲", "🍵", "💵", "🌱", "🔑", "🛁", "🍎", "🏃"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, accentBlue), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (habit == null) "Initiate Matrix Habit" else "Modulate Habit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textWhite,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Identifier Title", color = Color.White.copy(alpha = 0.6f)) },
                    placeholder = { Text("e.g., Early Gym session", color = Color.Gray) },
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

                Text("Pick Emoji Glyph", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textWhite)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.height(110.dp)) {
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) accentBlue.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isSelected) accentBlue else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedEmoji = emoji },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
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
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedEmoji, selectedAttribute)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_habit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentBlue,
                            disabledContainerColor = Color(0xFF1C2230)
                        )
                    ) {
                        Text("Deploy", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

// Helpers for calendar calculation
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
