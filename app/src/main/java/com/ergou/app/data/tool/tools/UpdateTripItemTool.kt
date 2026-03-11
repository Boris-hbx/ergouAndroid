package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTripItemUpdateRequest
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

class UpdateTripItemTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "update_trip_item"
    override val description = "更新出差费用明细，如类型、描述、金额、报销状态等"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("item_id") {
                put("type", "string")
                put("description", "费用明细ID")
            }
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
            putJsonObject("date") {
                put("type", "string")
                put("description", "日期 YYYY-MM-DD")
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "费用描述")
            }
            putJsonObject("amount") {
                put("type", "string")
                put("description", "金额")
            }
            putJsonObject("currency") {
                put("type", "string")
                put("description", "币种，如 CNY、CAD、USD")
            }
            putJsonObject("reimburse_status") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("pending"))
                    add(JsonPrimitive("submitted"))
                    add(JsonPrimitive("approved"))
                    add(JsonPrimitive("rejected"))
                    add(JsonPrimitive("na"))
                }
                put("description", "报销状态：pending待报销、submitted已提交、approved已批准、rejected已驳回、na不需报销")
            }
            putJsonObject("notes") {
                put("type", "string")
                put("description", "备注")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("item_id"))
            add(JsonPrimitive("trip_id"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val itemId = arguments["item_id"] ?: return "缺少费用明细ID"
        val tripId = arguments["trip_id"] ?: return "缺少出差ID"

        val result = nextApiService.updateTripItem(
            tripId,
            itemId,
            NextTripItemUpdateRequest(
                type = arguments["type"],
                date = arguments["date"],
                description = arguments["description"],
                amount = arguments["amount"]?.toDoubleOrNull(),
                currency = arguments["currency"],
                reimburseStatus = arguments["reimburse_status"],
                notes = arguments["notes"]
            )
        )

        return result.fold(
            onSuccess = { item ->
                "费用已更新：${item.description} ${"%.2f".format(item.amount)} ${item.currency}"
            },
            onFailure = { e -> "更新费用失败：${e.message}" }
        )
    }
}
