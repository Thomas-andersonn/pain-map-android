package com.example.painmap.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ClinicalTriageReport(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val urgencyLevel: UrgencyLevel = UrgencyLevel.MODERATE,
    val preliminaryAssessment: String,
    val potentialConditionsToDiscuss: List<String> = emptyList(),
    val recommendedSpecialties: List<String> = emptyList(),
    val suggestedClinicalQuestions: List<String> = emptyList(),
    val selfCareSuggestions: List<String> = emptyList(),
    val analyzedPainPoints: List<PainPoint> = emptyList()
)

@Serializable
enum class UrgencyLevel(val title: String, val description: String) {
    HIGH("High / Prompt Evaluation", "Evaluation by a healthcare professional is strongly recommended."),
    MODERATE("Moderate / Non-Emergency", "Schedule a standard appointment with a primary care doctor or specialist."),
    LOW("Low / Mild Symptoms", "Mild discomfort; monitor symptoms and consult a doctor if pain persists."),
    ROUTINE("Routine / General Observation", "Symptoms appear manageable with self-care and ergonomic adjustments.")
}
