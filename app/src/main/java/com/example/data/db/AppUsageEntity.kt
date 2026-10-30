package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_stats")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
