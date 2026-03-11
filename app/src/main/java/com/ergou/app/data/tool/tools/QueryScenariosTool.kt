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
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class QueryScenariosTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "query_scenarios"
    override val description = "查询学习场景列表。可按类别、状态筛选"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("category") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("\u82F1\u8BED"))
                    add(JsonPrimitive("\u7F16\u7A0B"))
                    add(JsonPrimitive("\u804C\u573A"))
                    add(JsonPrimitive("\u751F\u6D3B"))
                    add(JsonPrimitive("\u5176\u4ED6"))
                }
                put("description", "按分类筛选：英语、编程、职场、生活、其他（可选）")
            }
            putJsonObject("status") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("draft"))
                    add(JsonPrimitive("ready"))
                    add(JsonPrimitive("archived"))
                }
                put("description", "按状态筛选：draft草稿、ready已生成、archived已归档（可选）")
            }
            putJsonObject("limit") {
                put("type", "string")
                put("description", "返回数量限制，默认5")
            }
        }
        put("required", buildJsonArray { })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val category = arguments["category"]
        val status = arguments["status"]
        val limit = arguments["limit"]?.toIntOrNull() ?: 5
        val archived = status == "archived"

        val result = nextApiService.getScenarios(archived = archived, category = category)
        return result.fold(
            onSuccess = { scenarios ->
                var filtered = scenarios
                if (status != null && status != "archived") {
                    val apiStatus = if (status == "generated") "ready" else status
                    filtered = filtered.filter { it.status == apiStatus }
                }
                filtered = filtered.take(limit)

                if (filtered.isEmpty()) {
                    val catLabel = category?.let { "${it}类别的" } ?: ""
                    val statusLabel = status?.let {
                        when (it) {
                            "draft" -> "草稿"
                            "ready" -> "已生成"
                            "archived" -> "已归档"
                            else -> ""
                        } + "状态的"
                    } ?: ""
                    return@fold "没有${catLabel}${statusLabel}场景"
                }

                val sb = StringBuilder("学习场景（${filtered.size}项）：\n")
                filtered.forEach { s ->
                    val catLabel = s.category.ifBlank { "英语" }
                    val statusLabel = when (s.status) {
                        "ready" -> "已生成"
                        "generating" -> "生成中"
                        "error" -> "生成失败"
                        else -> "草稿"
                    }
                    val contentPreview = s.content?.lines()?.firstOrNull { it.isNotBlank() }?.take(50) ?: ""
                    sb.appendLine("[${s.id}] ${s.title} | $catLabel | $statusLabel${if (contentPreview.isNotBlank()) " | $contentPreview..." else ""}")
                }
                sb.toString()
            },
            onFailure = { e -> "查询场景失败：${e.message}" }
        )
    }
}
