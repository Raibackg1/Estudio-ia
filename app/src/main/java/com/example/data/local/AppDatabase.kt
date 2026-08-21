package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FlashcardDao
import com.example.data.local.dao.HighlightDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.StudyDao
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteEntity::class,
        HighlightEntity::class,
        FlashcardEntity::class,
        StudySessionEntity::class,
        StudyGoalEntity::class,
        QuizHistoryEntity::class,
        AcademicReminderEntity::class,
        WellnessHabitEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun highlightDao(): HighlightDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyDao(): StudyDao
    abstract fun reminderDao(): ReminderDao
    abstract fun wellnessDao(): WellnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutor_ai_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            instance?.let { populateInitialData(it) }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val noteDao = database.noteDao()
            val flashcardDao = database.flashcardDao()
            val highlightDao = database.highlightDao()
            val studyDao = database.studyDao()
            val reminderDao = database.reminderDao()
            val wellnessDao = database.wellnessDao()

            // Seed Reminders
            reminderDao.insertReminder(
                AcademicReminderEntity(
                    title = "Parcial de Física Cuántica",
                    subject = "Física",
                    type = "Examen",
                    dueDate = "2026-08-25",
                    dueTime = "08:30",
                    priority = "Alta",
                    notes = "Entran temas: Dualidad onda-corpúsculo, ecuación de Schrödinger y efecto fotoeléctrico."
                )
            )
            reminderDao.insertReminder(
                AcademicReminderEntity(
                    title = "Entrega de Ensayo Biología Molecular",
                    subject = "Biología",
                    type = "Entrega",
                    dueDate = "2026-08-28",
                    dueTime = "23:59",
                    priority = "Media",
                    notes = "Formato APA 7ma edición, mínimo 5 páginas."
                )
            )
            reminderDao.insertReminder(
                AcademicReminderEntity(
                    title = "Repaso Semanal de Flashcards",
                    subject = "Técnicas de Estudio",
                    type = "Repaso",
                    dueDate = "Hoy",
                    dueTime = "18:00",
                    priority = "Normal",
                    notes = "Completar 15 minutos de repetición espaciada SRS."
                )
            )

            // Seed Wellness today
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            wellnessDao.insertOrUpdate(
                WellnessHabitEntity(
                    date = todayStr,
                    waterGlasses = 4,
                    waterTargetGlasses = 8,
                    mealsCount = 2,
                    mealsLoggedText = "🍳 Desayuno: Avena con frutas\n🥗 Almuerzo: Ensalada con pollo y arroz",
                    stepsCount = 3450,
                    stepsTarget = 6000,
                    activeBreaksCount = 2,
                    energyScore = 4
                )
            )

            // Seed Notes
            noteDao.insertNote(
                NoteEntity(
                    title = "Leyes de la Termodinámica y Entropía",
                    category = "Física",
                    content = "1. Ley Cero: Equilibrio térmico.\n2. Primera Ley: Conservación de la energía (ΔU = Q - W).\n3. Segunda Ley: La entropía de un sistema aislado siempre aumenta con el tiempo.\n4. Tercera Ley: A temperatura de cero absoluto, la entropía alcanza un valor mínimo constante.",
                    summary = "Resumen de las 4 leyes fundamentales que rigen el calor, la energía y el desorden molecular.",
                    tags = "termodinámica,física,energía,entropía",
                    isFavorite = true,
                    templateType = "Cornell"
                )
            )
            noteDao.insertNote(
                NoteEntity(
                    title = "Estructura del ADN y Transcripción Genética",
                    category = "Biología",
                    content = "El ADN posee una doble hélice compuesta por nucleótidos (Adenina, Timina, Citosina, Guanina). Durante la transcripción, la ARN polimerasa sintetiza ARN mensajero a partir de la hebra molde.",
                    summary = "Bases nitrogenadas y el dogma central de la biología molecular.",
                    tags = "adn,genética,biología",
                    isFavorite = false,
                    templateType = "Feynman"
                )
            )

            // Seed Flashcards
            flashcardDao.insertAll(
                listOf(
                    FlashcardEntity(
                        deckName = "Física Universitaria",
                        question = "¿Cuál es la fórmula del Principio de Incertidumbre de Heisenberg?",
                        answer = "Δx · Δp ≥ ℏ / 2",
                        explanation = "Establece que no se puede conocer simultáneamente con precisión arbitraria la posición y el momento de una partícula.",
                        boxLevel = 1
                    ),
                    FlashcardEntity(
                        deckName = "Física Universitaria",
                        question = "¿Qué expresa la Ley de Ohm?",
                        answer = "V = I · R",
                        explanation = "La diferencia de potencial (V) es igual a la corriente (I) multiplicada por la resistencia (R).",
                        boxLevel = 2
                    ),
                    FlashcardEntity(
                        deckName = "Programación & Algoritmos",
                        question = "¿Cuál es la complejidad temporal promedio de QuickSort?",
                        answer = "O(N log N)",
                        explanation = "En el peor de los casos es O(N^2), pero en promedio divide eficientemente el arreglo usando un pivote.",
                        boxLevel = 3
                    ),
                    FlashcardEntity(
                        deckName = "Historia Universal",
                        question = "¿En qué año cayó el Muro de Berlín?",
                        answer = "9 de noviembre de 1989",
                        explanation = "Marcó el fin simbólico de la Guerra Fría y la posterior reunificación de Alemania.",
                        boxLevel = 1
                    )
                )
            )

            // Seed Highlights
            highlightDao.insertHighlight(
                HighlightEntity(
                    documentTitle = "Fundamentos de Física y Mecánica Cuántica",
                    pageNumber = 1,
                    selectedText = "La dualidad onda-corpúsculo postula que todas las partículas cuánticas exhiben propiedades ondulatorias y corpusculares.",
                    aiExplanation = "Significa que la luz y los electrones pueden comportarse como ondas continuas o como paquetes discretos de energía (fotones/partículas) según el experimento.",
                    colorHex = "#FEF08A",
                    category = "Principio Fundamental"
                )
            )

            // Seed Study Goals
            studyDao.insertGoal(
                StudyGoalEntity(
                    title = "Completar repaso de Física Cuántica",
                    targetMinutes = 120,
                    completedMinutes = 45,
                    deadlineDate = "Viernes"
                )
            )
            studyDao.insertGoal(
                StudyGoalEntity(
                    title = "Memorizar 20 Flashcards de Historia",
                    targetMinutes = 60,
                    completedMinutes = 60,
                    isCompleted = true,
                    deadlineDate = "Hoy"
                )
            )
        }
    }
}
