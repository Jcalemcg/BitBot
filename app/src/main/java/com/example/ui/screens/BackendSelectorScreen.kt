package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.TargetChipset
import com.example.ui.components.CodeSnippetCard
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
    onNavigateToStudioWithConfig: (LoRAHyperparams) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScale by remember { mutableStateOf(ModelScale.SCALE_1B) }
    var selectedQuant by remember { mutableStateOf(QuantFormat.TQ2_0) }
    var selectedChipset by remember { mutableStateOf(TargetChipset.SAMSUNG_S25_ADRENO830) }
    var isClassifierMode by remember { mutableStateOf(false) }

    val recommendation = remember(selectedScale, selectedQuant, selectedChipset, isClassifierMode) {
        BackendSelectorEngine.evaluate(
            modelScale = selectedScale,
            quantFormat = selectedQuant,
            chipset = selectedChipset,
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
            text = "HARDWARE & BACKEND SELECTOR",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Evaluate model class, parameters, and memory feasibility against verified mobile chipsets.",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // 1. Workload Type Selector
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
                    text = "Classifier ≤ 1M (Path B/C)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isClassifierMode) NeonCyan else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Model Scale (if LLM mode)
        if (!isClassifierMode) {
            Text(
                text = "SELECT MODEL SCALE:",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModelScale.entries.forEach { scale ->
                    val isSelected = selectedScale == scale
                    val isSweetSpot = scale.sweetSpot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberSurfaceVariant else CyberSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) NeonCyan else CyberCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedScale = scale }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("scale_chip_${scale.label}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = scale.label,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
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

            Spacer(modifier = Modifier.height(14.dp))

            // Quantization Format Selector
            Text(
                text = "QUANTIZATION FORMAT:",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Target Chipset Selector
        Text(
            text = "TARGET HARDWARE PROFILE:",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TargetChipset.entries.forEach { chipset ->
                val isSelected = selectedChipset == chipset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberSurfaceVariant else CyberSurface)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else CyberCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedChipset = chipset }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = chipset.displayName,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                        Text(
                            text = "${chipset.gpuName} · ${chipset.platform}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Recommendation Result Card
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
                        text = "RECOMMENDED DISPATCH",
                        fontSize = 11.sp,
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
                            when (recommendation.oomRisk) {
                                OomRisk.FATAL_OOM -> AlertCrimson.copy(alpha = 0.2f)
                                OomRisk.HIGH_THERMAL_RISK -> WarningAmber.copy(alpha = 0.2f)
                                else -> TelemetryEmerald.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (recommendation.oomRisk) {
                            OomRisk.FATAL_OOM -> "OOM BLOCK"
                            OomRisk.HIGH_THERMAL_RISK -> "THERMAL RISK"
                            OomRisk.MANAGEABLE -> "HIGH LOAD"
                            OomRisk.SAFE -> "SWEET SPOT"
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = when (recommendation.oomRisk) {
                            OomRisk.FATAL_OOM -> AlertCrimson
                            OomRisk.HIGH_THERMAL_RISK -> WarningAmber
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
                            text = "TIME / EPOCH",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = recommendation.timePerEpochDisplay,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.oomRisk == OomRisk.FATAL_OOM) AlertCrimson else TextPrimary
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
                            text = "EST. VRAM FOOTPRINT",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = "%.2f GB".format(recommendation.estimatedVramGb),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
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
                    text = "Hardware Feasibility Score:",
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

            // Warnings
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

            // Action Button: Open in Studio
            Button(
                onClick = {
                    onNavigateToStudioWithConfig(
                        LoRAHyperparams(
                            modelScale = selectedScale,
                            quantFormat = selectedQuant
                        )
                    )
                },
                enabled = recommendation.oomRisk != OomRisk.FATAL_OOM,
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
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (recommendation.oomRisk == OomRisk.FATAL_OOM) "CANNOT DISPATCH (OOM)" else "CONFIGURE IN BITNET STUDIO",
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
            subtitle = "Direct llama-finetune-lora parameters",
            code = recommendation.recipeSnippet
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
