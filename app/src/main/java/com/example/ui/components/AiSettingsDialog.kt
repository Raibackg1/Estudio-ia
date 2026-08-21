package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.AiProvider
import com.example.data.remote.OpenRouterPresets
import com.example.service.StudyShareIntentHandler
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsDialog(
    viewModel: TutorViewModel,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val configState by viewModel.aiConfigState.collectAsStateWithLifecycle()

    var openRouterKeyInput by remember(configState.openRouterApiKey) {
        mutableStateOf(configState.openRouterApiKey)
    }
    var selectedModel by remember(configState.openRouterModel) {
        mutableStateOf(configState.openRouterModel)
    }
    var customModelInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: OpenRouter Free, 1: WhatsApp Intent, 2: Proveedores

    val isWhatsAppAvailable = remember { StudyShareIntentHandler.isWhatsAppInstalled(context) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("ai_settings_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Ajustes",
                                tint = IndigoPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Configuración IA & WhatsApp",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OpenRouter API Free + Compartición",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = IndigoPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("OpenRouter Free", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // OpenRouter Settings
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate50),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (configState.openRouterApiKey.isNotBlank()) EmeraldSuccess else AmberWarning)
                                    )
                                    Text(
                                        text = if (configState.openRouterApiKey.isNotBlank()) "OpenRouter Conectado" else "Modo de Prueba / Fallback Activo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (configState.openRouterApiKey.isNotBlank()) EmeraldSuccess else AmberWarning
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Obtén tu clave gratuita en openrouter.ai/keys para acceder a modelos sin costo de Gemini 2.0, DeepSeek V3, Llama 3.3 y Qwen.",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // API Key Input
                        Text(
                            text = "OpenRouter API Key:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = openRouterKeyInput,
                            onValueChange = {
                                openRouterKeyInput = it
                                viewModel.updateOpenRouterApiKey(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("openrouter_key_input"),
                            placeholder = { Text("sk-or-v1-...", fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar Visibilidad",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Link button to OpenRouter keys
                        TextButton(
                            onClick = {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) { }
                            },
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Crear clave gratis en openrouter.ai", fontSize = 11.sp, color = IndigoPrimary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Model Selection
                        Text(
                            text = "Seleccionar Modelo Free:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OpenRouterPresets.FREE_MODELS.forEach { modelInfo ->
                            val isSelected = selectedModel == modelInfo.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedModel = modelInfo.id
                                        viewModel.updateOpenRouterModel(modelInfo.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedModel = modelInfo.id
                                            viewModel.updateOpenRouterModel(modelInfo.id)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = IndigoPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = modelInfo.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Surface(
                                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "FREE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EmeraldSuccess,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = modelInfo.description,
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Connection test button
                        Button(
                            onClick = {
                                viewModel.testAiConnection(openRouterKeyInput, selectedModel)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_openrouter_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !configState.isTestingConnection
                        ) {
                            if (configState.isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probando Conexión...")
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probar Conexión en Vivo")
                            }
                        }

                        // Test Result feedback
                        if (configState.connectionTestResult != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (configState.isConnectedSuccessfully == true) EmeraldSuccess.copy(alpha = 0.12f) else RoseHighlight.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = configState.connectionTestResult.orEmpty(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (configState.isConnectedSuccessfully == true) EmeraldSuccess else RoseHighlight,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    1 -> {
                        // WhatsApp & Share Intent Info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate50),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "WhatsApp",
                                            tint = EmeraldSuccess
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (isWhatsAppAvailable) "WhatsApp Detectado en tu Dispositivo" else "Compartición Directa Lista",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldSuccess
                                        )
                                        Text(
                                            text = "Intent Handler integrado con formato Markdown",
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Formatos soportados por TutorAI para WhatsApp:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                listOf(
                                    "📝 Apuntes completos con Método Cornell y etiquetas",
                                    "📑 Resúmenes ejecutivos en viñetas estructuradas",
                                    "📖 Explicaciones de libros y fragmentos resaltados",
                                    "🏆 Retos de simulacros de examen para compañeros",
                                    "⚡ Fórmulas resueltas y código con resaltado"
                                ).forEach { feat ->
                                    Text(
                                        text = "• $feat",
                                        fontSize = 11.sp,
                                        color = Slate700,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test WhatsApp Share Button
                        Button(
                            onClick = {
                                StudyShareIntentHandler.dispatchTextIntent(
                                    context = context,
                                    formattedText = "📚 *TUTOR AI - Prueba de Compartición*\n━━━━━━━━━━━━━━━━━━━━\n✨ ¡La integración con WhatsApp y grupos de estudio está 100% activa!\n\n_Descubre más en TutorAI_",
                                    chooserTitle = "Probar Compartir en WhatsApp"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_whatsapp_share_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Mensaje de Prueba")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Guardar y Listo")
                }
            }
        }
    }
}
