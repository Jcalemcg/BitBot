package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.BackendSelectorEngine
import com.example.data.engine.OomRisk
import com.example.data.model.LoRAHyperparams
import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.scanner.DeviceHardwareProfile
import com.example.data.scanner.DeviceTier
import com.example.ui.components.CodeSnippetCard
import com.example.ui.components.InlineExplainToggle
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TelemetryEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackendSelectorScreen(
    deviceProfile: DeviceHardwareProfile,
    onRescanRequested: () -> Unit,
    onNavigateToStudioWithConfig: (LoRAHyperparams) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScale by remember { mutableStateOf(ModelScale.SCALE_1B) }
    var selectedQuant by remember { mutableStateOf(QuantFormat.TQ2_0) }
    var isClassifierMode by remember { mutableStateOf(false) }

    val recommendation = remember(selectedScale, selectedQuant, deviceProfile, isClassifierMode) {
        BackendSelectorEngine.evaluate(
            modelScale = selectedScale,
            quantFormat = selectedQuant,
            deviceProfile = deviceProfile,
            isCustomClassifier = isClassifierMode
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = "HARDWARE SCAN & BACKEND SELECTOR",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Dynamically audited against your phone's live physical RAM, GPU drivers, and thermal state.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // 1. DEVICE HARDWARE AUDIT CARD (SCANNED LIVE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCANNED DEVICE PROFILE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${deviceProfile.manufacturer} ${deviceProfile.deviceModel}",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (deviceProfile.tier) {
                                DeviceTier.TIER_S, DeviceTier.TIER_A -> TelemetryEmerald.copy(alpha = 0.2f)
                                DeviceTier.TIER_B -> WarningAmber.copy(alpha = 0.2f)
                                DeviceTier.TIER_C -> AlertCrimson.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = deviceProfile.tier.badgeLabel,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = when (deviceProfile.tier) {
                            DeviceTier.TIER_S, DeviceTier.TIER_A -> TelemetryEmerald
                            DeviceTier.TIER_B -> WarningAmber
                            DeviceTier.TIER_C -> AlertCrimson
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = deviceProfile.tier.description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Pass/Fail Badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                deviceProfile.checkBadges.forEach { badge ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (badge.passed) TelemetryEmerald.copy(alpha = 0.1f) else AlertCrimson.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (badge.passed) TelemetryEmerald.copy(alpha = 0.4f) else AlertCrimson.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (badge.passed) "✓ PASS: " else "✕ FAIL: ",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.passed) TelemetryEmerald else AlertCrimson
                        )
                        Text(
                            text = "${badge.title} (${badge.detail})",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rescan button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onRescanRequested,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RE-SCAN LIVE MEMORY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Workload Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface, RoundedCornerShape(10.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isClassifierMode) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        if (!isClassifierMode) 1.dp else 0.dp,
                        if (!isClassifierMode) NeonCyan else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { isClassifierMode = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BitNet LLM (Path A)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (!isClassifierMode) NeonCyan else TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isClassifierMode) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        if (isClassifierMode) 1.dp else 0.dp,
                        if (isClassifierMode) NeonCyan else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { isClassifierMode = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Classifier ≤ 1M (Path B)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isClassifierMode) NeonCyan else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Model Scale (with DYNAMIC GATING & LOCK BADGES)
        if (!isClassifierMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT MODEL SCALE (DYNAMICALLY GATED):",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModelScale.entries.forEach { scale ->
                    val (canRun, lockReason) = deviceProfile.canRunModel(scale, selectedQuant)
                    val isSelected = selectedScale == scale
                    val isSweetSpot = scale == ModelScale.SCALE_1B && canRun

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    !canRun -> CyberDark
                                    isSelected -> CyberSurfaceVariant
                                    else -> CyberSurface
                                }
                            )
                            .border(
                                1.5.dp,
                                when {
                                    isSelected && !canRun -> AlertCrimson
                                    isSelected -> NeonCyan
                                    !canRun -> AlertCrimson.copy(alpha = 0.35f)
                                    else -> CyberCardBorder
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedScale = scale }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("scale_chip_${scale.label}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!canRun) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked: Exceeds RAM",
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = scale.label,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    !canRun -> AlertCrimson.copy(alpha = 0.7f)
                                    isSelected -> TextPrimary
                                    else -> TextSecondary
                                }
                            )
                            if (isSweetSpot) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "★",
                                    fontSize = 11.sp,
                                    color = TelemetryEmerald
                                )
                            }
                        }
                    }
                }
            }

            InlineExplainToggle(
                conceptKey = "MODEL_SCALE",
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quantization Format Selector
            Text(
                text = "QUANTIZATION FORMAT:",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            InlineExplainToggle(
                conceptKey = "QUANT_FORMAT",
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuantFormat.entries.forEach { quant ->
                    val isSelected = selectedQuant == quant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberSurfaceVariant else CyberSurface)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else CyberCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedQuant = quant }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = quant.displayName,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = quant.description,
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Recommendation Result Card (Calibrated to this device)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(
                    1.5.dp,
                    when (recommendation.oomRisk) {
                        OomRisk.FATAL_OOM -> AlertCrimson
                        OomRisk.HIGH_THERMAL_RISK -> WarningAmber
                        else -> NeonCyan
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECOMMENDED FOR ${deviceProfile.deviceModel.uppercase()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = recommendation.pathTitle,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = when (recommendation.oomRisk) {
                            OomRisk.FATAL_OOM -> AlertCrimson
                            else -> NeonCyanLight
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                recommendation.isLocked -> AlertCrimson.copy(alpha = 0.2f)
                                recommendation.oomRisk == OomRisk.HIGH_THERMAL_RISK -> WarningAmber.copy(alpha = 0.2f)
                                else -> TelemetryEmerald.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            recommendation.isLocked -> "LOCKED (RAM)"
                            recommendation.oomRisk == OomRisk.HIGH_THERMAL_RISK -> "HEAVY LOAD"
                            else -> "READY TO RUN"
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            recommendation.isLocked -> AlertCrimson
                            recommendation.oomRisk == OomRisk.HIGH_THERMAL_RISK -> WarningAmber
                            else -> TelemetryEmerald
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time & VRAM metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberDark, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "EST. TIME / EPOCH",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = recommendation.timePerEpochDisplay,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.isLocked) AlertCrimson else TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberDark, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "RAM NEEDED / AVAIL",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = "%.1f GB / %.1f GB".format(recommendation.estimatedVramGb, deviceProfile.availableRamGb),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.isLocked) AlertCrimson else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feasibility Score bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hardware Feasibility on Your Phone:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${recommendation.feasibilityScorePct}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        recommendation.feasibilityScorePct >= 70 -> TelemetryEmerald
                        recommendation.feasibilityScorePct >= 30 -> WarningAmber
                        else -> AlertCrimson
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { recommendation.feasibilityScorePct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    recommendation.feasibilityScorePct >= 70 -> TelemetryEmerald
                    recommendation.feasibilityScorePct >= 30 -> WarningAmber
                    else -> AlertCrimson
                },
                trackColor = CyberDark
            )

            // Warnings / Lock Explanation
            if (recommendation.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                recommendation.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AlertCrimson.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AlertCrimson,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = warning,
                            fontSize = 11.sp,
                            color = AlertCrimson,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Highlights
            if (recommendation.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                recommendation.highlights.forEach { hl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TelemetryEmerald.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Highlight",
                            tint = TelemetryEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = hl,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Open in Studio or Disabled
            Button(
                onClick = {
                    onNavigateToStudioWithConfig(
                        LoRAHyperparams(
                            modelScale = selectedScale,
                            quantFormat = selectedQuant
                        )
                    )
                },
                enabled = !recommendation.isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_training_studio_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = CyberDark,
                    disabledContainerColor = CyberCardBorder,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (recommendation.isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (recommendation.isLocked) "LOCKED (EXCEEDS DEVICE RAM)" else "CONFIGURE IN BITNET STUDIO",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Command Snippet
        CodeSnippetCard(
            title = "CLI DISPATCH SYNTAX",
            subtitle = "Customized for ${deviceProfile.deviceModel}",
            code = recommendation.recipeSnippet
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
