package com.ergou.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoulDao {

    @Query("SELECT * FROM soul_state WHERE id = 1")
    suspend fun getSoulState(): SoulStateEntity?

    @Query("SELECT * FROM soul_state WHERE id = 1")
    fun observeSoulState(): Flow<SoulStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SoulStateEntity)

    @Insert
    suspend fun insertLog(log: SoulEvolutionLogEntity)

    @Query("SELECT * FROM soul_evolution_log ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<SoulEvolutionLogEntity>

    @Query("SELECT * FROM soul_evolution_log ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentLogs(limit: Int): Flow<List<SoulEvolutionLogEntity>>

    @Query("SELECT COUNT(*) FROM soul_evolution_log WHERE triggerType = 'auto' AND createdAt > :since")
    suspend fun countAutoAdjustmentsSince(since: Long): Int
}
