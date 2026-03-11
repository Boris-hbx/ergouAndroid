package com.ergou.app.data.repository

import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.Message
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar

/**
 * 主动通知引擎 — 收集各模块待提醒数据，通过 LLM 生成二狗风格的提醒消息
 */
class ProactiveNotifier(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider,
    private val soulRepository: SoulRepository,
    private val llmService: LLMService
) {

    companion object {
        private const val QUIET_HOUR_START = 22
        private const val QUIET_HOUR_END = 8

        private val NOTIFICATION_PROMPT = """
你是二狗，一个有深度有知识的个人AI助手。请根据以下信息生成一条简短的主动关心消息（50字以内），用你平时的说话风格。不要列清单，就像随口跟主人说一句话。如果信息不多，可以简单提醒一下就好。

当前人格状态：
{SOUL_STATE}

待提醒信息：
{REMINDERS}
""".trimIndent()
    }

    /**
     * 是否在安静时段（22:00-8:00 不推送）
     */
    fun isQuietHour(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= QUIET_HOUR_START || hour < QUIET_HOUR_END
    }

    /**
     * 从各模块收集待提醒数据
     */
    suspend fun collectReminders(): List<ReminderContext> {
        // 未登录 Next 则跳过
        val isLoggedIn = authProvider.isLoggedIn.first()
        if (!isLoggedIn) {
            Timber.d("[Proactive] 未登录 Next，跳过数据收集")
            return emptyList()
        }

        val reminders = mutableListOf<ReminderContext>()

        // 1. 今日到期未完成的任务
        try {
            val todosResult = nextApiService.getTodos(tab = "today")
            todosResult.getOrNull()?.let { todos ->
                val pending = todos.filter { !it.completed }
                if (pending.isNotEmpty()) {
                    val names = pending.take(3).joinToString("、") { it.text }
                    val extra = if (pending.size > 3) "等${pending.size}项" else ""
                    reminders.add(
                        ReminderContext(
                            type = "task",
                            summary = "今天还有${pending.size}个任务未完成：$names$extra"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] 获取任务数据失败")
        }

        // 2. 今日未打卡的习惯
        try {
            val routinesResult = nextApiService.getRoutines()
            routinesResult.getOrNull()?.let { routines ->
                val unchecked = routines.filter { !it.completedToday }
                if (unchecked.isNotEmpty()) {
                    val names = unchecked.take(3).joinToString("、") { it.text }
                    val extra = if (unchecked.size > 3) "等${unchecked.size}项" else ""
                    reminders.add(
                        ReminderContext(
                            type = "routine",
                            summary = "今天还有${unchecked.size}个习惯未打卡：$names$extra"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] 获取习惯数据失败")
        }

        // 3. 已逾期的回顾项
        try {
            val reviewsResult = nextApiService.getReviews()
            reviewsResult.getOrNull()?.let { reviews ->
                val overdue = reviews.filter { it.dueStatus == "overdue" && !it.paused }
                if (overdue.isNotEmpty()) {
                    val names = overdue.take(3).joinToString("、") { it.text }
                    reminders.add(
                        ReminderContext(
                            type = "review",
                            summary = "有${overdue.size}个回顾项已逾期：$names"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] 获取回顾数据失败")
        }

        // 4. 本月超预算（通过 expense summary 判断）
        try {
            val summaryResult = nextApiService.getExpenseSummary(period = "month")
            summaryResult.getOrNull()?.let { summary ->
                // 简单阈值：月支出 > 5000 时提醒
                if (summary.totalAmount > 5000.0) {
                    reminders.add(
                        ReminderContext(
                            type = "expense",
                            summary = "本月支出已达${summary.totalAmount.toInt()}元，注意控制开支"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] 获取记账数据失败")
        }

        Timber.d("[Proactive] 收集到 ${reminders.size} 条提醒数据")
        return reminders
    }

    /**
     * 根据收集的提醒数据，让 LLM 生成二狗风格的通知文案
     * 无数据时返回 null（不打扰用户）
     */
    suspend fun generateNotification(reminders: List<ReminderContext>): String? {
        if (reminders.isEmpty()) return null

        try {
            val soulState = soulRepository.getSoulState()
            val stateDesc = """
                warmthLevel=${soulState.warmthLevel}（温度）
                verbosityLevel=${soulState.verbosityLevel}（话痨程度）
                proactivityLevel=${soulState.proactivityLevel}（主动性）
                relationshipStage=${soulState.relationshipStage}（关系阶段）
            """.trimIndent()

            val reminderText = reminders.joinToString("\n") { "- [${it.type}] ${it.summary}" }

            val prompt = NOTIFICATION_PROMPT
                .replace("{SOUL_STATE}", stateDesc)
                .replace("{REMINDERS}", reminderText)

            val messages = listOf(
                Message(role = "system", content = prompt),
                Message(role = "user", content = "请生成一条主动提醒消息。")
            )

            val request = ChatRequest(
                messages = messages,
                stream = false,
                temperature = 0.8,
                maxTokens = 100
            )

            val response = llmService.chat(request)
            val content = response.choices.firstOrNull()?.message?.content?.trim()

            if (content.isNullOrBlank()) {
                Timber.w("[Proactive] LLM 返回空内容")
                return null
            }

            Timber.d("[Proactive] 生成通知: ${content.take(60)}")
            return content
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] LLM 生成通知失败")
            return null
        }
    }
}

/**
 * 待提醒数据上下文
 */
data class ReminderContext(
    val type: String,
    val summary: String
)
