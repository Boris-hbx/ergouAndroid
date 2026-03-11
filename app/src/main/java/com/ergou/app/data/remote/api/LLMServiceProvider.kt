package com.ergou.app.data.remote.api

import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.ChatResponse
import com.ergou.app.util.ApiKeyProvider
import com.ergou.app.util.ModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * 根据用户选择的模型提供商动态路由 LLMService 调用。
 * 每次调用时读取当前设置，切换提供商即时生效。
 */
class LLMServiceProvider(
    private val deepSeekService: DeepSeekService,
    private val claudeService: ClaudeService,
    private val apiKeyProvider: ApiKeyProvider
) : LLMService {

    private suspend fun active(): LLMService {
        return when (apiKeyProvider.modelProvider.first()) {
            ModelProvider.DEEPSEEK -> deepSeekService
            ModelProvider.CLAUDE -> claudeService
        }
    }

    override suspend fun chat(request: ChatRequest): ChatResponse = active().chat(request)

    override fun chatStream(request: ChatRequest): Flow<String> = flow {
        emitAll(active().chatStream(request))
    }

    override fun chatStreamChunks(request: ChatRequest): Flow<ChatResponse> = flow {
        emitAll(active().chatStreamChunks(request))
    }

    override suspend fun chatWithImages(
        systemPrompt: String,
        userText: String,
        imageBase64List: List<String>,
        maxTokens: Int
    ): String = active().chatWithImages(systemPrompt, userText, imageBase64List, maxTokens)
}
