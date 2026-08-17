package com.example.painmap.ui.components.model3d

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.ui.graphics.Path
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
 * Z-Anatomy high-definition 3D Musculoskeletal Node with 3D coordinate space [x, y, z],
 * anatomical muscle contours, fiber striations, and joint capsule bounding geometries.
 * Coordinate space:
 * x: [-1.0 (Left) to +1.0 (Right)]
 * y: [-1.0 (Feet) to +1.0 (Head)]
 * z: [-1.0 (Posterior/Back) to +1.0 (Anterior/Front)]
 */
private data class ZAnatomy3DNode(
    val region: AnatomicalRegion,
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val radius3D: Float,
    val width3D: Float,
    val height3D: Float,
    val isJoint: Boolean = false,
    val isMuscularBelly: Boolean = true,
    val muscleGroup: String = "Skeletal"
)

private val Z_ANATOMY_MUSCULOSKELETAL_NODES = listOf(
    // 1. Head, Cranium & Cervical Spine (Z-Anatomy Head/Neck Group)
    ZAnatomy3DNode(AnatomicalRegion.HEAD, "Cranium & Temporalis", 0.0f, 0.86f, 0.0f, 0.12f, 0.20f, 0.22f),
    ZAnatomy3DNode(AnatomicalRegion.FOREHEAD, "Frontalis & Galea", 0.0f, 0.88f, 0.08f, 0.08f, 0.16f, 0.08f),
    ZAnatomy3DNode(AnatomicalRegion.TEMPLE_LEFT, "Left Temporalis", -0.10f, 0.85f, 0.04f, 0.06f, 0.08f, 0.08f),
    ZAnatomy3DNode(AnatomicalRegion.TEMPLE_RIGHT, "Right Temporalis", 0.10f, 0.85f, 0.04f, 0.06f, 0.08f, 0.08f),
    ZAnatomy3DNode(AnatomicalRegion.JAW_FACIAL, "Masseter & TMJ", 0.0f, 0.77f, 0.06f, 0.07f, 0.15f, 0.08f),
    ZAnatomy3DNode(AnatomicalRegion.NECK_CERVICAL, "Cervical Spine & Sternocleidomastoid", 0.0f, 0.69f, 0.0f, 0.08f, 0.12f, 0.10f, isJoint = true, muscleGroup = "Cervical"),

    // 2. Torso: Pectorals, Abdominals, Trapezius & Lumbar Spine (Z-Anatomy Torso Group)
    ZAnatomy3DNode(AnatomicalRegion.CHEST_THORACIC, "Pectoralis Major & Sternum", 0.0f, 0.50f, 0.07f, 0.16f, 0.38f, 0.24f, muscleGroup = "Pectoral"),
    ZAnatomy3DNode(AnatomicalRegion.UPPER_BACK, "Trapezius & Rhomboids", 0.0f, 0.50f, -0.07f, 0.16f, 0.38f, 0.24f, muscleGroup = "Thoracic"),
    ZAnatomy3DNode(AnatomicalRegion.ABDOMEN, "Rectus Abdominis & Obliques", 0.0f, 0.25f, 0.06f, 0.15f, 0.32f, 0.22f, muscleGroup = "Abdominal"),
    ZAnatomy3DNode(AnatomicalRegion.LOWER_BACK_LUMBAR, "Lumbar Erector Spinae & Latissimus", 0.0f, 0.25f, -0.06f, 0.15f, 0.32f, 0.22f, muscleGroup = "Lumbar"),
    ZAnatomy3DNode(AnatomicalRegion.PELVIS_HIPS, "Pelvis & Sacroiliac Joint", 0.0f, 0.04f, 0.0f, 0.16f, 0.34f, 0.16f, isJoint = true, muscleGroup = "Pelvic"),

    // 3. Upper Extremities: Deltoids, Biceps, Triceps, Forearms, Wrists (Z-Anatomy Arm Group)
    ZAnatomy3DNode(AnatomicalRegion.SHOULDER_LEFT, "Left Deltoid & Rotator Cuff", -0.34f, 0.58f, 0.0f, 0.11f, 0.16f, 0.16f, isJoint = true, muscleGroup = "Deltoid"),
    ZAnatomy3DNode(AnatomicalRegion.SHOULDER_RIGHT, "Right Deltoid & Rotator Cuff", 0.34f, 0.58f, 0.0f, 0.11f, 0.16f, 0.16f, isJoint = true, muscleGroup = "Deltoid"),
    ZAnatomy3DNode(AnatomicalRegion.ARM_UPPER_LEFT, "Left Biceps & Triceps", -0.38f, 0.42f, 0.0f, 0.09f, 0.14f, 0.18f, muscleGroup = "Brachial"),
    ZAnatomy3DNode(AnatomicalRegion.ARM_UPPER_RIGHT, "Right Biceps & Triceps", 0.38f, 0.42f, 0.0f, 0.09f, 0.14f, 0.18f, muscleGroup = "Brachial"),
    ZAnatomy3DNode(AnatomicalRegion.ELBOW_LEFT, "Left Elbow Joint", -0.42f, 0.28f, -0.02f, 0.08f, 0.11f, 0.11f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.ELBOW_RIGHT, "Right Elbow Joint", 0.42f, 0.28f, -0.02f, 0.08f, 0.11f, 0.11f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.FOREARM_LEFT, "Left Forearm Flexors & Extensors", -0.45f, 0.14f, 0.0f, 0.08f, 0.12f, 0.18f, muscleGroup = "Antebrachial"),
    ZAnatomy3DNode(AnatomicalRegion.FOREARM_RIGHT, "Right Forearm Flexors & Extensors", 0.45f, 0.14f, 0.0f, 0.08f, 0.12f, 0.18f, muscleGroup = "Antebrachial"),
    ZAnatomy3DNode(AnatomicalRegion.WRIST_HAND_LEFT, "Left Wrist & Hand", -0.48f, -0.02f, 0.0f, 0.08f, 0.10f, 0.14f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.WRIST_HAND_RIGHT, "Right Wrist & Hand", 0.48f, -0.02f, 0.0f, 0.08f, 0.10f, 0.14f, isJoint = true),

    // 4. Lower Extremities: Glutes, Quads, Hamstrings, Knees, Calves, Ankles (Z-Anatomy Leg Group)
    ZAnatomy3DNode(AnatomicalRegion.HIP_LEFT, "Left Hip & Gluteus Medius", -0.19f, 0.02f, 0.0f, 0.12f, 0.18f, 0.18f, isJoint = true, muscleGroup = "Gluteal"),
    ZAnatomy3DNode(AnatomicalRegion.HIP_RIGHT, "Right Hip & Gluteus Medius", 0.19f, 0.02f, 0.0f, 0.12f, 0.18f, 0.18f, isJoint = true, muscleGroup = "Gluteal"),
    ZAnatomy3DNode(AnatomicalRegion.THIGH_QUAD_LEFT, "Left Quadriceps & Hamstrings", -0.19f, -0.22f, 0.0f, 0.12f, 0.17f, 0.28f, muscleGroup = "Femoral"),
    ZAnatomy3DNode(AnatomicalRegion.THIGH_QUAD_RIGHT, "Right Quadriceps & Hamstrings", 0.19f, -0.22f, 0.0f, 0.12f, 0.17f, 0.28f, muscleGroup = "Femoral"),
    ZAnatomy3DNode(AnatomicalRegion.KNEE_LEFT, "Left Knee Joint & Patella", -0.19f, -0.45f, 0.02f, 0.09f, 0.14f, 0.14f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.KNEE_RIGHT, "Right Knee Joint & Patella", 0.19f, -0.45f, 0.02f, 0.09f, 0.14f, 0.14f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.CALF_SHIN_LEFT, "Left Gastrocnemius & Soleus", -0.19f, -0.66f, 0.0f, 0.09f, 0.13f, 0.24f, muscleGroup = "Crural"),
    ZAnatomy3DNode(AnatomicalRegion.CALF_SHIN_RIGHT, "Right Gastrocnemius & Soleus", 0.19f, -0.66f, 0.0f, 0.09f, 0.13f, 0.24f, muscleGroup = "Crural"),
    ZAnatomy3DNode(AnatomicalRegion.ANKLE_FOOT_LEFT, "Left Ankle & Talus Joint", -0.19f, -0.86f, 0.04f, 0.08f, 0.12f, 0.14f, isJoint = true),
    ZAnatomy3DNode(AnatomicalRegion.ANKLE_FOOT_RIGHT, "Right Ankle & Talus Joint", 0.19f, -0.86f, 0.04f, 0.08f, 0.12f, 0.14f, isJoint = true)
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
            // Top Bar: Z-Anatomy Title & Orbit Presets
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
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Z-Anatomy 3D Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Musculoskeletal & Articular Atlas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                Color(0xFF1E293B).copy(alpha = 0.08f),
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
                    drawZAnatomyMusculoskeletalModel(
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

private fun DrawScope.drawZAnatomyMusculoskeletalModel(
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

    val projectedNodes = Z_ANATOMY_MUSCULOSKELETAL_NODES.map { node ->
        val x1 = node.x * cos(yawRad) + node.z * sin(yawRad)
        val z1 = -node.x * sin(yawRad) + node.z * cos(yawRad)

        val y2 = node.y * cos(pitchRad) - z1 * sin(pitchRad)
        val z2 = node.y * sin(pitchRad) + z1 * cos(pitchRad)

        val perspective = 1.0f / (1.0f - z2 * 0.25f)
        val screenX = cx + x1 * baseScale * perspective
        val screenY = cy - y2 * baseScale * perspective

        val screenRadius = node.radius3D * baseScale * perspective
        val screenW = node.width3D * baseScale * perspective
        val screenH = node.height3D * baseScale * perspective

        ProjectedZAnatomyNode(
            node = node,
            screenX = screenX,
            screenY = screenY,
            screenZ = z2,
            radius = screenRadius,
            width = screenW,
            height = screenH
        )
    }.sortedBy { it.screenZ }

    // 1. Draw Kinematic Skeletal Rig & Vertebral Column
    drawZAnatomySkeletalKinematics(projectedNodes, baseScale)

    // 2. Draw Z-Anatomy Muscle Bellies and Heatmap Shaders
    projectedNodes.forEach { p ->
        val painPoint = activePainPoints.find { it.region == p.node.region }
        val isSelected = selectedRegion == p.node.region
        val isFrontFacing = p.screenZ >= -0.2f

        val baseMuscleFill = if (isFrontFacing) {
            Color(0xFFE2E8F0).copy(alpha = 0.60f)
        } else {
            Color(0xFFCBD5E1).copy(alpha = 0.40f)
        }

        val baseMuscleBorder = if (isFrontFacing) {
            Color(0xFF94A3B8).copy(alpha = 0.85f)
        } else {
            Color(0xFF64748B).copy(alpha = 0.50f)
        }

        if (painPoint != null) {
            val severityColor = getSeverityColor(painPoint.intensity)

            // Radiating Heatmap Aura
            drawCircle(
                color = severityColor.copy(alpha = 0.25f),
                radius = p.radius * pulseAura * 1.4f,
                center = Offset(p.screenX, p.screenY)
            )

            // Volumetric Muscle Belly Gradient
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        severityColor.copy(alpha = 0.90f),
                        severityColor.copy(alpha = 0.55f)
                    ),
                    center = Offset(p.screenX, p.screenY),
                    radius = p.radius * 1.2f
                ),
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.65f, p.radius * 0.65f),
                style = Fill
            )

            // Accent Boundary
            drawRoundRect(
                color = severityColor,
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.65f, p.radius * 0.65f),
                style = Stroke(width = 2.5f)
            )

            // Center Pin Indicator
            drawCircle(
                color = severityColor,
                radius = 12f * zoomScale,
                center = Offset(p.screenX, p.screenY)
            )
            drawCircle(
                color = Color.White,
                radius = 12f * zoomScale,
                center = Offset(p.screenX, p.screenY),
                style = Stroke(width = 2.0f)
            )
        } else {
            // Anatomical Muscle Contour
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseMuscleFill,
                        baseMuscleFill.copy(alpha = 0.35f)
                    )
                ),
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.60f, p.radius * 0.60f),
                style = Fill
            )

            drawRoundRect(
                color = if (isSelected) TealLight else baseMuscleBorder,
                topLeft = Offset(p.screenX - p.width / 2f, p.screenY - p.height / 2f),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(p.radius * 0.60f, p.radius * 0.60f),
                style = Stroke(width = if (isSelected) 2.5f else 1.5f)
            )

            // Articular Joint Node Capsule
            if (p.node.isJoint) {
                drawCircle(
                    color = if (isSelected) TealPrimary else Color(0xFF64748B).copy(alpha = 0.65f),
                    radius = p.radius * 0.38f,
                    center = Offset(p.screenX, p.screenY),
                    style = Fill
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = p.radius * 0.38f,
                    center = Offset(p.screenX, p.screenY),
                    style = Stroke(width = 1.0f)
                )
            }
        }
    }
}

private fun DrawScope.drawZAnatomySkeletalKinematics(nodes: List<ProjectedZAnatomyNode>, baseScale: Float) {
    val nodeMap = nodes.associateBy { it.node.region }
    val rigColor = Color(0xFF475569).copy(alpha = 0.45f)
    val spineColor = Color(0xFF334155).copy(alpha = 0.60f)

    fun connect(r1: AnatomicalRegion, r2: AnatomicalRegion, strokeWidth: Float = 2.0f, color: Color = rigColor) {
        val n1 = nodeMap[r1]
        val n2 = nodeMap[r2]
        if (n1 != null && n2 != null) {
            drawLine(
                color = color,
                start = Offset(n1.screenX, n1.screenY),
                end = Offset(n2.screenX, n2.screenY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }

    // Spine Column & Thoracic Cage
    connect(AnatomicalRegion.HEAD, AnatomicalRegion.NECK_CERVICAL, strokeWidth = 3.5f, color = spineColor)
    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.CHEST_THORACIC, strokeWidth = 3.0f, color = spineColor)
    connect(AnatomicalRegion.CHEST_THORACIC, AnatomicalRegion.ABDOMEN, strokeWidth = 3.0f, color = spineColor)
    connect(AnatomicalRegion.ABDOMEN, AnatomicalRegion.PELVIS_HIPS, strokeWidth = 3.5f, color = spineColor)

    // Upper Limbs Kinematics
    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.SHOULDER_LEFT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.SHOULDER_LEFT, AnatomicalRegion.ARM_UPPER_LEFT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.ARM_UPPER_LEFT, AnatomicalRegion.ELBOW_LEFT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.ELBOW_LEFT, AnatomicalRegion.FOREARM_LEFT, strokeWidth = 2.0f)
    connect(AnatomicalRegion.FOREARM_LEFT, AnatomicalRegion.WRIST_HAND_LEFT, strokeWidth = 1.8f)

    connect(AnatomicalRegion.NECK_CERVICAL, AnatomicalRegion.SHOULDER_RIGHT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.SHOULDER_RIGHT, AnatomicalRegion.ARM_UPPER_RIGHT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.ARM_UPPER_RIGHT, AnatomicalRegion.ELBOW_RIGHT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.ELBOW_RIGHT, AnatomicalRegion.FOREARM_RIGHT, strokeWidth = 2.0f)
    connect(AnatomicalRegion.FOREARM_RIGHT, AnatomicalRegion.WRIST_HAND_RIGHT, strokeWidth = 1.8f)

    // Lower Limbs Kinematics
    connect(AnatomicalRegion.PELVIS_HIPS, AnatomicalRegion.HIP_LEFT, strokeWidth = 2.8f)
    connect(AnatomicalRegion.HIP_LEFT, AnatomicalRegion.THIGH_QUAD_LEFT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.THIGH_QUAD_LEFT, AnatomicalRegion.KNEE_LEFT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.KNEE_LEFT, AnatomicalRegion.CALF_SHIN_LEFT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.CALF_SHIN_LEFT, AnatomicalRegion.ANKLE_FOOT_LEFT, strokeWidth = 2.0f)

    connect(AnatomicalRegion.PELVIS_HIPS, AnatomicalRegion.HIP_RIGHT, strokeWidth = 2.8f)
    connect(AnatomicalRegion.HIP_RIGHT, AnatomicalRegion.THIGH_QUAD_RIGHT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.THIGH_QUAD_RIGHT, AnatomicalRegion.KNEE_RIGHT, strokeWidth = 2.5f)
    connect(AnatomicalRegion.KNEE_RIGHT, AnatomicalRegion.CALF_SHIN_RIGHT, strokeWidth = 2.2f)
    connect(AnatomicalRegion.CALF_SHIN_RIGHT, AnatomicalRegion.ANKLE_FOOT_RIGHT, strokeWidth = 2.0f)
}

private data class ProjectedZAnatomyNode(
    val node: ZAnatomy3DNode,
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
    val bestNode = findClosestZAnatomyNode(touchPos, canvasWidth, canvasHeight, yaw, pitch, zoom)
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
    val bestNode = findClosestZAnatomyNode(touchPos, canvasWidth, canvasHeight, yaw, pitch, zoom)
    if (bestNode != null) {
        onSelect(bestNode.region)
    }
}

private fun findClosestZAnatomyNode(
    touchPos: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    yaw: Float,
    pitch: Float,
    zoom: Float
): ZAnatomy3DNode? {
    val cx = canvasWidth / 2f
    val cy = canvasHeight / 2f
    val baseScale = canvasHeight * 0.46f * zoom

    val yawRad = (yaw * PI / 180f).toFloat()
    val pitchRad = (pitch * PI / 180f).toFloat()

    var closestNode: ZAnatomy3DNode? = null
    var minDistance = Float.MAX_VALUE

    Z_ANATOMY_MUSCULOSKELETAL_NODES.forEach { node ->
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
