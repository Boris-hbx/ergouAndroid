package com.ergou.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soul_evolution_log")
data class SoulEvolutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parameterName: String = "",
    val oldValue: Float = 0f,
    val newValue: Float = 0f,
    val reason: String = "",
    val triggerSessionId: Long = 0,
    val triggerType: String = "",
    val createdAt: Long = 0
)
