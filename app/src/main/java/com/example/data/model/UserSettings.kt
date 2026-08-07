package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1, // Fixed ID for single-row settings
    val bgType: String = "MISTY_MOUNTAINS", // MISTY_MOUNTAINS, BAMBOO_GROVE, SOLID, CUSTOM_IMAGE
    val customBgUri: String? = null,
    val soundType: String = "BAMBOO_FLUTE", // SILENT, BAMBOO_FLUTE, MOUNTAIN_STREAM, TEMPLE_BELL, RAIN, CUSTOM_MP3
    val customSoundUri: String? = null,
    val soundVolume: Float = 0.5f,
    val lastActiveDay: Int = 1,
    val isAmbientPlaying: Boolean = false
)
