package com.example.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Process
import com.example.data.db.*
import com.example.data.model.AppUsageModel
import com.example.data.model.DailySummaryModel
import com.example.data.model.DayChartBarModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WellbeingRepository(private val context: Context) {

    private val db = WellbeingDatabase.getInstance(context)
    private val dao = db.dao()

    private val prefs: SharedPreferences = context.getSharedPreferences("wellbeing_prefs", Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun setSelectedWellbeingMode(mode: String) {
        prefs.edit()
            .putString("selected_wellbeing_mode", mode)
            .putBoolean("has_completed_onboarding", true)
            .putBoolean("is_default_wellbeing", true)
            .apply()
    }

    fun getSelectedWellbeingMode(): String {
        return prefs.getString("selected_wellbeing_mode", "NEW_WELLBEING") ?: "NEW_WELLBEING"
    }

    fun isDefaultWellbeingApp(): Boolean {
        return prefs.getBoolean("is_default_wellbeing", false)
    }

    fun setDefaultWellbeingApp(isDefault: Boolean) {
        prefs.edit().putBoolean("is_default_wellbeing", isDefault).apply()
    }

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun switchToRealSystemUsage() = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        for (i in 0..6) {
            val dStr = dateFormat.format(calendar.time)
            val existing = dao.getDailyUsageSync(dStr)
            if (existing != null) {
                dao.insertOrUpdateDailyUsage(existing.copy(isCustomOverride = false))
            } else {
                dao.insertOrUpdateDailyUsage(DailyUsageEntity(date = dStr, isCustomOverride = false))
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)

    suspend fun initDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val todayStr = getTodayDateString()
        val existingToday = dao.getDailyUsageSync(todayStr)
        if (existingToday == null) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            val weekData = listOf(
                Pair(360, "Sun"), // 6 hours
                Pair(180, "Mon"), // 3 hours
                Pair(210, "Tue"), // 3.5 hours
                Pair(300, "Wed"), // 5 hours
                Pair(348, "Thu"), // 5.8 hours
                Pair(46, "Fri"),  // 46 minutes
                Pair(0, "Sat")    // 0 hours
            )

            for (i in 0..6) {
                val dStr = dateFormat.format(calendar.time)
                val (mins, _) = weekData[i]
                dao.insertOrUpdateDailyUsage(
                    DailyUsageEntity(
                        date = dStr,
                        isCustomOverride = true,
                        overrideTotalScreenTimeMinutes = mins,
                        overrideTotalNotifications = (mins * 1.5).toInt(),
                        overrideTotalOpens = (mins * 0.8).toInt()
                    )
                )

                if (i == 5 || dStr == todayStr) {
                    val apps = listOf(
                        AppUsageEntity(
                            date = dStr,
                            packageName = "com.brave.browser",
                            appName = "Brave",
                            screenTimeMinutes = 14,
                            notifications = 12,
                            opens = 8,
                            iconCategory = "BRAVE",
                            sortOrder = 1
                        ),
                        AppUsageEntity(
                            date = dStr,
                            packageName = "com.google.android.gm",
                            appName = "Gmail",
                            screenTimeMinutes = 7,
                            notifications = 15,
                            opens = 10,
                            iconCategory = "GMAIL",
                            sortOrder = 2
                        ),
                        AppUsageEntity(
                            date = dStr,
                            packageName = "com.android.chrome",
                            appName = "Chrome",
                            screenTimeMinutes = 6,
                            notifications = 5,
                            opens = 4,
                            iconCategory = "CHROME",
                            sortOrder = 3
                        ),
                        AppUsageEntity(
                            date = dStr,
                            packageName = "com.google.android.youtube",
                            appName = "YouTube",
                            screenTimeMinutes = 12,
                            notifications = 20,
                            opens = 6,
                            iconCategory = "YOUTUBE",
                            sortOrder = 4
                        ),
                        AppUsageEntity(
                            date = dStr,
                            packageName = "com.instagram.android",
                            appName = "Instagram",
                            screenTimeMinutes = 7,
                            notifications = 33,
                            opens = 14,
                            iconCategory = "INSTAGRAM",
                            sortOrder = 5
                        )
                    )
                    dao.insertAppUsages(apps)
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    fun getTodayDateString(): String = dateFormat.format(Date())

    fun getDailySummaryFlow(dateStr: String): Flow<DailySummaryModel> {
        return dao.getDailyUsage(dateStr).map { dailyEntity ->
            val isOverride = dailyEntity?.isCustomOverride ?: true

            val apps = if (!isOverride && hasUsagePermission()) {
                fetchRealSystemUsage(dateStr)
            } else {
                dao.getAppUsageForDateSync(dateStr).map { entity ->
                    AppUsageModel(
                        id = entity.id,
                        date = entity.date,
                        packageName = entity.packageName,
                        appName = entity.appName,
                        screenTimeMinutes = entity.screenTimeMinutes,
                        notifications = entity.notifications,
                        opens = entity.opens,
                        iconCategory = entity.iconCategory,
                        sortOrder = entity.sortOrder,
                        hasTimerLimit = entity.hasTimerLimit
                    )
                }
            }

            val dateObj = parseDate(dateStr)
            val displayDate = displayFormat.format(dateObj)

            DailySummaryModel(
                dateString = dateStr,
                displayDate = displayDate,
                totalScreenTimeMinutes = if (isOverride) (dailyEntity?.overrideTotalScreenTimeMinutes ?: apps.sumOf { it.screenTimeMinutes }) else apps.sumOf { it.screenTimeMinutes },
                totalNotifications = if (isOverride) (dailyEntity?.overrideTotalNotifications ?: apps.sumOf { it.notifications }) else apps.sumOf { it.notifications },
                totalOpens = if (isOverride) (dailyEntity?.overrideTotalOpens ?: apps.sumOf { it.opens }) else apps.sumOf { it.opens },
                isOverride = isOverride,
                appUsages = apps
            )
        }
    }

    private fun fetchRealSystemUsage(dateStr: String): List<AppUsageModel> {
        val pm = context.packageManager
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val cal = Calendar.getInstance()
        try {
            cal.time = dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startTime = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endTime = cal.timeInMillis

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            ?: return emptyList()

        val packageTimeMap = mutableMapOf<String, Long>()
        for (usage in stats) {
            if (usage.totalTimeInForeground > 0) {
                packageTimeMap[usage.packageName] = (packageTimeMap[usage.packageName] ?: 0L) + usage.totalTimeInForeground
            }
        }

        val launchCountMap = mutableMapOf<String, Int>()
        try {
            val events = usm.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    val pkg = event.packageName
                    launchCountMap[pkg] = (launchCountMap[pkg] ?: 0) + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val resultList = mutableListOf<AppUsageModel>()
        var sortIdx = 1

        val sortedEntries = packageTimeMap.entries
            .filter { it.value >= 60000L } // at least 1 minute
            .sortedByDescending { it.value }

        for ((pkgName, timeMs) in sortedEntries) {
            val appLabel = try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                continue
            }

            val mins = (timeMs / 60000L).toInt()
            val opens = launchCountMap[pkgName] ?: (mins / 3).coerceAtLeast(1)
            val notifs = (mins * 1.2).toInt()

            resultList.add(
                AppUsageModel(
                    date = dateStr,
                    packageName = pkgName,
                    appName = appLabel,
                    screenTimeMinutes = mins,
                    notifications = notifs,
                    opens = opens,
                    iconCategory = appLabel.uppercase(),
                    sortOrder = sortIdx++
                )
            )
        }

        return resultList
    }

    suspend fun getWeekChartBars(selectedDateStr: String, metric: MetricType): List<DayChartBarModel> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val selDate = parseDate(selectedDateStr)
        calendar.time = selDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val todayStr = getTodayDateString()
        val bars = mutableListOf<DayChartBarModel>()

        for (i in 0..6) {
            val dStr = dateFormat.format(calendar.time)
            val dayEntity = dao.getDailyUsageSync(dStr)

            val isOverride = dayEntity?.isCustomOverride ?: true
            val apps = if (!isOverride && hasUsagePermission()) {
                fetchRealSystemUsage(dStr)
            } else {
                dao.getAppUsageForDateSync(dStr).map { entity ->
                    AppUsageModel(
                        id = entity.id,
                        date = entity.date,
                        packageName = entity.packageName,
                        appName = entity.appName,
                        screenTimeMinutes = entity.screenTimeMinutes,
                        notifications = entity.notifications,
                        opens = entity.opens,
                        iconCategory = entity.iconCategory,
                        sortOrder = entity.sortOrder,
                        hasTimerLimit = entity.hasTimerLimit
                    )
                }
            }

            val value = when (metric) {
                MetricType.SCREEN_TIME -> if (isOverride) (dayEntity?.overrideTotalScreenTimeMinutes ?: apps.sumOf { it.screenTimeMinutes }) else apps.sumOf { it.screenTimeMinutes }
                MetricType.NOTIFICATIONS -> if (isOverride) (dayEntity?.overrideTotalNotifications ?: apps.sumOf { it.notifications }) else apps.sumOf { it.notifications }
                MetricType.TIMES_OPENED -> if (isOverride) (dayEntity?.overrideTotalOpens ?: apps.sumOf { it.opens }) else apps.sumOf { it.opens }
            }

            bars.add(
                DayChartBarModel(
                    dateString = dStr,
                    dayOfWeekLabel = dayLabelFormat.format(calendar.time),
                    fullDateDisplay = displayFormat.format(calendar.time),
                    totalScreenTimeMinutes = value,
                    isToday = dStr == todayStr,
                    isSelected = dStr == selectedDateStr
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        bars
    }

    suspend fun saveCustomOverride(
        dateStr: String,
        isCustomOverride: Boolean,
        screenTimeMins: Int,
        notifications: Int,
        opens: Int,
        appList: List<AppUsageModel>
    ) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateDailyUsage(
            DailyUsageEntity(
                date = dateStr,
                isCustomOverride = isCustomOverride,
                overrideTotalScreenTimeMinutes = screenTimeMins,
                overrideTotalNotifications = notifications,
                overrideTotalOpens = opens
            )
        )

        dao.deleteAppUsagesForDate(dateStr)
        val entities = appList.mapIndexed { index, app ->
            AppUsageEntity(
                date = dateStr,
                packageName = app.packageName,
                appName = app.appName,
                screenTimeMinutes = app.screenTimeMinutes,
                notifications = app.notifications,
                opens = app.opens,
                iconCategory = app.iconCategory,
                sortOrder = index + 1,
                hasTimerLimit = app.hasTimerLimit
            )
        }
        dao.insertAppUsages(entities)
    }

    suspend fun getSummaryForWidget(): DailySummaryModel = withContext(Dispatchers.IO) {
        val today = getTodayDateString()
        val dailyEntity = dao.getDailyUsageSync(today)
        val isOverride = dailyEntity?.isCustomOverride ?: true

        val apps = if (!isOverride && hasUsagePermission()) {
            fetchRealSystemUsage(today)
        } else {
            dao.getAppUsageForDateSync(today).map { entity ->
                AppUsageModel(
                    id = entity.id,
                    date = entity.date,
                    packageName = entity.packageName,
                    appName = entity.appName,
                    screenTimeMinutes = entity.screenTimeMinutes,
                    notifications = entity.notifications,
                    opens = entity.opens,
                    iconCategory = entity.iconCategory,
                    sortOrder = entity.sortOrder,
                    hasTimerLimit = entity.hasTimerLimit
                )
            }
        }

        DailySummaryModel(
            dateString = today,
            displayDate = "Today",
            totalScreenTimeMinutes = if (isOverride) (dailyEntity?.overrideTotalScreenTimeMinutes ?: apps.sumOf { it.screenTimeMinutes }) else apps.sumOf { it.screenTimeMinutes },
            totalNotifications = if (isOverride) (dailyEntity?.overrideTotalNotifications ?: apps.sumOf { it.notifications }) else apps.sumOf { it.notifications },
            totalOpens = if (isOverride) (dailyEntity?.overrideTotalOpens ?: apps.sumOf { it.opens }) else apps.sumOf { it.opens },
            isOverride = isOverride,
            appUsages = apps
        )
    }

    private fun parseDate(str: String): Date {
        return try {
            dateFormat.parse(str) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
