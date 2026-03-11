package com.ergou.app.data.repository

import com.ergou.app.data.local.dao.SoulDao
import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextSoulState
import com.ergou.app.data.remote.dto.NextSoulStateUpdateRequest
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SoulRepositoryImpl(
    private val soulDao: SoulDao,
    private val nextApiService: NextApiService,
    private val nextAuthProvider: NextAuthProvider
) : SoulRepository {

    companion object {
        private val PARAM_BOUNDS = mapOf(
            "classicalRatio" to (0.6f..1.0f),
            "warmthLevel" to (0.0f..1.0f),
            "verbosityLevel" to (0.0f..1.0f),
            "proactivityLevel" to (0.0f..0.8f),
            "trustLevel" to (0.0f..1.0f)
        )

        /** 后端默认值，用于检测是否需要迁移 */
        private val DEFAULTS = SoulStateEntity()
    }

    override suspend fun getSoulState(): SoulStateEntity {
        return soulDao.getSoulState() ?: SoulStateEntity(
            createdAt = System.currentTimeMillis(),
            lastUpdatedAt = System.currentTimeMillis()
        ).also { soulDao.upsert(it) }
    }

    override fun observeSoulState(): Flow<SoulStateEntity> {
        return soulDao.observeSoulState().map { it ?: getSoulState() }
    }

    override suspend fun getRecentLogs(limit: Int): List<SoulEvolutionLogEntity> {
        return soulDao.getRecentLogs(limit)
    }

    override fun observeRecentLogs(limit: Int): Flow<List<SoulEvolutionLogEntity>> {
        return soulDao.observeRecentLogs(limit)
    }

    override suspend fun resetParameter(name: String) {
        val defaults = SoulStateEntity()
        val defaultValue = getParameterValue(defaults, name)
        val state = getSoulState()
        val oldValue = getParameterValue(state, name)

        if (oldValue == defaultValue) return

        val updated = setParameterValue(state, name, defaultValue)
        soulDao.upsert(updated.copy(lastUpdatedAt = System.currentTimeMillis()))

        soulDao.insertLog(
            SoulEvolutionLogEntity(
                parameterName = name,
                oldValue = oldValue,
                newValue = defaultValue,
                reason = "手动重置",
                triggerSessionId = 0,
                triggerType = "manual_reset",
                createdAt = System.currentTimeMillis()
            )
        )

        Timber.d("[Soul] 参数重置 %s: %.2f → %.2f", name, oldValue, defaultValue)
    }

    override suspend fun resetAllParameters() {
        val state = getSoulState()
        val defaults = SoulStateEntity(
            totalInteractions = state.totalInteractions,
            createdAt = state.createdAt,
            lastUpdatedAt = System.currentTimeMillis()
        )

        PARAM_BOUNDS.keys.forEach { name ->
            val oldValue = getParameterValue(state, name)
            val newValue = getParameterValue(defaults, name)
            if (oldValue != newValue) {
                soulDao.insertLog(
                    SoulEvolutionLogEntity(
                        parameterName = name,
                        oldValue = oldValue,
                        newValue = newValue,
                        reason = "全部重置",
                        triggerSessionId = 0,
                        triggerType = "manual_reset",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }

        soulDao.upsert(defaults)
        Timber.d("[Soul] 全部参数已重置")
    }

    override suspend fun syncFromBackend() {
        // 未登录时跳过
        val token = nextAuthProvider.sessionToken.first()
        if (token.isBlank()) {
            Timber.d("[Soul] 未登录，跳过灵魂状态同步")
            return
        }

        try {
            val localState = getSoulState()

            val remoteSoul = nextApiService.getSoulState().getOrElse { e ->
                Timber.w(e, "[Soul] 拉取后端灵魂状态失败，使用本地缓存")
                return
            }

            // 检测后端是否为默认值（老用户一次性上报）
            if (isBackendDefault(remoteSoul) && isLocalNonDefault(localState)) {
                Timber.d("[Soul] 检测到后端为默认值且本地有数据，执行一次性迁移上报")
                migrateLocalToBackend(localState)
                return
            }

            // 正常流程：用后端数据覆盖本地缓存
            val updated = localState.copy(
                classicalRatio = remoteSoul.classicalRatio.toFloat(),
                warmthLevel = remoteSoul.warmthLevel.toFloat(),
                verbosityLevel = remoteSoul.verbosityLevel.toFloat(),
                proactivityLevel = remoteSoul.proactivityLevel.toFloat(),
                trustLevel = remoteSoul.trustLevel.toFloat(),
                relationshipStage = remoteSoul.relationshipStage,
                totalInteractions = remoteSoul.totalInteractions,
                lastUpdatedAt = System.currentTimeMillis()
            )
            soulDao.upsert(updated)
            Timber.d("[Soul] 已从后端同步灵魂状态 stage=%s interactions=%d", updated.relationshipStage, updated.totalInteractions)
        } catch (e: Exception) {
            Timber.w(e, "[Soul] 灵魂状态同步失败，使用本地缓存")
        }
    }

    private fun isBackendDefault(remote: NextSoulState): Boolean {
        return remote.relationshipStage == "stranger" && remote.totalInteractions == 0
    }

    private fun isLocalNonDefault(local: SoulStateEntity): Boolean {
        return local.relationshipStage != DEFAULTS.relationshipStage
                || local.totalInteractions != DEFAULTS.totalInteractions
                || local.classicalRatio != DEFAULTS.classicalRatio
                || local.warmthLevel != DEFAULTS.warmthLevel
                || local.verbosityLevel != DEFAULTS.verbosityLevel
                || local.proactivityLevel != DEFAULTS.proactivityLevel
                || local.trustLevel != DEFAULTS.trustLevel
    }

    private suspend fun migrateLocalToBackend(local: SoulStateEntity) {
        val request = NextSoulStateUpdateRequest(
            classicalRatio = local.classicalRatio.toDouble(),
            warmthLevel = local.warmthLevel.toDouble(),
            verbosityLevel = local.verbosityLevel.toDouble(),
            proactivityLevel = local.proactivityLevel.toDouble(),
            trustLevel = local.trustLevel.toDouble(),
            relationshipStage = local.relationshipStage,
            totalInteractions = local.totalInteractions
        )

        nextApiService.putSoulState(request).onSuccess { remoteSoul ->
            // 上报成功后用后端返回值更新本地（保持一致）
            val synced = local.copy(
                classicalRatio = remoteSoul.classicalRatio.toFloat(),
                warmthLevel = remoteSoul.warmthLevel.toFloat(),
                verbosityLevel = remoteSoul.verbosityLevel.toFloat(),
                proactivityLevel = remoteSoul.proactivityLevel.toFloat(),
                trustLevel = remoteSoul.trustLevel.toFloat(),
                relationshipStage = remoteSoul.relationshipStage,
                totalInteractions = remoteSoul.totalInteractions,
                lastUpdatedAt = System.currentTimeMillis()
            )
            soulDao.upsert(synced)
            Timber.d("[Soul] 一次性迁移上报成功")
        }.onFailure { e ->
            Timber.w(e, "[Soul] 迁移上报失败，下次启动重试")
        }
    }

    private fun getParameterValue(state: SoulStateEntity, name: String): Float {
        return when (name) {
            "classicalRatio" -> state.classicalRatio
            "warmthLevel" -> state.warmthLevel
            "verbosityLevel" -> state.verbosityLevel
            "proactivityLevel" -> state.proactivityLevel
            "trustLevel" -> state.trustLevel
            else -> 0f
        }
    }

    private fun setParameterValue(state: SoulStateEntity, name: String, value: Float): SoulStateEntity {
        return when (name) {
            "classicalRatio" -> state.copy(classicalRatio = value)
            "warmthLevel" -> state.copy(warmthLevel = value)
            "verbosityLevel" -> state.copy(verbosityLevel = value)
            "proactivityLevel" -> state.copy(proactivityLevel = value)
            "trustLevel" -> state.copy(trustLevel = value)
            else -> state
        }
    }
}
