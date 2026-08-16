package com.example.painmap.ui.screens.painmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.BodyCategory
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.components.AnatomicalBodyViewer
import com.example.painmap.ui.components.PainLogBottomSheet
import com.example.painmap.ui.theme.SeverityCritical
import com.example.painmap.ui.theme.SeverityHigh
import com.example.painmap.ui.theme.SeverityLow
import com.example.painmap.ui.theme.SeverityMedium
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainMapScreen(
    uiState: PainMapUiState,
    onAction: (PainMapUiAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToTriage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onAction(PainMapUiAction.DismissError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "3D Pain Mapping",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.activePainPoints.size} joint/muscle point(s) recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.activePainPoints.isNotEmpty()) {
                        IconButton(onClick = { onAction(PainMapUiAction.ClearAllPoints) }) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear all points",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom Action Bar: Active Points Summary & Gemini CTA
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    if (uiState.activePainPoints.isNotEmpty()) {
                        // Quick Scroll of active pain points
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = uiState.activePainPoints,
                                key = { it.id }
                            ) { point ->
                                ActivePainPointBadge(
                                    point = point,
                                    onClick = { onAction(PainMapUiAction.OpenLoggingSheet(initialPoint = point)) },
                                    onDelete = { onAction(PainMapUiAction.DeletePainPoint(point.id)) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            onAction(
                                PainMapUiAction.RequestAiTriage(
                                    onSuccess = onNavigateToTriage
                                )
                            )
                        },
                        enabled = uiState.activePainPoints.isNotEmpty() && !uiState.isTriageLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isTriageLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Diagnosing Joint & Muscle Root Cause...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.activePainPoints.isEmpty()) "Tap a Joint/Muscle to Begin" else "Analyze with Gemini AI (${uiState.activePainPoints.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Quick Region Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BodyCategory.entries) { cat ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            // Find primary region for this category and open
                            val defaultRegion = when (cat) {
                                BodyCategory.HEAD_NECK -> AnatomicalRegion.NECK_CERVICAL
                                BodyCategory.TORSO -> AnatomicalRegion.LOWER_BACK_LUMBAR
                                BodyCategory.UPPER_LIMB -> AnatomicalRegion.SHOULDER_RIGHT
                                BodyCategory.LOWER_LIMB -> AnatomicalRegion.KNEE_RIGHT
                            }
                            onAction(PainMapUiAction.SelectRegion(defaultRegion))
                        },
                        label = { Text(text = "+ ${cat.label}", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3D Body Map Viewport
            AnatomicalBodyViewer(
                activePainPoints = uiState.activePainPoints,
                selectedRegion = uiState.selectedRegion,
                onSelectRegion = { region ->
                    onAction(PainMapUiAction.SelectRegion(region))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Material 3 Modal Bottom Sheet for granular pain logging
        if (uiState.isLoggingSheetOpen && uiState.currentEditingPoint != null) {
            PainLogBottomSheet(
                painPoint = uiState.currentEditingPoint,
                onDismiss = { onAction(PainMapUiAction.DismissLoggingSheet) },
                onSave = { updatedPoint ->
                    onAction(PainMapUiAction.SavePainPoint(updatedPoint))
                },
                onDelete = { id ->
                    onAction(PainMapUiAction.DeletePainPoint(id))
                }
            )
        }
    }
}

@Composable
private fun ActivePainPointBadge(
    point: PainPoint,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = when {
        point.intensity <= 3 -> SeverityLow
        point.intensity <= 6 -> SeverityMedium
        point.intensity <= 8 -> SeverityHigh
        else -> SeverityCritical
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(severityColor)
            )
            Text(
                text = "${point.region.displayName} (${point.intensity}/10)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
