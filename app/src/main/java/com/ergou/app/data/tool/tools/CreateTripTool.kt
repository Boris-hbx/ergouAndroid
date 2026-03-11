package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTripCreateRequest
import com.ergou.app.data.tool.Tool
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class CreateTripTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "create_trip"
    override val description = "创建一个出差记录。记录标题、目的地、日期范围和目的"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
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
                put("description", "出差目的（可选）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("title"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val title = arguments["title"] ?: return "缺少出差标题"

        val result = nextApiService.createTrip(
            NextTripCreateRequest(
                title = title,
                destination = arguments["destination"],
                dateFrom = arguments["date_from"],
                dateTo = arguments["date_to"],
                purpose = arguments["purpose"]
            )
        )

        return result.fold(
            onSuccess = { trip ->
                val dest = if (trip.destination != null) " → ${trip.destination}" else ""
                "出差已创建：${trip.title}$dest (ID:${trip.id})"
            },
            onFailure = { e -> "创建出差失败：${e.message}" }
        )
    }
}
