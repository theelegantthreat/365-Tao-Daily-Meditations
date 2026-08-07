package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BackgroundSoundManager
import com.example.data.local.TaoDatabase
import com.example.data.model.TaoMeditation
import com.example.data.model.UserSettings
import com.example.data.repository.TaoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaoDatabase.getDatabase(application)
    private val repository = TaoRepository(database.taoDao(), database.settingsDao())
    val soundManager = BackgroundSoundManager(application)
    private var isFirstLaunchCollection = true

    // UI States
    private val _activeDay = MutableStateFlow(1)
    val activeDay: StateFlow<Int> = _activeDay.asStateFlow()

    private val _isLoadingMeditation = MutableStateFlow(false)
    val isLoadingMeditation: StateFlow<Boolean> = _isLoadingMeditation.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Persistent User Settings (defaults to standard values)
    val userSettings: StateFlow<UserSettings> = repository.allSettings
        .map { it ?: UserSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    // Single selected meditation day state
    val activeMeditation: StateFlow<TaoMeditation?> = _activeDay
        .flatMapLatest { day ->
            _isLoadingMeditation.value = true
            repository.getMeditation(day)
                .onEach { _isLoadingMeditation.value = false }
                .catch { e ->
                    _errorMessage.value = "Error: ${e.message}"
                    _isLoadingMeditation.value = false
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Master list of 365 days
    val allMeditations: StateFlow<List<TaoMeditation>> = repository.getAll365Meditations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorites list
    val favorites: StateFlow<List<TaoMeditation>> = repository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect and sync settings when VM starts
        viewModelScope.launch {
            userSettings.collect { settings ->
                _activeDay.value = settings.lastActiveDay
                // Automatically manage playback based on settings
                if (isFirstLaunchCollection) {
                    isFirstLaunchCollection = false
                    // Force pause/stop at startup, and ensure settings reflect that it is paused
                    if (settings.isAmbientPlaying) {
                        viewModelScope.launch {
                            repository.saveSettings(settings.copy(isAmbientPlaying = false))
                        }
                    }
                    soundManager.stop()
                } else {
                    if (settings.soundType != "SILENT") {
                        if (settings.isAmbientPlaying) {
                            soundManager.play(settings.soundType, settings.customSoundUri, settings.soundVolume)
                        } else {
                            soundManager.pause()
                        }
                    } else {
                        soundManager.stop()
                    }
                }
            }
        }
    }

    fun selectDay(day: Int) {
        if (day in 1..365) {
            _activeDay.value = day
            viewModelScope.launch {
                val current = userSettings.value
                repository.saveSettings(current.copy(lastActiveDay = day))
            }
        }
    }

    fun toggleFavorite() {
        val currentMeditation = activeMeditation.value ?: return
        viewModelScope.launch {
            val updated = currentMeditation.copy(isFavorite = !currentMeditation.isFavorite)
            repository.updateMeditation(updated)
        }
    }

    fun toggleCompleted() {
        val currentMeditation = activeMeditation.value ?: return
        viewModelScope.launch {
            val updated = currentMeditation.copy(isCompleted = !currentMeditation.isCompleted)
            repository.updateMeditation(updated)
        }
    }

    fun saveUserNote(note: String) {
        val currentMeditation = activeMeditation.value ?: return
        viewModelScope.launch {
            val updated = currentMeditation.copy(userNote = note)
            repository.updateMeditation(updated)
        }
    }

    fun forceRefreshMeditation() {
        val day = _activeDay.value
        _isLoadingMeditation.value = true
        viewModelScope.launch {
            repository.getMeditation(day, forceRefresh = true)
                .catch { e ->
                    _errorMessage.value = "Failed to regenerate: ${e.message}"
                }
                .collect {
                    _isLoadingMeditation.value = false
                }
        }
    }

    fun updateBackgroundStyle(bgType: String, customUri: String?) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(
                current.copy(
                    bgType = bgType,
                    customBgUri = customUri
                )
            )
        }
    }

    fun updateSoundStyle(soundType: String, customUri: String?) {
        viewModelScope.launch {
            val current = userSettings.value
            val updated = current.copy(
                soundType = soundType,
                customSoundUri = customUri,
                isAmbientPlaying = soundType != "SILENT"
            )
            repository.saveSettings(updated)
            
            // Apply immediate playback update
            if (soundType != "SILENT") {
                soundManager.play(soundType, customUri, current.soundVolume)
            } else {
                soundManager.stop()
            }
        }
    }

    fun toggleAmbientPlayback() {
        viewModelScope.launch {
            val current = userSettings.value
            val isPlaying = current.isAmbientPlaying
            val nextPlayingState = !isPlaying
            
            val updated = if (current.soundType == "SILENT" && nextPlayingState) {
                current.copy(soundType = "BAMBOO_FLUTE", isAmbientPlaying = true)
            } else {
                current.copy(isAmbientPlaying = nextPlayingState)
            }
            
            repository.saveSettings(updated)
            
            // Apply immediate player command
            if (updated.soundType == "SILENT") {
                soundManager.stop()
            } else {
                if (nextPlayingState) {
                    if (soundManager.getCurrentSoundType() == updated.soundType) {
                        soundManager.resume()
                    } else {
                        soundManager.play(updated.soundType, updated.customSoundUri, updated.soundVolume)
                    }
                } else {
                    soundManager.pause()
                }
            }
        }
    }

    fun updateVolume(volume: Float) {
        viewModelScope.launch {
            val current = userSettings.value
            repository.saveSettings(current.copy(soundVolume = volume))
            soundManager.setVolume(volume)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stop()
    }
}
