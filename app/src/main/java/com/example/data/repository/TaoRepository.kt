package com.example.data.repository

import com.example.data.local.SettingsDao
import com.example.data.local.TaoDao
import com.example.data.model.TaoMeditation
import com.example.data.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaoRepository(
    private val taoDao: TaoDao,
    private val settingsDao: SettingsDao
) {
    // Settings logic
    val allSettings: Flow<UserSettings?> = settingsDao.getSettings()

    suspend fun saveSettings(settings: UserSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    // Meditation logic: Reactive flow that resolves any day's meditation
    fun getMeditation(day: Int, forceRefresh: Boolean = false): Flow<TaoMeditation> = flow {
        // First check database
        val cached = withContext(Dispatchers.IO) { taoDao.getMeditationDirect(day) }
        
        if (cached != null && !forceRefresh) {
            emit(cached)
            return@flow
        }

        // Emit an initial placeholder while we load/generate if not cached
        val tempSeed = SeededMeditations.getMeditation(day)
        emit(tempSeed)

        // Try to generate via Gemini if day > 10, or even for days 1-10 if forceRefresh
        var generated: TaoMeditation? = null
        if (day > 10 || forceRefresh) {
            generated = GeminiService.generateMeditation(day)
        }

        val resolved = if (generated != null) {
            // Keep user overrides like notes or completion status if doing a refresh
            if (cached != null) {
                generated.copy(
                    isFavorite = cached.isFavorite,
                    userNote = cached.userNote,
                    isCompleted = cached.isCompleted
                )
            } else {
                generated
            }
        } else {
            cached ?: tempSeed
        }

        // Cache the resolved meditation to local DB
        withContext(Dispatchers.IO) {
            taoDao.insertMeditation(resolved)
        }
        
        emit(resolved)
    }.flowOn(Dispatchers.IO)

    // Update individual meditation
    suspend fun updateMeditation(meditation: TaoMeditation) = withContext(Dispatchers.IO) {
        taoDao.updateMeditation(meditation)
    }

    // Direct insert
    suspend fun insertMeditation(meditation: TaoMeditation) = withContext(Dispatchers.IO) {
        taoDao.insertMeditation(meditation)
    }

    // Get only favorites
    fun getFavorites(): Flow<List<TaoMeditation>> {
        return taoDao.getFavorites()
    }

    // Get all meditations with saved journal thoughts
    fun getJournalEntries(): Flow<List<TaoMeditation>> {
        return taoDao.getJournalEntries()
    }

    // Reactively merge the 365-day master list with database-saved states
    fun getAll365Meditations(): Flow<List<TaoMeditation>> {
        return taoDao.getAllMeditations().map { dbList ->
            val dbMap = dbList.associateBy { it.day }
            (1..365).map { day ->
                dbMap[day] ?: SeededMeditations.getMeditation(day)
            }
        }.flowOn(Dispatchers.IO)
    }
}
