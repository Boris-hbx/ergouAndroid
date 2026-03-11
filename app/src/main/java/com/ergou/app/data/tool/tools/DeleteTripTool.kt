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
import kotlinx.serialization.json.putJsonObject

class DeleteTripTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "delete_trip"
    override val description = "删除一个出差记录及其所有费用明细"
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

        val result = nextApiService.deleteTrip(tripId)

        return result.fold(
            onSuccess = { "出差已删除" },
            onFailure = { e -> "删除出差失败：${e.message}" }
        )
    }
}
