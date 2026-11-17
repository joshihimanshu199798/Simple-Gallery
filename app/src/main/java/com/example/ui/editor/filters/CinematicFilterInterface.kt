package com.example.ui.editor.filters

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoImageProcessor
import com.example.ui.studio.CinematicFilterCatalog
import com.example.ui.studio.FilterCategory
import com.example.ui.studio.PremiumStudioSlider
import com.example.ui.studio.StudioFilterItem
import com.example.ui.theme.LocalCompleteUiTheme
import java.util.Locale

private val CardSurfaceVariant: Color
    @Composable get() = LocalCompleteUiTheme.current.surfaceVariant
private val DarkGraphiteBorder: Color
    @Composable get() = LocalCompleteUiTheme.current.borderStroke
private val NeonGreenAccent: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent
private val OnNeonGreenAccent: Color
    @Composable get() = LocalCompleteUiTheme.current.onPrimaryAccent
private val NeonGreenDim: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent.copy(alpha = 0.14f)
private val TextWhite: Color
    @Composable get() = LocalCompleteUiTheme.current.textPrimary
private val TextMuted: Color
    @Composable get() = LocalCompleteUiTheme.current.textSecondary

/**
 * Steps to the next (+1) or previous (-1) filter in [presets], including `null` (Original) at index -1.
 */
fun stepStudioFilter(
    current: StudioFilterItem?,
    presets: List<StudioFilterItem>,
    delta: Int
): StudioFilterItem? {
    if (presets.isEmpty()) return null
    val currentIdx = if (current == null) -1 else presets.indexOfFirst { it.id == current.id }
    val totalCount = presets.size + 1 // index 0 = Original(null), 1..size = presets
    val normalizedCurrent = currentIdx + 1
    val nextNormalized = ((normalizedCurrent + delta) % totalCount + totalCount) % totalCount
    return if (nextNormalized == 0) null else presets[nextNormalized - 1]
}

@Composable
fun CinematicFilterInterface(
    selectedFilter: StudioFilterItem?,
    filterIntensity: Float,
    onSelectFilter: (StudioFilterItem?) -> Unit,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    previewImageSource: String? = null
) {
    var activeCategory by remember { mutableStateOf<FilterCategory?>(null) }

    val filteredPresets = remember(activeCategory) {
        if (activeCategory == null) {
            CinematicFilterCatalog.ALL_PRESETS
        } else {
            CinematicFilterCatalog.ALL_PRESETS.filter { it.category == activeCategory }
        }
    }

    val isIntensitySliderOpen = selectedFilter != null
    val listState = rememberLazyListState()

    // Auto-scroll thumbnail strip whenever selectedFilter changes (via swipe or tap)
    LaunchedEffect(selectedFilter?.id, activeCategory) {
        if (selectedFilter == null) {
            listState.animateScrollToItem(0)
        } else {
            val idx = filteredPresets.indexOfFirst { it.id == selectedFilter.id }
            if (idx >= 0) {
                listState.animateScrollToItem((idx + 1).coerceAtLeast(0))
            }
        }
    }

    // Compact squircle size when intensity slider is open, full portrait card when closed
    val cardWidth by animateDpAsState(
        targetValue = if (isIntensitySliderOpen) 50.dp else 72.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "filter_card_width"
    )
    val cardHeight by animateDpAsState(
        targetValue = if (isIntensitySliderOpen) 46.dp else 86.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "filter_card_height"
    )

    // Horizontal swipe gesture detector on the filter header to cycle filters
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cinematic_filter_interface"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Modern Header Bar with Swipe Prev/Next Stepper & Category Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(filteredPresets, selectedFilter) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccum = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum += dragAmount
                            if (dragAccum <= -72f) {
                                dragAccum = 0f
                                onSelectFilter(stepStudioFilter(selectedFilter, filteredPresets, +1))
                            } else if (dragAccum >= 72f) {
                                dragAccum = 0f
                                onSelectFilter(stepStudioFilter(selectedFilter, filteredPresets, -1))
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Prev / Active Filter Pill / Next Stepper
            Surface(
                shape = RoundedCornerShape(50),
                color = CardSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGraphiteBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSelectFilter(stepStudioFilter(selectedFilter, filteredPresets, -1))
                            }
                            .testTag("filter_prev_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Filter",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedContent(
                        targetState = selectedFilter?.name ?: "Original (Swipe to Grade)",
                        transitionSpec = {
                            (slideInHorizontally { it / 2 } + fadeIn(tween(150))) togetherWith
                                (slideOutHorizontally { -it / 2 } + fadeOut(tween(120)))
                        },
                        label = "active_filter_name_anim"
                    ) { labelText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedFilter != null) Icons.Default.AutoAwesome else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = if (selectedFilter != null) NeonGreenAccent else TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSelectFilter(stepStudioFilter(selectedFilter, filteredPresets, +1))
                            }
                            .testTag("filter_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Filter",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Right: Reset button when filter active, or swipe hint badge
            if (selectedFilter != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGraphiteBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectFilter(null) }
                        .testTag("filter_clear_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset Filter",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                    }
                }
            } else {
                Text(
                    text = "${filteredPresets.size} LUTs",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextMuted
                )
            }
        }

        // 2. Modern Minimalist Category Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isAll = activeCategory == null
                val chipBg by animateColorAsState(
                    targetValue = if (isAll) NeonGreenAccent else CardSurfaceVariant,
                    label = "cat_all_bg"
                )
                val chipText by animateColorAsState(
                    targetValue = if (isAll) OnNeonGreenAccent else TextWhite,
                    label = "cat_all_text"
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = chipBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAll) NeonGreenAccent else DarkGraphiteBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { activeCategory = null }
                        .testTag("filter_category_all")
                ) {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = chipText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
            itemsIndexed(FilterCategory.entries) { _, cat ->
                val isSelected = activeCategory == cat
                val chipBg by animateColorAsState(
                    targetValue = if (isSelected) NeonGreenAccent else CardSurfaceVariant,
                    label = "cat_bg_${cat.name}"
                )
                val chipText by animateColorAsState(
                    targetValue = if (isSelected) OnNeonGreenAccent else TextWhite,
                    label = "cat_text_${cat.name}"
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = chipBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonGreenAccent else DarkGraphiteBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { activeCategory = cat }
                        .testTag("filter_category_${cat.name.lowercase()}")
                ) {
                    Text(
                        text = cat.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = chipText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 3. Sleek Intensity Slider (Opens when a filter is selected; hides filter names below)
        AnimatedVisibility(
            visible = isIntensitySliderOpen,
            enter = fadeIn(tween(180)) + expandVertically(),
            exit = fadeOut(tween(140)) + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardSurfaceVariant.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGraphiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Strength",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = TextMuted
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        PremiumStudioSlider(
                            value = filterIntensity,
                            onValueChange = onIntensityChange,
                            valueRange = 0f..1f,
                            defaultValue = 1f,
                            testTag = "filter_intensity_slider"
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NeonGreenDim,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreenAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onIntensityChange(1.0f) }
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", filterIntensity * 100f),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = NeonGreenAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 4. Modern Full-Bleed Filter Swatch Strip
        // When intensity slider is open: shrinks to compact 50x46 squircles and hides filter names!
        val filterContext = LocalContext.current
        val filterTileThumbModel = remember(previewImageSource) {
            if (!previewImageSource.isNullOrEmpty()) {
                PhotoImageProcessor.buildThumbnailRequest(filterContext, previewImageSource, 160, 160)
            } else {
                null
            }
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isIntensitySliderOpen) 8.dp else 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("filter_thumbnails_row")
        ) {
            // Item 0: Original (Unfiltered)
            item(key = "original_none") {
                val isSelected = selectedFilter == null
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "orig_scale"
                )
                Box(
                    modifier = Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .scale(scale)
                        .clip(RoundedCornerShape(if (isIntensitySliderOpen) 13.dp else 16.dp))
                        .background(CardSurfaceVariant)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) NeonGreenAccent else DarkGraphiteBorder,
                            shape = RoundedCornerShape(if (isIntensitySliderOpen) 13.dp else 16.dp)
                        )
                        .clickable { onSelectFilter(null) }
                        .testTag("filter_item_original")
                ) {
                    if (filterTileThumbModel != null) {
                        AsyncImage(
                            model = filterTileThumbModel,
                            contentDescription = "Original",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (isSelected) NeonGreenAccent else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom Gradient Scrim + Name ONLY when intensity slider is closed
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isIntensitySliderOpen,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(100)),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.82f)
                                        )
                                    )
                                )
                                .padding(horizontal = 4.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Original",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = NeonGreenAccent,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OnNeonGreenAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Items 1..N: Cinematic Filter Presets
            itemsIndexed(filteredPresets, key = { _, it -> it.id }) { _, preset ->
                val isSelected = selectedFilter?.id == preset.id
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "preset_scale_${preset.id}"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) NeonGreenAccent else DarkGraphiteBorder,
                    label = "preset_border_${preset.id}"
                )
                val shape = RoundedCornerShape(if (isIntensitySliderOpen) 13.dp else 16.dp)

                Box(
                    modifier = Modifier
                        .size(width = cardWidth, height = cardHeight)
                        .scale(scale)
                        .clip(shape)
                        .background(CardSurfaceVariant)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = borderColor,
                            shape = shape
                        )
                        .clickable { onSelectFilter(preset) }
                        .testTag("filter_item_${preset.id}")
                ) {
                    if (filterTileThumbModel != null) {
                        AsyncImage(
                            model = filterTileThumbModel,
                            contentDescription = preset.name,
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix(preset.matrixArray)),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(preset.previewGradient))
                        )
                    }

                    // Bottom Gradient Scrim + Filter Name ONLY when intensity slider is closed
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isIntensitySliderOpen,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(100)),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.84f)
                                        )
                                    )
                                )
                                .padding(horizontal = 4.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) NeonGreenAccent else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Active Checkmark Badge
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = NeonGreenAccent,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OnNeonGreenAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
