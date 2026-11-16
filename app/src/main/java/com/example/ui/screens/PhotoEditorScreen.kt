package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.example.ui.studio.PremiumStudioSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.data.PhotoFilterPreset
import com.example.data.PhotoImageProcessor
import java.io.File
import java.util.Locale

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

@Composable
fun PhotoEditorScreen(
    photo: PhotoEntity,
    initialTab: Int = 0,
    onCancel: () -> Unit,
    onSaveEdits: (
        preset: PhotoFilterPreset,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        rotationDegrees: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRatio: CropAspectRatio,
        saveAsCopy: Boolean,
        customCropBounds: RectF?
    ) -> Unit
) {
    BackHandler { onCancel() }

    var selectedTab by remember(photo.id, initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var selectedPreset by remember { mutableStateOf(PhotoFilterPreset.ORIGINAL) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }
    var selectedCropRatio by remember { mutableStateOf(CropAspectRatio.ORIGINAL) }
    var showOriginalCompare by remember { mutableStateOf(false) }
    var showDarkroomExifBar by remember { mutableStateOf(true) }

    // Normalized interactive crop rectangle [0f..1f]
    var cropLeft by remember(photo.id) { mutableFloatStateOf(0.0f) }
    var cropTop by remember(photo.id) { mutableFloatStateOf(0.0f) }
    var cropRight by remember(photo.id) { mutableFloatStateOf(1.0f) }
    var cropBottom by remember(photo.id) { mutableFloatStateOf(1.0f) }
    var previewBoxSize by remember { mutableStateOf(IntSize.Zero) }

    val isCustomCropped = cropLeft > 0.01f || cropTop > 0.01f || cropRight < 0.99f || cropBottom < 0.99f
    val croppedPixelW = ((cropRight - cropLeft) * photo.width).toInt().coerceAtLeast(16)
    val croppedPixelH = ((cropBottom - cropTop) * photo.height).toInt().coerceAtLeast(16)

    fun applyAspectRatioToCropBox(ratio: CropAspectRatio) {
        selectedCropRatio = ratio
        val r = ratio.ratio
        if (r == null) {
            // Freeform mode keeps current bounds or initializes a neat 88% frame if untouched
            if (!isCustomCropped) {
                cropLeft = 0.06f
                cropTop = 0.06f
                cropRight = 0.94f
                cropBottom = 0.94f
            }
            return
        }
        val imgRatio = photo.width.toFloat().coerceAtLeast(1f) / photo.height.toFloat().coerceAtLeast(1f)
        val normalizedRatio = r / imgRatio
        val maxBoxW: Float
        val maxBoxH: Float
        if (normalizedRatio >= 1f) {
            maxBoxW = 0.90f
            maxBoxH = (0.90f / normalizedRatio).coerceIn(0.20f, 0.90f)
        } else {
            maxBoxH = 0.90f
            maxBoxW = (0.90f * normalizedRatio).coerceIn(0.20f, 0.90f)
        }
        cropLeft = ((1f - maxBoxW) / 2f).coerceIn(0f, 0.85f)
        cropTop = ((1f - maxBoxH) / 2f).coerceIn(0f, 0.85f)
        cropRight = (cropLeft + maxBoxW).coerceIn(0.15f, 1f)
        cropBottom = (cropTop + maxBoxH).coerceIn(0.15f, 1f)
    }

    val composeColorMatrix = remember(selectedPreset, brightness, contrast, saturation) {
        val androidMatrix = PhotoImageProcessor.buildCombinedColorMatrix(
            preset = selectedPreset,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation
        )
        ColorMatrix(androidMatrix.array)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0C0A))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("photo_editor_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag("editor_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel editing",
                    tint = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (selectedTab == 2) "Interactive Crop Studio" else "Darkroom Studio",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (showOriginalCompare) {
                        "Viewing Unedited Original"
                    } else if (selectedTab == 2 || isCustomCropped) {
                        "${selectedCropRatio.label} • ${croppedPixelW}×${croppedPixelH} px"
                    } else {
                        "${selectedPreset.displayName} • ${selectedCropRatio.label}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Before / After Compare Toggle Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (showOriginalCompare) {
                        Color(0xFFF5B700)
                    } else {
                        Color.White.copy(alpha = 0.14f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { showOriginalCompare = !showOriginalCompare }
                        .testTag("editor_compare_original_button")
                ) {
                    Text(
                        text = if (showOriginalCompare) "Original" else "Compare",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (showOriginalCompare) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = {
                        selectedPreset = PhotoFilterPreset.ORIGINAL
                        brightness = 0f
                        contrast = 1f
                        saturation = 1f
                        rotationDegrees = 0
                        flipHorizontal = false
                        flipVertical = false
                        selectedCropRatio = CropAspectRatio.ORIGINAL
                        cropLeft = 0f
                        cropTop = 0f
                        cropRight = 1f
                        cropBottom = 1f
                        showOriginalCompare = false
                    },
                    modifier = Modifier.testTag("editor_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset all adjustments",
                        tint = Color.White
                    )
                }
            }
        }

        // Live Interactive Photo Preview + Interactive Draggable Crop Frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            val imageAspectRatio = (photo.width.toFloat().coerceAtLeast(1f) /
                photo.height.toFloat().coerceAtLeast(1f)).coerceIn(0.4f, 2.4f)

            Box(
                modifier = Modifier
                    .aspectRatio(
                        ratio = imageAspectRatio,
                        matchHeightConstraintsFirst = imageAspectRatio < 1f
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .onSizeChanged { previewBoxSize = it }
            ) {
                val animatedRotationZ by animateFloatAsState(
                    targetValue = if (showOriginalCompare) 0f else rotationDegrees.toFloat(),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "editorRotationZ"
                )
                val animatedFlipX by animateFloatAsState(
                    targetValue = if (!showOriginalCompare && flipHorizontal) -1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "editorFlipX"
                )
                val animatedFlipY by animateFloatAsState(
                    targetValue = if (!showOriginalCompare && flipVertical) -1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "editorFlipY"
                )
                AsyncImage(
                    model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                    contentDescription = photo.title,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = if (showOriginalCompare) null else ColorFilter.colorMatrix(composeColorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            rotationZ = animatedRotationZ,
                            scaleX = animatedFlipX,
                            scaleY = animatedFlipY
                        )
                )

                // Interactive Crop Frame Overlay (Always active on Tab 2 or when custom crop is active)
                if ((selectedTab == 2 || isCustomCropped) && !showOriginalCompare) {
                    val accentGold = Color(0xFFF5B400)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("interactive_crop_canvas")
                            .pointerInput(selectedCropRatio, previewBoxSize) {
                                var dragMode = 0 // 0=none, 1=TL, 2=TR, 3=BL, 4=BR, 5=Move
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        val w = size.width.toFloat().coerceAtLeast(1f)
                                        val h = size.height.toFloat().coerceAtLeast(1f)
                                        val nx = startOffset.x / w
                                        val ny = startOffset.y / h
                                        val hitRadius = 0.12f
                                        dragMode = when {
                                            abs(nx - cropLeft) < hitRadius && abs(ny - cropTop) < hitRadius -> 1
                                            abs(nx - cropRight) < hitRadius && abs(ny - cropTop) < hitRadius -> 2
                                            abs(nx - cropLeft) < hitRadius && abs(ny - cropBottom) < hitRadius -> 3
                                            abs(nx - cropRight) < hitRadius && abs(ny - cropBottom) < hitRadius -> 4
                                            nx in cropLeft..cropRight && ny in cropTop..cropBottom -> 5
                                            else -> 5
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val w = size.width.toFloat().coerceAtLeast(1f)
                                        val h = size.height.toFloat().coerceAtLeast(1f)
                                        val dx = dragAmount.x / w
                                        val dy = dragAmount.y / h
                                        val minSpan = 0.18f

                                        when (dragMode) {
                                            1 -> {
                                                cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                            }
                                            2 -> {
                                                cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                            }
                                            3 -> {
                                                cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                            }
                                            4 -> {
                                                cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                            }
                                            5 -> {
                                                val boxW = cropRight - cropLeft
                                                val boxH = cropBottom - cropTop
                                                val newLeft = (cropLeft + dx).coerceIn(0f, 1f - boxW)
                                                val newTop = (cropTop + dy).coerceIn(0f, 1f - boxH)
                                                cropLeft = newLeft
                                                cropRight = newLeft + boxW
                                                cropTop = newTop
                                                cropBottom = newTop + boxH
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val leftPx = cropLeft * w
                        val topPx = cropTop * h
                        val rightPx = cropRight * w
                        val bottomPx = cropBottom * h
                        val boxW = (rightPx - leftPx).coerceAtLeast(1f)
                        val boxH = (bottomPx - topPx).coerceAtLeast(1f)

                        // Darkened outer scrim around the crop rectangle
                        val scrimColor = Color.Black.copy(alpha = 0.58f)
                        drawRect(color = scrimColor, topLeft = Offset(0f, 0f), size = Size(w, topPx))
                        drawRect(color = scrimColor, topLeft = Offset(0f, bottomPx), size = Size(w, h - bottomPx))
                        drawRect(color = scrimColor, topLeft = Offset(0f, topPx), size = Size(leftPx, boxH))
                        drawRect(color = scrimColor, topLeft = Offset(rightPx, topPx), size = Size(w - rightPx, boxH))

                        // Rule-of-Thirds grid lines inside crop frame
                        val gridColor = Color.White.copy(alpha = 0.42f)
                        for (i in 1..2) {
                            val gx = leftPx + (boxW * i / 3f)
                            val gy = topPx + (boxH * i / 3f)
                            drawLine(gridColor, Offset(gx, topPx), Offset(gx, bottomPx), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(leftPx, gy), Offset(rightPx, gy), strokeWidth = 1.dp.toPx())
                        }

                        // Outer crop frame border
                        drawRect(
                            color = accentGold,
                            topLeft = Offset(leftPx, topPx),
                            size = Size(boxW, boxH),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 4 Heavy Corner Handles
                        val cornerLen = 18.dp.toPx()
                        val cornerStroke = 4.dp.toPx()
                        // Top-Left
                        drawLine(accentGold, Offset(leftPx, topPx), Offset(leftPx + cornerLen, topPx), cornerStroke)
                        drawLine(accentGold, Offset(leftPx, topPx), Offset(leftPx, topPx + cornerLen), cornerStroke)
                        // Top-Right
                        drawLine(accentGold, Offset(rightPx, topPx), Offset(rightPx - cornerLen, topPx), cornerStroke)
                        drawLine(accentGold, Offset(rightPx, topPx), Offset(rightPx, topPx + cornerLen), cornerStroke)
                        // Bottom-Left
                        drawLine(accentGold, Offset(leftPx, bottomPx), Offset(leftPx + cornerLen, bottomPx), cornerStroke)
                        drawLine(accentGold, Offset(leftPx, bottomPx), Offset(leftPx, bottomPx - cornerLen), cornerStroke)
                        // Bottom-Right
                        drawLine(accentGold, Offset(rightPx, bottomPx), Offset(rightPx - cornerLen, bottomPx), cornerStroke)
                        drawLine(accentGold, Offset(rightPx, bottomPx), Offset(rightPx, bottomPx - cornerLen), cornerStroke)
                    }

                    // Live Crop Resolution Badge inside top of crop frame
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "DRAG CORNERS TO CROP • ${croppedPixelW}×${croppedPixelH}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFF5B400),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Editor Mode Tabs: 0 = Filters, 1 = Light & Color, 2 = Crop & Rotate
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Film Filters") },
                        modifier = Modifier.testTag("editor_tab_filters")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Light & Tone") },
                        modifier = Modifier.testTag("editor_tab_adjust")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            if (!isCustomCropped && selectedCropRatio == CropAspectRatio.ORIGINAL) {
                                cropLeft = 0.06f
                                cropTop = 0.06f
                                cropRight = 0.94f
                                cropBottom = 0.94f
                            }
                        },
                        text = { Text("Crop & Rotate") },
                        modifier = Modifier.testTag("editor_tab_crop")
                    )
                }

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> direction * (width / 5) } + fadeIn(
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        )) togetherWith (slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> -direction * (width / 5) } + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        ))
                    },
                    label = "editorTabContentTransition"
                ) { activeTab ->
                when (activeTab) {
                    0 -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(PhotoFilterPreset.entries, key = { it.name }) { preset ->
                                val isSelected = preset == selectedPreset
                                val presetScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.04f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "presetScale"
                                )
                                val presetBg by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "presetBg"
                                )
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = presetBg,
                                    modifier = Modifier
                                        .animateItem()
                                        .graphicsLayer {
                                            scaleX = presetScale
                                            scaleY = presetScale
                                        }
                                        .width(128.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedPreset = preset }
                                        .padding(12.dp)
                                        .testTag("preset_chip_${preset.name.lowercase()}")
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = preset.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = preset.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AdjustmentSliderRow(
                                label = "Exposure",
                                value = brightness,
                                valueRange = -0.35f..0.35f,
                                formatted = String.format(Locale.US, "%+.0f%%", brightness * 100),
                                onValueChange = { brightness = it }
                            )
                            AdjustmentSliderRow(
                                label = "Contrast",
                                value = contrast,
                                valueRange = 0.6f..1.5f,
                                formatted = String.format(Locale.US, "%.0f%%", contrast * 100),
                                onValueChange = { contrast = it }
                            )
                            AdjustmentSliderRow(
                                label = "Saturation",
                                value = saturation,
                                valueRange = 0f..1.8f,
                                formatted = String.format(Locale.US, "%.0f%%", saturation * 100),
                                onValueChange = { saturation = it }
                            )
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Rotate, Flip & Quick Crop Zoom Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { rotationDegrees = (rotationDegrees - 90) % 360 },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("rotate_left_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("-90°", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick = { rotationDegrees = (rotationDegrees + 90) % 360 },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("rotate_right_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+90°", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick = { flipHorizontal = !flipHorizontal },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flip_h_button")
                                ) {
                                    Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Flip H", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick = {
                                        // Tighter center crop step
                                        val step = 0.06f
                                        if (cropRight - cropLeft > 0.28f && cropBottom - cropTop > 0.28f) {
                                            cropLeft = (cropLeft + step).coerceAtMost(0.4f)
                                            cropTop = (cropTop + step).coerceAtMost(0.4f)
                                            cropRight = (cropRight - step).coerceAtLeast(0.6f)
                                            cropBottom = (cropBottom - step).coerceAtLeast(0.6f)
                                        } else {
                                            cropLeft = 0f
                                            cropTop = 0f
                                            cropRight = 1f
                                            cropBottom = 1f
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("crop_zoom_step_button")
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Inset", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            // Row 2: Aspect Ratio Presets + Full Reset
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CropAspectRatio.entries) { ratio ->
                                    FilterChip(
                                        selected = selectedCropRatio == ratio,
                                        onClick = { applyAspectRatioToCropBox(ratio) },
                                        label = { Text(ratio.label) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Crop,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.testTag("crop_ratio_${ratio.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }
                }

                // Bottom Save Copy / Overwrite Buttons
                val finalCropRect = if (isCustomCropped) {
                    RectF(cropLeft, cropTop, cropRight, cropBottom)
                } else {
                    null
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSaveEdits(
                                selectedPreset,
                                brightness,
                                contrast,
                                saturation,
                                rotationDegrees,
                                flipHorizontal,
                                flipVertical,
                                selectedCropRatio,
                                false,
                                finalCropRect
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_overwrite_button")
                    ) {
                        Text(if (selectedTab == 2) "Apply Crop" else "Overwrite Original")
                    }

                    Button(
                        onClick = {
                            onSaveEdits(
                                selectedPreset,
                                brightness,
                                contrast,
                                saturation,
                                rotationDegrees,
                                flipHorizontal,
                                flipVertical,
                                selectedCropRatio,
                                true,
                                finalCropRect
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_save_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedTab == 2) "Save Cropped Copy" else "Save as Copy")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formatted: String,
    onValueChange: (Float) -> Unit
) {
    PremiumStudioSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        label = label,
        valueText = formatted
    )
}
