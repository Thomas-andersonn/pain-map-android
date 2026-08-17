package com.example.painmap.ui.components.model3d

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.theme.SeverityCritical
import com.example.painmap.ui.theme.SeverityHigh
import com.example.painmap.ui.theme.SeverityLow
import com.example.painmap.ui.theme.SeverityMedium
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 3D Anatomical Body Mesh Node with 3D coordinate space [x, y, z] and bounding radius for hit testing.
 * Coordinate space:
 * x: [-1.0 (Left) to +1.0 (Right)]
 * y: [-1.0 (Feet) to +1.0 (Head)]
 * z: [-1.0 (Posterior/Back) to +1.0 (Anterior/Front)]
 */
private data class Body3DNode(
    val region: AnatomicalRegion,
    val x: Float,
    val y: Float,
    val z: Float,
    val radius3D: Float,
    val segmentWidth: Float,
    val segmentHeight: Float,
    val isJoint: Boolean = false
)

private val ANATOMICAL_3D_NODES = listOf(
    // Head & Neck
    Body3DNode(AnatomicalRegion.HEAD, 0.0f, 0.85f, 0.0f, 0.12f, 0.18f, 0.22f),
    Body3DNode(AnatomicalRegion.JAW_FACIAL, 0.0f, 0.76f, 0.06f, 0.08f, 0.14f, 0.08f),
    Body3DNode(AnatomicalRegion.NECK_CERVICAL, 0.0f, 0.69f, 0.0f, 0.08f, 0.10f, 0.09f, isJoint = true),

    // Torso / Spine
    Body3DNode(AnatomicalRegion.CHEST_THORACIC, 0.0f, 0.48f, 0.06f, 0.16f, 0.36f, 0.26f),
    Body3DNode(AnatomicalRegion.UPPER_BACK, 0.0f, 0.48f, -0.06f, 0.16f, 0.36f, 0.26f),
    Body3DNode(AnatomicalRegion.ABDOMEN, 0.0f, 0.22f, 0.05f, 0.15f, 0.30f, 0.22f),
    Body3DNode(AnatomicalRegion.LOWER_BACK_LUMBAR, 0.0f, 0.22f, -0.05f, 0.15f, 0.30f, 0.22f),
    Body3DNode(AnatomicalRegion.PELVIS_HIPS, 0.0f, 0.02f, 0.0f, 0.16f, 0.34f, 0.16f, isJoint = true),

    // Upper Limbs - Shoulders
    Body3DNode(AnatomicalRegion.SHOULDER_LEFT, -0.32f, 0.58f, 0.0f, 0.10f, 0.14f, 0.14f, isJoint = true),
    Body3DNode(AnatomicalRegion.SHOULDER_RIGHT, 0.32f, 0.58f, 0.0f, 0.10f, 0.14f, 0.14f, isJoint = true),

    // Upper Limbs - Elbows & Forearms
    Body3DNode(AnatomicalRegion.ELBOW_LEFT, -0.42f, 0.34f, 0.0f, 0.09f, 0.12f, 0.14f, isJoint = true),
    Body3DNode(AnatomicalRegion.ELBOW_RIGHT, 0.42f, 0.34f, 0.0f, 0.09f, 0.12f, 0.14f, isJoint = true),
    Body3DNode(AnatomicalRegion.WRIST_HAND_LEFT, -0.50f, 0.08f, 0.0f, 0.09f, 0.10f, 0.16f, isJoint = true),
    Body3DNode(AnatomicalRegion.WRIST_HAND_RIGHT, 0.50f, 0.08f, 0.0f, 0.09f, 0.10f, 0.16f, isJoint = true),

    // Lower Limbs - Hips
    Body3DNode(AnatomicalRegion.HIP_LEFT, -0.18f, 0.0f, 0.0f, 0.11f, 0.16f, 0.16f, isJoint = true),
    Body3DNode(AnatomicalRegion.HIP_RIGHT, 0.18f, 0.0f, 0.0f, 0.11f, 0.16f, 0.16f, isJoint = true),

    // Lower Limbs - Thighs & Knees
    Body3DNode(AnatomicalRegion.THIGH_QUAD_LEFT, -0.18f, -0.22f, 0.0f, 0.12f, 0.16f, 0.26f),
    Body3DNode(AnatomicalRegion.THIGH_QUAD_RIGHT, 0.18f, -0.22f, 0.0f, 0.12f, 0.16f, 0.26f),
    Body3DNode(AnatomicalRegion.KNEE_LEFT, -0.18f, -0.44f, 0.0f, 0.09f, 0.13f, 0.13f, isJoint = true),
    Body3DNode(AnatomicalRegion.KNEE_RIGHT, 0.18f, -0.44f, 0.0f, 0.09f, 0.13f, 0.13f, isJoint = true),

    // Lower Limbs - Calves & Ankles/Feet
    Body3DNode(AnatomicalRegion.CALF_SHIN_LEFT, -0.18f, -0.64f, 0.0f, 0.09f, 0.12f, 0.24f),
    Body3DNode(AnatomicalRegion.CALF_SHIN_RIGHT, 0.18f, -0.64f, 0.0f, 0.09f, 0.12f, 0.24f),
    Body3DNode(AnatomicalRegion.ANKLE_FOOT_LEFT, -0.18f, -0.86f, 0.04f, 0.08f, 0.12f, 0.14f, isJoint = true),
    Body3DNode(AnatomicalRegion.ANKLE_FOOT_RIGHT, 0.18f, -0.86f, 0.04f, 0.08f, 0.12f, 0.14f, isJoint = true)
)

@Composable
fun Anatomical3DViewer(
    activePainPoints: List<PainPoint>,
    selectedRegion: AnatomicalRegion?,
    toolMode: PaintToolMode,
    brushIntensity: Int,
    onPaintRegion: (AnatomicalRegion, Int) -> Unit,
    onEraseRegion: (AnatomicalRegion) -> Unit,
    onSelectRegion: (AnatomicalRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var yawDegrees by remember { mutableFloatStateOf(0f) }
    var pitchDegrees by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "heatmap_pulse")
    val pulseAura by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val transformState = rememberTransformableState { zoomChange, _, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.65f, 2.5f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Top Bar: 3D View Presets (Front / Back / Reset)
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
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = null,
                        tint = TealLight,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "3D Anatomical Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    QuickViewButton("Front", isSelected = yawDegrees in -25f..25f) {
                        yawDegrees = 0f
                        pitchDegrees = 0f
                    }
                    QuickViewButton("Back", isSelected = yawDegrees in 155f..205f || yawDegrees in -205f..-155f) {
                        yawDegrees = 180f
                        pitchDegrees = 0f
                    }
                    IconButton(
                        onClick = {
                            yawDegrees = 0f
                            pitchDegrees = 0f
                            zoomScale = 1f
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset View",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3D Canvas Viewport
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .transformable(state = transformState)
                    .pointerInput(toolMode, brushIntensity) {
                        if (toolMode == PaintToolMode.ROTATE) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                yawDegrees = (yawDegrees + dragAmount.x * 0.5f) % 360f
                                pitchDegrees = (pitchDegrees - dragAmount.y * 0.3f).coerceIn(-45f, 45f)
                            }
                        } else {
                            detectDragGestures(
                                onDrag = { change, _ ->
                                    change.consume()
                                    hitTestAndProcess(
                                        touchPos = change.position,
                                        canvasWidth = size.width.toFloat(),
                                        canvasHeight = size.height.toFloat(),
                                        yaw = yawDegrees,
                                        pitch = pitchDegrees,
                                        zoom = zoomScale,
                                        toolMode = toolMode,
                                        brushIntensity = brushIntensity,
                                        onPaint = onPaintRegion,
                                        onErase = onEraseRegion
                                    )
                                }
                            )
                        }
                    }
                    .pointerInput(toolMode, brushIntensity) {
                        detectTapGestures { tapPos ->
                            if (toolMode == PaintToolMode.PAINT || toolMode == PaintToolMode.ERASE) {
                                hitTestAndProcess(
                                    touchPos = tapPos,
                                    canvasWidth = size.width.toFloat(),
                                    canvasHeight = size.height.toFloat(),
                                    yaw = yawDegrees,
                                    pitch = pitchDegrees,
                                    zoom = zoomScale,
                                    toolMode = toolMode,
                                    brushIntensity = brushIntensity,
                                    onPaint = onPaintRegion,
                                    onErase = onEraseRegion
                                )
                            } else {
                                hitTestAndSelect(
                                    touchPos = tapPos,
                                    canvasWidth = size.width.toFloat(),
                                    canvasHeight = size.height.toFloat(),
                                    yaw = yawDegrees,
                                    pitch = pitchDegrees,
                                    zoom = zoomScale,
                                    onSelect = onSelectRegion
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    draw3DAnatomicalModel(
                        activePainPoints = activePainPoints,
                        selectedRegion = selectedRegion,
                        yawDegrees = yawDegrees,
                        pitchDegrees = pitchDegrees,
                        zoomScale = zoomScale,
                        pulseAura = pulseAura
                    )
                }

                Text(
                    text = "${((yawDegrees % 360 + 360) % 360).toInt()}° Orbit",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickViewButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
            selectedLabelColor = TealPrimary
        ),
        modifier = Modifier.height(28.dp)
    )
}

private fun DrawScope.draw3DAnatomicalModel(
    activePainPoints: List<PainPoint>,
    selectedRegion: AnatomicalRegion?,
    yawDegrees: Float,
    pitchDegrees: Float,
    zoomScale: Float,
    pulseAura: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseScale = size.height * 0.46f * zoomScale

    val yawRad = (yawDegrees * PI / 180f).toFloat()
    val pitchRad = (pitchDegrees * PI / 180f).toFloat()

    val projectedNodes = ANATOMICAL_3D_NODES.map { node ->
        val x1 = node.x * cos(yawRad) + node.z * sin(yawRad)
        val z1 = -node.x * sin(yawRad) + node.z * cos(yawRad)

        val y2 = node.y * cos(pitchRad) - z1 * sin(pitchRad)
        val z2 = node.y * sin(pitchRad) + z1 * cos(pitchRad)

        val perspective = 1.0f / (1.0f - z2 * 0.25f)
        val screenX = cx + x1 * baseScale * perspective
        val screenY = cy - y2 * baseScale * perspective

        val screenRadius = node.radius3D * baseScale * perspective
        val screenW = node.segmentWidth * baseScale * perspective
        val screenH = node.segmentHeight * baseScale * perspective

        Projected3DNode(
            node = node,
            screenX = screenX,
            screenY = screenY,
            screenZ = z2,
            radius = screenRadius,
            width = screenW,
            height = screenH
        )
    }.sortedBy { it.screenZ }

    drawKinematicRig(projectedNodes, baseScale)

    projectedNodes.forEach { p ->
        val painPoint = activePainPoints.find { it.region == p.node.region }
        val isSelected = selectedRegion == p.node.region
        val isFrontFacing = p.screenZ >= -0.2f

        val baseBodyColor = if (isFrontFacing) {
            Color(0xFF64748B).copy(alpha = 0.55f)
        } else {
            Color(0xFF475569).copy(alpha = 0.35f)
        }

        if (painPoint != null) {
            val severityColor = getSeverityColor(painPoint.intensity)

            drawCircle(
                color = severityColor.copy(alpha = 0.22f),
                radius = p.radius * pulseAura * 1.3f,
                center = Offset(p.screenX, p.screenY)
            )

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        severityColor.copy(alpha = 0.85f),
                        severityColor.copy(alpha = 0.45f)
                    ),
                    center = Offset(p.screenX, p.screenY),
                    radius = p.radius * 1.1f
                ),
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.6f, p.radius * 0.6f),
                style = Fill
            )

            drawRoundRect(
                color = severityColor,
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.6f, p.radius * 0.6f),
                style = Stroke(width = 2.5f)
            )

            drawCircle(
                color = severityColor,
                radius = 12f * zoomScale,
                center = Offset(p.screenX, p.screenY)
            )
            drawCircle(
                color = Color.White,
                radius = 12f * zoomScale,
                center = Offset(p.screenX, p.screenY),
                style = Stroke(width = 1.5f)
            )
        } else {
            drawRoundRect(
                color = if (isSelected) TealLight.copy(alpha = 0.4f) else baseBodyColor,
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.5f, p.radius * 0.5f),
                style = Stroke(width = if (isSelected) 2.5f else 1.5f)
            )

            if (p.node.isJoint) {
                drawCircle(
                    color = if (isSelected) TealPrimary else baseBodyColor.copy(alpha = 0.6f),
                    radius = p.radius * 0.35f,
                    center = Offset(p.screenX, p.screenY),
                    style = Fill
                )
            }
        }
    }
}

private fun DrawScope.drawKinematicRig(nodes: List<Projected3DNode>, baseScale: Float) {
    val nodeMap = nodes.associateBy { it.node.region }
    val rigColor = Color(0xFF334155).copy(alpha = 0.4f)
    val strokeWidth = 2.0f

    fun connect(r1: AnatomicalRegion, r2: AnatomicalRegion) {
        val n1 = nodeMap[r1]
        val n2 = nodeMap[r2]
        if (n1 != null && n2 != null) {
            drawLine(
                color = rigColor,
                start = Offset(n1.screenX, n1.screenY),
                end = Offset(n2.screenX, n2.screenY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }

    // Spine Rig
    connect(AnatomicalRegion.HEAD, AnatomicalRegion.NECK_CERVICAL)
    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.CHEST_THORACIC)
    connect(AnatomicalRegion.CHEST_THORACIC, AnatomicalRegion.ABDOMEN)
    connect(AnatomicalRegion.ABDOMEN, AnatomicalRegion.PELVIS_HIPS)

    // Left Arm Rig
    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.SHOULDER_LEFT)
    connect(AnatomicalRegion.SHOULDER_LEFT, AnatomicalRegion.ELBOW_LEFT)
    connect(AnatomicalRegion.ELBOW_LEFT, AnatomicalRegion.WRIST_HAND_LEFT)

    // Right Arm Rig
    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.SHOULDER_RIGHT)
    connect(AnatomicalRegion.SHOULDER_RIGHT, AnatomicalRegion.ELBOW_RIGHT)
    connect(AnatomicalRegion.ELBOW_RIGHT, AnatomicalRegion.WRIST_HAND_RIGHT)

    // Left Leg Rig
    connect(AnatomicalRegion.PELVIS_HIPS, AnatomicalRegion.HIP_LEFT)
    connect(AnatomicalRegion.HIP_LEFT, AnatomicalRegion.THIGH_QUAD_LEFT)
    connect(AnatomicalRegion.THIGH_QUAD_LEFT, AnatomicalRegion.KNEE_LEFT)
    connect(AnatomicalRegion.KNEE_LEFT, AnatomicalRegion.CALF_SHIN_LEFT)
    connect(AnatomicalRegion.CALF_SHIN_LEFT, AnatomicalRegion.ANKLE_FOOT_LEFT)

    // Right Leg Rig
    connect(AnatomicalRegion.PELVIS_HIPS, AnatomicalRegion.HIP_RIGHT)
    connect(AnatomicalRegion.HIP_RIGHT, AnatomicalRegion.THIGH_QUAD_RIGHT)
    connect(AnatomicalRegion.THIGH_QUAD_RIGHT, AnatomicalRegion.KNEE_RIGHT)
    connect(AnatomicalRegion.KNEE_RIGHT, AnatomicalRegion.CALF_SHIN_RIGHT)
    connect(AnatomicalRegion.CALF_SHIN_RIGHT, AnatomicalRegion.ANKLE_FOOT_RIGHT)
}

private data class Projected3DNode(
    val node: Body3DNode,
    val screenX: Float,
    val screenY: Float,
    val screenZ: Float,
    val radius: Float,
    val width: Float,
    val height: Float
)

private fun hitTestAndProcess(
    touchPos: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float,
    toolMode: PaintToolMode,
    brushIntensity: Int,
    onPaint: (AnatomicalRegion, Int) -> Unit,
    onErase: (AnatomicalRegion) -> Unit
) {
    val bestNode = findClosest3DNode(touchPos, canvasWidth, canvasHeight, yaw, pitch, zoom)
    if (bestNode != null) {
        if (toolMode == PaintToolMode.PAINT) {
            onPaint(bestNode.region, brushIntensity)
        } else if (toolMode == PaintToolMode.ERASE) {
            onErase(bestNode.region)
        }
    }
}

private fun hitTestAndSelect(
    touchPos: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float,
    onSelect: (AnatomicalRegion) -> Unit
) {
    val bestNode = findClosest3DNode(touchPos, canvasWidth, canvasHeight, yaw, pitch, zoom)
    if (bestNode != null) {
        onSelect(bestNode.region)
    }
}

private fun findClosest3DNode(
    touchPos: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float
): Body3DNode? {
    val cx = canvasWidth / 2f
    val cy = canvasHeight / 2f
    val baseScale = canvasHeight * 0.46f * zoom

    val yawRad = (yaw * PI / 180f).toFloat()
    val pitchRad = (pitch * PI / 180f).toFloat()

    var closestNode: Body3DNode? = null
    var minDistance = Float.MAX_VALUE

    ANATOMICAL_3D_NODES.forEach { node ->
        val x1 = node.x * cos(yawRad) + node.z * sin(yawRad)
        val z1 = -node.x * sin(yawRad) + node.z * cos(yawRad)
        val y2 = node.y * cos(pitchRad) - z1 * sin(pitchRad)
        val z2 = node.y * sin(pitchRad) + z1 * cos(pitchRad)

        if (z2 >= -0.35f) {
            val perspective = 1.0f / (1.0f - z2 * 0.25f)
            val screenX = cx + x1 * baseScale * perspective
            val screenY = cy - y2 * baseScale * perspective
            val hitRadius = node.radius3D * baseScale * perspective * 1.35f

            val dist = hypot(touchPos.x - screenX, touchPos.y - screenY)
            if (dist <= hitRadius && dist < minDistance) {
                minDistance = dist
                closestNode = node
            }
        }
    }
    return closestNode
}

private fun getSeverityColor(intensity: Int): Color {
    return when {
        intensity <= 3 -> SeverityLow
        intensity <= 6 -> SeverityMedium
        intensity <= 8 -> SeverityHigh
        else -> SeverityCritical
    }
}
