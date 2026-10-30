package com.example.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.MetricType
import com.example.data.model.AppUsageModel
import com.example.ui.components.AppUsageItem
import com.example.ui.components.UsageBarChart
import com.example.ui.dialogs.EditStatsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActivityDetailsScreen(
    viewModel: AppActivityDetailsViewModel
) {
    val context = LocalContext.current

    val selectedMetric by viewModel.selectedMetric.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val summary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val chartBars by viewModel.chartBars.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val showEditSheet by viewModel.showEditSheet.collectAsStateWithLifecycle()
    val showFirstLaunchOnboarding by viewModel.showFirstLaunchOnboarding.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showPillMenu by remember { mutableStateOf(false) }
    var showSetDefaultDialog by remember { mutableStateOf(false) }

    var selectedTimerApp by remember { mutableStateOf<AppUsageModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App activity details",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* Back action */ },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF2B2B32))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select Default Wellbeing", color = Color(0xFFA8C7FA)) },
                            onClick = {
                                showMenu = false
                                showSetDefaultDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Stats (Secret Override)", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.openEditSheet()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Usage access settings", color = Color.White) },
                            onClick = {
                                showMenu = false
                                try {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131318)
                )
            )
        },
        containerColor = Color(0xFF131318)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Usage Permission Banner (if not granted)
            if (!hasPermission) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2B2B32))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFA8C7FA),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Usage Access Permission", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Grant permission for real foreground stats or use custom override", color = Color(0xFFC4C6D0), fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Text("Grant", color = Color(0xFFA8C7FA), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Metrics Switcher Pill ("Screen time v", "Notifications received v", "Times opened v")
            item {
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showPillMenu = true }
                            .testTag("metric_pill_button"),
                        color = Color(0xFFA8C7FA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMetric.title,
                                color = Color(0xFF003062),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select metric",
                                tint = Color(0xFF003062)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPillMenu,
                        onDismissRequest = { showPillMenu = false },
                        modifier = Modifier.background(Color(0xFF2B2B32))
                    ) {
                        MetricType.entries.forEach { metric ->
                            DropdownMenuItem(
                                text = { Text(metric.title, color = Color.White) },
                                onClick = {
                                    showPillMenu = false
                                    viewModel.setMetric(metric)
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Hero Display Value & Subtitle
            item {
                val valueToShow = when (selectedMetric) {
                    MetricType.SCREEN_TIME -> summary?.totalScreenTimeMinutes ?: 46
                    MetricType.NOTIFICATIONS -> summary?.totalNotifications ?: 85
                    MetricType.TIMES_OPENED -> summary?.totalOpens ?: 42
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedMetric.formatValue(valueToShow),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedDate == viewModel.selectedDate.value && summary?.displayDate?.contains("Today") == true) "Today" else summary?.displayDate ?: "Today",
                        color = Color(0xFFC4C6D0),
                        fontSize = 15.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Weekly Bar Chart
            item {
                UsageBarChart(
                    bars = chartBars,
                    metricType = selectedMetric,
                    onBarSelected = { dateStr ->
                        viewModel.selectDate(dateStr)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Date Navigation (< Fri, Oct 30 >)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateDate(-1) },
                        modifier = Modifier.testTag("prev_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous date",
                            tint = Color(0xFFC4C6D0)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = summary?.displayDate ?: "Fri, Oct 30",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("current_date_text")
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = { viewModel.navigateDate(1) },
                        modifier = Modifier.testTag("next_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next date",
                            tint = Color(0xFFC4C6D0)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // App Usage List
            items(summary?.appUsages ?: emptyList()) { app ->
                AppUsageItem(
                    app = app,
                    metricType = selectedMetric,
                    onClickTimer = {
                        selectedTimerApp = app
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Bottom Action Pill ("Show sites you visit")
            item {
                OutlinedButton(
                    onClick = { /* Action */ },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8E919E)),
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .testTag("show_sites_button")
                ) {
                    Text(
                        text = "Show sites you visit",
                        color = Color(0xFFD3E3FD),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // FIRST LAUNCH ONBOARDING SELECTION DIALOG
    if (showFirstLaunchOnboarding || showSetDefaultDialog) {
        var selectedMode by remember { mutableStateOf("NEW_WELLBEING") }

        AlertDialog(
            onDismissRequest = {
                if (!showFirstLaunchOnboarding) {
                    showSetDefaultDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = Color(0xFFA8C7FA),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Select Digital Wellbeing App",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select your preferred Digital Wellbeing handler. This choice invokes the Android OS system service.",
                        color = Color(0xFFC4C6D0),
                        fontSize = 13.sp
                    )

                    // Option A: New Digital Wellbeing
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (selectedMode == "NEW_WELLBEING") 2.dp else 1.dp,
                                color = if (selectedMode == "NEW_WELLBEING") Color(0xFFA8C7FA) else Color(0xFF383B46),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMode = "NEW_WELLBEING" },
                        color = if (selectedMode == "NEW_WELLBEING") Color(0xFF232836) else Color(0xFF1C1B20)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == "NEW_WELLBEING",
                                onClick = { selectedMode = "NEW_WELLBEING" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA8C7FA))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("This Digital Wellbeing (New)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF003062),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Recommended", color = Color(0xFFA8C7FA), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Requests real system UsageStatsManager service, custom stats editor & 2x2 widget", color = Color(0xFFC4C6D0), fontSize = 11.sp)
                            }
                        }
                    }

                    // Option B: System Digital Wellbeing
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (selectedMode == "SYSTEM_WELLBEING") 2.dp else 1.dp,
                                color = if (selectedMode == "SYSTEM_WELLBEING") Color(0xFFA8C7FA) else Color(0xFF383B46),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMode = "SYSTEM_WELLBEING" },
                        color = if (selectedMode == "SYSTEM_WELLBEING") Color(0xFF232836) else Color(0xFF1C1B20)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == "SYSTEM_WELLBEING",
                                onClick = { selectedMode = "SYSTEM_WELLBEING" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA8C7FA))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("System Stock Wellbeing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Opens built-in Android Settings system handler", color = Color(0xFFC4C6D0), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.selectOnboardingChoice(selectedMode)
                        showSetDefaultDialog = false

                        if (selectedMode == "NEW_WELLBEING") {
                            // Direct System Service Request for Usage Access & Default Settings
                            try {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    data = Uri.parse("package:" + context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        } else {
                            // Direct System Digital Wellbeing System Handler Launch
                            try {
                                val intent = Intent("android.settings.DIGITAL_WELLBEING_SETTINGS").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C7FA))
                ) {
                    Text("Launch System Service", color = Color(0xFF003062), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF2B2B32)
        )
    }

    // Secret Edit Stats BottomSheet
    if (showEditSheet) {
        val currentSummary = summary
        EditStatsBottomSheet(
            dateDisplay = currentSummary?.displayDate ?: selectedDate,
            initialScreenTimeMinutes = currentSummary?.totalScreenTimeMinutes ?: 46,
            initialNotifications = currentSummary?.totalNotifications ?: 85,
            initialOpens = currentSummary?.totalOpens ?: 42,
            initialIsCustom = currentSummary?.isOverride ?: true,
            initialAppList = currentSummary?.appUsages ?: emptyList(),
            onDismiss = { viewModel.closeEditSheet() },
            onSave = { isCustom, mins, notifs, opens, apps ->
                viewModel.saveCustomOverride(isCustom, mins, notifs, opens, apps)
            }
        )
    }

    // App Timer Dialog
    selectedTimerApp?.let { app ->
        var timerMinutes by remember { mutableIntStateOf(30) }
        AlertDialog(
            onDismissRequest = { selectedTimerApp = null },
            title = { Text("App timer for ${app.appName}", color = Color.White) },
            text = {
                Column {
                    Text("Set daily usage limit:", color = Color(0xFFC4C6D0))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = timerMinutes.toString(),
                        onValueChange = { timerMinutes = it.toIntOrNull() ?: 30 },
                        label = { Text("Minutes") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedTimerApp = null }) {
                    Text("Set timer")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTimerApp = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF2B2B32)
        )
    }
}
