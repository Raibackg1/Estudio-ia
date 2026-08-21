package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AcademicReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM academic_reminders ORDER BY isCompleted ASC, dueDate ASC, priority DESC")
    fun getAllReminders(): Flow<List<AcademicReminderEntity>>

    @Query("SELECT * FROM academic_reminders WHERE isCompleted = 0 ORDER BY dueDate ASC, priority DESC")
    fun getPendingReminders(): Flow<List<AcademicReminderEntity>>

    @Query("SELECT * FROM academic_reminders WHERE dueDate = :date ORDER BY priority DESC")
    fun getRemindersForDate(date: String): Flow<List<AcademicReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: AcademicReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: AcademicReminderEntity)

    @Query("UPDATE academic_reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompletedStatus(id: Long, isCompleted: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: AcademicReminderEntity)

    @Query("DELETE FROM academic_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}
