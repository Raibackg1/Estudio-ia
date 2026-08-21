package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String, // Pomodoro, TutorCall, VideoCall, PdfReading, Quiz, ProblemSolving
    val topic: String,
    val durationSeconds: Int,
    val keyLearnings: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_goals")
data class StudyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetMinutes: Int,
    val completedMinutes: Int = 0,
    val deadlineDate: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val feedback: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
