package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class FocusSoundType(val displayName: String, val description: String, val icon: String) {
    NONE("Silencio", "Sin sonido de fondo", "volume_off"),
    BINAURAL_ALPHA("Ondas Alfa (10 Hz)", "Estimula concentración profunda y calma mental", "headphones"),
    LOFI_CHILL("Lofi Acústico Armónico", "Tonos cálidos y suaves para lectura continua", "music_note"),
    RAIN_STUDY("Lluvia Suave", "Ruido de lluvia relajante para aislar ruidos", "water_drop"),
    WHITE_NOISE("Ruido Blanco Suave", "Máscara acústica para máxima concentración", "graphic_eq")
}

class AudioSoundtrackService {

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentSound = MutableStateFlow(FocusSoundType.NONE)
    val currentSound: StateFlow<FocusSoundType> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun playSound(type: FocusSoundType) {
        stopSound()
        if (type == FocusSoundType.NONE) return

        _currentSound.value = type
        _isPlaying.value = true

        synthJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBufferSize * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val buffer = ShortArray(bufferSize / 2)
            var phase1 = 0.0
            var phase2 = 0.0
            var lofiPhase = 0.0

            while (isActive) {
                for (i in buffer.indices) {
                    when (type) {
                        FocusSoundType.BINAURAL_ALPHA -> {
                            // Carrier 200Hz + Beat 10Hz
                            val sample1 = sin(phase1)
                            val sample2 = sin(phase2)
                            phase1 += 2.0 * Math.PI * 200.0 / sampleRate
                            phase2 += 2.0 * Math.PI * 210.0 / sampleRate
                            val mixed = (sample1 + sample2) * 0.15
                            buffer[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
                        }
                        FocusSoundType.LOFI_CHILL -> {
                            // Warm pentatonic chord synthesis (C minor 9th vibe)
                            lofiPhase += 2.0 * Math.PI * 130.81 / sampleRate // C3
                            val h1 = sin(lofiPhase)
                            val h2 = sin(lofiPhase * 1.5) * 0.5 // G3
                            val h3 = sin(lofiPhase * 1.2) * 0.4 // Eb3
                            val warm = (h1 + h2 + h3) * 0.12
                            buffer[i] = (warm * Short.MAX_VALUE).toInt().toShort()
                        }
                        FocusSoundType.RAIN_STUDY -> {
                            // Filtered brown/pink noise simulation
                            val noise = (Random.nextFloat() * 2f - 1f) * 0.18f
                            buffer[i] = (noise * Short.MAX_VALUE).toInt().toShort()
                        }
                        FocusSoundType.WHITE_NOISE -> {
                            val white = (Random.nextFloat() * 2f - 1f) * 0.08f
                            buffer[i] = (white * Short.MAX_VALUE).toInt().toShort()
                        }
                        FocusSoundType.NONE -> {
                            buffer[i] = 0
                        }
                    }
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignored
        }
        audioTrack = null
        _isPlaying.value = false
        _currentSound.value = FocusSoundType.NONE
    }
}
