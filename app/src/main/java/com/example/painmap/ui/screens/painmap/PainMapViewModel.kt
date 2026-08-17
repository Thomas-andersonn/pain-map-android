package com.example.painmap.ui.screens.painmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import com.example.painmap.domain.repository.AiTriageRepository
import com.example.painmap.domain.repository.PainRecordRepository
import com.example.painmap.ui.components.model3d.PaintToolMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Central ViewModel managing Unidirectional Data Flow for 3D pain-mapping, section painting, and AI triage.
 */
class PainMapViewModel(
    private val painRecordRepository: PainRecordRepository,
    private val aiTriageRepository: AiTriageRepository
) : ViewModel() {

    private val _localUiState = MutableStateFlow(PainMapUiState())

    val uiState: StateFlow<PainMapUiState> = combine(
        _localUiState,
        painRecordRepository.getActivePainPoints(),
        aiTriageRepository.getLatestTriageReport()
    ) { localState, activePoints, triageReport ->
        localState.copy(
            activePainPoints = activePoints,
            latestTriageReport = triageReport
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PainMapUiState()
    )

    fun onAction(action: PainMapUiAction) {
        when (action) {
            is PainMapUiAction.SelectRegion -> handleSelectRegion(action.region)
            is PainMapUiAction.OpenLoggingSheet -> handleOpenLoggingSheet(action.initialPoint)
            is PainMapUiAction.DismissLoggingSheet -> handleDismissLoggingSheet()
            is PainMapUiAction.SavePainPoint -> handleSavePainPoint(action.painPoint)
            is PainMapUiAction.DeletePainPoint -> handleDeletePainPoint(action.painPointId)
            is PainMapUiAction.ClearAllPoints -> handleClearAllPoints()
            is PainMapUiAction.RequestAiTriage -> handleRequestAiTriage(action.userNotes, action.onSuccess)
            is PainMapUiAction.DismissError -> handleDismissError()
            is PainMapUiAction.SetToolMode -> handleSetToolMode(action.mode)
            is PainMapUiAction.SetBrushIntensity -> handleSetBrushIntensity(action.intensity)
            is PainMapUiAction.PaintRegion -> handlePaintRegion(action.region, action.intensity)
            is PainMapUiAction.EraseRegion -> handleEraseRegion(action.region)
        }
    }

    private fun handleSelectRegion(region: AnatomicalRegion) {
        val existingPoint = uiState.value.activePainPoints.find { it.region == region }
        val pointToEdit = existingPoint ?: PainPoint(region = region)
        _localUiState.update {
            it.copy(
                selectedRegion = region,
                currentEditingPoint = pointToEdit,
                isLoggingSheetOpen = true
            )
        }
    }

    private fun handleOpenLoggingSheet(initialPoint: PainPoint?) {
        _localUiState.update {
            it.copy(
                selectedRegion = initialPoint?.region,
                currentEditingPoint = initialPoint ?: PainPoint(region = AnatomicalRegion.NECK_CERVICAL),
                isLoggingSheetOpen = true
            )
        }
    }

    private fun handleDismissLoggingSheet() {
        _localUiState.update {
            it.copy(
                isLoggingSheetOpen = false,
                currentEditingPoint = null
            )
        }
    }

    private fun handleSavePainPoint(painPoint: PainPoint) {
        viewModelScope.launch {
            painRecordRepository.savePainPoint(painPoint)
            _localUiState.update {
                it.copy(
                    isLoggingSheetOpen = false,
                    currentEditingPoint = null,
                    selectedRegion = null
                )
            }
        }
    }

    private fun handleDeletePainPoint(painPointId: String) {
        viewModelScope.launch {
            painRecordRepository.removePainPoint(painPointId)
            _localUiState.update {
                it.copy(
                    isLoggingSheetOpen = false,
                    currentEditingPoint = null,
                    selectedRegion = null
                )
            }
        }
    }

    private fun handleClearAllPoints() {
        viewModelScope.launch {
            painRecordRepository.clearActivePainPoints()
            _localUiState.update {
                it.copy(
                    selectedRegion = null,
                    currentEditingPoint = null
                )
            }
        }
    }

    private fun handleSetToolMode(mode: PaintToolMode) {
        _localUiState.update { it.copy(toolMode = mode) }
    }

    private fun handleSetBrushIntensity(intensity: Int) {
        _localUiState.update { it.copy(brushIntensity = intensity.coerceIn(1, 10)) }
    }

    private fun handlePaintRegion(region: AnatomicalRegion, intensity: Int) {
        viewModelScope.launch {
            val existing = uiState.value.activePainPoints.find { it.region == region }
            val pointToSave = existing?.copy(intensity = intensity)
                ?: PainPoint(
                    region = region,
                    intensity = intensity,
                    painTypes = setOf(PainType.ACHING)
                )
            painRecordRepository.savePainPoint(pointToSave)
        }
    }

    private fun handleEraseRegion(region: AnatomicalRegion) {
        viewModelScope.launch {
            val existing = uiState.value.activePainPoints.find { it.region == region }
            if (existing != null) {
                painRecordRepository.removePainPoint(existing.id)
            }
        }
    }

    private fun handleRequestAiTriage(userNotes: String, onSuccess: () -> Unit) {
        val currentPoints = uiState.value.activePainPoints
        if (currentPoints.isEmpty()) {
            _localUiState.update {
                it.copy(errorMessage = "Please paint or select at least one joint/muscle pain area first.")
            }
            return
        }

        viewModelScope.launch {
            _localUiState.update { it.copy(isTriageLoading = true, errorMessage = null) }
            val result = aiTriageRepository.analyzePainPoints(currentPoints, userNotes)
            result.fold(
                onSuccess = {
                    _localUiState.update { it.copy(isTriageLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _localUiState.update {
                        it.copy(
                            isTriageLoading = false,
                            errorMessage = error.message ?: "Failed to generate AI triage report."
                        )
                    }
                }
            )
        }
    }

    private fun handleDismissError() {
        _localUiState.update { it.copy(errorMessage = null) }
    }
}
