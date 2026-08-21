package com.example.data.remote

import android.util.Log
import com.example.data.remote.openrouter.OpenRouterChatRequest
import com.example.data.remote.openrouter.OpenRouterMessage
import com.example.data.remote.openrouter.RetrofitOpenRouterClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiGatewayClient {

    private const val TAG = "AiGatewayClient"

    suspend fun generateContent(
        prompt: String,
        systemPrompt: String = "Eres un tutor académico de élite, amigable, claro y motivador. Responde en español de forma estructurada, usando viñetas y ejemplos claros.",
        config: AiConfigurationState? = null
    ): String = withContext(Dispatchers.IO) {
        val activeProvider = config?.provider ?: AiProvider.OPENROUTER
        val openRouterKey = config?.openRouterApiKey?.trim().orEmpty()
        val openRouterModel = config?.openRouterModel?.ifBlank { "google/gemini-2.0-flash-exp:free" } ?: "google/gemini-2.0-flash-exp:free"
        val geminiKey = config?.geminiApiKey?.trim().orEmpty()

        // 1. Try OpenRouter if key is present or provider is OPENROUTER
        if ((activeProvider == AiProvider.OPENROUTER || geminiKey.isBlank()) && openRouterKey.isNotBlank()) {
            try {
                Log.d(TAG, "Calling OpenRouter API with model: $openRouterModel")
                val request = OpenRouterChatRequest(
                    model = openRouterModel,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = systemPrompt),
                        OpenRouterMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    maxTokens = 2048
                )
                val authHeader = if (openRouterKey.startsWith("Bearer ", ignoreCase = true)) openRouterKey else "Bearer $openRouterKey"
                val response = RetrofitOpenRouterClient.apiService.createChatCompletion(
                    authorization = authHeader,
                    request = request
                )

                val reply = response.choices?.firstOrNull()?.message?.content
                if (!reply.isNullOrBlank()) {
                    return@withContext reply.trim()
                } else if (response.error != null) {
                    Log.e(TAG, "OpenRouter returned error: ${response.error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "OpenRouter API call failed: ${e.message}", e)
            }
        }

        // 2. Try Gemini REST API if Gemini key is available
        if (geminiKey.isNotBlank() && !geminiKey.startsWith("MY_")) {
            try {
                Log.d(TAG, "Calling Gemini REST API fallback")
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
                val response = RetrofitGeminiClient.apiService.generateContent(geminiKey, request)
                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!resultText.isNullOrBlank()) {
                    return@withContext resultText.trim()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini REST API fallback failed: ${e.message}")
            }
        }

        // 3. High-Quality Local Academic Offline Engine fallback
        return@withContext GeminiClient.generateLocalAcademicResponse(prompt, systemPrompt)
    }

    suspend fun testConnection(apiKey: String, model: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Pair(false, "La API Key de OpenRouter no puede estar vacía.")
        }
        val startTime = System.currentTimeMillis()
        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val request = OpenRouterChatRequest(
                model = model.ifBlank { "google/gemini-2.0-flash-exp:free" },
                messages = listOf(
                    OpenRouterMessage(role = "system", content = "Responde en 1 frase corta confirmando conexión."),
                    OpenRouterMessage(role = "user", content = "Prueba de conexión con TutorAI.")
                ),
                maxTokens = 60
            )
            val response = RetrofitOpenRouterClient.apiService.createChatCompletion(
                authorization = authHeader,
                request = request
            )

            val latency = System.currentTimeMillis() - startTime
            val text = response.choices?.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) {
                return@withContext Pair(true, "🟢 Conexión exitosa ($latency ms) con modelo '$model': \"$text\"")
            } else {
                val errorMsg = response.error?.message ?: "Respuesta vacía del servidor."
                return@withContext Pair(false, "🔴 Error de OpenRouter: $errorMsg")
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            return@withContext Pair(false, "🔴 Error de red (${latency} ms): ${e.localizedMessage ?: e.message}")
        }
    }
}
