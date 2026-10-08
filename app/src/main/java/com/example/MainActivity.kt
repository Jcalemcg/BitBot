package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.FineTuningEngine
import com.example.ui.components.TelemetryTopBar
import com.example.ui.screens.BackendSelectorScreen
import com.example.ui.screens.BenchmarkMatrixScreen
import com.example.ui.screens.BitNetStudioScreen
import com.example.ui.screens.RecipesScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.TelemetryEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    SELECTOR("Selector", Icons.Default.Hub, "tab_selector"),
    STUDIO("BitNet Studio", Icons.Default.PlayCircleOutline, "tab_studio"),
    MATRIX("Benchmarks", Icons.Default.TableChart, "tab_matrix"),
    RECIPES("Recipes", Icons.Default.Code, "tab_recipes")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val coroutineScope = rememberCoroutineScope()
                val engine = remember { FineTuningEngine(applicationContext, coroutineScope) }
                val sessionState by engine.sessionState.collectAsState()

                var selectedTab by remember { mutableIntStateOf(0) }

                BackHandler(enabled = selectedTab != 0) {
                    selectedTab = 0
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberBackground),
                    topBar = {
                        Column(modifier = Modifier.statusBarsPadding()) {
                            TelemetryTopBar(
                                batteryPct = sessionState.batteryPct,
                                isCharging = sessionState.isCharging,
                                thermalTempC = sessionState.thermalTempC,
                                status = sessionState.status
                            )
                        }
                    },
                    bottomBar = {
                        OnDeviceMLNavBar(
                            currentTabIndex = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    },
                    containerColor = CyberBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> BackendSelectorScreen(
                                onNavigateToStudioWithConfig = { config ->
                                    engine.updateHyperparams(config)
                                    selectedTab = 1 // Switch to Studio
                                }
                            )
                            1 -> BitNetStudioScreen(engine = engine)
                            2 -> BenchmarkMatrixScreen()
                            3 -> RecipesScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnDeviceMLNavBar(
    currentTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = CyberDark,
        tonalElevation = 0.dp
    ) {
        AppTab.entries.forEachIndexed { index, tab ->
            val isSelected = currentTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberDark,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
