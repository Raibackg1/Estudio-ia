package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AiProvider(val displayName: String) {
    OPENROUTER("OpenRouter (Modelos Free & Premium)"),
    GEMINI("Google Gemini REST"),
    LOCAL_OFFLINE("Motor Académico Local (Offline)")
}

data class OpenRouterModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val isFree: Boolean,
    val description: String
)

object OpenRouterPresets {
    val FREE_MODELS = listOf(
        OpenRouterModelInfo(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Gemini 2.0 Flash (Free)",
            provider = "Google",
            isFree = true,
            description = "Ultra rápido, respuestas detalladas y análisis paso a paso."
        ),
        OpenRouterModelInfo(
            id = "deepseek/deepseek-chat:free",
            name = "DeepSeek V3 (Free)",
            provider = "DeepSeek",
            isFree = true,
            description = "Excelente razonamiento lógico, matemáticas y código."
        ),
        OpenRouterModelInfo(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            name = "Llama 3.3 70B (Free)",
            provider = "Meta",
            isFree = true,
            description = "Modelo líder de código abierto, redacción fluida y síntesis."
        ),
        OpenRouterModelInfo(
            id = "qwen/qwen-2.5-72b-instruct:free",
            name = "Qwen 2.5 72B (Free)",
            provider = "Alibaba",
            isFree = true,
            description = "Potente en ciencias exactas, lógica y multilingüe."
        ),
        OpenRouterModelInfo(
            id = "mistralai/mistral-7b-instruct:free",
            name = "Mistral 7B (Free)",
            provider = "Mistral AI",
            isFree = true,
            description = "Respuestas concisas, resúmenes rápidos y flashcards."
        )
    )
}

data class AiConfigurationState(
    val provider: AiProvider = AiProvider.OPENROUTER,
    val openRouterApiKey: String = "",
    val openRouterModel: String = "google/gemini-2.0-flash-exp:free",
    val geminiApiKey: String = "",
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val isConnectedSuccessfully: Boolean? = null
)

class AiSettingsManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ai_tutor_config", Context.MODE_PRIVATE)

    private val _configState = MutableStateFlow(loadConfig())
    val configState: StateFlow<AiConfigurationState> = _configState.asStateFlow()

    private fun loadConfig(): AiConfigurationState {
        val providerStr = prefs.getString(KEY_PROVIDER, AiProvider.OPENROUTER.name) ?: AiProvider.OPENROUTER.name
        val provider = try {
            AiProvider.valueOf(providerStr)
        } catch (e: Exception) {
            AiProvider.OPENROUTER
        }

        val buildConfigOpenRouterKey = try {
            val field = BuildConfig::class.java.getField("OPENROUTER_API_KEY")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() && !it.startsWith("MY_") } ?: ""
        } catch (e: Throwable) {
            try {
                val field2 = BuildConfig::class.java.getField("OPEN_ROUTER_API_KEY")
                (field2.get(null) as? String)?.takeIf { it.isNotBlank() && !it.startsWith("MY_") } ?: ""
            } catch (e2: Throwable) {
                ""
            }
        }

        val savedOpenRouterKey = prefs.getString(KEY_OPENROUTER_KEY, "") ?: ""
        val effectiveOpenRouterKey = savedOpenRouterKey.ifBlank { buildConfigOpenRouterKey }
        val openRouterModel = prefs.getString(KEY_OPENROUTER_MODEL, "google/gemini-2.0-flash-exp:free") ?: "google/gemini-2.0-flash-exp:free"
        val geminiKey = prefs.getString(KEY_GEMINI_KEY, BuildConfig.GEMINI_API_KEY) ?: ""

        return AiConfigurationState(
            provider = provider,
            openRouterApiKey = effectiveOpenRouterKey,
            openRouterModel = openRouterModel,
            geminiApiKey = geminiKey
        )
    }

    fun updateOpenRouterApiKey(apiKey: String) {
        prefs.edit().putString(KEY_OPENROUTER_KEY, apiKey.trim()).apply()
        _configState.value = _configState.value.copy(
            openRouterApiKey = apiKey.trim(),
            connectionTestResult = null,
            isConnectedSuccessfully = null
        )
    }

    fun updateOpenRouterModel(modelId: String) {
        prefs.edit().putString(KEY_OPENROUTER_MODEL, modelId.trim()).apply()
        _configState.value = _configState.value.copy(openRouterModel = modelId.trim())
    }

    fun updateProvider(provider: AiProvider) {
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()
        _configState.value = _configState.value.copy(provider = provider)
    }

    fun updateGeminiApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_KEY, apiKey.trim()).apply()
        _configState.value = _configState.value.copy(geminiApiKey = apiKey.trim())
    }

    fun setTestingState(isTesting: Boolean, result: String? = null, isSuccess: Boolean? = null) {
        _configState.value = _configState.value.copy(
            isTestingConnection = isTesting,
            connectionTestResult = result,
            isConnectedSuccessfully = isSuccess
        )
    }

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_OPENROUTER_KEY = "openrouter_api_key"
        private const val KEY_OPENROUTER_MODEL = "openrouter_model"
        private const val KEY_GEMINI_KEY = "gemini_api_key"
    }
}
