package com.example.painmap.ui.screens.painmap

import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.components.model3d.PaintToolMode

/**
 * Immutable UI state for the 3D Pain Mapping, Persistent Sessions & AI Triage workflow.
 */
data class PainMapUiState(
    val activePainPoints: List<PainPoint> = emptyList(),
    val selectedRegion: AnatomicalRegion? = null,
    val isLoggingSheetOpen: Boolean = false,
    val currentEditingPoint: PainPoint? = null,
    val isTriageLoading: Boolean = false,
    val latestTriageReport: ClinicalTriageReport? = null,
    val errorMessage: String? = null,
    val toolMode: PaintToolMode = PaintToolMode.ROTATE,
    val brushIntensity: Int = 5,

    // Persistent Sessions & Follow-Up Q&A
    val currentSessionId: String? = null,
    val sessionsList: List<PainAssessmentSession> = emptyList(),
    val chatHistory: List<ChatMessage> = emptyList(),
    val isAskingFollowUp: Boolean = false
)
