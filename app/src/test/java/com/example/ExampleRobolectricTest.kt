package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.StudyEventEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.OpenRouterPresets
import com.example.data.sample.SampleAcademicLibrary
import com.example.service.StudyShareIntentHandler
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.SlideItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("TutorAI", appName)
  }

  @Test
  fun `verify academic library documents available`() {
    val docs = SampleAcademicLibrary.documents
    assertTrue("Debe contener libros académicos precargados", docs.isNotEmpty())
    assertNotNull(docs.find { it.subject == "Física" })
    assertNotNull(docs.find { it.subject == "Biología" })
  }

  @Test
  fun `verify open router free models presets are configured`() {
    val models = OpenRouterPresets.FREE_MODELS
    assertTrue(models.isNotEmpty())
    assertTrue(models.any { it.id.contains("gemini-2.0-flash") })
    assertTrue(models.any { it.id.contains("deepseek") })
  }

  @Test
  fun `verify offline academic engine fallback`() {
    val response = GeminiClient.generateLocalAcademicResponse("Explica física cuántica y funciones de onda", "Tutor")
    assertTrue(response.isNotBlank())
    assertTrue(response.contains("Física") || response.contains("Explicación") || response.contains("Respuesta"))
  }

  @Test
  fun `verify calendar event creation and retrieval in room`() = runBlocking {
    val event = StudyEventEntity(
      title = "Examen Final de Física",
      subject = "Física",
      description = "Capítulos 1 al 5 sobre termodinámica y ondas",
      eventTimestamp = System.currentTimeMillis() + 86400000L,
      durationMinutes = 120,
      isExam = true,
      priority = "Alta",
      locationOrLink = "Aula Magna 301"
    )
    val id = db.studyEventDao().insertEvent(event)
    assertTrue(id > 0)

    val events = db.studyEventDao().getAllEvents().first()
    assertEquals(1, events.size)
    assertEquals("Examen Final de Física", events[0].title)
    assertTrue(events[0].isExam)

    val exams = db.studyEventDao().getUpcomingExams(System.currentTimeMillis()).first()
    assertEquals(1, exams.size)
    assertEquals("Física", exams[0].subject)

    db.studyEventDao().toggleEventCompleted(id, true)
    val updated = db.studyEventDao().getEventById(id)
    assertNotNull(updated)
    assertTrue(updated!!.isCompleted)
  }

  @Test
  fun `verify whatsapp intent building for slides and charts`() {
    val slides = listOf(
      SlideItem(1, "Introducción", listOf("Concepto 1", "Concepto 2"), "Exponer fundamentos"),
      SlideItem(2, "Desarrollo", listOf("Fórmulas", "Aplicación"), "Detallar casos")
    )
    val chartPoints = listOf(
      ChartDataPoint("Matemáticas", 85f, CyanAccent, "Dominio"),
      ChartDataPoint("Física", 90f, IndigoPrimary, "Óptimo")
    )

    // Verify calling intent handlers without crash
    StudyShareIntentHandler.shareSlidesPresentation(context, "Física y Ondas", slides)
    StudyShareIntentHandler.shareAcademicChart(context, "Rendimiento Académico", chartPoints)
    StudyShareIntentHandler.sharePdfDocument(context, "Guía de Estudio", "Física", 3, "Resumen")
  }
}
