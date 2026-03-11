package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodoUpdateRequest
import com.ergou.app.data.remote.dto.QuadrantMapper
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

class UpdateTaskTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "update_task"
    override val description = "修改一个已有任务的内容，如标题、描述、标签、截止日期、四象限等"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("task_id") {
                put("type", "string")
                put("description", "要修改的任务ID")
            }
            putJsonObject("text") {
                put("type", "string")
                put("description", "新的任务标题（可选）")
            }
            putJsonObject("content") {
                put("type", "string")
                put("description", "新的任务描述（可选）")
            }
            putJsonObject("tags") {
                put("type", "string")
                put("description", "新的标签，逗号分隔（可选）")
            }
            putJsonObject("due_date") {
                put("type", "string")
                put("description", "新的截止日期，格式 YYYY-MM-DD（可选）")
            }
            putJsonObject("quadrant") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("urgent-important"))
                    add(JsonPrimitive("not-urgent-important"))
                    add(JsonPrimitive("urgent-not-important"))
                    add(JsonPrimitive("not-urgent-not-important"))
                }
                put("description", "四象限：urgent-important重要紧急、not-urgent-important重要不紧急、urgent-not-important不重要紧急、not-urgent-not-important不重要不紧急")
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
        val text = arguments["text"]
        val content = arguments["content"]
        val tags = arguments["tags"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        val dueDate = arguments["due_date"]
        val quadrant = arguments["quadrant"]

        if (text == null && content == null && tags == null && dueDate == null && quadrant == null) {
            return "请至少提供一个要修改的字段（text/content/tags/due_date/quadrant）"
        }

        val result = nextApiService.updateTodo(
            id = taskId,
            request = NextTodoUpdateRequest(
                text = text,
                content = content,
                tags = tags,
                dueDate = dueDate,
                quadrant = quadrant
            )
        )

        return result.fold(
            onSuccess = { todo ->
                val label = QuadrantMapper.toChineseLabel(todo.quadrant)
                "任务已更新：${todo.text}${if (label.isNotBlank()) " ($label)" else ""}"
            },
            onFailure = { e -> "修改任务失败：${e.message}" }
        )
    }
}
