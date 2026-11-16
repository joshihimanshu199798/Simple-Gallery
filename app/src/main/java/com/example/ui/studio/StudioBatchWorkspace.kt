package com.example.ui.studio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.formatBytes
import com.example.ui.editor.filters.CinematicFilterInterface
import com.example.ui.editor.filters.stepStudioFilter
import com.example.ui.theme.LocalCompleteUiTheme
import kotlin.math.roundToInt

private val GraphiteBg: Color
    @Composable get() = LocalCompleteUiTheme.current.background
private val PanelBg: Color
    @Composable get() = LocalCompleteUiTheme.current.surface
private val CardBg: Color
    @Composable get() = LocalCompleteUiTheme.current.surfaceVariant
private val BorderColor: Color
    @Composable get() = LocalCompleteUiTheme.current.borderStroke
private val NeonGreen: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent
private val OnNeonGreen: Color
    @Composable get() = LocalCompleteUiTheme.current.onPrimaryAccent
private val NeonGreenGlow: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent.copy(alpha = 0.14f)
private val TextWhite: Color
    @Composable get() = LocalCompleteUiTheme.current.textPrimary
private val TextMuted: Color
    @Composable get() = LocalCompleteUiTheme.current.textSecondary

enum class BatchActionMode(val label: String) {
    BATCH_EDIT("Batch Filter & Crop"),
    APPLY_FILTER("Apply Filter Preset"),
    COMPRESS("Batch Compress (WebP)"),
    RESIZE_50("Batch Resize Studio"),
    CONVERT_JPEG("Batch Format Converter")
}

private enum class BatchSubTab(val label: String, val icon: ImageVector) {
    FILTERS("Filters", Icons.Default.Filter),
    CROP("Crop & Ratio", Icons.Default.Crop),
    PHOTOS("Photos", Icons.Default.Layers)
}

@Composable
fun StudioBatchWorkspace(
    allPhotos: List<PhotoEntity>,
    initialMode: BatchActionMode = BatchActionMode.BATCH_EDIT,
    onClose: () -> Unit,
    onExecuteBatch: (
        targets: List<PhotoEntity>,
        mode: BatchActionMode,
        presetId: String?,
        cropRatio: CropAspectRatio,
        filterIntensity: Float,
        rotationDegrees: Int
    ) -> Unit,
    onCompressBatch: ((
        targets: List<PhotoEntity>,
        format: String,
        quality: Int,
        scalePercent: Int,
        preserveExif: Boolean
    ) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val currentMode by remember { mutableStateOf(initialMode) }

    // Multi-photo selection state
    val selectedIds = remember {
        mutableStateListOf<Long>().apply {
            addAll(allPhotos.take(6).map { it.id })
        }
    }

    // Active reference photo for live preview
    var previewPhotoId by remember {
        mutableStateOf<Long?>(allPhotos.firstOrNull()?.id)
    }

    // Batch Filter & Crop state (for BATCH_EDIT / APPLY_FILTER)
    var isFilterEnabled by remember { mutableStateOf(true) }
    var selectedFilter by remember {
        mutableStateOf<StudioFilterItem?>(CinematicFilterCatalog.ALL_PRESETS.firstOrNull())
    }
    var filterIntensity by remember { mutableFloatStateOf(0.85f) }
    var filterCategory by remember { mutableStateOf<FilterCategory?>(null) }

    var isCropEnabled by remember { mutableStateOf(true) }
    var cropRatio by remember { mutableStateOf(CropAspectRatio.ORIGINAL) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var activeTab by remember { mutableStateOf(BatchSubTab.FILTERS) }

    // Dedicated Batch Resize state (for RESIZE_50)
    var resizeScalePercent by remember { mutableIntStateOf(50) }
    var resizeQuality by remember { mutableIntStateOf(92) }

    // Dedicated Batch Format Converter state (for CONVERT_JPEG)
    var targetFormat by remember { mutableStateOf("JPEG") }
    var convertQuality by remember { mutableIntStateOf(94) }

    // Execution state
    var isProcessing by remember { mutableStateOf(false) }

    val activePreviewPhoto = remember(previewPhotoId, allPhotos) {
        allPhotos.firstOrNull { it.id == previewPhotoId } ?: allPhotos.firstOrNull()
    }

    val activeCategoryPresets = remember(filterCategory) {
        val cat = filterCategory
        if (cat == null) CinematicFilterCatalog.ALL_PRESETS
        else CinematicFilterCatalog.ALL_PRESETS.filter { it.category == cat }
    }

    // Live color matrix calculation (only applies to Filter mode)
    val liveColorMatrix = remember(selectedFilter, filterIntensity, isFilterEnabled, currentMode) {
        if (currentMode == BatchActionMode.RESIZE_50 ||
            currentMode == BatchActionMode.CONVERT_JPEG ||
            !isFilterEnabled ||
            selectedFilter == null
        ) {
            null
        } else {
            val matrix = CinematicFilterCatalog.interpolateMatrix(
                selectedFilter!!.matrixArray,
                filterIntensity
            )
            ColorMatrix(matrix)
        }
    }

    val executeCurrentBatchAction = {
        val targets = allPhotos.filter { it.id in selectedIds }
        if (targets.isNotEmpty()) {
            isProcessing = true
            when (currentMode) {
                BatchActionMode.RESIZE_50 -> {
                    if (onCompressBatch != null) {
                        onCompressBatch(targets, "JPEG", resizeQuality, resizeScalePercent, false)
                    } else {
                        onExecuteBatch(targets, BatchActionMode.RESIZE_50, null, CropAspectRatio.ORIGINAL, 1f, 0)
                    }
                }
                BatchActionMode.CONVERT_JPEG -> {
                    if (onCompressBatch != null) {
                        onCompressBatch(targets, targetFormat, convertQuality, 100, false)
                    } else {
                        onExecuteBatch(targets, BatchActionMode.CONVERT_JPEG, null, CropAspectRatio.ORIGINAL, 1f, 0)
                    }
                }
                else -> {
                    onExecuteBatch(
                        targets,
                        currentMode,
                        if (isFilterEnabled) selectedFilter?.id else null,
                        if (isCropEnabled) cropRatio else CropAspectRatio.ORIGINAL,
                        if (isFilterEnabled) filterIntensity else 1f,
                        if (isCropEnabled) rotationDegrees else 0
                    )
                }
            }
        }
    }

    val primaryCtaLabel = when (currentMode) {
        BatchActionMode.RESIZE_50 -> "Resize ${selectedIds.size} Photos ($resizeScalePercent%)"
        BatchActionMode.CONVERT_JPEG -> "Convert ${selectedIds.size} to $targetFormat"
        else -> "Apply to ${selectedIds.size} Photos"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("studio_batch_workspace")
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("batch_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = currentMode.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NeonGreen, CircleShape)
                    )
                    Text(
                        text = "${selectedIds.size} Photos Selected",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = NeonGreen
                    )
                }
            }

            val topBtnEnabled = selectedIds.isNotEmpty() && !isProcessing
            Button(
                onClick = executeCurrentBatchAction,
                enabled = topBtnEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = OnNeonGreen,
                    disabledContainerColor = CardBg,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("batch_apply_top_button")
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (topBtnEnabled) OnNeonGreen else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (currentMode) {
                        BatchActionMode.RESIZE_50 -> "Resize"
                        BatchActionMode.CONVERT_JPEG -> "Convert"
                        else -> "Apply"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (topBtnEnabled) OnNeonGreen else TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // PROCESSING INDICATOR
        AnimatedVisibility(visible = isProcessing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LinearProgressIndicator(
                    color = NeonGreen,
                    trackColor = BorderColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Processing ${selectedIds.size} photos...",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // DEDICATED MODE WORKSPACE CONTENT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (currentMode) {
                BatchActionMode.RESIZE_50 -> {
                    DedicatedBatchResizeContent(
                        allPhotos = allPhotos,
                        selectedIds = selectedIds,
                        activePreviewPhoto = activePreviewPhoto,
                        scalePercent = resizeScalePercent,
                        onScalePercentChange = { resizeScalePercent = it },
                        quality = resizeQuality,
                        onQualityChange = { resizeQuality = it },
                        onSetActivePreview = { previewPhotoId = it },
                        onToggleSelect = { id ->
                            if (selectedIds.contains(id)) selectedIds.remove(id)
                            else selectedIds.add(id)
                        },
                        onSelectAll = {
                            selectedIds.clear()
                            selectedIds.addAll(allPhotos.map { it.id })
                        },
                        onClearAll = { selectedIds.clear() }
                    )
                }

                BatchActionMode.CONVERT_JPEG -> {
                    DedicatedBatchFormatConverterContent(
                        allPhotos = allPhotos,
                        selectedIds = selectedIds,
                        activePreviewPhoto = activePreviewPhoto,
                        targetFormat = targetFormat,
                        onSelectFormat = { targetFormat = it },
                        quality = convertQuality,
                        onQualityChange = { convertQuality = it },
                        onSetActivePreview = { previewPhotoId = it },
                        onToggleSelect = { id ->
                            if (selectedIds.contains(id)) selectedIds.remove(id)
                            else selectedIds.add(id)
                        },
                        onSelectAll = {
                            selectedIds.clear()
                            selectedIds.addAll(allPhotos.map { it.id })
                        },
                        onClearAll = { selectedIds.clear() }
                    )
                }

                else -> {
                    // BATCH FILTER & CROP STUDIO (with swipe-to-change-filter and modern filter UI)
                    var accumSwipeDrag by remember { mutableFloatStateOf(0f) }

                    activePreviewPhoto?.let { photo ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PanelBg,
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val targetRatio = if (isCropEnabled && cropRatio.ratio != null) {
                                    cropRatio.ratio!!.coerceIn(0.5f, 2.5f)
                                } else {
                                    16f / 9f
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 180.dp, max = 220.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0B0C0E))
                                        .pointerInput(activeCategoryPresets, selectedFilter) {
                                            detectHorizontalDragGestures(
                                                onDragStart = { accumSwipeDrag = 0f },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    accumSwipeDrag += dragAmount
                                                    if (accumSwipeDrag <= -72f) {
                                                        accumSwipeDrag = 0f
                                                        isFilterEnabled = true
                                                        selectedFilter = stepStudioFilter(
                                                            current = selectedFilter,
                                                            presets = activeCategoryPresets,
                                                            delta = 1
                                                        )
                                                    } else if (accumSwipeDrag >= 72f) {
                                                        accumSwipeDrag = 0f
                                                        isFilterEnabled = true
                                                        selectedFilter = stepStudioFilter(
                                                            current = selectedFilter,
                                                            presets = activeCategoryPresets,
                                                            delta = -1
                                                        )
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(targetRatio, matchHeightConstraintsFirst = false)
                                            .clip(RoundedCornerShape(10.dp))
                                            .graphicsLayer {
                                                if (isCropEnabled) {
                                                    rotationZ = rotationDegrees.toFloat()
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                            contentDescription = "Batch Live Preview",
                                            contentScale = ContentScale.Crop,
                                            colorFilter = liveColorMatrix?.let { ColorFilter.colorMatrix(it) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Floating Swipe Filter HUD Pill
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "Previous filter",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .clickable {
                                                        isFilterEnabled = true
                                                        selectedFilter = stepStudioFilter(selectedFilter, activeCategoryPresets, -1)
                                                    }
                                            )
                                            Text(
                                                text = if (isFilterEnabled && selectedFilter != null) {
                                                    "${selectedFilter!!.name} • ${(filterIntensity * 100).roundToInt()}%"
                                                } else {
                                                    "Swipe to switch filter"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                ),
                                                color = if (isFilterEnabled && selectedFilter != null) NeonGreen else Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Next filter",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .clickable {
                                                        isFilterEnabled = true
                                                        selectedFilter = stepStudioFilter(selectedFilter, activeCategoryPresets, 1)
                                                    }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isFilterEnabled && selectedFilter != null) NeonGreenGlow else CardBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isFilterEnabled && selectedFilter != null) NeonGreen else BorderColor
                                        )
                                    ) {
                                        Text(
                                            text = if (isFilterEnabled && selectedFilter != null) {
                                                "LUT: ${selectedFilter!!.name} (${(filterIntensity * 100).roundToInt()}%)"
                                            } else {
                                                "LUT: Original"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isFilterEnabled && selectedFilter != null) NeonGreen else TextMuted,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isCropEnabled && cropRatio != CropAspectRatio.ORIGINAL) NeonGreenGlow else CardBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isCropEnabled && cropRatio != CropAspectRatio.ORIGINAL) NeonGreen else BorderColor
                                        )
                                    ) {
                                        Text(
                                            text = if (isCropEnabled) "Ratio: ${cropRatio.label}" else "Ratio: Original",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isCropEnabled && cropRatio != CropAspectRatio.ORIGINAL) NeonGreen else TextMuted,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isCropEnabled && rotationDegrees != 0) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = NeonGreenGlow,
                                            border = BorderStroke(1.dp, NeonGreen)
                                        ) {
                                            Text(
                                                text = "Rotate: $rotationDegrees°",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = NeonGreen,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SUB-TAB SELECTOR (Filters | Crop & Ratio | Photos)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PanelBg,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BatchSubTab.entries.forEach { tab ->
                                val isSelected = activeTab == tab
                                val pillBg by animateColorAsState(
                                    targetValue = if (isSelected) NeonGreen else CardBg,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "batch_tab_bg_${tab.name}"
                                )
                                val pillText by animateColorAsState(
                                    targetValue = if (isSelected) OnNeonGreen else TextWhite,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "batch_tab_text_${tab.name}"
                                )
                                val pillIconTint by animateColorAsState(
                                    targetValue = if (isSelected) OnNeonGreen else TextMuted,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "batch_tab_icon_${tab.name}"
                                )

                                val tabText = if (tab == BatchSubTab.PHOTOS) {
                                    "Photos (${selectedIds.size})"
                                } else {
                                    tab.label
                                }

                                Surface(
                                    shape = RoundedCornerShape(11.dp),
                                    color = pillBg,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) NeonGreen else BorderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable { activeTab = tab }
                                        .testTag("batch_tab_${tab.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            tab.icon,
                                            contentDescription = null,
                                            tint = pillIconTint,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = tabText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = pillText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            val forward = targetState.ordinal >= initialState.ordinal
                            val dir = if (forward) 1 else -1
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialOffsetX = { w -> (w * 0.14f * dir).toInt() }
                            ) + fadeIn(tween(200)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )) togetherWith (slideOutHorizontally(
                                animationSpec = tween(150),
                                targetOffsetX = { w -> -(w * 0.10f * dir).toInt() }
                            ) + fadeOut(tween(140)) + scaleOut(targetScale = 0.98f, animationSpec = tween(140)))
                        },
                        label = "batch_subtab_transition",
                        modifier = Modifier.fillMaxWidth()
                    ) { targetTab ->
                        when (targetTab) {
                            BatchSubTab.FILTERS -> {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = PanelBg,
                                    border = BorderStroke(1.dp, BorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    CinematicFilterInterface(
                                        selectedFilter = if (isFilterEnabled) selectedFilter else null,
                                        onSelectFilter = { chosen ->
                                            selectedFilter = chosen
                                            isFilterEnabled = chosen != null
                                        },
                                        filterIntensity = filterIntensity,
                                        onIntensityChange = { filterIntensity = it },
                                        previewImageSource = activePreviewPhoto?.filePath,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            BatchSubTab.CROP -> {
                                BatchCropSection(
                                    isCropEnabled = isCropEnabled,
                                    onToggleCrop = { isCropEnabled = it },
                                    selectedRatio = cropRatio,
                                    onSelectRatio = { cropRatio = it },
                                    rotationDegrees = rotationDegrees,
                                    onRotateRight = { rotationDegrees = (rotationDegrees + 90) % 360 },
                                    onResetRotation = { rotationDegrees = 0 }
                                )
                            }

                            BatchSubTab.PHOTOS -> {
                                BatchPhotoSelectionSection(
                                    allPhotos = allPhotos,
                                    selectedIds = selectedIds,
                                    activePreviewPhotoId = previewPhotoId,
                                    onToggleSelect = { id ->
                                        if (selectedIds.contains(id)) selectedIds.remove(id)
                                        else selectedIds.add(id)
                                    },
                                    onSelectAll = {
                                        selectedIds.clear()
                                        selectedIds.addAll(allPhotos.map { it.id })
                                    },
                                    onClearAll = { selectedIds.clear() },
                                    onSetActivePreview = { id -> previewPhotoId = id }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // STICKY BOTTOM ACTION BAR
        Surface(
            color = PanelBg,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentMode) {
                            BatchActionMode.RESIZE_50 -> "SCALE AT $resizeScalePercent%"
                            BatchActionMode.CONVERT_JPEG -> "EXPORT AS $targetFormat ($convertQuality%)"
                            else -> "BATCH STUDIO READY"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        ),
                        color = NeonGreen
                    )
                    Text(
                        text = "${selectedIds.size} photos queued",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite
                    )
                }

                val bottomBtnEnabled = selectedIds.isNotEmpty() && !isProcessing
                Button(
                    onClick = executeCurrentBatchAction,
                    enabled = bottomBtnEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = OnNeonGreen,
                        disabledContainerColor = CardBg,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("batch_apply_button")
                ) {
                    Icon(
                        imageVector = when (currentMode) {
                            BatchActionMode.RESIZE_50 -> Icons.Default.Crop
                            BatchActionMode.CONVERT_JPEG -> Icons.Default.Transform
                            else -> Icons.Default.AutoFixHigh
                        },
                        contentDescription = null,
                        tint = if (bottomBtnEnabled) OnNeonGreen else TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = primaryCtaLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (bottomBtnEnabled) OnNeonGreen else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DedicatedBatchResizeContent(
    allPhotos: List<PhotoEntity>,
    selectedIds: List<Long>,
    activePreviewPhoto: PhotoEntity?,
    scalePercent: Int,
    onScalePercentChange: (Int) -> Unit,
    quality: Int,
    onQualityChange: (Int) -> Unit,
    onSetActivePreview: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    val origW = activePreviewPhoto?.width?.takeIf { it > 0 } ?: 4000
    val origH = activePreviewPhoto?.height?.takeIf { it > 0 } ?: 3000
    val targetW = ((origW * scalePercent) / 100f).roundToInt().coerceAtLeast(64)
    val targetH = ((origH * scalePercent) / 100f).roundToInt().coerceAtLeast(64)

    val totalOriginalBytes = remember(selectedIds.toList(), allPhotos) {
        allPhotos.filter { it.id in selectedIds }.sumOf { it.sizeBytes.coerceAtLeast(350_000L) }
    }
    val areaRatio = (scalePercent / 100f) * (scalePercent / 100f)
    val qualityFactor = (quality / 100f) * 0.85f
    val estimatedTargetBytes = (totalOriginalBytes * areaRatio * qualityFactor).toLong().coerceAtLeast(24_000L)
    val savingsPercent = if (totalOriginalBytes > 0) {
        ((1.0 - estimatedTargetBytes.toDouble() / totalOriginalBytes.toDouble()) * 100).roundToInt().coerceIn(5, 95)
    } else {
        75
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Live Resolution Transformation Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelBg,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "RESOLUTION SCALER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            ),
                            color = TextWhite
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NeonGreenGlow,
                        border = BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "-$savingsPercent% File Size",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                activePreviewPhoto?.let { photo ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0B0C0E)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom live dimensions comparison bar
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${origW}×${origH} px",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${targetW}×${targetH} px ($scalePercent%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = NeonGreen
                                )
                            }
                        }
                    }
                }

                // Total Batch Size Before -> After
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Original: ${formatBytes(totalOriginalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Estimated Output: ~${formatBytes(estimatedTargetBytes)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGreen
                    )
                }
            }
        }

        // 2. Scale Presets & Precision Slider Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelBg,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Scale Presets",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                val presets = listOf(
                    25 to "25% Compact",
                    50 to "50% Half Size",
                    75 to "75% Balanced",
                    90 to "90% High-Res"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (pct, title) ->
                        val isSelected = scalePercent == pct
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonGreen else CardBg,
                            border = BorderStroke(1.dp, if (isSelected) NeonGreen else BorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onScalePercentChange(pct) }
                                .testTag("resize_preset_$pct")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isSelected) OnNeonGreen else TextWhite
                                )
                                Text(
                                    text = title.substringAfter(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = if (isSelected) OnNeonGreen.copy(alpha = 0.85f) else TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Target Standard Resolution Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val maxDim = maxOf(origW, origH).coerceAtLeast(1000)
                    val resTargets = listOf(
                        "720p HD" to ((1280f / maxDim) * 100f).roundToInt().coerceIn(15, 100),
                        "1080p FHD" to ((1920f / maxDim) * 100f).roundToInt().coerceIn(25, 100),
                        "2K QHD" to ((2560f / maxDim) * 100f).roundToInt().coerceIn(35, 100),
                        "Social 1080px" to ((1080f / maxDim) * 100f).roundToInt().coerceIn(15, 100)
                    )
                    resTargets.forEach { (label, computedScale) ->
                        val isMatched = kotlin.math.abs(scalePercent - computedScale) <= 2
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isMatched) NeonGreenGlow else CardBg,
                            border = BorderStroke(1.dp, if (isMatched) NeonGreen else BorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onScalePercentChange(computedScale) }
                        ) {
                            Text(
                                text = "$label ($computedScale%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isMatched) NeonGreen else TextWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Custom Scale Slider
                PremiumStudioSlider(
                    value = scalePercent.toFloat(),
                    onValueChange = { onScalePercentChange(it.roundToInt().coerceIn(10, 100)) },
                    valueRange = 10f..100f,
                    label = "Custom Resolution Scale",
                    valueText = "$scalePercent% (${targetW}×${targetH})"
                )

                // Output Sharpness / Quality Slider
                PremiumStudioSlider(
                    value = quality.toFloat(),
                    onValueChange = { onQualityChange(it.roundToInt().coerceIn(50, 100)) },
                    valueRange = 50f..100f,
                    label = "JPEG Encoding Quality",
                    valueText = "$quality%"
                )
            }
        }

        // 3. Multi-Photo Target Selector
        BatchPhotoSelectionSection(
            allPhotos = allPhotos,
            selectedIds = selectedIds,
            activePreviewPhotoId = activePreviewPhoto?.id,
            onToggleSelect = onToggleSelect,
            onSelectAll = onSelectAll,
            onClearAll = onClearAll,
            onSetActivePreview = onSetActivePreview
        )
    }
}

@Composable
private fun DedicatedBatchFormatConverterContent(
    allPhotos: List<PhotoEntity>,
    selectedIds: List<Long>,
    activePreviewPhoto: PhotoEntity?,
    targetFormat: String,
    onSelectFormat: (String) -> Unit,
    quality: Int,
    onQualityChange: (Int) -> Unit,
    onSetActivePreview: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    val sourceExt = remember(activePreviewPhoto) {
        val pathExt = activePreviewPhoto?.filePath?.substringAfterLast('.', "")?.uppercase() ?: ""
        when {
            pathExt.isNotBlank() && pathExt.length <= 5 -> pathExt
            activePreviewPhoto?.mimeType?.contains("png", true) == true -> "PNG"
            activePreviewPhoto?.mimeType?.contains("webp", true) == true -> "WEBP"
            activePreviewPhoto?.mimeType?.contains("heic", true) == true -> "HEIC"
            else -> "RAW/JPG"
        }
    }

    val formatSpecs = listOf(
        Triple("JPEG", "Universal Standard (.jpg)", "Best compatibility across all cameras, web, and print apps"),
        Triple("WEBP", "Modern High-Efficiency (.webp)", "Up to 35% smaller file size with crisp visual fidelity"),
        Triple("PNG", "Lossless Studio Archive (.png)", "Zero compression artifacts, ideal for graphics & edits")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Live Format Conversion Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelBg,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "BATCH FORMAT CONVERTER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            ),
                            color = TextWhite
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NeonGreenGlow,
                        border = BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "$sourceExt → $targetFormat",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = NeonGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                activePreviewPhoto?.let { photo ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0B0C0E)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 2. Target Format Selector Cards
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelBg,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Output Container Format",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                formatSpecs.forEach { (fmt, title, subtitle) ->
                    val isSelected = targetFormat == fmt
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) NeonGreenGlow else CardBg,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) NeonGreen else BorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectFormat(fmt) }
                            .testTag("format_option_${fmt.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) NeonGreen else PanelBg
                                    ) {
                                        Text(
                                            text = fmt,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isSelected) OnNeonGreen else TextWhite,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(NeonGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = OnNeonGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Encoding Quality Slider (applicable to lossy/hybrid formats)
                if (targetFormat != "PNG") {
                    PremiumStudioSlider(
                        value = quality.toFloat(),
                        onValueChange = { onQualityChange(it.roundToInt().coerceIn(50, 100)) },
                        valueRange = 50f..100f,
                        label = "$targetFormat Encoding Quality",
                        valueText = "$quality%"
                    )
                }
            }
        }

        // 3. Multi-Photo Target Selector
        BatchPhotoSelectionSection(
            allPhotos = allPhotos,
            selectedIds = selectedIds,
            activePreviewPhotoId = activePreviewPhoto?.id,
            onToggleSelect = onToggleSelect,
            onSelectAll = onSelectAll,
            onClearAll = onClearAll,
            onSetActivePreview = onSetActivePreview
        )
    }
}

@Composable
private fun BatchCropSection(
    isCropEnabled: Boolean,
    onToggleCrop: (Boolean) -> Unit,
    selectedRatio: CropAspectRatio,
    onSelectRatio: (CropAspectRatio) -> Unit,
    rotationDegrees: Int,
    onRotateRight: () -> Unit,
    onResetRotation: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PanelBg,
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Uniform Crop & Orientation",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "Standardize aspect ratio and rotation across all selected photos",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = isCropEnabled,
                    onCheckedChange = onToggleCrop,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnNeonGreen,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CardBg
                    )
                )
            }

            if (isCropEnabled) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(CropAspectRatio.entries) { ratio ->
                        val isSelected = selectedRatio == ratio
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonGreen else CardBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonGreen else BorderColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectRatio(ratio) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = if (isSelected) OnNeonGreen else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) OnNeonGreen else TextWhite
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardBg,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onRotateRight)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rotate +90° ($rotationDegrees°)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                    }

                    if (rotationDegrees != 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onResetRotation)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reset Rotation",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "0°",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchPhotoSelectionSection(
    allPhotos: List<PhotoEntity>,
    selectedIds: List<Long>,
    activePreviewPhotoId: Long?,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onSetActivePreview: (Long) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PanelBg,
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Target Photos (${selectedIds.size}/${allPhotos.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "Tap to toggle selection • Long-tap or tap eye to preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardBg,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onSelectAll)
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardBg,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onClearAll)
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(allPhotos, key = { it.id }) { photo ->
                    val isSelected = selectedIds.contains(photo.id)
                    val isPreview = activePreviewPhotoId == photo.id

                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = when {
                                    isSelected -> NeonGreen
                                    isPreview -> Color.White.copy(alpha = 0.6f)
                                    else -> BorderColor
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSetActivePreview(photo.id)
                                onToggleSelect(photo.id)
                            }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Selection Checkmark Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(20.dp)
                                .background(
                                    if (isSelected) NeonGreen else Color.Black.copy(alpha = 0.55f),
                                    CircleShape
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = OnNeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
