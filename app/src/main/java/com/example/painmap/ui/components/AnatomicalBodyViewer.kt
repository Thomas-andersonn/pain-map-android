package com.example.painmap.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.theme.SeverityCritical
import com.example.painmap.ui.theme.SeverityHigh
import com.example.painmap.ui.theme.SeverityLow
import com.example.painmap.ui.theme.SeverityMedium
import com.example.painmap.ui.theme.TealDark
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary
import kotlin.math.roundToInt

enum class BodyOrientation(val label: String) {
    ANTERIOR("Front (Anterior)"),
    POSTERIOR("Back (Posterior)")
}

@Composable
fun AnatomicalBodyViewer(
    activePainPoints: List<PainPoint>,
    selectedRegion: AnatomicalRegion?,
    onSelectRegion: (AnatomicalRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var orientation by remember { mutableStateOf(BodyOrientation.ANTERIOR) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // View Mode Toggle (Front / Back)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = TealLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "3D Anatomical Body Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BodyOrientation.entries.forEach { opt ->
                        FilterChip(
                            selected = orientation == opt,
                            onClick = { orientation = opt },
                            label = {
                                Text(
                                    text = if (opt == BodyOrientation.ANTERIOR) "Front" else "Back",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Canvas & Interactive Nodes Viewport
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val canvasWidth = maxWidth.value
                val canvasHeight = maxHeight.value

                // Draw Anatomical Skeleton / Silhouette Outline
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f

                    val outlineColor = Color(0xFF475569).copy(alpha = 0.4f)
                    val stroke = Stroke(width = 2.5f, cap = StrokeCap.Round)

                    // Head
                    drawCircle(
                        color = outlineColor,
                        radius = h * 0.055f,
                        center = Offset(cx, h * 0.1f),
                        style = stroke
                    )

                    // Neck
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx, h * 0.155f),
                        end = Offset(cx, h * 0.2f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Shoulders & Clavicle
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.25f, h * 0.22f),
                        end = Offset(cx + w * 0.25f, h * 0.22f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Spine / Torso centerline
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx, h * 0.2f),
                        end = Offset(cx, h * 0.52f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Rib cage / Torso outline
                    val torsoPath = Path().apply {
                        moveTo(cx - w * 0.22f, h * 0.22f)
                        lineTo(cx - w * 0.16f, h * 0.48f)
                        lineTo(cx + w * 0.16f, h * 0.48f)
                        lineTo(cx + w * 0.22f, h * 0.22f)
                    }
                    drawPath(torsoPath, outlineColor, style = stroke)

                    // Left Arm (Screen Left)
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.25f, h * 0.22f),
                        end = Offset(cx - w * 0.32f, h * 0.38f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.32f, h * 0.38f),
                        end = Offset(cx - w * 0.38f, h * 0.54f),
                        strokeWidth = 2.5f
                    )

                    // Right Arm (Screen Right)
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx + w * 0.25f, h * 0.22f),
                        end = Offset(cx + w * 0.32f, h * 0.38f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx + w * 0.32f, h * 0.38f),
                        end = Offset(cx + w * 0.38f, h * 0.54f),
                        strokeWidth = 2.5f
                    )

                    // Pelvis line
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.17f, h * 0.52f),
                        end = Offset(cx + w * 0.17f, h * 0.52f),
                        strokeWidth = 2.5f
                    )

                    // Left Leg (Screen Left)
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.14f, h * 0.52f),
                        end = Offset(cx - w * 0.14f, h * 0.72f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx - w * 0.14f, h * 0.72f),
                        end = Offset(cx - w * 0.14f, h * 0.92f),
                        strokeWidth = 2.5f
                    )

                    // Right Leg (Screen Right)
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx + w * 0.14f, h * 0.52f),
                        end = Offset(cx + w * 0.14f, h * 0.72f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(cx + w * 0.14f, h * 0.72f),
                        end = Offset(cx + w * 0.14f, h * 0.92f),
                        strokeWidth = 2.5f
                    )
                }

                // Interactive Joint & Muscle Nodes
                val visibleNodes = getRegionsForOrientation(orientation)
                visibleNodes.forEach { (region, relativePos) ->
                    val activePoint = activePainPoints.find { it.region == region }
                    val isSelected = selectedRegion == region

                    val nodeX = (relativePos.x * canvasWidth).dp
                    val nodeY = (relativePos.y * canvasHeight).dp

                    val severityColor = if (activePoint != null) {
                        getSeverityColor(activePoint.intensity)
                    } else {
                        TealPrimary
                    }

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = ((relativePos.x - 0.5f) * this@BoxWithConstraints.constraints.maxWidth).roundToInt(),
                                    y = ((relativePos.y - 0.5f) * this@BoxWithConstraints.constraints.maxHeight).roundToInt()
                                )
                            }
                            .clickable { onSelectRegion(region) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (activePoint != null) {
                            // Pulsing glow ring for recorded pain point
                            Box(
                                modifier = Modifier
                                    .size((28 * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(severityColor.copy(alpha = 0.25f))
                            )
                        }

                        // Core Node
                        Box(
                            modifier = Modifier
                                .size(if (isSelected || activePoint != null) 22.dp else 16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activePoint != null) severityColor
                                    else if (isSelected) TealLight
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.5.dp,
                                    color = if (activePoint != null) Color.White else TealPrimary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activePoint != null) {
                                Text(
                                    text = "${activePoint.intensity}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hint Text
            Text(
                text = "Tap on any joint or muscle node to log sensations & intensity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class RelativePosition(val x: Float, val y: Float)

private fun getRegionsForOrientation(orientation: BodyOrientation): Map<AnatomicalRegion, RelativePosition> {
    return if (orientation == BodyOrientation.ANTERIOR) {
        mapOf(
            AnatomicalRegion.HEAD to RelativePosition(0.5f, 0.08f),
            AnatomicalRegion.JAW_FACIAL to RelativePosition(0.5f, 0.13f),
            AnatomicalRegion.NECK_CERVICAL to RelativePosition(0.5f, 0.17f),
            AnatomicalRegion.CHEST_THORACIC to RelativePosition(0.5f, 0.27f),
            AnatomicalRegion.ABDOMEN to RelativePosition(0.5f, 0.40f),
            AnatomicalRegion.SHOULDER_LEFT to RelativePosition(0.25f, 0.22f),
            AnatomicalRegion.SHOULDER_RIGHT to RelativePosition(0.75f, 0.22f),
            AnatomicalRegion.ELBOW_LEFT to RelativePosition(0.18f, 0.38f),
            AnatomicalRegion.ELBOW_RIGHT to RelativePosition(0.82f, 0.38f),
            AnatomicalRegion.WRIST_HAND_LEFT to RelativePosition(0.12f, 0.54f),
            AnatomicalRegion.WRIST_HAND_RIGHT to RelativePosition(0.88f, 0.54f),
            AnatomicalRegion.HIP_LEFT to RelativePosition(0.36f, 0.53f),
            AnatomicalRegion.HIP_RIGHT to RelativePosition(0.64f, 0.53f),
            AnatomicalRegion.KNEE_LEFT to RelativePosition(0.36f, 0.72f),
            AnatomicalRegion.KNEE_RIGHT to RelativePosition(0.64f, 0.72f),
            AnatomicalRegion.ANKLE_FOOT_LEFT to RelativePosition(0.36f, 0.92f),
            AnatomicalRegion.ANKLE_FOOT_RIGHT to RelativePosition(0.64f, 0.92f)
        )
    } else {
        mapOf(
            AnatomicalRegion.HEAD to RelativePosition(0.5f, 0.08f),
            AnatomicalRegion.NECK_CERVICAL to RelativePosition(0.5f, 0.17f),
            AnatomicalRegion.UPPER_BACK to RelativePosition(0.5f, 0.28f),
            AnatomicalRegion.LOWER_BACK_LUMBAR to RelativePosition(0.5f, 0.42f),
            AnatomicalRegion.PELVIS_HIPS to RelativePosition(0.5f, 0.52f),
            AnatomicalRegion.SHOULDER_LEFT to RelativePosition(0.25f, 0.22f),
            AnatomicalRegion.SHOULDER_RIGHT to RelativePosition(0.75f, 0.22f),
            AnatomicalRegion.ELBOW_LEFT to RelativePosition(0.18f, 0.38f),
            AnatomicalRegion.ELBOW_RIGHT to RelativePosition(0.82f, 0.38f),
            AnatomicalRegion.HIP_LEFT to RelativePosition(0.36f, 0.53f),
            AnatomicalRegion.HIP_RIGHT to RelativePosition(0.64f, 0.53f),
            AnatomicalRegion.KNEE_LEFT to RelativePosition(0.36f, 0.72f),
            AnatomicalRegion.KNEE_RIGHT to RelativePosition(0.64f, 0.72f),
            AnatomicalRegion.CALF_SHIN_LEFT to RelativePosition(0.36f, 0.82f),
            AnatomicalRegion.CALF_SHIN_RIGHT to RelativePosition(0.64f, 0.82f),
            AnatomicalRegion.ANKLE_FOOT_LEFT to RelativePosition(0.36f, 0.92f),
            AnatomicalRegion.ANKLE_FOOT_RIGHT to RelativePosition(0.64f, 0.92f)
        )
    }
}

private fun getSeverityColor(intensity: Int): Color {
    return when {
        intensity <= 3 -> SeverityLow
        intensity <= 6 -> SeverityMedium
        intensity <= 8 -> SeverityHigh
        else -> SeverityCritical
    }
}
