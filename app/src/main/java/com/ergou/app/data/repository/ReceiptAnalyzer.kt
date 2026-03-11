package com.ergou.app.data.repository

import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.dto.NextParsePreview
import com.ergou.app.data.remote.dto.NextParsePreviewItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

/**
 * 使用当前选中的 LLM 分析收据/小票照片，提取结构化数据。
 */
class ReceiptAnalyzer(
    private val llmService: LLMService
) {
    companion object {
        private const val SYSTEM_PROMPT = """你是一个专业的收据/小票分析助手。分析用户提供的收据照片，提取结构化信息。
请严格按照以下 JSON 格式返回，不要包含任何其他文字：
{
  "merchant": "商家名称",
  "date": "YYYY-MM-DD",
  "time": "HH:mm",
  "currency": "CAD 或 CNY 或 USD",
  "tags": ["分类标签"],
  "description": "一句话总结这笔消费",
  "items": [
    {"name": "商品名", "quantity": 1.0, "unit_price": 0.0, "amount": 0.0, "specs": "翻译"}
  ],
  "subtotal": 0.0,
  "tax": 0.0,
  "tip": 0.0,
  "total_amount": 0.0
}

注意：
- tags 从以下选择：超市、餐饮、交通、加油、购物、日用、住房、娱乐、医疗、教育
- currency 根据收据内容判断（加拿大默认 CAD，中国默认 CNY）
- 如有多张照片，可能是同一张长单据的不同部分，需要合并分析
- items 必须列出所有花费项目，包括商品、税(HST/GST/PST等)、小费(tip)、服务费、配送费等——只要是花钱的都要列入
- specs 字段用于翻译：英文收据翻译成中文，中文收据翻译成英文。如果不需要翻译则留空字符串
- time 从收据上提取交易时间，如无法识别则设为 null
- description 用一句话总结这笔消费的内容和场景
- 只返回 JSON，不要其他解释"""
    }

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * 分析收据照片，返回结构化预览。
     * @param imageBase64List base64 编码的图片列表
     * @param text 用户附加的文本备注
     */
    suspend fun analyze(imageBase64List: List<String>, text: String? = null): NextParsePreview {
        val prompt = when {
            imageBase64List.isEmpty() && !text.isNullOrBlank() ->
                "请根据以下文字描述，提取结构化的账单信息。\n内容：$text"
            text.isNullOrBlank() ->
                "请分析这张收据/小票照片，提取结构化信息。"
            else ->
                "请分析这张收据/小票照片，提取结构化信息。\n用户备注：$text"
        }

        Timber.d("[ReceiptAnalyzer] 发送分析请求 images=%d", imageBase64List.size)

        val content = llmService.chatWithImages(
            systemPrompt = SYSTEM_PROMPT,
            userText = prompt,
            imageBase64List = imageBase64List
        )

        Timber.d("[ReceiptAnalyzer] 返回: %s", content.take(200))
        return parseResponse(content)
    }

    private fun parseResponse(content: String): NextParsePreview {
        val jsonStr = content
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return try {
            val obj = json.decodeFromString<JsonObject>(jsonStr)
            NextParsePreview(
                merchant = obj["merchant"]?.jsonPrimitive?.contentOrNull ?: "",
                date = obj["date"]?.jsonPrimitive?.contentOrNull,
                time = obj["time"]?.jsonPrimitive?.contentOrNull,
                currency = obj["currency"]?.jsonPrimitive?.contentOrNull ?: "CAD",
                tags = obj["tags"]?.let { tagsEl ->
                    json.decodeFromString<List<String>>(tagsEl.toString())
                },
                description = obj["description"]?.jsonPrimitive?.contentOrNull,
                items = obj["items"]?.let { itemsEl ->
                    json.decodeFromString<List<NextParsePreviewItem>>(itemsEl.toString())
                } ?: emptyList(),
                subtotal = obj["subtotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                tax = obj["tax"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                tip = obj["tip"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                totalAmount = obj["total_amount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            )
        } catch (e: Exception) {
            Timber.e(e, "[ReceiptAnalyzer] JSON 解析失败: %s", jsonStr.take(200))
            throw Exception("AI 返回格式异常，请重试")
        }
    }
}
