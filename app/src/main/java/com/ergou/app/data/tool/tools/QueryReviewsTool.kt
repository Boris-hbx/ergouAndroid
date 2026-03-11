package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class QueryReviewsTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "query_reviews"
    override val description = "查询例行项列表，显示各例行项的到期状态。可按频率筛选"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("frequency") {
                put("type", "string")
                put("description", "按频率筛选：daily每日、weekly每周、monthly每月、yearly每年。不填则返回全部")
            }
        }
        put("required", buildJsonArray { })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val frequencyFilter = arguments["frequency"]

        val result = nextApiService.getReviews()
        return result.fold(
            onSuccess = { allReviews ->
                val reviews = if (frequencyFilter != null) {
                    allReviews.filter { it.frequency == frequencyFilter }
                } else {
                    allReviews
                }
                if (reviews.isEmpty()) return "没有例行项" + (frequencyFilter?.let { "（$it）" } ?: "")
                val sb = StringBuilder("例行项（${reviews.size}项）：\n")
                reviews.forEach { r ->
                    val freqLabel = when (r.frequency) {
                        "daily" -> "每日"
                        "weekly" -> "每周"
                        "yearly" -> "每年"
                        else -> "每月"
                    }
                    val status = r.dueLabel ?: r.dueStatus ?: ""
                    val pauseLabel = if (r.paused) " [已暂停]" else ""
                    sb.appendLine("- ${r.text} | $freqLabel | $status$pauseLabel (ID:${r.id})")
                }
                sb.toString()
            },
            onFailure = { e -> "获取例行项列表失败：${e.message}" }
        )
    }
}
