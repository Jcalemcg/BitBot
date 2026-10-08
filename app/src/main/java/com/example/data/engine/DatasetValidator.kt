package com.example.data.engine

import com.example.data.model.DatasetInspection

object DatasetValidator {

    fun validateDataset(
        docCount: Int,
        totalTokens: Int,
        trainSplit: Int = 80,
        valSplit: Int = 10,
        testSplit: Int = 10,
        dedupRatioPct: Float = 99.4f
    ): DatasetInspection {
        val warnings = mutableListOf<String>()

        val isUnderCap = totalTokens <= 50000
        if (!isUnderCap) {
            warnings.add("Dataset exceeds 50,000 token mobile budget ($totalTokens tokens). Risk of extreme thermal throttling. Consider Path D (Cloud PEFT).")
        }

        val totalSplit = trainSplit + valSplit + testSplit
        val isStratified = totalSplit == 100 && trainSplit in 70..85 && valSplit in 5..20

        if (totalSplit != 100) {
            warnings.add("Train/Val/Test split must sum to 100% (currently $totalSplit%).")
        }

        val isDeduplicated = dedupRatioPct >= 95.0f
        if (!isDeduplicated) {
            warnings.add("High duplication rate detected (${100 - dedupRatioPct}% duplicate sequences). Model may suffer from mode collapse.")
        }

        if (docCount < 20) {
            warnings.add("Dataset too small ($docCount documents). Minimum 50 documents recommended for meaningful LoRA rank adaptation.")
        }

        val isClean = isUnderCap && isStratified && isDeduplicated && docCount >= 20

        return DatasetInspection(
            documentCount = docCount,
            totalTokens = totalTokens,
            maxAllowedTokens = 50000,
            trainSplitPct = trainSplit,
            valSplitPct = valSplit,
            testSplitPct = testSplit,
            dedupScorePct = dedupRatioPct,
            isUnderCap = isUnderCap,
            isStratified = isStratified,
            isClean = isClean,
            warnings = warnings
        )
    }

    val SAMPLE_DATASETS = listOf(
        Pair("300 Docs / 18k Tokens (Oct 2026 QVAC Benchmark)", 18240 to 300),
        Pair("120 Docs / 7.2k Tokens (Quick Task Adaptation)", 7200 to 120),
        Pair("750 Docs / 46.5k Tokens (Near Mobile Ceiling)", 46500 to 750),
        Pair("1,200 Docs / 78k Tokens (Over Mobile Budget ⚠)", 78000 to 1200)
    )
}
