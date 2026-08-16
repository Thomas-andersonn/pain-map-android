package com.example.painmap.domain.repository

import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainPoint
import kotlinx.coroutines.flow.Flow

interface AiTriageRepository {
    /**
     * Dispatches anatomical pain points and optional user notes to Gemini AI to generate
     * a structured clinical triage report.
     */
    suspend fun analyzePainPoints(
        painPoints: List<PainPoint>,
        userNotes: String = ""
    ): Result<ClinicalTriageReport>

    /**
     * Emits the latest generated triage report if available.
     */
    fun getLatestTriageReport(): Flow<ClinicalTriageReport?>

    /**
     * Saves or caches a generated triage report.
     */
    suspend fun saveTriageReport(report: ClinicalTriageReport): Result<Unit>
}
