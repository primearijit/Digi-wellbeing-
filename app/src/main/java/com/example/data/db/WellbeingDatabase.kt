package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyUsageEntity::class, AppUsageEntity::class, AppTimerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WellbeingDatabase : RoomDatabase() {
    abstract fun dao(): WellbeingDao

    companion object {
        @Volatile
        private var INSTANCE: WellbeingDatabase? = null

        fun getInstance(context: Context): WellbeingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellbeingDatabase::class.java,
                    "digital_wellbeing_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
