package com.example.ui.screens.call

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.WhatsAppShareHelper
import com.example.service.StudyShareIntentHandler
import com.example.ui.components.AiSettingsDialog
import com.example.ui.components.TutorAvatarView
import com.example.ui.components.WaveformBars
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorAvatarState
import com.example.ui.viewmodel.TutorCallMode
import com.example.ui.viewmodel.TutorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCallScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val callMode by viewModel.callMode.collectAsStateWithLifecycle()
    val avatarState by viewModel.avatarState.collectAsStateWithLifecycle()
    val callDuration by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val isCallActive by viewModel.isCallActive.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isCameraOn by viewModel.isCameraOn.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.speechService.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.speechService.isListening.collectAsStateWithLifecycle()

    var userTextInput by remember { mutableStateOf("") }
    var showChatSheet by remember { mutableStateOf(false) }
    var showSpeechSpeedDialog by remember { mutableStateOf(false) }
    var showAiSettingsDialog by remember { mutableStateOf(false) }
    val speechRate by viewModel.speechService.speechRate.collectAsStateWithLifecycle()
    val aiConfig by viewModel.aiConfigState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickQuestions = listOf(
        "Explícame la teoría de la relatividad",
        "¿Cómo funciona el método de estudio Cornell?",
        "Resuelve la ecuación cuadrática paso a paso",
        "Dame 3 preguntas de examen sobre Biología",
        "Modo Socrático: Guíame sin darme la respuesta",
        "¿Cómo calculo la entropía en termodinámica?",
        "Técnica Feynman: Explícamelo como a un niño"
    )

    // Formatted Call Duration mm:ss
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val formattedDuration = "%02d:%02d".format(minutes, seconds)

    val latestTutorMessage = chatMessages.lastOrNull { it.sender == "TUTOR" }?.text
        ?: "Hola, ¿en qué materia o concepto te ayudo hoy?"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Slate900,
                        Color(0xFF111827),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Tutor Info & Call Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isCallActive) EmeraldSuccess else RoseHighlight)
                    )
                    Column {
                        Text(
                            text = "Sofía • Tutora IA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isCallActive) "Llamada activa • $formattedDuration" else "Llamada pausada",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }

                // Mode Tabs (Voz, Video, Chat)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate800)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setCallMode(TutorCallMode.VOICE_CALL) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (callMode == TutorCallMode.VOICE_CALL) IndigoPrimary else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Modo Voz",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setCallMode(TutorCallMode.VIDEO_CALL) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (callMode == TutorCallMode.VIDEO_CALL) IndigoPrimary else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Modo Video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showChatSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Ver Transcripción",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showAiSettingsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoDark)
                            .testTag("open_ai_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ajustes de IA",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Model Badge & WhatsApp Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Slate800.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { showAiSettingsDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (aiConfig.openRouterApiKey.isNotBlank()) EmeraldSuccess else AmberWarning)
                        )
                        Text(
                            text = if (aiConfig.openRouterApiKey.isNotBlank()) "OpenRouter Free: ${aiConfig.openRouterModel.substringAfterLast("/")}" else "Motor de IA: Gemini / Local",
                            fontSize = 11.sp,
                            color = Slate200,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                    }
                }

                Surface(
                    color = WhatsAppGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        viewModel.shareCallTakeawaysToWhatsApp(context)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(12.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 11.sp,
                            color = WhatsAppGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Visual Stage (Animated Avatar with Live Reactive Audio Halo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                IndigoDark.copy(alpha = 0.35f),
                                Slate900.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TutorAvatarView(
                        avatarState = avatarState,
                        isCameraOn = isCameraOn
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Audio Waveform Indicator
                    WaveformBars(
                        isActive = isSpeaking || isListening,
                        barColor = if (isListening) EmeraldSuccess else CyanAccent,
                        barCount = 9,
                        maxHeight = 28.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Subtitle Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Slate800.copy(alpha = 0.85f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(IndigoLight.copy(alpha = 0.3f), CyanAccent.copy(alpha = 0.3f)))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isListening) "🎤 Te escucho..." else "🗣️ Sofía explicando:",
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            viewModel.speechService.speak(latestTutorMessage)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Repetir audio",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showSpeechSpeedDialog = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "Velocidad de voz",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestTutorMessage,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                maxLines = 4
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Topic Suggestion Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickQuestions) { prompt ->
                    SuggestionChip(
                        onClick = {
                            viewModel.sendTutorMessage(prompt, readAloud = true)
                        },
                        label = {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = Slate200
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Slate800
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Slate700
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Input Bar (Voice & Text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userTextInput,
                    onValueChange = { userTextInput = it },
                    placeholder = { Text("Escribe una duda o ejercicio...", color = Slate400, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tutor_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    maxLines = 2,
                    trailingIcon = {
                        if (userTextInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val text = userTextInput
                                    userTextInput = ""
                                    viewModel.sendTutorMessage(text, readAloud = true)
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = CyanAccent)
                            }
                        }
                    }
                )

                // Push to Talk / Voice Listening Button
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopVoiceListening()
                        } else {
                            viewModel.startVoiceListening()
                        }
                    },
                    containerColor = if (isListening) RoseHighlight else IndigoPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("voice_talk_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Hablar con Sofía",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Call Actions Bar (Mute, Video, WhatsApp Share, End/Pause Call)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate800.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) RoseContainer else Slate700)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Silenciar audio",
                        tint = if (isMuted) RoseHighlight else Color.White
                    )
                }

                // Camera Toggle
                IconButton(
                    onClick = { viewModel.toggleCamera() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCameraOn) Slate700 else Slate800)
                ) {
                    Icon(
                        imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Cámara",
                        tint = Color.White
                    )
                }

                // WhatsApp Direct Share Button for Study Groups
                WhatsAppShareButton(
                    title = "Tutoría con Sofía",
                    content = latestTutorMessage,
                    category = "Explicación en Vivo",
                    label = "WhatsApp"
                )

                // Call Toggle (Pause / Resume)
                IconButton(
                    onClick = { viewModel.toggleCallActive() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCallActive) RoseHighlight else EmeraldSuccess)
                ) {
                    Icon(
                        imageVector = if (isCallActive) Icons.Default.CallEnd else Icons.Default.Call,
                        contentDescription = "Control de llamada",
                        tint = Color.White
                    )
                }
            }
        }

        // Speech Speed Control Dialog
        if (showSpeechSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeechSpeedDialog = false },
                title = { Text("Velocidad de Voz del Tutor", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Velocidad actual: ${speechRate}x", fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { rate ->
                                FilterChip(
                                    selected = speechRate == rate,
                                    onClick = {
                                        viewModel.speechService.setSpeechRate(rate)
                                    },
                                    label = { Text("${rate}x") }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeechSpeedDialog = false }) {
                        Text("Listo")
                    }
                }
            )
        }

        // Full Transcript Sheet Modal
        if (showChatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChatSheet = false },
                containerColor = Slate900,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transcripción de la Tutoría",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        WhatsAppShareButton(
                            title = "Apuntes de Tutoría",
                            content = chatMessages.joinToString("\n\n") { "${if (it.sender == "USER") "👤 Estudiante:" else "🤖 Sofía:"} ${it.text}" },
                            label = "Exportar Todo a WhatsApp"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) IndigoDark else Slate800
                                    ),
                                    modifier = Modifier.widthIn(max = 290.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isUser) "Tú" else "Sofía (Tutora IA)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) CyanAccent else AmberWarning
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.text,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // OpenRouter & WhatsApp Configuration Dialog
        if (showAiSettingsDialog) {
            AiSettingsDialog(
                viewModel = viewModel,
                onDismissRequest = { showAiSettingsDialog = false }
            )
        }
    }
}
