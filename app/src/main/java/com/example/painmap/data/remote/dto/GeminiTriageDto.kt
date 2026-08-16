package com.example.painmap.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiTriageDto(
    val urgencyLevel: String = "MODERATE",
    val preliminaryAssessment: String = "",
    val potentialConditionsToDiscuss: List<String> = emptyList(),
    val recommendedSpecialties: List<String> = emptyList(),
    val suggestedClinicalQuestions: List<String> = emptyList(),
    val selfCareSuggestions: List<String> = emptyList()
)
