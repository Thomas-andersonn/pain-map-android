package com.example.painmap.data.repository

import android.content.Context
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.PainAssessmentSession
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.repository.PainRecordRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PainRecordRepositoryImpl(
    private val context: Context? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PainRecordRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val _activePainPoints = MutableStateFlow<List<PainPoint>>(emptyList())
    override fun getActivePainPoints(): Flow<List<PainPoint>> = _activePainPoints.asStateFlow()

    private val _sessions = MutableStateFlow<List<PainAssessmentSession>>(emptyList())
    override fun getAllSessions(): Flow<List<PainAssessmentSession>> = _sessions.asStateFlow()

    init {
        loadPersistedData()
    }

    private fun loadPersistedData() {
        context?.let { ctx ->
            try {
                val pointsFile = File(ctx.filesDir, "active_pain_points.json")
                if (pointsFile.exists()) {
                    val content = pointsFile.readText()
                    if (content.isNotBlank()) {
                        val points = json.decodeFromString<List<PainPoint>>(content)
                        _activePainPoints.value = points
                    }
                }

                val sessionsFile = File(ctx.filesDir, "pain_sessions.json")
                if (sessionsFile.exists()) {
                    val content = sessionsFile.readText()
                    if (content.isNotBlank()) {
                        val sessions = json.decodeFromString<List<PainAssessmentSession>>(content)
                        _sessions.value = sessions
                    }
                }
            } catch (e: Exception) {
                // Ignore corrupt/empty cache on startup
            }
        }
    }

    private fun persistActivePoints() {
        context?.let { ctx ->
            try {
                val pointsFile = File(ctx.filesDir, "active_pain_points.json")
                val content = json.encodeToString(_activePainPoints.value)
                pointsFile.writeText(content)
            } catch (e: Exception) {
                // Disk write error handling
            }
        }
    }

    private fun persistSessions() {
        context?.let { ctx ->
            try {
                val sessionsFile = File(ctx.filesDir, "pain_sessions.json")
                val content = json.encodeToString(_sessions.value)
                sessionsFile.writeText(content)
            } catch (e: Exception) {
                // Disk write error handling
            }
        }
    }

    override suspend fun savePainPoint(painPoint: PainPoint): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.id == painPoint.id || it.region == painPoint.region }
            if (existingIndex >= 0) {
                currentList.toMutableList().apply {
                    this[existingIndex] = painPoint
                }
            } else {
                currentList + painPoint
            }
        }
        persistActivePoints()
        Result.success(Unit)
    }

    override suspend fun removePainPoint(id: String): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.update { currentList ->
            currentList.filterNot { it.id == id }
        }
        persistActivePoints()
        Result.success(Unit)
    }

    override suspend fun clearActivePainPoints(): Result<Unit> = withContext(ioDispatcher) {
        _activePainPoints.value = emptyList()
        persistActivePoints()
        Result.success(Unit)
    }

    override suspend fun saveSession(session: PainAssessmentSession): Result<Unit> = withContext(ioDispatcher) {
        _sessions.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.id == session.id }
            if (existingIndex >= 0) {
                currentList.toMutableList().apply {
                    this[existingIndex] = session
                }
            } else {
                listOf(session) + currentList
            }
        }
        persistSessions()
        Result.success(Unit)
    }

    override suspend fun getSessionById(sessionId: String): Result<PainAssessmentSession?> = withContext(ioDispatcher) {
        val session = _sessions.value.find { it.id == sessionId }
        Result.success(session)
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> = withContext(ioDispatcher) {
        _sessions.update { currentList ->
            currentList.filterNot { it.id == sessionId }
        }
        persistSessions()
        Result.success(Unit)
    }

    override suspend fun appendChatMessage(sessionId: String, message: ChatMessage): Result<Unit> = withContext(ioDispatcher) {
        _sessions.update { currentList ->
            val session = currentList.find { it.id == sessionId } ?: return@update currentList
            val updatedChat = session.chatHistory + message
            val updatedSession = session.copy(chatHistory = updatedChat)
            currentList.map { if (it.id == sessionId) updatedSession else it }
        }
        persistSessions()
        Result.success(Unit)
    }
}
