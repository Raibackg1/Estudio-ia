package com.example.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.service.StudyShareIntentHandler
import com.example.ui.components.VoiceDictationSheet
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val allStudySessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val totalStudySeconds by viewModel.totalStudySeconds.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Apuntes, 1 = Sesiones de Estudio
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    var showCreateNoteSheet by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var showVoiceDictationSheet by remember { mutableStateOf(false) }
    var voiceTargetField by remember { mutableStateOf("Contenido de Nota") }
    var showLogSessionDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Física", "Matemáticas", "Biología", "Ciencias", "Historia", "General")

    val filteredNotes = allNotes.filter { note ->
        (if (showOnlyFavorites) note.isFavorite else true) &&
        (selectedCategory == "Todos" || note.category.equals(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isBlank() || note.title.contains(searchQuery, ignoreCase = true) || note.content.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Cuaderno de Apuntes" else "Sesiones de Estudio Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Persistencia Local Room DB • Dictado STT Android",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                },
                actions = {
                    // Quick Speech-to-Text Voice Dictation Trigger
                    IconButton(
                        onClick = {
                            voiceTargetField = "Nueva Nota Rápida"
                            showVoiceDictationSheet = true
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .background(IndigoContainer)
                            .testTag("open_voice_dictation_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Dictado de Voz STT",
                            tint = IndigoPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary FAB for Voice Note Dictation
                SmallFloatingActionButton(
                    onClick = {
                        voiceTargetField = "Nota por Voz"
                        showVoiceDictationSheet = true
                    },
                    containerColor = CyanAccent,
                    contentColor = Slate900,
                    shape = CircleShape,
                    modifier = Modifier.testTag("dictate_voice_fab")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Dictar Nota por Voz")
                }

                // Primary FAB for Note Creation or Log Session
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 0) {
                            noteToEdit = null
                            showCreateNoteSheet = true
                        } else {
                            showLogSessionDialog = true
                        }
                    },
                    containerColor = IndigoPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Add else Icons.Default.Timer,
                        contentDescription = "Crear"
                    )
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
            // Main Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IndigoPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Apuntes (${allNotes.size})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Sesiones (${allStudySessions.size})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Search Bar & Filter by Favorites
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar en mis apuntes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("★", fontSize = 16.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                            selectedLabelColor = AmberWarning
                        )
                    )
                }

                // Category Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Notes List
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = Slate400, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No hay apuntes disponibles.", color = Slate600, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Dicta tus ideas con el micrófono o presiona '+' para crear un apunte con IA.", color = Slate400, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        noteToEdit = note
                                        showCreateNoteSheet = true
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(note.category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                colors = AssistChipDefaults.assistChipColors(containerColor = IndigoContainer)
                                            )
                                            if (note.templateType == "VoiceNote" || note.audioPath != null) {
                                                Surface(
                                                    color = CyanAccent.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(Icons.Default.Mic, contentDescription = null, tint = CyanDark, modifier = Modifier.size(12.dp))
                                                        Text("Voz STT", fontSize = 10.sp, color = CyanDark, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            // Favorite Toggle
                                            IconButton(
                                                onClick = { viewModel.toggleNoteFavorite(note) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                                    contentDescription = "Favorito",
                                                    tint = if (note.isFavorite) AmberWarning else Slate400,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Read note aloud with TTS
                                            IconButton(
                                                onClick = {
                                                    viewModel.speechService.speak("${note.title}. ${note.content}")
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar nota", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                                            }

                                            // Delete note
                                            IconButton(
                                                onClick = { viewModel.deleteNote(note) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = RoseHighlight, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = note.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = note.content,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 18.sp
                                    )

                                    if (note.summary.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanDark, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "Resumen IA: ${note.summary}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Share to WhatsApp Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(note.updatedAt)),
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )

                                        WhatsAppShareButton(
                                            title = note.title,
                                            content = "${note.content}\n\n*Resumen:* ${note.summary}",
                                            category = note.category,
                                            label = "WhatsApp"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Study Sessions Room DB View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Summary Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IndigoDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tiempo Total de Estudio", color = Slate300, fontSize = 12.sp)
                                val totalSecs = totalStudySeconds ?: 0
                                val hours = totalSecs / 3600
                                val mins = (totalSecs % 3600) / 60
                                Text(
                                    text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m ${totalSecs % 60}s",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Guardado en Room SQLite Offline", color = CyanAccent, fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = { showLogSessionDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = IndigoPrimary, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Registrar", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Historial de Sesiones Registradas", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    if (allStudySessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay sesiones de estudio registradas todavía.", color = Slate500, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allStudySessions, key = { it.id }) { session ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text(session.sessionType, fontSize = 10.sp) }
                                                )
                                                Text(
                                                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(session.completedAt)),
                                                    fontSize = 11.sp,
                                                    color = Slate400
                                                )
                                            }
                                            Text(session.topic, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            if (session.keyLearnings.isNotBlank()) {
                                                Text(session.keyLearnings, fontSize = 12.sp, color = Slate600, maxLines = 2)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${session.durationSeconds / 60}m",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = IndigoPrimary
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteStudySession(session.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = RoseHighlight, modifier = Modifier.size(16.dp))
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

        // Voice Dictation Modal Sheet (Speech-to-Text)
        if (showVoiceDictationSheet) {
            VoiceDictationSheet(
                viewModel = viewModel,
                initialTargetField = voiceTargetField,
                onTextCaptured = { transcribed ->
                    // Auto save as voice note in Room
                    viewModel.saveVoiceNote(
                        title = "Dictado: ${transcribed.take(30)}...",
                        content = transcribed,
                        audioPath = null,
                        category = selectedCategory.takeIf { it != "Todos" } ?: "General"
                    )
                    showVoiceDictationSheet = false
                },
                onDismissRequest = { showVoiceDictationSheet = false }
            )
        }

        // Create / Edit Note Sheet
        if (showCreateNoteSheet) {
            var titleInput by remember { mutableStateOf(noteToEdit?.title ?: "") }
            var contentInput by remember { mutableStateOf(noteToEdit?.content ?: "") }
            var categoryInput by remember { mutableStateOf(noteToEdit?.category ?: "General") }
            var summaryInput by remember { mutableStateOf(noteToEdit?.summary ?: "") }
            var templateInput by remember { mutableStateOf(noteToEdit?.templateType ?: "Standard") }
            var isFavoriteInput by remember { mutableStateOf(noteToEdit?.isFavorite ?: false) }
            var isGeneratingSummary by remember { mutableStateOf(false) }
            var showDictateInForm by remember { mutableStateOf(false) }

            ModalBottomSheet(
                onDismissRequest = { showCreateNoteSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
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
                        Text(
                            text = if (noteToEdit != null) "Editar Apunte" else "Crear Nuevo Apunte",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        // Voice Dictate button for note content
                        FilledTonalIconButton(
                            onClick = { showDictateInForm = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = IndigoContainer)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Dictar por voz", tint = IndigoPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título de la Nota") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Física", "Matemáticas", "Biología", "Historia", "General").forEach { cat ->
                            FilterChip(
                                selected = categoryInput == cat,
                                onClick = { categoryInput = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Templates row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Plantilla:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        listOf("Standard", "Cornell", "Feynman").forEach { tmpl ->
                            SuggestionChip(
                                onClick = {
                                    templateInput = tmpl
                                    if (tmpl == "Cornell" && contentInput.isBlank()) {
                                        contentInput = "📌 IDEAS CLAVE / PREGUNTAS:\n- \n\n📝 NOTAS DE CLASE:\n- \n\n🎯 RESUMEN FINAL:\n- "
                                    } else if (tmpl == "Feynman" && contentInput.isBlank()) {
                                        contentInput = "👶 EXPLICACIÓN SIMPLE (Como a un niño):\n- \n\n🔍 ANALOGÍA COTIDIANA:\n- \n\n⚠️ LAGUNAS DE CONOCIMIENTO A REFORZAR:\n- "
                                    }
                                },
                                label = { Text(tmpl, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Content Field
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Contenido de los apuntes (o dicta usando el micrófono)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI Auto-Summarize Button & Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (contentInput.isNotBlank()) {
                                    isGeneratingSummary = true
                                    viewModel.generateNoteSummary(contentInput) { summary ->
                                        summaryInput = summary
                                        isGeneratingSummary = false
                                    }
                                }
                            }
                        ) {
                            if (isGeneratingSummary) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resumiendo...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Resumen IA", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.saveNote(
                                    id = noteToEdit?.id ?: 0L,
                                    title = titleInput,
                                    content = contentInput,
                                    category = categoryInput,
                                    summary = summaryInput,
                                    templateType = templateInput,
                                    isFavorite = isFavoriteInput
                                )
                                showCreateNoteSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text("Guardar en Room")
                        }
                    }

                    if (summaryInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Resumen generado: $summaryInput", fontSize = 12.sp, color = IndigoDark)
                    }

                    if (showDictateInForm) {
                        VoiceDictationSheet(
                            viewModel = viewModel,
                            initialTargetField = "Contenido de Apuntes",
                            onTextCaptured = { text ->
                                contentInput = if (contentInput.isBlank()) text else "$contentInput\n$text"
                                showDictateInForm = false
                            },
                            onDismissRequest = { showDictateInForm = false }
                        )
                    }
                }
            }
        }

        // Log Manual Study Session Dialog
        if (showLogSessionDialog) {
            var sessionTopic by remember { mutableStateOf("") }
            var sessionTypeInput by remember { mutableStateOf("Estudio Offline") }
            var durationMinutesInput by remember { mutableStateOf("30") }
            var keyLearningsInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showLogSessionDialog = false },
                title = { Text("Registrar Sesión de Estudio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sessionTopic,
                            onValueChange = { sessionTopic = it },
                            label = { Text("Tema o Materia") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = durationMinutesInput,
                            onValueChange = { durationMinutesInput = it },
                            label = { Text("Duración (minutos)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = keyLearningsInput,
                            onValueChange = { keyLearningsInput = it },
                            label = { Text("Aprendizajes Clave") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mins = durationMinutesInput.toIntOrNull() ?: 30
                            viewModel.logStudySession(
                                sessionType = sessionTypeInput,
                                topic = sessionTopic.ifBlank { "Sesión de Repaso" },
                                durationSeconds = mins * 60,
                                keyLearnings = keyLearningsInput
                            )
                            showLogSessionDialog = false
                        }
                    ) {
                        Text("Guardar Sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogSessionDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
