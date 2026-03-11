package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.ui.trip.TripStatus
import com.ergou.app.ui.trip.getTripStatus
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class QueryTripsTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "query_trips"
    override val description = "查询出差列表，显示各出差的目的地、日期、总金额、费用项数和报销统计。支持按状态筛选"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("status") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("ongoing"))
                    add(JsonPrimitive("completed"))
                    add(JsonPrimitive("planned"))
                }
                put("description", "按状态筛选：ongoing进行中、completed已结束、planned计划中（可选，默认全部）")
            }
        }
        put("required", buildJsonArray { })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val statusFilter = arguments["status"]

        val result = nextApiService.getTrips()
        return result.fold(
            onSuccess = { trips ->
                val filtered = if (statusFilter != null) {
                    val targetStatus = when (statusFilter) {
                        "ongoing" -> TripStatus.ONGOING
                        "completed" -> TripStatus.COMPLETED
                        "planned" -> TripStatus.PLANNED
                        else -> null
                    }
                    if (targetStatus != null) trips.filter { getTripStatus(it) == targetStatus } else trips
                } else {
                    trips
                }

                if (filtered.isEmpty()) {
                    val label = when (statusFilter) {
                        "ongoing" -> "进行中的"
                        "completed" -> "已结束的"
                        "planned" -> "计划中的"
                        else -> ""
                    }
                    return "没有${label}出差记录"
                }

                val sb = StringBuilder("出差列表（${filtered.size}项）：\n")
                filtered.forEach { t ->
                    val status = getTripStatus(t)
                    val dest = if (t.destination != null) " -> ${t.destination}" else ""
                    val dates = listOfNotNull(t.dateFrom, t.dateTo).joinToString(" ~ ")
                    val amount = if (t.totalAmount > 0) " | ${"%.2f".format(t.totalAmount)} ${t.currency}" else ""
                    val itemInfo = if (t.itemCount > 0) " | ${t.itemCount}笔费用" else ""
                    val reimburse = t.reimburseSummary?.let { rs ->
                        if (rs.total > 0) " | 报销:${rs.approved}/${rs.total}笔已批准" else ""
                    } ?: ""
                    sb.appendLine("- [${status.label}] ${t.title}$dest | $dates$amount$itemInfo$reimburse (ID:${t.id})")
                }
                sb.toString()
            },
            onFailure = { e -> "获取出差列表失败：${e.message}" }
        )
    }
}
