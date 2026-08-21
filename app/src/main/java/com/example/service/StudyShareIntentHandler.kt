package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.data.local.entity.NoteEntity

enum class WhatsAppShareDestination {
    DIRECT_WHATSAPP_OR_CHOOSER,
    WHATSAPP_ONLY,
    SYSTEM_CHOOSER
}

object StudyShareIntentHandler {

    private const val WHATSAPP_PKG = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PKG = "com.whatsapp.w4b"

    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(WHATSAPP_PKG, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                pm.getPackageInfo(WHATSAPP_BUSINESS_PKG, 0)
                true
            } catch (ex: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun getAvailableWhatsAppPackage(context: Context): String? {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(WHATSAPP_PKG, 0)
            WHATSAPP_PKG
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                pm.getPackageInfo(WHATSAPP_BUSINESS_PKG, 0)
                WHATSAPP_BUSINESS_PKG
            } catch (ex: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    /**
     * Dispatches an Intent to WhatsApp or presents a System Chooser
     */
    fun dispatchTextIntent(
        context: Context,
        formattedText: String,
        chooserTitle: String = "Compartir con Grupo de Estudio",
        forceWhatsApp: Boolean = false
    ) {
        val targetPackage = getAvailableWhatsAppPackage(context)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formattedText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (targetPackage != null) {
            intent.setPackage(targetPackage)
            try {
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // If direct launch fails, fallback
                intent.setPackage(null)
            }
        }

        if (forceWhatsApp) {
            // Try WhatsApp Web Intent URL as fallback
            try {
                val url = "https://api.whatsapp.com/send?text=" + Uri.encode(formattedText)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                return
            } catch (e: Exception) {
                // Fallback to chooser below
            }
        }

        // Generic Intent Chooser
        try {
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (ex: Exception) {
            Toast.makeText(context, "No se encontró una app para compartir.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares a full Study Note formatted for WhatsApp with bold headers, tags and structure
     */
    fun shareNote(context: Context, note: NoteEntity) {
        val msg = buildString {
            append("📝 *APUNTE ACADÉMICO - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📌 *Título:* ${note.title}\n")
            append("🏷️ *Materia:* ${note.category}\n")
            if (note.tags.isNotBlank()) {
                append("🔖 *Etiquetas:* ${note.tags}\n")
            }
            append("\n📖 *Contenido del Apunte:*\n")
            append(note.content)

            if (note.summary.isNotBlank()) {
                append("\n\n💡 *Resumen Inteligente (IA):*\n")
                append(note.summary)
            }

            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("✨ _Generado y organizado con TutorAI para Android_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Apunte: ${note.title}")
    }

    /**
     * Shares an AI-Generated Summary directly to WhatsApp
     */
    fun shareAiSummary(
        context: Context,
        topic: String,
        summaryContent: String,
        source: String = "Resumen de Estudio"
    ) {
        val msg = buildString {
            append("📑 *SÍNTESIS EJECUTIVA - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🎯 *Tema:* $topic\n")
            append("📚 *Fuente:* $source\n\n")
            append("✨ *Puntos Clave y Conclusiones:*\n")
            append(summaryContent)
            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🚀 _Comparte con tu equipo de estudio - TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Síntesis IA")
    }

    /**
     * Shares a PDF Text Highlight + AI Step-by-Step Explanation
     */
    fun sharePdfHighlightExplanation(
        context: Context,
        documentTitle: String,
        pageNumber: Int,
        selectedSnippet: String,
        aiExplanation: String
    ) {
        val msg = buildString {
            append("📖 *ANÁLISIS DE LECTURA - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📚 *Libro:* $documentTitle\n")
            append("📄 *Página:* $pageNumber\n\n")
            append("🔍 *Fragmento Resaltado:*\n")
            append("\"$selectedSnippet\"\n\n")
            append("💡 *Explicación Pedagógica del Tutor IA:*\n")
            append(aiExplanation)
            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🎓 _Estudio inteligente con TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Explicación de Lectura")
    }

    /**
     * Shares a Quiz Exam Challenge or Results to challenge classmates
     */
    fun shareQuizChallenge(
        context: Context,
        topic: String,
        score: Int,
        totalQuestions: Int
    ) {
        val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
        val medal = when {
            percentage >= 90 -> "🥇 ¡Puntaje Máximo!"
            percentage >= 70 -> "🥈 ¡Buen Desempeño!"
            else -> "🥉 ¡En Progreso!"
        }

        val msg = buildString {
            append("🏆 *RETO ACADÉMICO - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("$medal\n\n")
            append("Acabo de rendir el simulacro de examen sobre *$topic*.\n\n")
            append("📊 *Resultados:* $score / $totalQuestions aciertos (*$percentage%*)\n\n")
            append("⚔️ ¿Te atreves a superar mi puntuación en este tema? 🚀\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📲 _Descarga TutorAI y prepárate para los exámenes_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Retar a Grupo de Estudio en WhatsApp")
    }

    /**
     * Shares a 30+ Tool result (e.g. Solver, Feynman, Mindmap, Citations)
     */
    fun shareToolResult(
        context: Context,
        toolName: String,
        prompt: String,
        result: String
    ) {
        val msg = buildString {
            append("⚡ *HERRAMIENTA ACADÉMICA - $toolName*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📥 *Consulta / Ejercicio:* $prompt\n\n")
            append("📤 *Solución y Desarrollo:*\n")
            append(result)
            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("✨ _Generado con las 30+ Herramientas de TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir resultado de $toolName")
    }

    /**
     * Dispatches text directly to a specific phone number on WhatsApp
     */
    fun dispatchToWhatsAppNumber(
        context: Context,
        phoneNumber: String,
        formattedText: String
    ) {
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
        val url = if (cleanNumber.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanNumber&text=" + Uri.encode(formattedText)
        } else {
            "https://api.whatsapp.com/send?text=" + Uri.encode(formattedText)
        }

        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            dispatchTextIntent(context, formattedText, forceWhatsApp = true)
        }
    }

    /**
     * Shares an Academic Reminder (Exam, Homework, Deadline) to WhatsApp
     */
    fun shareReminder(
        context: Context,
        title: String,
        subject: String,
        type: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        notes: String,
        phoneNumber: String = ""
    ) {
        val msg = buildString {
            append("⏰ *RECORDATORIO ACADÉMICO - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📌 *Evento:* $title ($type)\n")
            append("📚 *Materia:* $subject\n")
            append("📅 *Fecha límite:* $dueDate a las $dueTime hs\n")
            append("🔥 *Prioridad:* $priority\n")
            if (notes.isNotBlank()) {
                append("📝 *Detalles:* $notes\n")
            }
            append("\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🤖 _Notificación generada por tu Asistente de Estudio Sofía_")
        }

        if (phoneNumber.isNotBlank()) {
            dispatchToWhatsAppNumber(context, phoneNumber, msg)
        } else {
            dispatchTextIntent(context, msg, chooserTitle = "Enviar Recordatorio por WhatsApp")
        }
    }

    /**
     * Shares Complete Daily Student Digest (Study hours, Wellness, Water, Steps, Tasks)
     */
    fun shareDailyStudentDigest(
        context: Context,
        studyMinutes: Int,
        sessionsCount: Int,
        waterGlasses: Int,
        stepsCount: Int,
        pendingTasksCount: Int,
        phoneNumber: String = ""
    ) {
        val msg = buildString {
            append("🌟 *REPORTE DIARIO DEL ESTUDIANTE - SOFÍA ASISTENTE*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("⏱️ *Estudio Activo:* ${studyMinutes / 60}h ${studyMinutes % 60}m ($sessionsCount sesiones)\n")
            append("💧 *Hidratación:* $waterGlasses / 8 vasos (${waterGlasses * 250} ml)\n")
            append("👟 *Actividad Física:* $stepsCount / 6,000 pasos\n")
            append("📋 *Tareas/Exámenes Pendientes:* $pendingTasksCount eventos\n")
            append("\n💡 *Consejo de Sofía:*\n")
            if (waterGlasses < 6) {
                append("⚠️ ¡Recuerda beber agua! Tu cerebro necesita hidratación para consolidar la memoria a largo plazo.\n")
            } else {
                append("✅ ¡Excelente ritmo de hidratación y estudio! Descansa bien esta noche para fijar lo aprendido.\n")
            }
            append("\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🚀 _¡Vamos con todo hacia el éxito académico!_")
        }

        if (phoneNumber.isNotBlank()) {
            dispatchToWhatsAppNumber(context, phoneNumber, msg)
        } else {
            dispatchTextIntent(context, msg, chooserTitle = "Enviar Reporte del Día a WhatsApp")
        }
    }

    /**
     * Shares Live Tutor Call key takeaways
     */
    fun shareCallTakeaways(
        context: Context,
        durationFormatted: String,
        takeaways: String
    ) {
        val msg = buildString {
            append("🗣️ *RESUMEN DE SESIÓN CON SOFÍA (TUTORA IA)*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("⏱️ *Duración de la llamada:* $durationFormatted\n\n")
            append("💡 *Principales Temas Tratados:*\n")
            append(takeaways)
            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("🎓 _TutorAI - Tutoría 1 a 1 en Tiempo Real_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Resumen de Tutoría")
    }

    /**
     * Shares AI-Generated Presentation Slides to WhatsApp contacts or groups
     */
    fun shareSlidesPresentation(
        context: Context,
        topic: String,
        slides: List<Pair<String, List<String>>> // Slide title -> bullet points
    ) {
        val msg = buildString {
            append("📑 *PRESENTACIÓN & DIAPOSITIVAS - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🎯 *Tema:* $topic\n")
            append("🔢 *Total Diapositivas:* ${slides.size}\n\n")

            slides.forEachIndexed { index, (title, bullets) ->
                append("🔹 *[Diapositiva ${index + 1}] $title*\n")
                bullets.forEach { bullet ->
                    append("  • $bullet\n")
                }
                append("\n")
            }

            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🚀 _Diapositivas diseñadas con Sofía TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Diapositivas en WhatsApp")
    }

    /**
     * Shares Academic Visual Chart / Analytics to WhatsApp
     */
    fun shareAcademicChart(
        context: Context,
        chartTitle: String,
        labels: List<String>,
        values: List<Float>,
        unit: String = "horas"
    ) {
        val msg = buildString {
            append("📊 *GRÁFICO ACADÉMICO & ANÁLISIS - TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📌 *Gráfico:* $chartTitle\n\n")

            labels.zip(values).forEach { (label, value) ->
                val barLength = (value / (values.maxOrNull()?.takeIf { it > 0 } ?: 1f) * 10).toInt().coerceIn(1, 10)
                val bar = "█".repeat(barLength)
                append("• *$label:* $value $unit  $bar\n")
            }

            append("\n━━━━━━━━━━━━━━━━━━━━\n")
            append("📈 _Estadísticas generadas por TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Gráfico en WhatsApp")
    }

    /**
     * Shares an Academic Document / PDF Book chapter summary to WhatsApp
     */
    fun sharePdfDocument(
        context: Context,
        docTitle: String,
        subject: String,
        author: String,
        summary: String,
        keyTerms: List<String> = emptyList()
    ) {
        val msg = buildString {
            append("📜 *DOCUMENTO ACADÉMICO - BIBLIOTECA TUTOR AI*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📖 *Título:* $docTitle\n")
            append("🏷️ *Materia:* $subject\n")
            append("✍️ *Autor:* $author\n\n")
            append("💡 *Sinopsis & Contenido Principal:*\n")
            append(summary)

            if (keyTerms.isNotEmpty()) {
                append("\n\n🔑 *Términos Clave:* ${keyTerms.joinToString(", ")}")
            }

            append("\n\n━━━━━━━━━━━━━━━━━━━━\n")
            append("📚 _Disponible en la Biblioteca PDF de TutorAI_")
        }
        dispatchTextIntent(context, msg, chooserTitle = "Compartir Documento en WhatsApp")
    }
}
