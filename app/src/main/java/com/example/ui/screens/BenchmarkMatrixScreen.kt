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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.repository.BenchmarkData
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ChipsetA18
import com.example.ui.theme.ChipsetAdreno
import com.example.ui.theme.ChipsetMali
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
            text = "HARDWARE REALITY & BENCHMARK MATRIX",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Empirical measurements (time per epoch in TQ2_0 format) on actual flagship mobile chipsets.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Sweet Spot Callout Banner
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PHONE-CLASS SWEET SPOT: ≤ 1B PARAMETERS",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryEmerald
                    )
                    Text(
                        text = "Fine-tuning ~300 documents (~18k tokens) on 1B BitNet takes ~1.3 h on Samsung S25. Anything above 2.7B triggers severe thermal throttles or physical OOM.",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empirical Benchmark Matrix Table
        Text(
            text = "EMPIRICAL TIME PER EPOCH (TQ2_0 FORMAT):",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Table container with horizontal scroll support
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.width(540.dp)) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberDark, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "MODEL", modifier = Modifier.width(70.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "S25 (Adreno 830)", modifier = Modifier.width(150.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ChipsetAdreno)
                    Text(text = "Pixel 9 (Mali)", modifier = Modifier.width(150.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ChipsetMali)
                    Text(text = "iPhone 16 (A18)", modifier = Modifier.width(150.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ChipsetA18)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                BenchmarkData.BENCHMARK_TABLE.forEach { entry ->
                    val isSweet = entry.scale.sweetSpot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                if (isSweet) 1.dp else 0.dp,
                                if (isSweet) TelemetryEmerald.copy(alpha = 0.25f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.width(70.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.scale.label,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSweet) TelemetryEmerald else TextPrimary
                            )
                            if (isSweet) {
                                Text(text = "★", fontSize = 10.sp, color = TelemetryEmerald, modifier = Modifier.padding(start = 2.dp))
                            }
                        }

                        // S25 cell
                        Box(modifier = Modifier.width(150.dp)) {
                            Text(
                                text = entry.s25Display,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = if (entry.s25Oom) AlertCrimson else TextPrimary
                            )
                        }

                        // Pixel 9 cell
                        Box(modifier = Modifier.width(150.dp)) {
                            Text(
                                text = entry.pixel9Display,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = if (entry.pixel9Oom) AlertCrimson else TextPrimary
                            )
                        }

                        // iPhone 16 cell
                        Box(modifier = Modifier.width(150.dp)) {
                            Text(
                                text = entry.iphone16Display,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = if (entry.iphone16Oom) AlertCrimson else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 11x Speedup Card: GPU vs CPU
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPU ACCELERATION: UP TO 11× FASTER",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "On Samsung Galaxy S25, fine-tuning 1B BitNet on the Adreno 830 GPU was up to 11× faster than running on CPU cores. Always dispatch to GPU and enable dynamic tiling in QVAC Fabric.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Speedup Visual Bar
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Adreno 830 GPU:", fontSize = 10.sp, color = NeonCyanLight, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(NeonCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = "1h 18m", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyberDark)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Kryo CPU Cores (11× Slower):", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(AlertCrimson.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = "~14h 18m", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Format Choice: TQ1_0 vs TQ2_0
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberDark)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "QUANTIZATION TRADEOFF: TQ1_0 VS TQ2_0",
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
                        Text(text = "TQ2_0 (Default)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Text(text = "~2.0 bits/weight · Numerically stable gradient updates during LoRA training.", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberSurface, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "TQ1_0 (Deploy)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TelemetryEmerald)
                        Text(text = "~1.6 bits/weight · 7B base ≈ 1.9GB vs 4.3GB. Extreme RAM compression for deployment.", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rule: Use TQ2_0 for fine-tuning, then convert the merged model to TQ1_0 for deployed APK inference.",
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
