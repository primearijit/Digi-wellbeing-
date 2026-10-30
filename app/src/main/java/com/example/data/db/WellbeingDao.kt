package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WellbeingDao {

    @Query("SELECT * FROM daily_usage_stats WHERE date = :date")
    fun getDailyUsage(date: String): Flow<DailyUsageEntity?>

    @Query("SELECT * FROM daily_usage_stats WHERE date = :date")
    suspend fun getDailyUsageSync(date: String): DailyUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyUsage(entity: DailyUsageEntity)

    @Query("SELECT * FROM app_usage_stats WHERE date = :date ORDER BY sortOrder ASC, screenTimeMinutes DESC")
    fun getAppUsageForDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_stats WHERE date = :date ORDER BY sortOrder ASC, screenTimeMinutes DESC")
    suspend fun getAppUsageForDateSync(date: String): List<AppUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsages(entities: List<AppUsageEntity>)

    @Query("DELETE FROM app_usage_stats WHERE date = :date")
    suspend fun deleteAppUsagesForDate(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(entity: AppUsageEntity)

    @Query("DELETE FROM app_usage_stats WHERE id = :id")
    suspend fun deleteAppUsageById(id: Long)

    @Query("SELECT * FROM app_timers")
    fun getAllTimers(): Flow<List<AppTimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setTimer(timer: AppTimerEntity)
}
