package com.example.ui.screens.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.WhatsAppShareHelper
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsQuizScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Flashcards SRS, 1: Simulador de Exámenes

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Repaso Espaciado & Simulador de Examen", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("🗂️ Flashcards (SRS)", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("🎯 Simulador de Quiz", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (activeTab == 0) {
                FlashcardsTabContent(viewModel)
            } else {
                QuizSimulatorTabContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsTabContent(viewModel: TutorViewModel) {
    val allCards by viewModel.allFlashcards.collectAsStateWithLifecycle()
    val deckNames by viewModel.deckNames.collectAsStateWithLifecycle()
    var selectedDeck by remember { mutableStateOf("Todos") }
    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showAddCardSheet by remember { mutableStateOf(false) }

    val filteredCards = allCards.filter {
        selectedDeck == "Todos" || it.deckName == selectedDeck
    }

    val currentCard = filteredCards.getOrNull(currentCardIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0)))

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    FilterChip(
                        selected = selectedDeck == "Todos",
                        onClick = {
                            selectedDeck = "Todos"
                            currentCardIndex = 0
                            isFlipped = false
                        },
                        label = { Text("Todos (${allCards.size})", fontSize = 11.sp) }
                    )
                }
                items(deckNames) { deck ->
                    FilterChip(
                        selected = selectedDeck == deck,
                        onClick = {
                            selectedDeck = deck
                            currentCardIndex = 0
                            isFlipped = false
                        },
                        label = { Text(deck, fontSize = 11.sp) }
                    )
                }
            }

            IconButton(onClick = { showAddCardSheet = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Añadir Tarjeta", tint = IndigoPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay flashcards en este mazo. ¡Crea una!", color = Slate600)
            }
        } else if (currentCard != null) {
            // Flashcard Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tarjeta ${currentCardIndex + 1} de ${filteredCards.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Nivel Leitner: Caja ${currentCard.boxLevel}/5",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Flipping Flashcard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .testTag("flashcard_flip_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rotation <= 90f) IndigoContainer else Slate900
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT SIDE (Question)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Help, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentCard.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = OnIndigoContainer,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "👆 Toca para ver la respuesta",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                    } else {
                        // BACK SIDE (Answer + Explanation)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = currentCard.answer,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                            if (currentCard.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentCard.explanation,
                                    fontSize = 13.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            IconButton(
                                onClick = { viewModel.speechService.speak(currentCard.answer) }
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar respuesta", tint = CyanAccent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating Buttons (Repasar vs Dominado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hard / Review soon
                OutlinedButton(
                    onClick = {
                        viewModel.reviewCard(currentCard, knewIt = false)
                        isFlipped = false
                        if (currentCardIndex + 1 < filteredCards.size) currentCardIndex += 1 else currentCardIndex = 0
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseHighlight)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Repasar Pronto")
                }

                // Mastered / Correct
                Button(
                    onClick = {
                        viewModel.reviewCard(currentCard, knewIt = true)
                        isFlipped = false
                        if (currentCardIndex + 1 < filteredCards.size) currentCardIndex += 1 else currentCardIndex = 0
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("¡Me la sé!")
                }
            }
        }

        // Add Flashcard Bottom Sheet
        if (showAddCardSheet) {
            var deckInput by remember { mutableStateOf("Física") }
            var questionInput by remember { mutableStateOf("") }
            var answerInput by remember { mutableStateOf("") }
            var explanationInput by remember { mutableStateOf("") }

            ModalBottomSheet(onDismissRequest = { showAddCardSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Nueva Flashcard de Repaso", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deckInput,
                        onValueChange = { deckInput = it },
                        label = { Text("Materia / Mazo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        label = { Text("Pregunta frontal...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { answerInput = it },
                        label = { Text("Respuesta...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = explanationInput,
                        onValueChange = { explanationInput = it },
                        label = { Text("Explicación complementaria (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (questionInput.isNotBlank() && answerInput.isNotBlank()) {
                                viewModel.addFlashcard(deckInput, questionInput, answerInput, explanationInput)
                                showAddCardSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Crear Tarjeta")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizSimulatorTabContent(viewModel: TutorViewModel) {
    var topicInput by remember { mutableStateOf("Termodinámica y Leyes de la Física") }
    val generatedQuiz by viewModel.generatedQuiz.collectAsStateWithLifecycle()
    val currentQuizIndex by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val selectedAnswer by viewModel.selectedQuizAnswer.collectAsStateWithLifecycle()
    val isQuizCompleted by viewModel.isQuizCompleted.collectAsStateWithLifecycle()
    val isToolLoading by viewModel.isToolLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (generatedQuiz.isEmpty()) {
            // Setup Screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Generador de Simulacros con IA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Ingresa cualquier materia o tema y la IA creará un examen personalizado de opción múltiple para evaluarte.", fontSize = 13.sp, color = Slate600)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Tema del examen") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Temas sugeridos:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                "Física Cuántica",
                                "Biología Molecular y ADN",
                                "Estructuras de Datos y Big-O",
                                "Historia: Segunda Guerra Mundial",
                                "Filosofía y Lógica"
                            )
                        ) { sugg ->
                            SuggestionChip(
                                onClick = { topicInput = sugg },
                                label = { Text(sugg, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateAIQuiz(topicInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        if (isToolLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando Preguntas...")
                        } else {
                            Icon(Icons.Default.Quiz, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Comenzar Simulacro")
                        }
                    }
                }
            }
        } else if (!isQuizCompleted) {
            // Active Quiz Question View
            val question = generatedQuiz[currentQuizIndex]

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pregunta ${currentQuizIndex + 1} de ${generatedQuiz.size}",
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Text(
                            text = "Puntaje: $quizScore",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = question.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option Choices
                    question.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswer == index
                        val isCorrect = index == question.correctIndex
                        val isAnswered = selectedAnswer != null

                        val cardColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                            isCorrect -> EmeraldContainer
                            isSelected && !isCorrect -> RoseContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isAnswered) {
                                    viewModel.answerQuizQuestion(index)
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) IndigoPrimary else Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + index).toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (selectedAnswer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Explicación: ${question.explanation}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text(if (currentQuizIndex + 1 < generatedQuiz.size) "Siguiente Pregunta" else "Ver Resultados")
                        }
                    }
                }
            }
        } else {
            // Quiz Complete / Score Screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (quizScore >= generatedQuiz.size / 2) Icons.Default.EmojiEvents else Icons.Default.School,
                        contentDescription = null,
                        tint = if (quizScore >= generatedQuiz.size / 2) AmberWarning else IndigoPrimary,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("¡Simulacro Completado!", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    val pct = if (generatedQuiz.isNotEmpty()) (quizScore * 100) / generatedQuiz.size else 0
                    Text(
                        text = "Tu resultado: $quizScore de ${generatedQuiz.size} correctas ($pct%)",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // WhatsApp Challenge Share Button!
                    Button(
                        onClick = {
                            WhatsAppShareHelper.shareQuizChallenge(context, topicInput, quizScore, generatedQuiz.size)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retar a Amigos en WhatsApp")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.generateAIQuiz(topicInput) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Intentar Nuevo Quiz")
                    }
                }
            }
        }
    }
}
