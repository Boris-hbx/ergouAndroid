package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextEnglishUpdateRequest
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

class UpdateScenarioTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "update_scenario"
    override val description = "更新学习场景信息，如标题、分类、笔记等"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("scenario_id") {
                put("type", "string")
                put("description", "场景ID（必填）")
            }
            putJsonObject("title") {
                put("type", "string")
                put("description", "新标题（可选）")
            }
            putJsonObject("title_en") {
                put("type", "string")
                put("description", "英文标题（可选）")
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "场景描述（可选）")
            }
            putJsonObject("category") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("\u82F1\u8BED"))
                    add(JsonPrimitive("\u7F16\u7A0B"))
                    add(JsonPrimitive("\u804C\u573A"))
                    add(JsonPrimitive("\u751F\u6D3B"))
                    add(JsonPrimitive("\u5176\u4ED6"))
                }
                put("description", "分类：英语、编程、职场、生活、其他（可选）")
            }
            putJsonObject("notes") {
                put("type", "string")
                put("description", "笔记（可选）")
            }
            putJsonObject("status") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("draft"))
                    add(JsonPrimitive("ready"))
                }
                put("description", "状态：draft草稿、ready已生成（可选）")
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

        val request = NextEnglishUpdateRequest(
            title = arguments["title"],
            titleEn = arguments["title_en"],
            description = arguments["description"],
            category = arguments["category"],
            notes = arguments["notes"],
            status = arguments["status"]
        )

        val result = nextApiService.updateScenario(id, request)
        return result.fold(
            onSuccess = { scenario -> "场景已更新：${scenario.title}" },
            onFailure = { e -> "更新场景失败：${e.message}" }
        )
    }
}
