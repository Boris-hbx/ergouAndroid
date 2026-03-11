package com.ergou.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soul_state")
data class SoulStateEntity(
    @PrimaryKey val id: Int = 1,
    val classicalRatio: Float = 0.9f,
    val warmthLevel: Float = 0.3f,
    val verbosityLevel: Float = 0.3f,
    val proactivityLevel: Float = 0.2f,
    val trustLevel: Float = 0.1f,
    val relationshipStage: String = "stranger",
    val totalInteractions: Int = 0,
    val lastUpdatedAt: Long = 0,
    val createdAt: Long = 0
)
