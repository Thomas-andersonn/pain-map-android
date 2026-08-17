package com.example.painmap.ui.screens.painmap

import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.components.model3d.PaintToolMode

/**
 * Immutable UI state for the 3D Pain Mapping & AI Triage workflow.
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
    val brushIntensity: Int = 5
)
