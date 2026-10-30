package com.example.data.db

enum class MetricType(val title: String, val unitSingular: String, val unitPlural: String) {
    SCREEN_TIME("Screen time", "minute", "minutes"),
    NOTIFICATIONS("Notifications received", "notification", "notifications"),
    TIMES_OPENED("Times opened", "time", "times");

    fun formatValue(value: Int): String {
        return when (this) {
            SCREEN_TIME -> {
                val hours = value / 60
                val mins = value % 60
                if (hours > 0) {
                    if (mins > 0) "$hours ${if (hours == 1) "hour" else "hours"} $mins ${if (mins == 1) "minute" else "minutes"}"
                    else "$hours ${if (hours == 1) "hour" else "hours"}"
                } else {
                    "$mins ${if (mins == 1) "minute" else "minutes"}"
                }
            }
            NOTIFICATIONS -> "$value ${if (value == 1) "notification" else "notifications"}"
            TIMES_OPENED -> "$value ${if (value == 1) "time" else "times"}"
        }
    }

    fun formatShortValue(value: Int): String {
        return when (this) {
            SCREEN_TIME -> {
                val hours = value / 60
                val mins = value % 60
                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            }
            NOTIFICATIONS -> "${value}"
            TIMES_OPENED -> "${value}"
        }
    }
}
