package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExplanationRepository
import com.example.data.model.SmartPreset
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
fun SmartPresetSlider(
    currentLevel: Int,
    onLevelChanged: (SmartPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember { ExplanationRepository.PRESETS }
    val activePreset = remember(currentLevel) {
        presets.firstOrNull { it.level == currentLevel } ?: presets[2]
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Top label & title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SMART DIAL: QUALITY VS. BATTERY",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    if (activePreset.recommendationBadge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(TelemetryEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activePreset.recommendationBadge,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TelemetryEmerald
                            )
                        }
                    }
                }
                Text(
                    text = "One-touch beginner preset balancing depth, heat, and battery drain",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Level Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDark, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Level ${activePreset.level}: ${activePreset.name}",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = activePreset.tagline,
                    fontSize = 11.sp,
                    color = NeonCyanLight,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5-Level Slider
        Slider(
            value = activePreset.level.toFloat(),
            onValueChange = { newVal ->
                val rounded = newVal.toInt().coerceIn(1, 5)
                val targetPreset = presets.first { it.level == rounded }
                onLevelChanged(targetPreset)
            },
            valueRange = 1f..5f,
            steps = 3, // 1, 2, 3, 4, 5
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = CyberDark,
                activeTickColor = CyberDark,
                inactiveTickColor = TextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("smart_preset_slider")
        )

        // Labels under slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1: Battery Saver", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
            Text(text = "3: Sweet Spot", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TelemetryEmerald, fontWeight = FontWeight.Bold)
            Text(text = "5: Max Power", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = WarningAmber)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Outcome Impact Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Battery impact
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(CyberDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (activePreset.batteryDrainPct > 20) WarningAmber else TelemetryEmerald,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(text = "BATTERY DRAIN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(
                        text = "~${activePreset.batteryDrainPct}% total",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Time impact
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(CyberDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(text = "ESTIMATED TIME", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(
                        text = if (activePreset.estimatedTimeMinutes >= 60)
                            "~${activePreset.estimatedTimeMinutes / 60}h ${activePreset.estimatedTimeMinutes % 60}m"
                        else "~${activePreset.estimatedTimeMinutes} min",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Heat & Learning Depth Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Heat Level
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(CyberDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeviceThermostat,
                    contentDescription = null,
                    tint = if (activePreset.level >= 4) AlertCrimson else WarningAmber,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(text = "PHONE HEAT", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(
                        text = activePreset.heatLevel,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Learning Depth
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(CyberDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TelemetryEmerald,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(text = "LEARNING DEPTH", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(
                        text = activePreset.learningDepth,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Inline explanation toggle for Smart Preset
        InlineExplainToggle(conceptKey = "SMART_PRESET")
    }
}
