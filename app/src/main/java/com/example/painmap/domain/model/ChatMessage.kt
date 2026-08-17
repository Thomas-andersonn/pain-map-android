package com.example.painmap.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class MessageSender {
    USER,
    AI
}

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
