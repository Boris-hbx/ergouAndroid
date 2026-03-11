package com.ergou.app.data.remote.api

import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.ChatResponse
import kotlinx.coroutines.flow.Flow

interface LLMService {
    suspend fun chat(request: ChatRequest): ChatResponse
    fun chatStream(request: ChatRequest): Flow<String>
    /** 流式返回完整 chunk（包含 delta.tool_calls），供 ToolExecutor 使用 */
    fun chatStreamChunks(request: ChatRequest): Flow<ChatResponse>
    /** 多模态：发送图片 + 文本，返回文本回复 */
    suspend fun chatWithImages(
        systemPrompt: String,
        userText: String,
        imageBase64List: List<String>,
        maxTokens: Int = 4096
    ): String = throw UnsupportedOperationException("此模型不支持图片输入")
}
