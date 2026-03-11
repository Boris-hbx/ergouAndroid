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
import kotlinx.serialization.json.putJsonObject

class DeleteExpenseTool(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : Tool {
    override val name = "delete_expense"
    override val description = "删除一条记账记录。需要提供记账条目ID"
    override val parameters: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("expense_id") {
                put("type", "string")
                put("description", "要删除的记账条目ID")
            }
        }
        put("required", buildJsonArray {
            add(JsonPrimitive("expense_id"))
        })
    }

    override suspend fun execute(arguments: Map<String, String>): String {
        if (!authProvider.isLoggedIn.first()) {
            return "请先在设置中登录Next账号"
        }

        val expenseId = arguments["expense_id"] ?: return "缺少记账条目ID"

        // First fetch the expense to confirm it exists and show details
        val getResult = nextApiService.getExpenseById(expenseId)
        val entry = getResult.getOrNull()

        val deleteResult = nextApiService.deleteExpense(expenseId)
        return deleteResult.fold(
            onSuccess = {
                if (entry != null) {
                    "已删除记账：${entry.notes ?: "消费"} ${"%.2f".format(entry.amount)} ${entry.currency} (ID:${expenseId})"
                } else {
                    "已删除记账条目 (ID:${expenseId})"
                }
            },
            onFailure = { e -> "删除失败：${e.message}" }
        )
    }
}
