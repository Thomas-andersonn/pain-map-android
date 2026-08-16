package com.example.painmap.domain.repository

import com.example.painmap.domain.model.PainPoint
import kotlinx.coroutines.flow.Flow

interface PainRecordRepository {
    /**
     * Emits the current list of active pain points mapped by the user.
     */
    fun getActivePainPoints(): Flow<List<PainPoint>>

    /**
     * Adds or updates a pain point.
     */
    suspend fun savePainPoint(painPoint: PainPoint): Result<Unit>

    /**
     * Removes a pain point by its unique identifier.
     */
    suspend fun removePainPoint(id: String): Result<Unit>

    /**
     * Clears all active pain points.
     */
    suspend fun clearActivePainPoints(): Result<Unit>
}
