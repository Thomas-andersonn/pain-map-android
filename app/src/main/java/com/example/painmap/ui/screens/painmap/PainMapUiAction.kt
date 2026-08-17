package com.example.painmap.ui.screens.painmap

import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.components.model3d.PaintToolMode

/**
 * Sealed interface of all User-Initiated Actions for the PainMap screen (UDF).
 */
sealed interface PainMapUiAction {
    data class SelectRegion(val region: AnatomicalRegion) : PainMapUiAction
    data class OpenLoggingSheet(val initialPoint: PainPoint? = null) : PainMapUiAction
    data object DismissLoggingSheet : PainMapUiAction
    data class SavePainPoint(val painPoint: PainPoint) : PainMapUiAction
    data class DeletePainPoint(val painPointId: String) : PainMapUiAction
    data object ClearAllPoints : PainMapUiAction
    data class RequestAiTriage(
        val userNotes: String = "",
        val onSuccess: () -> Unit = {}
    ) : PainMapUiAction
    data object DismissError : PainMapUiAction

    // 3D Paint & Brush Actions
    data class SetToolMode(val mode: PaintToolMode) : PainMapUiAction
    data class SetBrushIntensity(val intensity: Int) : PainMapUiAction
    data class PaintRegion(val region: AnatomicalRegion, val intensity: Int) : PainMapUiAction
    data class EraseRegion(val region: AnatomicalRegion) : PainMapUiAction
}
