package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.formatBytes
import com.example.service.AiOrganizerUiState
import com.example.service.AiPhotoAnalysisResult
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.LocalCompleteUiTheme
import java.io.File
import kotlin.math.abs

@Composable
fun ToolsHubScreen(
    allPhotos: List<PhotoEntity>,
    vaultPhotos: List<PhotoEntity>,
    trashedPhotos: List<PhotoEntity>,
    isVaultUnlocked: Boolean,
    vaultPinHint: String,
    onAttemptUnlockVault: (String) -> Boolean,
    onLockVault: () -> Unit,
    onChangeVaultPin: (String) -> Unit,
    onUnhideVaultPhoto: (PhotoEntity) -> Unit,
    onRestoreTrashPhoto: (PhotoEntity) -> Unit,
    onDeletePermanently: (PhotoEntity) -> Unit,
    onEmptyTrash: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenCollageStudio: () -> Unit,
    onOpenWatermarkStudio: () -> Unit,
    onOpenEditorForPhoto: (PhotoEntity) -> Unit,
    onOpenLightboxForPhoto: (PhotoEntity) -> Unit,
    onMoveToTrash: (PhotoEntity) -> Unit,
    onCycleCompleteTheme: () -> Unit,
    onCycleLayoutStyle: () -> Unit,
    aiOrganizerUiState: AiOrganizerUiState = AiOrganizerUiState.Idle,
    aiRecentResults: List<AiPhotoAnalysisResult> = emptyList(),
    aiMoveIntoCategoryAlbums: Boolean = true,
    aiAutoTagOnImport: Boolean = true,
    onStartAiOrganizeAll: (onlyUntagged: Boolean) -> Unit = {},
    onAnalyzeSinglePhotoWithAi: (PhotoEntity) -> Unit = {},
    onCancelAiService: () -> Unit = {},
    onToggleAiMoveAlbums: () -> Unit = {},
    onToggleAiAutoImport: () -> Unit = {},
    onBatchShrinkPhotos: (ids: List<Long>, scaleFactor: Float, jpegQuality: Int) -> Unit = { _, _, _ -> },
    onInspectExifForPhoto: (PhotoEntity) -> Unit = {},
    selectedPhotoIds: Set<Long> = emptySet(),
    onToggleSelectPhoto: (Long) -> Unit = {},
    onSelectAllPhotos: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBatchCreateCollage: (List<PhotoEntity>) -> Unit = {},
    onBatchAiTag: (List<Long>) -> Unit = {},
    onBatchFavorite: (List<Long>) -> Unit = {},
    onBatchVault: (List<Long>) -> Unit = {}
) {
    val activeTheme = LocalCompleteUiTheme.current
    // 0 = Studio Launchpad, 1 = AI Smart Organizer, 2 = Storage & Duplicate Analyzer, 3 = Private Vault, 4 = Recycle Bin
    var selectedSection by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.background)
            .padding(bottom = 96.dp)
            .testTag("tools_hub_screen")
    ) {
        // Refined Studio Header & Segmented Navigation Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRO STUDIO & BATCH HUB",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "Multi-select batch studio, Darkroom grading, AI organizer & private vault",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeTheme.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (selectedPhotoIds.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = activeTheme.primaryAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onClearSelection() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = activeTheme.onPrimaryAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${selectedPhotoIds.size} Selected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = activeTheme.onPrimaryAccent
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    label = { Text("Studio Hub") },
                    leadingIcon = {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("tools_subtab_studio")
                )
                FilterChip(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    label = { Text("AI Organizer") },
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("tools_subtab_ai")
                )
                FilterChip(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    label = { Text("Storage & Duplicates") },
                    leadingIcon = {
                        Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("tools_subtab_storage")
                )
                FilterChip(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    label = { Text("Vault (${vaultPhotos.size})") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("tools_subtab_vault")
                )
                FilterChip(
                    selected = selectedSection == 4,
                    onClick = { selectedSection = 4 },
                    label = { Text("Trash (${trashedPhotos.size})") },
                    leadingIcon = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("tools_subtab_trash")
                )
            }
        }

        AnimatedContent(
            targetState = selectedSection,
            transitionSpec = {
                val forward = targetState >= initialState
                val dir = if (forward) 1 else -1
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetX = { w -> (w * 0.14f * dir).toInt() }
                ) + fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )) togetherWith (slideOutHorizontally(
                    animationSpec = tween(160),
                    targetOffsetX = { w -> -(w * 0.10f * dir).toInt() }
                ) + fadeOut(tween(150)) + scaleOut(targetScale = 0.97f, animationSpec = tween(150)))
            },
            label = "tools_section_transition",
            modifier = Modifier.fillMaxSize()
        ) { targetSection ->
            when (targetSection) {
                0 -> {
                    StudioLaunchpadContent(
                        allPhotos = allPhotos,
                        vaultPhotos = vaultPhotos,
                        activeTheme = activeTheme,
                        selectedPhotoIds = selectedPhotoIds,
                        onToggleSelectPhoto = onToggleSelectPhoto,
                        onSelectAllPhotos = onSelectAllPhotos,
                        onClearSelection = onClearSelection,
                        onBatchCreateCollage = onBatchCreateCollage,
                        onBatchAiTag = onBatchAiTag,
                        onBatchFavorite = onBatchFavorite,
                        onBatchVault = onBatchVault,
                        onOpenCompare = onOpenCompare,
                        onOpenCollageStudio = onOpenCollageStudio,
                        onOpenWatermarkStudio = onOpenWatermarkStudio,
                        onOpenEditorForPhoto = onOpenEditorForPhoto,
                        onCycleCompleteTheme = onCycleCompleteTheme,
                        onCycleLayoutStyle = onCycleLayoutStyle,
                        onSwitchToAiOrganizer = { selectedSection = 1 },
                        onSwitchToStorage = { selectedSection = 2 },
                        onSwitchToVault = { selectedSection = 3 },
                        onBatchShrinkPhotos = onBatchShrinkPhotos,
                        onInspectExifForPhoto = onInspectExifForPhoto
                    )
                }
                1 -> {
                    AiSmartOrganizerHubSection(
                        allPhotos = allPhotos,
                        aiState = aiOrganizerUiState,
                        recentResults = aiRecentResults,
                        moveIntoCategoryAlbums = aiMoveIntoCategoryAlbums,
                        autoTagOnImport = aiAutoTagOnImport,
                        activeTheme = activeTheme,
                        onStartOrganizeAll = onStartAiOrganizeAll,
                        onAnalyzeSinglePhoto = onAnalyzeSinglePhotoWithAi,
                        onCancelService = onCancelAiService,
                        onToggleMoveIntoAlbums = onToggleAiMoveAlbums,
                        onToggleAutoTagOnImport = onToggleAiAutoImport,
                        onOpenLightbox = onOpenLightboxForPhoto
                    )
                }
                2 -> {
                    StorageAndDuplicateAnalyzerSection(
                        allPhotos = allPhotos,
                        vaultPhotos = vaultPhotos,
                        trashedPhotos = trashedPhotos,
                        activeTheme = activeTheme,
                        onOpenLightbox = onOpenLightboxForPhoto,
                        onOpenCompare = onOpenCompare,
                        onMoveToTrash = onMoveToTrash
                    )
                }
                3 -> {
                    VaultScreen(
                        isUnlocked = isVaultUnlocked,
                        currentPinHint = vaultPinHint,
                        vaultPhotos = vaultPhotos,
                        onAttemptUnlock = onAttemptUnlockVault,
                        onLockVault = onLockVault,
                        onChangePin = onChangeVaultPin,
                        onPhotoClick = onOpenLightboxForPhoto,
                        onUnhidePhoto = onUnhideVaultPhoto
                    )
                }
                else -> {
                    RecycleBinScreen(
                        trashedPhotos = trashedPhotos,
                        onRestorePhoto = onRestoreTrashPhoto,
                        onDeletePermanently = onDeletePermanently,
                        onEmptyTrash = onEmptyTrash
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioLaunchpadContent(
    allPhotos: List<PhotoEntity>,
    vaultPhotos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    selectedPhotoIds: Set<Long>,
    onToggleSelectPhoto: (Long) -> Unit,
    onSelectAllPhotos: () -> Unit,
    onClearSelection: () -> Unit,
    onBatchCreateCollage: (List<PhotoEntity>) -> Unit,
    onBatchAiTag: (List<Long>) -> Unit,
    onBatchFavorite: (List<Long>) -> Unit,
    onBatchVault: (List<Long>) -> Unit,
    onOpenCompare: () -> Unit,
    onOpenCollageStudio: () -> Unit,
    onOpenWatermarkStudio: () -> Unit,
    onOpenEditorForPhoto: (PhotoEntity) -> Unit,
    onCycleCompleteTheme: () -> Unit,
    onCycleLayoutStyle: () -> Unit,
    onSwitchToAiOrganizer: () -> Unit,
    onSwitchToStorage: () -> Unit,
    onSwitchToVault: () -> Unit,
    onBatchShrinkPhotos: (ids: List<Long>, scaleFactor: Float, jpegQuality: Int) -> Unit,
    onInspectExifForPhoto: (PhotoEntity) -> Unit
) {
    val totalBytes = remember(allPhotos) { allPhotos.sumOf { it.sizeBytes } }
    val favoritesCount = remember(allPhotos) { allPhotos.count { it.isFavorite } }
    val editedCount = remember(allPhotos) { allPhotos.count { it.appliedFilter != "Original" } }
    var selectedShrinkTier by remember { mutableIntStateOf(1) } // 0 = Web 50%, 1 = Balanced 75%, 2 = Lossless 85%
    val selectedPhotos = remember(allPhotos, selectedPhotoIds) {
        allPhotos.filter { it.id in selectedPhotoIds }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Live Archive Telemetry & Quick Studio Command Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
                    .testTag("studio_telemetry_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    activeTheme.primaryAccent.copy(alpha = 0.10f),
                                    activeTheme.secondaryAccent.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ARCHIVE STUDIO OVERVIEW",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = activeTheme.primaryAccent
                            )
                            Text(
                                text = "${allPhotos.size} Active Prints • ${formatBytes(totalBytes)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = activeTheme.textPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = activeTheme.secondaryAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSwitchToStorage() }
                        ) {
                            Text(
                                text = "STORAGE: OPTIMAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = activeTheme.onSecondaryAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 4 Live Stat Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioMetricBadge(
                            label = "Favorites",
                            value = "$favoritesCount",
                            activeTheme = activeTheme,
                            modifier = Modifier.weight(1f)
                        )
                        StudioMetricBadge(
                            label = "Graded",
                            value = "$editedCount",
                            activeTheme = activeTheme,
                            modifier = Modifier.weight(1f)
                        )
                        StudioMetricBadge(
                            label = "Vaulted",
                            value = "${vaultPhotos.size}",
                            activeTheme = activeTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section Label: CREATIVE & EDITING STUDIO
        item {
            Text(
                text = "CREATIVE & EDITING STUDIO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = activeTheme.textSecondary,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }

        // Row 1: Bento Collage Maker & Date/EXIF Watermark Studio
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolFeatureCard(
                    title = "Bento Collage Maker",
                    subtitle = "Combine 2–4 photos into a custom Bento or Split story frame",
                    icon = Icons.Default.AutoAwesomeMosaic,
                    accentColor = activeTheme.primaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_collage_maker"),
                    onClick = onOpenCollageStudio
                )
                StudioToolFeatureCard(
                    title = "Watermark & Date Stamp",
                    subtitle = "Stamp custom signature & date onto prints",
                    icon = Icons.Default.BrandingWatermark,
                    accentColor = activeTheme.secondaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_watermark_studio"),
                    onClick = onOpenWatermarkStudio
                )
            }
        }

        // Row 2: Side-by-Side Compare & Darkroom Editor
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolFeatureCard(
                    title = "Compare Shots",
                    subtitle = "Split-curtain wipe & side-by-side detail comparison",
                    icon = Icons.Default.Compare,
                    accentColor = activeTheme.tertiaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_compare"),
                    onClick = onOpenCompare
                )
                StudioToolFeatureCard(
                    title = "Darkroom Studio",
                    subtitle = "Film LUTs, exposure, contrast, crop & aspect ratios",
                    icon = Icons.Default.Tune,
                    accentColor = activeTheme.primaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_darkroom"),
                    onClick = {
                        (selectedPhotos.firstOrNull() ?: allPhotos.firstOrNull())?.let { onOpenEditorForPhoto(it) }
                    }
                )
            }
        }

        // Row 2b: Featured AI Background Photo Categorizer & Auto-Tagger Card
        item {
            StudioToolFeatureCard(
                title = "AI Background Photo Organizer (Gemini)",
                subtitle = "Automatically tag & sort local photos into 'Food', 'Travel', 'Portraits', 'Nature' & more",
                icon = Icons.Default.AutoAwesome,
                accentColor = activeTheme.primaryAccent,
                activeTheme = activeTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_card_ai_organizer"),
                onClick = onSwitchToAiOrganizer
            )
        }

        // Row 2c: Batch Smart Photo Shrink & Storage Optimizer Studio Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                    .testTag("tool_card_batch_shrink")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.16f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Compress,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Smart Batch Photo Shrink & Optimizer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTheme.textPrimary
                                )
                                Text(
                                    text = "Compress high-res prints while preserving image clarity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = activeTheme.textSecondary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedShrinkTier == 0,
                            onClick = { selectedShrinkTier = 0 },
                            label = { Text("Web 50%") }
                        )
                        FilterChip(
                            selected = selectedShrinkTier == 1,
                            onClick = { selectedShrinkTier = 1 },
                            label = { Text("Balanced 75%") }
                        )
                        FilterChip(
                            selected = selectedShrinkTier == 2,
                            onClick = { selectedShrinkTier = 2 },
                            label = { Text("Lossless 85%") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val scale = when (selectedShrinkTier) {
                            0 -> 0.50f
                            1 -> 0.75f
                            else -> 1.0f
                        }
                        val quality = when (selectedShrinkTier) {
                            0 -> 68
                            1 -> 80
                            else -> 86
                        }
                        Button(
                            enabled = allPhotos.isNotEmpty(),
                            onClick = {
                                val idsToShrink = if (selectedPhotoIds.isNotEmpty()) {
                                    selectedPhotoIds.toList()
                                } else {
                                    allPhotos.sortedByDescending { it.sizeBytes }.take(3).map { it.id }
                                }
                                onBatchShrinkPhotos(idsToShrink, scale, quality)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeTheme.primaryAccent,
                                contentColor = activeTheme.onPrimaryAccent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("batch_shrink_top3_button")
                        ) {
                            Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (selectedPhotoIds.isNotEmpty()) {
                                    "Shrink Selected (${selectedPhotoIds.size})"
                                } else {
                                    "Shrink Top 3 Largest"
                                }
                            )
                        }

                        OutlinedButton(
                            enabled = allPhotos.isNotEmpty(),
                            onClick = {
                                onBatchShrinkPhotos(allPhotos.map { it.id }, scale, quality)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("batch_shrink_all_button")
                        ) {
                            Text("Optimize All (${allPhotos.size})")
                        }
                    }
                }
            }
        }

        // Section Label: ARCHIVE, GRID & PRIVACY
        item {
            Text(
                text = "ARCHIVE, GRID & PRIVACY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = activeTheme.textSecondary,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }

        // Row 3: Complete UI Theme Switcher & Photo Grid Columns Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolFeatureCard(
                    title = "UI Theme Engine",
                    subtitle = "Active: ${activeTheme.name} (${activeTheme.modeLabel})",
                    icon = Icons.Default.Palette,
                    accentColor = activeTheme.secondaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_theme_engine"),
                    onClick = onCycleCompleteTheme
                )
                StudioToolFeatureCard(
                    title = "Photo Grid Columns",
                    subtitle = "Cycle 1 Col • 2 Cols • 3 Cols • 4 Cols in Home tab",
                    icon = Icons.Default.GridView,
                    accentColor = activeTheme.tertiaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_photo_grid"),
                    onClick = onCycleLayoutStyle
                )
            }
        }

        // Row 4: Storage & Duplicates Analyzer + Private PIN Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolFeatureCard(
                    title = "Storage & Duplicates",
                    subtitle = "${allPhotos.size} items (${formatBytes(totalBytes)}) • Burst & space analyzer",
                    icon = Icons.Default.PieChart,
                    accentColor = activeTheme.primaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_storage_analyzer"),
                    onClick = onSwitchToStorage
                )
                StudioToolFeatureCard(
                    title = "Private PIN Vault",
                    subtitle = "Lock confidential media behind a 4-digit PIN",
                    icon = Icons.Default.Lock,
                    accentColor = activeTheme.secondaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_card_private_vault"),
                    onClick = onSwitchToVault
                )
            }
        }
    }
}

@Composable
private fun StudioMetricBadge(
    label: String,
    value: String,
    activeTheme: CompleteUiThemeSpec,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = activeTheme.surface.copy(alpha = 0.88f),
        modifier = modifier.border(1.dp, activeTheme.borderStroke, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = activeTheme.primaryAccent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = activeTheme.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StorageAndDuplicateAnalyzerSection(
    allPhotos: List<PhotoEntity>,
    vaultPhotos: List<PhotoEntity>,
    trashedPhotos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onOpenLightbox: (PhotoEntity) -> Unit,
    onOpenCompare: () -> Unit,
    onMoveToTrash: (PhotoEntity) -> Unit
) {
    val totalActiveBytes = remember(allPhotos) { allPhotos.sumOf { it.sizeBytes }.coerceAtLeast(1L) }
    val vaultBytes = remember(vaultPhotos) { vaultPhotos.sumOf { it.sizeBytes } }
    val trashBytes = remember(trashedPhotos) { trashedPhotos.sumOf { it.sizeBytes } }

    val albumBreakdown = remember(allPhotos) {
        allPhotos.groupBy { it.albumName }
            .map { (album, list) ->
                Triple(album, list.size, list.sumOf { it.sizeBytes })
            }
            .sortedByDescending { it.third }
    }

    // Detect burst shots or similar captures (close capture timestamps or matching dimensions in same album)
    val similarBurstGroups = remember(allPhotos) {
        val sorted = allPhotos.sortedByDescending { it.dateTaken }
        val pairs = mutableListOf<Pair<PhotoEntity, PhotoEntity>>()
        for (i in 0 until sorted.size - 1) {
            val a = sorted[i]
            val b = sorted[i + 1]
            val timeDiffMin = abs(a.dateTaken - b.dateTaken) / (1000L * 60L)
            val sameAlbumAndRes = a.albumName == b.albumName && a.width == b.width && a.height == b.height
            if (timeDiffMin <= 30L || sameAlbumAndRes) {
                pairs.add(a to b)
            }
        }
        pairs.take(6)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_analyzer_section"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Local Archive Storage Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "Public Archive: ${formatBytes(totalActiveBytes)} (${allPhotos.size} files) • Vault: ${formatBytes(vaultBytes)} • Trash: ${formatBytes(trashBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeTheme.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    albumBreakdown.forEach { (albumName, count, bytes) ->
                        val fraction = (bytes.toFloat() / totalActiveBytes.toFloat()).coerceIn(0.04f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$albumName ($count items)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTheme.textPrimary
                                )
                                Text(
                                    text = formatBytes(bytes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeTheme.primaryAccent
                                )
                            }
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = activeTheme.primaryAccent,
                                trackColor = activeTheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Burst & Similar Shot Detector Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Burst & Similar Shot Finder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = "${similarBurstGroups.size} similar / burst pairs detected for review",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                        OutlinedButton(onClick = onOpenCompare) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compare")
                        }
                    }

                    similarBurstGroups.forEach { (first, second) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = activeTheme.backgroundSecondary.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(first.filePath),
                                    contentDescription = first.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenLightbox(first) }
                                )
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(second.filePath),
                                    contentDescription = second.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenLightbox(second) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${first.title.take(16)}… & ${second.title.take(16)}…",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = activeTheme.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${first.albumName} • ${second.formattedSize}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = activeTheme.textSecondary
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onMoveToTrash(second) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Recycle duplicate",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clean", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioToolFeatureCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    activeTheme: CompleteUiThemeSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        modifier = modifier
            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = activeTheme.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = activeTheme.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoCollageMakerSheet(
    photos: List<PhotoEntity>,
    onDismiss: () -> Unit,
    onCreateCollage: (photoPaths: List<String>, title: String, layoutMode: Int, bgHex: String) -> Unit
) {
    val activeTheme = LocalCompleteUiTheme.current
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    val selectedPhotos = remember {
        mutableStateListOf<PhotoEntity>().apply {
            addAll(photos.take(3))
        }
    }
    var collageTitle by remember { mutableStateOf("Autumn Bento Story") }
    // 0 = 2-Up Split, 1 = 3-Up Bento Hero, 2 = 4-Up Quad Grid
    var layoutMode by remember { mutableIntStateOf(1) }
    var selectedBgHex by remember { mutableStateOf("#FDFBF7") }

    val frameBackgroundOptions = listOf(
        "#FDFBF7" to "Warm Ivory",
        "#FFEFF4" to "Sakura Pink",
        "#E8F5E9" to "Matcha Mint",
        "#121417" to "Darkroom Slate",
        "#FFF8E7" to "Editorial Gold"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("bento_collage_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "BENTO COLLAGE MAKER STUDIO",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = activeTheme.textPrimary
            )
            Text(
                text = "Select 2 to 4 photos to render a high-resolution Bento Keepsake Collage into your local gallery.",
                style = MaterialTheme.typography.bodySmall,
                color = activeTheme.textSecondary
            )

            // Layout Mode Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = layoutMode == 0,
                    onClick = { layoutMode = 0 },
                    label = { Text("2-Up Split") }
                )
                FilterChip(
                    selected = layoutMode == 1,
                    onClick = { layoutMode = 1 },
                    label = { Text("3-Up Bento Hero") }
                )
                FilterChip(
                    selected = layoutMode == 2,
                    onClick = { layoutMode = 2 },
                    label = { Text("4-Up Quad Grid") }
                )
            }

            // Frame Color Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(frameBackgroundOptions) { (hex, label) ->
                    FilterChip(
                        selected = selectedBgHex == hex,
                        onClick = { selectedBgHex = hex },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = collageTitle,
                onValueChange = { collageTitle = it },
                label = { Text("Collage Footer Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collage_title_input")
            )

            Text(
                text = "Tap photos to include (${selectedPhotos.size}/4 selected):",
                style = MaterialTheme.typography.labelLarge,
                color = activeTheme.textPrimary
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos, key = { "col_pick_${it.id}" }) { item ->
                    val isSelected = selectedPhotos.any { it.id == item.id }
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) activeTheme.primaryAccent else activeTheme.borderStroke,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected && selectedPhotos.size > 2) {
                                    selectedPhotos.removeAll { it.id == item.id }
                                } else if (!isSelected && selectedPhotos.size < 4) {
                                    selectedPhotos.add(item)
                                }
                            }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(item.filePath),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(activeTheme.primaryAccent, CircleShape)
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = selectedPhotos.size >= 2,
                    onClick = {
                        onCreateCollage(
                            selectedPhotos.map { it.filePath },
                            collageTitle,
                            layoutMode,
                            selectedBgHex
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryAccent,
                        contentColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("confirm_create_collage_button")
                ) {
                    Icon(Icons.Default.AutoAwesomeMosaic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Render Bento Collage")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkStudioSheet(
    photos: List<PhotoEntity>,
    onDismiss: () -> Unit,
    onCreateWatermarkedPhoto: (photo: PhotoEntity, watermarkText: String, includeDateAndExif: Boolean) -> Unit
) {
    val activeTheme = LocalCompleteUiTheme.current
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var targetPhoto by remember { mutableStateOf(photos.first()) }
    var watermarkText by remember { mutableStateOf("© Studio Archive • ${targetPhoto.albumName}") }
    var includeDateAndExif by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("watermark_studio_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "EXIF & DATE WATERMARK STUDIO",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = activeTheme.textPrimary
            )
            Text(
                text = "Stamp a custom archival signature, capture date, and camera EXIF badge onto any photo.",
                style = MaterialTheme.typography.bodySmall,
                color = activeTheme.textSecondary
            )

            // Live Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(18.dp))
            ) {
                AsyncImage(
                    model = PhotoImageProcessor.resolveCoilModel(targetPhoto.filePath),
                    contentDescription = targetPhoto.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(
                            text = watermarkText.ifBlank { "Studio Archive" },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (includeDateAndExif) {
                            Text(
                                text = "${targetPhoto.fullTimestampText} • ${targetPhoto.cameraModel} (${targetPhoto.exifSummary})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = watermarkText,
                onValueChange = { watermarkText = it },
                label = { Text("Watermark / Copyright Signature") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("watermark_text_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include Capture Date & Camera EXIF",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "${targetPhoto.cameraModel} • ${targetPhoto.exifSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeTheme.textSecondary
                    )
                }
                Switch(
                    checked = includeDateAndExif,
                    onCheckedChange = { includeDateAndExif = it }
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos, key = { "wm_pick_${it.id}" }) { item ->
                    val isSelected = item.id == targetPhoto.id
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) activeTheme.primaryAccent else activeTheme.borderStroke,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                targetPhoto = item
                            }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(item.filePath),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onCreateWatermarkedPhoto(targetPhoto, watermarkText, includeDateAndExif)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryAccent,
                        contentColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("confirm_watermark_button")
                ) {
                    Icon(Icons.Default.BrandingWatermark, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Stamped Print")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompareBottomSheet(
    photos: List<PhotoEntity>,
    onDismiss: () -> Unit,
    onOpenInEditor: (PhotoEntity) -> Unit
) {
    val activeTheme = LocalCompleteUiTheme.current
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var leftPhoto by remember { mutableStateOf(photos.first()) }
    var rightPhoto by remember {
        mutableStateOf(photos.getOrElse(1) { photos.first() })
    }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var isSideBySide by remember { mutableStateOf(false) }
    var pickingForRight by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("photo_compare_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRO ARCHIVE COMPARE STUDIO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = if (isSideBySide) "Side-by-Side Optical Diff" else "Interactive Split-Curtain Wipe",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTheme.primaryAccent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !isSideBySide,
                        onClick = { isSideBySide = false },
                        label = { Text("Curtain Wipe") }
                    )
                    FilterChip(
                        selected = isSideBySide,
                        onClick = { isSideBySide = true },
                        label = { Text("Side-by-Side") }
                    )
                }
            }

            if (!isSideBySide) {
                // Interactive Split-Curtain Comparison Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                ) {
                    // Bottom layer: Right Photo (B)
                    AsyncImage(
                        model = PhotoImageProcessor.resolveCoilModel(rightPhoto.filePath),
                        contentDescription = rightPhoto.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top clipped layer: Left Photo (A)
                    AsyncImage(
                        model = PhotoImageProcessor.resolveCoilModel(leftPhoto.filePath),
                        contentDescription = leftPhoto.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                clipRect(right = size.width * splitFraction) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    )

                    // Badges for A & B
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "A: ${leftPhoto.title.take(18)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = activeTheme.primaryAccent,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "B: ${rightPhoto.title.take(18)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.onPrimaryAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                com.example.ui.studio.PremiumStudioSlider(
                    value = splitFraction,
                    onValueChange = { splitFraction = it },
                    valueRange = 0.05f..0.95f,
                    label = "Split Curtain Position",
                    valueText = "${(splitFraction * 100).toInt()}%"
                )
            } else {
                // Side-by-Side Comparison View
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(leftPhoto.filePath),
                            contentDescription = leftPhoto.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A: ${leftPhoto.resolutionText} • ${leftPhoto.formattedSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textPrimary
                        )
                        Text(
                            text = leftPhoto.exifSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(rightPhoto.filePath),
                            contentDescription = rightPhoto.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "B: ${rightPhoto.resolutionText} • ${rightPhoto.formattedSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.primaryAccent
                        )
                        Text(
                            text = rightPhoto.exifSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }
                }
            }

            // Selector Toggle for Photo A vs Photo B
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !pickingForRight,
                        onClick = { pickingForRight = false },
                        label = { Text("Select Photo A") }
                    )
                    FilterChip(
                        selected = pickingForRight,
                        onClick = { pickingForRight = true },
                        label = { Text("Select Photo B") }
                    )
                }

                Button(
                    onClick = {
                        onOpenInEditor(if (pickingForRight) rightPhoto else leftPhoto)
                    }
                ) {
                    Text("Edit Selected")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos, key = { "cmp_${it.id}" }) { item ->
                    val isSelected = if (pickingForRight) {
                        item.id == rightPhoto.id
                    } else {
                        item.id == leftPhoto.id
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) activeTheme.primaryAccent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (pickingForRight) rightPhoto = item else leftPhoto = item
                            }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(item.filePath),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                    .padding(3.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
