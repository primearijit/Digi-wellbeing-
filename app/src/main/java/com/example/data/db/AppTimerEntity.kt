package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_timers")
data class AppTimerEntity(
    @PrimaryKey val packageName: String,
    val timerLimitMinutes: Int? = null
)
