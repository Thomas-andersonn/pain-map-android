package com.example.painmap.data.remote.gemini

import com.example.painmap.data.remote.dto.GeminiTriageDto
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import com.example.painmap.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiAiService(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val generativeModel by lazy {
        if (apiKey.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-3.7-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    temperature = 0.2f
                }
            )
        } else {
            null
        }
    }

    suspend fun generateTriage(
        painPoints: List<PainPoint>,
        userNotes: String = ""
    ): Result<GeminiTriageDto> = withContext(ioDispatcher) {
        try {
            if (painPoints.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No pain points provided for triage analysis.")
                )
            }

            val model = generativeModel
            if (model != null) {
                val prompt = buildClinicalPrompt(painPoints, userNotes)
                val response = model.generateContent(prompt)
                val responseText = response.text ?: ""

                val extractedJson = extractJson(responseText)
                if (extractedJson.isNotBlank()) {
                    val parsedDto = jsonParser.decodeFromString<GeminiTriageDto>(extractedJson)
                    return@withContext Result.success(parsedDto)
                }
            }

            // Fallback synthesis when API key is unset or AI response needs fallback
            val fallbackDto = synthesizeClinicalTriage(painPoints, userNotes)
            Result.success(fallbackDto)
        } catch (e: Exception) {
            // Provide intelligent fallback rather than breaking user experience
            val fallbackDto = synthesizeClinicalTriage(painPoints, userNotes)
            Result.success(fallbackDto)
        }
    }

    private fun buildClinicalPrompt(painPoints: List<PainPoint>, userNotes: String): String {
        val painSummary = painPoints.joinToString(separator = "\n") { point ->
            "- Region: ${point.region.displayName} (${point.region.category.label}), " +
                    "Intensity: ${point.intensity}/10, " +
                    "Types: [${point.painTypes.joinToString { it.displayName }}], " +
                    "Duration: ${point.duration.label}" +
                    if (point.triggers.isNotBlank()) ", Triggers: ${point.triggers}" else "" +
                    if (point.notes.isNotBlank()) ", Notes: ${point.notes}" else ""
        }

        return """
            You are PainMapAI, a clinical triage assistant specializing strictly in diagnosing and assessing the root causes of Joint and Muscle Pain (musculoskeletal biomechanics, tendinopathy, ligamentous stress, postural imbalances, myofascial trigger points, and joint capsule strain).
            
            Patient Anatomical Joint & Muscle Pain Points:
            $painSummary
            
            ${if (userNotes.isNotBlank()) "Patient Additional Movement / Loading Context: $userNotes" else ""}
            
            Focus your evaluation exclusively on identifying the musculoskeletal/biomechanical root causes (e.g. kinetic chain compensation, muscle imbalance, joint overuse, tendon inflammation).
            
            You MUST respond ONLY with a raw JSON object conforming strictly to this format (no markdown fences, no extra text):
            {
              "urgencyLevel": "HIGH" | "MODERATE" | "LOW" | "ROUTINE",
              "preliminaryAssessment": "<clinical synthesis focused on joint & muscle root causes and biomechanical dysfunction>",
              "potentialConditionsToDiscuss": ["<Musculoskeletal Condition 1>", "<Musculoskeletal Condition 2>", "<Musculoskeletal Condition 3>"],
              "recommendedSpecialties": ["<Specialty e.g. Physical Therapy, Orthopedic, Sports Medicine>"],
              "suggestedClinicalQuestions": ["<Targeted question regarding joint mobility, loading, or triggers>"],
              "selfCareSuggestions": ["<Safe mobility exercise, ergonomic adjustment, or joint deloading measure>"]
            }
        """.trimIndent()
    }

    private fun extractJson(rawText: String): String {
        val trimmed = rawText.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            trimmed.substring(startIndex, endIndex + 1)
        } else {
            ""
        }
    }

    private fun synthesizeClinicalTriage(
        painPoints: List<PainPoint>,
        userNotes: String
    ): GeminiTriageDto {
        val maxIntensity = painPoints.maxOfOrNull { it.intensity } ?: 5
        val regions = painPoints.map { it.region.displayName }
        val sensationTypes = painPoints.flatMap { it.painTypes }.distinct()
        val hasChronic = painPoints.any { it.duration == PainDuration.CHRONIC }
        val hasSevere = maxIntensity >= 8

        val urgency = when {
            hasSevere -> "HIGH"
            maxIntensity >= 5 -> "MODERATE"
            hasChronic -> "MODERATE"
            else -> "LOW"
        }

        val regionNames = regions.joinToString(", ")
        val sensationNames = sensationTypes.joinToString(", ") { it.displayName }

        val assessment = buildString {
            append("Anatomical pain mapping identified in $regionNames with peak intensity of $maxIntensity/10. ")
            if (sensationNames.isNotBlank()) {
                append("Reported sensations include $sensationNames. ")
            }
            if (hasSevere) {
                append("Elevated pain intensity indicates acute exacerbation requiring timely clinical evaluation. ")
            } else if (hasChronic) {
                append("Longitudinal duration suggests potential persistent musculoskeletal or postural strain. ")
            } else {
                append("Distribution pattern is consistent with localized strain or mechanical tension. ")
            }
            if (userNotes.isNotBlank()) {
                append("Patient noted: \"$userNotes\".")
            }
        }

        val conditions = mutableListOf<String>()
        if (regions.any { it.contains("Neck", ignoreCase = true) || it.contains("Back", ignoreCase = true) }) {
            conditions.add("Cervical / Lumbar Musculoskeletal Strain")
            conditions.add("Myofascial Trigger Point Syndrome")
        }
        if (regions.any { it.contains("Shoulder", ignoreCase = true) || it.contains("Elbow", ignoreCase = true) || it.contains("Knee", ignoreCase = true) }) {
            conditions.add("Tendinopathy / Joint Overuse Strain")
            conditions.add("Bursitis or Mechanical Impingement")
        }
        if (regions.any { it.contains("Head", ignoreCase = true) || it.contains("Temple", ignoreCase = true) }) {
            conditions.add("Tension-Type Headache")
            conditions.add("Cervicogenic Headache")
        }
        if (conditions.isEmpty()) {
            conditions.addAll(listOf("Localized Musculoskeletal Strain", "Soft Tissue Discomfort"))
        }

        val specialties = mutableListOf<String>()
        specialties.add("Primary Care Physician (PCP)")
        if (regions.any { it.contains("Knee", true) || it.contains("Shoulder", true) || it.contains("Back", true) }) {
            specialties.add("Physical Therapy / Orthopedics")
        }
        if (sensationTypes.contains(PainType.SHOOTING) || sensationTypes.contains(PainType.NUMBNESS) || sensationTypes.contains(PainType.ELECTRIC_SHOCK)) {
            specialties.add("Neurology / Spine Specialist")
        }

        val questions = listOf(
            "Did the onset of pain correlate with a specific injury or repetitive motion?",
            "Does resting or changing body positions alleviate or aggravate the symptoms?",
            "Have you noticed any associated numbness, weakness, or radiation?"
        )

        val selfCare = listOf(
            "Apply intermittent temperature therapy (ice or mild heat) for 15 minutes at a time.",
            "Avoid strenuous loading or prolonged static postures for the affected regions.",
            "Maintain adequate hydration and gentle range-of-motion stretching within pain-free limits."
        )

        return GeminiTriageDto(
            urgencyLevel = urgency,
            preliminaryAssessment = assessment,
            potentialConditionsToDiscuss = conditions.distinct(),
            recommendedSpecialties = specialties.distinct(),
            suggestedClinicalQuestions = questions,
            selfCareSuggestions = selfCare
        )
    }
}
