package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class QueryExpensesTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "query_expenses"
    override val description = "查询最近的记账条目列表。可按时间范围和标签筛选"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("period") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("today"))
                    add(JsonPrimitive("week"))
                    add(JsonPrimitive("month"))
                }
                put("description", "时间范围：today今天、week本周、month本月，默认month")
            }
            putJsonObject("tag") {
                put("type", "string")
                put("description", "按标签筛选（可选），如'餐饮'")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "返回条目数量上限，默认10")
            }
        }
        put("required", buildJsonArray {})
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val period = arguments["period"] ?: "month"
        val tag = arguments["tag"]
        val limit = arguments["limit"]?.toIntOrNull() ?: 10

        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val (from, to) = when (period) {
            "today" -> today.format(fmt) to today.format(fmt)
            "week" -> {
                val weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                weekStart.format(fmt) to today.format(fmt)
            }
            else -> {
                val monthStart = today.withDayOfMonth(1)
                monthStart.format(fmt) to today.format(fmt)
            }
        }

        val result = nextApiService.getExpenses(from = from, to = to, tags = tag)
        return result.fold(
            onSuccess = { entries ->
                if (entries.isEmpty()) {
                    val periodLabel = when (period) {
                        "today" -> "今天"
                        "week" -> "本周"
                        else -> "本月"
                    }
                    val tagStr = if (tag != null) "标签「${tag}」的" else ""
                    return "${periodLabel}没有${tagStr}消费记录"
                }

                val limited = entries.take(limit)
                val total = entries.sumOf { it.amount }
                val sb = StringBuilder()
                val periodLabel = when (period) {
                    "today" -> "今天"
                    "week" -> "本周"
                    else -> "本月"
                }
                sb.appendLine("${periodLabel}消费记录（共${entries.size}笔，总计${"%.2f".format(total)}）：")
                limited.forEachIndexed { index, entry ->
                    val tagStr = if (!entry.tags.isNullOrEmpty()) " [${entry.tags.joinToString(",")}]" else ""
                    sb.appendLine("${index + 1}. ${entry.date ?: ""} ${entry.notes ?: "消费"} ${"%.2f".format(entry.amount)} ${entry.currency}${tagStr}")
                }
                if (entries.size > limit) {
                    sb.appendLine("...还有 ${entries.size - limit} 笔未显示")
                }
                sb.toString()
            },
            onFailure = { e -> "查询消费记录失败：${e.message}" }
        )
    }
}
