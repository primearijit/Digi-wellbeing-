package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUsageModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStatsBottomSheet(
    dateDisplay: String,
    initialScreenTimeMinutes: Int,
    initialNotifications: Int,
    initialOpens: Int,
    initialIsCustom: Boolean,
    initialAppList: List<AppUsageModel>,
    onDismiss: () -> Unit,
    onSave: (
        isCustom: Boolean,
        screenTimeMins: Int,
        notifications: Int,
        opens: Int,
        apps: List<AppUsageModel>
    ) -> Unit
) {
    var isCustom by remember { mutableStateOf(initialIsCustom) }
    var hours by remember { mutableIntStateOf(initialScreenTimeMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialScreenTimeMinutes % 60) }
    var notificationsCount by remember { mutableIntStateOf(initialNotifications) }
    var opensCount by remember { mutableIntStateOf(initialOpens) }

    val appList = remember { mutableStateListOf<AppUsageModel>().apply { addAll(initialAppList) } }

    var showAddAppDialog by remember { mutableStateOf(false) }
    var editingAppIndex by remember { mutableIntStateOf(-1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1C1B20)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("edit_stats_bottom_sheet")
        ) {
            Text(
                text = "Edit Stats ($dateDisplay)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Override Quick Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B2B32))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Use Custom Data", color = Color.White, fontWeight = FontWeight.Medium)
                    Text(
                        if (isCustom) "Showing custom overridden stats" else "Showing real system stats",
                        color = Color(0xFFC4C6D0),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isCustom,
                    onCheckedChange = { isCustom = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF003062),
                        checkedTrackColor = Color(0xFFA8C7FA)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hours & Minutes Selectors
            Text("Total Screen Time Override", color = Color(0xFFA8C7FA), fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = hours.toString(),
                    onValueChange = { hours = it.toIntOrNull()?.coerceIn(0, 24) ?: 0 },
                    label = { Text("Hours") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA8C7FA)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = minutes.toString(),
                    onValueChange = { minutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                    label = { Text("Minutes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA8C7FA)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notifications & Opens
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = notificationsCount.toString(),
                    onValueChange = { notificationsCount = it.toIntOrNull()?.coerceAtLeast(0) ?: 0 },
                    label = { Text("Notifications") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA8C7FA)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = opensCount.toString(),
                    onValueChange = { opensCount = it.toIntOrNull()?.coerceAtLeast(0) ?: 0 },
                    label = { Text("Times Opened") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA8C7FA)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App List Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Apps", color = Color(0xFFA8C7FA), fontSize = 14.sp)
                TextButton(onClick = { showAddAppDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFA8C7FA))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App", color = Color(0xFFA8C7FA))
                }
            }

            // App List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                itemsIndexed(appList) { index, app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2B2B32))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.appName, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(
                                "${app.screenTimeMinutes} mins • ${app.notifications} notifs • ${app.opens} opens",
                                color = Color(0xFFC4C6D0),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { editingAppIndex = index }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit app", tint = Color(0xFFA8C7FA))
                        }

                        IconButton(onClick = { appList.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete app", tint = Color(0xFFFF8A80))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save & Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFFC4C6D0))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val totalMins = (hours * 60) + minutes
                        onSave(isCustom, totalMins, notificationsCount, opensCount, appList.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C7FA))
                ) {
                    Text("Save & Update Widget", color = Color(0xFF003062), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add/Edit App Dialog
    if (showAddAppDialog || editingAppIndex != -1) {
        val editingApp = if (editingAppIndex != -1 && editingAppIndex < appList.size) appList[editingAppIndex] else null

        var appName by remember { mutableStateOf(editingApp?.appName ?: "") }
        var appMins by remember { mutableStateOf(editingApp?.screenTimeMinutes?.toString() ?: "10") }
        var appNotifs by remember { mutableStateOf(editingApp?.notifications?.toString() ?: "5") }
        var appOpens by remember { mutableStateOf(editingApp?.opens?.toString() ?: "3") }
        var iconCategory by remember { mutableStateOf(editingApp?.iconCategory ?: "BRAVE") }

        AlertDialog(
            onDismissRequest = {
                showAddAppDialog = false
                editingAppIndex = -1
            },
            title = { Text(if (editingApp != null) "Edit App Usage" else "Add App Override", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name") }
                    )
                    OutlinedTextField(
                        value = appMins,
                        onValueChange = { appMins = it },
                        label = { Text("Screen Time (Minutes)") }
                    )
                    OutlinedTextField(
                        value = appNotifs,
                        onValueChange = { appNotifs = it },
                        label = { Text("Notifications") }
                    )
                    OutlinedTextField(
                        value = appOpens,
                        onValueChange = { appOpens = it },
                        label = { Text("Times Opened") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = appMins.toIntOrNull() ?: 10
                        val n = appNotifs.toIntOrNull() ?: 0
                        val o = appOpens.toIntOrNull() ?: 0
                        val newModel = AppUsageModel(
                            date = "",
                            packageName = "custom.pkg.${appName.lowercase().replace(" ", "")}",
                            appName = appName.ifEmpty { "Custom App" },
                            screenTimeMinutes = m,
                            notifications = n,
                            opens = o,
                            iconCategory = iconCategory
                        )
                        if (editingAppIndex != -1) {
                            appList[editingAppIndex] = newModel
                        } else {
                            appList.add(newModel)
                        }
                        showAddAppDialog = false
                        editingAppIndex = -1
                    }
                ) {
                    Text("Save App")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddAppDialog = false
                    editingAppIndex = -1
                }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF2B2B32)
        )
    }
}
