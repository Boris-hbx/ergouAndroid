package com.ergou.app.data.repository

import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity
import kotlinx.coroutines.flow.Flow

interface SoulRepository {
    suspend fun getSoulState(): SoulStateEntity
    fun observeSoulState(): Flow<SoulStateEntity>
    suspend fun getRecentLogs(limit: Int = 50): List<SoulEvolutionLogEntity>
    fun observeRecentLogs(limit: Int = 50): Flow<List<SoulEvolutionLogEntity>>
    suspend fun resetParameter(name: String)
    suspend fun resetAllParameters()

    /** 从后端拉取灵魂状态并缓存到本地 Room；首次迁移时上报本地数据 */
    suspend fun syncFromBackend()
}
