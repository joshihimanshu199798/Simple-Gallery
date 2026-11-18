package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.DarkroomCanvasBackground
import com.example.ui.theme.DarkroomCardSurface
import com.example.ui.theme.DarkroomControlSurface
import com.example.ui.theme.DarkroomCoralAccent
import com.example.ui.theme.DarkroomOnCoral
import com.example.ui.theme.DarkroomOverlayScrim
import com.example.ui.theme.DarkroomReadyGreen
import com.example.ui.theme.DarkroomSubtleBorder
import com.example.ui.theme.DarkroomTextPrimary
import com.example.ui.theme.DarkroomTextSecondary
import com.example.ui.theme.DarkroomTrackInactive
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Minimal Darkroom & Color top bar: Cancel (left), Ready status pill (center), Save Copy (right).
 */
@Composable
fun DarkroomHeaderBar(
    onCancel: () -> Unit,
    onSaveCopy: () -> Unit,
    onStatusPillClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Prominent Cancel button
        Surface(
            shape = RoundedCornerShape(50),
            color = DarkroomCardSurface,
            border = BorderStroke(1.dp, DarkroomSubtleBorder),
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onCancel)
                .testTag("editor_cancel_btn")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = stringResource(R.string.darkroom_cancel),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = DarkroomTextPrimary
                )
            }
        }

        // Center: "Ready" status pill
        Surface(
            shape = RoundedCornerShape(50),
            color = DarkroomCardSurface,
            border = BorderStroke(1.dp, DarkroomSubtleBorder),
            modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .clip(RoundedCornerShape(50))
                .then(
                    if (onStatusPillClick != null) {
                        Modifier.clickable(onClick = onStatusPillClick)
                    } else {
                        Modifier
                    }
                )
                .testTag("editor_status_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(DarkroomReadyGreen)
                )
                Text(
                    text = stringResource(R.string.darkroom_ready),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = DarkroomTextPrimary
                )
            }
        }

        // Right: Coral "Save Copy" button
        Button(
            onClick = onSaveCopy,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkroomCoralAccent,
                contentColor = DarkroomOnCoral
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            ),
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                .testTag("editor_save_copy_btn")
        ) {
            Text(
                text = stringResource(R.string.darkroom_save_copy),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = DarkroomOnCoral
            )
        }
    }
}

/**
 * Horizontal workflow filmstrip of compact, rounded-square tool icons
 * (Exposure, Crop, Color Balance, Film LUTs, Effects) positioned below the top bar.
 */
@Composable
fun DarkroomWorkflowFilmstrip(
    currentTab: EditorToolTab,
    modifiedTabs: Set<EditorToolTab>,
    onSelectTab: (EditorToolTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedTools = remember {
        listOf(
            EditorToolTab.LIGHT,
            EditorToolTab.CROP,
            EditorToolTab.COLOR,
            EditorToolTab.FILTERS,
            EditorToolTab.DETAIL
        )
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("darkroom_workflow_filmstrip"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(orderedTools, key = { _, tab -> tab.name }) { _, tab ->
            val isSelected = currentTab == tab
            val hasEdits = tab in modifiedTabs

            val toolIcon: ImageVector = when (tab) {
                EditorToolTab.LIGHT -> Icons.Default.Tune
                EditorToolTab.CROP -> Icons.Default.Crop
                EditorToolTab.COLOR -> Icons.Default.Palette
                EditorToolTab.FILTERS -> Icons.Default.Movie
                EditorToolTab.DETAIL -> Icons.Default.FilterVintage
            }
            val toolLabel = when (tab) {
                EditorToolTab.LIGHT -> stringResource(R.string.darkroom_tool_exposure)
                EditorToolTab.CROP -> stringResource(R.string.darkroom_tool_crop)
                EditorToolTab.COLOR -> stringResource(R.string.darkroom_tool_color_balance)
                EditorToolTab.FILTERS -> stringResource(R.string.darkroom_tool_film_luts)
                EditorToolTab.DETAIL -> stringResource(R.string.darkroom_tool_effects)
            }

            val tileScale by animateFloatAsState(
                targetValue = if (isSelected) 1.03f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "workflow_tile_scale_${tab.name}"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) DarkroomCoralAccent else DarkroomSubtleBorder,
                animationSpec = tween(180),
                label = "workflow_tile_border_${tab.name}"
            )
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    DarkroomCoralAccent.copy(alpha = 0.14f)
                } else {
                    DarkroomCardSurface
                },
                animationSpec = tween(180),
                label = "workflow_tile_bg_${tab.name}"
            )
            val iconTint by animateColorAsState(
                targetValue = if (isSelected) DarkroomCoralAccent else DarkroomTextSecondary,
                animationSpec = tween(180),
                label = "workflow_tile_icon_${tab.name}"
            )
            val labelTint by animateColorAsState(
                targetValue = if (isSelected) DarkroomTextPrimary else DarkroomTextSecondary,
                animationSpec = tween(180),
                label = "workflow_tile_label_${tab.name}"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .width(70.dp)
                    .scale(tileScale)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 2.dp)
                    .testTag("editor_tab_${tab.name.lowercase()}")
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(containerColor)
                        .border(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = toolIcon,
                        contentDescription = toolLabel,
                        tint = iconTint,
                        modifier = Modifier.size(21.dp)
                    )
                    if (hasEdits) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(DarkroomCoralAccent)
                        )
                    }
                }
                Text(
                    text = toolLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = labelTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Interactive Darkroom Main Canvas with elevated #18181C frame and draggable
 * Before (ORIGINAL, left) / After (EDITED, right) split-view slider handle.
 */
@Composable
fun DarkroomBeforeAfterCanvas(
    photo: PhotoEntity,
    liveColorMatrix: ColorMatrix,
    cropRatio: CropAspectRatio,
    rotationDegrees: Int,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    keystoneVertical: Float,
    keystoneHorizontal: Float,
    vignette: Float,
    grain: Float,
    selectedFramePreset: FrameBorderPreset,
    showCropGrid: Boolean,
    splitPosition: Float,
    onSplitPositionChange: (Float) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    activeFilterName: String?,
    onSwitchPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var imageStageSize by remember { mutableStateOf(IntSize.Zero) }
    var isDraggingSplit by remember { mutableStateOf(false) }

    val handleScale by animateFloatAsState(
        targetValue = if (isDraggingSplit) 1.14f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "split_handle_scale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DarkroomCardSurface,
        border = BorderStroke(1.dp, DarkroomSubtleBorder),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("darkroom_canvas_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF222228),
                            DarkroomCardSurface,
                            Color(0xFF131317)
                        )
                    )
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val aspect = when {
                cropRatio.ratio != null -> cropRatio.ratio!!
                photo.height > 0 -> (photo.width.toFloat() / photo.height.toFloat()).coerceIn(0.45f, 2.4f)
                else -> 1f
            }

            val framePadDp = (selectedFramePreset.padRatio * 150f).dp
            val bottomFramePadDp = framePadDp * selectedFramePreset.bottomWeight

            val editorPreviewModel = remember(photo.id, photo.filePath) {
                PhotoImageProcessor.buildPreviewRequest(context, photo.filePath, 1600)
            }
            val contentScale = if (cropRatio != CropAspectRatio.ORIGINAL) {
                ContentScale.Crop
            } else {
                ContentScale.Fit
            }

            // Interactive Photo Stage with Before/After Split Mask
            Box(
                modifier = Modifier
                    .aspectRatio(aspect, matchHeightConstraintsFirst = aspect < 1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkroomCanvasBackground)
                    .onSizeChanged { imageStageSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSplitPositionChange(0.5f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDraggingSplit = true
                            },
                            onDragEnd = {
                                isDraggingSplit = false
                            },
                            onDragCancel = {
                                isDraggingSplit = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val w = imageStageSize.width.toFloat().coerceAtLeast(1f)
                                val prev = splitPosition
                                val next = (prev + dragAmount / w).coerceIn(0.05f, 0.95f)
                                if ((prev < 0.5f && next >= 0.5f) || (prev > 0.5f && next <= 0.5f)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSplitPositionChange(next)
                            }
                        )
                    }
                    .testTag("darkroom_before_after_stage")
            ) {
                // 1. RIGHT / BASE LAYER: Edited Photo (ColorMatrix + Vignette + Film Grain)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotationDegrees.toFloat()
                            rotationX = keystoneVertical
                            rotationY = keystoneHorizontal
                            cameraDistance = 14f * this.density
                            scaleX = if (flipHorizontal) -1f else 1f
                            scaleY = if (flipVertical) -1f else 1f
                        }
                        .background(selectedFramePreset.frameColor)
                        .padding(
                            start = framePadDp,
                            top = framePadDp,
                            end = framePadDp,
                            bottom = bottomFramePadDp
                        )
                ) {
                    AsyncImage(
                        model = editorPreviewModel,
                        contentDescription = photo.title,
                        contentScale = contentScale,
                        colorFilter = ColorFilter.colorMatrix(liveColorMatrix),
                        modifier = Modifier.fillMaxSize()
                    )

                    if (vignette > 0f || grain > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (vignette > 0f) {
                                val maxRadius = size.maxDimension * 0.75f
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(
                                                alpha = (vignette / 100f * 0.85f).coerceIn(0f, 0.88f)
                                            )
                                        ),
                                        center = center,
                                        radius = maxRadius.coerceAtLeast(1f)
                                    )
                                )
                            }
                            if (grain > 0f) {
                                val alpha = (grain / 100f * 0.18f).coerceIn(0f, 0.25f)
                                val step = 14.dp.toPx().coerceAtLeast(6f)
                                var y = 0f
                                var row = 0
                                while (y < size.height) {
                                    var x = if (row % 2 == 0) 0f else step / 2f
                                    while (x < size.width) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = alpha),
                                            radius = 1.2f,
                                            center = Offset(x, y)
                                        )
                                        x += step
                                    }
                                    y += step
                                    row++
                                }
                            }
                        }
                    }
                }

                // 2. LEFT CLIPPED LAYER: Original Unedited Photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val clipRightPx = (size.width * splitPosition).coerceIn(0f, size.width)
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = clipRightPx,
                                bottom = size.height
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        }
                        .graphicsLayer {
                            rotationZ = rotationDegrees.toFloat()
                            rotationX = keystoneVertical
                            rotationY = keystoneHorizontal
                            cameraDistance = 14f * this.density
                            scaleX = if (flipHorizontal) -1f else 1f
                            scaleY = if (flipVertical) -1f else 1f
                        }
                        .background(selectedFramePreset.frameColor)
                        .padding(
                            start = framePadDp,
                            top = framePadDp,
                            end = framePadDp,
                            bottom = bottomFramePadDp
                        )
                ) {
                    AsyncImage(
                        model = editorPreviewModel,
                        contentDescription = "${photo.title} original",
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. Optional Rule-of-Thirds Grid when in Crop mode
                if (showCropGrid) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridColor = Color.White.copy(alpha = 0.35f)
                        val thirdW = size.width / 3f
                        val thirdH = size.height / 3f
                        drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth = 1.2.dp.toPx())
                        drawLine(gridColor, Offset(thirdW * 2f, 0f), Offset(thirdW * 2f, size.height), strokeWidth = 1.2.dp.toPx())
                        drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth = 1.2.dp.toPx())
                        drawLine(gridColor, Offset(0f, thirdH * 2f), Offset(size.width, thirdH * 2f), strokeWidth = 1.2.dp.toPx())
                    }
                }

                // 4. Vertical Split Divider Hairline + ORIGINAL / EDITED Overlay + Tactile Circular Handle
                if (imageStageSize.width > 0 && imageStageSize.height > 0) {
                    val splitXPx = (imageStageSize.width * splitPosition).roundToInt()
                    val centerYPx = imageStageSize.height / 2

                    // Crisp Vertical Divider Line with subtle shadow halo
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineX = size.width * splitPosition
                        drawLine(
                            color = Color.Black.copy(alpha = 0.45f),
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, size.height),
                            strokeWidth = 3.5.dp.toPx()
                        )
                        drawLine(
                            color = if (isDraggingSplit) DarkroomCoralAccent else Color.White.copy(alpha = 0.92f),
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, size.height),
                            strokeWidth = 1.6.dp.toPx()
                        )
                    }

                    // "ORIGINAL" pill badge positioned near the slider handle (left side)
                    val labelGapPx = with(density) { 10.dp.roundToPx() }
                    val approxOriginalWidthPx = with(density) { 68.dp.roundToPx() }
                    val approxEditedWidthPx = with(density) { 56.dp.roundToPx() }
                    val labelYOffsetPx = (centerYPx - with(density) { 46.dp.roundToPx() })
                        .coerceIn(with(density) { 10.dp.roundToPx() }, (imageStageSize.height - with(density) { 36.dp.roundToPx() }).coerceAtLeast(10))

                    AnimatedVisibility(
                        visible = splitPosition > 0.22f,
                        enter = fadeIn(tween(140)),
                        exit = fadeOut(tween(110)),
                        modifier = Modifier.offset {
                            val rawX = splitXPx - approxOriginalWidthPx - labelGapPx
                            IntOffset(
                                x = rawX.coerceAtLeast(with(density) { 6.dp.roundToPx() }),
                                y = labelYOffsetPx
                            )
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkroomOverlayScrim,
                            border = BorderStroke(1.dp, DarkroomSubtleBorder),
                            modifier = Modifier.testTag("darkroom_original_label")
                        ) {
                            Text(
                                text = stringResource(R.string.darkroom_original_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = DarkroomTextPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // "EDITED" pill badge positioned near the slider handle (right side)
                    AnimatedVisibility(
                        visible = splitPosition < 0.78f,
                        enter = fadeIn(tween(140)),
                        exit = fadeOut(tween(110)),
                        modifier = Modifier.offset {
                            val maxX = (imageStageSize.width - approxEditedWidthPx - with(density) { 6.dp.roundToPx() }).coerceAtLeast(0)
                            val rawX = splitXPx + labelGapPx
                            IntOffset(
                                x = rawX.coerceAtMost(maxX),
                                y = labelYOffsetPx
                            )
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkroomOverlayScrim,
                            border = BorderStroke(1.dp, DarkroomCoralAccent.copy(alpha = 0.65f)),
                            modifier = Modifier.testTag("darkroom_edited_label")
                        ) {
                            Text(
                                text = stringResource(R.string.darkroom_edited_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = DarkroomCoralAccent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Tactile Draggable Center Handle (48dp touch area wrapping 36dp coral-ringed circular knob)
                    val handleTouchPx = with(density) { 48.dp.roundToPx() }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = splitXPx - handleTouchPx / 2,
                                    y = centerYPx - handleTouchPx / 2
                                )
                            }
                            .size(48.dp)
                            .scale(handleScale)
                            .testTag("darkroom_split_handle")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DarkroomCardSurface.copy(alpha = 0.94f),
                            border = BorderStroke(
                                width = if (isDraggingSplit) 2.2.dp else 1.8.dp,
                                color = DarkroomCoralAccent
                            ),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Reveal original",
                                    tint = DarkroomTextPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Reveal edited",
                                    tint = DarkroomCoralAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subtle Top-Left Undo / Redo Micro-Pills (inside #18181C card corners)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CanvasMicroIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    enabled = canUndo,
                    onClick = onUndo,
                    testTag = "editor_undo_btn"
                )
                CanvasMicroIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    enabled = canRedo,
                    onClick = onRedo,
                    testTag = "editor_redo_btn"
                )
            }

            // Subtle Top-Right Active LUT Pill + Photo Switcher Micro-Button
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!activeFilterName.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DarkroomOverlayScrim,
                        border = BorderStroke(1.dp, DarkroomCoralAccent.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = activeFilterName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DarkroomCoralAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
                CanvasMicroIconButton(
                    icon = Icons.Default.PhotoLibrary,
                    contentDescription = "Switch Photo",
                    enabled = true,
                    onClick = onSwitchPhotoClick,
                    testTag = "editor_select_photo_btn"
                )
            }
        }
    }
}

@Composable
private fun CanvasMicroIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = CircleShape,
        color = DarkroomOverlayScrim,
        border = BorderStroke(1.dp, DarkroomSubtleBorder),
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) DarkroomTextPrimary else DarkroomTextSecondary.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Ergonomic Darkroom Slider Row with coral track, circular thumb, live numerical
 * value readout, and a dedicated reset ('↺') button at the end of its row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkroomAdjustmentSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float,
    valueText: String,
    testTagKey: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isDragged || isPressed

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val clampedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val isModified = abs(clampedValue - defaultValue) > (rangeSpan * 0.008f)

    val rawFraction = ((clampedValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (isActive) Spring.StiffnessHigh else Spring.StiffnessMedium
        ),
        label = "darkroom_slider_frac_$testTagKey"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (isActive) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "darkroom_slider_thumb_$testTagKey"
    )

    val isBiDirectional = valueRange.start < 0f && valueRange.endInclusive > 0f
    val anchorFraction = if (isBiDirectional) {
        ((0f - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    } else if (defaultValue > valueRange.start && defaultValue < valueRange.endInclusive) {
        ((defaultValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("darkroom_slider_row_$testTagKey"),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Row header: Parameter Label + Live Numerical Value Readout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 38.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = if (isModified || isActive) DarkroomTextPrimary else DarkroomTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = if (isModified || isActive) DarkroomCoralAccent else DarkroomTextSecondary
            )
        }

        // Control Row: Coral-Track Slider + End-of-Row Reset ('↺') Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                value = clampedValue,
                onValueChange = { onValueChange(it.coerceIn(valueRange.start, valueRange.endInclusive)) },
                valueRange = valueRange,
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = DarkroomCoralAccent,
                    activeTrackColor = DarkroomCoralAccent,
                    inactiveTrackColor = DarkroomTrackInactive
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .scale(thumbScale)
                            .shadow(4.dp, CircleShape, clip = false)
                            .background(DarkroomCoralAccent, CircleShape)
                            .border(2.dp, DarkroomOnCoral, CircleShape)
                    )
                },
                track = {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                    ) {
                        val trackW = size.width.coerceAtLeast(1f)
                        val trackH = 4.5.dp.toPx()
                        val centerY = size.height / 2f
                        val radius = CornerRadius(trackH / 2f, trackH / 2f)

                        drawRoundRect(
                            color = DarkroomTrackInactive,
                            topLeft = Offset(0f, centerY - trackH / 2f),
                            size = Size(trackW, trackH),
                            cornerRadius = radius
                        )

                        val thumbX = trackW * animatedFraction
                        if (isBiDirectional || (defaultValue > valueRange.start && defaultValue < valueRange.endInclusive)) {
                            val anchorX = trackW * anchorFraction
                            val leftX = minOf(anchorX, thumbX)
                            val rightX = maxOf(anchorX, thumbX)
                            val activeW = (rightX - leftX).coerceAtLeast(2.dp.toPx())
                            drawRoundRect(
                                color = DarkroomCoralAccent,
                                topLeft = Offset(leftX, centerY - trackH / 2f),
                                size = Size(activeW, trackH),
                                cornerRadius = radius
                            )
                            drawCircle(
                                color = DarkroomTextPrimary.copy(alpha = 0.5f),
                                radius = 2.dp.toPx(),
                                center = Offset(anchorX, centerY)
                            )
                        } else {
                            drawRoundRect(
                                color = DarkroomCoralAccent,
                                topLeft = Offset(0f, centerY - trackH / 2f),
                                size = Size(thumbX.coerceAtLeast(trackH), trackH),
                                cornerRadius = radius
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .testTag("darkroom_slider_$testTagKey")
            )

            // Small Reset Button ('↺') at the end of the row
            Surface(
                shape = CircleShape,
                color = if (isModified) {
                    DarkroomCoralAccent.copy(alpha = 0.16f)
                } else {
                    DarkroomControlSurface
                },
                border = BorderStroke(
                    1.dp,
                    if (isModified) DarkroomCoralAccent.copy(alpha = 0.55f) else DarkroomSubtleBorder
                ),
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(enabled = isModified) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onValueChange(defaultValue)
                    }
                    .testTag("darkroom_reset_$testTagKey")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "${stringResource(R.string.darkroom_reset_param)} $label",
                        tint = if (isModified) DarkroomCoralAccent else DarkroomTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Film LUTs Adjustment Panel: Horizontally scrolling carousel of live-preview thumbnails
 * (Kodak Gold 100, Fuji Velvia 50, CineStill 800T, Portra 400, etc.) with a dedicated
 * Intensity slider right below the carousel.
 */
@Composable
fun DarkroomFilmLutCarouselPanel(
    previewImageSource: String?,
    selectedFilter: StudioFilterItem?,
    filterIntensity: Float,
    onSelectFilter: (StudioFilterItem?) -> Unit,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeCategory by remember { mutableStateOf<FilterCategory?>(null) }

    val visibleLuts = remember(activeCategory) {
        if (activeCategory == null) {
            CinematicFilterCatalog.ALL_PRESETS
        } else {
            CinematicFilterCatalog.ALL_PRESETS.filter { it.category == activeCategory }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedFilter?.id, activeCategory) {
        if (selectedFilter == null) {
            listState.animateScrollToItem(0)
        } else {
            val idx = visibleLuts.indexOfFirst { it.id == selectedFilter.id }
            if (idx >= 0) {
                listState.animateScrollToItem((idx + 1).coerceAtLeast(0))
            }
        }
    }

    val thumbRequest = remember(previewImageSource) {
        if (!previewImageSource.isNullOrBlank()) {
            PhotoImageProcessor.buildThumbnailRequest(context, previewImageSource, 180, 180)
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("darkroom_film_lut_panel"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item(key = "lut_cat_all") {
                val isSelected = activeCategory == null
                LutCategoryChip(
                    label = "All",
                    selected = isSelected,
                    onClick = { activeCategory = null },
                    testTag = "lut_category_all"
                )
            }
            itemsIndexed(FilterCategory.entries, key = { _, c -> c.name }) { _, cat ->
                LutCategoryChip(
                    label = cat.label,
                    selected = activeCategory == cat,
                    onClick = { activeCategory = cat },
                    testTag = "lut_category_${cat.name.lowercase()}"
                )
            }
        }

        // Live-Preview LUT Thumbnail Carousel
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("darkroom_lut_carousel")
        ) {
            // Item 0: Original (No LUT)
            item(key = "lut_original") {
                LutLivePreviewCard(
                    name = "Original",
                    thumbRequest = thumbRequest,
                    colorMatrix = null,
                    fallbackGradient = listOf(DarkroomControlSurface, DarkroomCardSurface),
                    isSelected = selectedFilter == null,
                    onClick = { onSelectFilter(null) },
                    testTag = "lut_item_original"
                )
            }

            // Items 1..N: Film & Cinema LUTs (starting with Kodak Gold 100, Fuji Velvia 50, CineStill 800T)
            itemsIndexed(visibleLuts, key = { _, preset -> preset.id }) { _, preset ->
                val presetMatrix = remember(preset.id) { ColorMatrix(preset.matrixArray) }
                LutLivePreviewCard(
                    name = preset.name,
                    thumbRequest = thumbRequest,
                    colorMatrix = presetMatrix,
                    fallbackGradient = preset.previewGradient,
                    isSelected = selectedFilter?.id == preset.id,
                    onClick = {
                        onSelectFilter(preset)
                        if (filterIntensity < 0.05f) {
                            onIntensityChange(1f)
                        }
                    },
                    testTag = "lut_item_${preset.id}"
                )
            }
        }

        // Dedicated Intensity Slider directly below the carousel
        DarkroomAdjustmentSliderRow(
            label = if (selectedFilter != null) {
                "${stringResource(R.string.darkroom_slider_intensity)} · ${selectedFilter.name}"
            } else {
                stringResource(R.string.darkroom_slider_intensity)
            },
            value = if (selectedFilter != null) filterIntensity else 0f,
            onValueChange = { newIntensity ->
                if (selectedFilter == null && visibleLuts.isNotEmpty() && newIntensity > 0.02f) {
                    onSelectFilter(visibleLuts.first())
                }
                onIntensityChange(newIntensity)
            },
            valueRange = 0f..1f,
            defaultValue = 1f,
            valueText = String.format(
                Locale.US,
                "%.0f%%",
                (if (selectedFilter != null) filterIntensity else 0f) * 100f
            ),
            testTagKey = "lut_intensity"
        )
    }
}

@Composable
private fun LutCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg by animateColorAsState(
        targetValue = if (selected) DarkroomCoralAccent else DarkroomControlSurface,
        animationSpec = tween(160),
        label = "lut_chip_bg_$label"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) DarkroomOnCoral else DarkroomTextSecondary,
        animationSpec = tween(160),
        label = "lut_chip_text_$label"
    )
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, if (selected) DarkroomCoralAccent else DarkroomSubtleBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun LutLivePreviewCard(
    name: String,
    thumbRequest: Any?,
    colorMatrix: ColorMatrix?,
    fallbackGradient: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "lut_card_scale_$name"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) DarkroomCoralAccent else DarkroomSubtleBorder,
        animationSpec = tween(160),
        label = "lut_card_border_$name"
    )

    Box(
        modifier = Modifier
            .size(width = 78.dp, height = 94.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkroomControlSurface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        if (thumbRequest != null) {
            AsyncImage(
                model = thumbRequest,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(fallbackGradient))
            )
        }

        // Bottom Scrim + Crisp LUT Name
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.86f)
                        )
                    )
                )
                .padding(horizontal = 5.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) DarkroomCoralAccent else DarkroomTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        if (isSelected) {
            Surface(
                shape = CircleShape,
                color = DarkroomCoralAccent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DarkroomOnCoral,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
