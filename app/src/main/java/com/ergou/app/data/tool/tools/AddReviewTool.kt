package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextReviewCreateRequest
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

class AddReviewTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "add_review"
    override val description = "创建一个例行项（日常习惯或定期事项）。支持设置频率（每日/每周/每月/每年）和分类"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("text") {
                put("type", "string")
                put("description", "回顾项内容")
            }
            putJsonObject("frequency") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("daily"))
                    add(JsonPrimitive("weekly"))
                    add(JsonPrimitive("monthly"))
                    add(JsonPrimitive("yearly"))
                }
                put("description", "频率：daily每日、weekly每周、monthly每月、yearly每年。默认daily")
            }
            putJsonObject("category") {
                put("type", "string")
                put("description", "分类，如'健康'、'财务'、'学习'等")
            }
            putJsonObject("notes") {
                put("type", "string")
                put("description", "备注（可选）")
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

        val text = arguments["text"] ?: return "缺少回顾内容"
        val frequency = arguments["frequency"] ?: "daily"
        val category = arguments["category"]
        val notes = arguments["notes"]

        val result = nextApiService.createReview(
            NextReviewCreateRequest(
                text = text,
                frequency = frequency,
                category = category,
                notes = notes
            )
        )

        return result.fold(
            onSuccess = { review ->
                val freqLabel = when (review.frequency) {
                    "daily" -> "每日"
                    "weekly" -> "每周"
                    "yearly" -> "每年"
                    else -> "每月"
                }
                "例行已创建：${review.text} ($freqLabel, ID:${review.id})"
            },
            onFailure = { e -> "创建回顾失败：${e.message}" }
        )
    }
}
