package com.ergou.app.data.tool

import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.FunctionCall
import com.ergou.app.data.remote.dto.Message
import com.ergou.app.data.remote.dto.ToolCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

/**
 * Tool Use 循环引擎
 * 全程流式：内容实时输出，工具调用从流中解析
 * LLM 请求工具 → 执行工具 → 结果回注 → LLM 继续，最多5轮
 */
class ToolExecutor(
    private val llmService: LLMService,
    private val toolRegistry: ToolRegistry
) {
    companion object {
        private const val MAX_ROUNDS = 5
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 带工具调用的对话 — 返回最终文本回复的流
     * 全程使用流式请求，内容实时 emit，工具调用从 delta 中解析
     */
    fun chatWithTools(messages: List<Message>): Flow<String> = flow {
        val toolDefinitions = toolRegistry.getAllDefinitions()
        val conversationMessages = messages.toMutableList()
        var round = 0

        while (round < MAX_ROUNDS) {
            round++
            Timber.d("[ToolExecutor] 第${round}轮开始，消息数: ${conversationMessages.size}")

            val request = ChatRequest(
                messages = conversationMessages,
                tools = if (toolDefinitions.isNotEmpty()) toolDefinitions else null,
                stream = true
            )

            // 流式收集：同时处理内容输出和工具调用解析
            val contentBuilder = StringBuilder()
            // tool_calls 按 index 累积: index → (id, name, arguments)
            val toolCallIds = mutableMapOf<Int, String>()
            val toolCallNames = mutableMapOf<Int, String>()
            val toolCallArgs = mutableMapOf<Int, StringBuilder>()
            var hasToolCalls = false

            try {
                llmService.chatStreamChunks(request).collect { chunk ->
                    val choice = chunk.choices.firstOrNull() ?: return@collect
                    val delta = choice.delta ?: return@collect

                    // 处理文本内容 — 实时 emit
                    if (delta.content != null) {
                        contentBuilder.append(delta.content)
                        emit(delta.content)
                    }

                    // 处理工具调用 delta
                    val deltaToolCalls = delta.toolCalls
                    if (deltaToolCalls != null) {
                        hasToolCalls = true
                        for (dtc in deltaToolCalls) {
                            val idx = dtc.index
                            // 首次出现的工具调用：记录 id 和 name
                            if (dtc.id != null) {
                                toolCallIds[idx] = dtc.id
                            }
                            if (dtc.function?.name != null) {
                                toolCallNames[idx] = dtc.function.name
                            }
                            // 累积 arguments 片段
                            if (dtc.function?.arguments != null) {
                                toolCallArgs.getOrPut(idx) { StringBuilder() }
                                    .append(dtc.function.arguments)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "[ToolExecutor] 流式请求失败")
                // 降级：不带工具重试一次
                Timber.d("[ToolExecutor] 降级为无工具流式请求")
                val fallbackRequest = ChatRequest(
                    messages = conversationMessages,
                    stream = true
                )
                llmService.chatStream(fallbackRequest).collect { emit(it) }
                return@flow
            }

            // 没有工具调用 — 最终回复已经流式输出完毕
            if (!hasToolCalls) {
                if (contentBuilder.isEmpty()) {
                    emit("（二狗想说什么但忘了，再问一次？）")
                }
                return@flow
            }

            // 有工具调用：组装完整的 ToolCall 列表
            val toolCalls = toolCallIds.keys.sorted().map { idx ->
                ToolCall(
                    id = toolCallIds[idx] ?: "",
                    type = "function",
                    function = FunctionCall(
                        name = toolCallNames[idx] ?: "",
                        arguments = toolCallArgs[idx]?.toString() ?: "{}"
                    )
                )
            }

            Timber.d("[ToolExecutor] 第${round}轮检测到工具调用: ${toolCalls.map { it.function.name }}")

            // 加入 assistant 的 tool_calls 消息
            conversationMessages.add(
                Message(
                    role = "assistant",
                    content = contentBuilder.toString().ifBlank { null },
                    toolCalls = toolCalls
                )
            )

            // 执行每个工具调用
            for (toolCall in toolCalls) {
                val toolName = toolCall.function.name
                val argsString = toolCall.function.arguments

                val args = try {
                    val jsonObj = json.decodeFromString<JsonObject>(argsString)
                    jsonObj.mapValues { it.value.jsonPrimitive.content }
                } catch (e: Exception) {
                    Timber.w(e, "[ToolExecutor] 解析工具参数失败: $argsString")
                    emptyMap()
                }

                val result = toolRegistry.executeTool(toolName, args)

                conversationMessages.add(
                    Message(
                        role = "tool",
                        content = result,
                        toolCallId = toolCall.id
                    )
                )
            }

            Timber.d("[ToolExecutor] 第${round}轮工具执行完成")
        }

        if (round >= MAX_ROUNDS) {
            emit("（工具调用轮次已达上限）")
        }
    }
}
