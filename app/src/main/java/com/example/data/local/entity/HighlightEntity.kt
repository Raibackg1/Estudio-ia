package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentTitle: String,
    val pageNumber: Int,
    val selectedText: String,
    val aiExplanation: String = "",
    val noteComment: String = "",
    val colorHex: String = "#FEF08A",
    val category: String = "Concepto Clave",
    val createdAt: Long = System.currentTimeMillis()
)
