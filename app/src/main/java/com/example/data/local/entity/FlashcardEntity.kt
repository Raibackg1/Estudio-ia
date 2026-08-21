package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckName: String,
    val question: String,
    val answer: String,
    val explanation: String = "",
    val boxLevel: Int = 1, // Leitner box: 1 to 5
    val reviewCount: Int = 0,
    val correctStreak: Int = 0,
    val lastReviewedAt: Long = 0L,
    val nextReviewDue: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
