package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.QuizHistoryEntity
import com.example.data.local.entity.StudyGoalEntity
import com.example.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE sessionType = :type ORDER BY completedAt DESC")
    fun getSessionsByType(type: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT SUM(durationSeconds) FROM study_sessions")
    fun getTotalStudySeconds(): Flow<Int?>

    @Query("SELECT * FROM study_goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<StudyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: StudyGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: StudyGoalEntity)

    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC")
    fun getAllQuizHistory(): Flow<List<QuizHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(quiz: QuizHistoryEntity): Long
}
