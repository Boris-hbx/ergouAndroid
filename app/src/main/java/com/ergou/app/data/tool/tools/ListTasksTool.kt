package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
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

class ListTasksTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "list_tasks"
    override val description = "列出Next后端的任务列表。可按时间范围(tab)、四象限(quadrant)筛选，支持包含已完成任务"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("tab") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("today"))
                    add(JsonPrimitive("week"))
                    add(JsonPrimitive("month"))
                }
                put("description", "筛选范围：today/week/month，不传则返回全部")
            }
            putJsonObject("include_completed") {
                put("type", "boolean")
                put("description", "是否包含已完成任务，默认false只返回未完成的")
            }
            putJsonObject("quadrant") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("urgent-important"))
                    add(JsonPrimitive("not-urgent-important"))
                    add(JsonPrimitive("urgent-not-important"))
                    add(JsonPrimitive("not-urgent-not-important"))
                }
                put("description", "按四象限筛选：urgent-important/not-urgent-important/urgent-not-important/not-urgent-not-important")
            }
        }
        put("required", buildJsonArray { })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val tab = arguments["tab"]
        val includeCompleted = arguments["include_completed"]?.lowercase() == "true"
        val quadrant = arguments["quadrant"]

        val result = nextApiService.getTodos(tab)
        return result.fold(
            onSuccess = { todos ->
                var filtered = todos.filter { !it.deleted }
                if (!includeCompleted) {
                    filtered = filtered.filter { !it.completed }
                }
                if (quadrant != null) {
                    filtered = filtered.filter { it.quadrant == quadrant }
                }

                if (filtered.isEmpty()) {
                    val scope = tab?.let { "${it}范围的" } ?: ""
                    val quadrantLabel = quadrant?.let { QuadrantMapper.toChineseLabel(it) + "的" } ?: ""
                    return@fold "当前没有${scope}${quadrantLabel}${if (includeCompleted) "" else "未完成"}任务"
                }

                val sb = StringBuilder()
                sb.appendLine("任务列表（${filtered.size}项）：")
                filtered.forEach { todo ->
                    val icon = if (todo.completed) "✅" else "⬜"
                    val label = QuadrantMapper.toChineseLabel(todo.quadrant)
                    val tagsStr = todo.tags?.joinToString(",") ?: ""
                    val dueDateStr = todo.dueDate?.let { " | 截止:$it" } ?: ""
                    sb.appendLine("$icon [${todo.id}] ${todo.text} | $label | ${todo.tab} | 进度${todo.progress}%$dueDateStr${if (tagsStr.isNotBlank()) " | 标签:$tagsStr" else ""}")
                }
                sb.toString()
            },
            onFailure = { e -> "获取任务列表失败：${e.message}" }
        )
    }
}
