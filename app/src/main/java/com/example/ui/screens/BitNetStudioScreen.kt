package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.DatasetValidator
import com.example.data.engine.FineTuningEngine
import com.example.data.model.CheckpointItem
import com.example.data.model.EngineTrainingStatus
import com.example.ui.components.InlineExplainToggle
import com.example.ui.components.LossChartCanvas
import com.example.ui.components.SmartPresetSlider
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

@Composable
fun BitNetStudioScreen(
    engine: FineTuningEngine,
    modifier: Modifier = Modifier
) {
    val sessionState by engine.sessionState.collectAsState()
    val hyperparams by engine.hyperparams.collectAsState()
    val checkpoints by engine.checkpoints.collectAsState()
    val activeModelRepo by engine.activeModelRepo.collectAsState()
    val activeDatasetName by engine.activeDatasetName.collectAsState()

    var smartPresetLevel by remember { mutableIntStateOf(3) }
    var showConfigPanel by remember { mutableStateOf(false) }
    var selectedDatasetIndex by remember { mutableStateOf(0) }
    var showEvalInspector by remember { mutableStateOf(false) }

    val currentDataset = DatasetValidator.SAMPLE_DATASETS[selectedDatasetIndex]
    val datasetInspection = remember(selectedDatasetIndex, activeDatasetName) {
        DatasetValidator.validateDataset(
            docCount = hyperparams.documentCount,
            totalTokens = hyperparams.totalDatasetTokens
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PATH A // BITNET LORA STUDIO",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Model: $activeModelRepo · QVAC GPU Engine",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyanLight
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurfaceVariant)
                    .clickable { showConfigPanel = !showConfigPanel }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_hyperparams_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tune Hyperparameters",
                        tint = NeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showConfigPanel) "HIDE ADVANCED" else "ADVANCED TUNING",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. SMART PRESET SLIDER (FOR EVERYONE INCLUDING ZERO KNOWLEDGE)
        SmartPresetSlider(
            currentLevel = smartPresetLevel,
            onLevelChanged = { preset ->
                smartPresetLevel = preset.level
                engine.updateHyperparams(preset.hyperparams)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. ADVANCED HYPERPARAMETER TUNING PANEL (Collapsible with inline explanations)
        AnimatedVisibility(visible = showConfigPanel) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberDark)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "DETAILED HYPERPARAMETERS (WITH INLINE EXPLANATIONS)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyanLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rank selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LoRA Rank (r):", fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4, 8, 16).forEach { r ->
                            val isSel = hyperparams.rank == r
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) NeonCyan else CyberSurfaceVariant)
                                    .clickable { engine.updateHyperparams(hyperparams.copy(rank = r)) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "r=$r",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) CyberDark else TextPrimary
                                )
                            }
                        }
                    }
                }
                InlineExplainToggle(conceptKey = "LORA_RANK", modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Alpha selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LoRA Alpha (α):", fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(8, 16, 32).forEach { a ->
                            val isSel = hyperparams.alpha == a
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) NeonCyan else CyberSurfaceVariant)
                                    .clickable { engine.updateHyperparams(hyperparams.copy(alpha = a)) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "α=$a",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) CyberDark else TextPrimary
                                )
                            }
                        }
                    }
                }
                InlineExplainToggle(conceptKey = "LORA_ALPHA", modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Batch Size selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Batch Size (-b / -ub):", fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(64, 128, 256).forEach { b ->
                            val isSel = hyperparams.batchSize == b
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) NeonCyan else CyberSurfaceVariant)
                                    .clickable { engine.updateHyperparams(hyperparams.copy(batchSize = b, microBatch = b)) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$b",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) CyberDark else TextPrimary
                                )
                            }
                        }
                    }
                }
                InlineExplainToggle(conceptKey = "BATCH_SIZE", modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic GPU Tiling toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Adreno Dynamic Tiling", fontSize = 12.sp, color = TextPrimary)
                        Text(text = "QVAC auto-bandwidth tuning", fontSize = 10.sp, color = TextMuted)
                    }
                    Switch(
                        checked = hyperparams.dynamicTiling,
                        onCheckedChange = { engine.updateHyperparams(hyperparams.copy(dynamicTiling = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberDark,
                            checkedTrackColor = TelemetryEmerald
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. DATASET QUALITY GATE CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(
                    1.dp,
                    if (datasetInspection.isClean) TelemetryEmerald.copy(alpha = 0.5f) else AlertCrimson,
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (datasetInspection.isClean) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (datasetInspection.isClean) TelemetryEmerald else AlertCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DATASET QUALITY GATE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (datasetInspection.isClean) TelemetryEmerald else AlertCrimson
                    )
                }

                Text(
                    text = if (datasetInspection.isClean) "GATE PASSED" else "GATE REJECTED",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (datasetInspection.isClean) TelemetryEmerald else AlertCrimson
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dataset Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DatasetValidator.SAMPLE_DATASETS.forEachIndexed { index, pair ->
                    val isSel = selectedDatasetIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) CyberSurfaceVariant else CyberDark)
                            .border(
                                1.dp,
                                if (isSel) NeonCyan else CyberCardBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedDatasetIndex = index }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = pair.first,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSel) NeonCyanLight else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row: Tokens / Cap / Split / Dedup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tokens / Budget:", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${datasetInspection.totalTokens} / 50k tokens",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (datasetInspection.isUnderCap) TextPrimary else AlertCrimson
                    )
                }
                Column {
                    Text(text = "Stratified Split:", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${datasetInspection.trainSplitPct}/${datasetInspection.valSplitPct}/${datasetInspection.testSplitPct}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column {
                    Text(text = "Dedup Rate:", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${datasetInspection.dedupScorePct}%",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryEmerald
                    )
                }
            }

            if (datasetInspection.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                datasetInspection.warnings.forEach { w ->
                    Text(
                        text = "• $w",
                        fontSize = 10.sp,
                        color = AlertCrimson,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            InlineExplainToggle(conceptKey = "DATASET_CAP")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. LIVE TELEMETRY STATUS BANNER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberDark)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ${sessionState.status.name}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (sessionState.status) {
                        EngineTrainingStatus.TRAINING -> TelemetryEmerald
                        EngineTrainingStatus.PAUSED_BATTERY -> AlertCrimson
                        EngineTrainingStatus.PAUSED_THERMAL -> WarningAmber
                        EngineTrainingStatus.COMPLETED -> NeonCyan
                        else -> TextSecondary
                    }
                )

                Text(
                    text = "EPOCH ${sessionState.currentEpoch} / ${sessionState.totalEpochs}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyanLight
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val stepFraction = if (sessionState.totalStepsPerEpoch > 0) {
                sessionState.currentStep.toFloat() / sessionState.totalStepsPerEpoch
            } else 0f

            LinearProgressIndicator(
                progress = { stepFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (sessionState.status) {
                    EngineTrainingStatus.PAUSED_BATTERY -> AlertCrimson
                    EngineTrainingStatus.PAUSED_THERMAL -> WarningAmber
                    else -> TelemetryEmerald
                },
                trackColor = CyberSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Step ${sessionState.currentStep} / ${sessionState.totalStepsPerEpoch}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Text(
                    text = "${sessionState.tokensProcessed} tokens (${sessionState.tokensPerSecond} tok/s)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = sessionState.statusMessage,
                fontSize = 11.sp,
                color = if (sessionState.status == EngineTrainingStatus.PAUSED_BATTERY || sessionState.status == EngineTrainingStatus.PAUSED_THERMAL) AlertCrimson else TextSecondary,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. LIVE LOSS CHART & EXPLANATION
        LossChartCanvas(
            lossHistory = sessionState.lossHistory,
            currentLoss = sessionState.currentLoss
        )
        InlineExplainToggle(conceptKey = "LOSS_METRIC", modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(14.dp))

        // 6. PRIMARY INTERACTIVE CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (sessionState.status) {
                EngineTrainingStatus.TRAINING -> {
                    Button(
                        onClick = { engine.pauseTraining(EngineTrainingStatus.PAUSED_USER) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pause_training_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningAmber,
                            contentColor = CyberDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PAUSE", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                EngineTrainingStatus.PAUSED_USER,
                EngineTrainingStatus.PAUSED_BATTERY,
                EngineTrainingStatus.PAUSED_THERMAL -> {
                    Button(
                        onClick = { engine.resumeTraining() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("resume_training_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TelemetryEmerald,
                            contentColor = CyberDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "RESUME", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = { engine.startTraining() },
                        enabled = datasetInspection.isClean,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_training_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = CyberDark,
                            disabledContainerColor = CyberCardBorder,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "START FINE-TUNING", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedButton(
                onClick = { engine.saveCheckpoint() },
                modifier = Modifier.testTag("save_checkpoint_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanLight)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "SAVE CKPT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7. SIMULATION TRIGGERS FOR BATTERY & THERMAL WITH EXPLANATIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberDark)
                    .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable { engine.triggerThermalSlowdownSimulation() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeviceThermostat, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "TEST THERMAL (>1.5×)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = WarningAmber, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberDark)
                    .border(1.dp, AlertCrimson.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable {
                        val isLow = sessionState.batteryPct < 30
                        engine.simulateBatteryLevel(if (isLow) 85 else 24, charging = false)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = AlertCrimson, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sessionState.batteryPct < 30) "RESTORE BATTERY" else "TEST BATTERY (<30%)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AlertCrimson,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                InlineExplainToggle(conceptKey = "THERMAL_COOLDOWN")
            }
            Box(modifier = Modifier.weight(1f)) {
                InlineExplainToggle(conceptKey = "BATTERY_GUARD")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. CHECKPOINTS LIST
        if (checkpoints.isNotEmpty()) {
            Text(
                text = "SAVED CHECKPOINTS (${checkpoints.size})",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                checkpoints.take(3).forEach { ckpt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurface)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${ckpt.id} · Epoch ${ckpt.epoch} (Step ${ckpt.step})",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Loss: %.3f · %s · Full Optimizer State".format(ckpt.loss, ckpt.timestamp),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberSurfaceVariant)
                                .clickable { engine.restoreCheckpoint(ckpt) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "RESTORE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 9. EVAL QUALITY CHECK INSPECTOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRAINED VS UNTRAINED EVAL CHECK",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = NeonCyanLight
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberSurfaceVariant)
                    .clickable { showEvalInspector = !showEvalInspector }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (showEvalInspector) "COLLAPSE" else "INSPECT COMPILATION",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        InlineExplainToggle(conceptKey = "PERPLEXITY_METRIC", modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(6.dp))

        AnimatedVisibility(visible = showEvalInspector) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                engine.getEvalComparisons().forEach { pair ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberDark)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "PROMPT: \"${pair.prompt}\"",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Baseline output
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberSurface, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "BASELINE (FROZEN TERNARY):", fontSize = 10.sp, color = TextMuted)
                                Text(text = "PPL: ${pair.baselinePerplexity}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                            }
                            Text(text = pair.baselineOutput, fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Finetuned output
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TelemetryEmerald.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .border(1.dp, TelemetryEmerald.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "BITNET LORA ADAPTED (RANK 8):", fontSize = 10.sp, color = TelemetryEmerald, fontWeight = FontWeight.Bold)
                                Text(text = "PPL: ${pair.finetunedPerplexity} (-77%)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TelemetryEmerald, fontWeight = FontWeight.Bold)
                            }
                            Text(text = pair.finetunedOutput, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
