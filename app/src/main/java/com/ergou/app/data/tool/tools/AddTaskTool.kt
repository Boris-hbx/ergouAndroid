package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodoCreateRequest
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

class AddTaskTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "add_task"
    override val description = "添加一个新任务到Next后端。支持设置四象限、标签、截止日期等"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("text") {
                put("type", "string")
                put("description", "任务标题")
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
            putJsonObject("tab") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("today"))
                    add(JsonPrimitive("week"))
                    add(JsonPrimitive("month"))
                }
                put("description", "时间范围：today今天、week本周、month本月")
            }
            putJsonObject("content") {
                put("type", "string")
                put("description", "任务详细描述（可选）")
            }
            putJsonObject("tags") {
                put("type", "string")
                put("description", "标签，逗号分隔，如'工作,重要'")
            }
            putJsonObject("due_date") {
                put("type", "string")
                put("description", "截止日期，格式 YYYY-MM-DD（可选）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("text"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val text = arguments["text"] ?: return "缺少任务标题"
        val quadrant = arguments["quadrant"] ?: "not-urgent-important"
        val tab = arguments["tab"] ?: "today"
        val content = arguments["content"]
        val tags = arguments["tags"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        val dueDate = arguments["due_date"]

        val result = nextApiService.createTodo(
            NextTodoCreateRequest(
                text = text,
                quadrant = quadrant,
                tab = tab,
                content = content,
                tags = tags,
                dueDate = dueDate
            )
        )

        return result.fold(
            onSuccess = { todo ->
                val label = QuadrantMapper.toChineseLabel(todo.quadrant)
                "任务已添加：${todo.text} (${label}, ${todo.tab}, ID:${todo.id})"
            },
            onFailure = { e -> "添加任务失败：${e.message}" }
        )
    }
}
