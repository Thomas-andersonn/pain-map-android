package com.example.painmap.data.repository

import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.repository.PainRecordRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PainRecordRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PainRecordRepository {

    private val _activePainPoints = MutableStateFlow<List<PainPoint>>(emptyList())
    override fun getActivePainPoints(): Flow<List<PainPoint>> = _activePainPoints.asStateFlow()

    override suspend fun savePainPoint(painPoint: PainPoint): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.id == painPoint.id }
            if (existingIndex >= 0) {
                // Update existing point
                currentList.toMutableList().apply {
                    this[existingIndex] = painPoint
                }
            } else {
                // Append new point
                currentList + painPoint
            }
        }
        Result.success(Unit)
    }

    override suspend fun removePainPoint(id: String): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.update { currentList ->
            currentList.filterNot { it.id == id }
        }
        Result.success(Unit)
    }

    override suspend fun clearActivePainPoints(): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.value = emptyList()
        Result.success(Unit)
    }
}
