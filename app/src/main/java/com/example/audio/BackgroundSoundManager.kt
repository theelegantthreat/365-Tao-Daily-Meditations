package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class BackgroundSoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundType: String = "SILENT"
    private var currentCustomUri: String? = null
    private var currentVolume: Float = 0.5f

    companion object {
        private const val TAG = "BackgroundSoundManager"
        
        // Curated peaceful streams
        val PRESET_SOUNDS = mapOf(
            "SILENT" to "None (Silence)",
            "BAMBOO_FLUTE" to "Serene Bamboo Flute",
            "MOUNTAIN_STREAM" to "Mountain Stream Water",
            "TEMPLE_BELL" to "Deep Temple Bells",
            "RAIN_SHOWERS" to "Gentle Rain Showers"
        )

        private val SOUND_URLS = mapOf(
            "BAMBOO_FLUTE" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            "MOUNTAIN_STREAM" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            "TEMPLE_BELL" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
            "RAIN_SHOWERS" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3"
        )
    }

    fun play(soundType: String, customUri: String?, volume: Float) {
        currentVolume = volume
        
        if (soundType == "SILENT") {
            stop()
            currentSoundType = "SILENT"
            return
        }

        // Avoid re-initializing if same sound is already playing
        if (currentSoundType == soundType && currentCustomUri == customUri && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.setVolume(volume, volume)
            return
        }

        stop()

        currentSoundType = soundType
        currentCustomUri = customUri

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                isLooping = true
                setVolume(volume, volume)
            }

            if (soundType == "CUSTOM_MP3" && !customUri.isNullOrEmpty()) {
                val uri = Uri.parse(customUri)
                // We must use context contentResolver or let the MediaPlayer resolve it
                mp.setDataSource(context, uri)
            } else {
                val url = SOUND_URLS[soundType] ?: return
                mp.setDataSource(context, Uri.parse(url))
            }

            mp.setOnPreparedListener {
                it.start()
                Log.d(TAG, "Sound playback started successfully for $soundType")
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stop()
                false
            }

            mp.prepareAsync()
            mediaPlayer = mp

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission issue playing custom MP3: ${e.message}")
            stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sound playback: ${e.message}", e)
            stop()
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume
        mediaPlayer?.setVolume(volume, volume)
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    Log.d(TAG, "Sound playback paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing media player: ${e.message}")
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    Log.d(TAG, "Sound playback resumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming media player: ${e.message}")
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun getCurrentSoundType(): String = currentSoundType
}
