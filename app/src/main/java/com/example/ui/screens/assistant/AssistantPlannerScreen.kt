package com.example.ui.screens.assistant

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AcademicReminderEntity
import com.example.service.WhatsAppShareHelper
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantPlannerScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val todayWellness by viewModel.todayWellness.collectAsStateWithLifecycle()
    val liveSensorSteps by viewModel.stepSensorHelper.liveSteps.collectAsState()
    val studentPhone by viewModel.studentPhoneNumber.collectAsState()
    val totalStudySeconds by viewModel.totalStudySeconds.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val isToolLoading by viewModel.isToolLoading.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("Todos") }
    var showAddReminderModal by remember { mutableStateOf(false) }
    var showMealLogModal by remember { mutableStateOf(false) }
    var showActiveBreakModal by remember { mutableStateOf(false) }
    var showAiPlanModal by remember { mutableStateOf(false) }
    var aiPlanContent by remember { mutableStateOf("") }
    var selectedReminderForPlan by remember { mutableStateOf<AcademicReminderEntity?>(null) }
    var showPhoneConfigModal by remember { mutableStateOf(false) }

    // Start sensor listener on entry
    LaunchedEffect(Unit) {
        viewModel.stepSensorHelper.startListening(todayWellness?.stepsCount ?: 0)
    }

    val waterGlasses = todayWellness?.waterGlasses ?: 0
    val waterTarget = todayWellness?.waterTargetGlasses ?: 8
    val steps = (todayWellness?.stepsCount ?: 0).coerceAtLeast(liveSensorSteps)
    val stepsTarget = todayWellness?.stepsTarget ?: 6000
    val breaksCount = todayWellness?.activeBreaksCount ?: 0

    val filteredReminders = remember(allReminders, selectedFilter) {
        if (selectedFilter == "Todos") {
            allReminders
        } else {
            allReminders.filter { it.type.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldSuccess)
                        Text("Mi Asistente & Hábitos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    // WhatsApp Fast Digest Button
                    IconButton(
                        onClick = { viewModel.shareDailyDigestToWhatsApp(context) },
                        modifier = Modifier.testTag("btn_share_daily_wsp")
                    ) {
                        Badge(containerColor = WhatsAppGreen) {
                            Icon(Icons.Default.Share, contentDescription = "Enviar reporte a WhatsApp", tint = Color.White)
                        }
                    }

                    // Phone Settings
                    IconButton(onClick = { showPhoneConfigModal = true }) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = "Configurar Celular")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddReminderModal = true },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_reminder")
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = "Añadir Evento o Recordatorio")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student Brain Health & Habits Dashboard Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text("Rendimiento Cognitivo Hoy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Cuidado físico = Mayor retención y memoria", color = Slate400, fontSize = 11.sp)
                                }
                            }

                            // Active study minutes pill
                            Surface(
                                color = IndigoPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "⏱️ ${(totalStudySeconds ?: 0) / 60} min",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Column Grid: Water / Steps / Active Breaks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Water Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$waterGlasses / $waterTarget", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Vasos (${waterGlasses * 250}ml)", color = Slate400, fontSize = 10.sp)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { viewModel.removeWaterGlass() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos agua", tint = Slate400, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.addWaterGlass() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más agua", tint = CyanAccent, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            // Steps Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$steps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Meta: $stepsTarget", color = Slate400, fontSize = 10.sp)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = (steps.toFloat() / stepsTarget.toFloat()).coerceIn(0f, 1f),
                                        color = EmeraldSuccess,
                                        trackColor = Slate700,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }

                            // Active Breaks & Meals Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$breaksCount Pausas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Estiramientos", color = Slate400, fontSize = 10.sp)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = { showActiveBreakModal = true },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Pausar (2m)", fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Food / Nutrition Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800)
                                .clickable { showMealLogModal = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = RoseHighlight, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (todayWellness?.mealsLoggedText.isNullOrBlank()) "Registrar comida o snack saludable..." else "${todayWellness?.mealsCount} comidas registradas",
                                    color = Slate200,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Academic Calendar & Reminders Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Calendario & Recordatorios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${filteredReminders.count { !it.isCompleted }} eventos pendientes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Filter chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(listOf("Todos", "Examen", "Entrega", "Tarea", "Repaso")) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Reminders List
            if (filteredReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay eventos en esta categoría", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Toca el botón '+' para programar un parcial, entrega de trabajo o sesión de repaso.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredReminders) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        onToggleCompleted = { viewModel.toggleReminderCompleted(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) },
                        onShareToWhatsApp = { viewModel.shareReminderToWhatsApp(context, reminder) },
                        onPlanWithAi = {
                            selectedReminderForPlan = reminder
                            viewModel.planExamWithAi(
                                examTitle = reminder.title,
                                subject = reminder.subject,
                                examDate = reminder.dueDate
                            ) { plan ->
                                aiPlanContent = plan
                                showAiPlanModal = true
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Reminder Modal
    if (showAddReminderModal) {
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Física") }
        var type by remember { mutableStateOf("Examen") }
        var dueDate by remember { mutableStateOf("2026-08-25") }
        var dueTime by remember { mutableStateOf("09:00") }
        var priority by remember { mutableStateOf("Alta") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddReminderModal = false },
            title = { Text("Nuevo Evento Académico", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título del Evento / Examen") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Materia") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Tipo (Examen/Tarea)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Fecha (AAAA-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dueTime,
                            onValueChange = { dueTime = it },
                            label = { Text("Hora (HH:MM)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Temas a evaluar o apuntes clave") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.saveReminder(
                                title = title,
                                subject = subject,
                                type = type,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                priority = priority,
                                notes = notes
                            )
                            showAddReminderModal = false
                        }
                    }
                ) {
                    Text("Guardar Evento")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Meal Log Modal
    if (showMealLogModal) {
        var mealType by remember { mutableStateOf("Almuerzo") }
        var mealDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showMealLogModal = false },
            title = { Text("Registrar Nutrición de Estudio", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Una nutrición rica en omega-3, proteínas y glucosa estable optimiza la memoria a largo plazo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Desayuno", "Almuerzo", "Merienda", "Cena").forEach { type ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = mealDesc,
                        onValueChange = { mealDesc = it },
                        label = { Text("¿Qué comiste? (ej. Avena, Frutos secos, Pescado)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mealDesc.isNotBlank()) {
                            viewModel.logMealEntry(mealType, mealDesc)
                            showMealLogModal = false
                        }
                    }
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMealLogModal = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Active Break Modal
    if (showActiveBreakModal) {
        var breakSecondsRemaining by remember { mutableStateOf(120) }
        var isTimerRunning by remember { mutableStateOf(true) }

        LaunchedEffect(isTimerRunning) {
            while (isTimerRunning && breakSecondsRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                breakSecondsRemaining--
            }
            if (breakSecondsRemaining == 0) {
                viewModel.addActiveBreak()
            }
        }

        AlertDialog(
            onDismissRequest = { showActiveBreakModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = AmberWarning)
                    Text("Pausa Activa de 2 Minutos", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${breakSecondsRemaining / 60}:${String.format("%02d", breakSecondsRemaining % 60)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🧘 Ejercicios Guiados:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Regla 20-20-20: Mira a 6 metros de distancia durante 20 segundos para relajar el cristalino.", fontSize = 12.sp)
                            Text("2. Cuello: Gira la cabeza suavemente en círculos lentos hacia ambos lados.", fontSize = 12.sp)
                            Text("3. Espalda: Estira los brazos por encima de la cabeza e inhala profundamente.", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addActiveBreak()
                        showActiveBreakModal = false
                    }
                ) {
                    Text("¡Pausa Completada!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActiveBreakModal = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Phone Config Modal
    if (showPhoneConfigModal) {
        var tempPhone by remember { mutableStateOf(studentPhone) }

        AlertDialog(
            onDismissRequest = { showPhoneConfigModal = false },
            title = { Text("Configurar WhatsApp del Estudiante", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ingresa tu número con código de país para enviarte resúmenes diarios, alertas de exámenes y tareas directamente a tu WhatsApp.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = { Text("Número de WhatsApp (+549..., +52..., etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudentPhoneNumber(tempPhone)
                        showPhoneConfigModal = false
                    }
                ) {
                    Text("Guardar Número")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneConfigModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // AI Study Plan Modal
    if (showAiPlanModal) {
        ModalBottomSheet(
            onDismissRequest = { showAiPlanModal = false },
            containerColor = Slate900,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                        Text("Plan de Estudio de Sofía", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    }
                    IconButton(onClick = { viewModel.speechService.speak(aiPlanContent) }) {
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
                            Text(
                                text = aiPlanContent,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WhatsAppShareButton(
                        title = "Plan de Estudio: ${selectedReminderForPlan?.title}",
                        content = aiPlanContent,
                        category = selectedReminderForPlan?.subject ?: "Examen",
                        label = "Enviar Plan a WhatsApp"
                    )

                    Button(
                        onClick = {
                            selectedReminderForPlan?.let {
                                viewModel.saveNote(
                                    title = "Plan de Estudio: ${it.title}",
                                    content = aiPlanContent,
                                    category = it.subject,
                                    summary = "Cronograma de preparación para el examen de fecha ${it.dueDate}"
                                )
                            }
                            showAiPlanModal = false
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
}

@Composable
fun ReminderItemCard(
    reminder: AcademicReminderEntity,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onShareToWhatsApp: () -> Unit,
    onPlanWithAi: () -> Unit
) {
    val priorityColor = when (reminder.priority) {
        "Alta" -> RoseHighlight
        "Media" -> AmberWarning
        else -> CyanAccent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (reminder.isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = reminder.isCompleted,
                        onCheckedChange = { onToggleCompleted() }
                    )
                    Column {
                        Text(
                            text = reminder.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(reminder.subject, fontSize = 11.sp, color = IndigoPrimary, fontWeight = FontWeight.SemiBold)
                            Text("•", fontSize = 11.sp, color = Color.Gray)
                            Text(reminder.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("•", fontSize = 11.sp, color = Color.Gray)
                            Text("📅 ${reminder.dueDate} ${reminder.dueTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Priority Badge
                Surface(
                    color = priorityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = reminder.priority,
                        color = priorityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (reminder.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reminder.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Planner
                FilledTonalButton(
                    onClick = onPlanWithAi,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Plan IA", fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // WhatsApp Share
                    IconButton(
                        onClick = onShareToWhatsApp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = RoseHighlight, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
