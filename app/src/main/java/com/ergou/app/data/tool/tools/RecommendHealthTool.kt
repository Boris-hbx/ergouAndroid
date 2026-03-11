package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.tool.Tool
import com.ergou.app.ui.health.HealthData
import com.ergou.app.ui.health.HealthItem
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import timber.log.Timber

class RecommendHealthTool(
    private val nextApiService: NextApiService? = null
) : Tool {
    override val name = "recommend_health"
    override val description = "根据症状推荐合适的养生功法。输入症状描述（如腰酸、失眠、肩颈僵硬），返回2-3个推荐功法及理由"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("symptom") {
                put("type", "string")
                put("description", "症状描述，如'腰酸'、'失眠'、'肩颈僵硬'、'消化不好'")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("symptom"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        val symptom = arguments["symptom"] ?: return "请描述你的症状"

        // Try API search first
        val apiResults = try {
            val result = nextApiService?.searchHealth(symptom)
            result?.getOrNull()
        } catch (e: Exception) {
            Timber.w(e, "[RecommendHealthTool] API 搜索失败，降级到本地数据")
            null
        }

        if (apiResults != null && apiResults.isNotEmpty()) {
            val recommendations = apiResults.take(3)
            return buildString {
                append("针对「$symptom」，推荐以下功法：\n")
                recommendations.forEachIndexed { index, item ->
                    append("\n${index + 1}. 【${item.categoryId}】${item.name}")
                    append("\n   ${item.description}")
                    append("\n   功效：${item.benefits}")
                    if (item.meridians.isNotEmpty()) append("\n   经络：${item.meridians.joinToString("、")}")
                }
            }
        }

        // Fallback to local matching
        return recommendFromLocal(symptom)
    }

    private fun recommendFromLocal(symptom: String): String {
        val matches = findMatchingItems(symptom)

        if (matches.isEmpty()) {
            return "暂未找到针对「$symptom」的精确匹配功法。建议尝试八段锦，它能全面调理身体经络气血，对多种亚健康状态都有帮助。"
        }

        val recommendations = matches.take(3)
        return buildString {
            append("针对「$symptom」，推荐以下功法：\n")
            recommendations.forEachIndexed { index, (item, categoryName, reason) ->
                append("\n${index + 1}. 【${categoryName}】${item.name}")
                append("\n   ${item.description}")
                append("\n   功效：${item.benefits}")
                append("\n   推荐理由：$reason")
            }
        }
    }

    private fun findMatchingItems(symptom: String): List<Triple<HealthItem, String, String>> {
        val symptomLower = symptom.lowercase()
        val results = mutableListOf<Triple<HealthItem, String, String>>()

        val symptomKeywords = SYMPTOM_MAP.entries.filter { (keywords, _) ->
            keywords.any { symptomLower.contains(it) }
        }.flatMap { it.value }.toSet()

        for (category in HealthData.categories) {
            for (item in category.items) {
                val benefitsLower = item.benefits.lowercase()
                val meridiansLower = item.meridians.lowercase()
                val keyPointsLower = item.keyPoints.lowercase()
                val allText = "$benefitsLower $meridiansLower $keyPointsLower"

                // Direct match in benefits
                if (benefitsLower.contains(symptomLower)) {
                    results.add(Triple(item, category.name, "功效直接对应「$symptom」"))
                    continue
                }

                // Keyword-based match
                for (keyword in symptomKeywords) {
                    if (allText.contains(keyword)) {
                        results.add(Triple(item, category.name, "涉及${keyword}相关经络穴位，有助于缓解$symptom"))
                        break
                    }
                }
            }
        }

        return results.distinctBy { it.first.id }
    }

    companion object {
        private val SYMPTOM_MAP = mapOf(
            listOf("腰", "腰酸", "腰痛", "腰背") to listOf("肾", "腰", "膀胱经", "肾俞", "委中"),
            listOf("失眠", "睡不着", "入睡困难", "多梦") to listOf("心", "安神", "神门", "心经"),
            listOf("肩", "颈", "肩颈", "脖子", "颈椎") to listOf("颈椎", "肩", "大椎", "风池"),
            listOf("胃", "消化", "脾胃", "胃胀", "食欲") to listOf("脾胃", "消化", "足三里", "中脘", "脾经", "胃经"),
            listOf("头痛", "头晕", "头疼") to listOf("百会", "风池", "太阳", "头", "督脉"),
            listOf("眼", "眼睛", "视力", "目") to listOf("睛明", "肝经", "目", "肝"),
            listOf("呼吸", "咳嗽", "肺", "气短") to listOf("肺经", "呼吸", "肺", "列缺", "中府"),
            listOf("疲劳", "累", "乏力", "没精神") to listOf("气", "阳气", "体质", "三焦"),
            listOf("情绪", "焦虑", "烦躁", "心烦", "抑郁") to listOf("肝", "疏肝", "心", "情志"),
            listOf("腿", "膝盖", "下肢", "腿疼") to listOf("腿", "下肢", "膝", "膀胱经", "肾经")
        )
    }
}
