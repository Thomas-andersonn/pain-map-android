package com.example.painmap.ui.screens.painmap

import com.example.painmap.data.remote.gemini.GeminiAiService
import com.example.painmap.data.repository.AiTriageRepositoryImpl
import com.example.painmap.data.repository.PainRecordRepositoryImpl
import com.example.painmap.domain.model.AnatomicalRegion
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
    fun requestAiTriage_generatesReportSuccessfully() = runTest {
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
        assertTrue(state.latestTriageReport?.potentialConditionsToDiscuss?.isNotEmpty() == true)
    }
}
