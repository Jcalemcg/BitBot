package com.example.data.engine

import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.model.TargetChipset
import com.example.data.model.TrainingPathId
import com.example.data.repository.BenchmarkData

enum class OomRisk {
    SAFE,
    MANAGEABLE,
    HIGH_THERMAL_RISK,
    FATAL_OOM
}

data class BackendRecommendation(
    val recommendedPath: TrainingPathId,
    val pathTitle: String,
    val timePerEpochDisplay: String,
    val timePerEpochSeconds: Long?,
    val estimatedVramGb: Float,
    val oomRisk: OomRisk,
    val feasibilityScorePct: Int,
    val isSweetSpot: Boolean,
    val warnings: List<String>,
    val highlights: List<String>,
    val recipeSnippet: String
)

object BackendSelectorEngine {

    fun evaluate(
        modelScale: ModelScale,
        quantFormat: QuantFormat,
        chipset: TargetChipset,
        isCustomClassifier: Boolean = false,
        requiresCloudPeft: Boolean = false
    ): BackendRecommendation {
        if (isCustomClassifier) {
            val path = if (chipset == TargetChipset.IPHONE_16_A18) TrainingPathId.PATH_C else TrainingPathId.PATH_B
            return BackendRecommendation(
                recommendedPath = path,
                pathTitle = if (path == TrainingPathId.PATH_B) "Path B: LiteRT Signature Training" else "Path C: Core ML MLUpdateTask",
                timePerEpochDisplay = "< 45 seconds",
                timePerEpochSeconds = 45L,
                estimatedVramGb = 0.05f,
                oomRisk = OomRisk.SAFE,
                feasibilityScorePct = 98,
                isSweetSpot = true,
                warnings = if (path == TrainingPathId.PATH_C) listOf(
                    "Format Constraint: Model MUST be 'neuralnetwork' format. 'mlprogram' cannot be trained on-device."
                ) else listOf(
                    "Limited to small models (≤ ~1M parameters). No LoRA support."
                ),
                highlights = listOf("Zero GPU thermal pressure", "Fast local CPU adaptation"),
                recipeSnippet = "Interpreter.runSignature(inputs, \"train\")"
            )
        }

        if (requiresCloudPeft || modelScale == ModelScale.SCALE_13B) {
            return BackendRecommendation(
                recommendedPath = TrainingPathId.PATH_D,
                pathTitle = "Path D: Off-Device PEFT → On-Device Adapter",
                timePerEpochDisplay = "~15 min (Cloud H100) / On-device loading: 2.1s",
                timePerEpochSeconds = 900L,
                estimatedVramGb = 0.8f,
                oomRisk = OomRisk.SAFE,
                feasibilityScorePct = 92,
                isSweetSpot = false,
                warnings = listOf(
                    "Ships customer training data off-device (requires explicit user consent).",
                    "LiteRT-LM: loraPath is immutable after session creation (adapter switching needs session restart)."
                ),
                highlights = listOf("Works for standard HF models (Gemma-2 2B, Phi-2)", "Zero mobile GPU thermal strain"),
                recipeSnippet = "ct.utils.MultiFunctionDescriptor + LiteRT-LM --lora_ckpt adapter.bin"
            )
        }

        // Path A: BitNet On-Device LoRA (QVAC Fabric)
        val benchmark = BenchmarkData.BENCHMARK_TABLE.firstOrNull { it.scale == modelScale }
        val (timeStr, seconds, isOom) = when (chipset) {
            TargetChipset.SAMSUNG_S25_ADRENO830 -> Triple(
                benchmark?.s25Display ?: "N/A",
                benchmark?.s25Seconds,
                benchmark?.s25Oom ?: false
            )
            TargetChipset.PIXEL_9_MALI -> Triple(
                benchmark?.pixel9Display ?: "N/A",
                benchmark?.pixel9Seconds,
                benchmark?.pixel9Oom ?: false
            )
            TargetChipset.IPHONE_16_A18 -> Triple(
                benchmark?.iphone16Display ?: "N/A",
                benchmark?.iphone16Seconds,
                benchmark?.iphone16Oom ?: false
            )
        }

        val estimatedVram = when (quantFormat) {
            QuantFormat.TQ1_0 -> modelScale.tq1SizeGb + 0.6f
            QuantFormat.TQ2_0 -> modelScale.tq2SizeGb + 0.9f
        }

        val oomRisk = when {
            isOom -> OomRisk.FATAL_OOM
            modelScale == ModelScale.SCALE_7B -> OomRisk.HIGH_THERMAL_RISK
            modelScale == ModelScale.SCALE_2_7B -> OomRisk.MANAGEABLE
            else -> OomRisk.SAFE
        }

        val feasibilityScore = when (oomRisk) {
            OomRisk.SAFE -> 95
            OomRisk.MANAGEABLE -> 72
            OomRisk.HIGH_THERMAL_RISK -> 35
            OomRisk.FATAL_OOM -> 0
        }

        val warnings = mutableListOf<String>()
        val highlights = mutableListOf<String>()

        if (isOom) {
            warnings.add("CRITICAL OOM: ${modelScale.label} exceeds physical unified memory on ${chipset.displayName}.")
            warnings.add("Recommendation: Switch to ≤ 1B model or deploy via Path D (Off-device PEFT).")
        } else {
            if (modelScale.sweetSpot) {
                highlights.add("Sweet spot for handheld fine-tuning: practical epoch times under battery operation.")
            }
            if (chipset == TargetChipset.SAMSUNG_S25_ADRENO830) {
                highlights.add("Adreno 830 GPU achieves up to 11× speedup over CPU execution.")
                highlights.add("Dynamic tiling is enabled by QVAC Fabric for peak memory bandwidth.")
            } else if (chipset == TargetChipset.PIXEL_9_MALI) {
                warnings.add("Mali-G715 GPU has higher latency than Adreno 830 (e.g. 1B model takes ~2h 08m vs 1h 18m).")
            } else if (chipset == TargetChipset.IPHONE_16_A18) {
                warnings.add("iOS suspends background GPU tasks — keep application foregrounded during active epochs.")
            }

            if (quantFormat == QuantFormat.TQ2_0) {
                highlights.add("TQ2_0 chosen: High numerical stability for ternary gradient descent.")
            } else {
                highlights.add("TQ1_0 chosen: Minimum RAM footprint (~1.6 bits/weight).")
            }
        }

        val recipe = """
./llama-finetune-lora \
  -m models/bitnet-${modelScale.label.lowercase()}.${quantFormat.name.lowercase().substring(0, 4)}.gguf \
  -f train.jsonl \
  --output-adapter bitnet-lora-adapter.gguf \
  -ngl 999 -c 128 -b 128 -ub 128 \
  --flash-attn off \
  --num-epochs 8
        """.trimIndent()

        return BackendRecommendation(
            recommendedPath = if (isOom) TrainingPathId.PATH_D else TrainingPathId.PATH_A,
            pathTitle = if (isOom) "Path D: Off-device PEFT (Fallback due to OOM)" else "Path A: BitNet On-Device LoRA",
            timePerEpochDisplay = timeStr,
            timePerEpochSeconds = seconds,
            estimatedVramGb = estimatedVram,
            oomRisk = oomRisk,
            feasibilityScorePct = feasibilityScore,
            isSweetSpot = modelScale.sweetSpot && !isOom,
            warnings = warnings,
            highlights = highlights,
            recipeSnippet = recipe
        )
    }
}
