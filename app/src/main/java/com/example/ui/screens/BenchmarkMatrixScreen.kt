package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.scanner.DeviceHardwareProfile
import com.example.data.scanner.DeviceTier
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
fun BenchmarkMatrixScreen(
    deviceProfile: DeviceHardwareProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "YOUR DEVICE PERFORMANCE BENCHMARKS",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Calibrated specifically for ${deviceProfile.manufacturer} ${deviceProfile.deviceModel} (${deviceProfile.tier.title}).",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Best Case Scenario & Expectations Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TelemetryEmerald.copy(alpha = 0.12f))
                .border(1.dp, TelemetryEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TelemetryEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "BEST-CASE SCENARIO FOR YOUR DEVICE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryEmerald
                    )
                    Text(
                        text = when (deviceProfile.tier) {
                            DeviceTier.TIER_S -> "Your hardware has ample RAM (%.1f GB). You can comfortably fine-tune 1B models in ~1.3h or 2.7B models overnight on a charging stand.".format(deviceProfile.totalRamGb)
                            DeviceTier.TIER_A -> "Your hardware is in the high-performance tier (%.1f GB RAM). The 1B BitNet model is your optimal sweet spot. Stay at or below 1B for best stability.".format(deviceProfile.totalRamGb)
                            DeviceTier.TIER_B -> "Mainstream mobile tier (%.1f GB RAM). Fast 125M and 350M models will run smoothly. 1B models may run warm and approach available memory limits.".format(deviceProfile.totalRamGb)
                            DeviceTier.TIER_C -> "Resource-constrained device (%.1f GB RAM). We recommend lightweight 125M models or off-device PEFT (Path D) to prevent phone lag.".format(deviceProfile.totalRamGb)
                        },
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Benchmark Table
        Text(
            text = "ESTIMATED PERFORMANCE BY MODEL SCALE:",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Table container with horizontal scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.width(520.dp)) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "MODEL", modifier = Modifier.width(70.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "FEASIBILITY", modifier = Modifier.width(130.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Text(text = "TIME / EPOCH", modifier = Modifier.width(120.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "BATTERY DRAIN", modifier = Modifier.width(110.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = WarningAmber)
                    Text(text = "RAM NEEDED", modifier = Modifier.width(90.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                ModelScale.entries.forEach { scale ->
                    val (canRun, reason) = deviceProfile.canRunModel(scale, QuantFormat.TQ2_0)
                    val isSweet = scale == ModelScale.SCALE_1B && canRun
                    val seconds = if (canRun) deviceProfile.estimateSecondsForModel(scale) else null
                    val drainPct = if (canRun) deviceProfile.estimateBatteryDrainForModel(scale) else 0

                    val timeStr = if (canRun && seconds != null) {
                        if (seconds >= 3600) "${seconds / 3600}h ${(seconds % 3600) / 60}m"
                        else "${seconds / 60}m ${seconds % 60}s"
                    } else "Cannot Run"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                if (isSweet) 1.dp else 0.dp,
                                if (isSweet) TelemetryEmerald.copy(alpha = 0.35f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Model name
                        Row(modifier = Modifier.width(70.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = scale.label,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    !canRun -> AlertCrimson.copy(alpha = 0.6f)
                                    isSweet -> TelemetryEmerald
                                    else -> TextPrimary
                                }
                            )
                            if (isSweet) {
                                Text(text = "★", fontSize = 10.sp, color = TelemetryEmerald, modifier = Modifier.padding(start = 2.dp))
                            }
                        }

                        // Feasibility Badge
                        Box(modifier = Modifier.width(130.dp)) {
                            Text(
                                text = when {
                                    !canRun -> "🔒 Exceeds RAM"
                                    isSweet -> "★ Sweet Spot"
                                    scale == ModelScale.SCALE_2_7B -> "⚠ Heavy Load"
                                    else -> "✓ Ready to Run"
                                },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    !canRun -> AlertCrimson
                                    isSweet -> TelemetryEmerald
                                    scale == ModelScale.SCALE_2_7B -> WarningAmber
                                    else -> NeonCyanLight
                                }
                            )
                        }

                        // Time
                        Box(modifier = Modifier.width(120.dp)) {
                            Text(
                                text = timeStr,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (!canRun) AlertCrimson else TextPrimary
                            )
                        }

                        // Battery drain
                        Box(modifier = Modifier.width(110.dp)) {
                            Text(
                                text = if (canRun) "~$drainPct% / epoch" else "N/A",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (drainPct > 30) AlertCrimson else if (drainPct > 15) WarningAmber else TelemetryEmerald
                            )
                        }

                        // RAM needed
                        Box(modifier = Modifier.width(90.dp)) {
                            Text(
                                text = "%.1f GB".format(scale.tq2SizeGb + 0.9f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hardware Expectations & Safety Guidelines
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECOMMENDED EXPECTATIONS FOR SESSIONS",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "• Keep Phone Plugged In: Training on mobile GPUs draws significant power. Plugging into a fast charger prevents the automatic 30% battery pause.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Text(
                    text = "• Keep App Open: Mobile operating systems aggressively throttle or freeze background graphics threads. Leave the screen awake during active epochs.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Text(
                    text = "• Stay in Cool Environment: If your phone heats up, automatic thermal cooldown will pause training to safeguard battery longevity.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quantization format note
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberDark)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "QUANTIZATION TRADEOFF ON YOUR DEVICE",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberSurface, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "TQ2_0 (Training Default)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Text(text = "Best math stability for fine-tuning. Fits cleanly on your device for ≤ 1B models.", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberSurface, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "TQ1_0 (Deploy Only)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TelemetryEmerald)
                        Text(text = "~1.6 bits/weight. Extreme compression to ensure your final APK stays tiny.", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
