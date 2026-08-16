package com.example.painmap.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PainPoint(
    val id: String,
    val anatomicalRegion: String,
    val xRatio: Float = 0f,
    val yRatio: Float = 0f,
    val zRatio: Float = 0f,
    val intensity: Int, // 1 to 10
    val painType: PainType = PainType.ACHING,
    val notes: String = ""
)

@Serializable
enum class PainType {
    THROBBING,
    STABBING,
    ACHING,
    BURNING,
    NUMBNESS,
    TINGLING,
    STIFFNESS
}
