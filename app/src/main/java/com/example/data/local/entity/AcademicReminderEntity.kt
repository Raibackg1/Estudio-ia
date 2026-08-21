package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_reminders")
data class AcademicReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val type: String, // "Examen", "Entrega", "Tarea", "Clase", "Repaso"
    val dueDate: String, // e.g. "2026-08-25"
    val dueTime: String = "09:00",
    val priority: String = "Normal", // "Alta", "Media", "Normal"
    val isCompleted: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
