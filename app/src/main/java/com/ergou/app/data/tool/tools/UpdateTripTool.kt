package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTripUpdateRequest
import com.ergou.app.data.tool.Tool
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class UpdateTripTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "update_trip"
    override val description = "更新出差信息，如标题、目的地、日期、目的等"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("trip_id") {
                put("type", "string")
                put("description", "出差ID")
            }
            putJsonObject("title") {
                put("type", "string")
                put("description", "出差标题")
            }
            putJsonObject("destination") {
                put("type", "string")
                put("description", "目的地")
            }
            putJsonObject("date_from") {
                put("type", "string")
                put("description", "开始日期 YYYY-MM-DD")
            }
            putJsonObject("date_to") {
                put("type", "string")
                put("description", "结束日期 YYYY-MM-DD")
            }
            putJsonObject("purpose") {
                put("type", "string")
                put("description", "出差目的")
            }
            putJsonObject("notes") {
                put("type", "string")
                put("description", "备注")
            }
            putJsonObject("currency") {
                put("type", "string")
                put("description", "币种，如 CNY、CAD、USD")
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

        val result = nextApiService.updateTrip(
            tripId,
            NextTripUpdateRequest(
                title = arguments["title"],
                destination = arguments["destination"],
                dateFrom = arguments["date_from"],
                dateTo = arguments["date_to"],
                purpose = arguments["purpose"],
                notes = arguments["notes"],
                currency = arguments["currency"]
            )
        )

        return result.fold(
            onSuccess = { trip -> "出差已更新：${trip.title}" },
            onFailure = { e -> "更新出差失败：${e.message}" }
        )
    }
}
