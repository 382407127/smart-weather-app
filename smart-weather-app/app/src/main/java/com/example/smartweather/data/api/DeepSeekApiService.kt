package com.example.smartweather.data.api

import com.example.smartweather.data.model.DeepSeekResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DeepSeek API接口
 * 文档：https://platform.deepseek.com/api-docs/
 */
interface DeepSeekApiService {
    
    /**
     * 聊天补全接口
     * @param authorization Bearer token
     * @param request 请求体
     */
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}

/**
 * DeepSeek请求体
 */
data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)

/**
 * 消息
 */
data class Message(
    val role: String,
    val content: String
)
