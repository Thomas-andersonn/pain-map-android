package com.example.painmap.ui.screens.painmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.PainAssessmentSession
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

class PainMapViewModel(
    private val painRecordRepository: PainRecordRepository,
    private val aiTriageRepository: AiTriageRepository
) : ViewModel() {

    private val _localUiState = MutableStateFlow(PainMapUiState())

    val uiState: StateFlow<PainMapUiState> = combine(
        painRecordRepository.getActivePainPoints(),
        painRecordRepository.getAllSessions(),
        aiTriageRepository.getLatestTriageReport(),
        _localUiState
    ) { activePoints, sessions, latestReport, localState ->
        localState.copy(
            activePainPoints = activePoints,
            sessionsList = sessions,
            latestTriageReport = latestReport ?: localState.latestTriageReport
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
            is PainMapUiAction.PaintRegion -> handlePaintRegion(
                action.region,
                action.intensity,
                action.uvX,
                action.uvY,
                action.x,
                action.y,
                action.z
            )
            is PainMapUiAction.EraseRegion -> handleEraseRegion(action.region)
            is PainMapUiAction.SendFollowUpQuestion -> handleSendFollowUpQuestion(action.question)
            is PainMapUiAction.LoadSession -> handleLoadSession(action.sessionId, action.onLoaded)
            is PainMapUiAction.DeleteSession -> handleDeleteSession(action.sessionId)
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

    private fun handleOpenLoggingSheet(initialPoint: PainPoint?) {
        _localUiState.update {
            it.copy(
                isLoggingSheetOpen = true,
                currentEditingPoint = initialPoint,
                selectedRegion = initialPoint?.region
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

    private fun handlePaintRegion(
        region: AnatomicalRegion,
        intensity: Int,
        uvX: Float? = null,
        uvY: Float? = null,
        x: Float? = null,
        y: Float? = null,
        z: Float? = null
    ) {
        viewModelScope.launch {
            val existing = uiState.value.activePainPoints.find { it.region == region }
            val pointToSave = existing?.copy(
                intensity = intensity,
                uvX = uvX ?: existing.uvX,
                uvY = uvY ?: existing.uvY,
                x = x ?: existing.x,
                y = y ?: existing.y,
                z = z ?: existing.z
            ) ?: PainPoint(
                region = region,
                intensity = intensity,
                uvX = uvX,
                uvY = uvY,
                x = x ?: region.defaultX,
                y = y ?: region.defaultY,
                z = z ?: region.defaultZ,
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
                onSuccess = { report ->
                    val newSession = PainAssessmentSession(
                        painPoints = currentPoints,
                        triageReport = report,
                        chatHistory = emptyList()
                    )
                    painRecordRepository.saveSession(newSession)

                    _localUiState.update {
                        it.copy(
                            isTriageLoading = false,
                            currentSessionId = newSession.id,
                            chatHistory = emptyList(),
                            latestTriageReport = report
                        )
                    }
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

    private fun handleSendFollowUpQuestion(question: String) {
        if (question.isBlank()) return
        val currentReport = uiState.value.latestTriageReport ?: return
        val sessionId = uiState.value.currentSessionId

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            message = question.trim()
        )

        val activeSession = PainAssessmentSession(
            id = sessionId ?: "current_session",
            painPoints = uiState.value.activePainPoints,
            triageReport = currentReport,
            chatHistory = uiState.value.chatHistory + userMessage
        )

        _localUiState.update {
            it.copy(
                chatHistory = it.chatHistory + userMessage,
                isAskingFollowUp = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            if (sessionId != null) {
                painRecordRepository.appendChatMessage(sessionId, userMessage)
            }

            val result = aiTriageRepository.askFollowUpQuestion(activeSession, question)
            result.fold(
                onSuccess = { aiMessage ->
                    if (sessionId != null) {
                        painRecordRepository.appendChatMessage(sessionId, aiMessage)
                    }
                    _localUiState.update {
                        it.copy(
                            chatHistory = it.chatHistory + aiMessage,
                            isAskingFollowUp = false
                        )
                    }
                },
                onFailure = { err ->
                    _localUiState.update {
                        it.copy(
                            isAskingFollowUp = false,
                            errorMessage = err.message ?: "Failed to receive AI response."
                        )
                    }
                }
            )
        }
    }

    private fun handleLoadSession(sessionId: String, onLoaded: () -> Unit) {
        viewModelScope.launch {
            val sessionResult = painRecordRepository.getSessionById(sessionId)
            val session = sessionResult.getOrNull()
            if (session != null) {
                // Restore pain points to active view
                painRecordRepository.clearActivePainPoints()
                for (point in session.painPoints) {
                    painRecordRepository.savePainPoint(point)
                }

                session.triageReport?.let {
                    aiTriageRepository.saveTriageReport(it)
                }

                _localUiState.update {
                    it.copy(
                        currentSessionId = session.id,
                        chatHistory = session.chatHistory,
                        latestTriageReport = session.triageReport
                    )
                }
                onLoaded()
            }
        }
    }

    private fun handleDeleteSession(sessionId: String) {
        viewModelScope.launch {
            painRecordRepository.deleteSession(sessionId)
        }
    }

    private fun handleDismissError() {
        _localUiState.update { it.copy(errorMessage = null) }
    }
}
