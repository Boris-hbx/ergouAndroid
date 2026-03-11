package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.ui.health.HealthData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import timber.log.Timber

class QueryHealthTool(
    private val nextApiService: NextApiService? = null
) : Tool {
    override val name = "query_health"
    override val description = "查询养生功法信息。可按分类（八段锦/易筋经/站桩/经络穴位）或关键词搜索功法条目"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("category") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("baduanjin"))
                    add(JsonPrimitive("yijinjing"))
                    add(JsonPrimitive("zhanzhung"))
                    add(JsonPrimitive("meridians"))
                }
                put("description", "功法分类：baduanjin八段锦、yijinjing易筋经、zhanzhung站桩、meridians经络穴位（可选）")
            }
            putJsonObject("keyword") {
                put("type", "string")
                put("description", "搜索关键词，可匹配名称、功效、穴位（可选）")
            }
        }
        put("required", buildJsonArray {})
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        val category = arguments["category"]
        val keyword = arguments["keyword"]

        // Try API search first if keyword is provided
        if (!keyword.isNullOrBlank() && nextApiService != null) {
            try {
                val result = nextApiService.searchHealth(keyword)
                val apiItems = result.getOrNull()
                if (apiItems != null && apiItems.isNotEmpty()) {
                    val filtered = if (category != null) {
                        apiItems.filter { it.categoryId == category }
                    } else {
                        apiItems
                    }
                    if (filtered.isNotEmpty()) {
                        val results = filtered.map { item ->
                            buildString {
                                append("【${item.categoryId}】${item.name}")
                                append("\n  描述：${item.description}")
                                append("\n  功效：${item.benefits}")
                                if (item.meridians.isNotEmpty()) append("\n  经络：${item.meridians.joinToString("、")}")
                                if (item.keyPoints.isNotEmpty()) append("\n  穴位：${item.keyPoints.joinToString("、")}")
                            }
                        }
                        return "找到 ${results.size} 个功法：\n\n${results.joinToString("\n\n")}"
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "[QueryHealthTool] API 搜索失败，降级到本地数据")
            }
        } else if (category != null && nextApiService != null) {
            // Try API for category listing
            try {
                val result = nextApiService.getHealthItems(category)
                val apiItems = result.getOrNull()
                if (apiItems != null && apiItems.isNotEmpty()) {
                    val results = apiItems.map { item ->
                        buildString {
                            append("【${category}】${item.name}")
                            append("\n  描述：${item.description}")
                            append("\n  功效：${item.benefits}")
                            if (item.meridians.isNotEmpty()) append("\n  经络：${item.meridians.joinToString("、")}")
                            if (item.keyPoints.isNotEmpty()) append("\n  穴位：${item.keyPoints.joinToString("、")}")
                        }
                    }
                    return "找到 ${results.size} 个功法：\n\n${results.joinToString("\n\n")}"
                }
            } catch (e: Exception) {
                Timber.w(e, "[QueryHealthTool] API 获取分类失败，降级到本地数据")
            }
        }

        // Fallback to local HealthData
        return queryLocal(category, keyword?.lowercase())
    }

    private fun queryLocal(category: String?, keyword: String?): String {
        val categories = if (category != null) {
            HealthData.categories.filter { it.id == category }
        } else {
            HealthData.categories
        }

        if (categories.isEmpty()) {
            return "未找到分类：$category，支持的分类：baduanjin/yijinjing/zhanzhung/meridians"
        }

        val results = categories.flatMap { cat ->
            val items = if (keyword.isNullOrBlank()) {
                cat.items
            } else {
                cat.items.filter { item ->
                    item.name.lowercase().contains(keyword) ||
                            item.benefits.lowercase().contains(keyword) ||
                            item.keyPoints.lowercase().contains(keyword) ||
                            item.meridians.lowercase().contains(keyword) ||
                            item.description.lowercase().contains(keyword)
                }
            }
            items.map { item ->
                buildString {
                    append("【${cat.name}】${item.name}")
                    append("\n  描述：${item.description}")
                    append("\n  功效：${item.benefits}")
                    if (item.meridians.isNotBlank()) append("\n  经络：${item.meridians}")
                    if (item.keyPoints.isNotBlank()) append("\n  穴位：${item.keyPoints}")
                }
            }
        }

        if (results.isEmpty()) {
            val searchDesc = buildString {
                if (category != null) append("分类=$category ")
                if (!keyword.isNullOrBlank()) append("关键词=$keyword")
            }
            return "未找到匹配的功法（$searchDesc）"
        }

        return "找到 ${results.size} 个功法：\n\n${results.joinToString("\n\n")}"
    }
}
