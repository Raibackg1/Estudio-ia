package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SpeechRecognitionState
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDictationSheet(
    viewModel: TutorViewModel,
    initialTargetField: String = "Contenido de Nota",
    onTextCaptured: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val recognitionState by viewModel.speechService.recognitionState.collectAsStateWithLifecycle()
    val isListening by viewModel.speechService.isListening.collectAsStateWithLifecycle()
    val rmsDb by viewModel.speechService.rmsDb.collectAsStateWithLifecycle()
    val isRecordingAudio by viewModel.audioRecorderHelper.isRecording.collectAsStateWithLifecycle()

    var transcribedText by remember { mutableStateOf("") }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isRecordAudioFileChecked by remember { mutableStateOf(true) }
    var recordingTimerSeconds by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.speechService.startListening { result ->
                transcribedText = result
            }
            if (isRecordAudioFileChecked) {
                viewModel.audioRecorderHelper.startRecording()
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de micrófono para el dictado por voz", Toast.LENGTH_SHORT).show()
        }
    }

    // Timer effect while listening or recording
    LaunchedEffect(isListening, isRecordingAudio) {
        if (isListening || isRecordingAudio) {
            recordingTimerSeconds = 0
            while (true) {
                delay(1000)
                recordingTimerSeconds++
                viewModel.audioRecorderHelper.updateAmplitude()
            }
        }
    }

    // Auto-update transcribed text from partial recognition
    LaunchedEffect(recognitionState) {
        when (val state = recognitionState) {
            is SpeechRecognitionState.Listening -> {
                if (state.partialText.isNotBlank()) {
                    transcribedText = state.partialText
                }
            }
            is SpeechRecognitionState.Success -> {
                transcribedText = state.recognizedText
            }
            else -> {}
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) (1f + (rmsDb * 0.45f).coerceIn(0f, 0.5f)) else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pulseScale"
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (isListening) viewModel.speechService.stopListening()
            if (isRecordingAudio) viewModel.audioRecorderHelper.stopRecording()
            viewModel.speechService.resetState()
            onDismissRequest()
        },
        containerColor = Slate900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Slate600) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "Dictado por Voz Android (STT)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Objetivo: $initialTargetField",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (isListening) viewModel.speechService.stopListening()
                        if (isRecordingAudio) viewModel.audioRecorderHelper.stopRecording()
                        viewModel.speechService.resetState()
                        onDismissRequest()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Microphone Animated Button & Wave Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Outer glowing animated halo
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        CyanAccent.copy(alpha = 0.35f),
                                        IndigoPrimary.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Main Mic Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) {
                                Brush.linearGradient(listOf(RoseHighlight, AmberWarning))
                            } else {
                                Brush.linearGradient(listOf(IndigoPrimary, CyanDark))
                            }
                        )
                        .clickable {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                if (isListening) {
                                    viewModel.speechService.stopListening()
                                    if (isRecordingAudio) viewModel.audioRecorderHelper.stopRecording()
                                } else {
                                    viewModel.speechService.startListening { result ->
                                        transcribedText = result
                                    }
                                    if (isRecordAudioFileChecked) {
                                        viewModel.audioRecorderHelper.startRecording()
                                    }
                                }
                            }
                        }
                        .testTag("voice_dictation_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Detener Dictado" else "Iniciar Dictado",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State & Timer indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isListening -> EmeraldSuccess
                                recognitionState is SpeechRecognitionState.Error -> RoseHighlight
                                else -> Slate400
                            }
                        )
                )
                Text(
                    text = when (val state = recognitionState) {
                        is SpeechRecognitionState.Listening -> "Escuchando y procesando voz... (${String.format("%02d:%02d", recordingTimerSeconds / 60, recordingTimerSeconds % 60)})"
                        is SpeechRecognitionState.Success -> "¡Dictado capturado exitosamente!"
                        is SpeechRecognitionState.Error -> state.message
                        is SpeechRecognitionState.Ready -> "Preparando micrófono..."
                        SpeechRecognitionState.Idle -> "Toca el micrófono para dictar tus ideas"
                    },
                    fontSize = 13.sp,
                    color = if (recognitionState is SpeechRecognitionState.Error) RoseHighlight else Slate300,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Waveform Bars
            WaveformBars(
                isActive = isListening,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(30.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Transcription Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 180.dp),
                color = Slate800.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (transcribedText.isBlank()) {
                        Text(
                            text = "Tu voz transcrita aparecerá aquí en tiempo real utilizando el motor Speech-to-Text de Android...",
                            color = Slate500,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Text(
                            text = transcribedText,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save audio file toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Text("Guardar archivo de audio de la nota (.m4a)", fontSize = 12.sp, color = Slate300)
                }
                Switch(
                    checked = isRecordAudioFileChecked,
                    onCheckedChange = { isRecordAudioFileChecked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        transcribedText = ""
                        viewModel.speechService.resetState()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpiar", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (isListening) viewModel.speechService.stopListening()
                        val audioFile = if (isRecordingAudio) viewModel.audioRecorderHelper.stopRecording() else null
                        if (transcribedText.isNotBlank()) {
                            onTextCaptured(transcribedText)
                            Toast.makeText(context, "Texto insertado", Toast.LENGTH_SHORT).show()
                            viewModel.speechService.resetState()
                            onDismissRequest()
                        } else {
                            Toast.makeText(context, "No hay texto dictado para insertar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = transcribedText.isNotBlank(),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("apply_transcription_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aplicar al Apunte", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
