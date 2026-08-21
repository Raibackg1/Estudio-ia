package com.example.ui.screens.pdf

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.sample.AcademicDocument
import com.example.service.WhatsAppShareHelper
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDoc by viewModel.selectedDoc.collectAsStateWithLifecycle()
    val currentPageIndex by viewModel.currentPageIndex.collectAsStateWithLifecycle()
    val isPdfSpeaking by viewModel.isPdfSpeaking.collectAsStateWithLifecycle()
    val aiExplanationModal by viewModel.aiExplanationModal.collectAsStateWithLifecycle()
    val isToolLoading by viewModel.isToolLoading.collectAsStateWithLifecycle()
    val allHighlights by viewModel.allHighlights.collectAsStateWithLifecycle()
    val academicDocs by viewModel.academicDocs.collectAsStateWithLifecycle()

    var showDocumentPickerSheet by remember { mutableStateOf(false) }
    var showHighlightsSheet by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var customHighlightText by remember { mutableStateOf("") }
    var selectedHighlightColor by remember { mutableStateOf("#FEF08A") }
    var fontSizeSp by remember { mutableStateOf(15.sp) }

    val context = LocalContext.current

    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val fileName = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Mi Documento"
                if (content.isNotBlank()) {
                    viewModel.importCustomDocument(
                        title = fileName,
                        subject = "Documento Importado",
                        content = content,
                        author = "Estudiante"
                    )
                    android.widget.Toast.makeText(context, "¡Documento importado con éxito!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Archivo cargado para análisis", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Documento añadido a tu biblioteca", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val docHighlights = allHighlights.filter { it.documentTitle == selectedDoc.title }
    val currentPage = selectedDoc.pages.getOrNull(currentPageIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedDoc.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${selectedDoc.subject} • Pág. ${currentPageIndex + 1} de ${selectedDoc.totalPages}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Document Selector
                    IconButton(onClick = { showDocumentPickerSheet = true }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Cambiar Libro")
                    }
                    // Saved Highlights
                    IconButton(onClick = { showHighlightsSheet = true }) {
                        Badge(containerColor = AmberWarning) {
                            Icon(Icons.Default.BorderColor, contentDescription = "Mis Resaltados")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Document Player & Highlighting Controls Bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Quick Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page
                        IconButton(
                            onClick = { viewModel.goToPage(currentPageIndex - 1) },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
                        }

                        // Read Aloud / Pause TTS
                        Button(
                            onClick = { viewModel.readCurrentPageAloud() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPdfSpeaking) RoseHighlight else IndigoPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("pdf_tts_btn")
                        ) {
                            Icon(
                                imageVector = if (isPdfSpeaking) Icons.Default.Pause else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPdfSpeaking) "Pausar Lectura" else "Leer Página (Voz)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Next Page
                        IconButton(
                            onClick = { viewModel.goToPage(currentPageIndex + 1) },
                            enabled = currentPageIndex < selectedDoc.totalPages - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente")
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chapter Title & Quick Tool Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPage?.chapterTitle ?: "Lectura",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Resumir Página
                        FilledTonalButton(
                            onClick = {
                                currentPage?.let {
                                    viewModel.explainSnippet("Resumen estructurado y conceptos fundamentales de: ${it.contentText}")
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resumir", fontSize = 11.sp)
                        }

                        // Enviar a Notas
                        IconButton(
                            onClick = {
                                currentPage?.let {
                                    viewModel.saveNote(
                                        title = "${selectedDoc.title} - ${it.chapterTitle}",
                                        content = it.contentText,
                                        category = selectedDoc.subject,
                                        summary = "Apunte extraído del capítulo ${currentPageIndex + 1}"
                                    )
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Guardar en Notas", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // PDF Academic Page Content Reader
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                item {
                    // Academic Paper Canvas styling
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Text paragraphs
                            val paragraphs = currentPage?.contentText?.split("\n\n") ?: emptyList()
                            paragraphs.forEach { paragraph ->
                                Text(
                                    text = paragraph.trim(),
                                    fontSize = fontSizeSp,
                                    lineHeight = 24.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            customHighlightText = paragraph.trim()
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Key Terms Pill Tags
                            Text(
                                text = "🏷️ Conceptos Clave de la Página:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(currentPage?.keyTerms ?: emptyList()) { term ->
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.explainSnippet("Explica el concepto clave '$term' en el contexto de ${selectedDoc.title}")
                                        },
                                        label = { Text(term, fontSize = 11.sp) },
                                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Review Questions
                            Text(
                                text = "❓ Preguntas de Autoevaluación:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            currentPage?.reviewQuestions?.forEach { q ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            viewModel.explainSnippet("Responde a la siguiente pregunta de autoevaluación: $q")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                                        Text(q, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selection Action Bar / Bottom Sheet when paragraph selected
        if (customHighlightText.isNotBlank()) {
            AlertDialog(
                onDismissRequest = { customHighlightText = "" },
                title = { Text("Acciones sobre el Párrafo", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "\"${customHighlightText.take(120)}...\"",
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Text("Selecciona color de resaltado:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "#FEF08A" to HighlightYellow,
                                "#BBF7D0" to HighlightGreen,
                                "#BAE6FD" to HighlightBlue,
                                "#FBCFE8" to HighlightPink,
                                "#E9D5FF" to HighlightPurple
                            ).forEach { (hex, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedHighlightColor == hex) 3.dp else 1.dp,
                                            color = if (selectedHighlightColor == hex) Slate900 else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedHighlightColor = hex }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.explainSnippet(customHighlightText)
                            val text = customHighlightText
                            customHighlightText = ""
                        }
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explicar con IA")
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveHighlight(customHighlightText, selectedHighlightColor)
                                customHighlightText = ""
                            }
                        ) {
                            Text("Resaltar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        WhatsAppShareButton(
                            title = selectedDoc.title,
                            content = customHighlightText,
                            category = selectedDoc.subject,
                            label = "WhatsApp"
                        )
                    }
                }
            )
        }

        // AI Deep Explanation Bottom Sheet Modal
        if (aiExplanationModal != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeExplanationModal() },
                containerColor = Slate900,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                            Text("Explicación del Tutor IA", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        IconButton(onClick = { viewModel.speechService.speak(aiExplanationModal ?: "") }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = CyanAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .padding(14.dp)
                        ) {
                            item {
                                if (isToolLoading) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(20.dp))
                                        Text("Sofía está analizando y desglosando el concepto...", color = Slate200, fontSize = 13.sp)
                                    }
                                } else {
                                    Text(
                                        text = aiExplanationModal ?: "",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WhatsAppShareButton(
                            title = "Explicación: ${selectedDoc.title}",
                            content = aiExplanationModal ?: "",
                            category = selectedDoc.subject,
                            label = "Enviar a WhatsApp"
                        )

                        Button(
                            onClick = {
                                viewModel.saveNote(
                                    title = "Explicación: ${selectedDoc.title}",
                                    content = aiExplanationModal ?: "",
                                    category = selectedDoc.subject
                                )
                                viewModel.closeExplanationModal()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar en Notas")
                        }
                    }
                }
            }
        }

        // Library Document Picker Sheet
        if (showDocumentPickerSheet) {
            ModalBottomSheet(onDismissRequest = { showDocumentPickerSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Biblioteca y Lecturas", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // File picker button
                        Button(
                            onClick = {
                                showDocumentPickerSheet = false
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cargar PDF/Texto", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(academicDocs) { doc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectDocument(doc)
                                        showDocumentPickerSheet = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (doc.id == selectedDoc.id) IndigoContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = IndigoPrimary)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${doc.subject} • ${doc.totalPages} páginas • ${doc.author}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Highlights Drawer Sheet
        if (showHighlightsSheet) {
            ModalBottomSheet(onDismissRequest = { showHighlightsSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mis Resaltados en este Libro (${docHighlights.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (docHighlights.isEmpty()) {
                        Text("Aún no tienes párrafos resaltados. Toca cualquier párrafo para guardarlo.", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(docHighlights) { hl ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Página ${hl.pageNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IndigoPrimary)
                                            IconButton(
                                                onClick = { viewModel.deleteHighlight(hl) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RoseHighlight, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Text("\"${hl.selectedText}\"", fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
