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

class DeleteScenarioTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "delete_scenario"
    override val description = "删除一个学习场景"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("scenario_id") {
                put("type", "string")
                put("description", "场景ID（必填）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("scenario_id"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val id = arguments["scenario_id"] ?: return "缺少场景ID"

        val result = nextApiService.deleteScenario(id)
        return result.fold(
            onSuccess = { "场景已删除" },
            onFailure = { e -> "删除场景失败：${e.message}" }
        )
    }
}
