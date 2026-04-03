package com.example.smartweather.data.model

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek API响应
 */
data class DeepSeekResponse(
    val id: String?,
    val `object`: String?,
    val created: Long?,
    val model: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

/**
 * 选择项
 */
data class Choice(
    val index: Int,
    val message: ResponseMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * 响应消息
 */
data class ResponseMessage(
    val role: String,
    val content: String
)

/**
 * Token使用量
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)
