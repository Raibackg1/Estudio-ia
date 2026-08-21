package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data object Ready : SpeechRecognitionState
    data class Listening(val partialText: String = "", val rmsdB: Float = 0f) : SpeechRecognitionState
    data class Success(val recognizedText: String) : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}

class SpeechService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val recognitionState: StateFlow<SpeechRecognitionState> = _recognitionState.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null
    private var onDictationResultCallback: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SpeechService", "Error initializing TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(_speechPitch.value)
            isTtsInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechDoneCallback?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (text.isBlank()) return
        onSpeechDoneCallback = onDone
        if (isTtsInitialized) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TutorAI_${System.currentTimeMillis()}")
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TutorAI_Speech")
            _isSpeaking.value = true
        }
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        if (isTtsInitialized) {
            try {
                tts?.setSpeechRate(rate)
            } catch (e: Exception) {
                Log.e("SpeechService", "Error setting speech rate: ${e.message}")
            }
        }
    }

    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
        if (isTtsInitialized) {
            try {
                tts?.setPitch(pitch)
            } catch (e: Exception) {
                Log.e("SpeechService", "Error setting speech pitch: ${e.message}")
            }
        }
    }

    /**
     * Start Speech-to-Text dictation using Android SpeechRecognizer.
     */
    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            val errMsg = "El servicio de reconocimiento de voz de Android no está disponible."
            _recognitionState.value = SpeechRecognitionState.Error(errMsg)
            onResult(errMsg)
            return
        }

        onDictationResultCallback = onResult
        _spokenText.value = ""
        _recognitionState.value = SpeechRecognitionState.Ready

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _recognitionState.value = SpeechRecognitionState.Listening(partialText = "", rmsdB = 0f)
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = (rmsdB.coerceIn(0f, 10f) / 10f)
                        _rmsDb.value = normalized
                        val current = _recognitionState.value
                        if (current is SpeechRecognitionState.Listening) {
                            _recognitionState.value = current.copy(rmsdB = normalized)
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorDescription = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Error de captura de audio"
                            SpeechRecognizer.ERROR_CLIENT -> "Error en el cliente de voz"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono no otorgado"
                            SpeechRecognizer.ERROR_NETWORK -> "Error de red en el reconocimiento"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera agotado"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó voz clara. Intenta de nuevo."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor está ocupado"
                            SpeechRecognizer.ERROR_SERVER -> "Error en el servidor de reconocimiento"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Sin detección de voz"
                            else -> "Error en el dictado de voz ($error)"
                        }
                        _recognitionState.value = SpeechRecognitionState.Error(errorDescription)
                        Log.w("SpeechService", "SpeechRecognizer error: $errorDescription ($error)")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _spokenText.value = text
                            _recognitionState.value = SpeechRecognitionState.Success(text)
                            onDictationResultCallback?.invoke(text)
                        } else {
                            _recognitionState.value = SpeechRecognitionState.Idle
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            _spokenText.value = partial
                            _recognitionState.value = SpeechRecognitionState.Listening(partialText = partial, rmsdB = _rmsDb.value)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicta tus notas o preguntas para el Tutor...")
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _recognitionState.value = SpeechRecognitionState.Error("Fallo al iniciar dictado: ${e.message}")
            Log.e("SpeechService", "Error in speech recognition: ${e.message}", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            if (_recognitionState.value is SpeechRecognitionState.Listening) {
                val currentText = _spokenText.value
                if (currentText.isNotBlank()) {
                    _recognitionState.value = SpeechRecognitionState.Success(currentText)
                } else {
                    _recognitionState.value = SpeechRecognitionState.Idle
                }
            }
        } catch (e: Exception) {
            Log.e("SpeechService", "Error stopping listening: ${e.message}")
        }
    }

    fun resetState() {
        _recognitionState.value = SpeechRecognitionState.Idle
        _spokenText.value = ""
        _rmsDb.value = 0f
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignored
        }
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignored
        }
    }
}
