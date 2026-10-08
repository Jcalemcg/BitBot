package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrainingPathId
import com.example.data.repository.BenchmarkData
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

@Composable
fun RecipesScreen(
    modifier: Modifier = Modifier
) {
    var selectedPathId by remember { mutableStateOf(TrainingPathId.PATH_A) }

    val activePathInfo = remember(selectedPathId) {
        BenchmarkData.TRAINING_PATHS.first { it.id == selectedPathId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "DISPATCH PATHS & TECHNICAL RECIPES",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Production specifications, code patterns, constraints, and negative boundaries for Paths A–E.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Path Tabs Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BenchmarkData.TRAINING_PATHS.forEach { path ->
                val isSel = path.id == selectedPathId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) CyberSurfaceVariant else CyberSurface)
                        .border(
                            1.dp,
                            if (isSel) NeonCyan else CyberCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPathId = path.id }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = path.id.name.replace("_", " "),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) NeonCyanLight else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Path Detail Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text(
                text = activePathInfo.title,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = activePathInfo.subtitle,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = NeonCyanLight,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f).background(CyberDark, RoundedCornerShape(6.dp)).padding(8.dp)) {
                    Text(text = "TARGET CLASS:", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = activePathInfo.modelClass, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f).background(CyberDark, RoundedCornerShape(6.dp)).padding(8.dp)) {
                    Text(text = "WHERE TRAINING RUNS:", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = activePathInfo.executionLocation, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f).background(CyberDark, RoundedCornerShape(6.dp)).padding(8.dp)) {
                    Text(text = "MATURITY:", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = activePathInfo.maturity, fontSize = 11.sp, color = TelemetryEmerald, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f).background(CyberDark, RoundedCornerShape(6.dp)).padding(8.dp)) {
                    Text(text = "DEVICE SUPPORT:", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = activePathInfo.deviceSupport, fontSize = 11.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = activePathInfo.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "CONSTRAINTS & POLICIES:",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = WarningAmber
            )
            Spacer(modifier = Modifier.height(4.dp))
            activePathInfo.constraints.forEach { c ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "• ", color = WarningAmber, fontSize = 11.sp)
                    Text(text = c, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Code Recipe according to Path
        when (selectedPathId) {
            TrainingPathId.PATH_A -> {
                CodeSnippetCard(
                    title = "RECIPE: BITNET ON-DEVICE LORA",
                    subtitle = "llama-finetune-lora via QVAC Fabric",
                    code = """
./llama-finetune-lora \
  -m models/bitnet-1b.tq2_0.gguf \
  -f train.jsonl \
  --output-adapter bitnet-lora-adapter.gguf \
  -ngl 999 -c 128 -b 128 -ub 128 \
  --flash-attn off \
  --num-epochs 8
                    """.trimIndent()
                )
            }
            TrainingPathId.PATH_B -> {
                CodeSnippetCard(
                    title = "RECIPE: LITERT 4-SIGNATURE TRAINING",
                    subtitle = "TensorFlow conversion with resource variables",
                    code = """
# Author 4 @tf.function signatures in TensorFlow:
# 1. 'train', 2. 'infer', 3. 'save', 4. 'restore'
converter.experimental_enable_resource_variables = True
tflite_model = converter.convert()

// In Kotlin/Android:
val interpreter = Interpreter(modelBuffer, options)
interpreter.runSignature(trainInputs, "train")
// Save/restore signatures map to pause/resume checkpoints
interpreter.runSignature(checkpointInputs, "save")
                    """.trimIndent()
                )
            }
            TrainingPathId.PATH_C -> {
                CodeSnippetCard(
                    title = "RECIPE: CORE ML MLUPDATETASK",
                    subtitle = "Strict format constraint: 'neuralnetwork' only",
                    code = """
// Python model export:
builder = NeuralNetworkBuilder(spec)
builder.set_updatable_layers(['fc1', 'fc2'])
// CRITICAL: Must export as .neuralnetwork (mlprogram is NOT updatable)

// Swift iOS runtime:
let updateTask = try MLUpdateTask(
    forModelAt: compiledModelURL,
    trainingData: batchProvider,
    configuration: config,
    completionHandler: { context in
        try context.model.write(to: sandboxModelURL)
    }
)
updateTask.resume()
                    """.trimIndent()
                )
            }
            TrainingPathId.PATH_D -> {
                CodeSnippetCard(
                    title = "RECIPE: OFF-DEVICE PEFT → LITERT-LM",
                    subtitle = "Attention LoRA conversion & loading",
                    code = """
# 1. Train off-device via HF PEFT (q_proj, v_proj)
# 2. Convert adapter via MediaPipe / LiteRT-LM converter:
python -m mediapipe.tasks.python.genai.converter \
  --lora_ckpt adapter.bin \
  --lora_rank 8 \
  --backend 'gpu'

# 3. Load on-device in LiteRT-LM (Note: loraPath is immutable!):
val session = LlmInference.create(
    LlmInferenceOptions.builder()
        .setModelPath("gemma-2-2b.bin")
        .setLoraPath("adapter_gpu.bin")
        .build()
)
                    """.trimIndent()
                )
            }
            TrainingPathId.PATH_E -> {
                CodeSnippetCard(
                    title = "RECIPE: EXECUTORCH PREVIEW",
                    subtitle = "Experimental SGD training extension",
                    code = """
// Flag-gated experimental preview
// Author verified LoRA extension on mobile PyTorch
// Limited to standard SGD optimizer updates
val executor = ExecuTorchModule.load("model_with_train_ops.pte")
executor.forwardTraining(batchInputs, optimizer = SGD(lr = 1e-4))
                    """.trimIndent()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Negative Boundaries Card ("What We Deliberately Do Not Claim")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AlertCrimson.copy(alpha = 0.08f))
                .border(1.dp, AlertCrimson.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WHAT WE DELIBERATELY DO NOT CLAIM",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AlertCrimson
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BenchmarkData.NEGATIVE_CLAIMS.forEach { claim ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "✕ ", color = AlertCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = claim,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
