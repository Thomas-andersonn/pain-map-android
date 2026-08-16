package com.example.painmap.ui.screens.painmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.repository.AiTriageRepository
import com.example.painmap.domain.repository.PainRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PainMapViewModel(
    private val painRecordRepository: PainRecordRepository,
    private val aiTriageRepository: AiTriageRepository
) : ViewModel() {

    private val _localUiState = MutableStateFlow(PainMapUiState())

    val uiState: StateFlow<PainMapUiState> = combine(
        _localUiState,
        painRecordRepository.getActivePainPoints(),
        aiTriageRepository.getLatestTriageReport()
    ) { localState, activePoints, latestReport ->
        localState.copy(
            activePainPoints = activePoints,
            latestTriageReport = latestReport
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PainMapUiState()
    )

    fun onAction(action: PainMapUiAction) {
        when (action) {
            is PainMapUiAction.SelectRegion -> handleSelectRegion(action.region)
            is PainMapUiAction.OpenLoggingSheet -> handleOpenLoggingSheet(action.initialPoint, action.targetRegion)
            is PainMapUiAction.DismissLoggingSheet -> handleDismissLoggingSheet()
            is PainMapUiAction.SavePainPoint -> handleSavePainPoint(action.painPoint)
            is PainMapUiAction.DeletePainPoint -> handleDeletePainPoint(action.id)
            is PainMapUiAction.ClearAllPoints -> handleClearAllPoints()
            is PainMapUiAction.RequestAiTriage -> handleRequestAiTriage(action.userNotes, action.onSuccess)
            is PainMapUiAction.DismissError -> handleDismissError()
        }
    }

    private fun handleSelectRegion(region: AnatomicalRegion) {
        val existingPoint = uiState.value.activePainPoints.find { it.region == region }
        _localUiState.update {
            it.copy(
                selectedRegion = region,
                currentEditingPoint = existingPoint ?: PainPoint(region = region),
                isLoggingSheetOpen = true
            )
        }
    }

    private fun handleOpenLoggingSheet(initialPoint: PainPoint?, targetRegion: AnatomicalRegion?) {
        val pointToEdit = initialPoint ?: (targetRegion?.let { PainPoint(region = it) } ?: PainPoint(region = AnatomicalRegion.LOWER_BACK_LUMBAR))
        _localUiState.update {
            it.copy(
                selectedRegion = pointToEdit.region,
                currentEditingPoint = pointToEdit,
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
            val result = painRecordRepository.savePainPoint(painPoint)
            if (result.isSuccess) {
                _localUiState.update {
                    it.copy(
                        isLoggingSheetOpen = false,
                        currentEditingPoint = null
                    )
                }
            } else {
                _localUiState.update {
                    it.copy(errorMessage = "Failed to save pain point: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    private fun handleDeletePainPoint(id: String) {
        viewModelScope.launch {
            painRecordRepository.removePainPoint(id)
            _localUiState.update {
                it.copy(
                    isLoggingSheetOpen = false,
                    currentEditingPoint = null
                )
            }
        }
    }

    private fun handleClearAllPoints() {
        viewModelScope.launch {
            painRecordRepository.clearActivePainPoints()
        }
    }

    private fun handleRequestAiTriage(userNotes: String, onSuccess: () -> Unit) {
        val currentPoints = uiState.value.activePainPoints
        if (currentPoints.isEmpty()) {
            _localUiState.update {
                it.copy(errorMessage = "Please place at least one joint or muscle pain point before requesting AI analysis.")
            }
            return
        }

        viewModelScope.launch {
            _localUiState.update { it.copy(isTriageLoading = true, errorMessage = null) }
            val result = aiTriageRepository.analyzePainPoints(currentPoints, userNotes)
            if (result.isSuccess) {
                _localUiState.update { it.copy(isTriageLoading = false) }
                onSuccess()
            } else {
                _localUiState.update {
                    it.copy(
                        isTriageLoading = false,
                        errorMessage = "AI Triage analysis failed: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    private fun handleDismissError() {
        _localUiState.update { it.copy(errorMessage = null) }
    }
}
