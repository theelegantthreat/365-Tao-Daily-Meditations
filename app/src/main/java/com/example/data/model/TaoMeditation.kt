package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditations")
data class TaoMeditation(
    @PrimaryKey val day: Int, // 1 to 365
    val title: String,
    val verse: String,
    val commentary: String,
    val isFavorite: Boolean = false,
    val userNote: String = "",
    val isCompleted: Boolean = false
) {
    val formattedDay: String
        get() = String.format("%03d/365", day)
}
