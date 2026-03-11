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

class DeleteTaskTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "delete_task"
    override val description = "删除一个任务。请在删除前与用户确认，避免误删"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("task_id") {
                put("type", "string")
                put("description", "要删除的任务ID")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("task_id"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val taskId = arguments["task_id"] ?: return "缺少任务ID"

        val result = nextApiService.deleteTodo(taskId)

        return result.fold(
            onSuccess = { "任务已删除" },
            onFailure = { e -> "删除任务失败：${e.message}" }
        )
    }
}
