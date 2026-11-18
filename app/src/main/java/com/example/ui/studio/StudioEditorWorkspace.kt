package com.example.ui.studio

import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.ui.theme.DarkroomCanvasBackground
import com.example.ui.theme.DarkroomCardSurface
import com.example.ui.theme.DarkroomControlSurface
import com.example.ui.theme.DarkroomCoralAccent
import com.example.ui.theme.DarkroomOnCoral
import com.example.ui.theme.DarkroomSubtleBorder
import com.example.ui.theme.DarkroomTextPrimary
import com.example.ui.theme.DarkroomTextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun StudioEditorWorkspace(
    photo: PhotoEntity,
    allPhotos: List<PhotoEntity> = emptyList(),
    onSelectPhoto: (PhotoEntity) -> Unit = {},
    onPickFromDevice: (() -> Unit)? = null,
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
    val haptic = LocalHapticFeedback.current

    // Non-destructive Edit History auto-restore for previously edited photos
    val restoredHistory = remember(photo.id) {
        StudioPreferenceStore.loadPhotoEditHistory(context, photo.id)
    }

    var currentTab by remember(initialTab) { mutableStateOf(initialTab) }
    var isPanelExpanded by remember { mutableStateOf(false) }
    var splitPosition by remember(photo.id) { mutableFloatStateOf(0.5f) }
    var userRecipes by remember { mutableStateOf(StudioPreferenceStore.loadUserRecipes(context)) }
    var showSaveRecipeDialog by remember { mutableStateOf(false) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

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

    val undoStack = remember(photo.id) { mutableStateListOf<EditSnapshot>() }
    val redoStack = remember(photo.id) { mutableStateListOf<EditSnapshot>() }

    // Live Adjustment Parameters
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

    // Geometry & Framing
    var keystoneVertical by remember(photo.id) { mutableFloatStateOf(restoredHistory?.keystoneVertical ?: 0f) }
    var keystoneHorizontal by remember(photo.id) { mutableFloatStateOf(restoredHistory?.keystoneHorizontal ?: 0f) }
    var selectedFramePreset by remember(photo.id) {
        mutableStateOf(
            runCatching {
                FrameBorderPreset.valueOf(restoredHistory?.frameName ?: FrameBorderPreset.NONE.name)
            }.getOrDefault(FrameBorderPreset.NONE)
        )
    }
    var rotationDegrees by remember(photo.id) { mutableIntStateOf(0) }
    var flipHorizontal by remember(photo.id) { mutableStateOf(false) }
    var flipVertical by remember(photo.id) { mutableStateOf(false) }
    var cropRatio by remember(photo.id) { mutableStateOf(CropAspectRatio.ORIGINAL) }
    var customCropRect by remember(photo.id) { mutableStateOf<RectF?>(null) }

    // Selective HSL Mixer
    val hslBands = remember(photo.id) {
        mutableStateMapOf<HslColorBand, HslBandSetting>().apply {
            HslColorBand.entries.forEach { put(it, HslBandSetting()) }
        }
    }

    // Film LUTs
    var selectedFilter by remember(photo.id) {
        mutableStateOf<StudioFilterItem?>(
            restoredHistory?.filterId?.takeIf { it.isNotEmpty() }?.let { id ->
                CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == id }
            }
        )
    }
    var filterIntensity by remember(photo.id) { mutableFloatStateOf(restoredHistory?.filterIntensity ?: 1f) }

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

    fun applySnapshot(snap: EditSnapshot) {
        exposure = snap.exposure
        brightness = snap.brightness
        contrast = snap.contrast
        saturation = snap.saturation
        highlights = snap.highlights
        shadows = snap.shadows
        temperature = snap.temperature
        tint = snap.tint
        vignette = snap.vignette
        grain = snap.grain
        skinSmoothing = snap.skinSmoothing
        rotationDegrees = snap.rotation
        flipHorizontal = snap.flipH
        flipVertical = snap.flipV
        selectedFilter = CinematicFilterCatalog.ALL_PRESETS.firstOrNull { it.id == snap.filterId }
        filterIntensity = snap.filterIntensity
        keystoneVertical = snap.keystoneV
        keystoneHorizontal = snap.keystoneH
        selectedFramePreset = snap.framePreset
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            redoStack.add(captureSnapshot())
            applySnapshot(undoStack.removeAt(undoStack.lastIndex))
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            undoStack.add(captureSnapshot())
            applySnapshot(redoStack.removeAt(redoStack.lastIndex))
        }
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

    fun saveEditedCopy() {
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

    // Track which workflow tools have active modifications (coral indicator dots)
    val modifiedTabs = remember(
        exposure, brightness, contrast, highlights, shadows,
        temperature, tint, saturation,
        rotationDegrees, flipHorizontal, flipVertical, cropRatio, keystoneVertical, keystoneHorizontal,
        selectedFilter, filterIntensity,
        vignette, grain, skinSmoothing, selectedFramePreset
    ) {
        buildSet {
            if (abs(exposure) > 0.02f || abs(brightness) > 0.5f || abs(contrast - 1f) > 0.02f ||
                abs(highlights) > 0.5f || abs(shadows) > 0.5f
            ) {
                add(EditorToolTab.LIGHT)
            }
            if (rotationDegrees != 0 || flipHorizontal || flipVertical ||
                cropRatio != CropAspectRatio.ORIGINAL || abs(keystoneVertical) > 0.5f || abs(keystoneHorizontal) > 0.5f
            ) {
                add(EditorToolTab.CROP)
            }
            if (abs(temperature) > 0.5f || abs(tint) > 0.5f || abs(saturation - 1f) > 0.02f) {
                add(EditorToolTab.COLOR)
            }
            if (selectedFilter != null && filterIntensity > 0.02f) {
                add(EditorToolTab.FILTERS)
            }
            if (vignette > 0.5f || grain > 0.5f || skinSmoothing > 0.5f || selectedFramePreset != FrameBorderPreset.NONE) {
                add(EditorToolTab.DETAIL)
            }
        }
    }

    // Real-time composite ColorMatrix
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

        val expScale = 2.0.pow(exposure.toDouble()).toFloat()
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

    val openPhotoSwitcher: () -> Unit = {
        if (allPhotos.isNotEmpty()) {
            showPhotoPickerSheet = true
        } else {
            onPickFromDevice?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkroomCanvasBackground)
            .testTag("studio_editor_workspace")
    ) {
        // 1. MINIMAL TOP HEADER BAR: Cancel | Ready | Save Copy
        DarkroomHeaderBar(
            onCancel = onClose,
            onSaveCopy = ::saveEditedCopy,
            onStatusPillClick = openPhotoSwitcher
        )

        // 2. HORIZONTAL WORKFLOW FILMSTRIP: Exposure | Crop | Color Balance | Film LUTs | Effects
        DarkroomWorkflowFilmstrip(
            currentTab = currentTab,
            modifiedTabs = modifiedTabs,
            onSelectTab = { nextTab ->
                if (currentTab != nextTab) {
                    pushUndo()
                    currentTab = nextTab
                }
            }
        )

        // 3. INTERACTIVE MAIN CANVAS AREA WITH BEFORE/AFTER SPLIT SLIDER
        DarkroomBeforeAfterCanvas(
            photo = photo,
            liveColorMatrix = liveColorMatrix,
            cropRatio = cropRatio,
            rotationDegrees = rotationDegrees,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            keystoneVertical = keystoneVertical,
            keystoneHorizontal = keystoneHorizontal,
            vignette = vignette,
            grain = grain,
            selectedFramePreset = selectedFramePreset,
            showCropGrid = currentTab == EditorToolTab.CROP,
            splitPosition = splitPosition,
            onSplitPositionChange = { splitPosition = it },
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onUndo = ::undo,
            onRedo = ::redo,
            activeFilterName = selectedFilter?.name,
            onSwitchPhotoClick = openPhotoSwitcher,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 4. EXPANDABLE DETAILED ADJUSTMENT PANEL (#18181C surface with #252529 border)
        val panelMaxHeight = when {
            isPanelExpanded -> 340.dp
            currentTab == EditorToolTab.FILTERS -> 244.dp
            else -> 236.dp
        }
        val activePanelTitle = when (currentTab) {
            EditorToolTab.LIGHT -> "Exposure & Tone"
            EditorToolTab.CROP -> "Crop & Geometry"
            EditorToolTab.COLOR -> "Color Balance"
            EditorToolTab.FILTERS -> "Film LUTs"
            EditorToolTab.DETAIL -> "Vignette & Grain"
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkroomCardSurface,
            border = BorderStroke(1.dp, DarkroomSubtleBorder),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .testTag("darkroom_adjustment_panel")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expandable Sheet Handle + Active Panel Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DarkroomSubtleBorder)
                            .clickable { isPanelExpanded = !isPanelExpanded }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activePanelTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = DarkroomTextPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (currentTab in modifiedTabs) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = DarkroomControlSurface,
                                    border = BorderStroke(1.dp, DarkroomSubtleBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            pushUndo()
                                            when (currentTab) {
                                                EditorToolTab.LIGHT -> {
                                                    exposure = 0f
                                                    brightness = 0f
                                                    contrast = 1f
                                                    highlights = 0f
                                                    shadows = 0f
                                                    temperature = 0f
                                                }
                                                EditorToolTab.CROP -> {
                                                    cropRatio = CropAspectRatio.ORIGINAL
                                                    rotationDegrees = 0
                                                    flipHorizontal = false
                                                    flipVertical = false
                                                    keystoneVertical = 0f
                                                    keystoneHorizontal = 0f
                                                }
                                                EditorToolTab.COLOR -> {
                                                    temperature = 0f
                                                    tint = 0f
                                                    saturation = 1f
                                                    HslColorBand.entries.forEach { hslBands[it] = HslBandSetting() }
                                                }
                                                EditorToolTab.FILTERS -> {
                                                    selectedFilter = null
                                                    filterIntensity = 1f
                                                }
                                                EditorToolTab.DETAIL -> {
                                                    vignette = 0f
                                                    grain = 0f
                                                    skinSmoothing = 0f
                                                    selectedFramePreset = FrameBorderPreset.NONE
                                                }
                                            }
                                        }
                                        .testTag("darkroom_reset_section_btn")
                                ) {
                                    Text(
                                        text = "Reset",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.5.sp
                                        ),
                                        color = DarkroomCoralAccent,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = DarkroomControlSurface,
                                border = BorderStroke(1.dp, DarkroomSubtleBorder),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { isPanelExpanded = !isPanelExpanded }
                                    .testTag("editor_tray_expand_toggle")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPanelExpanded) {
                                            Icons.Default.KeyboardArrowDown
                                        } else {
                                            Icons.Default.KeyboardArrowUp
                                        },
                                        contentDescription = if (isPanelExpanded) "Collapse panel" else "Expand panel",
                                        tint = DarkroomTextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkroomSubtleBorder, thickness = 0.8.dp)

                // Active Tool Control Body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 148.dp, max = panelMaxHeight)
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
                                initialOffsetX = { w -> (w * 0.12f * dir).toInt() }
                            ) + fadeIn(tween(180))) togetherWith (slideOutHorizontally(
                                animationSpec = tween(130),
                                targetOffsetX = { w -> -(w * 0.08f * dir).toInt() }
                            ) + fadeOut(tween(120)))
                        },
                        label = "darkroom_panel_body_transition",
                        modifier = Modifier.fillMaxWidth()
                    ) { activeTab ->
                        when (activeTab) {
                            EditorToolTab.LIGHT -> {
                                DarkroomExposureSlidersPanel(
                                    brightness = brightness,
                                    onBrightnessChange = { brightness = it },
                                    contrast = contrast,
                                    onContrastChange = { contrast = it },
                                    highlights = highlights,
                                    onHighlightsChange = { highlights = it },
                                    shadows = shadows,
                                    onShadowsChange = { shadows = it },
                                    temperature = temperature,
                                    onTemperatureChange = { temperature = it },
                                    exposure = exposure,
                                    onExposureChange = { exposure = it },
                                    userRecipes = userRecipes,
                                    onApplyRecipe = ::applyRecipe,
                                    onOpenSaveRecipe = { showSaveRecipeDialog = true },
                                    onAutoTone = {
                                        pushUndo()
                                        exposure = 0.15f
                                        brightness = 8f
                                        contrast = 1.12f
                                        highlights = -14f
                                        shadows = 22f
                                    }
                                )
                            }

                            EditorToolTab.COLOR -> {
                                DarkroomColorBalancePanel(
                                    temperature = temperature,
                                    onTemperatureChange = { temperature = it },
                                    tint = tint,
                                    onTintChange = { tint = it },
                                    saturation = saturation,
                                    onSaturationChange = { saturation = it },
                                    brightness = brightness,
                                    onBrightnessChange = { brightness = it },
                                    contrast = contrast,
                                    onContrastChange = { contrast = it },
                                    highlights = highlights,
                                    onHighlightsChange = { highlights = it },
                                    shadows = shadows,
                                    onShadowsChange = { shadows = it },
                                    hslBands = hslBands,
                                    onUpdateHslBand = { band, setting -> hslBands[band] = setting }
                                )
                            }

                            EditorToolTab.FILTERS -> {
                                DarkroomFilmLutCarouselPanel(
                                    previewImageSource = photo.filePath,
                                    selectedFilter = selectedFilter,
                                    filterIntensity = filterIntensity,
                                    onSelectFilter = { next ->
                                        pushUndo()
                                        selectedFilter = next
                                    },
                                    onIntensityChange = { filterIntensity = it }
                                )
                            }

                            EditorToolTab.CROP -> {
                                DarkroomCropControlPanel(
                                    cropRatio = cropRatio,
                                    rotationDegrees = rotationDegrees,
                                    flipHorizontal = flipHorizontal,
                                    flipVertical = flipVertical,
                                    keystoneVertical = keystoneVertical,
                                    keystoneHorizontal = keystoneHorizontal,
                                    onSelectCropRatio = {
                                        pushUndo()
                                        cropRatio = it
                                    },
                                    onRotateLeft = {
                                        pushUndo()
                                        rotationDegrees = (rotationDegrees - 90 + 360) % 360
                                    },
                                    onRotateRight = {
                                        pushUndo()
                                        rotationDegrees = (rotationDegrees + 90) % 360
                                    },
                                    onFlipH = {
                                        pushUndo()
                                        flipHorizontal = !flipHorizontal
                                    },
                                    onFlipV = {
                                        pushUndo()
                                        flipVertical = !flipVertical
                                    },
                                    onKeystoneVChange = { keystoneVertical = it },
                                    onKeystoneHChange = { keystoneHorizontal = it }
                                )
                            }

                            EditorToolTab.DETAIL -> {
                                DarkroomEffectsControlPanel(
                                    vignette = vignette,
                                    onVignetteChange = { vignette = it },
                                    grain = grain,
                                    onGrainChange = { grain = it },
                                    skinSmoothing = skinSmoothing,
                                    onSkinSmoothingChange = { skinSmoothing = it },
                                    selectedFrame = selectedFramePreset,
                                    onSelectFrame = {
                                        pushUndo()
                                        selectedFramePreset = it
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. DEDICATED CLEARANCE FOR UNIFIED FLOATING BOTTOM NAV PILL (Photos | Albums | Studio)
        // Guarantees the anchored floating dock sits clearly below the adjustment panel with zero overlap
        Spacer(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(86.dp)
        )
    }

    // Photo Switcher Sheet
    if (showPhotoPickerSheet) {
        StudioPhotoPickerSheet(
            photos = allPhotos,
            selectedIds = setOf(photo.id),
            isMultiSelect = false,
            title = "Select Photo to Edit",
            onDismiss = { showPhotoPickerSheet = false },
            onSelectSinglePhoto = { chosen ->
                onSelectPhoto(chosen)
                showPhotoPickerSheet = false
            },
            onPickFromDevice = onPickFromDevice?.let { pick ->
                {
                    showPhotoPickerSheet = false
                    pick()
                }
            }
        )
    }

    // Save Custom Recipe Dialog
    if (showSaveRecipeDialog) {
        var recipeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveRecipeDialog = false },
            containerColor = DarkroomCardSurface,
            title = {
                Text(
                    text = "Save Custom Darkroom Recipe",
                    fontWeight = FontWeight.Bold,
                    color = DarkroomTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Save your current tone, color temperature, LUT, and grain settings as a reusable 1-tap preset.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkroomTextSecondary
                    )
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Recipe Name (e.g., Warm 35mm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = recipeName.trim().ifEmpty { "Darkroom Look ${userRecipes.size + 1}" }
                        userRecipes = StudioPreferenceStore.saveUserRecipe(
                            context,
                            buildCurrentRecipe(finalName)
                        )
                        showSaveRecipeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkroomCoralAccent,
                        contentColor = DarkroomOnCoral
                    )
                ) {
                    Text("Save Recipe", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveRecipeDialog = false }) {
                    Text("Cancel", color = DarkroomTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DarkroomExposureSlidersPanel(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    highlights: Float,
    onHighlightsChange: (Float) -> Unit,
    shadows: Float,
    onShadowsChange: (Float) -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    exposure: Float,
    onExposureChange: (Float) -> Unit,
    userRecipes: List<StudioCustomRecipe>,
    onApplyRecipe: (StudioCustomRecipe) -> Unit,
    onOpenSaveRecipe: () -> Unit,
    onAutoTone: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action & Saved Recipes Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item(key = "auto_tone") {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DarkroomCoralAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, DarkroomCoralAccent.copy(alpha = 0.45f)),
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DarkroomCoralAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Auto Tone",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DarkroomCoralAccent
                        )
                    }
                }
            }

            item(key = "save_recipe") {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DarkroomControlSurface,
                    border = BorderStroke(1.dp, DarkroomSubtleBorder),
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
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DarkroomTextPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Save Recipe",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DarkroomTextPrimary
                        )
                    }
                }
            }

            items(userRecipes, key = { it.id }) { recipe ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DarkroomControlSurface,
                    border = BorderStroke(1.dp, DarkroomSubtleBorder),
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
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = DarkroomCoralAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = DarkroomTextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Core 5 Fine-Tuning Sliders: Brightness, Contrast, Highlights, Shadows, Color Temperature
        DarkroomAdjustmentSliderRow(
            label = stringResource(R.string.darkroom_slider_brightness),
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = -80f..80f,
            defaultValue = 0f,
            valueText = formatSignedInt(brightness),
            testTagKey = "brightness"
        )
        DarkroomAdjustmentSliderRow(
            label = stringResource(R.string.darkroom_slider_contrast),
            value = contrast,
            onValueChange = onContrastChange,
            valueRange = 0.5f..1.8f,
            defaultValue = 1f,
            valueText = String.format(Locale.US, "%.0f%%", contrast * 100f),
            testTagKey = "contrast"
        )
        DarkroomAdjustmentSliderRow(
            label = stringResource(R.string.darkroom_slider_highlights),
            value = highlights,
            onValueChange = onHighlightsChange,
            valueRange = -100f..100f,
            defaultValue = 0f,
            valueText = formatSignedInt(highlights),
            testTagKey = "highlights"
        )
        DarkroomAdjustmentSliderRow(
            label = stringResource(R.string.darkroom_slider_shadows),
            value = shadows,
            onValueChange = onShadowsChange,
            valueRange = -100f..100f,
            defaultValue = 0f,
            valueText = formatSignedInt(shadows),
            testTagKey = "shadows"
        )
        DarkroomAdjustmentSliderRow(
            label = stringResource(R.string.darkroom_slider_color_temp),
            value = temperature,
            onValueChange = onTemperatureChange,
            valueRange = -50f..50f,
            defaultValue = 0f,
            valueText = formatKelvinOffset(temperature),
            testTagKey = "color_temperature"
        )
        DarkroomAdjustmentSliderRow(
            label = "Exposure EV",
            value = exposure,
            onValueChange = onExposureChange,
            valueRange = -2f..2f,
            defaultValue = 0f,
            valueText = String.format(Locale.US, "%+.1f EV", exposure),
            testTagKey = "exposure_ev"
        )
    }
}

@Composable
private fun DarkroomColorBalancePanel(
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    tint: Float,
    onTintChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    highlights: Float,
    onHighlightsChange: (Float) -> Unit,
    shadows: Float,
    onShadowsChange: (Float) -> Unit,
    hslBands: Map<HslColorBand, HslBandSetting>,
    onUpdateHslBand: (HslColorBand, HslBandSetting) -> Unit
) {
    var isHslMixerMode by remember { mutableStateOf(false) }
    var activeHslBand by remember { mutableStateOf(HslColorBand.ORANGE) }
    var activeHslChannel by remember { mutableIntStateOf(1) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Switcher Pills (Sliders vs HSL Mixer)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ColorModePill(
                label = "Color Sliders",
                selected = !isHslMixerMode,
                onClick = { isHslMixerMode = false }
            )
            ColorModePill(
                label = "HSL Mixer",
                selected = isHslMixerMode,
                onClick = { isHslMixerMode = true },
                modifier = Modifier.testTag("editor_hsl_mixer_toggle")
            )
        }

        if (!isHslMixerMode) {
            DarkroomAdjustmentSliderRow(
                label = stringResource(R.string.darkroom_slider_color_temp),
                value = temperature,
                onValueChange = onTemperatureChange,
                valueRange = -50f..50f,
                defaultValue = 0f,
                valueText = formatKelvinOffset(temperature),
                testTagKey = "color_tab_temp"
            )
            DarkroomAdjustmentSliderRow(
                label = "Tint",
                value = tint,
                onValueChange = onTintChange,
                valueRange = -50f..50f,
                defaultValue = 0f,
                valueText = formatSignedInt(tint),
                testTagKey = "color_tab_tint"
            )
            DarkroomAdjustmentSliderRow(
                label = "Saturation",
                value = saturation,
                onValueChange = onSaturationChange,
                valueRange = 0f..2f,
                defaultValue = 1f,
                valueText = String.format(Locale.US, "%.0f%%", saturation * 100f),
                testTagKey = "color_tab_saturation"
            )
            DarkroomAdjustmentSliderRow(
                label = stringResource(R.string.darkroom_slider_brightness),
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = -80f..80f,
                defaultValue = 0f,
                valueText = formatSignedInt(brightness),
                testTagKey = "color_tab_brightness"
            )
            DarkroomAdjustmentSliderRow(
                label = stringResource(R.string.darkroom_slider_contrast),
                value = contrast,
                onValueChange = onContrastChange,
                valueRange = 0.5f..1.8f,
                defaultValue = 1f,
                valueText = String.format(Locale.US, "%.0f%%", contrast * 100f),
                testTagKey = "color_tab_contrast"
            )
            DarkroomAdjustmentSliderRow(
                label = stringResource(R.string.darkroom_slider_highlights),
                value = highlights,
                onValueChange = onHighlightsChange,
                valueRange = -100f..100f,
                defaultValue = 0f,
                valueText = formatSignedInt(highlights),
                testTagKey = "color_tab_highlights"
            )
            DarkroomAdjustmentSliderRow(
                label = stringResource(R.string.darkroom_slider_shadows),
                value = shadows,
                onValueChange = onShadowsChange,
                valueRange = -100f..100f,
                defaultValue = 0f,
                valueText = formatSignedInt(shadows),
                testTagKey = "color_tab_shadows"
            )
        } else {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                                width = if (isSelected) 2.2.dp else 1.dp,
                                color = if (isSelected) Color.White else DarkroomSubtleBorder,
                                shape = CircleShape
                            )
                            .clickable { activeHslBand = band }
                    )
                }
            }

            DarkroomAdjustmentSliderRow(
                label = channelName,
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
                valueText = formatSignedInt(hslVal),
                testTagKey = "hsl_channel"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hue" to 0, "Saturation" to 1, "Luminance" to 2).forEach { (label, idx) ->
                    val selected = activeHslChannel == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) DarkroomCoralAccent.copy(alpha = 0.16f) else DarkroomControlSurface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) DarkroomCoralAccent else DarkroomSubtleBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { activeHslChannel = idx }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) DarkroomCoralAccent else DarkroomTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) DarkroomCoralAccent else DarkroomControlSurface,
        border = BorderStroke(1.dp, if (selected) DarkroomCoralAccent else DarkroomSubtleBorder),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) DarkroomOnCoral else DarkroomTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun DarkroomCropControlPanel(
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
    onKeystoneHChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Aspect Ratio Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CropAspectRatio.entries) { ratio ->
                val isSelected = cropRatio == ratio
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) DarkroomCoralAccent else DarkroomControlSurface,
                    label = "crop_bg_${ratio.name}"
                )
                val pillText by animateColorAsState(
                    targetValue = if (isSelected) DarkroomOnCoral else DarkroomTextPrimary,
                    label = "crop_text_${ratio.name}"
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = pillBg,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) DarkroomCoralAccent else DarkroomSubtleBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectCropRatio(ratio) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = pillText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = ratio.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = pillText
                        )
                    }
                }
            }
        }

        // Transform Buttons: -90°, +90°, Flip H, Flip V
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DarkroomGeometryButton(
                icon = Icons.AutoMirrored.Filled.RotateLeft,
                label = "-90°",
                isActive = false,
                onClick = onRotateLeft,
                modifier = Modifier.weight(1f)
            )
            DarkroomGeometryButton(
                icon = Icons.AutoMirrored.Filled.RotateRight,
                label = "+90°",
                isActive = rotationDegrees != 0,
                onClick = onRotateRight,
                modifier = Modifier.weight(1f)
            )
            DarkroomGeometryButton(
                icon = Icons.Default.Flip,
                label = "Flip H",
                isActive = flipHorizontal,
                onClick = onFlipH,
                modifier = Modifier.weight(1f)
            )
            DarkroomGeometryButton(
                icon = Icons.Default.Flip,
                label = "Flip V",
                isActive = flipVertical,
                onClick = onFlipV,
                modifier = Modifier.weight(1f)
            )
        }

        DarkroomAdjustmentSliderRow(
            label = "Vertical Perspective",
            value = keystoneVertical,
            onValueChange = onKeystoneVChange,
            valueRange = -25f..25f,
            defaultValue = 0f,
            valueText = String.format(Locale.US, "%+.0f°", keystoneVertical),
            testTagKey = "keystone_v"
        )
        DarkroomAdjustmentSliderRow(
            label = "Horizontal Perspective",
            value = keystoneHorizontal,
            onValueChange = onKeystoneHChange,
            valueRange = -25f..25f,
            defaultValue = 0f,
            valueText = String.format(Locale.US, "%+.0f°", keystoneHorizontal),
            testTagKey = "keystone_h"
        )
    }
}

@Composable
private fun DarkroomGeometryButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) DarkroomCoralAccent.copy(alpha = 0.16f) else DarkroomControlSurface,
        border = BorderStroke(
            1.dp,
            if (isActive) DarkroomCoralAccent else DarkroomSubtleBorder
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
                tint = if (isActive) DarkroomCoralAccent else DarkroomTextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                ),
                color = if (isActive) DarkroomCoralAccent else DarkroomTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DarkroomEffectsControlPanel(
    vignette: Float,
    onVignetteChange: (Float) -> Unit,
    grain: Float,
    onGrainChange: (Float) -> Unit,
    skinSmoothing: Float,
    onSkinSmoothingChange: (Float) -> Unit,
    selectedFrame: FrameBorderPreset,
    onSelectFrame: (FrameBorderPreset) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FrameBorderPreset.entries) { frame ->
                val isSelected = selectedFrame == frame
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) DarkroomCoralAccent else DarkroomControlSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) DarkroomCoralAccent else DarkroomSubtleBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectFrame(frame) }
                ) {
                    Text(
                        text = frame.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) DarkroomOnCoral else DarkroomTextSecondary,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }
        }

        DarkroomAdjustmentSliderRow(
            label = "Vignette",
            value = vignette,
            onValueChange = onVignetteChange,
            valueRange = 0f..100f,
            defaultValue = 0f,
            valueText = "${vignette.toInt()}%",
            testTagKey = "vignette"
        )
        DarkroomAdjustmentSliderRow(
            label = "Film Grain",
            value = grain,
            onValueChange = onGrainChange,
            valueRange = 0f..100f,
            defaultValue = 0f,
            valueText = "${grain.toInt()}%",
            testTagKey = "film_grain"
        )
        DarkroomAdjustmentSliderRow(
            label = "Soft Focus",
            value = skinSmoothing,
            onValueChange = onSkinSmoothingChange,
            valueRange = 0f..100f,
            defaultValue = 0f,
            valueText = "${skinSmoothing.toInt()}%",
            testTagKey = "soft_focus"
        )
    }
}

private fun formatSignedInt(value: Float): String {
    val rounded = value.toInt()
    return when {
        rounded > 0 -> "+$rounded"
        else -> "$rounded"
    }
}

private fun formatKelvinOffset(temp: Float): String {
    val kelvin = (5500 + (temp * 40f).toInt()).coerceIn(3500, 7500)
    return if (abs(temp) < 0.5f) "${kelvin}K" else "${formatSignedInt(temp)} · ${kelvin}K"
}
