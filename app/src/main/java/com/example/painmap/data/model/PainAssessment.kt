package com.example.painmap.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PainAssessment(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val painPoints: List<PainPoint> = emptyList(),
    val userDescription: String = "",
    val aiInsight: String = "",
    val suggestedFollowUps: List<String> = emptyList()
)
