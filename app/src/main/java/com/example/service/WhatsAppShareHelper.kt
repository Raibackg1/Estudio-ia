package com.example.service

import android.content.Context
import com.example.data.local.entity.NoteEntity

object WhatsAppShareHelper {

    fun isWhatsAppInstalled(context: Context): Boolean {
        return StudyShareIntentHandler.isWhatsAppInstalled(context)
    }

    fun shareToWhatsApp(context: Context, title: String, content: String, category: String = "Estudio") {
        StudyShareIntentHandler.shareAiSummary(context, title, content, source = category)
    }

    fun shareNote(context: Context, note: NoteEntity) {
        StudyShareIntentHandler.shareNote(context, note)
    }

    fun shareQuizChallenge(context: Context, topic: String, score: Int, total: Int) {
        StudyShareIntentHandler.shareQuizChallenge(context, topic, score, total)
    }

    fun sharePdfHighlight(
        context: Context,
        documentTitle: String,
        pageNumber: Int,
        selectedSnippet: String,
        aiExplanation: String
    ) {
        StudyShareIntentHandler.sharePdfHighlightExplanation(
            context,
            documentTitle,
            pageNumber,
            selectedSnippet,
            aiExplanation
        )
    }

    fun shareToolResult(context: Context, toolName: String, prompt: String, result: String) {
        StudyShareIntentHandler.shareToolResult(context, toolName, prompt, result)
    }

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
        StudyShareIntentHandler.shareReminder(
            context,
            title,
            subject,
            type,
            dueDate,
            dueTime,
            priority,
            notes,
            phoneNumber
        )
    }

    fun shareDailyDigest(
        context: Context,
        studyMinutes: Int,
        sessionsCount: Int,
        waterGlasses: Int,
        stepsCount: Int,
        pendingTasksCount: Int,
        phoneNumber: String = ""
    ) {
        StudyShareIntentHandler.shareDailyStudentDigest(
            context,
            studyMinutes,
            sessionsCount,
            waterGlasses,
            stepsCount,
            pendingTasksCount,
            phoneNumber
        )
    }

    fun shareDirectMessage(context: Context, phoneNumber: String, message: String) {
        StudyShareIntentHandler.dispatchToWhatsAppNumber(context, phoneNumber, message)
    }
}
