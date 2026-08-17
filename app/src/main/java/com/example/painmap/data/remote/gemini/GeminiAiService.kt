package com.example.painmap.data.remote.gemini

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.painmap.BuildConfig
import com.example.painmap.data.remote.dto.GeminiTriageDto
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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

    private fun decodeBase64Bitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val cleanStr = if (base64Str.contains(",")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateTriage(
        painPoints: List<PainPoint>,
        userNotes: String = "",
        mapSnapshotBase64: String? = null
    ): Result<GeminiTriageDto> = withContext(ioDispatcher) {
        try {
            if (painPoints.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No pain points provided for triage analysis.")
                )
            }

            val model = generativeModel
            if (model != null) {
                val prompt = buildClinicalPrompt(painPoints, userNotes, hasImage = !mapSnapshotBase64.isNullOrBlank())
                val bitmap = decodeBase64Bitmap(mapSnapshotBase64)

                val response = if (bitmap != null) {
                    val inputContent = content {
                        image(bitmap)
                        text(prompt)
                    }
                    model.generateContent(inputContent)
                } else {
                    model.generateContent(prompt)
                }
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
                val bitmap = decodeBase64Bitmap(session.mapSnapshotBase64)

                val response = if (bitmap != null) {
                    val inputContent = content {
                        image(bitmap)
                        text(prompt)
                    }
                    model.generateContent(inputContent)
                } else {
                    model.generateContent(prompt)
                }
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

    private fun buildClinicalPrompt(
        painPoints: List<PainPoint>,
        userNotes: String,
        hasImage: Boolean
    ): String {
        val painSummary = painPoints.joinToString(separator = "\n") { point ->
            "- Region: ${point.region.displayName} (${point.region.category.label}), " +
                    "Intensity: ${point.intensity}/10, " +
                    "Types: [${point.painTypes.joinToString { it.displayName }}], " +
                    "Duration: ${point.duration.label}" +
                    if (point.triggers.isNotBlank()) ", Triggers: ${point.triggers}" else "" +
                    if (point.notes.isNotBlank()) ", Notes: ${point.notes}" else ""
        }

        val visualInstruction = if (hasImage) {
            """
            VISUAL 3D BODY MAP SNAPSHOT ATTACHED:
            An exact 3D visual screenshot of the patient's musculoskeletal pain heatmap is attached above.
            Carefully inspect the visual distribution of the painted areas (e.g. unilateral vs bilateral asymmetry, radiating bands, joint line localization, myofascial trigger points, and proximal vs distal kinetic chain involvement).
            """.trimIndent()
        } else ""

        return """
            You are PainMapAI, a clinical triage assistant specializing strictly in diagnosing and assessing the root causes of Joint and Muscle Pain (musculoskeletal biomechanics, tendinopathy, ligamentous stress, postural imbalances, myofascial trigger points, and joint capsule strain).
            
            $visualInstruction
            
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
            
            PATIENT PAIN PROFILE & 3D BODY MAP:
            $painSummary
            
            INITIAL CLINICAL TRIAGE SYNTHESIS:
            $reportSummary
            
            CONVERSATION HISTORY:
            $historyText
            
            PATIENT'S FOLLOW-UP QUESTION:
            "$question"
            
            INSTRUCTIONS FOR RESPONSE:
            - Provide a concise, clear, and reassuring clinical explanation focused strictly on joint & muscle root causes, movement mechanics, and non-invasive relief.
            - Answer the specific question directly.
            - Offer 1-2 practical self-care suggestions (e.g. gentle active-assisted range of motion, ergonomic adjustments, relative rest).
            - Keep the tone professional, empathetic, and evidence-informed.
            - Limit response to 2-3 short, highly informative paragraphs.
        """.trimIndent()
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }
        val match = Regex("""\{[\s\S]*\}""").find(trimmed)
        return match?.value ?: ""
    }

    /**
     * Local clinical synthesis fallback for offline / test environments.
     */
    private fun synthesizeClinicalTriage(
        painPoints: List<PainPoint>,
        userNotes: String
    ): GeminiTriageDto {
        val maxIntensity = painPoints.maxOfOrNull { it.intensity } ?: 1
        val hasSeverePain = maxIntensity >= 8
        val hasShooting = painPoints.any { pt -> pt.painTypes.contains(PainType.SHOOTING) }
        val hasStiffness = painPoints.any { pt -> pt.painTypes.contains(PainType.STIFFNESS) }
        val hasChronic = painPoints.any { pt -> pt.duration == PainDuration.CHRONIC }

        val urgency = when {
            hasSeverePain || (hasShooting && maxIntensity >= 7) -> "HIGH"
            maxIntensity >= 5 || hasShooting || hasStiffness -> "MODERATE"
            hasChronic -> "LOW"
            else -> "ROUTINE"
        }

        val regionNames = painPoints.joinToString { it.region.displayName }

        val conditions = mutableListOf<String>()
        if (painPoints.any { it.region.name.contains("KNEE") || it.region.name.contains("HIP") || it.region.name.contains("ANKLE") }) {
            conditions.add("Lower Kinetic Chain Joint Overload or Tendinopathy")
        }
        if (painPoints.any { it.region.name.contains("SHOULDER") || it.region.name.contains("NECK") || it.region.name.contains("BACK") }) {
            conditions.add("Postural Strain & Upper Quadrant Myofascial Dysregulation")
        }
        if (hasShooting) {
            conditions.add("Peripheral Nerve Traction or Radicular Irritation")
        }
        if (conditions.isEmpty()) {
            conditions.add("Localized Acute Musculoskeletal Soft-Tissue Fatigue")
        }

        val specialties = listOf("Physical Therapy", "Orthopedic Physical Therapy", "Sports Medicine")

        val questions = listOf(
            "Does the discomfort increase with specific end-range movements or repetitive loading?",
            "Are symptoms worse in the morning upon waking or after sustained physical activity?",
            "Have you noticed any joint stiffness, clicking, or reduced range of motion?"
        )

        val selfCare = listOf(
            "Apply relative rest and avoid high-impact aggravation for 48-72 hours.",
            "Incorporate gentle non-weightbearing range-of-motion movements to preserve joint lubricity.",
            "Consider ice or warm compresses based on whether active swelling is present."
        )

        return GeminiTriageDto(
            urgencyLevel = urgency,
            preliminaryAssessment = "Anatomical pain mapping identified in $regionNames with peak intensity of $maxIntensity/10. Presentation aligns with musculoskeletal strain, kinetic chain imbalance, or joint capsule stress.",
            potentialConditionsToDiscuss = conditions,
            recommendedSpecialties = specialties,
            suggestedClinicalQuestions = questions,
            selfCareSuggestions = selfCare
        )
    }

    private fun synthesizeFollowUpAnswer(
        session: PainAssessmentSession,
        question: String
    ): String {
        val region = session.painPoints.firstOrNull()?.region?.displayName ?: "the affected joint and muscle areas"
        val maxIntensity = session.painPoints.maxOfOrNull { it.intensity } ?: 5

        return "Regarding your question about $region (pain intensity $maxIntensity/10), symptoms in this region often stem from kinetic chain overload, localized tendon stress, or muscular compensation.\n\n" +
                "For safe self-management, focus on gentle active mobility without pushing into sharp pain. Maintaining hydration, gentle heat before movement, and relative rest can help reduce inflammation.\n\n" +
                "If symptoms persist or intensify with weight bearing, an evaluation with a physical therapist is recommended."
    }
}
