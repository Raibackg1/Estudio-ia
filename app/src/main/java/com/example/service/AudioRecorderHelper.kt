package com.example.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

/**
 * Audio recorder for voice notes and voice dictation sessions.
 * Records audio using standard Android MediaRecorder and saves locally to app storage.
 */
class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _maxAmplitude = MutableStateFlow(0)
    val maxAmplitude: StateFlow<Int> = _maxAmplitude.asStateFlow()

    fun startRecording(): File? {
        try {
            stopRecording() // Clean up any active session

            val recordsDir = File(context.filesDir, "voice_notes").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "VoiceNote_${System.currentTimeMillis()}.m4a"
            val file = File(recordsDir, fileName)
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordingDurationSeconds.value = 0
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording: ${e.message}", e)
            _isRecording.value = false
            mediaRecorder?.release()
            mediaRecorder = null
            return null
        }
    }

    fun updateAmplitude(): Int {
        return try {
            val amp = mediaRecorder?.maxAmplitude ?: 0
            _maxAmplitude.value = amp
            amp
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(): File? {
        if (!_isRecording.value) return currentOutputFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping recording: ${e.message}")
        } finally {
            mediaRecorder = null
            _isRecording.value = false
        }
        return currentOutputFile
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignored
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
}
