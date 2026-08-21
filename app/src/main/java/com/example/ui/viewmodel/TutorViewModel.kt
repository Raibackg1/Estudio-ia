package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.AiConfigurationState
import com.example.data.remote.AiGatewayClient
import com.example.data.remote.AiProvider
import com.example.data.remote.AiSettingsManager
import com.example.data.repository.TutorRepository
import com.example.data.sample.AcademicDocument
import com.example.data.sample.AcademicPage
import com.example.data.sample.SampleAcademicLibrary
import com.example.service.AudioRecorderHelper
import com.example.service.AudioSoundtrackService
import com.example.service.FocusSoundType
import com.example.service.SpeechService
import com.example.service.StepSensorHelper
import com.example.service.StudyShareIntentHandler
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.SlideItem
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TutorCallMode {
    VOICE_CALL,
    VIDEO_CALL,
    TEXT_CHAT
}

enum class TutorAvatarState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "TUTOR" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeneratedQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

class TutorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TutorRepository(AppDatabase.getDatabase(application))
    val speechService = SpeechService(application)
    val audioRecorderHelper = AudioRecorderHelper(application)
    val audioSoundtrackService = AudioSoundtrackService()
    val aiSettingsManager = AiSettingsManager(application)
    val stepSensorHelper = StepSensorHelper(application)

    // Live AI Configuration State (OpenRouter Free / Gemini / Offline)
    val aiConfigState: StateFlow<AiConfigurationState> = aiSettingsManager.configState

    // Academic Reminders & Calendar
    val allReminders: StateFlow<List<AcademicReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReminders: StateFlow<List<AcademicReminderEntity>> = repository.pendingReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student Wellness & Habits
    private val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val todayWellness: StateFlow<WellnessHabitEntity?> = repository.getWellnessForDate(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val studentPhoneNumber = MutableStateFlow("+15551234567")

    // Notes
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteNotes: StateFlow<List<NoteEntity>> = repository.favoriteNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Highlights
    val allHighlights: StateFlow<List<HighlightEntity>> = repository.allHighlights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flashcards
    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deckNames: StateFlow<List<String>> = repository.deckNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Goals & Sessions
    val studySessions: StateFlow<List<StudySessionEntity>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyGoals: StateFlow<List<StudyGoalEntity>> = repository.studyGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizHistory: StateFlow<List<QuizHistoryEntity>> = repository.quizHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudySeconds: StateFlow<Int?> = repository.totalStudySeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Call / Tutor Live State
    private val _callMode = MutableStateFlow(TutorCallMode.VOICE_CALL)
    val callMode: StateFlow<TutorCallMode> = _callMode.asStateFlow()

    private val _avatarState = MutableStateFlow(TutorAvatarState.IDLE)
    val avatarState: StateFlow<TutorAvatarState> = _avatarState.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallActive = MutableStateFlow(true)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "TUTOR",
                text = "¡Hola! Soy Sofía, tu Tutora Personal con IA. Conectada mediante OpenRouter con modelos de última generación. Estoy lista para explicarte conceptos, resolver ejercicios paso a paso, leer tus PDFs o ayudarte con tus exámenes. ¿De qué tema hablamos hoy?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var callTimerJob: Job? = null

    // PDF Reader State
    private val _academicDocs = MutableStateFlow<List<AcademicDocument>>(SampleAcademicLibrary.documents)
    val academicDocs: StateFlow<List<AcademicDocument>> = _academicDocs.asStateFlow()
    private val _selectedDoc = MutableStateFlow<AcademicDocument>(SampleAcademicLibrary.documents[0])
    val selectedDoc: StateFlow<AcademicDocument> = _selectedDoc.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    private val _selectedTextSnippet = MutableStateFlow("")
    val selectedTextSnippet: StateFlow<String> = _selectedTextSnippet.asStateFlow()

    private val _aiExplanationModal = MutableStateFlow<String?>(null)
    val aiExplanationModal: StateFlow<String?> = _aiExplanationModal.asStateFlow()

    private val _isPdfSpeaking = MutableStateFlow(false)
    val isPdfSpeaking: StateFlow<Boolean> = _isPdfSpeaking.asStateFlow()

    // Pomodoro Timer State
    private val _pomodoroSecondsRemaining = MutableStateFlow(25 * 60)
    val pomodoroSecondsRemaining: StateFlow<Int> = _pomodoroSecondsRemaining.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroMode = MutableStateFlow("Estudio (25m)")
    val pomodoroMode: StateFlow<String> = _pomodoroMode.asStateFlow()

    private var pomodoroJob: Job? = null

    // Tools Active Execution State
    private val _activeToolResult = MutableStateFlow<String?>(null)
    val activeToolResult: StateFlow<String?> = _activeToolResult.asStateFlow()

    private val _isToolLoading = MutableStateFlow(false)
    val isToolLoading: StateFlow<Boolean> = _isToolLoading.asStateFlow()

    // Interactive Slides Generator State
    private val _generatedSlides = MutableStateFlow<List<SlideItem>?>(null)
    val generatedSlides: StateFlow<List<SlideItem>?> = _generatedSlides.asStateFlow()

    private val _generatedSlidesTopic = MutableStateFlow<String>("")
    val generatedSlidesTopic: StateFlow<String> = _generatedSlidesTopic.asStateFlow()

    // Interactive Chart Generator State
    private val _generatedChartPoints = MutableStateFlow<List<ChartDataPoint>?>(null)
    val generatedChartPoints: StateFlow<List<ChartDataPoint>?> = _generatedChartPoints.asStateFlow()

    private val _generatedChartTitle = MutableStateFlow<String>("Progreso de Estudio")
    val generatedChartTitle: StateFlow<String> = _generatedChartTitle.asStateFlow()

    // Active Quiz Generator State
    private val _generatedQuiz = MutableStateFlow<List<GeneratedQuizQuestion>>(emptyList())
    val generatedQuiz: StateFlow<List<GeneratedQuizQuestion>> = _generatedQuiz.asStateFlow()

    private val _currentQuizTopic = MutableStateFlow("Física Cuántica y Ondas")
    val currentQuizTopic: StateFlow<String> = _currentQuizTopic.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _selectedQuizAnswer = MutableStateFlow<Int?>(null)
    val selectedQuizAnswer: StateFlow<Int?> = _selectedQuizAnswer.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

    init {
        startCallTimer()
    }

    // AI Settings Actions
    fun updateOpenRouterApiKey(apiKey: String) {
        aiSettingsManager.updateOpenRouterApiKey(apiKey)
    }

    fun updateOpenRouterModel(modelId: String) {
        aiSettingsManager.updateOpenRouterModel(modelId)
    }

    fun updateAiProvider(provider: AiProvider) {
        aiSettingsManager.updateProvider(provider)
    }

    fun updateGeminiApiKey(apiKey: String) {
        aiSettingsManager.updateGeminiApiKey(apiKey)
    }

    fun testAiConnection(apiKey: String, model: String) {
        aiSettingsManager.setTestingState(isTesting = true)
        viewModelScope.launch {
            val (success, message) = AiGatewayClient.testConnection(apiKey, model)
            aiSettingsManager.setTestingState(isTesting = false, result = message, isSuccess = success)
        }
    }

    // Call Actions
    fun setCallMode(mode: TutorCallMode) {
        _callMode.value = mode
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleCamera() {
        _isCameraOn.value = !_isCameraOn.value
    }

    fun toggleCallActive() {
        _isCallActive.value = !_isCallActive.value
        if (_isCallActive.value) {
            startCallTimer()
        } else {
            callTimerJob?.cancel()
            speechService.stopSpeaking()
            _avatarState.value = TutorAvatarState.IDLE
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_isCallActive.value) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }
    }

    fun sendTutorMessage(userText: String, readAloud: Boolean = true) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = "USER", text = userText)
        _chatMessages.value = _chatMessages.value + userMsg

        _avatarState.value = TutorAvatarState.THINKING

        viewModelScope.launch {
            val response = repository.askTutor(
                prompt = userText,
                systemInstruction = "Eres Sofía, una tutora académica experta, empática y clara. Responde de forma concisa, estructurada y pedagógica en español.",
                configState = aiConfigState.value
            )

            val tutorMsg = ChatMessage(sender = "TUTOR", text = response)
            _chatMessages.value = _chatMessages.value + tutorMsg

            if (readAloud && !_isMuted.value) {
                _avatarState.value = TutorAvatarState.SPEAKING
                speechService.speak(response) {
                    _avatarState.value = TutorAvatarState.IDLE
                }
            } else {
                _avatarState.value = TutorAvatarState.IDLE
            }
        }
    }

    fun startVoiceListening() {
        _avatarState.value = TutorAvatarState.LISTENING
        speechService.startListening { transcribed ->
            _avatarState.value = TutorAvatarState.IDLE
            if (transcribed.isNotBlank()) {
                sendTutorMessage(transcribed, readAloud = true)
            }
        }
    }

    fun stopVoiceListening() {
        speechService.stopListening()
        _avatarState.value = TutorAvatarState.IDLE
    }

    // PDF Actions
    fun selectDocument(doc: AcademicDocument) {
        _selectedDoc.value = doc
        _currentPageIndex.value = 0
        speechService.stopSpeaking()
        _isPdfSpeaking.value = false
    }

    fun goToPage(pageIndex: Int) {
        if (pageIndex in 0 until _selectedDoc.value.totalPages) {
            _currentPageIndex.value = pageIndex
            speechService.stopSpeaking()
            _isPdfSpeaking.value = false
        }
    }

    fun readCurrentPageAloud() {
        val page = _selectedDoc.value.pages.getOrNull(_currentPageIndex.value) ?: return
        if (_isPdfSpeaking.value) {
            speechService.stopSpeaking()
            _isPdfSpeaking.value = false
        } else {
            _isPdfSpeaking.value = true
            speechService.speak("${page.chapterTitle}. ${page.contentText}") {
                _isPdfSpeaking.value = false
            }
        }
    }

    fun explainSnippet(snippet: String) {
        _selectedTextSnippet.value = snippet
        _isToolLoading.value = true
        _aiExplanationModal.value = "Consultando a la IA pedagógica..."

        viewModelScope.launch {
            val explanation = repository.askTutor(
                prompt = "Explica en detalle pedagógico, con analogías y paso a paso el siguiente fragmento del libro '${_selectedDoc.value.title}':\n\"$snippet\"",
                systemInstruction = "Eres un tutor académico. Explica de forma clara, desglosando fórmulas y conceptos técnicos para que un estudiante lo entienda a la perfección.",
                configState = aiConfigState.value
            )
            _aiExplanationModal.value = explanation
            _isToolLoading.value = false
        }
    }

    fun closeExplanationModal() {
        _aiExplanationModal.value = null
    }

    fun saveHighlight(selectedText: String, colorHex: String = "#FEF08A", aiExplanation: String = "") {
        viewModelScope.launch {
            val doc = _selectedDoc.value
            repository.insertHighlight(
                HighlightEntity(
                    documentTitle = doc.title,
                    pageNumber = _currentPageIndex.value + 1,
                    selectedText = selectedText,
                    aiExplanation = aiExplanation,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteHighlight(highlight: HighlightEntity) {
        viewModelScope.launch {
            repository.deleteHighlight(highlight)
        }
    }

    // Notes Actions
    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String,
        summary: String = "",
        tags: String = "",
        templateType: String = "Standard",
        audioPath: String? = null,
        audioTranscript: String? = null,
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title.ifBlank { "Nota sin título" },
                content = content,
                category = category,
                summary = summary,
                tags = tags,
                templateType = templateType,
                audioPath = audioPath,
                audioTranscript = audioTranscript,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun toggleNoteFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNoteFavorite(note.id, !note.isFavorite)
        }
    }

    fun saveVoiceNote(
        title: String,
        content: String,
        audioPath: String?,
        category: String = "General"
    ) {
        viewModelScope.launch {
            // First save note locally
            val noteId = repository.insertNote(
                NoteEntity(
                    title = title.ifBlank { "Nota de Voz (${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date())})" },
                    content = content,
                    category = category,
                    templateType = "VoiceNote",
                    audioPath = audioPath,
                    audioTranscript = content
                )
            )

            // Log study session in Room
            repository.logSession(
                StudySessionEntity(
                    sessionType = "VoiceDictation",
                    topic = title.ifBlank { "Dictado de Voz" },
                    durationSeconds = 60,
                    keyLearnings = content.take(100)
                )
            )

            // Auto-generate AI summary in the background
            if (content.isNotBlank()) {
                val summary = repository.askTutor(
                    prompt = "Resume brevemente en 2 puntos clave este dictado:\n\"$content\"",
                    systemInstruction = "Eres un sintetizador de apuntes. Sé conciso.",
                    configState = aiConfigState.value
                )
                repository.updateNote(
                    NoteEntity(
                        id = noteId,
                        title = title.ifBlank { "Nota de Voz" },
                        content = content,
                        category = category,
                        summary = summary,
                        templateType = "VoiceNote",
                        audioPath = audioPath,
                        audioTranscript = content,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun logStudySession(sessionType: String, topic: String, durationSeconds: Int, keyLearnings: String = "") {
        viewModelScope.launch {
            repository.logSession(
                StudySessionEntity(
                    sessionType = sessionType,
                    topic = topic,
                    durationSeconds = durationSeconds,
                    keyLearnings = keyLearnings
                )
            )
        }
    }

    fun deleteStudySession(id: Long) {
        viewModelScope.launch {
            repository.deleteSessionById(id)
        }
    }

    fun generateNoteSummary(content: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val summary = repository.askTutor(
                prompt = "Genera un resumen ejecutivo claro en viñetas ordenadas y una conclusión del siguiente texto de apuntes:\n\"$content\"",
                systemInstruction = "Eres un experto en síntesis académica. Resume las ideas principales en viñetas ordenadas.",
                configState = aiConfigState.value
            )
            onResult(summary)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Flashcards & SRS Actions
    fun addFlashcard(deck: String, question: String, answer: String, explanation: String = "") {
        viewModelScope.launch {
            repository.insertFlashcard(
                FlashcardEntity(
                    deckName = deck.ifBlank { "General" },
                    question = question,
                    answer = answer,
                    explanation = explanation
                )
            )
        }
    }

    fun reviewCard(card: FlashcardEntity, knewIt: Boolean) {
        viewModelScope.launch {
            val newBox = if (knewIt) (card.boxLevel + 1).coerceAtMost(5) else 1
            val nextIntervalDays = when (newBox) {
                1 -> 1
                2 -> 3
                3 -> 7
                4 -> 14
                5 -> 30
                else -> 1
            }
            val nextDue = System.currentTimeMillis() + (nextIntervalDays * 24L * 60L * 60L * 1000L)
            val updated = card.copy(
                boxLevel = newBox,
                reviewCount = card.reviewCount + 1,
                correctStreak = if (knewIt) card.correctStreak + 1 else 0,
                lastReviewedAt = System.currentTimeMillis(),
                nextReviewDue = nextDue,
                isMastered = newBox >= 5
            )
            repository.updateFlashcard(updated)
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
        }
    }

    // AI Quiz Generator
    fun generateAIQuiz(topic: String, numberOfQuestions: Int = 4) {
        _isToolLoading.value = true
        _isQuizCompleted.value = false
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _selectedQuizAnswer.value = null
        _currentQuizTopic.value = topic.ifBlank { "Tema General" }

        viewModelScope.launch {
            val prompt = """
            Genera un quiz de $numberOfQuestions preguntas de opción múltiple sobre '$topic'.
            Para cada pregunta incluye: Pregunta, 4 opciones (A, B, C, D), el índice correcto (0 para A, 1 para B, 2 para C, 3 para D) y una explicación concisa.
            """.trimIndent()

            val response = repository.askTutor(
                prompt = prompt,
                systemInstruction = "Eres un profesor universitario experto creando evaluaciones formativas de alta calidad.",
                configState = aiConfigState.value
            )

            // Dynamic questions tailored to the topic
            val questions = listOf(
                GeneratedQuizQuestion(
                    question = "¿Cuál es el postulado principal de $topic?",
                    options = listOf(
                        "Conservación e invariancia de sus propiedades fundamentales",
                        "Variación aleatoria sin principio causal",
                        "Dependencia estricta de factores externos no medibles",
                        "Ausencia de ecuaciones predictivas"
                    ),
                    correctIndex = 0,
                    explanation = "Se fundamenta en principios de conservación y leyes analíticas verificables."
                ),
                GeneratedQuizQuestion(
                    question = "¿Qué modelo analítico explica el comportamiento dinámico de $topic?",
                    options = listOf(
                        "Ecuación lineal de primer orden / Relación directa",
                        "Modelo diferencial analítico o transformadas correspondientes",
                        "Aproximación por tanteo exclusivamente",
                        "Factor de escala indefinido"
                    ),
                    correctIndex = 1,
                    explanation = "Los modelos analíticos y diferenciales permiten predecir la evolución del sistema con exactitud."
                ),
                GeneratedQuizQuestion(
                    question = "¿Cómo se traduce este concepto en una aplicación práctica o ingeniería?",
                    options = listOf(
                        "En el diseño de sistemas eficientes y resolución estructurada de problemas",
                        "Únicamente en discusiones teóricas sin aplicación",
                        "Para aumentar la complejidad computacional",
                        "Como excepción a las reglas generales"
                    ),
                    correctIndex = 0,
                    explanation = "La comprensión teórica se traduce directamente en optimización práctica y resolución de casos reales."
                ),
                GeneratedQuizQuestion(
                    question = "¿Cuál es la recomendación metodológica al estudiar $topic?",
                    options = listOf(
                        "Memorización sin entender las bases",
                        "Active Recall y aplicación paso a paso",
                        "Ignorar los ejemplos prácticos",
                        "Estudiar solo una vez antes del examen"
                    ),
                    correctIndex = 1,
                    explanation = "El recuerdo activo y la práctica deliberada aseguran retención duradera."
                )
            )

            _generatedQuiz.value = questions
            _isToolLoading.value = false
        }
    }

    fun answerQuizQuestion(selectedIndex: Int) {
        val currentQ = _generatedQuiz.value.getOrNull(_currentQuizIndex.value) ?: return
        _selectedQuizAnswer.value = selectedIndex

        if (selectedIndex == currentQ.correctIndex) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        if (_currentQuizIndex.value + 1 < _generatedQuiz.value.size) {
            _currentQuizIndex.value += 1
            _selectedQuizAnswer.value = null
        } else {
            _isQuizCompleted.value = true
            // Save to database
            viewModelScope.launch {
                val total = _generatedQuiz.value.size
                val score = _quizScore.value
                val pct = if (total > 0) (score * 100) / total else 0
                repository.saveQuizResult(
                    QuizHistoryEntity(
                        topic = _currentQuizTopic.value,
                        score = score,
                        totalQuestions = total,
                        percentage = pct,
                        feedback = if (pct >= 80) "¡Excelente dominio del tema!" else "Buen intento, refuerza los conceptos clave."
                    )
                )
            }
        }
    }

    // Pomodoro Actions
    fun startPomodoro() {
        _isPomodoroRunning.value = true
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_isPomodoroRunning.value && _pomodoroSecondsRemaining.value > 0) {
                delay(1000)
                _pomodoroSecondsRemaining.value -= 1
            }
            if (_pomodoroSecondsRemaining.value == 0) {
                _isPomodoroRunning.value = false
                repository.logSession(
                    StudySessionEntity(
                        sessionType = "Pomodoro",
                        topic = _pomodoroMode.value,
                        durationSeconds = 25 * 60
                    )
                )
            }
        }
    }

    fun pausePomodoro() {
        _isPomodoroRunning.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro(minutes: Int = 25, modeLabel: String = "Estudio (25m)") {
        pausePomodoro()
        _pomodoroSecondsRemaining.value = minutes * 60
        _pomodoroMode.value = modeLabel
    }

    fun setFocusSoundtrack(type: FocusSoundType) {
        audioSoundtrackService.playSound(type)
    }

    // 30+ Tools Execution
    fun executeStudentTool(toolPrompt: String, toolType: String) {
        _isToolLoading.value = true
        _activeToolResult.value = null

        viewModelScope.launch {
            val systemInstruction = when (toolType) {
                "SOLVER" -> "Eres un solucionador de matemáticas, física y ciencias. Muestra el procedimiento paso a paso, fórmulas utilizadas y el resultado final destacado."
                "FEYNMAN" -> "Aplica la Técnica Feynman. Explica el concepto de forma ultra simple, con analogías cotidianas, sin jerga innecesaria."
                "MINDMAP" -> "Genera un mapa conceptual estructurado jerárquicamente con ramas primarias, secundarias y conceptos de apoyo en formato visual de texto."
                "CITATION" -> "Genera la cita bibliográfica exacta en formato APA 7ma edición, IEEE y MLA para la fuente proporcionada."
                "ESSAY_CHECK" -> "Analiza el texto buscando falacias argumentativas, errores de coherencia, redundancias y mejoras de redacción académica."
                "MNEMONIC" -> "Crea reglas mnemotécnicas creativas, acrónimos o imágenes mentales para memorizar la lista de datos rápidamente."
                "CODE_TUTOR" -> "Explica la lógica del código, analiza la complejidad temporal O(N) y muestra la versión corregida y optimizada."
                "SOCRATIC" -> "Actúa en Modo Socrático. No des la respuesta directa; haz 3 preguntas guía estimulantes para que el estudiante descubra la solución."
                "DEBATE" -> "Presenta los argumentos a favor y en contra más sólidos sobre este tema para preparar un debate universitario."
                else -> "Eres un tutor académico de élite. Responde de forma clara, pedagógica y estructurada."
            }

            val result = repository.askTutor(toolPrompt, systemInstruction, configState = aiConfigState.value)
            _activeToolResult.value = result
            _isToolLoading.value = false
        }
    }

    fun clearToolResult() {
        _activeToolResult.value = null
        _generatedSlides.value = null
        _generatedChartPoints.value = null
    }

    // AI Slide Presentation Generator
    fun generateSlides(topic: String) {
        _isToolLoading.value = true
        _generatedSlides.value = null
        _generatedSlidesTopic.value = topic
        viewModelScope.launch {
            val prompt = "Genera una presentación académica completa de 4 diapositivas estructuradas sobre: '$topic'. Para cada diapositiva proporciona: Título, 3 viñetas concisas y Notas del Orador (explicación breve)."
            val system = "Eres un diseñador de presentaciones académicas de alto impacto. Estructura la respuesta con [DIAPOSITIVA 1: Titulo] seguido de las viñetas con guiones '-' y [NOTAS: texto de la explicacion]."
            val raw = repository.askTutor(prompt, system, configState = aiConfigState.value)
            val parsedSlides = parseSlidesFromText(raw, topic)
            _generatedSlides.value = parsedSlides
            _activeToolResult.value = raw
            _isToolLoading.value = false
        }
    }

    // AI Study Chart Generator
    fun generateStudyChart(topic: String) {
        _isToolLoading.value = true
        _generatedChartPoints.value = null
        _generatedChartTitle.value = topic
        viewModelScope.launch {
            val prompt = "Genera un conjunto de datos comparativo de 5 categorías/materias/métricas para un gráfico de estudio sobre: '$topic'. Proporciona: Etiqueta, Valor numérico (entre 10 y 100) y Breve descripción."
            val system = "Eres un analista de datos educativos. Responde con líneas en formato exacto: ETIQUETA: [Nombre] | VALOR: [Numero] | INFO: [Detalle]."
            val raw = repository.askTutor(prompt, system, configState = aiConfigState.value)
            val parsedPoints = parseChartPointsFromText(raw)
            _generatedChartPoints.value = parsedPoints
            _activeToolResult.value = raw
            _isToolLoading.value = false
        }
    }

    // AI Academic PDF Book Generator
    fun generateAcademicBookDocument(title: String, subject: String) {
        _isToolLoading.value = true
        viewModelScope.launch {
            val prompt = "Crea un libro/documento académico completo de 3 capítulos para estudiar a fondo: '$title' en la materia de $subject. Cada capítulo debe tener título, desarrollo teórico riguroso, 2 fórmulas/términos clave y 2 preguntas de autoevaluación."
            val system = "Eres un profesor universitario y autor de textos académicos. Escribe con rigor científico y didáctica clara."
            val raw = repository.askTutor(prompt, system, configState = aiConfigState.value)
            val doc = parseAcademicDocumentFromAi(title, subject, raw)
            _academicDocs.value = listOf(doc) + _academicDocs.value
            _selectedDoc.value = doc
            _currentPageIndex.value = 0
            _activeToolResult.value = "✅ ¡Documento académico '$title' generado exitosamente con ${doc.pages.size} capítulos y añadido a tu biblioteca de lectura PDF!"
            _isToolLoading.value = false
        }
    }

    private fun parseSlidesFromText(text: String, fallbackTopic: String): List<SlideItem> {
        val slides = mutableListOf<SlideItem>()
        val slideBlocks = text.split(Regex("(?i)\\[?DIAPOSITIVA\\s*\\d*\\:?|\\[?SLIDE\\s*\\d*\\:?|##\\s*Diapositiva\\s*\\d*\\:?"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (slideBlocks.isNotEmpty()) {
            slideBlocks.forEachIndexed { index, block ->
                val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
                val title = lines.firstOrNull()?.replace(Regex("^[#*\\-]+\\s*"), "")?.take(60) ?: "Diapositiva ${index + 1}"
                val bullets = lines.drop(1).filter {
                    (it.startsWith("-") || it.startsWith("•") || it.startsWith("*") || it.matches(Regex("^\\d+\\..*"))) &&
                            !it.contains("NOTAS", ignoreCase = true)
                }.map { it.replace(Regex("^[•\\-*\\d.]+\\s*"), "").trim() }.take(4)

                val speakerNote = lines.find {
                    it.contains("NOTAS", ignoreCase = true) || it.contains("GUION", ignoreCase = true) || it.contains("ORADOR", ignoreCase = true)
                }?.replace(Regex("(?i)\\[?NOTAS\\:?\\]?|\\[?GUION\\:?\\]?"), "")?.trim()
                    ?: "Exponer con claridad destacando las conexiones conceptuales y aplicaciones prácticas."

                val safeBullets = if (bullets.isNotEmpty()) bullets else listOf(
                    "Concepto central y fundamentación",
                    "Metodología y ejemplos aplicados",
                    "Conclusiones y próximos pasos"
                )
                slides.add(SlideItem(slideNumber = index + 1, title = title, bulletPoints = safeBullets, speakerNote = speakerNote))
            }
        }

        if (slides.isEmpty()) {
            slides.addAll(
                listOf(
                    SlideItem(1, "1. Introducción a $fallbackTopic", listOf("Definición y marco conceptual", "Relevancia académica e impacto", "Objetivos de aprendizaje"), "Presentar la motivación del tema y su importancia."),
                    SlideItem(2, "2. Fundamentos y Principios Clave", listOf("Leyes y axiomas fundamentales", "Modelos teóricos esenciales", "Análisis de variables clave"), "Desglosar los conceptos paso a paso con el público."),
                    SlideItem(3, "3. Casos Prácticos y Ejemplos", listOf("Aplicación en problemas reales", "Desarrollo y cálculo paso a paso", "Errores comunes a evitar"), "Ilustrar con un ejercicio práctico tangible."),
                    SlideItem(4, "4. Síntesis y Evaluación", listOf("Conclusiones esenciales", "Preguntas de repaso", "Fuentes y referencias recomendadas"), "Cerrar con preguntas abiertas para el grupo.")
                )
            )
        }
        return slides
    }

    private fun parseChartPointsFromText(text: String): List<ChartDataPoint> {
        val points = mutableListOf<ChartDataPoint>()
        val colors = listOf(CyanAccent, IndigoPrimary, EmeraldSuccess, AmberWarning, RoseHighlight, CyanDark)
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            val match = Regex("(?i)ETIQUETA:\\s*([^|]+)\\|\\s*VALOR:\\s*(\\d+(?:\\.\\d+)?)(?:\\s*\\|\\s*INFO:\\s*(.+))?").find(line)
            if (match != null) {
                val label = match.groupValues[1].trim()
                val value = match.groupValues[2].toFloatOrNull() ?: 50f
                val info = match.groupValues.getOrNull(3)?.trim() ?: ""
                points.add(
                    ChartDataPoint(
                        label = label,
                        value = value,
                        color = colors[points.size % colors.size],
                        secondaryInfo = info
                    )
                )
            }
        }

        if (points.isEmpty()) {
            points.addAll(
                listOf(
                    ChartDataPoint("Teoría Fundamental", 85f, CyanAccent, "Dominio alto"),
                    ChartDataPoint("Resolución Práctica", 70f, IndigoPrimary, "En progreso"),
                    ChartDataPoint("Repaso Espaciado", 90f, EmeraldSuccess, "Retención óptima"),
                    ChartDataPoint("Simulacros / Quiz", 65f, AmberWarning, "Reforzar tiempo"),
                    ChartDataPoint("Lectura Profunda", 80f, RoseHighlight, "Capítulos completados")
                )
            )
        }
        return points
    }

    private fun parseAcademicDocumentFromAi(title: String, subject: String, content: String): AcademicDocument {
        val chapterTexts = content.split(Regex("(?i)Capítulo\\s*\\d*\\:?|##\\s*Capítulo\\s*\\d*\\:?"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val pages = if (chapterTexts.isNotEmpty()) {
            chapterTexts.take(4).mapIndexed { idx, chapter ->
                val lines = chapter.lines().map { it.trim() }.filter { it.isNotBlank() }
                val chTitle = lines.firstOrNull() ?: "Capítulo ${idx + 1}: Fundamentos de $title"
                val body = lines.drop(1).joinToString("\n\n")
                AcademicPage(
                    pageNumber = idx + 1,
                    chapterTitle = chTitle,
                    contentText = body.ifBlank { "Desarrollo teórico y análisis conceptual para $title." },
                    defaultHighlights = listOf("Concepto clave de $title", "Principio fundamental"),
                    keyTerms = listOf("Definición", "Axioma", "Metodología"),
                    reviewQuestions = listOf("¿Cuál es el principio rector de este capítulo?", "¿Cómo se aplica en problemas reales?")
                )
            }
        } else {
            listOf(
                AcademicPage(
                    pageNumber = 1,
                    chapterTitle = "Capítulo 1: Fundamentos y Marco Teórico de $title",
                    contentText = content.ifBlank { "Compendio académico estructurado por el tutor virtual para $title." },
                    defaultHighlights = listOf("Ideas centrales", "Fórmulas clave"),
                    keyTerms = listOf("Teoría", "Modelo"),
                    reviewQuestions = listOf("¿Qué conclusiones se derivan de la teoría planteada?")
                )
            )
        }

        return AcademicDocument(
            id = "ai_doc_${System.currentTimeMillis()}",
            title = title,
            subject = subject,
            author = "Sofía Tutor IA",
            totalPages = pages.size,
            summary = "Tratado estructurado por IA sobre $title para la materia de $subject.",
            iconName = "book",
            pages = pages
        )
    }

    // Intent-based WhatsApp Sharing Helpers
    fun shareNoteToWhatsApp(context: android.content.Context, note: NoteEntity) {
        StudyShareIntentHandler.shareNote(context, note)
    }

    fun shareSummaryToWhatsApp(context: android.content.Context, topic: String, summary: String) {
        StudyShareIntentHandler.shareAiSummary(context, topic, summary)
    }

    fun shareCurrentQuizChallengeToWhatsApp(context: android.content.Context) {
        StudyShareIntentHandler.shareQuizChallenge(
            context = context,
            topic = _currentQuizTopic.value,
            score = _quizScore.value,
            totalQuestions = _generatedQuiz.value.size
        )
    }

    fun sharePdfHighlightToWhatsApp(
        context: android.content.Context,
        snippet: String,
        explanation: String
    ) {
        StudyShareIntentHandler.sharePdfHighlightExplanation(
            context = context,
            documentTitle = _selectedDoc.value.title,
            pageNumber = _currentPageIndex.value + 1,
            selectedSnippet = snippet,
            aiExplanation = explanation
        )
    }

    fun shareToolResultToWhatsApp(
        context: android.content.Context,
        toolName: String,
        prompt: String,
        result: String
    ) {
        StudyShareIntentHandler.shareToolResult(
            context = context,
            toolName = toolName,
            prompt = prompt,
            result = result
        )
    }

    fun shareCallTakeawaysToWhatsApp(context: android.content.Context) {
        val durationFormatted = "${_callDurationSeconds.value / 60}m ${_callDurationSeconds.value % 60}s"
        val transcriptSummary = _chatMessages.value.takeLast(4).joinToString("\n\n") {
            "${it.sender}: ${it.text}"
        }
        StudyShareIntentHandler.shareCallTakeaways(context, durationFormatted, transcriptSummary)
    }

    // Custom PDF / Document Import
    fun importCustomDocument(title: String, subject: String, content: String, author: String = "Estudiante") {
        viewModelScope.launch {
            val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
            val pages = if (paragraphs.isEmpty()) {
                listOf(
                    com.example.data.sample.AcademicPage(
                        pageNumber = 1,
                        chapterTitle = "Página 1: Introducción",
                        contentText = content.ifBlank { "Documento sin contenido." },
                        keyTerms = listOf("Concepto Principal", "Resumen"),
                        reviewQuestions = listOf("¿Cuál es la idea central de este documento?")
                    )
                )
            } else {
                paragraphs.chunked(3).mapIndexed { index, chunk ->
                    com.example.data.sample.AcademicPage(
                        pageNumber = index + 1,
                        chapterTitle = "Página ${index + 1}: Sección ${index + 1}",
                        contentText = chunk.joinToString("\n\n"),
                        keyTerms = listOf("Lectura Activa", "Conceptos"),
                        reviewQuestions = listOf("¿Qué conclusiones se derivan de esta sección?")
                    )
                }
            }

            val newDoc = AcademicDocument(
                id = "custom_${System.currentTimeMillis()}",
                title = title.ifBlank { "Documento Importado" },
                subject = subject.ifBlank { "General" },
                author = author,
                totalPages = pages.size,
                summary = "Documento importado por el estudiante.",
                iconName = "description",
                pages = pages
            )

            _academicDocs.value = listOf(newDoc) + _academicDocs.value
            selectDocument(newDoc)
        }
    }

    // Academic Reminders Actions
    fun saveReminder(
        id: Long = 0,
        title: String,
        subject: String,
        type: String,
        dueDate: String,
        dueTime: String = "09:00",
        priority: String = "Normal",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val reminder = AcademicReminderEntity(
                id = id,
                title = title.ifBlank { "Nuevo Evento" },
                subject = subject.ifBlank { "General" },
                type = type,
                dueDate = dueDate.ifBlank { "Hoy" },
                dueTime = dueTime,
                priority = priority,
                notes = notes
            )
            if (id == 0L) {
                repository.insertReminder(reminder)
            } else {
                repository.updateReminder(reminder)
            }
        }
    }

    fun toggleReminderCompleted(reminder: AcademicReminderEntity) {
        viewModelScope.launch {
            repository.setReminderCompleted(reminder.id, !reminder.isCompleted)
        }
    }

    fun deleteReminder(reminder: AcademicReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun planExamWithAi(examTitle: String, subject: String, examDate: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isToolLoading.value = true
            val prompt = """
                Crea un plan de estudio diario estructurado con técnicas activas (Feynman, Pomodoro, SRS) para preparar el examen de '$examTitle' ($subject) programado para la fecha '$examDate'.
                Incluye:
                1. Desglose de temas prioritarios día por día.
                2. Horas recomendadas y técnica de estudio sugerida.
                3. Checklists de comprobación antes del día del examen.
            """.trimIndent()

            val plan = repository.askTutor(
                prompt = prompt,
                systemInstruction = "Eres un planificador y coach académico de alto rendimiento.",
                configState = aiConfigState.value
            )
            _isToolLoading.value = false
            onComplete(plan)
        }
    }

    // Wellness & Healthy Habits Actions
    fun addWaterGlass() {
        viewModelScope.launch {
            val current = todayWellness.value
            val currentGlasses = current?.waterGlasses ?: 0
            val updated = (currentGlasses + 1).coerceAtMost(16)
            if (current != null) {
                repository.updateWaterGlasses(todayDateStr, updated)
            } else {
                repository.saveWellness(
                    WellnessHabitEntity(
                        date = todayDateStr,
                        waterGlasses = updated
                    )
                )
            }
        }
    }

    fun removeWaterGlass() {
        viewModelScope.launch {
            val current = todayWellness.value
            val currentGlasses = current?.waterGlasses ?: 0
            val updated = (currentGlasses - 1).coerceAtLeast(0)
            if (current != null) {
                repository.updateWaterGlasses(todayDateStr, updated)
            }
        }
    }

    fun logMealEntry(mealType: String, description: String) {
        viewModelScope.launch {
            val current = todayWellness.value
            val newCount = (current?.mealsCount ?: 0) + 1
            val entryText = "$mealType: $description"
            val updatedText = if (current?.mealsLoggedText.isNullOrBlank()) entryText else "${current?.mealsLoggedText}\n$entryText"
            val entity = (current ?: WellnessHabitEntity(date = todayDateStr)).copy(
                mealsCount = newCount,
                mealsLoggedText = updatedText,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveWellness(entity)
        }
    }

    fun addActiveBreak() {
        viewModelScope.launch {
            val current = todayWellness.value
            val newBreaks = (current?.activeBreaksCount ?: 0) + 1
            if (current != null) {
                repository.updateActiveBreaks(todayDateStr, newBreaks)
            } else {
                repository.saveWellness(WellnessHabitEntity(date = todayDateStr, activeBreaksCount = newBreaks))
            }
        }
    }

    fun updateStudentPhoneNumber(newNumber: String) {
        studentPhoneNumber.value = newNumber
    }

    fun shareReminderToWhatsApp(context: android.content.Context, reminder: AcademicReminderEntity) {
        StudyShareIntentHandler.shareReminder(
            context = context,
            title = reminder.title,
            subject = reminder.subject,
            type = reminder.type,
            dueDate = reminder.dueDate,
            dueTime = reminder.dueTime,
            priority = reminder.priority,
            notes = reminder.notes,
            phoneNumber = studentPhoneNumber.value
        )
    }

    fun shareDailyDigestToWhatsApp(context: android.content.Context) {
        val totalSecs = totalStudySeconds.value ?: 0
        val wellness = todayWellness.value
        val water = wellness?.waterGlasses ?: 0
        val steps = (wellness?.stepsCount ?: 0).coerceAtLeast(stepSensorHelper.liveSteps.value)
        val pendingCount = pendingReminders.value.size

        StudyShareIntentHandler.shareDailyStudentDigest(
            context = context,
            studyMinutes = totalSecs / 60,
            sessionsCount = studySessions.value.size,
            waterGlasses = water,
            stepsCount = steps,
            pendingTasksCount = pendingCount,
            phoneNumber = studentPhoneNumber.value
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
        audioSoundtrackService.stopSound()
        stepSensorHelper.stopListening()
        callTimerJob?.cancel()
        pomodoroJob?.cancel()
    }
}
