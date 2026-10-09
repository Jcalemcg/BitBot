package com.example.data.model

data class ConceptExplanation(
    val key: String,
    val title: String,
    val plainDefinition: String,
    val analogy: String,
    val outcomeIfIncreased: String,
    val outcomeIfDecreased: String,
    val recommendedTip: String
)

data class SmartPreset(
    val level: Int,
    val name: String,
    val tagline: String,
    val hyperparams: LoRAHyperparams,
    val batteryDrainPct: Int,
    val estimatedTimeMinutes: Int,
    val heatLevel: String,
    val learningDepth: String,
    val recommendationBadge: String? = null
)

object ExplanationRepository {

    val CONCEPTS = mapOf(
        "SMART_PRESET" to ConceptExplanation(
            key = "SMART_PRESET",
            title = "Quality vs. Battery & Speed Dial",
            plainDefinition = "A master control that balances how deeply the AI learns against how much phone battery and time it consumes.",
            analogy = "Like choosing your car's driving mode: 'Eco Mode' sips gas for a quick errand, while 'Sport+ Mode' burns fuel for maximum track performance.",
            outcomeIfIncreased = "AI learns subtle reasoning and specialized jargon, but phone gets warmer and uses up to ~30% battery.",
            outcomeIfDecreased = "Runs in under 15 minutes and uses < 5% battery, but the AI only learns simple surface-level changes.",
            recommendedTip = "Start on Level 3 (Balanced Sweet Spot) for the best mix of quality and phone battery."
        ),
        "MODEL_SCALE" to ConceptExplanation(
            key = "MODEL_SCALE",
            title = "Model Size (125M to 13B Parameters)",
            plainDefinition = "The overall capacity of the AI's neural network, measured by how many internal connection weights it possesses.",
            analogy = "Think of this as a student's brain capacity. A 1B model is a bright high school student who can study right on your phone. A 7B or 13B model is a giant encyclopedia that physically overflows your phone's memory desk and causes a crash (Out-Of-Memory).",
            outcomeIfIncreased = "Smarter, richer responses, but exponentially slower. 7B and 13B models will crash on Galaxy S25.",
            outcomeIfDecreased = "Much faster training (10 min vs hours) and zero risk of crashing, but simpler vocabulary.",
            recommendedTip = "Stick to 1B for phone training. It's the verified sweet spot that fits mobile RAM."
        ),
        "QUANT_FORMAT" to ConceptExplanation(
            key = "QUANT_FORMAT",
            title = "Quantization Format (TQ2_0 vs TQ1_0)",
            plainDefinition = "How many digital bits are used to store each number inside the model.",
            analogy = "Like saving a photograph. TQ2_0 is high-definition RAW format where you can edit colors cleanly without distortion (training). TQ1_0 is a compressed JPEG that takes almost no storage in your phone's gallery (finished app).",
            outcomeIfIncreased = "TQ2_0 gives clean, stable learning without math errors, but takes ~2× more RAM during training.",
            outcomeIfDecreased = "TQ1_0 shrinks memory to just 1.6 bits per weight, perfect for the final app, but less stable during training.",
            recommendedTip = "Always train with TQ2_0, then convert the finished model to TQ1_0 for the final app."
        ),
        "LORA_RANK" to ConceptExplanation(
            key = "LORA_RANK",
            title = "LoRA Rank (r)",
            plainDefinition = "The width of the extra adapter layer that learns your new data without altering the original AI.",
            analogy = "Think of LoRA like sticky notes added into a thick textbook. Instead of rewriting the entire book (which crashes your phone), you write notes in the margins. Rank 8 = small, focused sticky notes. Rank 16 = larger sticky notes with more room for detail.",
            outcomeIfIncreased = "The AI can memorize more complex rules, but requires more graphics memory and calculation time.",
            outcomeIfDecreased = "Uses almost no memory and runs super fast, but can only adapt to simple keywords or tone changes.",
            recommendedTip = "Rank 8 is the industry standard default for mobile LoRA adapters."
        ),
        "LORA_ALPHA" to ConceptExplanation(
            key = "LORA_ALPHA",
            title = "LoRA Alpha (α)",
            plainDefinition = "The scaling multiplier that determines how strongly the new adapter influences the model's answers.",
            analogy = "This is the volume knob for your sticky notes. A higher Alpha tells the AI: 'Pay strong attention to my new notes!' A lower Alpha says: 'Stick mostly to your original knowledge.'",
            outcomeIfIncreased = "The AI changes its behavior more aggressively, but setting it too high might cause it to forget general common sense.",
            outcomeIfDecreased = "Subtle, conservative changes. Might ignore parts of your new dataset if set too low.",
            recommendedTip = "Rule of thumb: Set Alpha to exactly 2× your Rank (e.g., Rank 8 → Alpha 16)."
        ),
        "BATCH_SIZE" to ConceptExplanation(
            key = "BATCH_SIZE",
            title = "Batch Size (-b)",
            plainDefinition = "How many training examples the phone's graphics chip reads simultaneously before updating its notes.",
            analogy = "Studying with flashcards: reading 128 flashcards at once is much faster for a fast reader, but requires a wider desk. Reading 64 cards at a time takes less desk space.",
            outcomeIfIncreased = "Trains faster overall on modern GPUs, but uses more instant graphics RAM.",
            outcomeIfDecreased = "Uses less peak memory (safer for older phones), but takes slightly more time.",
            recommendedTip = "128 is ideal for Snapdragon 8 Elite (Adreno 830) GPUs."
        ),
        "DATASET_CAP" to ConceptExplanation(
            key = "DATASET_CAP",
            title = "Dataset 50k Token Mobile Budget",
            plainDefinition = "The total number of words/tokens allowed in your training file before it becomes too heavy for a phone.",
            analogy = "A backpack limit for a day hike. Carrying 300 pages (~18k words) is a brisk walk. Trying to carry a 20-volume encyclopedia (~100k words) will exhaust you and drain your battery.",
            outcomeIfIncreased = "More examples give the AI better domain coverage, but exceeding 50k tokens causes phone overheating and multi-hour runs.",
            outcomeIfDecreased = "Fast, lightweight fine-tuning in under 30 minutes.",
            recommendedTip = "Keep mobile datasets between 150 to 500 documents (~10k to ~30k tokens)."
        ),
        "LOSS_METRIC" to ConceptExplanation(
            key = "LOSS_METRIC",
            title = "Cross-Entropy Loss Score",
            plainDefinition = "A measurement of how many mistakes the AI is making as it predicts each word in your dataset.",
            analogy = "Your score on an exam, but in reverse! High loss (3.4) means the AI is guessing blindly. Low loss (1.1) means the AI has learned your patterns and gets most answers right.",
            outcomeIfIncreased = "Indicates the AI is confused or hasn't started learning yet.",
            outcomeIfDecreased = "Indicates the AI is successfully mastering your custom instructions.",
            recommendedTip = "A successful run typically drops from ~3.4 down to between 1.0 and 1.4."
        ),
        "PERPLEXITY_METRIC" to ConceptExplanation(
            key = "PERPLEXITY_METRIC",
            title = "Perplexity (PPL Confusion Meter)",
            plainDefinition = "How surprised or confused the AI is when reading test sentences in your chosen subject.",
            analogy = "A doctor hearing medical jargon has low perplexity (zero confusion). A high school student hearing the same jargon has high perplexity (confused).",
            outcomeIfIncreased = "The AI doesn't understand your domain and will speak generic, vague answers.",
            outcomeIfDecreased = "The AI speaks fluently in your specific domain with high accuracy.",
            recommendedTip = "A drop in perplexity (e.g., from 18 down to 4) proves your training succeeded!"
        ),
        "BATTERY_GUARD" to ConceptExplanation(
            key = "BATTERY_GUARD",
            title = "Battery 30% Safety Threshold",
            plainDefinition = "An automated safety guard that pauses training if your battery drops below 30% without a charger.",
            analogy = "A flight computer reserving fuel to ensure the plane can land safely. We never let heavy AI training drain your phone to zero.",
            outcomeIfIncreased = "Safer battery preservation; pauses earlier.",
            outcomeIfDecreased = "Allows longer runs on battery, but risks unexpected phone shutoffs.",
            recommendedTip = "Always plug your phone into a charging stand during longer training sessions."
        ),
        "THERMAL_COOLDOWN" to ConceptExplanation(
            key = "THERMAL_COOLDOWN",
            title = "Thermal Throttling & Cooldown",
            plainDefinition = "When your phone's processor gets too warm, it automatically slows down to protect the hardware.",
            analogy = "A runner taking a walking break when their heart rate gets too high. If 3 epochs take >1.5× normal time, our engine pauses for a cooldown.",
            outcomeIfIncreased = "Prevents hardware heat degradation and battery swelling.",
            outcomeIfDecreased = "None — safety cooldown is essential on mobile devices.",
            recommendedTip = "Train in a cool room or remove thick protective phone cases during long sessions."
        )
    )

    val PRESETS = listOf(
        SmartPreset(
            level = 1,
            name = "Quick Demo / Battery Saver",
            tagline = "Super fast 10-minute check. Minimal battery use.",
            hyperparams = LoRAHyperparams(
                modelScale = ModelScale.SCALE_125M,
                quantFormat = QuantFormat.TQ2_0,
                rank = 4,
                alpha = 8,
                batchSize = 64,
                epochs = 4
            ),
            batteryDrainPct = 3,
            estimatedTimeMinutes = 10,
            heatLevel = "Cool (Zero heat)",
            learningDepth = "Light surface tuning"
        ),
        SmartPreset(
            level = 2,
            name = "Light Task Personalization",
            tagline = "Adapts tone and keywords in under 30 minutes.",
            hyperparams = LoRAHyperparams(
                modelScale = ModelScale.SCALE_350M,
                quantFormat = QuantFormat.TQ2_0,
                rank = 8,
                alpha = 16,
                batchSize = 128,
                epochs = 6
            ),
            batteryDrainPct = 8,
            estimatedTimeMinutes = 28,
            heatLevel = "Mild (Slight warmth)",
            learningDepth = "Good tone adaptation"
        ),
        SmartPreset(
            level = 3,
            name = "Recommended Sweet Spot",
            tagline = "The verified 1B phone sweet spot: deep learning in ~1.3 hours.",
            hyperparams = LoRAHyperparams(
                modelScale = ModelScale.SCALE_1B,
                quantFormat = QuantFormat.TQ2_0,
                rank = 8,
                alpha = 16,
                batchSize = 128,
                epochs = 8
            ),
            batteryDrainPct = 14,
            estimatedTimeMinutes = 78,
            heatLevel = "Moderate (Warm phone)",
            learningDepth = "High domain mastery",
            recommendationBadge = "RECOMMENDED"
        ),
        SmartPreset(
            level = 4,
            name = "Deep Domain Specialist",
            tagline = "Higher rank (r=16) for intricate coding or technical tasks.",
            hyperparams = LoRAHyperparams(
                modelScale = ModelScale.SCALE_1B,
                quantFormat = QuantFormat.TQ2_0,
                rank = 16,
                alpha = 32,
                batchSize = 128,
                epochs = 10
            ),
            batteryDrainPct = 20,
            estimatedTimeMinutes = 98,
            heatLevel = "High (Recommend charger)",
            learningDepth = "Complex logic & coding"
        ),
        SmartPreset(
            level = 5,
            name = "Maximum Power (Heavy Compute)",
            tagline = "2.7B parameter heavy adaptation. Requires charging stand.",
            hyperparams = LoRAHyperparams(
                modelScale = ModelScale.SCALE_2_7B,
                quantFormat = QuantFormat.TQ2_0,
                rank = 16,
                alpha = 32,
                batchSize = 128,
                epochs = 8
            ),
            batteryDrainPct = 35,
            estimatedTimeMinutes = 208,
            heatLevel = "Very High (Charger Required)",
            learningDepth = "Full mobile capability ceiling"
        )
    )

    fun getExplanation(key: String): ConceptExplanation {
        return CONCEPTS[key] ?: ConceptExplanation(
            key = key,
            title = key,
            plainDefinition = "A fine-tuning hyperparameter.",
            analogy = "A configuration parameter for edge model training.",
            outcomeIfIncreased = "Uses more compute.",
            outcomeIfDecreased = "Uses less compute.",
            recommendedTip = "Use default recommendation."
        )
    }
}
