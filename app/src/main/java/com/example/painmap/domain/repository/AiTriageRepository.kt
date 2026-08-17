package com.example.painmap.domain.repository

import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainPoint
import kotlinx.coroutines.flow.Flow

interface AiTriageRepository {
    /**
     * Dispatches anatomical pain points and optional user notes to Gemini AI to generate
     * a structured clinical triage report.
     */
    suspend fun analyzePainPoints(
        painPoints: List<PainPoint>,
        userNotes: String = "",
        mapSnapshotBase64: String? = null
    ): Result<ClinicalTriageReport>

    /**
     * Emits the latest generated triage report if available.
     */
    fun getLatestTriageReport(): Flow<ClinicalTriageReport?>

    /**
     * Saves or caches a generated triage report.
     */
    suspend fun saveTriageReport(report: ClinicalTriageReport): Result<Unit>

    /**
     * Sends a follow-up question to Gemini AI within the context of a pain assessment session.
     */
    suspend fun askFollowUpQuestion(
        session: PainAssessmentSession,
        question: String
    ): Result<ChatMessage>
}
