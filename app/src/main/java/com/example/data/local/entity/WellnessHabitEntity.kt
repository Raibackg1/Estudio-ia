package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_wellness")
data class WellnessHabitEntity(
    @PrimaryKey val date: String, // e.g. "2026-08-20"
    val waterGlasses: Int = 0, // 0..8 (250ml each)
    val waterTargetGlasses: Int = 8,
    val mealsCount: Int = 0, // 0..4 (Desayuno, Almuerzo, Merienda, Cena)
    val mealsLoggedText: String = "",
    val stepsCount: Int = 0,
    val stepsTarget: Int = 6000,
    val activeBreaksCount: Int = 0,
    val energyScore: Int = 4, // 1..5
    val moodNote: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
