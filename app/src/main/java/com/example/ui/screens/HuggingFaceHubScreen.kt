package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.huggingface.HFDatasetEntry
import com.example.data.huggingface.HFModelEntry
import com.example.data.huggingface.HuggingFaceRepository
import com.example.data.scanner.DeviceHardwareProfile
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
import kotlinx.coroutines.launch

@Composable
fun HuggingFaceHubScreen(
    repository: HuggingFaceRepository,
    deviceProfile: DeviceHardwareProfile,
    onModelSelected: (HFModelEntry) -> Unit,
    onDatasetSelected: (HFDatasetEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val userToken by repository.userApiToken.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Models, 1: Datasets
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var modelsList by remember { mutableStateOf<List<HFModelEntry>>(emptyList()) }
    var datasetsList by remember { mutableStateOf<List<HFDatasetEntry>>(emptyList()) }

    var showTokenDialog by remember { mutableStateOf(false) }
    var tokenInputText by remember { mutableStateOf(userToken ?: "") }

    // Load items on launch and on search
    fun performSearch(query: String) {
        coroutineScope.launch {
            isLoading = true
            if (activeTab == 0) {
                modelsList = repository.getModels(query)
            } else {
                datasetsList = repository.getDatasets(query)
            }
            isLoading = false
        }
    }

    LaunchedEffect(activeTab) {
        performSearch(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        // Hub Header & Token Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HUGGING FACE HUB // EDGE HUB",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Live models & instruction datasets audited against ${deviceProfile.deviceModel}.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurfaceVariant)
                    .clickable { showTokenDialog = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("hf_token_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Hugging Face Token",
                        tint = if (userToken != null) TelemetryEmerald else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (userToken != null) "TOKEN ACTIVE" else "PUBLIC API",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (userToken != null) TelemetryEmerald else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                performSearch(it)
            },
            placeholder = {
                Text(
                    text = if (activeTab == 0) "Search models (e.g. bitnet, 1b, gguf)..." else "Search datasets (e.g. alpaca, code, chat)...",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        performSearch("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hf_search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Tag Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = if (activeTab == 0) {
                listOf("All", "bitnet", "gguf", "1b", "smollm", "gemma")
            } else {
                listOf("All", "alpaca", "code", "no_robots", "dolly")
            }

            chips.forEach { chipTag ->
                val isSelected = (chipTag == "All" && searchQuery.isBlank()) || searchQuery.equals(chipTag, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberSurfaceVariant else CyberDark)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else CyberCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            val newQuery = if (chipTag == "All") "" else chipTag
                            searchQuery = newQuery
                            performSearch(newQuery)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (chipTag == "All") "ALL" else "#$chipTag",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) NeonCyanLight else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Models vs Datasets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface, RoundedCornerShape(8.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeTab == 0) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        if (activeTab == 0) 1.dp else 0.dp,
                        if (activeTab == 0) NeonCyan else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MODELS (${modelsList.size})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == 0) NeonCyan else TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeTab == 1) TelemetryEmerald.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        if (activeTab == 1) 1.dp else 0.dp,
                        if (activeTab == 1) TelemetryEmerald else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DATASETS (${datasetsList.size})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == 1) TelemetryEmerald else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Content Lists
            if (activeTab == 0) {
                // Models List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(modelsList, key = { it.repoId }) { model ->
                        val (canRun, reason) = deviceProfile.canRunModel(model.scale, model.quantFormat)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurface)
                                .border(
                                    1.dp,
                                    if (model.isSweetSpot && canRun) TelemetryEmerald.copy(alpha = 0.5f) else CyberCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.repoId,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${model.scale.label} · ${model.format} · ${model.quantFormat.displayName}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonCyanLight,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${model.downloads / 1000}k",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMuted
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${model.likes}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = model.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Compatibility Badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (canRun) TelemetryEmerald.copy(alpha = 0.1f) else AlertCrimson.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (canRun) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (canRun) TelemetryEmerald else AlertCrimson,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canRun)
                                        "✓ FITS IN RAM (${deviceProfile.tier.badgeLabel} · Est. %.1f GB)".format(model.scale.tq2SizeGb + 0.9f)
                                    else
                                        "🔒 EXCEEDS RAM: $reason",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canRun) TelemetryEmerald else AlertCrimson
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onModelSelected(model) },
                                enabled = canRun,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = CyberDark,
                                    disabledContainerColor = CyberDark,
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (canRun) Icons.Default.PlayArrow else Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canRun) "IMPORT INTO BITNET STUDIO" else "LOCKED (EXCEEDS RAM)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Datasets List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(datasetsList, key = { it.repoId }) { dataset ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurface)
                                .border(
                                    1.dp,
                                    if (dataset.isUnder50kCap) TelemetryEmerald.copy(alpha = 0.5f) else AlertCrimson,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dataset.repoId,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${dataset.tokenCount} tokens · ${dataset.docCount} examples",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (dataset.isUnder50kCap) TelemetryEmerald else AlertCrimson,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${dataset.downloads / 1000}k",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dataset.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mobile Gate Status Badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (dataset.isUnder50kCap) TelemetryEmerald.copy(alpha = 0.1f) else AlertCrimson.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dataset.isUnder50kCap) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (dataset.isUnder50kCap) TelemetryEmerald else AlertCrimson,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (dataset.isUnder50kCap)
                                        "✓ UNDER 50K MOBILE BUDGET (${dataset.tokenCount} / 50k tokens)"
                                    else
                                        "⚠ EXCEEDS 50K MOBILE BUDGET (Overheating Risk)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dataset.isUnder50kCap) TelemetryEmerald else AlertCrimson
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onDatasetSelected(dataset)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Loaded ${dataset.repoId} into BitNet Studio!")
                                    }
                                },
                                enabled = dataset.isUnder50kCap,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TelemetryEmerald,
                                    contentColor = CyberDark,
                                    disabledContainerColor = CyberDark,
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (dataset.isUnder50kCap) "VALIDATE & LOAD INTO STUDIO" else "REJECTED (OVER 50K TOKENS)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }

    // Hugging Face Token Settings Dialog
    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = {
                Text(
                    text = "HUGGING FACE AUTHENTICATION",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column {
                    Text(
                        text = "Public models and datasets require zero authentication. You only need a User Access Token if accessing private or gated repositories (e.g. meta-llama).",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tokenInputText,
                        onValueChange = { tokenInputText = it },
                        placeholder = { Text("hf_...", fontSize = 12.sp, color = TextMuted) },
                        label = { Text("User Access Token", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.setUserToken(tokenInputText)
                        showTokenDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberDark
                    )
                ) {
                    Text(text = "SAVE TOKEN", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        tokenInputText = ""
                        repository.setUserToken(null)
                        showTokenDialog = false
                    }
                ) {
                    Text(text = "CLEAR / USE PUBLIC", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = CyberDark
        )
    }
}
