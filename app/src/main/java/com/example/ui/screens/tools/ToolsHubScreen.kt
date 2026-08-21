package com.example.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.FocusSoundType
import com.example.service.WhatsAppShareHelper
import com.example.ui.components.InteractiveSlidesViewer
import com.example.ui.components.InteractiveStudyChart
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

data class StudentToolItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val promptTemplate: String,
    val typeCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val activeToolResult by viewModel.activeToolResult.collectAsStateWithLifecycle()
    val isToolLoading by viewModel.isToolLoading.collectAsStateWithLifecycle()
    val generatedSlides by viewModel.generatedSlides.collectAsStateWithLifecycle()
    val generatedSlidesTopic by viewModel.generatedSlidesTopic.collectAsStateWithLifecycle()
    val generatedChartPoints by viewModel.generatedChartPoints.collectAsStateWithLifecycle()
    val generatedChartTitle by viewModel.generatedChartTitle.collectAsStateWithLifecycle()
    val pomodoroSeconds by viewModel.pomodoroSecondsRemaining.collectAsStateWithLifecycle()
    val isPomodoroRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val pomodoroMode by viewModel.pomodoroMode.collectAsStateWithLifecycle()
    val currentFocusSound by viewModel.audioSoundtrackService.currentSound.collectAsStateWithLifecycle()

    var selectedToolCategory by remember { mutableStateOf("Todas") }
    var selectedToolToRun by remember { mutableStateOf<StudentToolItem?>(null) }
    var userPromptInput by remember { mutableStateOf("") }
    var showPomodoroSheet by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val allTools = remember {
        listOf(
            // 1-5: Ciencias & Matemáticas
            StudentToolItem("math_solver", "Resolvedor Paso a Paso", "Matemáticas", "Resuelve derivadas, integrales, matrices y ecuaciones desglosando cada paso.", Icons.Default.Calculate, IndigoPrimary, "Resuelve y explica detalladamente paso a paso: ", "SOLVER"),
            StudentToolItem("physics_lab", "Simulador de Física", "Física", "Calcula trayectorias, fuerzas, entropía y circuitos eléctricos.", Icons.Default.Science, CyanAccent, "Explica las leyes y fórmulas físicas aplicadas a: ", "SOLVER"),
            StudentToolItem("unit_converter", "Conversor Científico", "Matemáticas", "Conversión dimensional de unidades del SI y constantes físicas universales.", Icons.Default.SwapHoriz, EmeraldSuccess, "Realiza la conversión y explica el análisis dimensional de: ", "SOLVER"),
            StudentToolItem("bio_explainer", "Explorador Celular", "Biología", "Desglose de mitosis, síntesis de proteínas, genética y enzimas.", Icons.Default.Biotech, AmberWarning, "Explica el proceso biológico y su función fisiológica en: ", "FEYNMAN"),
            StudentToolItem("code_debugger", "Tutor de Programación", "Tecnología", "Depura algoritmos en Kotlin, Python, C++, Java y explica la complejidad O(N).", Icons.Default.Code, RoseHighlight, "Analiza, optimiza y explica este código: ", "CODE_TUTOR"),

            // 6-10: Generadores Multimedia & Presentaciones
            StudentToolItem("slides_creator", "Creador de Diapositivas IA", "Colaboración", "Diseña presentaciones con vista interactiva, temas visuales, notas del orador y envío a WhatsApp.", Icons.Default.Slideshow, CyanAccent, "Genera diapositivas sobre: ", "SLIDES"),
            StudentToolItem("charts_creator", "Generador de Gráficos IA", "Productividad", "Crea gráficos interactivos de barras, curvas de retención y proyecciones de estudio.", Icons.Default.BarChart, AmberWarning, "Genera un gráfico analítico sobre: ", "CHART"),
            StudentToolItem("pdf_book_creator", "Creador de Libros & PDFs", "Redacción", "Escribe tratados y guías completas de 3 capítulos y las importa a tu lector PDF con voz.", Icons.Default.MenuBook, EmeraldSuccess, "Escribe un tratado académico sobre: ", "PDF_DOC"),
            StudentToolItem("feynman_method", "Método Feynman", "Técnicas de Estudio", "Explica conceptos complejos en lenguaje ultra simple, como si tuvieras 10 años.", Icons.Default.ChildCare, IndigoLight, "Aplica el Método Feynman para explicar de forma sencilla: ", "FEYNMAN"),
            StudentToolItem("socratic_mode", "Modo Socrático", "Técnicas de Estudio", "No te da la respuesta directa; te guía con preguntas reflexivas paso a paso.", Icons.Default.Psychology, CyanDark, "Guíame en modo socrático sobre la siguiente pregunta: ", "SOCRATIC"),
            StudentToolItem("mind_map", "Mapa Conceptual IA", "Técnicas de Estudio", "Genera un árbol jerárquico de nodos y conexiones visuales de texto.", Icons.Default.AccountTree, EmeraldSuccess, "Genera un mapa mental jerárquico y estructurado sobre: ", "MINDMAP"),
            StudentToolItem("cornell_gen", "Estructurador Cornell", "Técnicas de Estudio", "Organiza cualquier apunte en preguntas clave, notas centrales y síntesis final.", Icons.Default.FormatListNumbered, AmberWarning, "Convierte este texto al formato Cornell de toma de apuntes: ", "CORNELL"),
            StudentToolItem("mnemonic_lab", "Generador de Mnemotecnias", "Técnicas de Estudio", "Crea acrónimos divertidos y rimas para memorizar listas en segundos.", Icons.Default.Lightbulb, RoseHighlight, "Crea reglas mnemotécnicas creativas para memorizar: ", "MNEMONIC"),

            // 11-15: Redacción, Ensayos & Tesis
            StudentToolItem("apa_citations", "Generador APA 7ma / IEEE", "Redacción", "Crea citas bibliográficas exactas para libros, artículos y páginas web.", Icons.Default.FormatQuote, IndigoPrimary, "Genera las referencias bibliográficas en formato APA 7ma edición y MLA para: ", "CITATION"),
            StudentToolItem("fallacy_checker", "Detector de Falacias", "Redacción", "Analiza argumentos e identifica falacias lógicas, sesgos y contradicciones.", Icons.Default.FactCheck, RoseHighlight, "Analiza este texto e identifica falacias lógicas o inconsistencias: ", "ESSAY_CHECK"),
            StudentToolItem("essay_intro", "Redactor de Introducciones", "Redacción", "Crea introducciones con gancho, tesis clara y delimitación del tema.", Icons.Default.EditNote, CyanDark, "Redacta 3 opciones de introducción académica para un ensayo sobre: ", "ESSAY_CHECK"),
            StudentToolItem("conclusion_maker", "Síntesis & Conclusiones", "Redacción", "Redacta conclusiones contundentes que responden a la pregunta de investigación.", Icons.Default.DoneAll, EmeraldSuccess, "Redacta una conclusión académica sólida y reflexiva para: ", "ESSAY_CHECK"),
            StudentToolItem("academic_polisher", "Pulidor de Tono Académico", "Redacción", "Eleva el vocabulario de tus textos haciéndolos formales y precisos.", Icons.Default.AutoFixHigh, AmberWarning, "Reescribe este párrafo con un tono académico formal y riguroso: ", "ESSAY_CHECK"),

            // 16-20: Productividad & Enfoque
            StudentToolItem("pomodoro_binaural", "Pomodoro & Ondas Alfa", "Productividad", "Temporizador de 25 minutos con sintetizador de ondas alfa (10Hz) y Lofi.", Icons.Default.Timer, RoseHighlight, "", "POMODORO"),
            StudentToolItem("gpa_calc", "Calculador de Promedios (GPA)", "Productividad", "Calcula tu promedio ponderado acumulado y nota requerida en el examen final.", Icons.Default.Score, IndigoPrimary, "Calcula la nota mínima requerida dadas las siguientes calificaciones y ponderaciones: ", "SOLVER"),
            StudentToolItem("study_scheduler", "Planificador de Exámenes", "Productividad", "Diseña un calendario de repaso espaciado optimizado para tus fechas límite.", Icons.Default.CalendarMonth, CyanAccent, "Crea un cronograma de estudio de 7 días con técnica Pomodoro para preparar el examen de: ", "SOLVER"),
            StudentToolItem("topic_breakdown", "Descompositor de Temas", "Productividad", "Divide materias gigantescas en micro-lecciones digeribles de 15 minutos.", Icons.Default.CallSplit, EmeraldSuccess, "Desglosa este tema extenso en 5 micro-temas de 15 minutos: ", "SOLVER"),
            StudentToolItem("weakspot_detector", "Detector de Puntos Débiles", "Productividad", "Descubre qué áreas necesitas reforzar antes de tu prueba oficial.", Icons.Default.GpsFixed, AmberWarning, "Evalúa mis conocimientos y dime qué conceptos me falta reforzar sobre: ", "SOCRATIC"),

            // 21-25: Colaboración & Retos WhatsApp
            StudentToolItem("whatsapp_challenge", "Retos para Grupo de Estudio", "Colaboración", "Crea retos, trivias y resúmenes con emojis formateados para WhatsApp.", Icons.Default.Share, WhatsAppGreen, "Crea un reto de estudio divertido con 3 preguntas para WhatsApp sobre: ", "DEBATE"),
            StudentToolItem("debate_arena", "Simulador de Debates", "Colaboración", "Analiza posturas antagónicas y prepara contraargumentos sólidos.", Icons.Default.Gavel, RoseHighlight, "Presenta los 3 argumentos a favor y los 3 en contra más contundentes sobre: ", "DEBATE"),
            StudentToolItem("lang_tutor", "Tutor de Idiomas & Fonética", "Idiomas", "Aprende inglés, francés o alemán con ejercicios gramaticales y pronunciación.", Icons.Default.Translate, CyanAccent, "Explica la regla gramatical, ejemplos y errores comunes en inglés de: ", "SOLVER"),
            StudentToolItem("analogy_gen", "Generador de Analogías", "Técnicas de Estudio", "Conecta abstracciones teóricas con situaciones reales y tangibles.", Icons.Default.Compare, EmeraldSuccess, "Crea 3 analogías intuitivas del mundo real para entender: ", "FEYNMAN"),
            StudentToolItem("realworld_examples", "Casos Reales de Aplicación", "Ciencias", "¿Para qué sirve esto en la vida real? Casos de la industria y la ciencia moderna.", Icons.Default.LightbulbCircle, AmberWarning, "¿Cuáles son las aplicaciones reales en la ingeniería e industria de: ", "SOLVER"),

            // 26-30: Herramientas Universitarias de Alto Impacto
            StudentToolItem("oral_exam_sim", "Simulador de Examen Oral", "Técnicas de Estudio", "Simula preguntas difíciles de un profesor universitario con réplicas.", Icons.Default.RecordVoiceOver, IndigoPrimary, "Actúa como un profesor universitario exigente y hazme 3 preguntas orales sobre: ", "SOCRATIC"),
            StudentToolItem("anki_cards_gen", "Exportador de Tarjetas Anki", "Técnicas de Estudio", "Genera pares Pregunta-Respuesta listos para copiar a Anki o Quizlet.", Icons.Default.FlipToBack, RoseHighlight, "Genera 5 tarjetas de memorización Anki (Front/Back) sobre: ", "FEYNMAN"),
            StudentToolItem("science_fair", "Asistente de Proyectos", "Ciencias", "Guía para plantear hipótesis, variables controladas y metodología científica.", Icons.Default.Assessment, CyanDark, "Estructura una metodología científica y planteamiento de hipótesis para un proyecto de: ", "SOLVER"),
            StudentToolItem("logic_truth_tables", "Tablas de Verdad & Lógica", "Matemáticas", "Resuelve proposiciones lógicas, tautologías y silogismos.", Icons.Default.DataObject, EmeraldSuccess, "Construye la tabla de verdad y determina si es tautología para la proposición: ", "SOLVER"),
            StudentToolItem("paper_summarizer", "Sintetizador de Papers", "Redacción", "Resume artículos científicos extrayendo abstract, metodología y conclusiones.", Icons.Default.Description, AmberWarning, "Sintetiza el siguiente artículo académico en Abstract, Metodología, Hallazgos y Limitaciones: ", "ESSAY_CHECK")
        )
    }

    val toolCategories = listOf("Todas", "Matemáticas", "Física", "Biología", "Técnicas de Estudio", "Redacción", "Productividad", "Colaboración", "Idiomas")

    val filteredTools = allTools.filter {
        selectedToolCategory == "Todas" || it.category.equals(selectedToolCategory, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("30+ Superpoderes de Estudio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    // Pomodoro Quick Access Button
                    IconButton(onClick = { showPomodoroSheet = true }) {
                        Badge(containerColor = if (isPomodoroRunning) EmeraldSuccess else IndigoPrimary) {
                            Icon(Icons.Default.Timer, contentDescription = "Pomodoro")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(toolCategories) { cat ->
                    FilterChip(
                        selected = selectedToolCategory == cat,
                        onClick = { selectedToolCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            // Grid of Tools
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTools, key = { it.id }) { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .clickable {
                                if (tool.id == "pomodoro_binaural") {
                                    showPomodoroSheet = true
                                } else {
                                    selectedToolToRun = tool
                                    userPromptInput = ""
                                    viewModel.clearToolResult()
                                }
                            }
                            .testTag("tool_card_${tool.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tool.iconTint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = null,
                                        tint = tool.iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = tool.category,
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column {
                                Text(
                                    text = tool.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tool.description,
                                    fontSize = 11.sp,
                                    color = Slate600,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Tool Execution Modal
        if (selectedToolToRun != null) {
            val tool = selectedToolToRun!!
            ModalBottomSheet(
                onDismissRequest = {
                    selectedToolToRun = null
                    viewModel.clearToolResult()
                },
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tool.iconTint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tool.icon, contentDescription = null, tint = tool.iconTint, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                            Text(tool.description, fontSize = 12.sp, color = Slate400)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = userPromptInput,
                        onValueChange = { userPromptInput = it },
                        placeholder = { Text("Ingresa el problema, ecuación, tema o texto...", color = Slate400, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800,
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            when (tool.typeCode) {
                                "SLIDES" -> {
                                    val query = userPromptInput.ifBlank { "Técnicas de Estudio y Memorización" }
                                    viewModel.generateSlides(query)
                                }
                                "CHART" -> {
                                    val query = userPromptInput.ifBlank { "Distribución de Horas y Rendimiento por Materia" }
                                    viewModel.generateStudyChart(query)
                                }
                                "PDF_DOC" -> {
                                    val query = userPromptInput.ifBlank { "Compendio de Física Cuántica y Mecánica" }
                                    viewModel.generateAcademicBookDocument(query, "Ciencias Exactas")
                                }
                                else -> {
                                    val fullPrompt = "${tool.promptTemplate} $userPromptInput"
                                    viewModel.executeStudentTool(fullPrompt, tool.typeCode)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isToolLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Procesando con Sofía...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ejecutar Herramienta")
                        }
                    }

                    if (generatedSlides != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        InteractiveSlidesViewer(
                            topic = generatedSlidesTopic.ifBlank { "Presentación Académica" },
                            slides = generatedSlides!!,
                            onSaveToNotes = { title, content ->
                                viewModel.saveNote(title = title, content = content, category = "Presentación")
                            },
                            onSpeak = { text ->
                                viewModel.speechService.speak(text)
                            }
                        )
                    } else if (generatedChartPoints != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        InteractiveStudyChart(
                            title = generatedChartTitle.ifBlank { "Progreso de Estudio" },
                            subtitle = "Métricas analíticas calculadas por Sofía Tutor",
                            dataPoints = generatedChartPoints!!,
                            onSaveToNotes = { title, content ->
                                viewModel.saveNote(title = title, content = content, category = "Estadísticas")
                            }
                        )
                    } else if (activeToolResult != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate800)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp)
                                    .padding(14.dp)
                            ) {
                                item {
                                    Text(
                                        text = activeToolResult ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(activeToolResult ?: ""))
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = CyanAccent)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.speechService.speak(activeToolResult ?: "")
                                    }
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = CyanAccent)
                                }
                            }

                            WhatsAppShareButton(
                                title = tool.title,
                                content = activeToolResult ?: "",
                                category = tool.category,
                                label = "Enviar a WhatsApp"
                            )
                        }
                    }
                }
            }
        }

        // Pomodoro Focus Timer & Synthesized Binaural Waves Sheet
        if (showPomodoroSheet) {
            val minutesLeft = pomodoroSeconds / 60
            val secondsLeft = pomodoroSeconds % 60
            val formattedTime = "%02d:%02d".format(minutesLeft, secondsLeft)

            ModalBottomSheet(
                onDismissRequest = { showPomodoroSheet = false },
                containerColor = Slate900,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Temporizador Pomodoro & Sonidos de Enfoque", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sintetizador acústico 100% offline para maximizar tu concentración.", fontSize = 12.sp, color = Slate400)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer Circular Card
                    Card(
                        modifier = Modifier.size(160.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.sweepGradient(listOf(IndigoPrimary, CyanAccent, IndigoPrimary))
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(pomodoroMode, fontSize = 12.sp, color = CyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (isPomodoroRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPomodoroRunning) AmberWarning else IndigoPrimary)
                        ) {
                            Icon(if (isPomodoroRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPomodoroRunning) "Pausar" else "Iniciar")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetPomodoro(25, "Estudio (25m)") }
                        ) {
                            Text("Reiniciar")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Binaural Sound Selector
                    Text("🎵 Paisaje Sonoro de Fondo:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate300)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FocusSoundType.values()) { sound ->
                            FilterChip(
                                selected = currentFocusSound == sound,
                                onClick = { viewModel.setFocusSoundtrack(sound) },
                                label = { Text(sound.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}
