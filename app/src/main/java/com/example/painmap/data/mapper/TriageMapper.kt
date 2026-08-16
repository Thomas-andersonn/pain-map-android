package com.example.painmap.data.mapper

import com.example.painmap.data.remote.dto.GeminiTriageDto
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.UrgencyLevel
import java.util.UUID

object TriageMapper {

    fun mapToDomain(
        dto: GeminiTriageDto,
        analyzedPainPoints: List<PainPoint>
    ): ClinicalTriageReport {
        val mappedUrgency = parseUrgencyLevel(dto.urgencyLevel)

        return ClinicalTriageReport(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            urgencyLevel = mappedUrgency,
            preliminaryAssessment = dto.preliminaryAssessment.ifBlank {
                "Anatomical pain mapping recorded across ${analyzedPainPoints.size} region(s)."
            },
            potentialConditionsToDiscuss = dto.potentialConditionsToDiscuss.ifEmpty {
                listOf("Musculoskeletal Strain", "Myofascial Discomfort")
            },
            recommendedSpecialties = dto.recommendedSpecialties.ifEmpty {
                listOf("Primary Care Physician", "Physical Therapy")
            },
            suggestedClinicalQuestions = dto.suggestedClinicalQuestions.ifEmpty {
                listOf(
                    "When did you first notice this sensation?",
                    "Does movement or rest change the intensity?"
                )
            },
            selfCareSuggestions = dto.selfCareSuggestions.ifEmpty {
                listOf(
                    "Apply gentle heat or cold pack to affected areas as tolerated",
                    "Maintain comfortable posture and avoid abrupt heavy lifting"
                )
            },
            analyzedPainPoints = analyzedPainPoints
        )
    }

    private fun parseUrgencyLevel(rawUrgency: String): UrgencyLevel {
        return when (rawUrgency.trim().uppercase()) {
            "HIGH", "CRITICAL", "URGENT", "EMERGENCY" -> UrgencyLevel.HIGH
            "MODERATE", "MEDIUM" -> UrgencyLevel.MODERATE
            "LOW", "MILD" -> UrgencyLevel.LOW
            "ROUTINE", "NORMAL" -> UrgencyLevel.ROUTINE
            else -> UrgencyLevel.MODERATE
        }
    }
}
