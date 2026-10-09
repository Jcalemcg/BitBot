package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.FineTuningEngine
import com.example.data.huggingface.HuggingFaceRepository
import com.example.data.scanner.DeviceHardwareScanner
import com.example.ui.components.TelemetryTopBar
import com.example.ui.screens.BackendSelectorScreen
import com.example.ui.screens.BenchmarkMatrixScreen
import com.example.ui.screens.BitNetStudioScreen
import com.example.ui.screens.HuggingFaceHubScreen
import com.example.ui.screens.RecipesScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    SELECTOR("Selector", Icons.Default.Hub, "tab_selector"),
    STUDIO("BitNet Studio", Icons.Default.PlayCircleOutline, "tab_studio"),
    HF_HUB("HF Hub", Icons.Default.CloudDownload, "tab_hf_hub"),
    MATRIX("Benchmarks", Icons.Default.TableChart, "tab_matrix"),
    RECIPES("Recipes", Icons.Default.Code, "tab_recipes")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val engine = remember { FineTuningEngine(applicationContext, coroutineScope) }
                val hfRepository = remember { HuggingFaceRepository() }
                val sessionState by engine.sessionState.collectAsState()

                var deviceProfile by remember {
                    mutableStateOf(DeviceHardwareScanner.scan(context))
                }

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
                                status = sessionState.status,
                                deviceDisplayName = deviceProfile.deviceModel,
                                tierLabel = deviceProfile.tier.badgeLabel
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
                                deviceProfile = deviceProfile,
                                onRescanRequested = {
                                    deviceProfile = DeviceHardwareScanner.scan(context)
                                },
                                onNavigateToStudioWithConfig = { config ->
                                    engine.updateHyperparams(config)
                                    selectedTab = 1
                                }
                            )
                            1 -> BitNetStudioScreen(engine = engine)
                            2 -> HuggingFaceHubScreen(
                                repository = hfRepository,
                                deviceProfile = deviceProfile,
                                onModelSelected = { model ->
                                    engine.loadHuggingFaceModel(model)
                                    selectedTab = 1 // Switch to Studio with model loaded
                                },
                                onDatasetSelected = { dataset ->
                                    engine.loadHuggingFaceDataset(dataset)
                                    selectedTab = 1 // Switch to Studio with dataset loaded
                                }
                            )
                            3 -> BenchmarkMatrixScreen(deviceProfile = deviceProfile)
                            4 -> RecipesScreen()
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
                        fontSize = 9.sp,
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
