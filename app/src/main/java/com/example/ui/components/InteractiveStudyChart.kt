package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.StudyShareIntentHandler
import com.example.ui.theme.*

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color = CyanAccent,
    val secondaryInfo: String = ""
)

enum class ChartType {
    BAR,
    LINE_TREND,
    DISTRIBUTION
}

@Composable
fun InteractiveStudyChart(
    title: String,
    subtitle: String,
    dataPoints: List<ChartDataPoint>,
    unit: String = "hrs",
    chartType: ChartType = ChartType.BAR,
    onSaveToNotes: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var animProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(dataPoints) {
        animProgress = 0f
        kotlinx.coroutines.delay(50)
        animProgress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    val maxValue = remember(dataPoints) {
        (dataPoints.maxOfOrNull { it.value } ?: 10f).coerceAtLeast(1f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📊 $title",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                // WhatsApp Share Button
                IconButton(
                    onClick = {
                        val labels = dataPoints.map { it.label }
                        val values = dataPoints.map { it.value }
                        StudyShareIntentHandler.shareAcademicChart(context, title, labels, values, unit)
                    },
                    modifier = Modifier.testTag("btn_share_chart_wsp")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Enviar a WhatsApp", tint = WhatsAppGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800.copy(alpha = 0.6f))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val barWidth = (w / (dataPoints.size * 1.6f)).coerceIn(12f, 48f)
                    val spacing = (w - (barWidth * dataPoints.size)) / (dataPoints.size + 1)

                    // Grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = Slate700.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    if (chartType == ChartType.LINE_TREND) {
                        val path = Path()
                        dataPoints.forEachIndexed { i, point ->
                            val x = spacing * (i + 1) + barWidth * i + (barWidth / 2)
                            val normalizedVal = (point.value / maxValue).coerceIn(0f, 1f)
                            val y = h - (normalizedVal * h * animatedProgress)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                            // Point circle
                            drawCircle(
                                color = point.color,
                                radius = 5f,
                                center = Offset(x, y)
                            )
                        }
                        drawPath(
                            path = path,
                            color = CyanAccent,
                            style = Stroke(width = 4f)
                        )
                    } else {
                        // Bar Chart
                        dataPoints.forEachIndexed { i, point ->
                            val x = spacing * (i + 1) + barWidth * i
                            val normalizedVal = (point.value / maxValue).coerceIn(0f, 1f)
                            val barHeight = normalizedVal * h * animatedProgress
                            val y = h - barHeight

                            val barColor = if (selectedPointIndex == i) AmberWarning else point.color

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Labels under chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEachIndexed { index, point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedPointIndex = index }
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = point.label,
                            color = if (selectedPointIndex == index) AmberWarning else Slate300,
                            fontSize = 10.sp,
                            fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${point.value.toInt()}$unit",
                            color = point.color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (onSaveToNotes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        val content = buildString {
                            append("📊 Estadísticas de Estudio: $title\n\n")
                            dataPoints.forEach {
                                append("• ${it.label}: ${it.value} $unit ${if (it.secondaryInfo.isNotBlank()) "(${it.secondaryInfo})" else ""}\n")
                            }
                        }
                        onSaveToNotes("Gráfico: $title", content)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Estadísticas en Apuntes", fontSize = 11.sp)
                }
            }
        }
    }
}
