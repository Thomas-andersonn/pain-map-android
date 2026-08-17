package com.example.painmap.data.repository

import com.example.painmap.data.remote.gemini.GeminiAiService
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import com.example.painmap.domain.model.UrgencyLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiTriageRepositoryTest {

    private lateinit var repository: AiTriageRepositoryImpl
    private lateinit var geminiService: GeminiAiService

    @Before
    fun setUp() {
        geminiService = GeminiAiService(apiKey = "")
        repository = AiTriageRepositoryImpl(geminiService = geminiService)
    }

    @Test
    fun analyzePainPoints_withValidPainPoints_returnsValidTriageReport() = runBlocking {
        val testPainPoints = listOf(
            PainPoint(
                region = AnatomicalRegion.LOWER_BACK_LUMBAR,
                intensity = 7,
                painTypes = setOf(PainType.ACHING, PainType.STIFFNESS),
                duration = PainDuration.SUBACUTE,
                triggers = "Prolonged sitting at desk"
            ),
            PainPoint(
                region = AnatomicalRegion.NECK_CERVICAL,
                intensity = 5,
                painTypes = setOf(PainType.THROBBING),
                duration = PainDuration.ACUTE
            )
        )

        val result = repository.analyzePainPoints(testPainPoints, userNotes = "Stiffness worse in morning")
        
        assertTrue("Analysis should succeed", result.isSuccess)
        val report = result.getOrNull()
        assertNotNull("Report should not be null", report)
        assertEquals(UrgencyLevel.MODERATE, report?.urgencyLevel)
        assertTrue(report?.potentialConditionsToDiscuss?.isNotEmpty() == true)
        assertTrue(report?.recommendedSpecialties?.isNotEmpty() == true)
        assertTrue(report?.suggestedClinicalQuestions?.isNotEmpty() == true)
        assertEquals(2, report?.analyzedPainPoints?.size)

        // Verify repository StateFlow cached the latest report
        val latest = repository.getLatestTriageReport().first()
        assertEquals(report?.id, latest?.id)
    }

    @Test
    fun analyzePainPoints_withHighIntensity_evaluatesToHighUrgency() = runBlocking {
        val severePainPoint = listOf(
            PainPoint(
                region = AnatomicalRegion.HEAD,
                intensity = 9,
                painTypes = setOf(PainType.STABBING, PainType.SHOOTING),
                duration = PainDuration.ACUTE
            )
        )

        val result = repository.analyzePainPoints(severePainPoint)
        assertTrue(result.isSuccess)
        val report = result.getOrNull()
        assertEquals(UrgencyLevel.HIGH, report?.urgencyLevel)
    }

    @Test
    fun analyzePainPoints_withEmptyPainPoints_returnsFailure() = runBlocking {
        val result = repository.analyzePainPoints(emptyList())
        assertTrue("Analysis should fail on empty input", result.isFailure)
    }

    @Test
    fun saveTriageReport_updatesStateFlow() = runBlocking {
        val testReport = com.example.painmap.domain.model.ClinicalTriageReport(
            preliminaryAssessment = "Direct saved assessment",
            urgencyLevel = UrgencyLevel.LOW
        )

        val saveResult = repository.saveTriageReport(testReport)
        assertTrue(saveResult.isSuccess)

        val latest = repository.getLatestTriageReport().first()
        assertEquals(testReport.id, latest?.id)
        assertEquals("Direct saved assessment", latest?.preliminaryAssessment)
    }

    @Test
    fun askFollowUpQuestion_returnsAiChatMessage() = runBlocking {
        val session = PainAssessmentSession(
            painPoints = listOf(PainPoint(region = AnatomicalRegion.KNEE_RIGHT, intensity = 6))
        )
        val result = repository.askFollowUpQuestion(session, "What stretches should I do?")

        assertTrue(result.isSuccess)
        val msg = result.getOrNull()
        assertNotNull(msg)
        assertEquals(MessageSender.AI, msg?.sender)
        assertTrue(msg?.message?.isNotBlank() == true)
    }
}
