package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewDue ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY nextReviewDue ASC")
    fun getFlashcardsByDeck(deckName: String): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT deckName FROM flashcards")
    fun getDeckNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE deckName = :deckName")
    suspend fun deleteDeck(deckName: String)
}
