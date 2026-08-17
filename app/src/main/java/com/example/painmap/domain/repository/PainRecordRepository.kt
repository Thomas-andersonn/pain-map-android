package com.example.painmap.domain.repository

import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.PainAssessmentSession
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

    /**
     * Emits all persistent historical assessment sessions.
     */
    fun getAllSessions(): Flow<List<PainAssessmentSession>>

    /**
     * Persists or updates an assessment session.
     */
    suspend fun saveSession(session: PainAssessmentSession): Result<Unit>

    /**
     * Retrieves a specific session by ID.
     */
    suspend fun getSessionById(sessionId: String): Result<PainAssessmentSession?>

    /**
     * Deletes a session from persistent storage.
     */
    suspend fun deleteSession(sessionId: String): Result<Unit>

    /**
     * Appends a chat message to a session's Q&A history.
     */
    suspend fun appendChatMessage(sessionId: String, message: ChatMessage): Result<Unit>
}
