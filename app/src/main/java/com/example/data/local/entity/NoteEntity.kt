package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General", // Matemáticas, Historia, Ciencias, Idiomas, Filosofía, General
    val summary: String = "",
    val tags: String = "", // Comma-separated
    val isFavorite: Boolean = false,
    val templateType: String = "Standard", // Standard, Cornell, Feynman, Q&A
    val audioPath: String? = null,
    val audioTranscript: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
