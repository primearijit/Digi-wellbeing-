package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage_stats")
data class DailyUsageEntity(
    @PrimaryKey val date: String,
    val isCustomOverride: Boolean = true,
    val overrideTotalScreenTimeMinutes: Int = 46,
    val overrideTotalNotifications: Int = 85,
    val overrideTotalOpens: Int = 42
)
