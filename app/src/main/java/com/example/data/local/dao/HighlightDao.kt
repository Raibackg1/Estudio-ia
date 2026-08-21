package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights ORDER BY createdAt DESC")
    fun getAllHighlights(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE documentTitle = :docTitle ORDER BY pageNumber ASC, createdAt ASC")
    fun getHighlightsByDocument(docTitle: String): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteHighlightById(id: Long)
}
