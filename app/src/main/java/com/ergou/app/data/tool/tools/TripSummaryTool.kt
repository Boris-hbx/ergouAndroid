package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.ui.trip.getTripStatus
import com.ergou.app.ui.trip.typeToLabel
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class TripSummaryTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "trip_summary"
    override val description = "查看出差详情汇总：行程信息、各类型费用统计、报销状态统计"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("trip_id") {
                put("type", "string")
                put("description", "出差ID")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("trip_id"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val tripId = arguments["trip_id"] ?: return "缺少出差ID"

        val result = nextApiService.getTripById(tripId)
        return result.fold(
            onSuccess = { trip ->
                val status = getTripStatus(trip)
                val sb = StringBuilder()

                // Basic info
                sb.appendLine("出差：${trip.title} [${status.label}]")
                if (trip.destination != null) sb.appendLine("目的地：${trip.destination}")
                val dates = listOfNotNull(trip.dateFrom, trip.dateTo).joinToString(" ~ ")
                if (dates.isNotBlank()) sb.appendLine("日期：$dates")
                if (trip.purpose != null) sb.appendLine("目的：${trip.purpose}")

                sb.appendLine()
                sb.appendLine("总费用：${"%.2f".format(trip.totalAmount)} ${trip.currency}（${trip.itemCount}笔）")

                // Type breakdown
                val items = trip.items ?: emptyList()
                if (items.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("费用分类：")
                    items.groupBy { it.type }
                        .map { (type, typeItems) -> typeToLabel(type) to typeItems.sumOf { it.amount } }
                        .sortedByDescending { it.second }
                        .forEach { (label, amount) ->
                            val pct = if (trip.totalAmount > 0) "%.0f".format(amount / trip.totalAmount * 100) else "0"
                            sb.appendLine("  ${label}: ${"%.2f".format(amount)} ${trip.currency} (${pct}%)")
                        }
                }

                // Reimburse summary
                trip.reimburseSummary?.let { rs ->
                    if (rs.total > 0) {
                        sb.appendLine()
                        sb.appendLine("报销状态（共${rs.total}笔）：")
                        if (rs.approved > 0) sb.appendLine("  已批准：${rs.approved}笔")
                        if (rs.submitted > 0) sb.appendLine("  已提交：${rs.submitted}笔")
                        if (rs.pending > 0) sb.appendLine("  待提交：${rs.pending}笔")
                        if (rs.rejected > 0) sb.appendLine("  已拒绝：${rs.rejected}笔")
                        if (rs.na > 0) sb.appendLine("  无需报销：${rs.na}笔")
                        val ratio = if (rs.total > 0) "%.0f".format(rs.approved.toDouble() / rs.total * 100) else "0"
                        sb.appendLine("  批准率：${ratio}%")
                    }
                }

                sb.toString()
            },
            onFailure = { e -> "获取出差详情失败：${e.message}" }
        )
    }
}
