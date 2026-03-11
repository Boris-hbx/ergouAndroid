package com.ergou.app.data.remote.api

import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.ChatResponse
import com.ergou.app.data.remote.dto.Choice
import com.ergou.app.data.remote.dto.Delta
import com.ergou.app.data.remote.dto.DeltaFunctionCall
import com.ergou.app.data.remote.dto.DeltaToolCall
import com.ergou.app.data.remote.dto.FunctionCall
import com.ergou.app.data.remote.dto.MessageResponse
import com.ergou.app.data.remote.dto.ToolCall
import com.ergou.app.util.ApiKeyProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import timber.log.Timber

/**
 * Claude (Anthropic Messages API) 实现。
 * 内部将 OpenAI 格式的 DTO 转换为 Anthropic 格式。
 */
class ClaudeService(
    private val httpClient: HttpClient,
    private val apiKeyProvider: ApiKeyProvider
) : LLMService {

    companion object {
        private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        private const val MODEL = "claude-opus-4-6"
        private const val ANTHROPIC_VERSION = "2023-06-01"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun getApiKey(): String = apiKeyProvider.claudeApiKey.first()

    // ── 非流式 ──

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val key = getApiKey()
        if (key.isBlank()) throw Exception("请先在设置中配置 Claude API Key")
        val body = buildRequestBody(request, stream = false)

        val response = httpClient.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("x-api-key", key)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
            Timber.e("[Claude] API 错误 status=%s body=%s", response.status, errorBody.take(500))
            throw Exception("Claude API 错误 (${response.status}): ${errorBody.take(200)}")
        }

        val responseJson: JsonObject = response.body()
        return parseNonStreamingResponse(responseJson)
    }

    // ── 流式 ──

    override fun chatStream(request: ChatRequest): Flow<String> = flow {
        chatStreamChunks(request).collect { chunk ->
            val content = chunk.choices.firstOrNull()?.delta?.content
            if (content != null) emit(content)
        }
    }

    override fun chatStreamChunks(request: ChatRequest): Flow<ChatResponse> = flow {
        val key = getApiKey()
        if (key.isBlank()) throw Exception("请先在设置中配置 Claude API Key")
        val body = buildRequestBody(request, stream = true)

        val response = httpClient.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("x-api-key", key)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
            Timber.e("[Claude] API 错误 status=%s body=%s", response.status, errorBody.take(500))
            throw Exception("Claude API 错误 (${response.status}): ${errorBody.take(200)}")
        }

        val channel = response.bodyAsChannel()
        var currentEventType = ""
        // 记录每个 content_block 的类型, index → "text" | "tool_use"
        val blockTypes = mutableMapOf<Int, String>()
        // tool_use block 的 id 和 name (在 content_block_start 时获得)
        val toolUseIds = mutableMapOf<Int, String>()
        val toolUseNames = mutableMapOf<Int, String>()
        // 将 tool_use 的 content block index 映射为 tool call index (0-based)
        var toolCallCounter = 0
        val blockToToolIndex = mutableMapOf<Int, Int>()

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            when {
                line.startsWith("event: ") -> {
                    currentEventType = line.removePrefix("event: ").trim()
                }
                line.startsWith("data: ") -> {
                    val data = line.removePrefix("data: ").trim()
                    if (data.isEmpty()) continue

                    try {
                        val obj = json.decodeFromString<JsonObject>(data)
                        val chunk = parseStreamEvent(
                            currentEventType, obj,
                            blockTypes, toolUseIds, toolUseNames,
                            blockToToolIndex, { toolCallCounter++ }
                        )
                        if (chunk != null) emit(chunk)
                    } catch (e: Exception) {
                        Timber.w(e, "[Claude] 解析SSE数据失败: $data")
                    }
                }
            }
        }
    }

    // ── 多模态（图片） ──

    override suspend fun chatWithImages(
        systemPrompt: String,
        userText: String,
        imageBase64List: List<String>,
        maxTokens: Int
    ): String {
        val key = getApiKey()
        if (key.isBlank()) throw Exception("请先在设置中配置 Claude API Key")

        val userContent = buildJsonArray {
            for (base64 in imageBase64List) {
                add(buildJsonObject {
                    put("type", "image")
                    put("source", buildJsonObject {
                        put("type", "base64")
                        put("media_type", "image/jpeg")
                        put("data", base64)
                    })
                })
            }
            add(buildJsonObject {
                put("type", "text")
                put("text", userText)
            })
        }

        val requestBody = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", maxTokens)
            put("system", systemPrompt)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", userContent)
                })
            })
        }

        val response = httpClient.post(ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("x-api-key", key)
            header("anthropic-version", ANTHROPIC_VERSION)
            setBody(requestBody)
        }

        val responseJson: JsonObject = response.body()
        val content = responseJson["content"]?.jsonArray
            ?.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "text" }
            ?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: throw Exception("Claude 未返回分析结果")

        return content
    }

    // ── 请求构建 ──

    private fun buildRequestBody(request: ChatRequest, stream: Boolean): JsonObject {
        // 提取 system 消息
        val systemMessages = request.messages.filter { it.role == "system" }
        val otherMessages = request.messages.filter { it.role != "system" }
        val systemPrompt = systemMessages.mapNotNull { it.content }.joinToString("\n")

        // 转换消息
        val claudeMessages = convertMessages(otherMessages)

        // 转换工具定义
        val claudeTools = request.tools?.map { toolDef ->
            buildJsonObject {
                put("name", toolDef.function.name)
                put("description", toolDef.function.description)
                put("input_schema", toolDef.function.parameters)
            }
        }

        return buildJsonObject {
            put("model", MODEL)
            put("max_tokens", request.maxTokens)
            if (systemPrompt.isNotBlank()) put("system", systemPrompt)
            put("messages", JsonArray(claudeMessages))
            if (claudeTools != null && claudeTools.isNotEmpty()) {
                put("tools", JsonArray(claudeTools))
            }
            if (stream) put("stream", true)
        }
    }

    /**
     * 将 OpenAI 格式的消息列表转为 Claude 格式。
     * - assistant + tool_calls → content 数组含 text + tool_use
     * - role=tool → role=user, content=[{type:tool_result}]
     * - 合并相邻 tool 消息为同一个 user 消息
     */
    private fun convertMessages(messages: List<com.ergou.app.data.remote.dto.Message>): List<JsonElement> {
        val result = mutableListOf<JsonObject>()
        var i = 0

        while (i < messages.size) {
            val msg = messages[i]
            when (msg.role) {
                "assistant" -> {
                    val contentBlocks = mutableListOf<JsonElement>()
                    if (!msg.content.isNullOrBlank()) {
                        contentBlocks.add(buildJsonObject {
                            put("type", "text")
                            put("text", msg.content)
                        })
                    }
                    msg.toolCalls?.forEach { tc ->
                        val inputJson = try {
                            json.decodeFromString<JsonObject>(tc.function.arguments)
                        } catch (_: Exception) {
                            JsonObject(emptyMap())
                        }
                        contentBlocks.add(buildJsonObject {
                            put("type", "tool_use")
                            put("id", tc.id)
                            put("name", tc.function.name)
                            put("input", inputJson)
                        })
                    }
                    result.add(buildJsonObject {
                        put("role", "assistant")
                        put("content", JsonArray(contentBlocks))
                    })
                    i++
                }
                "tool" -> {
                    // 收集连续的 tool 消息，合并为一个 user 消息
                    val toolResults = mutableListOf<JsonElement>()
                    while (i < messages.size && messages[i].role == "tool") {
                        val toolMsg = messages[i]
                        toolResults.add(buildJsonObject {
                            put("type", "tool_result")
                            put("tool_use_id", toolMsg.toolCallId ?: "")
                            put("content", toolMsg.content ?: "")
                        })
                        i++
                    }
                    result.add(buildJsonObject {
                        put("role", "user")
                        put("content", JsonArray(toolResults))
                    })
                }
                else -> {
                    // user 等普通消息
                    result.add(buildJsonObject {
                        put("role", msg.role)
                        put("content", JsonPrimitive(msg.content ?: ""))
                    })
                    i++
                }
            }
        }
        return result
    }

    // ── 非流式响应解析 ──

    private fun parseNonStreamingResponse(obj: JsonObject): ChatResponse {
        val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: ""
        val stopReason = obj["stop_reason"]?.jsonPrimitive?.contentOrNull
        val contentArray = obj["content"]?.jsonArray ?: JsonArray(emptyList())

        var textContent = ""
        val toolCalls = mutableListOf<ToolCall>()

        for (block in contentArray) {
            val blockObj = block.jsonObject
            when (blockObj["type"]?.jsonPrimitive?.contentOrNull) {
                "text" -> {
                    textContent += blockObj["text"]?.jsonPrimitive?.contentOrNull ?: ""
                }
                "tool_use" -> {
                    toolCalls.add(
                        ToolCall(
                            id = blockObj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                            type = "function",
                            function = FunctionCall(
                                name = blockObj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                                arguments = blockObj["input"]?.toString() ?: "{}"
                            )
                        )
                    )
                }
            }
        }

        val finishReason = when (stopReason) {
            "tool_use" -> "tool_calls"
            "end_turn" -> "stop"
            else -> stopReason
        }

        return ChatResponse(
            id = id,
            choices = listOf(
                Choice(
                    index = 0,
                    message = MessageResponse(
                        role = "assistant",
                        content = textContent.ifBlank { null },
                        toolCalls = toolCalls.ifEmpty { null }
                    ),
                    finishReason = finishReason
                )
            )
        )
    }

    // ── 流式事件解析 ──

    private fun parseStreamEvent(
        eventType: String,
        data: JsonObject,
        blockTypes: MutableMap<Int, String>,
        toolUseIds: MutableMap<Int, String>,
        toolUseNames: MutableMap<Int, String>,
        blockToToolIndex: MutableMap<Int, Int>,
        nextToolIndex: () -> Int
    ): ChatResponse? {
        return when (eventType) {
            "content_block_start" -> {
                val index = data["index"]?.jsonPrimitive?.intOrNull ?: 0
                val block = data["content_block"]?.jsonObject ?: return null
                val type = block["type"]?.jsonPrimitive?.contentOrNull ?: return null
                blockTypes[index] = type

                if (type == "tool_use") {
                    val id = block["id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val name = block["name"]?.jsonPrimitive?.contentOrNull ?: ""
                    toolUseIds[index] = id
                    toolUseNames[index] = name
                    val toolIdx = nextToolIndex()
                    blockToToolIndex[index] = toolIdx

                    // 发出带 id 和 name 的初始 tool call delta
                    ChatResponse(
                        choices = listOf(
                            Choice(
                                delta = Delta(
                                    toolCalls = listOf(
                                        DeltaToolCall(
                                            index = toolIdx,
                                            id = id,
                                            type = "function",
                                            function = DeltaFunctionCall(name = name, arguments = "")
                                        )
                                    )
                                )
                            )
                        )
                    )
                } else null
            }

            "content_block_delta" -> {
                val index = data["index"]?.jsonPrimitive?.intOrNull ?: 0
                val delta = data["delta"]?.jsonObject ?: return null
                val deltaType = delta["type"]?.jsonPrimitive?.contentOrNull ?: return null

                when (deltaType) {
                    "text_delta" -> {
                        val text = delta["text"]?.jsonPrimitive?.contentOrNull ?: return null
                        ChatResponse(
                            choices = listOf(Choice(delta = Delta(content = text)))
                        )
                    }
                    "input_json_delta" -> {
                        val partialJson = delta["partial_json"]?.jsonPrimitive?.contentOrNull ?: return null
                        val toolIdx = blockToToolIndex[index] ?: 0
                        ChatResponse(
                            choices = listOf(
                                Choice(
                                    delta = Delta(
                                        toolCalls = listOf(
                                            DeltaToolCall(
                                                index = toolIdx,
                                                function = DeltaFunctionCall(arguments = partialJson)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    }
                    else -> null
                }
            }

            "message_delta" -> {
                val msgDelta = data["delta"]?.jsonObject ?: return null
                val stopReason = msgDelta["stop_reason"]?.jsonPrimitive?.contentOrNull
                if (stopReason != null) {
                    val finishReason = when (stopReason) {
                        "tool_use" -> "tool_calls"
                        "end_turn" -> "stop"
                        else -> stopReason
                    }
                    ChatResponse(
                        choices = listOf(Choice(finishReason = finishReason))
                    )
                } else null
            }

            else -> null
        }
    }
}
