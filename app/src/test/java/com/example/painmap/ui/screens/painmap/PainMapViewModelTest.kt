package com.example.painmap.ui.screens.painmap

import com.example.painmap.data.remote.gemini.GeminiAiService
import com.example.painmap.data.repository.AiTriageRepositoryImpl
import com.example.painmap.data.repository.PainRecordRepositoryImpl
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PainMapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var painRecordRepository: PainRecordRepositoryImpl
    private lateinit var aiTriageRepository: AiTriageRepositoryImpl
    private lateinit var viewModel: PainMapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        painRecordRepository = PainRecordRepositoryImpl(ioDispatcher = testDispatcher)
        aiTriageRepository = AiTriageRepositoryImpl(
            geminiService = GeminiAiService(apiKey = "", ioDispatcher = testDispatcher),
            ioDispatcher = testDispatcher
        )
        viewModel = PainMapViewModel(painRecordRepository, aiTriageRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectRegion_opensLoggingSheetWithSelectedRegion() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.SelectRegion(AnatomicalRegion.KNEE_LEFT))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Logging sheet should be open", state.isLoggingSheetOpen)
        assertEquals(AnatomicalRegion.KNEE_LEFT, state.selectedRegion)
        assertEquals(AnatomicalRegion.KNEE_LEFT, state.currentEditingPoint?.region)
    }

    @Test
    fun savePainPoint_persistsPointAndClosesSheet() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val testPoint = PainPoint(
            region = AnatomicalRegion.SHOULDER_RIGHT,
            intensity = 8,
            painTypes = setOf(PainType.BURNING, PainType.STIFFNESS),
            duration = PainDuration.SUBACUTE
        )

        viewModel.onAction(PainMapUiAction.SavePainPoint(testPoint))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Sheet should close after save", state.isLoggingSheetOpen)
        assertEquals(1, state.activePainPoints.size)
        assertEquals(AnatomicalRegion.SHOULDER_RIGHT, state.activePainPoints[0].region)
        assertEquals(8, state.activePainPoints[0].intensity)
    }

    @Test
    fun requestAiTriage_generatesReportAndSavesSessionSuccessfully() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val testPoint = PainPoint(
            region = AnatomicalRegion.LOWER_BACK_LUMBAR,
            intensity = 6
        )
        painRecordRepository.savePainPoint(testPoint)
        advanceUntilIdle()

        var successCallbackCalled = false
        viewModel.onAction(
            PainMapUiAction.RequestAiTriage(
                userNotes = "Pain when bending",
                onSuccess = { successCallbackCalled = true }
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Loading indicator should be cleared", state.isTriageLoading)
        assertTrue("Success callback should be invoked", successCallbackCalled)
        assertNotNull("Triage report should be set", state.latestTriageReport)
        assertNotNull("Current session ID should be set", state.currentSessionId)
        assertEquals(1, state.sessionsList.size)
        assertTrue(state.latestTriageReport?.potentialConditionsToDiscuss?.isNotEmpty() == true)
    }

    @Test
    fun sendFollowUpQuestion_appendsUserAndAiMessage() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val testPoint = PainPoint(region = AnatomicalRegion.KNEE_RIGHT, intensity = 6)
        painRecordRepository.savePainPoint(testPoint)
        advanceUntilIdle()

        viewModel.onAction(PainMapUiAction.RequestAiTriage())
        advanceUntilIdle()

        viewModel.onAction(PainMapUiAction.SendFollowUpQuestion("What stretches help?"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.chatHistory.size)
        assertEquals(MessageSender.USER, state.chatHistory[0].sender)
        assertEquals("What stretches help?", state.chatHistory[0].message)
        assertEquals(MessageSender.AI, state.chatHistory[1].sender)
        assertFalse("Follow up loading should be cleared", state.isAskingFollowUp)
    }

    @Test
    fun loadSession_restoresPainPointsAndTriageReport() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val testSession = PainAssessmentSession(
            id = "saved-session-123",
            painPoints = listOf(PainPoint(region = AnatomicalRegion.ANKLE_FOOT_LEFT, intensity = 7)),
            triageReport = com.example.painmap.domain.model.ClinicalTriageReport(
                preliminaryAssessment = "Ankle sprain presentation"
            )
        )
        painRecordRepository.saveSession(testSession)
        advanceUntilIdle()

        var loadCallbackCalled = false
        viewModel.onAction(
            PainMapUiAction.LoadSession(
                sessionId = "saved-session-123",
                onLoaded = { loadCallbackCalled = true }
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("onLoaded callback should be called", loadCallbackCalled)
        assertEquals("saved-session-123", state.currentSessionId)
        assertEquals(1, state.activePainPoints.size)
        assertEquals(AnatomicalRegion.ANKLE_FOOT_LEFT, state.activePainPoints[0].region)
        assertEquals("Ankle sprain presentation", state.latestTriageReport?.preliminaryAssessment)
    }

    @Test
    fun deletePainPoint_removesPointAndClosesSheet() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val testPoint = PainPoint(
            id = "point-delete-1",
            region = AnatomicalRegion.ELBOW_LEFT,
            intensity = 5
        )
        painRecordRepository.savePainPoint(testPoint)
        advanceUntilIdle()

        viewModel.onAction(PainMapUiAction.DeletePainPoint("point-delete-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Active pain points should be empty", state.activePainPoints.isEmpty())
        assertFalse("Logging sheet should be closed", state.isLoggingSheetOpen)
    }

    @Test
    fun clearAllPoints_removesAllPainPoints() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        painRecordRepository.savePainPoint(PainPoint(region = AnatomicalRegion.HEAD))
        painRecordRepository.savePainPoint(PainPoint(region = AnatomicalRegion.NECK_CERVICAL))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.activePainPoints.size)

        viewModel.onAction(PainMapUiAction.ClearAllPoints)
        advanceUntilIdle()

        assertTrue("All active points should be cleared", viewModel.uiState.value.activePainPoints.isEmpty())
    }

    @Test
    fun requestAiTriage_withEmptyPainPoints_setsErrorMessage() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        var successCalled = false
        viewModel.onAction(PainMapUiAction.RequestAiTriage(onSuccess = { successCalled = true }))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Success callback should not be called", successCalled)
        assertFalse("Triage should not be loading", state.isTriageLoading)
        assertNotNull("Error message should be set", state.errorMessage)
    }

    @Test
    fun dismissError_clearsErrorMessage() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.RequestAiTriage())
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.onAction(PainMapUiAction.DismissError)
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun setToolMode_updatesToolMode() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.SetToolMode(com.example.painmap.ui.components.model3d.PaintToolMode.PAINT))
        advanceUntilIdle()
        assertEquals(com.example.painmap.ui.components.model3d.PaintToolMode.PAINT, viewModel.uiState.value.toolMode)
    }

    @Test
    fun setBrushIntensity_updatesBrushIntensity() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.SetBrushIntensity(8))
        advanceUntilIdle()
        assertEquals(8, viewModel.uiState.value.brushIntensity)
    }

    @Test
    fun paintRegion_createsAndPersistsPainPoint() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.PaintRegion(AnatomicalRegion.LOWER_BACK_LUMBAR, 7))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.activePainPoints.size)
        assertEquals(AnatomicalRegion.LOWER_BACK_LUMBAR, state.activePainPoints[0].region)
        assertEquals(7, state.activePainPoints[0].intensity)
    }

    @Test
    fun eraseRegion_removesPainPoint() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onAction(PainMapUiAction.PaintRegion(AnatomicalRegion.KNEE_LEFT, 6))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.activePainPoints.size)

        viewModel.onAction(PainMapUiAction.EraseRegion(AnatomicalRegion.KNEE_LEFT))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.activePainPoints.isEmpty())
    }
}
