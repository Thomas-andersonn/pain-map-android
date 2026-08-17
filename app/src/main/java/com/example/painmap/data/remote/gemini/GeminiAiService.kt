package com.example.painmap.data.remote.gemini

import com.example.painmap.BuildConfig
import com.example.painmap.data.remote.dto.GeminiTriageDto
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
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

    private val generativeChatModel by lazy {
        if (apiKey.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-3.7-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.4f
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
            val fallbackDto = synthesizeClinicalTriage(painPoints, userNotes)
            Result.success(fallbackDto)
        }
    }

    suspend fun askFollowUpQuestion(
        session: PainAssessmentSession,
        question: String
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val model = generativeChatModel
            if (model != null) {
                val prompt = buildFollowUpPrompt(session, question)
                val response = model.generateContent(prompt)
                val text = response.text?.trim() ?: ""
                if (text.isNotBlank()) {
                    return@withContext Result.success(text)
                }
            }

            val fallback = synthesizeFollowUpAnswer(session, question)
            Result.success(fallback)
        } catch (e: Exception) {
            val fallback = synthesizeFollowUpAnswer(session, question)
            Result.success(fallback)
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

    private fun buildFollowUpPrompt(session: PainAssessmentSession, question: String): String {
        val painSummary = session.painPoints.joinToString(separator = "\n") { point ->
            "- ${point.region.displayName} (${point.region.category.label}): VAS ${point.intensity}/10, Sensations: [${point.painTypes.joinToString { it.displayName }}], Duration: ${point.duration.label}"
        }

        val reportSummary = session.triageReport?.let { report ->
            """
            Synthesis: ${report.preliminaryAssessment}
            Potential Conditions: ${report.potentialConditionsToDiscuss.joinToString()}
            Recommended Specialties: ${report.recommendedSpecialties.joinToString()}
            """.trimIndent()
        } ?: "No previous triage report."

        val historyText = session.chatHistory.takeLast(6).joinToString(separator = "\n") { msg ->
            val speaker = if (msg.sender == MessageSender.USER) "Patient" else "PainMapAI"
            "$speaker: ${msg.message}"
        }

        return """
            You are PainMapAI, a clinical musculoskeletal triage assistant specializing in Joint & Muscle Pain, biomechanics, kinetic chain root causes, and safe physical therapy mobility.
            
            PATIENT PAIN PROFILE:
            $painSummary
            
            INITIAL CLINICAL TRIAGE SYNTHESIS:
            $reportSummary
            
            ${if (historyText.isNotBlank()) "CONVERSATION HISTORY:\n$historyText\n" else ""}
            
            PATIENT FOLLOW-UP QUESTION:
            "$question"
            
            Provide a clear, empathetic, and clinically precise answer directly addressing the patient's question based on their anatomical pain locations and joint biomechanics. Keep paragraphs concise, action-oriented, and reassuring. If suggesting exercises or stretches, explain how they relieve tension safely without overloading inflamed joints.
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
                append("Severe discomfort noted; evaluate joint articulation and tendon load with a specialist. ")
            } else if (hasChronic) {
                append("Chronic duration indicates persistent joint loading, muscle tightness, or postural compensation. ")
            } else {
                append("Presentation aligns with acute musculoskeletal strain or joint overuse. ")
            }
            if (userNotes.isNotBlank()) {
                append("Patient notes: $userNotes")
            }
        }

        val potentialConditions = mutableListOf<String>()
        if (regions.any { it.contains("Spine", true) || it.contains("Back", true) }) {
            potentialConditions.add("Lumbar / Thoracic Strain & Postural Muscle Imbalance")
        }
        if (regions.any { it.contains("Knee", true) || it.contains("Hip", true) || it.contains("Ankle", true) }) {
            potentialConditions.add("Lower Kinetic Chain Joint Overload or Tendinopathy")
        }
        if (regions.any { it.contains("Shoulder", true) || it.contains("Neck", true) }) {
            potentialConditions.add("Cervicothoracic Facet Strain or Rotator Cuff Tendon Stress")
        }
        if (potentialConditions.isEmpty()) {
            potentialConditions.add("Localized Myofascial Trigger Point Strain")
            potentialConditions.add("Joint Capsule Overuse")
        }

        return GeminiTriageDto(
            urgencyLevel = urgency,
            preliminaryAssessment = assessment,
            potentialConditionsToDiscuss = potentialConditions,
            recommendedSpecialties = listOf("Physical Therapy", "Orthopedic Physical Therapy", "Sports Medicine"),
            suggestedClinicalQuestions = listOf(
                "Does pain worsen with weight-bearing movements, prolonged sitting, or morning stiffness?",
                "Are there specific joint positions that provide immediate relief?",
                "Has there been previous joint injury or compensatory movement patterns?"
            ),
            selfCareSuggestions = listOf(
                "Gentle active range of motion and joint deloading exercises within pain-free limits.",
                "Apply ice for acute throbbing flares (15-20 min) or gentle heat for chronic muscle stiffness.",
                "Avoid aggressive end-range loading or painful provocative movements."
            )
        )
    }

    private fun synthesizeFollowUpAnswer(
        session: PainAssessmentSession,
        question: String
    ): String {
        val regionNames = session.painPoints.joinToString(", ") { it.region.displayName }
        return "Based on your mapped pain points ($regionNames), it is recommended to focus on gentle mobility and avoiding positions that provoke sharp discomfort. For persistent joint or muscle stiffness, consider gentle deloading stretches and consult a physical therapist for a personalized kinetic chain assessment."
    }
}
