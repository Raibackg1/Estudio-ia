package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WellnessHabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Query("SELECT * FROM student_wellness WHERE date = :date LIMIT 1")
    fun getWellnessForDate(date: String): Flow<WellnessHabitEntity?>

    @Query("SELECT * FROM student_wellness ORDER BY date DESC LIMIT 7")
    fun getRecentWellness(): Flow<List<WellnessHabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(wellness: WellnessHabitEntity)

    @Query("UPDATE student_wellness SET waterGlasses = :glasses, updatedAt = :time WHERE date = :date")
    suspend fun updateWater(date: String, glasses: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE student_wellness SET stepsCount = :steps, updatedAt = :time WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE student_wellness SET activeBreaksCount = :breaks, updatedAt = :time WHERE date = :date")
    suspend fun updateActiveBreaks(date: String, breaks: Int, time: Long = System.currentTimeMillis())
}
