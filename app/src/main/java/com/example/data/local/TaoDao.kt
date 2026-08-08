package com.example.data.local

import androidx.room.*
import com.example.data.model.TaoMeditation
import kotlinx.coroutines.flow.Flow

@Dao
interface TaoDao {
    @Query("SELECT * FROM meditations WHERE day = :day")
    fun getMeditation(day: Int): Flow<TaoMeditation?>

    @Query("SELECT * FROM meditations WHERE day = :day")
    suspend fun getMeditationDirect(day: Int): TaoMeditation?

    @Query("SELECT * FROM meditations ORDER BY day ASC")
    fun getAllMeditations(): Flow<List<TaoMeditation>>

    @Query("SELECT * FROM meditations WHERE isFavorite = 1 ORDER BY day ASC")
    fun getFavorites(): Flow<List<TaoMeditation>>

    @Query("SELECT * FROM meditations WHERE userNote IS NOT NULL AND userNote != '' ORDER BY day ASC")
    fun getJournalEntries(): Flow<List<TaoMeditation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeditation(meditation: TaoMeditation)

    @Update
    suspend fun updateMeditation(meditation: TaoMeditation)
}
