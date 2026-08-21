package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.service.StudyShareIntentHandler
import com.example.ui.theme.*

data class SlideItem(
    val slideNumber: Int,
    val title: String,
    val bulletPoints: List<String>,
    val speakerNote: String = ""
)

enum class SlideTheme(val displayName: String, val bgBrush: Brush, val textColor: Color, val accentColor: Color) {
    INDIGO("Índigo Profundo", Brush.linearGradient(listOf(Slate900, Color(0xFF1E1B4B))), Color.White, CyanAccent),
    EMERALD("Esmeralda Zen", Brush.linearGradient(listOf(Slate900, Color(0xFF064E3B))), Color.White, EmeraldSuccess),
    SUNSET("Atardecer Cálido", Brush.linearGradient(listOf(Slate900, Color(0xFF4C1D24))), Color.White, RoseHighlight),
    SLATE("Modo Minimalista", Brush.linearGradient(listOf(Slate900, Slate800)), Color.White, AmberWarning)
}

@Composable
fun InteractiveSlidesViewer(
    topic: String,
    slides: List<SlideItem>,
    onSaveToNotes: (String, String) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSlideIndex by remember { mutableStateOf(0) }
    var currentTheme by remember { mutableStateOf(SlideTheme.INDIGO) }
    var showSpeakerNotes by remember { mutableStateOf(false) }

    val currentSlide = slides.getOrNull(currentSlideIndex) ?: return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar: Topic + Theme selector + Slide Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📽️ Diapositivas: $topic",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "Diapositiva ${currentSlideIndex + 1} de ${slides.size}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                // Theme switcher icon
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SlideTheme.values().forEach { theme ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor)
                                .clickable { currentTheme = theme }
                                .padding(if (currentTheme == theme) 2.dp else 0.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Main Slide Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(currentTheme.bgBrush)
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Slide Title
                    Text(
                        text = currentSlide.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.accentColor,
                        lineHeight = 22.sp
                    )

                    Divider(color = currentTheme.accentColor.copy(alpha = 0.3f), thickness = 1.dp)

                    // Bullets
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentSlide.bulletPoints.forEach { point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", fontSize = 16.sp, color = currentTheme.accentColor, fontWeight = FontWeight.Bold)
                                Text(
                                    text = point,
                                    fontSize = 13.sp,
                                    color = currentTheme.textColor,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Speaker Notes Expandable
            if (currentSlide.speakerNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = showSpeakerNotes) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🎙️ Notas del Orador (Guion de Exposición):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmberWarning)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentSlide.speakerNote, fontSize = 11.sp, color = Slate200, lineHeight = 16.sp)
                        }
                    }
                }

                TextButton(
                    onClick = { showSpeakerNotes = !showSpeakerNotes },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = if (showSpeakerNotes) "Ocultar notas del orador" else "Ver notas del orador",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Controls (Prev / Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentSlideIndex > 0) currentSlideIndex-- },
                    enabled = currentSlideIndex > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Anterior", tint = if (currentSlideIndex > 0) Color.White else Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slides.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentSlideIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (index == currentSlideIndex) currentTheme.accentColor else Slate600)
                                .clickable { currentSlideIndex = index }
                        )
                    }
                }

                IconButton(
                    onClick = { if (currentSlideIndex < slides.size - 1) currentSlideIndex++ },
                    enabled = currentSlideIndex < slides.size - 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Siguiente", tint = if (currentSlideIndex < slides.size - 1) Color.White else Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Slate700, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: WhatsApp Share, TTS Voice, Save to Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Button
                Button(
                    onClick = {
                        val slideTuples = slides.map { Pair(it.title, it.bulletPoints) }
                        StudyShareIntentHandler.shareSlidesPresentation(context, topic, slideTuples)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_share_slides_wsp")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Speak slide content
                    IconButton(
                        onClick = {
                            val textToSpeak = "${currentSlide.title}. " + currentSlide.bulletPoints.joinToString(". ") + ". " + currentSlide.speakerNote
                            onSpeak(textToSpeak)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = CyanAccent)
                    }

                    // Save to notes
                    FilledTonalButton(
                        onClick = {
                            val fullContent = slides.joinToString("\n\n") { slide ->
                                "## Diapositiva ${slide.slideNumber}: ${slide.title}\n" +
                                        slide.bulletPoints.joinToString("\n") { "• $it" } +
                                        if (slide.speakerNote.isNotBlank()) "\n_Guion:_ ${slide.speakerNote}" else ""
                            }
                            onSaveToNotes("Presentación: $topic", fullContent)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
