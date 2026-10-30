package com.example.data.model

data class DailySummaryModel(
    val dateString: String, // e.g. "2026-10-30"
    val displayDate: String, // e.g. "Fri, Oct 30"
    val totalScreenTimeMinutes: Int,
    val totalNotifications: Int,
    val totalOpens: Int,
    val isOverride: Boolean,
    val appUsages: List<AppUsageModel>
)

data class AppUsageModel(
    val id: Long = 0,
    val date: String,
    val packageName: String,
    val appName: String,
    val screenTimeMinutes: Int,
    val notifications: Int,
    val opens: Int,
    val iconCategory: String = "DEFAULT",
    val sortOrder: Int = 0,
    val hasTimerLimit: Boolean = true
)

data class DayChartBarModel(
    val dateString: String,
    val dayOfWeekLabel: String, // e.g. "Sun", "Mon"
    val fullDateDisplay: String, // e.g. "Fri, Oct 30"
    val totalScreenTimeMinutes: Int,
    val isToday: Boolean,
    val isSelected: Boolean
)
