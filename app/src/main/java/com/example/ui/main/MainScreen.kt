package com.example.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.assistant.AssistantPlannerScreen
import com.example.ui.screens.call.TutorCallScreen
import com.example.ui.screens.flashcards.FlashcardsQuizScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.pdf.PdfReaderScreen
import com.example.ui.screens.tools.ToolsHubScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

enum class MainNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CALL("Llamada", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent, "nav_call"),
    ASSISTANT("Hábitos & Agenda", Icons.Filled.Spa, Icons.Outlined.Spa, "nav_assistant"),
    PDF("Lectura PDF", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_pdf"),
    NOTES("Apuntes", Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_notes"),
    TOOLS("30+ Herramientas", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_tools")
}

@Composable
fun MainScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(MainNavDestination.CALL) }
    val isSpeaking by viewModel.speechService.isSpeaking.collectAsStateWithLifecycle()
    val isPomodoroRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val pomodoroSeconds by viewModel.pomodoroSecondsRemaining.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Column {
                // Persistent Floating Audio / Pomodoro Bar if running
                if (isSpeaking || isPomodoroRunning) {
                    Surface(
                        color = Slate900,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpeaking) CyanAccent else EmeraldSuccess)
                                )
                                Text(
                                    text = if (isSpeaking) "🗣️ Sofía leyendo en voz alta..." else "⏳ Pomodoro activo: ${pomodoroSeconds / 60}:${pomodoroSeconds % 60}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (isSpeaking) {
                                TextButton(
                                    onClick = { viewModel.speechService.stopSpeaking() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Detener", color = RoseHighlight, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Material 3 Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    MainNavDestination.values().forEach { destination ->
                        val selected = currentDestination == destination
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title,
                                    tint = if (selected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                MainNavDestination.CALL -> TutorCallScreen(viewModel = viewModel)
                MainNavDestination.ASSISTANT -> AssistantPlannerScreen(viewModel = viewModel)
                MainNavDestination.PDF -> PdfReaderScreen(viewModel = viewModel)
                MainNavDestination.NOTES -> NotesScreen(viewModel = viewModel)
                MainNavDestination.TOOLS -> ToolsHubScreen(viewModel = viewModel)
            }
        }
    }
}
