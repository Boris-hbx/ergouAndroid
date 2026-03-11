package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextEnglishCreateRequest
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

class GenerateEnglishScenarioTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "generate_english_scenario"
    override val description = "创建一个英语学习场景并通过AI生成内容。支持多分类（英语/编程/职场/生活）"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("title") {
                put("type", "string")
                put("description", "场景标题，如'点餐'、'面试'、'旅游'、'职场邮件'")
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
                put("description", "分类：英语、编程、职场、生活、其他")
            }
            putJsonObject("description") {
                put("type", "string")
                put("description", "场景描述（可选）")
            }
            putJsonObject("difficulty") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("beginner"))
                    add(JsonPrimitive("intermediate"))
                    add(JsonPrimitive("advanced"))
                }
                put("description", "难度：beginner初级、intermediate中级、advanced高级（可选，默认intermediate）")
            }
            putJsonObject("focus") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("vocabulary"))
                    add(JsonPrimitive("grammar"))
                    add(JsonPrimitive("conversation"))
                    add(JsonPrimitive("writing"))
                }
                put("description", "学习重点：vocabulary词汇、grammar语法、conversation对话、writing写作（可选）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("title"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val title = arguments["title"] ?: return "缺少场景标题"
        val category = arguments["category"] ?: "\u82F1\u8BED"
        val difficulty = arguments["difficulty"]
        val focus = arguments["focus"]
        val userDesc = arguments["description"]

        // 将 difficulty/focus 合并到 description 中，供后端 AI 生成时参考
        val descParts = mutableListOf<String>()
        if (userDesc != null) descParts.add(userDesc)
        if (difficulty != null) {
            val diffLabel = when (difficulty) {
                "beginner" -> "初级难度"
                "advanced" -> "高级难度"
                else -> "中级难度"
            }
            descParts.add(diffLabel)
        }
        if (focus != null) {
            val focusLabel = when (focus) {
                "vocabulary" -> "重点:词汇"
                "grammar" -> "重点:语法"
                "conversation" -> "重点:对话"
                "writing" -> "重点:写作"
                else -> ""
            }
            if (focusLabel.isNotBlank()) descParts.add(focusLabel)
        }
        val description = descParts.joinToString(" | ").ifBlank { null }

        return try {
            // Step 1: Create scenario
            val createResult = nextApiService.createScenario(
                NextEnglishCreateRequest(title = title, category = category, description = description)
            )

            val scenario = createResult.getOrElse { return "创建场景失败：${it.message}" }

            // Step 2: Generate content via AI
            val genResult = nextApiService.generateScenario(scenario.id)
            genResult.fold(
                onSuccess = { generated ->
                    val content = generated.content
                    val summary = content?.let {
                        val lines = it.lines().filter { l -> l.isNotBlank() }.take(3)
                        lines.joinToString("\n")
                    } ?: "内容生成中..."
                    "场景已创建并生成内容：${generated.title}（${generated.category}）\n\n$summary"
                },
                onFailure = { e ->
                    "场景已创建（ID:${scenario.id}），但AI内容生成失败：${e.message}"
                }
            )
        } catch (e: Exception) {
            "生成英语场景出错: ${e.message}"
        }
    }
}
