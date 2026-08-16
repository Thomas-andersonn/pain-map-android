package com.example.painmap.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PainPoint(
    val id: String = UUID.randomUUID().toString(),
    val region: AnatomicalRegion,
    val x: Float = region.defaultX,
    val y: Float = region.defaultY,
    val z: Float = region.defaultZ,
    val intensity: Int = 5, // 1 to 10 scale
    val painTypes: Set<PainType> = setOf(PainType.ACHING),
    val duration: PainDuration = PainDuration.ACUTE,
    val triggers: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class PainType(val displayName: String) {
    THROBBING("Throbbing"),
    STABBING("Stabbing"),
    BURNING("Burning"),
    ACHING("Dull Ache"),
    SHOOTING("Shooting / Radiating"),
    NUMBNESS("Numbness"),
    STIFFNESS("Stiffness"),
    ELECTRIC_SHOCK("Electric Shock")
}

@Serializable
enum class PainDuration(val label: String) {
    ACUTE("Acute (< 2 weeks)"),
    SUBACUTE("Subacute (2–12 weeks)"),
    CHRONIC("Chronic (> 3 months)")
}
