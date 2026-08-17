package com.example.painmap.ui.screens.triage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.painmap.domain.model.ChatMessage
import com.example.painmap.domain.model.ClinicalTriageReport
import com.example.painmap.domain.model.MessageSender
import com.example.painmap.domain.model.UrgencyLevel
import com.example.painmap.ui.theme.SeverityCritical
import com.example.painmap.ui.theme.SeverityHigh
import com.example.painmap.ui.theme.SeverityLow
import com.example.painmap.ui.theme.SeverityMedium
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary

import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TriageResultScreen(
    report: ClinicalTriageReport?,
    chatHistory: List<ChatMessage> = emptyList(),
    mapSnapshotBase64: String? = null,
    isAskingFollowUp: Boolean = false,
    onSendFollowUp: (String) -> Unit = {},
    onNavigateBackToMap: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TealLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "AI Clinical Triage Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToMap) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Body Map"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBackToMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Edit Body Map", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onNavigateToDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No triage report available. Please map pain points first.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Urgency Banner Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getUrgencyColor(report.urgencyLevel).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(getUrgencyColor(report.urgencyLevel)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.urgencyLevel.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = getUrgencyColor(report.urgencyLevel)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = report.urgencyLevel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 2. Multimodal 3D Visual Map Snapshot Card
                if (!mapSnapshotBase64.isNullOrBlank()) {
                    val mapBitmap = remember(mapSnapshotBase64) {
                        try {
                            val cleanStr = if (mapSnapshotBase64.contains(",")) {
                                mapSnapshotBase64.substringAfter(",")
                            } else {
                                mapSnapshotBase64
                            }
                            val decodedBytes = android.util.Base64.decode(cleanStr, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (mapBitmap != null) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TealPrimary.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "3D Pain Heatmap Analyzed by Gemini 3.7 Flash",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    bitmap = mapBitmap,
                                    contentDescription = "3D Anatomical Pain Map",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // 2. Clinical Synthesis
                SectionCard(
                    title = "Joint & Muscle Root Cause Synthesis",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = TealPrimary
                ) {
                    Text(
                        text = report.preliminaryAssessment,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 3. Potential Conditions
                if (report.potentialConditionsToDiscuss.isNotEmpty()) {
                    SectionCard(
                        title = "Potential Musculoskeletal Conditions to Discuss",
                        icon = Icons.Default.LocalHospital,
                        iconTint = SeverityHigh
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            report.potentialConditionsToDiscuss.forEach { condition ->
                                BulletItem(text = condition)
                            }
                        }
                    }
                }

                // 4. Recommended Specialties
                if (report.recommendedSpecialties.isNotEmpty()) {
                    SectionCard(
                        title = "Recommended Care Specialties",
                        icon = Icons.Default.CheckCircle,
                        iconTint = SeverityLow
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.recommendedSpecialties.forEach { specialty ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(text = specialty, fontWeight = FontWeight.Medium) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }

                // 5. Questions for Doctor
                if (report.suggestedClinicalQuestions.isNotEmpty()) {
                    SectionCard(
                        title = "Questions for Your Doctor / Physical Therapist",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = SeverityMedium
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            report.suggestedClinicalQuestions.forEach { question ->
                                BulletItem(text = question)
                            }
                        }
                    }
                }

                // 6. Ergonomic & Mobility Guidance
                if (report.selfCareSuggestions.isNotEmpty()) {
                    SectionCard(
                        title = "Mobility & Ergonomic Self-Care",
                        icon = Icons.Default.SelfImprovement,
                        iconTint = TealLight
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            report.selfCareSuggestions.forEach { suggestion ->
                                BulletItem(text = suggestion)
                            }
                        }
                    }
                }

                // 7. Interactive Gemini AI Follow-Up Q&A Card
                SectionCard(
                    title = "Ask Gemini AI Follow-Up",
                    icon = Icons.Default.QuestionAnswer,
                    iconTint = TealPrimary
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ask questions specific to your mapped pain points, stretching exercises, or joint biomechanics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Suggested Question Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "What safe stretches can I do right now?",
                                "Could this be related to my desk posture?",
                                "When should I see an orthopedic specialist?",
                                "How can I prevent this from recurring?"
                            ).forEach { chipQuestion ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onSendFollowUp(chipQuestion)
                                        focusManager.clearFocus()
                                    },
                                    label = { Text(text = chipQuestion, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = TealPrimary.copy(alpha = 0.08f),
                                        labelColor = TealPrimary
                                    )
                                )
                            }
                        }

                        // Chat Message Stream
                        if (chatHistory.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chatHistory.forEach { chatMsg ->
                                    val isUser = chatMsg.sender == MessageSender.USER
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 14.dp,
                                                        topEnd = 14.dp,
                                                        bottomStart = if (isUser) 14.dp else 2.dp,
                                                        bottomEnd = if (isUser) 2.dp else 14.dp
                                                    )
                                                )
                                                .background(
                                                    if (isUser) TealPrimary
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                                .fillMaxWidth(if (chatMsg.message.length > 50) 0.9f else 0.75f)
                                        ) {
                                            Column {
                                                if (!isUser) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AutoAwesome,
                                                            contentDescription = null,
                                                            tint = TealPrimary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "Gemini 3.7 Flash",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TealPrimary
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                }
                                                Text(
                                                    text = chatMsg.message,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Loading Indicator
                        if (isAskingFollowUp) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TealPrimary
                                )
                                Text(
                                    text = "Gemini is analyzing your question...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Input Box & Send Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                placeholder = { Text(text = "Ask a question about this session...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (questionInput.isNotBlank() && !isAskingFollowUp) {
                                            onSendFollowUp(questionInput)
                                            questionInput = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )

                            IconButton(
                                onClick = {
                                    if (questionInput.isNotBlank() && !isAskingFollowUp) {
                                        onSendFollowUp(questionInput)
                                        questionInput = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = questionInput.isNotBlank() && !isAskingFollowUp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (questionInput.isNotBlank() && !isAskingFollowUp) TealPrimary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (questionInput.isNotBlank() && !isAskingFollowUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(TealPrimary)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        )
    }
}

private fun getUrgencyColor(urgency: UrgencyLevel): Color {
    return when (urgency) {
        UrgencyLevel.HIGH -> SeverityHigh
        UrgencyLevel.MODERATE -> SeverityMedium
        UrgencyLevel.LOW -> SeverityLow
        UrgencyLevel.ROUTINE -> TealPrimary
    }
}
