package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.TaoMeditation
import com.example.data.model.UserSettings

@Database(entities = [TaoMeditation::class, UserSettings::class], version = 2, exportSchema = false)
abstract class TaoDatabase : RoomDatabase() {
    abstract fun taoDao(): TaoDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TaoDatabase? = null

        fun getDatabase(context: Context): TaoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaoDatabase::class.java,
                    "tao_meditations_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
