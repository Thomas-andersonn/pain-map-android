package com.example.painmap.ui.screens.painmap

import androidx.compose.runtime.Immutable
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainPoint

@Immutable
data class PainMapUiState(
    val activePainPoints: List<PainPoint> = emptyList(),
    val selectedRegion: AnatomicalRegion? = null,
    val isLoggingSheetOpen: Boolean = false,
    val currentEditingPoint: PainPoint? = null,
    val isTriageLoading: Boolean = false,
    val latestTriageReport: ClinicalTriageReport? = null,
    val errorMessage: String? = null
)
