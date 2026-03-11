package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodoUpdateRequest
import com.ergou.app.data.tool.Tool
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class CompleteTaskTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "complete_task"
    override val description = "完成一个任务，将其标记为已完成"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("task_id") {
                put("type", "string")
                put("description", "要完成的任务ID")
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

        val result = nextApiService.updateTodo(
            id = taskId,
            request = NextTodoUpdateRequest(completed = true)
        )

        return result.fold(
            onSuccess = { todo -> "任务「${todo.text}」已完成！" },
            onFailure = { e -> "完成任务失败：${e.message}" }
        )
    }
}
