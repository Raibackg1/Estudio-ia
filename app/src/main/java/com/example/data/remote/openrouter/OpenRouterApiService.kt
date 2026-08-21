package com.example.data.remote.openrouter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenRouterMessage>,
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    @Json(name = "message") val message: OpenRouterMessage?,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterError(
    @Json(name = "message") val message: String?,
    @Json(name = "code") val code: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "choices") val choices: List<OpenRouterChoice>? = null,
    @Json(name = "error") val error: OpenRouterError? = null
)

interface OpenRouterApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://tutorai.app",
        @Header("X-Title") appTitle: String = "TutorAI Android",
        @Body request: OpenRouterChatRequest
    ): OpenRouterChatResponse
}

object RetrofitOpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: OpenRouterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenRouterApiService::class.java)
    }
}
