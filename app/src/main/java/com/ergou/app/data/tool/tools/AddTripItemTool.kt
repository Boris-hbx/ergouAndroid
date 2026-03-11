package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTripItemCreateRequest
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

class AddTripItemTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "add_trip_item"
    override val description = "为出差添加一笔费用明细（机票/火车/酒店/打车/餐饮/会议/通讯/其他）"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("trip_id") {
                put("type", "string")
                put("description", "出差ID")
            }
            putJsonObject("type") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("flight"))
                    add(JsonPrimitive("train"))
                    add(JsonPrimitive("hotel"))
                    add(JsonPrimitive("taxi"))
                    add(JsonPrimitive("meal"))
                    add(JsonPrimitive("meeting"))
                    add(JsonPrimitive("telecom"))
                    add(JsonPrimitive("misc"))
                }
                put("description", "费用类型：flight机票、train火车、hotel酒店、taxi打车、meal餐饮、meeting会议、telecom通讯、misc其他")
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "费用描述")
            }
            putJsonObject("amount") {
                put("type", "number")
                put("description", "金额")
            }
            putJsonObject("date") {
                put("type", "string")
                put("description", "日期 YYYY-MM-DD（可选）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("trip_id"))
            add(JsonPrimitive("description"))
            add(JsonPrimitive("amount"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val tripId = arguments["trip_id"] ?: return "缺少出差ID"
        val description = arguments["description"] ?: return "缺少费用描述"
        val amount = arguments["amount"]?.toDoubleOrNull() ?: return "缺少金额"
        val type = arguments["type"] ?: "misc"
        val date = arguments["date"]

        val result = nextApiService.createTripItem(
            tripId,
            NextTripItemCreateRequest(
                type = type,
                description = description,
                amount = amount,
                date = date
            )
        )

        return result.fold(
            onSuccess = { item ->
                val typeLabel = when (item.type) {
                    "flight" -> "机票"
                    "train" -> "火车"
                    "hotel" -> "酒店"
                    "taxi" -> "打车"
                    "meal" -> "餐饮"
                    "meeting" -> "会议"
                    "telecom" -> "通讯"
                    else -> "其他"
                }
                "费用已添加：$typeLabel - ${item.description} ${"%.2f".format(item.amount)} ${item.currency}"
            },
            onFailure = { e -> "添加费用失败：${e.message}" }
        )
    }
}
