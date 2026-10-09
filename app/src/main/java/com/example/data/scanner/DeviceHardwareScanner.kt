package com.example.data.scanner

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import kotlin.math.roundToInt

enum class DeviceTier(
    val title: String,
    val badgeLabel: String,
    val description: String,
    val maxRecommendedScale: ModelScale,
    val relativeSpeedMultiplier: Float
) {
    TIER_S(
        title = "Tier S: Ultra Flagship Edge AI",
        badgeLabel = "TIER S (FLAGSHIP)",
        description = "High unified memory (≥ 12 GB) with advanced GPU acceleration. Capable of fine-tuning models up to 2.7B on-device.",
        maxRecommendedScale = ModelScale.SCALE_2_7B,
        relativeSpeedMultiplier = 1.0f
    ),
    TIER_A(
        title = "Tier A: High Performance Mobile AI",
        badgeLabel = "TIER A (HIGH PERF)",
        description = "Solid mobile memory (8–11 GB) with Vulkan support. Optimized for the 1B parameter sweet spot.",
        maxRecommendedScale = ModelScale.SCALE_1B,
        relativeSpeedMultiplier = 1.25f
    ),
    TIER_B(
        title = "Tier B: Mainstream Mobile",
        badgeLabel = "TIER B (MAINSTREAM)",
        description = "Moderate mobile memory (4–7 GB). Best suited for 125M–350M models or lightweight classifiers.",
        maxRecommendedScale = ModelScale.SCALE_350M,
        relativeSpeedMultiplier = 1.8f
    ),
    TIER_C(
        title = "Tier C: Resource-Constrained",
        badgeLabel = "TIER C (CONSTRAINED)",
        description = "Limited memory (< 4 GB). On-device LLM fine-tuning risks system out-of-memory. Recommended: Path B or Path D.",
        maxRecommendedScale = ModelScale.SCALE_125M,
        relativeSpeedMultiplier = 2.5f
    )
}

data class HardwareCheckBadge(
    val title: String,
    val detail: String,
    val passed: Boolean
)

data class DeviceHardwareProfile(
    val deviceModel: String,
    val manufacturer: String,
    val hardwareName: String,
    val totalRamGb: Float,
    val availableRamGb: Float,
    val cpuCores: Int,
    val hasVulkanSupport: Boolean,
    val batteryPct: Int,
    val isCharging: Boolean,
    val tier: DeviceTier,
    val checkBadges: List<HardwareCheckBadge>
) {
    fun canRunModel(scale: ModelScale, quantFormat: QuantFormat): Pair<Boolean, String?> {
        val requiredRamGb = when (quantFormat) {
            QuantFormat.TQ1_0 -> scale.tq1SizeGb + 0.6f
            QuantFormat.TQ2_0 -> scale.tq2SizeGb + 0.9f
        }

        // Leave at least 1.2 GB safety buffer for Android OS and background services
        val maxSafeBudgetGb = (totalRamGb - 1.2f).coerceAtLeast(0.5f)

        return if (requiredRamGb > maxSafeBudgetGb || scale.paramCountMillions > tier.maxRecommendedScale.paramCountMillions * 2.2f) {
            val shortageGb = requiredRamGb - availableRamGb
            val reason = if (requiredRamGb > totalRamGb) {
                "Exceeds physical RAM (Needs %.1f GB, Total %.1f GB)".format(requiredRamGb, totalRamGb)
            } else {
                "Memory shortfall (Needs %.1f GB, only %.1f GB free)".format(requiredRamGb, availableRamGb)
            }
            Pair(false, reason)
        } else {
            Pair(true, null)
        }
    }

    fun estimateSecondsForModel(scale: ModelScale): Long {
        val baseSeconds = when (scale) {
            ModelScale.SCALE_125M -> 610L
            ModelScale.SCALE_350M -> 1720L
            ModelScale.SCALE_1B -> 4680L
            ModelScale.SCALE_2_7B -> 12480L
            ModelScale.SCALE_7B -> 46000L
            ModelScale.SCALE_13B -> 83000L
        }
        val coreAdjustment = if (cpuCores >= 8) 1.0f else (8f / cpuCores.coerceAtLeast(2))
        val vulkanAdjustment = if (hasVulkanSupport) 1.0f else 3.2f
        return (baseSeconds * tier.relativeSpeedMultiplier * coreAdjustment * vulkanAdjustment).toLong()
    }

    fun estimateBatteryDrainForModel(scale: ModelScale): Int {
        val baseDrain = when (scale) {
            ModelScale.SCALE_125M -> 3
            ModelScale.SCALE_350M -> 8
            ModelScale.SCALE_1B -> 14
            ModelScale.SCALE_2_7B -> 32
            ModelScale.SCALE_7B -> 75
            ModelScale.SCALE_13B -> 100
        }
        return (baseDrain * tier.relativeSpeedMultiplier).roundToInt().coerceIn(2, 95)
    }
}

object DeviceHardwareScanner {

    fun scan(context: Context): DeviceHardwareProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalRamBytes = if (memInfo.totalMem > 0L) {
            memInfo.totalMem
        } else {
            // Robust fallback for JVM test environments or unpopulated memory fields
            Runtime.getRuntime().maxMemory().coerceAtLeast(4L * 1024L * 1024L * 1024L)
        }
        val availRamBytes = if (memInfo.availMem > 0L) {
            memInfo.availMem
        } else {
            (totalRamBytes * 0.45f).toLong()
        }

        val totalRamGb = (totalRamBytes / (1024f * 1024f * 1024f) * 10f).roundToInt() / 10f
        val availRamGb = (availRamBytes / (1024f * 1024f * 1024f) * 10f).roundToInt() / 10f

        val cpuCores = Runtime.getRuntime().availableProcessors()
        val hasVulkan = context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)

        // Read battery state
        var batteryPct = 85
        var isCharging = false
        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                batteryPct = (level * 100 / scale.toFloat()).toInt()
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (_: Exception) {
            // Keep default
        }

        // Determine capability tier based on actual hardware measurements
        val tier = when {
            totalRamGb >= 11.5f && hasVulkan && cpuCores >= 8 -> DeviceTier.TIER_S
            totalRamGb >= 7.5f && hasVulkan -> DeviceTier.TIER_A
            totalRamGb >= 3.5f -> DeviceTier.TIER_B
            else -> DeviceTier.TIER_C
        }

        val checkBadges = listOf(
            HardwareCheckBadge(
                title = "RAM Capacity",
                detail = "%.1f GB Total (%.1f GB Free)".format(totalRamGb, availRamGb),
                passed = totalRamGb >= 5.5f
            ),
            HardwareCheckBadge(
                title = "GPU Acceleration",
                detail = if (hasVulkan) "Vulkan Hardware Detected" else "CPU Fallback Only",
                passed = hasVulkan
            ),
            HardwareCheckBadge(
                title = "Compute Cores",
                detail = "$cpuCores Parallel CPU Cores",
                passed = cpuCores >= 6
            ),
            HardwareCheckBadge(
                title = "Battery Headroom",
                detail = if (isCharging) "$batteryPct% (Charging)" else "$batteryPct% on Battery",
                passed = isCharging || batteryPct >= 30
            )
        )

        val deviceModel = if (Build.MODEL.isNotBlank()) Build.MODEL else "Android Device"
        val manufacturer = if (Build.MANUFACTURER.isNotBlank()) Build.MANUFACTURER else "Generic"
        val hardwareName = if (Build.HARDWARE.isNotBlank()) Build.HARDWARE else Build.BOARD

        return DeviceHardwareProfile(
            deviceModel = deviceModel,
            manufacturer = manufacturer,
            hardwareName = hardwareName,
            totalRamGb = totalRamGb,
            availableRamGb = availRamGb,
            cpuCores = cpuCores,
            hasVulkanSupport = hasVulkan,
            batteryPct = batteryPct,
            isCharging = isCharging,
            tier = tier,
            checkBadges = checkBadges
        )
    }
}
