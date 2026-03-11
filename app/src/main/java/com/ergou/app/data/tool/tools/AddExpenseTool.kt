package com.ergou.app.data.tool.tools

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextExpenseCreateRequest
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

class AddExpenseTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "add_expense"
    override val description = "记一笔账到Next后端。支持金额、标签、币种和描述"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("amount") {
                put("type", "number")
                put("description", "金额")
            }
            putJsonObject("notes") {
                put("type", "string")
                put("description", "消费描述/备注")
            }
            putJsonObject("tags") {
                put("type", "string")
                put("description", "标签，逗号分隔，如'餐饮,外卖'")
            }
            putJsonObject("currency") {
                put("type", "string")
                putJsonArray("enum") {
                    add(JsonPrimitive("CAD"))
                    add(JsonPrimitive("CNY"))
                    add(JsonPrimitive("USD"))
                }
                put("description", "币种：CAD加元、CNY人民币、USD美元，默认CAD")
            }
            putJsonObject("date") {
                put("type", "string")
                put("description", "日期，格式 YYYY-MM-DD（可选，默认今天）")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("amount"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val amount = arguments["amount"]?.toDoubleOrNull() ?: return "缺少金额"
        val notes = arguments["notes"]
        val tags = arguments["tags"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        val currency = arguments["currency"] ?: "CAD"
        val date = arguments["date"]

        val result = nextApiService.createExpense(
            NextExpenseCreateRequest(
                amount = amount,
                notes = notes,
                tags = tags,
                currency = currency,
                date = date
            )
        )

        return result.fold(
            onSuccess = { entry ->
                val tagStr = if (!entry.tags.isNullOrEmpty()) " [${entry.tags.joinToString(",")}]" else ""
                "已记账：${"%.2f".format(entry.amount)} ${entry.currency}${tagStr} ${entry.notes ?: ""} (ID:${entry.id})"
            },
            onFailure = { e -> "记账失败：${e.message}" }
        )
    }
}
