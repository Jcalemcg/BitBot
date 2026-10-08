package com.example.data.model

enum class TrainingPathId {
    PATH_A,
    PATH_B,
    PATH_C,
    PATH_D,
    PATH_E
}

data class TrainingPathInfo(
    val id: TrainingPathId,
    val title: String,
    val subtitle: String,
    val modelClass: String,
    val executionLocation: String,
    val deviceSupport: String,
    val maturity: String,
    val isRecommendedForDevice: Boolean = false,
    val summary: String,
    val constraints: List<String>,
    val negativeBoundaries: List<String> = emptyList()
)

enum class ModelScale(
    val label: String,
    val paramCountMillions: Int,
    val tq1SizeGb: Float,
    val tq2SizeGb: Float,
    val sweetSpot: Boolean = false
) {
    SCALE_125M("125M", 125, 0.05f, 0.09f, true),
    SCALE_350M("350M", 350, 0.12f, 0.23f, true),
    SCALE_1B("1B", 1000, 0.32f, 0.65f, true),
    SCALE_2_7B("2.7B", 2700, 0.85f, 1.75f, false),
    SCALE_7B("7B", 7000, 1.9f, 4.3f, false),
    SCALE_13B("13B", 13000, 3.4f, 7.8f, false)
}

enum class QuantFormat(val displayName: String, val bitsPerWeight: Float, val description: String) {
    TQ2_0("TQ2_0 (Default)", 2.0f, "Numerically stable for gradient descent during fine-tuning"),
    TQ1_0("TQ1_0 (Inference)", 1.58f, "Extreme memory compression (~1.6 bits/weight) for deployment")
}

enum class TargetChipset(val displayName: String, val gpuName: String, val platform: String) {
    SAMSUNG_S25_ADRENO830("Samsung Galaxy S25", "Adreno 830 (Vulkan)", "Android"),
    PIXEL_9_MALI("Google Pixel 9", "Mali-G715 (Vulkan)", "Android"),
    IPHONE_16_A18("Apple iPhone 16", "Apple A18 GPU (Metal)", "iOS")
}

data class BenchmarkItem(
    val scale: ModelScale,
    val s25Display: String,
    val s25Seconds: Long?,
    val s25Oom: Boolean,
    val pixel9Display: String,
    val pixel9Seconds: Long?,
    val pixel9Oom: Boolean,
    val iphone16Display: String,
    val iphone16Seconds: Long?,
    val iphone16Oom: Boolean,
    val sweetSpotNote: String? = null
)

data class LoRAHyperparams(
    val modelScale: ModelScale = ModelScale.SCALE_1B,
    val quantFormat: QuantFormat = QuantFormat.TQ2_0,
    val rank: Int = 8,
    val alpha: Int = 16,
    val batchSize: Int = 128,
    val microBatch: Int = 128,
    val contextWindow: Int = 128,
    val epochs: Int = 8,
    val dynamicTiling: Boolean = true,
    val maskedLossAssistant: Boolean = true,
    val totalDatasetTokens: Int = 18000,
    val documentCount: Int = 300
)

enum class EngineTrainingStatus {
    IDLE,
    VALIDATING,
    TRAINING,
    PAUSED_USER,
    PAUSED_BATTERY,
    PAUSED_THERMAL,
    COMPLETED
}

data class StepTelemetry(
    val step: Int,
    val epoch: Int,
    val loss: Float,
    val epochProgress: Float,
    val timestampMs: Long
)

data class TrainingSessionState(
    val status: EngineTrainingStatus = EngineTrainingStatus.IDLE,
    val currentEpoch: Int = 1,
    val totalEpochs: Int = 8,
    val currentStep: Int = 0,
    val totalStepsPerEpoch: Int = 140,
    val currentLoss: Float = 3.42f,
    val initialLoss: Float = 3.42f,
    val lossHistory: List<StepTelemetry> = emptyList(),
    val epochElapsedSec: Long = 0L,
    val estimatedEpochTotalSec: Long = 4680L, // 1h 18m for S25 1B
    val thermalTempC: Float = 32.5f,
    val thermalSlowdownRatio: Float = 1.0f,
    val consecutiveSlowEpochs: Int = 0,
    val batteryPct: Int = 85,
    val isCharging: Boolean = false,
    val batteryThresholdPct: Int = 30,
    val tokensProcessed: Int = 0,
    val tokensPerSecond: Float = 28.5f,
    val activeCheckpointCount: Int = 0,
    val statusMessage: String = "Ready to start BitNet LoRA fine-tuning"
)

data class CheckpointItem(
    val id: String,
    val epoch: Int,
    val step: Int,
    val loss: Float,
    val timestamp: String,
    val optimizerStateSaved: Boolean = true,
    val adapterFormat: String = "FP16 GGUF"
)

data class DatasetInspection(
    val documentCount: Int = 300,
    val totalTokens: Int = 18240,
    val maxAllowedTokens: Int = 50000,
    val trainSplitPct: Int = 80,
    val valSplitPct: Int = 10,
    val testSplitPct: Int = 10,
    val dedupScorePct: Float = 99.6f,
    val isUnderCap: Boolean = true,
    val isStratified: Boolean = true,
    val isClean: Boolean = true,
    val warnings: List<String> = emptyList()
)

data class EvalPair(
    val prompt: String,
    val baselineOutput: String,
    val finetunedOutput: String,
    val baselinePerplexity: Float,
    val finetunedPerplexity: Float
)
