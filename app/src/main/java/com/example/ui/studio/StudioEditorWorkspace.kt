package com.example.ui.studio

import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.editor.filters.CinematicFilterInterface
import com.example.ui.editor.filters.stepStudioFilter
import com.example.ui.theme.LocalCompleteUiTheme
import java.util.Locale
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

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
private val NeonGreenDim: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent.copy(alpha = 0.14f)
private val TextWhite: Color
    @Composable get() = LocalCompleteUiTheme.current.textPrimary
private val TextMuted: Color
    @Composable get() = LocalCompleteUiTheme.current.textSecondary

private enum class LightParam(val label: String, val min: Float, val max: Float, val defaultVal: Float) {
    EXPOSURE("Exposure", -2f, 2f, 0f),
    BRIGHTNESS("Brightness", -80f, 80f, 0f),
    CONTRAST("Contrast", 0.5f, 1.8f, 1f),
    HIGHLIGHTS("Highlights", -100f, 100f, 0f),
    SHADOWS("Shadows", -100f, 100f, 0f)
}

private enum class ColorParam(val label: String, val min: Float, val max: Float, val defaultVal: Float) {
    TEMPERATURE("Warmth", -50f, 50f, 0f),
    TINT("Tint", -50f, 50f, 0f),
    SATURATION("Saturation", 0f, 2f, 1f)
}

private enum class DetailParam(val label: String, val min: Float, val max: Float, val defaultVal: Float) {
    VIGNETTE("Vignette", 0f, 100f, 0f),
    GRAIN("Film Grain", 0f, 100f, 0f),
    SMOOTH("Soft Focus", 0f, 100f, 0f)
}

@Composable
fun StudioEditorWorkspace(
    photo: PhotoEntity,
    initialTab: EditorToolTab = EditorToolTab.LIGHT,
    onClose: () -> Unit,
    onSaveEdits: (
        photo: PhotoEntity,
        cropRatio: CropAspectRatio,
        customCropRect: RectF?,
        rotationDegrees: Int,
        flipH: Boolean,
        flipV: Boolean,
        exposure: Float,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        highlights: Float,
        shadows: Float,
        temperature: Float,
        tint: Float,
        vignette: Float,
        grain: Float,
        filterMatrix: FloatArray?,
        filterIntensity: Float,
        skinSmoothing: Float,
        saveAsCopy: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }
    val context = LocalContext.current

    // Non-destructive Edit History auto-restore for previously edited photos
    val restoredHistory = remember(photo.id) {
        StudioPreferenceStore.loadPhotoEditHistory(context, photo.id)
    }

    var currentTab by remember { mutableStateOf(initialTab) }
    var userRecipes by remember { mutableStateOf(StudioPreferenceStore.loadUserRecipes(context)) }
    var showSaveRecipeDialog by remember { mutableStateOf(false) }
    var showHistogramOverlay by remember { mutableStateOf(true) }

    data class EditSnapshot(
        val exposure: Float,
        val brightness: Float,
        val contrast: Float,
        val saturation: Float,
        val highlights: Float,
        val shadows: Float,
        val temperature: Float,
        val tint: Float,
        val vignette: Float,
        val grain: Float,
        val skinSmoothing: Float,
        val rotation: Int,
        val flipH: Boolean,
        val flipV: Boolean,
        val filterId: String,
        val filterIntensity: Float,
        val keystoneV: Float,
        val keystoneH: Float,
        val framePreset: FrameBorderPreset
    )

    val undoStack = remember { mutableStateListOf<EditSnapshot>() }
    val redoStack = remember { mutableStateListOf<EditSnapshot>() }

    // Live State (initialized from non-destructive recipe history if present)
    var exposure by remember(photo.id) { mutableFloatStateOf(restoredHistory?.exposure ?: 0f) }
    var brightness by remember(photo.id) { mutableFloatStateOf(restoredHistory?.brightness ?: 0f) }
    var contrast by remember(photo.id) { mutableFloatStateOf(restoredHistory?.contrast ?: 1f) }
    var saturation by remember(photo.id) { mutableFloatStateOf(restoredHistory?.saturation ?: 1f) }
    var highlights by remember(photo.id) { mutableFloatStateOf(restoredHistory?.highlights ?: 0f) }
    var shadows by remember(photo.id) { mutableFloatStateOf(restoredHistory?.shadows ?: 0f) }
    var temperature by remember(photo.id) { mutableFloatStateOf(restoredHistory?.temperature ?: 0f) }
    var tint by remember(photo.id) { mutableFloatStateOf(restoredHistory?.tint ?: 0f) }
    var vignette by remember(photo.id) { mutableFloatStateOf(restoredHistory?.vignette ?: 0f) }
    var grain by remember(photo.id) { mutableFloatStateOf(restoredHistory?.grain ?: 0f) }
    var skinSmoothing by remember(photo.id) { mutableFloatStateOf(restoredHistory?.skinSmoothing ?: 0f) }

    // Keystone Perspective & Frame Border State
    var keystoneVertical by remember(photo.id) { mutableFloatStateOf(restoredHistory?.keystoneVertical ?: 0f) }
    var keystoneHorizontal by remember(photo.id) { mutableFloatStateOf(restoredHistory?.keystoneHorizontal ?: 0f) }
    var selectedFramePreset by remember(photo.id) {
        mutableStateOf(
            runCatching {
                FrameBorderPreset.valueOf(restoredHistory?.frameName ?: FrameBorderPreset.NONE.name)
            }.getOrDefault(FrameBorderPreset.NONE)
        )
    }

    // Selective Color / HSL Mixer State
    val hslBands = remember(photo.id) {
        mutableStateMapOf<HslColorBand, HslBandSetting>().apply {
            HslColorBand.entries.forEach { put(it, HslBandSetting()) }
        }
    }

    var rotationDegrees by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }
    var cropRatio by remember { mutableStateOf(CropAspectRatio.ORIGINAL) }
    var customCropRect by remember { mutableStateOf<RectF?>(null) }

    // Filters
    var selectedFilter by remember(photo.id) {
        mutableStateOf<StudioFilterItem?>(
            restoredHistory?.filterId?.takeIf { it.isNotEmpty() }?.let { id ->
                CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == id }
            }
        )
    }
    var filterIntensity by remember(photo.id) { mutableFloatStateOf(restoredHistory?.filterIntensity ?: 1f) }

    // Export Menu
    var showExportMenu by remember { mutableStateOf(false) }

    fun captureSnapshot(): EditSnapshot = EditSnapshot(
        exposure = exposure,
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        highlights = highlights,
        shadows = shadows,
        temperature = temperature,
        tint = tint,
        vignette = vignette,
        grain = grain,
        skinSmoothing = skinSmoothing,
        rotation = rotationDegrees,
        flipH = flipHorizontal,
        flipV = flipVertical,
        filterId = selectedFilter?.id.orEmpty(),
        filterIntensity = filterIntensity,
        keystoneV = keystoneVertical,
        keystoneH = keystoneHorizontal,
        framePreset = selectedFramePreset
    )

    fun pushUndo() {
        undoStack.add(captureSnapshot())
        redoStack.clear()
        if (undoStack.size > 25) undoStack.removeAt(0)
    }

    fun applyRecipe(recipe: StudioCustomRecipe) {
        pushUndo()
        exposure = recipe.exposure
        brightness = recipe.brightness
        contrast = recipe.contrast
        saturation = recipe.saturation
        highlights = recipe.highlights
        shadows = recipe.shadows
        temperature = recipe.temperature
        tint = recipe.tint
        vignette = recipe.vignette
        grain = recipe.grain
        skinSmoothing = recipe.skinSmoothing
        if (recipe.filterId.isNotEmpty()) {
            selectedFilter = CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == recipe.filterId }
            filterIntensity = recipe.filterIntensity
        }
        keystoneVertical = recipe.keystoneVertical
        keystoneHorizontal = recipe.keystoneHorizontal
        selectedFramePreset = runCatching { FrameBorderPreset.valueOf(recipe.frameName) }
            .getOrDefault(FrameBorderPreset.NONE)
    }

    fun buildCurrentRecipe(name: String): StudioCustomRecipe = StudioCustomRecipe(
        id = "custom_${System.currentTimeMillis()}",
        name = name,
        exposure = exposure,
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        highlights = highlights,
        shadows = shadows,
        temperature = temperature,
        tint = tint,
        vignette = vignette,
        grain = grain,
        skinSmoothing = skinSmoothing,
        filterId = selectedFilter?.id.orEmpty(),
        filterIntensity = filterIntensity,
        keystoneVertical = keystoneVertical,
        keystoneHorizontal = keystoneHorizontal,
        frameName = selectedFramePreset.name
    )

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val curr = captureSnapshot()
            redoStack.add(curr)
            val prev = undoStack.removeAt(undoStack.lastIndex)
            exposure = prev.exposure
            brightness = prev.brightness
            contrast = prev.contrast
            saturation = prev.saturation
            highlights = prev.highlights
            shadows = prev.shadows
            temperature = prev.temperature
            tint = prev.tint
            vignette = prev.vignette
            grain = prev.grain
            skinSmoothing = prev.skinSmoothing
            rotationDegrees = prev.rotation
            flipHorizontal = prev.flipH
            flipVertical = prev.flipV
            selectedFilter = CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == prev.filterId }
            filterIntensity = prev.filterIntensity
            keystoneVertical = prev.keystoneV
            keystoneHorizontal = prev.keystoneH
            selectedFramePreset = prev.framePreset
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val curr = captureSnapshot()
            undoStack.add(curr)
            val next = redoStack.removeAt(redoStack.lastIndex)
            exposure = next.exposure
            brightness = next.brightness
            contrast = next.contrast
            saturation = next.saturation
            highlights = next.highlights
            shadows = next.shadows
            temperature = next.temperature
            tint = next.tint
            vignette = next.vignette
            grain = next.grain
            skinSmoothing = next.skinSmoothing
            rotationDegrees = next.rotation
            flipHorizontal = next.flipH
            flipVertical = next.flipV
            selectedFilter = CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == next.filterId }
            filterIntensity = next.filterIntensity
            keystoneVertical = next.keystoneV
            keystoneHorizontal = next.keystoneH
            selectedFramePreset = next.framePreset
        }
    }

    // Aggregate HSL Mixer shifts into R/G/B channel offsets & saturation factor
    val hslSnapshot = hslBands.entries.map { it.key to it.value }
    val liveColorMatrix = remember(
        exposure,
        brightness,
        contrast,
        saturation,
        temperature,
        tint,
        selectedFilter,
        filterIntensity,
        highlights,
        shadows,
        hslSnapshot
    ) {
        var hslSatBonus = 0f
        var hslRedShift = 0f
        var hslGreenShift = 0f
        var hslBlueShift = 0f
        hslSnapshot.forEach { (band, setting) ->
            hslSatBonus += (setting.satShift / 100f) * 0.12f
            val rad = Math.toRadians((band.centerHue + setting.hueShift).toDouble())
            val lumOffset = setting.lumShift * 0.22f
            hslRedShift += (cos(rad).toFloat() * setting.satShift * 0.18f) + lumOffset
            hslGreenShift += (sin(rad).toFloat() * setting.satShift * 0.18f) + lumOffset
            hslBlueShift += (-cos(rad).toFloat() * setting.satShift * 0.15f) + lumOffset
        }

        val androidCm = android.graphics.ColorMatrix()
        androidCm.setSaturation((saturation + hslSatBonus).coerceIn(0f, 2.5f))

        val expScale = Math.pow(2.0, exposure.toDouble()).toFloat()
        val totalScale = contrast * expScale
        val totalOffset = brightness + (highlights * 0.25f) + (shadows * 0.25f) + (1f - contrast) * 64f

        val lightMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                totalScale, 0f, 0f, 0f, totalOffset + (temperature * 0.6f + tint * 0.3f) + hslRedShift,
                0f, totalScale, 0f, 0f, totalOffset - (tint * 0.4f) + hslGreenShift,
                0f, 0f, totalScale, 0f, totalOffset - (temperature * 0.6f) + hslBlueShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        androidCm.postConcat(lightMatrix)

        if (selectedFilter != null && filterIntensity > 0.01f) {
            val interpolated = CinematicFilterCatalog.interpolateMatrix(
                selectedFilter!!.matrixArray,
                filterIntensity
            )
            androidCm.postConcat(android.graphics.ColorMatrix(interpolated))
        }
        ColorMatrix(androidCm.array)
    }

    // Horizontal swipe gesture accumulator on the main photo canvas to switch filters
    var canvasSwipeAccum by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("studio_editor_workspace")
    ) {
        // 1. MODERN FLAGSHIP TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .testTag("editor_close_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentTab.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (restoredHistory != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = NeonGreenDim
                            ) {
                                Text(
                                    text = "History",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = NeonGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (selectedFilter != null) {
                            "Filter: ${selectedFilter!!.name} (${(filterIntensity * 100).toInt()}%)"
                        } else {
                            "Swipe photo left/right to switch filters"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedFilter != null) NeonGreen else TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Live RGB Histogram Toggle Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (showHistogramOverlay) NeonGreenDim else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showHistogramOverlay) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { showHistogramOverlay = !showHistogramOverlay }
                        .testTag("editor_histogram_toggle")
                ) {
                    Text(
                        text = "RGB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        color = if (showHistogramOverlay) NeonGreen else TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                val studioHaptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                var showUndoHistoryMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = {
                            studioHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            undo()
                        },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("editor_undo_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) TextWhite else TextMuted.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (undoStack.isNotEmpty()) {
                        DropdownMenu(
                            expanded = showUndoHistoryMenu,
                            onDismissRequest = { showUndoHistoryMenu = false },
                            modifier = Modifier.background(PanelBg)
                        ) {
                            Text(
                                text = "Action History (${undoStack.size} steps)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            undoStack.takeLast(6).reversed().forEachIndexed { idx, snapshot ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Step -${idx + 1} • Filter ${snapshot.filterId ?: "None"} (${snapshot.exposure} exp)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextWhite
                                        )
                                    },
                                    onClick = {
                                        studioHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        repeat(idx + 1) { undo() }
                                        showUndoHistoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        studioHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        redo()
                    },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("editor_redo_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) TextWhite else TextMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save / Export Menu
                Box {
                    Button(
                        onClick = { showExportMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = OnNeonGreen
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("editor_save_export_btn")
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = OnNeonGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnNeonGreen
                        )
                    }

                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false },
                        modifier = Modifier.background(PanelBg)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "Save as Copy",
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Creates a new edited photo & saves recipe history",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonGreen)
                            },
                            onClick = {
                                showExportMenu = false
                                StudioPreferenceStore.savePhotoEditHistory(
                                    context,
                                    photo.id,
                                    buildCurrentRecipe("Last Edit")
                                )
                                onSaveEdits(
                                    photo,
                                    cropRatio,
                                    customCropRect,
                                    rotationDegrees,
                                    flipHorizontal,
                                    flipVertical,
                                    exposure,
                                    brightness,
                                    contrast,
                                    saturation,
                                    highlights,
                                    shadows,
                                    temperature,
                                    tint,
                                    vignette,
                                    grain,
                                    selectedFilter?.matrixArray,
                                    filterIntensity,
                                    skinSmoothing,
                                    true
                                )
                            }
                        )
                        HorizontalDivider(color = BorderColor)
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "Overwrite Original",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF43F5E)
                                    )
                                    Text(
                                        text = "Replaces the original photo file & saves recipe",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFFF43F5E))
                            },
                            onClick = {
                                showExportMenu = false
                                StudioPreferenceStore.savePhotoEditHistory(
                                    context,
                                    photo.id,
                                    buildCurrentRecipe("Last Edit")
                                )
                                onSaveEdits(
                                    photo,
                                    cropRatio,
                                    customCropRect,
                                    rotationDegrees,
                                    flipHorizontal,
                                    flipVertical,
                                    exposure,
                                    brightness,
                                    contrast,
                                    saturation,
                                    highlights,
                                    shadows,
                                    temperature,
                                    tint,
                                    vignette,
                                    grain,
                                    selectedFilter?.matrixArray,
                                    filterIntensity,
                                    skinSmoothing,
                                    false
                                )
                            }
                        )
                    }
                }
            }
        }

        // 2. CENTER INTERACTIVE VIEWPORT WITH HORIZONTAL SWIPE FILTER NAVIGATION & LIVE RGB HISTOGRAM
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0C0E14))
                .border(1.dp, BorderColor.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .pointerInput(selectedFilter) {
                    detectHorizontalDragGestures(
                        onDragStart = { canvasSwipeAccum = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            canvasSwipeAccum += dragAmount
                            if (canvasSwipeAccum <= -85f) {
                                canvasSwipeAccum = 0f
                                pushUndo()
                                selectedFilter = stepStudioFilter(
                                    selectedFilter,
                                    CinematicFilterCatalog.ALL_PRESETS,
                                    +1
                                )
                            } else if (canvasSwipeAccum >= 85f) {
                                canvasSwipeAccum = 0f
                                pushUndo()
                                selectedFilter = stepStudioFilter(
                                    selectedFilter,
                                    CinematicFilterCatalog.ALL_PRESETS,
                                    -1
                                )
                            }
                        }
                    )
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val aspect = when {
                cropRatio.ratio != null -> cropRatio.ratio!!
                photo.height > 0 -> (photo.width.toFloat() / photo.height.toFloat()).coerceIn(0.4f, 2.5f)
                else -> 1f
            }

            // Outer Frame Border Wrapper (Museum Mat, Polaroid, Letterbox, etc.)
            val framePadDp = (selectedFramePreset.padRatio * 180f).dp
            val bottomFramePadDp = framePadDp * selectedFramePreset.bottomWeight

            Box(
                modifier = Modifier
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(12.dp))
                    .graphicsLayer {
                        rotationZ = rotationDegrees.toFloat()
                        rotationX = keystoneVertical
                        rotationY = keystoneHorizontal
                        cameraDistance = 14f * density
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
                    model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                    contentDescription = photo.title,
                    contentScale = if (cropRatio != CropAspectRatio.ORIGINAL) ContentScale.Crop else ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(liveColorMatrix),
                    modifier = Modifier.fillMaxSize()
                )

                // Live Vignette & Film Grain Preview Overlay
                if (vignette > 0f || grain > 0f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (vignette > 0f) {
                            val maxRadius = size.maxDimension * 0.75f
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = (vignette / 100f * 0.85f).coerceIn(0f, 0.88f))
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

                // Rule of Thirds & Keystone Grid Overlay when in CROP tab
                if (currentTab == EditorToolTab.CROP) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridColor = Color.White.copy(alpha = 0.35f)
                        val thirdW = size.width / 3f
                        val thirdH = size.height / 3f
                        drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(thirdW * 2f, 0f), Offset(thirdW * 2f, size.height), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(0f, thirdH * 2f), Offset(size.width, thirdH * 2f), strokeWidth = 1.5f)
                    }
                }
            }

            // Live Interactive RGB & Luminance Histogram Overlay (Top-Right of Viewport)
            androidx.compose.animation.AnimatedVisibility(
                visible = showHistogramOverlay,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.92f),
                exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.92f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                LiveRgbHistogramOverlay(
                    photoId = photo.id,
                    exposure = exposure,
                    brightness = brightness,
                    contrast = contrast,
                    highlights = highlights,
                    shadows = shadows,
                    temperature = temperature,
                    saturation = saturation
                )
            }

            // Floating Swipe Filter HUD Pill at bottom of viewport
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.68f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable {
                                pushUndo()
                                selectedFilter = stepStudioFilter(
                                    selectedFilter,
                                    CinematicFilterCatalog.ALL_PRESETS,
                                    -1
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Filter",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        Icons.Default.Movie,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(13.dp)
                    )

                    AnimatedContent(
                        targetState = selectedFilter?.name ?: "Original • Swipe Filter",
                        transitionSpec = {
                            (slideInHorizontally { it / 2 } + fadeIn(tween(150))) togetherWith
                                (slideOutHorizontally { -it / 2 } + fadeOut(tween(120)))
                        },
                        label = "hud_filter_pill"
                    ) { filterLabel ->
                        Text(
                            text = filterLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable {
                                pushUndo()
                                selectedFilter = stepStudioFilter(
                                    selectedFilter,
                                    CinematicFilterCatalog.ALL_PRESETS,
                                    +1
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Filter",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. MODERN PURPOSE-BUILT CONTROL DECK + BOTTOM TOOL SWITCHER
        Surface(
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = PanelBg,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Tool Feature Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val forward = targetState.ordinal >= initialState.ordinal
                            val dir = if (forward) 1 else -1
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialOffsetX = { w -> (w * 0.14f * dir).toInt() }
                            ) + fadeIn(tween(180))) togetherWith (slideOutHorizontally(
                                animationSpec = tween(140),
                                targetOffsetX = { w -> -(w * 0.10f * dir).toInt() }
                            ) + fadeOut(tween(130)))
                        },
                        label = "editor_tool_tab_transition",
                        modifier = Modifier.fillMaxWidth()
                    ) { activeTab ->
                        when (activeTab) {
                            EditorToolTab.CROP -> {
                                CropControlPanel(
                                    cropRatio = cropRatio,
                                    rotationDegrees = rotationDegrees,
                                    flipHorizontal = flipHorizontal,
                                    flipVertical = flipVertical,
                                    keystoneVertical = keystoneVertical,
                                    keystoneHorizontal = keystoneHorizontal,
                                    onSelectCropRatio = { cropRatio = it },
                                    onRotateLeft = { rotationDegrees = (rotationDegrees - 90 + 360) % 360 },
                                    onRotateRight = { rotationDegrees = (rotationDegrees + 90) % 360 },
                                    onFlipH = { flipHorizontal = !flipHorizontal },
                                    onFlipV = { flipVertical = !flipVertical },
                                    onKeystoneVChange = { keystoneVertical = it },
                                    onKeystoneHChange = { keystoneHorizontal = it },
                                    onResetGeometry = {
                                        cropRatio = CropAspectRatio.ORIGINAL
                                        rotationDegrees = 0
                                        flipHorizontal = false
                                        flipVertical = false
                                        keystoneVertical = 0f
                                        keystoneHorizontal = 0f
                                    }
                                )
                            }
                            EditorToolTab.LIGHT -> {
                                LightControlPanel(
                                    exposure = exposure,
                                    onExposureChange = { exposure = it },
                                    brightness = brightness,
                                    onBrightnessChange = { brightness = it },
                                    contrast = contrast,
                                    onContrastChange = { contrast = it },
                                    highlights = highlights,
                                    onHighlightsChange = { highlights = it },
                                    shadows = shadows,
                                    onShadowsChange = { shadows = it },
                                    userRecipes = userRecipes,
                                    onApplyRecipe = { applyRecipe(it) },
                                    onOpenSaveRecipe = { showSaveRecipeDialog = true },
                                    onAutoTone = {
                                        pushUndo()
                                        exposure = 0.2f
                                        brightness = 6f
                                        contrast = 1.14f
                                        highlights = -12f
                                        shadows = 22f
                                    },
                                    onResetLight = {
                                        pushUndo()
                                        exposure = 0f
                                        brightness = 0f
                                        contrast = 1f
                                        highlights = 0f
                                        shadows = 0f
                                    }
                                )
                            }
                            EditorToolTab.COLOR -> {
                                ColorControlPanel(
                                    temperature = temperature,
                                    onTempChange = { temperature = it },
                                    tint = tint,
                                    onTintChange = { tint = it },
                                    saturation = saturation,
                                    onSatChange = { saturation = it },
                                    hslBands = hslBands,
                                    onUpdateHslBand = { band, setting -> hslBands[band] = setting },
                                    onApplyWarm = {
                                        pushUndo()
                                        temperature = 18f
                                        tint = 4f
                                        saturation = 1.12f
                                    },
                                    onApplyCool = {
                                        pushUndo()
                                        temperature = -18f
                                        tint = -4f
                                        saturation = 1.05f
                                    },
                                    onApplyVivid = {
                                        pushUndo()
                                        saturation = 1.35f
                                    },
                                    onApplyBw = {
                                        pushUndo()
                                        saturation = 0f
                                    },
                                    onResetColor = {
                                        pushUndo()
                                        temperature = 0f
                                        tint = 0f
                                        saturation = 1f
                                        HslColorBand.entries.forEach { hslBands[it] = HslBandSetting() }
                                    }
                                )
                            }
                            EditorToolTab.FILTERS -> {
                                CinematicFilterInterface(
                                    selectedFilter = selectedFilter,
                                    filterIntensity = filterIntensity,
                                    onSelectFilter = { selectedFilter = it },
                                    onIntensityChange = { filterIntensity = it },
                                    previewImageSource = photo.filePath
                                )
                            }
                            EditorToolTab.DETAIL -> {
                                DetailControlPanel(
                                    vignette = vignette,
                                    onVignetteChange = { vignette = it },
                                    grain = grain,
                                    onGrainChange = { grain = it },
                                    skinSmoothing = skinSmoothing,
                                    onSkinSmoothingChange = { skinSmoothing = it },
                                    selectedFrame = selectedFramePreset,
                                    onSelectFrame = { selectedFramePreset = it },
                                    onResetDetail = {
                                        pushUndo()
                                        vignette = 0f
                                        grain = 0f
                                        skinSmoothing = 0f
                                        selectedFramePreset = FrameBorderPreset.NONE
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderColor.copy(alpha = 0.55f), thickness = 0.8.dp)

                // Modern Icon + Label Bottom Tool Switcher Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorToolTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        val tabIcon: ImageVector = when (tab) {
                            EditorToolTab.CROP -> Icons.Default.Crop
                            EditorToolTab.LIGHT -> Icons.Default.WbSunny
                            EditorToolTab.COLOR -> Icons.Default.Palette
                            EditorToolTab.FILTERS -> Icons.Default.Movie
                            EditorToolTab.DETAIL -> Icons.Default.FilterVintage
                        }
                        val shortLabel = when (tab) {
                            EditorToolTab.CROP -> "Crop"
                            EditorToolTab.LIGHT -> "Light"
                            EditorToolTab.COLOR -> "Color"
                            EditorToolTab.FILTERS -> "Filters"
                            EditorToolTab.DETAIL -> "Effects"
                        }

                        val tabBg by animateColorAsState(
                            targetValue = if (isSelected) NeonGreen else CardBg.copy(alpha = 0.7f),
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "editor_tab_bg_${tab.name}"
                        )
                        val tabText by animateColorAsState(
                            targetValue = if (isSelected) OnNeonGreen else TextWhite,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "editor_tab_text_${tab.name}"
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = tabBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonGreen else BorderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    pushUndo()
                                    currentTab = tab
                                }
                                .testTag("editor_tab_${tab.name.lowercase()}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = tab.label,
                                    tint = tabText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = tabText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Custom Recipe Modal Dialog
    if (showSaveRecipeDialog) {
        var recipeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveRecipeDialog = false },
            containerColor = PanelBg,
            title = {
                Text(
                    text = "Save Custom Studio Recipe",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Save your current Exposure, Contrast, Color Balance, LUT Filter, Grain, and Frame settings as a reusable 1-tap preset.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Preset Name (e.g. Moody Street)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = recipeName.trim().ifEmpty { "Studio Look ${userRecipes.size + 1}" }
                        userRecipes = StudioPreferenceStore.saveUserRecipe(
                            context,
                            buildCurrentRecipe(finalName)
                        )
                        showSaveRecipeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = OnNeonGreen
                    )
                ) {
                    Text("Save Preset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveRecipeDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun LiveRgbHistogramOverlay(
    photoId: Long,
    exposure: Float,
    brightness: Float,
    contrast: Float,
    highlights: Float,
    shadows: Float,
    temperature: Float,
    saturation: Float
) {
    val clipShadows = exposure < -0.9f || brightness < -35f || shadows < -65f
    val clipHighlights = exposure > 0.9f || brightness > 35f || highlights > 65f

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = Modifier
            .width(126.dp)
            .height(64.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RGB HIST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SHD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (clipShadows) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.35f)
                    )
                    Text(
                        text = "HLT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (clipHighlights) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.35f)
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val bins = 24
                val w = size.width
                val h = size.height
                val expShift = (exposure * 0.22f) + (brightness / 240f) + (shadows - highlights) * -0.001f
                val spread = (0.22f / contrast.coerceIn(0.5f, 1.8f)).coerceIn(0.12f, 0.38f)
                val tempShift = (temperature / 200f) * saturation.coerceIn(0f, 2f)
                val seed = (photoId % 7).toFloat() * 0.02f

                fun buildCurve(centerOffset: Float): Path {
                    val path = Path()
                    for (i in 0 until bins) {
                        val xNorm = i.toFloat() / (bins - 1).toFloat()
                        val center = (0.5f + expShift + centerOffset + seed).coerceIn(0.12f, 0.88f)
                        val diff = (xNorm - center) / spread
                        val bell = exp((-0.5f * diff * diff).toDouble()).toFloat()
                        val ripple = 0.85f + 0.15f * sin((i * 1.3f + photoId).toDouble()).toFloat()
                        val yVal = (bell * ripple).coerceIn(0.05f, 1f)
                        val px = xNorm * w
                        val py = h - (yVal * h * 0.92f)
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    return path
                }

                drawPath(buildCurve(tempShift), color = Color(0xFFEF4444).copy(alpha = 0.75f), style = Stroke(width = 1.6.dp.toPx()))
                drawPath(buildCurve(0f), color = Color(0xFF22C55E).copy(alpha = 0.75f), style = Stroke(width = 1.6.dp.toPx()))
                drawPath(buildCurve(-tempShift), color = Color(0xFF3B82F6).copy(alpha = 0.75f), style = Stroke(width = 1.6.dp.toPx()))
                drawPath(buildCurve(tempShift * 0.25f), color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 1.2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun CropControlPanel(
    cropRatio: CropAspectRatio,
    rotationDegrees: Int,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    keystoneVertical: Float,
    keystoneHorizontal: Float,
    onSelectCropRatio: (CropAspectRatio) -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onKeystoneVChange: (Float) -> Unit,
    onKeystoneHChange: (Float) -> Unit,
    onResetGeometry: () -> Unit
) {
    var activeKeystoneAxis by remember { mutableIntStateOf(0) } // 0 = Vertical, 1 = Horizontal

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Transform Action Pills (-90°, +90°, Flip H, Flip V, Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GeometryActionButton(
                icon = Icons.AutoMirrored.Filled.RotateLeft,
                label = "-90°",
                isActive = false,
                onClick = onRotateLeft,
                modifier = Modifier.weight(1f)
            )
            GeometryActionButton(
                icon = Icons.AutoMirrored.Filled.RotateRight,
                label = "+90°",
                isActive = rotationDegrees != 0,
                onClick = onRotateRight,
                modifier = Modifier.weight(1f)
            )
            GeometryActionButton(
                icon = Icons.Default.Flip,
                label = "Flip H",
                isActive = flipHorizontal,
                onClick = onFlipH,
                modifier = Modifier.weight(1f)
            )
            GeometryActionButton(
                icon = Icons.Default.Flip,
                label = "Flip V",
                isActive = flipVertical,
                onClick = onFlipV,
                modifier = Modifier.weight(1f)
            )
            GeometryActionButton(
                icon = Icons.Default.Refresh,
                label = "Reset",
                isActive = false,
                onClick = onResetGeometry,
                modifier = Modifier.weight(0.9f)
            )
        }

        // Row 2: Perspective & Keystone Correction Slider + Axis Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (activeKeystoneAxis == 0) NeonGreenDim else CardBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeKeystoneAxis == 0) NeonGreen else BorderColor
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { activeKeystoneAxis = 0 }
            ) {
                Text(
                    text = String.format(Locale.US, "Keystone V %+.0f°", keystoneVertical),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (activeKeystoneAxis == 0) NeonGreen else TextWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (activeKeystoneAxis == 1) NeonGreenDim else CardBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeKeystoneAxis == 1) NeonGreen else BorderColor
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { activeKeystoneAxis = 1 }
            ) {
                Text(
                    text = String.format(Locale.US, "Keystone H %+.0f°", keystoneHorizontal),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (activeKeystoneAxis == 1) NeonGreen else TextWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        PremiumStudioSlider(
            value = if (activeKeystoneAxis == 0) keystoneVertical else keystoneHorizontal,
            onValueChange = {
                if (activeKeystoneAxis == 0) onKeystoneVChange(it) else onKeystoneHChange(it)
            },
            valueRange = -25f..25f,
            defaultValue = 0f,
            label = if (activeKeystoneAxis == 0) "Vertical Perspective" else "Horizontal Perspective",
            valueText = String.format(
                Locale.US,
                "%+.0f°",
                if (activeKeystoneAxis == 0) keystoneVertical else keystoneHorizontal
            )
        )

        // Row 3: Aspect Ratio Frame Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CropAspectRatio.entries) { ratio ->
                val isSelected = cropRatio == ratio
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) NeonGreen else CardBg,
                    label = "crop_ratio_bg_${ratio.name}"
                )
                val pillText by animateColorAsState(
                    targetValue = if (isSelected) OnNeonGreen else TextWhite,
                    label = "crop_ratio_text_${ratio.name}"
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = pillBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectCropRatio(ratio) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = pillText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = ratio.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = pillText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeometryActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) NeonGreenDim else CardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) NeonGreen else BorderColor
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) NeonGreen else TextWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                ),
                color = if (isActive) NeonGreen else TextWhite,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LightControlPanel(
    exposure: Float,
    onExposureChange: (Float) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    highlights: Float,
    onHighlightsChange: (Float) -> Unit,
    shadows: Float,
    onShadowsChange: (Float) -> Unit,
    userRecipes: List<StudioCustomRecipe>,
    onApplyRecipe: (StudioCustomRecipe) -> Unit,
    onOpenSaveRecipe: () -> Unit,
    onAutoTone: () -> Unit,
    onResetLight: () -> Unit
) {
    var activeParam by remember { mutableStateOf(LightParam.EXPOSURE) }

    val currentValue = when (activeParam) {
        LightParam.EXPOSURE -> exposure
        LightParam.BRIGHTNESS -> brightness
        LightParam.CONTRAST -> contrast
        LightParam.HIGHLIGHTS -> highlights
        LightParam.SHADOWS -> shadows
    }

    val currentValueText = when (activeParam) {
        LightParam.EXPOSURE -> String.format(Locale.US, "%+.1f EV", exposure)
        LightParam.BRIGHTNESS -> String.format(Locale.US, "%+.0f", brightness)
        LightParam.CONTRAST -> String.format(Locale.US, "%.0f%%", contrast * 100f)
        LightParam.HIGHLIGHTS -> String.format(Locale.US, "%+.0f", highlights)
        LightParam.SHADOWS -> String.format(Locale.US, "%+.0f", shadows)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Custom User Presets ("Save My Recipe") + Auto Tone Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NeonGreenDim,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onAutoTone)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Auto Tone",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NeonGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenSaveRecipe)
                        .testTag("editor_save_recipe_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = OnNeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Save Recipe",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnNeonGreen
                        )
                    }
                }
            }

            items(userRecipes, key = { it.id }) { recipe ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onApplyRecipe(recipe) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextWhite,
                            maxLines = 1
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onResetLight)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Dedicated Precision Slider for the selected Light Parameter
        PremiumStudioSlider(
            value = currentValue,
            onValueChange = { newVal ->
                when (activeParam) {
                    LightParam.EXPOSURE -> onExposureChange(newVal)
                    LightParam.BRIGHTNESS -> onBrightnessChange(newVal)
                    LightParam.CONTRAST -> onContrastChange(newVal)
                    LightParam.HIGHLIGHTS -> onHighlightsChange(newVal)
                    LightParam.SHADOWS -> onShadowsChange(newVal)
                }
            },
            valueRange = activeParam.min..activeParam.max,
            defaultValue = activeParam.defaultVal,
            label = activeParam.label,
            valueText = currentValueText
        )

        // Interactive Parameter Dial Pills (All 5 light controls visible at once)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LightParam.entries.forEach { param ->
                val isSelected = activeParam == param
                val badgeText = when (param) {
                    LightParam.EXPOSURE -> String.format(Locale.US, "%+.1f", exposure)
                    LightParam.BRIGHTNESS -> String.format(Locale.US, "%+.0f", brightness)
                    LightParam.CONTRAST -> "${(contrast * 100).toInt()}%"
                    LightParam.HIGHLIGHTS -> String.format(Locale.US, "%+.0f", highlights)
                    LightParam.SHADOWS -> String.format(Locale.US, "%+.0f", shadows)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NeonGreenDim else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeParam = param }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = if (isSelected) NeonGreen else TextWhite,
                            maxLines = 1
                        )
                        Text(
                            text = param.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = if (isSelected) NeonGreen else TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorControlPanel(
    temperature: Float,
    onTempChange: (Float) -> Unit,
    tint: Float,
    onTintChange: (Float) -> Unit,
    saturation: Float,
    onSatChange: (Float) -> Unit,
    hslBands: Map<HslColorBand, HslBandSetting>,
    onUpdateHslBand: (HslColorBand, HslBandSetting) -> Unit,
    onApplyWarm: () -> Unit,
    onApplyCool: () -> Unit,
    onApplyVivid: () -> Unit,
    onApplyBw: () -> Unit,
    onResetColor: () -> Unit
) {
    var isHslMixerMode by remember { mutableStateOf(false) }
    var activeParam by remember { mutableStateOf(ColorParam.TEMPERATURE) }
    var activeHslBand by remember { mutableStateOf(HslColorBand.BLUE) }
    var activeHslChannel by remember { mutableIntStateOf(1) } // 0 = Hue, 1 = Saturation, 2 = Luminance

    val currentValue = when (activeParam) {
        ColorParam.TEMPERATURE -> temperature
        ColorParam.TINT -> tint
        ColorParam.SATURATION -> saturation
    }

    val currentValueText = when (activeParam) {
        ColorParam.TEMPERATURE -> String.format(Locale.US, "%+.0f", temperature)
        ColorParam.TINT -> String.format(Locale.US, "%+.0f", tint)
        ColorParam.SATURATION -> String.format(Locale.US, "%.0f%%", saturation * 100f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Switcher Row: Global Balance vs Selective HSL Mixer + Quick Moods
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (!isHslMixerMode) NeonGreen else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!isHslMixerMode) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { isHslMixerMode = false }
                ) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (!isHslMixerMode) OnNeonGreen else TextWhite,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isHslMixerMode) NeonGreen else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHslMixerMode) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { isHslMixerMode = true }
                        .testTag("editor_hsl_mixer_toggle")
                ) {
                    Text(
                        text = "HSL Mixer",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isHslMixerMode) OnNeonGreen else TextWhite,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }

            val moods = listOf(
                Triple("Warm", temperature > 10f, onApplyWarm),
                Triple("Cool", temperature < -10f, onApplyCool),
                Triple("Vivid", saturation > 1.25f, onApplyVivid),
                Triple("B&W", saturation == 0f, onApplyBw),
                Triple("Reset", false, onResetColor)
            )
            items(moods) { (label, active, action) ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (active) NeonGreenDim else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (active) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = action)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (active) NeonGreen else TextWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (!isHslMixerMode) {
            PremiumStudioSlider(
                value = currentValue,
                onValueChange = { newVal ->
                    when (activeParam) {
                        ColorParam.TEMPERATURE -> onTempChange(newVal)
                        ColorParam.TINT -> onTintChange(newVal)
                        ColorParam.SATURATION -> onSatChange(newVal)
                    }
                },
                valueRange = activeParam.min..activeParam.max,
                defaultValue = activeParam.defaultVal,
                label = activeParam.label,
                valueText = currentValueText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorParam.entries.forEach { param ->
                    val isSelected = activeParam == param
                    val badgeText = when (param) {
                        ColorParam.TEMPERATURE -> String.format(Locale.US, "%+.0f", temperature)
                        ColorParam.TINT -> String.format(Locale.US, "%+.0f", tint)
                        ColorParam.SATURATION -> "${(saturation * 100).toInt()}%"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonGreenDim else CardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonGreen else BorderColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeParam = param }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = param.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) NeonGreen else TextWhite
                            )
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = if (isSelected) NeonGreen else TextMuted
                            )
                        }
                    }
                }
            }
        } else {
            // Selective Color / 8-Band HSL Mixer
            val currentBandSetting = hslBands[activeHslBand] ?: HslBandSetting()
            val hslVal = when (activeHslChannel) {
                0 -> currentBandSetting.hueShift
                1 -> currentBandSetting.satShift
                else -> currentBandSetting.lumShift
            }
            val hslRange = if (activeHslChannel == 0) -30f..30f else -100f..100f
            val channelName = when (activeHslChannel) {
                0 -> "${activeHslBand.label} Hue"
                1 -> "${activeHslBand.label} Saturation"
                else -> "${activeHslBand.label} Luminance"
            }

            // 8 Color Band Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HslColorBand.entries.forEach { band ->
                    val isSelected = activeHslBand == band
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 28.dp else 22.dp)
                            .clip(CircleShape)
                            .background(band.bandColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .clickable { activeHslBand = band }
                    )
                }
            }

            PremiumStudioSlider(
                value = hslVal,
                onValueChange = { newVal ->
                    val updated = when (activeHslChannel) {
                        0 -> currentBandSetting.copy(hueShift = newVal)
                        1 -> currentBandSetting.copy(satShift = newVal)
                        else -> currentBandSetting.copy(lumShift = newVal)
                    }
                    onUpdateHslBand(activeHslBand, updated)
                },
                valueRange = hslRange,
                defaultValue = 0f,
                label = channelName,
                valueText = String.format(Locale.US, "%+.0f", hslVal)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hue" to 0, "Saturation" to 1, "Luminance" to 2).forEach { (label, idx) ->
                    val selected = activeHslChannel == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) NeonGreenDim else CardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) NeonGreen else BorderColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { activeHslChannel = idx }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) NeonGreen else TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailControlPanel(
    vignette: Float,
    onVignetteChange: (Float) -> Unit,
    grain: Float,
    onGrainChange: (Float) -> Unit,
    skinSmoothing: Float,
    onSkinSmoothingChange: (Float) -> Unit,
    selectedFrame: FrameBorderPreset,
    onSelectFrame: (FrameBorderPreset) -> Unit,
    onResetDetail: () -> Unit
) {
    var activeParam by remember { mutableStateOf(DetailParam.VIGNETTE) }

    val currentValue = when (activeParam) {
        DetailParam.VIGNETTE -> vignette
        DetailParam.GRAIN -> grain
        DetailParam.SMOOTH -> skinSmoothing
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Border & Frame Studio Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FrameBorderPreset.entries) { frame ->
                val isSelected = selectedFrame == frame
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) NeonGreen else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectFrame(frame) }
                ) {
                    Text(
                        text = frame.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp
                        ),
                        color = if (isSelected) OnNeonGreen else TextWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onResetDetail)
                ) {
                    Text(
                        text = "Reset Effects",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        PremiumStudioSlider(
            value = currentValue,
            onValueChange = { newVal ->
                when (activeParam) {
                    DetailParam.VIGNETTE -> onVignetteChange(newVal)
                    DetailParam.GRAIN -> onGrainChange(newVal)
                    DetailParam.SMOOTH -> onSkinSmoothingChange(newVal)
                }
            },
            valueRange = activeParam.min..activeParam.max,
            defaultValue = activeParam.defaultVal,
            label = activeParam.label,
            valueText = "${currentValue.toInt()}%"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailParam.entries.forEach { param ->
                val isSelected = activeParam == param
                val v = when (param) {
                    DetailParam.VIGNETTE -> vignette.toInt()
                    DetailParam.GRAIN -> grain.toInt()
                    DetailParam.SMOOTH -> skinSmoothing.toInt()
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NeonGreenDim else CardBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonGreen else BorderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeParam = param }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = param.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) NeonGreen else TextWhite
                        )
                        Text(
                            text = "$v%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = if (isSelected) NeonGreen else TextMuted
                        )
                    }
                }
            }
        }
    }
}
