package com.example.painmap.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PainAssessmentSession(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val painPoints: List<PainPoint> = emptyList(),
    val triageReport: ClinicalTriageReport? = null,
    val chatHistory: List<ChatMessage> = emptyList()
)
