package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.AiConfigurationState
import com.example.data.remote.AiGatewayClient
import com.example.data.sample.AcademicDocument
import com.example.data.sample.SampleAcademicLibrary
import kotlinx.coroutines.flow.Flow

class TutorRepository(private val db: AppDatabase) {

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = db.noteDao().getFavoriteNotes()
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = db.noteDao().getNotesByCategory(category)
    fun searchNotes(query: String): Flow<List<NoteEntity>> = db.noteDao().searchNotes(query)
    suspend fun insertNote(note: NoteEntity): Long = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)
    suspend fun toggleNoteFavorite(id: Long, isFavorite: Boolean) = db.noteDao().updateFavoriteStatus(id, isFavorite)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)
    suspend fun deleteNoteById(id: Long) = db.noteDao().deleteNoteById(id)

    // Highlights
    val allHighlights: Flow<List<HighlightEntity>> = db.highlightDao().getAllHighlights()
    fun getHighlightsByDoc(title: String): Flow<List<HighlightEntity>> = db.highlightDao().getHighlightsByDocument(title)
    suspend fun insertHighlight(highlight: HighlightEntity): Long = db.highlightDao().insertHighlight(highlight)
    suspend fun deleteHighlight(highlight: HighlightEntity) = db.highlightDao().deleteHighlight(highlight)
    suspend fun deleteHighlightById(id: Long) = db.highlightDao().deleteHighlightById(id)

    // Flashcards
    val allFlashcards: Flow<List<FlashcardEntity>> = db.flashcardDao().getAllFlashcards()
    val deckNames: Flow<List<String>> = db.flashcardDao().getDeckNames()
    fun getFlashcardsByDeck(deck: String): Flow<List<FlashcardEntity>> = db.flashcardDao().getFlashcardsByDeck(deck)
    suspend fun insertFlashcard(card: FlashcardEntity): Long = db.flashcardDao().insertFlashcard(card)
    suspend fun updateFlashcard(card: FlashcardEntity) = db.flashcardDao().updateFlashcard(card)
    suspend fun deleteFlashcard(card: FlashcardEntity) = db.flashcardDao().deleteFlashcard(card)

    // Study Sessions & Goals
    val studySessions: Flow<List<StudySessionEntity>> = db.studyDao().getAllSessions()
    fun getSessionsByType(type: String): Flow<List<StudySessionEntity>> = db.studyDao().getSessionsByType(type)
    val studyGoals: Flow<List<StudyGoalEntity>> = db.studyDao().getAllGoals()
    val quizHistory: Flow<List<QuizHistoryEntity>> = db.studyDao().getAllQuizHistory()
    val totalStudySeconds: Flow<Int?> = db.studyDao().getTotalStudySeconds()

    suspend fun logSession(session: StudySessionEntity): Long = db.studyDao().insertSession(session)
    suspend fun deleteSessionById(id: Long) = db.studyDao().deleteSessionById(id)
    suspend fun insertGoal(goal: StudyGoalEntity): Long = db.studyDao().insertGoal(goal)
    suspend fun updateGoal(goal: StudyGoalEntity) = db.studyDao().updateGoal(goal)
    suspend fun deleteGoal(goal: StudyGoalEntity) = db.studyDao().deleteGoal(goal)
    suspend fun saveQuizResult(quiz: QuizHistoryEntity): Long = db.studyDao().insertQuizResult(quiz)

    // Academic Reminders & Calendar
    val allReminders: Flow<List<AcademicReminderEntity>> = db.reminderDao().getAllReminders()
    val pendingReminders: Flow<List<AcademicReminderEntity>> = db.reminderDao().getPendingReminders()
    fun getRemindersForDate(date: String): Flow<List<AcademicReminderEntity>> = db.reminderDao().getRemindersForDate(date)
    suspend fun insertReminder(reminder: AcademicReminderEntity): Long = db.reminderDao().insertReminder(reminder)
    suspend fun updateReminder(reminder: AcademicReminderEntity) = db.reminderDao().updateReminder(reminder)
    suspend fun setReminderCompleted(id: Long, isCompleted: Boolean) = db.reminderDao().setCompletedStatus(id, isCompleted)
    suspend fun deleteReminder(reminder: AcademicReminderEntity) = db.reminderDao().deleteReminder(reminder)
    suspend fun deleteReminderById(id: Long) = db.reminderDao().deleteReminderById(id)

    // Student Wellness & Habits
    fun getWellnessForDate(date: String): Flow<WellnessHabitEntity?> = db.wellnessDao().getWellnessForDate(date)
    val recentWellness: Flow<List<WellnessHabitEntity>> = db.wellnessDao().getRecentWellness()
    suspend fun saveWellness(wellness: WellnessHabitEntity) = db.wellnessDao().insertOrUpdate(wellness)
    suspend fun updateWaterGlasses(date: String, glasses: Int) = db.wellnessDao().updateWater(date, glasses)
    suspend fun updateSteps(date: String, steps: Int) = db.wellnessDao().updateSteps(date, steps)
    suspend fun updateActiveBreaks(date: String, breaks: Int) = db.wellnessDao().updateActiveBreaks(date, breaks)

    // Sample Academic Documents
    fun getAcademicDocuments(): List<AcademicDocument> = SampleAcademicLibrary.documents
    fun getDocumentById(id: String): AcademicDocument? = SampleAcademicLibrary.documents.find { it.id == id }

    // AI Queries via OpenRouter / Gemini / Offline Gateway
    suspend fun askTutor(
        prompt: String,
        systemInstruction: String? = null,
        configState: AiConfigurationState? = null
    ): String {
        val sysPrompt = systemInstruction ?: "Eres un tutor académico de élite, amigable, claro y motivador. Responde en español de forma estructurada, usando viñetas y ejemplos claros."
        return AiGatewayClient.generateContent(
            prompt = prompt,
            systemPrompt = sysPrompt,
            config = configState
        )
    }
}

