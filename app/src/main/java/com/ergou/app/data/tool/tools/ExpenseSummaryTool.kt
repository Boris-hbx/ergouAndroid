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

class ExpenseSummaryTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "expense_summary"
    override val description = "查看消费汇总统计。支持按天、周、月统计，可按标签筛选，包含与上期对比"
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
                put("description", "统计周期：today今天、week本周、month本月")
            }
            putJsonObject("tag") {
                put("type", "string")
                put("description", "按标签筛选汇总（可选），如'餐饮'、'交通'")
            }
        }
        put("required", buildJsonArray { })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val period = arguments["period"] ?: "month"
        val tag = arguments["tag"]

        val result = nextApiService.getExpenseSummary(period)
        return result.fold(
            onSuccess = { summary ->
                if (summary.entryCount == 0) {
                    val periodLabel = when (period) {
                        "today" -> "今天"
                        "week" -> "本周"
                        else -> "本月"
                    }
                    return "${periodLabel}暂无消费记录"
                }

                val sb = StringBuilder()

                // If filtering by tag, show only that tag's data
                if (tag != null && !summary.tagTotals.isNullOrEmpty()) {
                    val tagData = summary.tagTotals.find { it.tag == tag }
                    if (tagData != null) {
                        val pct = if (summary.totalAmount > 0) "%.0f".format(tagData.amount / summary.totalAmount * 100) else "0"
                        sb.appendLine("${tag}消费汇总（${summary.period}）：${"%.2f".format(tagData.amount)}，${tagData.count}笔，占比${pct}%")
                    } else {
                        sb.appendLine("${summary.period}没有标签「${tag}」的消费记录")
                    }
                } else {
                    sb.appendLine("消费汇总（${summary.period}）：总计 ${"%.2f".format(summary.totalAmount)}，${summary.entryCount}笔")
                }

                if (tag == null && !summary.tagTotals.isNullOrEmpty()) {
                    sb.appendLine("---")
                    summary.tagTotals.sortedByDescending { it.amount }.forEach { t ->
                        val pct = if (summary.totalAmount > 0) "%.0f".format(t.amount / summary.totalAmount * 100) else "0"
                        sb.appendLine("${t.tag}: ${"%.2f".format(t.amount)} (${pct}%, ${t.count}笔)")
                    }
                }

                // Month-over-month comparison for monthly period
                if (period == "month") {
                    val lastMonth = LocalDate.now().minusMonths(1)
                    val lastMonthDate = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val lastResult = nextApiService.getExpenseSummary("month", date = lastMonthDate)
                    lastResult.fold(
                        onSuccess = { lastSummary ->
                            if (lastSummary.totalAmount > 0) {
                                val change = (summary.totalAmount - lastSummary.totalAmount) / lastSummary.totalAmount * 100
                                val sign = if (change >= 0) "+" else ""
                                sb.appendLine("---")
                                sb.appendLine("环比上月：${sign}${"%.0f".format(change)}%（上月 ${"%.2f".format(lastSummary.totalAmount)}）")
                            }
                        },
                        onFailure = { /* ignore comparison failure */ }
                    )
                }

                sb.toString()
            },
            onFailure = { e -> "获取消费汇总失败：${e.message}" }
        )
    }
}
