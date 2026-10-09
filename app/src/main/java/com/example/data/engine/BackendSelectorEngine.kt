package com.example.data.engine

import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.model.TrainingPathId
import com.example.data.scanner.DeviceHardwareProfile
import com.example.data.scanner.DeviceTier

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
    val isLocked: Boolean,
    val lockReason: String?,
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
        deviceProfile: DeviceHardwareProfile,
        isCustomClassifier: Boolean = false,
        requiresCloudPeft: Boolean = false
    ): BackendRecommendation {
        if (isCustomClassifier) {
            return BackendRecommendation(
                recommendedPath = TrainingPathId.PATH_B,
                pathTitle = "Path B: LiteRT Signature Training",
                timePerEpochDisplay = "< 30 seconds on ${deviceProfile.deviceModel}",
                timePerEpochSeconds = 30L,
                estimatedVramGb = 0.05f,
                oomRisk = OomRisk.SAFE,
                isLocked = false,
                lockReason = null,
                feasibilityScorePct = 99,
                isSweetSpot = true,
                warnings = listOf(
                    "Designed for models measured in kilobytes-to-megabytes (≤ ~1M params). No LLM LoRA support."
                ),
                highlights = listOf(
                    "Zero graphics pressure on ${deviceProfile.deviceModel}",
                    "Runs directly on CPU cores (${deviceProfile.cpuCores} active cores)"
                ),
                recipeSnippet = "Interpreter.runSignature(inputs, \"train\")"
            )
        }

        if (requiresCloudPeft) {
            return BackendRecommendation(
                recommendedPath = TrainingPathId.PATH_D,
                pathTitle = "Path D: Off-Device PEFT → On-Device Adapter",
                timePerEpochDisplay = "~12 min (Cloud) / On-device load: 1.8s",
                timePerEpochSeconds = 720L,
                estimatedVramGb = 0.6f,
                oomRisk = OomRisk.SAFE,
                isLocked = false,
                lockReason = null,
                feasibilityScorePct = 95,
                isSweetSpot = false,
                warnings = listOf(
                    "Requires user consent to transfer training data off-device.",
                    "LiteRT-LM: loraPath is immutable after session creation."
                ),
                highlights = listOf(
                    "Compatible with standard HF models (Gemma-2 2B, Phi-2)",
                    "Safest option: preserves phone battery and prevents all thermal load"
                ),
                recipeSnippet = "python -m mediapipe.tasks.python.genai.converter --lora_ckpt adapter.bin --backend 'gpu'"
            )
        }

        // Check if model fits in user's scanned hardware
        val (canRun, lockReason) = deviceProfile.canRunModel(modelScale, quantFormat)

        val estimatedVram = when (quantFormat) {
            QuantFormat.TQ1_0 -> modelScale.tq1SizeGb + 0.6f
            QuantFormat.TQ2_0 -> modelScale.tq2SizeGb + 0.9f
        }

        val seconds = if (canRun) deviceProfile.estimateSecondsForModel(modelScale) else null
        val timeDisplay = if (canRun && seconds != null) {
            if (seconds >= 3600) {
                val h = seconds / 3600
                val m = (seconds % 3600) / 60
                "${h}h ${m}m"
            } else {
                val m = seconds / 60
                val s = seconds % 60
                "${m}m ${s}s"
            }
        } else {
            "OOM (Cannot run)"
        }

        val oomRisk = when {
            !canRun -> OomRisk.FATAL_OOM
            modelScale == ModelScale.SCALE_2_7B -> OomRisk.HIGH_THERMAL_RISK
            modelScale == ModelScale.SCALE_1B -> if (deviceProfile.tier == DeviceTier.TIER_C) OomRisk.HIGH_THERMAL_RISK else OomRisk.SAFE
            else -> OomRisk.SAFE
        }

        val feasibilityScore = when {
            !canRun -> 0
            modelScale == ModelScale.SCALE_2_7B -> 45
            modelScale == ModelScale.SCALE_1B -> 92
            else -> 98
        }

        val isSweet = modelScale == ModelScale.SCALE_1B && canRun && (deviceProfile.tier == DeviceTier.TIER_S || deviceProfile.tier == DeviceTier.TIER_A)

        val warnings = mutableListOf<String>()
        val highlights = mutableListOf<String>()

        if (!canRun) {
            warnings.add("MEMORY LIMIT: $lockReason")
            warnings.add("This model cannot be fine-tuned directly on ${deviceProfile.deviceModel}. Please choose a smaller model (≤ 1B) or use Path D (Cloud PEFT).")
        } else {
            if (isSweet) {
                highlights.add("Best-Case Sweet Spot: Perfect balance of intelligence and battery efficiency for your hardware.")
            }
            if (deviceProfile.hasVulkanSupport) {
                highlights.add("GPU acceleration active: Vulkan hardware backend will accelerate matrix operations.")
            } else {
                warnings.add("No Vulkan GPU detected: Training will fall back to CPU emulation (slower epoch times).")
            }
            if (quantFormat == QuantFormat.TQ2_0) {
                highlights.add("TQ2_0 format: High numerical stability for gradient calculations.")
            } else {
                highlights.add("TQ1_0 format: Extreme ~1.6 bits/weight memory compression.")
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
            recommendedPath = if (!canRun) TrainingPathId.PATH_D else TrainingPathId.PATH_A,
            pathTitle = if (!canRun) "Path D: Off-Device PEFT (Exceeds Device RAM)" else "Path A: BitNet On-Device LoRA",
            timePerEpochDisplay = timeDisplay,
            timePerEpochSeconds = seconds,
            estimatedVramGb = estimatedVram,
            oomRisk = oomRisk,
            isLocked = !canRun,
            lockReason = lockReason,
            feasibilityScorePct = feasibilityScore,
            isSweetSpot = isSweet,
            warnings = warnings,
            highlights = highlights,
            recipeSnippet = recipe
        )
    }
}
