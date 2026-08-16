package com.example.painmap.data.repository

import com.example.painmap.data.mapper.TriageMapper
import com.example.painmap.data.remote.gemini.GeminiAiService
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.repository.AiTriageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AiTriageRepositoryImpl(
    private val geminiService: GeminiAiService = GeminiAiService(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AiTriageRepository {

    private val _latestReport = MutableStateFlow<ClinicalTriageReport?>(null)
    override fun getLatestTriageReport(): Flow<ClinicalTriageReport?> = _latestReport.asStateFlow()

    override suspend fun analyzePainPoints(
        painPoints: List<PainPoint>,
        userNotes: String
    ): Result<ClinicalTriageReport> = withContext(ioDispatcher) {
        val result = geminiService.generateTriage(painPoints, userNotes)
        result.map { dto ->
            val report = TriageMapper.mapToDomain(dto, painPoints)
            _latestReport.value = report
            report
        }
    }

    override suspend fun saveTriageReport(report: ClinicalTriageReport): Result<Unit> = withContext(ioDispatcher) {
        _latestReport.value = report
        Result.success(Unit)
    }
}
