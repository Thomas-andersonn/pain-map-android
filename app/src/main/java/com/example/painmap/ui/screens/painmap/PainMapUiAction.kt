package com.example.painmap.ui.screens.painmap

import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint

sealed interface PainMapUiAction {
    data class SelectRegion(val region: AnatomicalRegion) : PainMapUiAction
    data class OpenLoggingSheet(val initialPoint: PainPoint? = null, val targetRegion: AnatomicalRegion? = null) : PainMapUiAction
    data object DismissLoggingSheet : PainMapUiAction
    data class SavePainPoint(val painPoint: PainPoint) : PainMapUiAction
    data class DeletePainPoint(val id: String) : PainMapUiAction
    data object ClearAllPoints : PainMapUiAction
    data class RequestAiTriage(val userNotes: String = "", val onSuccess: () -> Unit = {}) : PainMapUiAction
    data object DismissError : PainMapUiAction
}
