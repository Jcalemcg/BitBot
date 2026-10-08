package com.example.data.repository

import com.example.data.model.BenchmarkItem
import com.example.data.model.ModelScale
import com.example.data.model.TrainingPathId
import com.example.data.model.TrainingPathInfo

object BenchmarkData {
    val BENCHMARK_TABLE: List<BenchmarkItem> = listOf(
        BenchmarkItem(
            scale = ModelScale.SCALE_125M,
            s25Display = "10m 10s",
            s25Seconds = 610L,
            s25Oom = false,
            pixel9Display = "17m 25s",
            pixel9Seconds = 1045L,
            pixel9Oom = false,
            iphone16Display = "12m 24s",
            iphone16Seconds = 744L,
            iphone16Oom = false,
            sweetSpotNote = "Ultra-fast iteration; ideal for on-device classifiers"
        ),
        BenchmarkItem(
            scale = ModelScale.SCALE_350M,
            s25Display = "28m 40s",
            s25Seconds = 1720L,
            s25Oom = false,
            pixel9Display = "50m 52s",
            pixel9Seconds = 3052L,
            pixel9Oom = false,
            iphone16Display = "44m 34s",
            iphone16Seconds = 2674L,
            iphone16Oom = false,
            sweetSpotNote = "Sub-30m per epoch on S25; solid task specialization"
        ),
        BenchmarkItem(
            scale = ModelScale.SCALE_1B,
            s25Display = "1h 18m",
            s25Seconds = 4680L,
            s25Oom = false,
            pixel9Display = "2h 08m",
            pixel9Seconds = 7680L,
            pixel9Oom = false,
            iphone16Display = "1h 45m",
            iphone16Seconds = 6300L,
            iphone16Oom = false,
            sweetSpotNote = "★ Phone-Class Sweet Spot: ~1.3h on S25 for ~18k tokens"
        ),
        BenchmarkItem(
            scale = ModelScale.SCALE_2_7B,
            s25Display = "3h 28m",
            s25Seconds = 12480L,
            s25Oom = false,
            pixel9Display = "5h 04m",
            pixel9Seconds = 18240L,
            pixel9Oom = false,
            iphone16Display = "4h 24m",
            iphone16Seconds = 15840L,
            iphone16Oom = false,
            sweetSpotNote = "Feasible overnight on charging stand; heavy thermal load"
        ),
        BenchmarkItem(
            scale = ModelScale.SCALE_7B,
            s25Display = "OOM ⚠",
            s25Seconds = null,
            s25Oom = true,
            pixel9Display = "13h 06m",
            pixel9Seconds = 47160L,
            pixel9Oom = false,
            iphone16Display = "12h 24m",
            iphone16Seconds = 44640L,
            iphone16Oom = false,
            sweetSpotNote = "Exceeds S25 VRAM (TQ2_0). Pixel 9 takes ~13h per epoch"
        ),
        BenchmarkItem(
            scale = ModelScale.SCALE_13B,
            s25Display = "OOM ⚠",
            s25Seconds = null,
            s25Oom = true,
            pixel9Display = "OOM ⚠",
            pixel9Seconds = null,
            pixel9Oom = true,
            iphone16Display = "23h 10m",
            iphone16Seconds = 83400L,
            iphone16Oom = false,
            sweetSpotNote = "Theoretical ceiling only; 23h/epoch on A18 is non-shipped"
        )
    )

    val TRAINING_PATHS: List<TrainingPathInfo> = listOf(
        TrainingPathInfo(
            id = TrainingPathId.PATH_A,
            title = "Path A: BitNet on-device LoRA",
            subtitle = "QVAC Fabric (llama.cpp fork)",
            modelClass = "BitNet b1.58 LLMs (125M – 2.7B practical)",
            executionLocation = "On-device phone GPU (Vulkan / Metal)",
            deviceSupport = "Galaxy S25 (Adreno 830), Pixel 9 (Mali), iPhone 16 (A18)",
            maturity = "Working (BitNet models only)",
            isRecommendedForDevice = true,
            summary = "Base weights remain ternary (1.58-bit) and frozen; rank 8/alpha 16 LoRA adapters train in FP16. Adreno GPU is up to 11× faster than CPU.",
            constraints = listOf(
                "Phone-class practical sweet spot: ≤ 1B (e.g. ~300 docs / ~18k tokens in ~1.3h)",
                "Models > 2.7B hit severe thermal limits or OOM on Android with TQ2_0",
                "TQ2_0 recommended for fine-tuning stability; convert to TQ1_0 for deployment",
                "Requires dynamic tiling enabled on Adreno/Mali for peak bandwidth"
            ),
            negativeBoundaries = listOf(
                "No mobile QLoRA (quantized adapter + quantized optimizer states)",
                "No full fine-tuning of LLMs due to activation/gradient RAM walls"
            )
        ),
        TrainingPathInfo(
            id = TrainingPathId.PATH_B,
            title = "Path B: LiteRT signature training",
            subtitle = "TensorFlow / LiteRT on-device adaptation",
            modelClass = "Small classifiers & regressors (≤ ~1M params)",
            executionLocation = "On-device, CPU (Android)",
            deviceSupport = "Android (Java/C++ API)",
            maturity = "Production",
            isRecommendedForDevice = false,
            summary = "Uses four @tf.function signatures: 'train', 'infer', 'save', 'restore' with experimental_enable_resource_variables=True.",
            constraints = listOf(
                "Designed for models measured in kilobytes-to-megabytes, not LLMs",
                "No LoRA support",
                "Android only (no official iOS on-device training API)"
            )
        ),
        TrainingPathInfo(
            id = TrainingPathId.PATH_C,
            title = "Path C: Core ML MLUpdateTask",
            subtitle = "Apple Core ML updatable model pipeline",
            modelClass = "Small updatable neuralnetwork models",
            executionLocation = "On-device Apple Neural Engine & GPU (iOS 13+)",
            deviceSupport = "Apple iOS 13+",
            maturity = "Production (with strict format constraint)",
            isRecommendedForDevice = false,
            summary = "Orchestrated via MLUpdateTask and MLBatchProvider with completion handlers persisting weights to the app sandbox.",
            constraints = listOf(
                "FORMAT CONSTRAINT: Works ONLY on legacy 'neuralnetwork' models",
                "Modern 'mlprogram' format (iOS 15+) CANNOT be trained on-device",
                "Must convert with coremltools NeuralNetworkBuilder upfront (irreversible)"
            )
        ),
        TrainingPathInfo(
            id = TrainingPathId.PATH_D,
            title = "Path D: Off-device PEFT → On-device adapter",
            subtitle = "Cloud/PC fine-tuning with mobile runtime loading",
            modelClass = "Full HF models (Gemma-2 2B, Gemma, Phi-2)",
            executionLocation = "Off-device training (PC/Cloud), on-device inference",
            deviceSupport = "LiteRT-LM (GPU), Core ML multifunction (iOS 18+), MNN, llama.cpp",
            maturity = "Production",
            isRecommendedForDevice = false,
            summary = "Train attention LoRA adapters (q_proj, v_proj) with HuggingFace PEFT, convert via MediaPipe/LiteRT-LM converter, load via loraPath.",
            constraints = listOf(
                "Requires user consent to ship data off-phone to cloud/PC",
                "LiteRT-LM constraint: loraPath is immutable after session creation (adapter switching requires session reconstruction)"
            )
        ),
        TrainingPathInfo(
            id = TrainingPathId.PATH_E,
            title = "Path E: ExecuTorch training extension",
            subtitle = "PyTorch mobile training preview",
            modelClass = "LLMs including LoRA (verified by authors)",
            executionLocation = "On-device (Android & iOS)",
            deviceSupport = "Experimental technical preview",
            maturity = "Flag-gated preview (SGD only)",
            isRecommendedForDevice = false,
            summary = "Community & PyTorch preview for on-device PyTorch model training. Supports basic SGD updates.",
            constraints = listOf(
                "Strictly flag-gated experimental preview",
                "Currently limited to SGD optimizer (no AdamW/Adam on mobile)",
                "Not recommended for commercial production apps yet"
            )
        )
    )

    val NEGATIVE_CLAIMS = listOf(
        "No on-device QLoRA (quantized adapter + quantized optimizer states) — no mobile runtime supports it.",
        "No on-device full fine-tuning of LLMs — gradient + optimizer state memory rules it out.",
        "No ONNX Runtime on-device training — packages are deprecated (frozen at 1.19.2); ORT is for inference only."
    )
}
