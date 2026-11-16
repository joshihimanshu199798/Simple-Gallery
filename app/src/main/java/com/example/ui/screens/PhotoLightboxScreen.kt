package com.example.ui.screens

import android.media.ExifInterface
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale as CoilScale
import coil.size.Size as CoilSize
import com.example.data.AlbumSummary
import com.example.data.PhotoEntity
import com.example.data.PhotoFilterPreset
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.LocalCompleteUiTheme
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Rich optical & file EXIF telemetry extracted from the local image file via [ExifInterface]
 * merged with stored [PhotoEntity] camera metadata.
 */
data class DetailedExifTelemetry(
    val cameraModel: String,
    val cameraMake: String,
    val lensSpec: String,
    val aperture: String,
    val apertureDescription: String,
    val shutterSpeed: String,
    val shutterDescription: String,
    val iso: String,
    val isoDescription: String,
    val focalLength: String,
    val focalEquivalent35mm: String,
    val exposureBias: String,
    val meteringMode: String,
    val whiteBalance: String,
    val flashStatus: String,
    val colorSpace: String,
    val orientationLabel: String,
    val aspectRatioLabel: String,
    val softwarePipeline: String
)

@Composable
fun rememberDetailedExifTelemetry(photo: PhotoEntity): DetailedExifTelemetry {
    val context = LocalContext.current
    return remember(
        photo.id,
        photo.filePath,
        photo.cameraModel,
        photo.aperture,
        photo.shutterSpeed,
        photo.iso,
        photo.focalLength,
        photo.appliedFilter,
        photo.width,
        photo.height
    ) {
        val exif = PhotoImageProcessor.readExifFromPathOrUri(context, photo.filePath)

        val rawMake = exif?.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
        val cameraMake = rawMake.ifEmpty {
            photo.cameraModel.substringBefore(" ").ifEmpty { "Optical Studio" }
        }

        val fNumNumber = photo.aperture
            .removePrefix("f/")
            .removePrefix("F/")
            .toFloatOrNull() ?: 1.8f
        val apertureDesc = when {
            fNumNumber <= 1.8f -> "Wide Open • Shallow DOF"
            fNumNumber <= 2.8f -> "Fast Prime • Soft Bokeh"
            fNumNumber <= 5.6f -> "Balanced Sharpness"
            else -> "Deep Landscape Focus"
        }

        val shutterDesc = when {
            photo.shutterSpeed.contains("1/4000") ||
                photo.shutterSpeed.contains("1/2000") ||
                photo.shutterSpeed.contains("1/1000") -> "High-Speed Action Freeze"
            photo.shutterSpeed.contains("1/500") ||
                photo.shutterSpeed.contains("1/320") ||
                photo.shutterSpeed.contains("1/250") -> "Crisp Handheld Exposure"
            photo.shutterSpeed.contains("1/125") ||
                photo.shutterSpeed.contains("1/60") -> "Standard Daylight Shutter"
            else -> "Low-Light / Long Exposure"
        }

        val isoValue = photo.iso.filter { it.isDigit() }.toIntOrNull() ?: 100
        val isoDesc = when {
            isoValue <= 100 -> "Base Native ISO • Max DR"
            isoValue <= 400 -> "Clean Fine-Grain Sensor"
            isoValue <= 800 -> "Balanced Low-Light Gain"
            else -> "High-Sensitivity Night Gain"
        }

        val focalMm = photo.focalLength.filter { it.isDigit() }.toIntOrNull() ?: 26
        val fovType = when {
            focalMm <= 20 -> "Ultra-Wide Angle"
            focalMm <= 30 -> "Wide Standard Prime"
            focalMm <= 55 -> "Natural 50mm Perspective"
            else -> "Portrait / Telephoto Optics"
        }

        val expBiasAttr = exif?.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)?.trim().orEmpty()
        val exposureBias = if (expBiasAttr.isNotEmpty() && expBiasAttr != "0" && expBiasAttr != "0/1") {
            "${expBiasAttr} EV"
        } else {
            "0.0 EV (Balanced)"
        }

        val meteringInt = exif?.getAttributeInt(ExifInterface.TAG_METERING_MODE, -1) ?: -1
        val meteringMode = when (meteringInt) {
            2 -> "Center-Weighted"
            3 -> "Spot Metering"
            4 -> "Multi-Spot"
            else -> "Matrix / Evaluative"
        }

        val wbInt = exif?.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1) ?: -1
        val whiteBalance = when (wbInt) {
            1 -> "Manual Kelvin WB"
            else -> "Auto White Balance (AWB)"
        }

        val flashInt = exif?.getAttributeInt(ExifInterface.TAG_FLASH, 0) ?: 0
        val flashStatus = if ((flashInt and 1) != 0) "Fired • Strobe Sync" else "Off • Natural Ambient"

        val colorSpaceInt = exif?.getAttributeInt(ExifInterface.TAG_COLOR_SPACE, 1) ?: 1
        val colorSpace = if (colorSpaceInt == 1) {
            "sRGB IEC61966-2.1"
        } else {
            "Display P3 / Wide Gamut"
        }

        val orientInt = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            ?: ExifInterface.ORIENTATION_NORMAL
        val orientationLabel = when (orientInt) {
            ExifInterface.ORIENTATION_ROTATE_90 -> "Rotated 90° CW"
            ExifInterface.ORIENTATION_ROTATE_180 -> "Rotated 180°"
            ExifInterface.ORIENTATION_ROTATE_270 -> "Rotated 270° CW"
            else -> if (photo.width >= photo.height) "Horizontal (Landscape)" else "Vertical (Portrait)"
        }

        fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
        val w = photo.width.coerceAtLeast(1)
        val h = photo.height.coerceAtLeast(1)
        val divisor = gcd(w, h).coerceAtLeast(1)
        val rw = w / divisor
        val rh = h / divisor
        val aspectRatioLabel = if (rw <= 20 && rh <= 20) {
            "$rw:$rh"
        } else {
            String.format(Locale.US, "%.2f:1", w.toFloat() / h.toFloat())
        }

        val lensSpec = "${photo.focalLength} ${photo.aperture} • $fovType"
        val softwarePipeline = if (photo.appliedFilter != "Original") {
            "Darkroom Studio (${photo.appliedFilter})"
        } else {
            exif?.getAttribute(ExifInterface.TAG_SOFTWARE)?.takeIf { it.isNotBlank() }
                ?: "Optical RAW-to-JPEG Engine"
        }

        DetailedExifTelemetry(
            cameraModel = photo.cameraModel,
            cameraMake = cameraMake,
            lensSpec = lensSpec,
            aperture = photo.aperture,
            apertureDescription = apertureDesc,
            shutterSpeed = photo.shutterSpeed,
            shutterDescription = shutterDesc,
            iso = photo.iso,
            isoDescription = isoDesc,
            focalLength = photo.focalLength,
            focalEquivalent35mm = "${focalMm}mm ($fovType)",
            exposureBias = exposureBias,
            meteringMode = meteringMode,
            whiteBalance = whiteBalance,
            flashStatus = flashStatus,
            colorSpace = colorSpace,
            orientationLabel = orientationLabel,
            aspectRatioLabel = aspectRatioLabel,
            softwarePipeline = softwarePipeline
        )
    }
}

private val StudioGold = Color(0xFFF5B400)
private val StudioEmerald = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PhotoLightboxScreen(
    photo: PhotoEntity,
    currentList: List<PhotoEntity>,
    albums: List<AlbumSummary>,
    histogram: List<Float>,
    isSlideshowPlaying: Boolean,
    slideshowIntervalSec: Int,
    onClose: () -> Unit,
    onSelectPhoto: (PhotoEntity) -> Unit,
    onToggleSlideshow: () -> Unit,
    onCycleSlideshowInterval: () -> Unit,
    onToggleFavorite: (PhotoEntity) -> Unit,
    onOpenEditor: (PhotoEntity) -> Unit,
    onCreateMemory: (PhotoEntity) -> Unit,
    onSharePhoto: (PhotoEntity) -> Unit,
    onMoveOrCopyAlbum: (PhotoEntity, String, Boolean) -> Unit,
    onToggleVaultLock: (PhotoEntity) -> Unit,
    onMoveToTrash: (PhotoEntity) -> Unit,
    onUpdateMetadata: (
        PhotoEntity,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit,
    onAnalyzeWithAi: (PhotoEntity) -> Unit = {},
    onOpenCollageStudio: () -> Unit = {},
    onApplyQuickLut: (PhotoEntity, PhotoFilterPreset, Boolean) -> Unit = { _, _, _ -> },
    onShrinkPhoto: (PhotoEntity, Float, Int, Boolean) -> Unit = { _, _, _, _ -> },
    onCreateFramedPhoto: (PhotoEntity, String, Boolean) -> Unit = { _, _, _ -> },
    onStampWatermark: (PhotoEntity, String, Boolean) -> Unit = { _, _, _ -> },
    onQuickRotate90: (PhotoEntity) -> Unit = {},
    onDuplicatePhoto: (PhotoEntity) -> Unit = {},
    onOpenCompare: () -> Unit = {},
    onOpenCropStudio: (PhotoEntity) -> Unit = onOpenEditor,
    onDeletePermanently: (PhotoEntity) -> Unit = onMoveToTrash,
    faceTags: List<com.example.data.PhotoFaceTagEntity> = emptyList(),
    personClusters: List<com.example.data.PersonClusterEntity> = emptyList(),
    onStripExifAndShare: (PhotoEntity) -> Unit = onSharePhoto,
    onOpenPersonDetail: (com.example.data.PersonClusterEntity) -> Unit = {}
) {
    BackHandler { onClose() }

    val currentIndex = remember(photo.id, currentList) {
        currentList.indexOfFirst { it.id == photo.id }.coerceAtLeast(0)
    }

    var isMotionActive by remember { mutableStateOf(false) }

    val minZoom = 1f
    val maxZoom = 6f
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var targetZoom by remember(photo.id) { mutableFloatStateOf(1f) }
    var offsetX by remember(photo.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(photo.id) { mutableFloatStateOf(0f) }
    var unzoomedSwipeAccumulatorX by remember(photo.id) { mutableFloatStateOf(0f) }
    var unzoomedSwipeAccumulatorY by remember(photo.id) { mutableFloatStateOf(0f) }
    var zoomedDuringGesture by remember(photo.id) { mutableStateOf(false) }
    var showExifSheet by remember { mutableStateOf(false) }

    fun maxPanOffset(scale: Float, dimensionPx: Int): Float {
        if (scale <= 1.01f) return 0f
        val baseSize = if (dimensionPx > 0) dimensionPx.toFloat() else 1080f
        return (baseSize * (scale - 1f)) / 2f
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (targetZoom * zoomChange).coerceIn(minZoom, maxZoom)
        if (abs(zoomChange - 1f) > 0.004f || newScale > 1.02f) {
            zoomedDuringGesture = true
        }
        val scaleRatio = if (targetZoom > 0f) newScale / targetZoom else 1f
        targetZoom = newScale

        if (newScale > 1.01f) {
            val maxX = maxPanOffset(newScale, viewportSize.width)
            val maxY = maxPanOffset(newScale, viewportSize.height)
            offsetX = (offsetX * scaleRatio + panChange.x).coerceIn(-maxX, maxX)
            offsetY = (offsetY * scaleRatio + panChange.y).coerceIn(-maxY, maxY)
            unzoomedSwipeAccumulatorX = 0f
            unzoomedSwipeAccumulatorY = 0f
        } else {
            offsetX = 0f
            offsetY = 0f
            if (!zoomedDuringGesture) {
                if (abs(panChange.x) > abs(panChange.y)) {
                    unzoomedSwipeAccumulatorX += panChange.x
                } else {
                    unzoomedSwipeAccumulatorY += panChange.y
                }
            }
        }
    }

    val isGestureTransforming = transformableState.isTransformInProgress

    LaunchedEffect(isGestureTransforming) {
        if (isGestureTransforming) {
            unzoomedSwipeAccumulatorX = 0f
            unzoomedSwipeAccumulatorY = 0f
            zoomedDuringGesture = targetZoom > 1.02f
        } else {
            if (targetZoom <= 1.03f) {
                targetZoom = 1f
                offsetX = 0f
                offsetY = 0f
                val swipeThreshold = ((viewportSize.width.takeIf { it > 0 } ?: 600) * 0.18f)
                    .coerceIn(80f, 180f)
                val verticalThreshold = ((viewportSize.height.takeIf { it > 0 } ?: 900) * 0.14f)
                    .coerceIn(95f, 180f)
                if (!zoomedDuringGesture) {
                    if (abs(unzoomedSwipeAccumulatorX) >= abs(unzoomedSwipeAccumulatorY) && currentList.size > 1) {
                        if (unzoomedSwipeAccumulatorX <= -swipeThreshold && currentIndex < currentList.lastIndex) {
                            onSelectPhoto(currentList[currentIndex + 1])
                        } else if (unzoomedSwipeAccumulatorX >= swipeThreshold && currentIndex > 0) {
                            onSelectPhoto(currentList[currentIndex - 1])
                        }
                    } else if (unzoomedSwipeAccumulatorY >= verticalThreshold) {
                        onClose()
                    } else if (unzoomedSwipeAccumulatorY <= -verticalThreshold) {
                        showExifSheet = true
                    }
                }
            } else {
                val maxX = maxPanOffset(targetZoom, viewportSize.width)
                val maxY = maxPanOffset(targetZoom, viewportSize.height)
                offsetX = offsetX.coerceIn(-maxX, maxX)
                offsetY = offsetY.coerceIn(-maxY, maxY)
            }
            unzoomedSwipeAccumulatorX = 0f
            unzoomedSwipeAccumulatorY = 0f
            zoomedDuringGesture = false
        }
    }

    // Slideshow or Cinema Motion auto-advance effect (paused while actively zoomed in)
    LaunchedEffect(
        isSlideshowPlaying,
        isMotionActive,
        photo.id,
        slideshowIntervalSec,
        currentList.size,
        targetZoom > 1.05f
    ) {
        if ((isSlideshowPlaying || isMotionActive) && currentList.size > 1 && targetZoom <= 1.05f) {
            val waitMs = if (isMotionActive) 3800L else slideshowIntervalSec * 1000L
            delay(waitMs)
            val nextIdx = (currentIndex + 1) % currentList.size
            onSelectPhoto(currentList[nextIdx])
        }
    }

    val gestureSpringSpec = remember(isGestureTransforming) {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (isGestureTransforming) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        )
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = gestureSpringSpec,
        label = "smooth_zoom_scale"
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = gestureSpringSpec,
        label = "smooth_zoom_offset_x"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = gestureSpringSpec,
        label = "smooth_zoom_offset_y"
    )

    var controlsVisible by remember { mutableStateOf(true) }
    var showFilmstripScrubber by remember { mutableStateOf(false) }
    var showExifHudOverlay by remember { mutableStateOf(false) }
    var showFaceTags by remember { mutableStateOf(false) }
    var showMoveAlbumDialog by remember { mutableStateOf(false) }
    var showLutDrawer by remember { mutableStateOf(false) }
    var showFrameDrawer by remember { mutableStateOf(false) }
    var showStampDrawer by remember { mutableStateOf(false) }
    var showShrinkSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSplitLoupe by remember { mutableStateOf(false) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var previewLutPreset by remember(photo.id) { mutableStateOf(PhotoFilterPreset.ORIGINAL) }
    var previewFrameStyle by remember(photo.id) { mutableStateOf("Museum White Mat") }

    val exifTelemetry = rememberDetailedExifTelemetry(photo)
    val filmstripListState = rememberLazyListState()

    // Smoothly center the active photo in the bottom filmstrip carousel
    LaunchedEffect(currentIndex, currentList.size) {
        if (currentList.isNotEmpty() && currentIndex in currentList.indices) {
            filmstripListState.animateScrollToItem(
                index = (currentIndex - 2).coerceAtLeast(0)
            )
        }
    }

    // Cinema Motion (Ken Burns smooth pan & zoom effect)
    val infiniteTransition = rememberInfiniteTransition(label = "cinema_motion_transition")
    val motionScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motion_scale"
    )
    val motionPanX by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motion_pan_x"
    )

    val liveColorFilter = remember(previewLutPreset) {
        if (previewLutPreset == PhotoFilterPreset.ORIGINAL) {
            null
        } else {
            val androidMatrix = PhotoImageProcessor.buildCombinedColorMatrix(
                preset = previewLutPreset,
                brightness = 0f,
                contrast = 1f,
                saturation = 1f
            )
            ColorFilter.colorMatrix(ColorMatrix(androidMatrix.array))
        }
    }

    val context = LocalContext.current
    var isLoadingHighRes by remember(photo.id) { mutableStateOf(false) }
    var highResLoadError by remember(photo.id) { mutableStateOf(false) }
    var decodedResolutionLabel by remember(photo.id) {
        mutableStateOf(
            if (photo.width > 0 && photo.height > 0) "${photo.width} × ${photo.height}" else null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("photo_lightbox_screen")
    ) {
        // 1. Edge-to-Edge Full-Screen High-Resolution Zoomable & Swipeable Canvas
        // Occupies the entire viewport at all times so hiding/showing chrome causes zero layout jump.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewportSize = it }
        ) {
            Crossfade(
                targetState = photo,
                animationSpec = tween(durationMillis = 220),
                label = "lightbox_photo_crossfade"
            ) { targetPhoto ->
                val coilModel = remember(targetPhoto.id, targetPhoto.filePath, targetPhoto.sizeBytes) {
                    PhotoImageProcessor.resolveCoilModel(targetPhoto.filePath)
                }
                val highResRequest = remember(coilModel, targetPhoto.id, targetPhoto.width, targetPhoto.height) {
                    val useOriginalBounds = targetPhoto.width in 1..4096 && targetPhoto.height in 1..4096
                    ImageRequest.Builder(context)
                        .data(coilModel)
                        .apply {
                            if (useOriginalBounds) {
                                size(CoilSize.ORIGINAL)
                                precision(Precision.EXACT)
                            } else {
                                // Decode up to 4K (4096px longest edge) to keep massive multi-megapixel files crisp & GPU-safe
                                size(4096, 4096)
                                scale(CoilScale.FIT)
                                precision(Precision.INEXACT)
                            }
                        }
                        .crossfade(false)
                        .build()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("lightbox_zoomable_canvas")
                        .pointerInput(targetPhoto.id) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    if (targetZoom > 1.05f) {
                                        targetZoom = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    } else {
                                        val doubleTapZoom = 2.75f
                                        targetZoom = doubleTapZoom
                                        val maxX = maxPanOffset(doubleTapZoom, size.width)
                                        val maxY = maxPanOffset(doubleTapZoom, size.height)
                                        if (size.width > 0 && size.height > 0) {
                                            val dx = tapOffset.x - (size.width / 2f)
                                            val dy = tapOffset.y - (size.height / 2f)
                                            offsetX = (-dx * (doubleTapZoom - 1f)).coerceIn(-maxX, maxX)
                                            offsetY = (-dy * (doubleTapZoom - 1f)).coerceIn(-maxY, maxY)
                                        }
                                    }
                                },
                                onTap = {
                                    controlsVisible = !controlsVisible
                                }
                            )
                        }
                        .transformable(state = transformableState),
                    contentAlignment = Alignment.Center
                ) {
                    val liveScale = if (isGestureTransforming) targetZoom else animatedScale
                    val livePanX = if (isGestureTransforming) offsetX else animatedOffsetX
                    val livePanY = if (isGestureTransforming) offsetY else animatedOffsetY

                    val effectiveScale = if (isMotionActive && targetZoom <= 1.05f) {
                        liveScale * motionScale
                    } else {
                        liveScale
                    }
                    val effectiveOffsetX = if (isMotionActive && targetZoom <= 1.05f) {
                        livePanX + motionPanX
                    } else {
                        livePanX
                    }

                    val frameBorderPadding = if (showFrameDrawer) 14.dp else 0.dp
                    val frameBgColor = when (previewFrameStyle) {
                        "35mm Film" -> Color(0xFF121212)
                        "Gold Gallery" -> Color(0xFF1A150E)
                        "Obsidian Mat" -> Color(0xFF141820)
                        "Sakura Press" -> Color(0xFFFFF0F5)
                        else -> Color(0xFFFAF8F5)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (showFrameDrawer) {
                                    Modifier
                                        .padding(horizontal = 16.dp, vertical = 72.dp)
                                        .background(frameBgColor, RoundedCornerShape(14.dp))
                                        .border(2.dp, StudioGold, RoundedCornerShape(14.dp))
                                        .padding(frameBorderPadding)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = highResRequest,
                            contentDescription = targetPhoto.title,
                            contentScale = ContentScale.Fit,
                            colorFilter = if (showSplitLoupe) null else liveColorFilter,
                            onLoading = {
                                if (targetPhoto.id == photo.id) {
                                    isLoadingHighRes = true
                                    highResLoadError = false
                                }
                            },
                            onSuccess = { state ->
                                if (targetPhoto.id == photo.id) {
                                    isLoadingHighRes = false
                                    highResLoadError = false
                                    val drawable = state.result.drawable
                                    val w = drawable.intrinsicWidth
                                    val h = drawable.intrinsicHeight
                                    if (w > 0 && h > 0) {
                                        val bestW = maxOf(w, targetPhoto.width)
                                        val bestH = maxOf(h, targetPhoto.height)
                                        decodedResolutionLabel = "$bestW × $bestH"
                                    }
                                }
                            },
                            onError = {
                                if (targetPhoto.id == photo.id) {
                                    isLoadingHighRes = false
                                    highResLoadError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = effectiveScale,
                                    scaleY = effectiveScale,
                                    translationX = effectiveOffsetX,
                                    translationY = livePanY
                                )
                        )

                        if (isLoadingHighRes && targetPhoto.id == photo.id) {
                            CircularProgressIndicator(
                                color = StudioGold,
                                strokeWidth = 3.dp,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("lightbox_highres_loading")
                            )
                        }

                        if (highResLoadError && targetPhoto.id == photo.id) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E1B18).copy(alpha = 0.92f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier
                                    .padding(24.dp)
                                    .testTag("lightbox_highres_error")
                            ) {
                                Text(
                                    text = "Unable to decode full-resolution preview for this file.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }

                        // Detected Face Tags Bounding-Box Overlay
                        if (showFaceTags) {
                            val tagsForPhoto = remember(targetPhoto.id, faceTags) {
                                faceTags.filter { it.photoId == targetPhoto.id }
                            }
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = effectiveScale,
                                        scaleY = effectiveScale,
                                        translationX = effectiveOffsetX,
                                        translationY = livePanY
                                    )
                            ) {
                                val boxW = maxWidth
                                val boxH = maxHeight
                                if (tagsForPhoto.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.72f),
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .windowInsetsPadding(WindowInsets.statusBars)
                                            .padding(top = 68.dp)
                                    ) {
                                        Text(
                                            text = "No face tags detected in this photo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                } else {
                                    tagsForPhoto.forEach { tag ->
                                        val person = personClusters.firstOrNull { it.id == tag.personId }
                                        val leftDp = boxW * tag.leftFrac.coerceIn(0.05f, 0.75f)
                                        val topDp = boxH * tag.topFrac.coerceIn(0.08f, 0.72f)
                                        val faceWidthDp = (boxW * (tag.rightFrac - tag.leftFrac).coerceIn(0.16f, 0.48f))
                                        val faceHeightDp = (boxH * (tag.bottomFrac - tag.topFrac).coerceIn(0.14f, 0.42f))
                                        Box(
                                            modifier = Modifier
                                                .offset(x = leftDp, y = topDp)
                                                .size(width = faceWidthDp, height = faceHeightDp)
                                                .border(2.dp, Color(0xFFC84B28), RoundedCornerShape(14.dp))
                                                .clickable(enabled = person != null) {
                                                    if (person != null) {
                                                        onOpenPersonDetail(person)
                                                        onClose()
                                                    }
                                                }
                                                .testTag("lightbox_face_box_${tag.id}")
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(0xFFC84B28),
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .offset(y = 12.dp)
                                            ) {
                                                Text(
                                                    text = person?.displayTitle ?: "Person",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Split-Screen Interactive "Before & After" Loupe Overlay
                        if (showSplitLoupe && liveColorFilter != null) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val totalBoxWidth = this.maxWidth
                                val splitWidth = totalBoxWidth * splitFraction
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(splitWidth)
                                        .clipToBounds()
                                ) {
                                    AsyncImage(
                                        model = highResRequest,
                                        contentDescription = "Graded result preview",
                                        contentScale = ContentScale.Fit,
                                        colorFilter = liveColorFilter,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(totalBoxWidth)
                                            .graphicsLayer(
                                                scaleX = effectiveScale,
                                                scaleY = effectiveScale,
                                                translationX = effectiveOffsetX,
                                                translationY = livePanY
                                            )
                                    )
                                }

                                // Interactive Divider Handle with Drag gestures
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(36.dp)
                                        .offset(x = splitWidth - 18.dp)
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures { change, dragAmount ->
                                                change.consume()
                                                val totalPx = size.width.toFloat()
                                                if (totalPx > 0f) {
                                                    val deltaFraction = dragAmount / 400f
                                                    val next = (splitFraction + deltaFraction).coerceIn(0.05f, 0.95f)
                                                    if (abs(next - 0.5f) < 0.02f && abs(splitFraction - 0.5f) >= 0.02f) {
                                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                    }
                                                    splitFraction = next
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .fillMaxHeight()
                                            .background(StudioGold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = StudioGold,
                                        shadowElevation = 6.dp,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Compare,
                                                contentDescription = "Drag to compare Before and After",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(horizontal = 14.dp, vertical = 64.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.72f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = "ORIGINAL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            ),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = StudioGold,
                                        shadowElevation = 4.dp
                                    ) {
                                        Text(
                                            text = "GRADED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.5.sp
                                            ),
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Floating Top Bar Overlay with Smooth Vertical Gradient Scrim
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(180)) + slideInVertically { -it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically { -it / 2 },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.84f),
                                Color.Black.copy(alpha = 0.48f),
                                Color.Transparent
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("lightbox_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to gallery",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${currentIndex + 1} of ${currentList.size} • ${photo.albumName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.82f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (decodedResolutionLabel != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.16f),
                                    modifier = Modifier.testTag("lightbox_resolution_badge")
                                ) {
                                    Text(
                                        text = "$decodedResolutionLabel • HD",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isSlideshowPlaying) {
                        AssistChip(
                            onClick = onCycleSlideshowInterval,
                            label = {
                                Text(
                                    "${slideshowIntervalSec}s",
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Slideshow interval",
                                    tint = StudioGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    val targetFaceCount = remember(photo.id, faceTags) {
                        faceTags.count { it.photoId == photo.id }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (showFaceTags) Color(0xFFC84B28) else Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showFaceTags = !showFaceTags }
                            .testTag("lightbox_face_tags_toggle")
                    ) {
                        Text(
                            text = if (targetFaceCount > 0) "Faces ($targetFaceCount)" else "Faces",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSharePhoto(photo) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("lightbox_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share image",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showExifSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("lightbox_exif_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Photo EXIF & Privacy Sheet",
                            tint = Color.White
                        )
                    }

                    if (liveColorFilter != null) {
                        IconButton(
                            onClick = {
                                showSplitLoupe = !showSplitLoupe
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("lightbox_split_compare_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = "Compare original & graded with split wipe loupe",
                                tint = if (showSplitLoupe) StudioGold else Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleSlideshow,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("lightbox_slideshow_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSlideshowPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isSlideshowPlaying) "Pause slideshow" else "Play slideshow",
                            tint = if (isSlideshowPlaying) StudioGold else Color.White
                        )
                    }
                }
            }
        }

        // 3. Right-Side Floating Zoom & Filmstrip Controls
        androidx.compose.animation.AnimatedVisibility(
            visible = controlsVisible,
            enter = slideInHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetX = { it }
            ) + fadeIn(tween(180)),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (targetZoom > 1.05f) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                targetZoom = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                            .testTag("lightbox_zoom_reset_pill")
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fx", targetZoom),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Zoom In (+) Button (48dp touch target)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E1E22).copy(alpha = 0.84f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val next = (targetZoom + 0.75f).coerceAtMost(maxZoom)
                            val ratio = if (targetZoom > 0f) next / targetZoom else 1f
                            targetZoom = next
                            val maxX = maxPanOffset(next, viewportSize.width)
                            val maxY = maxPanOffset(next, viewportSize.height)
                            offsetX = (offsetX * ratio).coerceIn(-maxX, maxX)
                            offsetY = (offsetY * ratio).coerceIn(-maxY, maxY)
                        }
                        .testTag("lightbox_zoom_in_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom in",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Zoom Out (-) Button (48dp touch target)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E1E22).copy(alpha = 0.84f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val next = (targetZoom - 0.75f).coerceAtLeast(minZoom)
                            val ratio = if (targetZoom > 0f) next / targetZoom else 1f
                            targetZoom = next
                            if (next <= 1.02f) {
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                val maxX = maxPanOffset(next, viewportSize.width)
                                val maxY = maxPanOffset(next, viewportSize.height)
                                offsetX = (offsetX * ratio).coerceIn(-maxX, maxX)
                                offsetY = (offsetY * ratio).coerceIn(-maxY, maxY)
                            }
                        }
                        .testTag("lightbox_zoom_out_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom out",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Golden Filmstrip / Multi-Photo Preview Toggle Button
                Surface(
                    shape = CircleShape,
                    color = if (showFilmstripScrubber) StudioGold else Color(0xFF1E1E22).copy(alpha = 0.84f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            1.dp,
                            if (showFilmstripScrubber) StudioGold else Color.White.copy(alpha = 0.16f),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            showFilmstripScrubber = !showFilmstripScrubber
                        }
                        .testTag("lightbox_filmstrip_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Theaters,
                            contentDescription = "Toggle multi-photo filmstrip preview",
                            tint = if (showFilmstripScrubber) Color(0xFF14120E) else StudioGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 4. Bottom Overlay: Optional Filmstrip + Pro Studio Dock Over Translucent Gradient Scrim
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(180)) + slideInVertically { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = showLutDrawer,
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    QuickLutColorGradingDrawer(
                        selectedPreset = previewLutPreset,
                        onSelectPreset = { previewLutPreset = it },
                        onApplyLut = { saveCopy ->
                            onApplyQuickLut(photo, previewLutPreset, saveCopy)
                            showLutDrawer = false
                        },
                        onClose = {
                            previewLutPreset = PhotoFilterPreset.ORIGINAL
                            showLutDrawer = false
                        }
                    )
                }

                AnimatedVisibility(
                    visible = showFrameDrawer,
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    QuickGalleryFrameDrawer(
                        selectedFrame = previewFrameStyle,
                        onSelectFrame = { previewFrameStyle = it },
                        onSaveFramedPrint = { includeExif ->
                            onCreateFramedPhoto(photo, previewFrameStyle, includeExif)
                            showFrameDrawer = false
                        },
                        onClose = { showFrameDrawer = false }
                    )
                }

                AnimatedVisibility(
                    visible = showStampDrawer,
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    QuickWatermarkStampDrawer(
                        photo = photo,
                        onApplyStamp = { text, includeExif ->
                            onStampWatermark(photo, text, includeExif)
                            showStampDrawer = false
                        },
                        onClose = { showStampDrawer = false }
                    )
                }

                AnimatedVisibility(
                    visible = showFilmstripScrubber && currentList.isNotEmpty(),
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF0E0E0E).copy(alpha = 0.94f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF282828),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .testTag("lightbox_filmstrip_carousel")
                        ) {
                            LazyRow(
                                state = filmstripListState,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                itemsIndexed(
                                    items = currentList,
                                    key = { _, item -> item.id }
                                ) { idx, item ->
                                    val isCurrent = item.id == photo.id
                                    val thumbSize by animateDpAsState(
                                        targetValue = if (isCurrent) 66.dp else 56.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "filmstrip_thumb_size"
                                    )
                                    val thumbBorderColor by animateColorAsState(
                                        targetValue = if (isCurrent) StudioGold else Color.White.copy(alpha = 0.14f),
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "filmstrip_thumb_border"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .size(thumbSize)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isCurrent) 2.5.dp else 1.dp,
                                                color = thumbBorderColor,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onSelectPhoto(item)
                                            }
                                            .testTag("filmstrip_thumb_$idx")
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
                        }

                        InteractiveFilmstripScrubberBar(
                            currentIndex = currentIndex,
                            totalCount = currentList.size,
                            onScrubToIndex = { targetIdx ->
                                val coerced = targetIdx.coerceIn(0, currentList.lastIndex)
                                if (coerced != currentIndex) {
                                    onSelectPhoto(currentList[coerced])
                                }
                            }
                        )
                    }
                }

                // Bottom Pro Studio Action Dock — Compact single row by default to keep the high-res image unobstructed
                var showAllDockRows by remember { mutableStateOf(false) }
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFF141416).copy(alpha = 0.90f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF2E2E32),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .testTag("lightbox_bottom_studio_dock")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top Header Bar inside Dock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDIO & PHOTO TOOLS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 10.sp
                                ),
                                color = StudioGold
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.10f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showAllDockRows = !showAllDockRows }
                                    .testTag("toggle_dock_rows_button")
                            ) {
                                Text(
                                    text = if (showAllDockRows) "Compact" else "More Tools",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Row 1 (Always Visible - 6 Equal Columns): Share | Favorite | LUTs | Crop | Studio | Shrink
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LightboxDockItem(
                                icon = Icons.Default.Share,
                                label = "Share",
                                tint = Color(0xFFFF8F6B),
                                tag = "dock_share_button",
                                modifier = Modifier.weight(1f),
                                onClick = { onSharePhoto(photo) }
                            )
                            LightboxDockItem(
                                icon = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                label = "Favorite",
                                tint = if (photo.isFavorite) Color(0xFFFF5A6E) else Color.White,
                                tag = "dock_favorite_button",
                                modifier = Modifier.weight(1f),
                                onClick = { onToggleFavorite(photo) }
                            )
                            LightboxDockItem(
                                icon = Icons.Default.Tune,
                                label = "LUTs",
                                tint = if (showLutDrawer || previewLutPreset != PhotoFilterPreset.ORIGINAL) {
                                    StudioGold
                                } else {
                                    Color.White
                                },
                                tag = "dock_luts_button",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    showFrameDrawer = false
                                    showStampDrawer = false
                                    showLutDrawer = !showLutDrawer
                                }
                            )
                            LightboxDockItem(
                                icon = Icons.Default.Crop,
                                label = "Crop",
                                tint = StudioGold,
                                tag = "dock_crop_button",
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenCropStudio(photo) }
                            )
                            LightboxDockItem(
                                icon = Icons.Default.Edit,
                                label = "Studio",
                                tint = Color.White,
                                tag = "dock_edit_button",
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenEditor(photo) }
                            )
                            LightboxDockItem(
                                icon = Icons.Default.Compress,
                                label = "Shrink",
                                tint = StudioEmerald,
                                tag = "dock_shrink_button",
                                modifier = Modifier.weight(1f),
                                onClick = { showShrinkSheet = true }
                            )
                        }

                        // Row 2: Rotate | Duplicate | Album | Collage | Vault | Delete
                        AnimatedVisibility(visible = showAllDockRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LightboxDockItem(
                                    icon = Icons.Default.Rotate90DegreesCw,
                                    label = "Rotate",
                                    tint = Color.White,
                                    tag = "dock_rotate_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickRotate90(photo) }
                                )
                                LightboxDockItem(
                                    icon = Icons.Default.ContentCopy,
                                    label = "Duplicate",
                                    tint = Color.White,
                                    tag = "dock_duplicate_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDuplicatePhoto(photo) }
                                )
                                LightboxDockItem(
                                    icon = Icons.Default.CreateNewFolder,
                                    label = "Album",
                                    tint = Color.White,
                                    tag = "dock_album_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = { showMoveAlbumDialog = true }
                                )
                                LightboxDockItem(
                                    icon = Icons.Default.GridView,
                                    label = "Collage",
                                    tint = StudioGold,
                                    tag = "dock_collage_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = onOpenCollageStudio
                                )
                                LightboxDockItem(
                                    icon = if (photo.isVaultLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                    label = if (photo.isVaultLocked) "Unhide" else "Vault",
                                    tint = Color.White,
                                    tag = "dock_vault_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = { onToggleVaultLock(photo) }
                                )
                                LightboxDockItem(
                                    icon = Icons.Default.DeleteForever,
                                    label = "Delete",
                                    tint = Color(0xFFFF897D),
                                    tag = "dock_trash_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = { showDeleteConfirmDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Smart Photo Shrink & Size Optimizer Modal Bottom Sheet
    if (showShrinkSheet) {
        PhotoShrinkOptimizerBottomSheet(
            photo = photo,
            onDismiss = { showShrinkSheet = false },
            onConfirmShrink = { scaleFactor, quality, replaceOriginal ->
                onShrinkPhoto(photo, scaleFactor, quality, replaceOriginal)
                showShrinkSheet = false
            }
        )
    }

    // EXIF & Metadata Inspector + Editor Bottom Sheet
    if (showExifSheet) {
        ExifMetadataBottomSheet(
            photo = photo,
            histogram = histogram,
            onDismiss = { showExifSheet = false },
            onAnalyzeWithAi = {
                onAnalyzeWithAi(photo)
            },
            onStripExifAndShare = {
                showExifSheet = false
                onStripExifAndShare(photo)
            },
            onSaveMetadata = { title, album, tags, notes, loc, cam, aper, shut, iso, focal ->
                onUpdateMetadata(photo, title, album, tags, notes, loc, cam, aper, shut, iso, focal)
                showExifSheet = false
            }
        )
    }

    // Move or Copy to Album Dialog
    if (showMoveAlbumDialog) {
        MoveOrCopyAlbumDialog(
            currentAlbum = photo.albumName,
            albums = albums,
            onDismiss = { showMoveAlbumDialog = false },
            onConfirm = { targetAlbum, copyInsteadOfMove ->
                onMoveOrCopyAlbum(photo, targetAlbum, copyInsteadOfMove)
                showMoveAlbumDialog = false
            }
        )
    }

    // Delete Photo from Local Storage Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            modifier = Modifier.testTag("delete_confirmation_dialog"),
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Photo from Local Storage?",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Permanently delete \"${photo.title}\" (${photo.formattedSize}) from your device's local storage? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onMoveToTrash(photo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("move_to_trash_instead_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Move to Recycle Bin Instead")
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePermanently(photo)
                    },
                    modifier = Modifier.testTag("confirm_delete_photos_button")
                ) {
                    Text("Delete from Device", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_delete_photos_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Interactive horizontal scrubber track with a glowing golden indicator dot beneath the filmstrip.
 */
@Composable
private fun InteractiveFilmstripScrubberBar(
    currentIndex: Int,
    totalCount: Int,
    onScrubToIndex: (Int) -> Unit
) {
    val progress = remember(currentIndex, totalCount) {
        if (totalCount <= 1) 0f
        else (currentIndex.toFloat() / (totalCount - 1).toFloat()).coerceIn(0f, 1f)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(horizontal = 4.dp)
            .pointerInput(totalCount) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetIdx = (fraction * (totalCount - 1)).roundToInt()
                    onScrubToIndex(targetIdx)
                }
            }
            .pointerInput(totalCount) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetIdx = (fraction * (totalCount - 1)).roundToInt()
                    onScrubToIndex(targetIdx)
                }
            }
            .testTag("filmstrip_scrubber_track"),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = maxWidth
        // Background dark track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1F1F1F))
        )

        // Active golden progress track
        Box(
            modifier = Modifier
                .width(trackWidth * progress)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(StudioGold.copy(alpha = 0.65f))
        )

        // Golden circular thumb indicator (matches screenshot)
        val thumbOffset = (trackWidth - 14.dp) * progress
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(14.dp)
                .clip(CircleShape)
                .background(StudioGold)
                .border(2.dp, Color.Black, CircleShape)
        )
    }
}

/**
 * Live LUT Color Grading Drawer displayed right above the filmstrip when `LUTs` is tapped.
 */
@Composable
private fun QuickLutColorGradingDrawer(
    selectedPreset: PhotoFilterPreset,
    onSelectPreset: (PhotoFilterPreset) -> Unit,
    onApplyLut: (saveAsCopy: Boolean) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF161618).copy(alpha = 0.96f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .testTag("quick_lut_drawer")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "CINEMA LUT COLOR GRADING",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = StudioGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (selectedPreset != PhotoFilterPreset.ORIGINAL) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = StudioGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onApplyLut(true) }
                                .testTag("apply_lut_button")
                        ) {
                            Text(
                                text = "SAVE LUT PRINT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close LUTs",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(PhotoFilterPreset.entries) { _, preset ->
                    val isSelected = preset == selectedPreset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioGold,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF242428),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Live Gallery Frame & Matting Drawer displayed when `Frame` is tapped in the bottom dock.
 */
@Composable
private fun QuickGalleryFrameDrawer(
    selectedFrame: String,
    onSelectFrame: (String) -> Unit,
    onSaveFramedPrint: (includeExif: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val frames = remember {
        listOf("Museum White Mat", "35mm Film", "Gold Gallery", "Obsidian Mat", "Sakura Press")
    }
    var stampExifFooter by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF161618).copy(alpha = 0.96f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .testTag("quick_frame_drawer")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "GALLERY FRAME & MATTING STUDIO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = StudioGold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSaveFramedPrint(stampExifFooter) }
                            .testTag("save_framed_print_button")
                    ) {
                        Text(
                            text = "EXPORT FRAME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Frame Studio",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(frames) { _, frameName ->
                    val isSelected = frameName == selectedFrame
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFrame(frameName) },
                        label = {
                            Text(
                                text = frameName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioGold,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF242428),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Include Camera & EXIF Signature on Frame Margin",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Switch(
                    checked = stampExifFooter,
                    onCheckedChange = { stampExifFooter = it }
                )
            }
        }
    }
}

/**
 * Live Date & EXIF Watermark Stamp Drawer displayed when `Stamp` is tapped in the bottom dock.
 */
@Composable
private fun QuickWatermarkStampDrawer(
    photo: PhotoEntity,
    onApplyStamp: (watermarkText: String, includeExif: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val stampPresets = remember(photo.albumName, photo.cameraModel) {
        listOf(
            "© Studio Archive • ${photo.albumName}",
            "Shot on ${photo.cameraModel} • ${photo.exifSummary}",
            "${photo.title} • ${photo.locationTag}",
            "Archival Master Print • ${photo.dateHeader}"
        )
    }
    var selectedText by remember(photo.id) { mutableStateOf(stampPresets.first()) }
    var includeDateAndExif by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF161618).copy(alpha = 0.96f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .testTag("quick_stamp_drawer")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrandingWatermark,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "DATE & EXIF WATERMARK STAMP",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = StudioGold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onApplyStamp(selectedText, includeDateAndExif) }
                            .testTag("save_stamped_print_button")
                    ) {
                        Text(
                            text = "STAMP PRINT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Stamp Studio",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(stampPresets) { _, presetText ->
                    val isSelected = presetText == selectedText
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedText = presetText },
                        label = {
                            Text(
                                text = presetText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioGold,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF242428),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Include Capture Date & Camera EXIF Badge (${photo.exifSummary})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = includeDateAndExif,
                    onCheckedChange = { includeDateAndExif = it }
                )
            }
        }
    }
}

/**
 * Smart Photo Shrink & Storage Optimizer Bottom Sheet triggered by the green `Shrink` icon in the dock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoShrinkOptimizerBottomSheet(
    photo: PhotoEntity,
    onDismiss: () -> Unit,
    onConfirmShrink: (scaleFactor: Float, jpegQuality: Int, replaceOriginal: Boolean) -> Unit
) {
    val activeTheme = LocalCompleteUiTheme.current
    var selectedPresetIdx by remember { mutableIntStateOf(1) }
    var replaceOriginal by remember { mutableStateOf(false) }

    data class ShrinkPreset(
        val title: String,
        val subtitle: String,
        val scaleFactor: Float,
        val quality: Int,
        val estimatedRatio: Float
    )

    val presets = remember {
        listOf(
            ShrinkPreset("Web & Messaging Share (Fast)", "50% dimensions • 68% JPEG quality", 0.50f, 68, 0.22f),
            ShrinkPreset("Smart Balanced Archival (Recommended)", "75% dimensions • 80% JPEG quality", 0.75f, 80, 0.42f),
            ShrinkPreset("Lossless-Look High Resolution", "100% dimensions • 85% JPEG quality", 1.0f, 85, 0.64f)
        )
    }

    val activePreset = presets[selectedPresetIdx]
    val estimatedBytes = (photo.sizeBytes * activePreset.estimatedRatio).toLong().coerceAtLeast(18_000L)
    val savedPct = ((1f - activePreset.estimatedRatio) * 100f).roundToInt()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface,
        modifier = Modifier.testTag("photo_shrink_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = StudioEmerald.copy(alpha = 0.18f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = null,
                            tint = StudioEmerald
                        )
                    }
                }
                Column {
                    Text(
                        text = "Smart Photo Shrink & Optimizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "Current: ${photo.formattedSize} (${photo.resolutionText}) → Est. ${estimatedBytes / 1024} KB (-$savedPct%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioEmerald
                    )
                }
            }

            presets.forEachIndexed { idx, preset ->
                val isSelected = idx == selectedPresetIdx
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            StudioEmerald.copy(alpha = 0.14f)
                        } else {
                            activeTheme.surfaceVariant.copy(alpha = 0.55f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) StudioEmerald else activeTheme.borderStroke,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedPresetIdx = idx }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = preset.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPresetIdx = idx }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (replaceOriginal) "Replace original file in place" else "Save as optimized copy in 'Edited Studio'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = activeTheme.textPrimary
                )
                Switch(
                    checked = replaceOriginal,
                    onCheckedChange = { replaceOriginal = it }
                )
            }

            Button(
                onClick = {
                    onConfirmShrink(activePreset.scaleFactor, activePreset.quality, replaceOriginal)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioEmerald,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_shrink_button")
            ) {
                Icon(Icons.Default.Compress, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shrink Photo Now (Save ~$savedPct%)",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Translucent Pro-Camera Telemetry Viewfinder Overlay displayed directly over the selected photo.
 */
@Composable
fun ExifViewfinderHudOverlay(
    photo: PhotoEntity,
    telemetry: DetailedExifTelemetry,
    histogram: List<Float>,
    onOpenFullExifSheet: () -> Unit,
    onDismissHud: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF101216).copy(alpha = 0.88f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .testTag("exif_hud_overlay_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Camera Body & Lens + Full Sheet Button + Close HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StudioGold.copy(alpha = 0.22f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = StudioGold,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = telemetry.cameraModel.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${telemetry.focalLength} • ${telemetry.colorSpace}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenFullExifSheet() }
                            .testTag("hud_open_exif_sheet_button")
                    ) {
                        Text(
                            text = "FULL EXIF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismissHud,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide EXIF HUD",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 4-Column Exposure Settings Dial Strip (Aperture | Shutter | ISO | EV & Metering)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HudTelemetryCell(
                    label = "APERTURE",
                    value = telemetry.aperture,
                    subtext = "f-stop",
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                HudTelemetryCell(
                    label = "EXPOSURE",
                    value = telemetry.shutterSpeed,
                    subtext = "Shutter",
                    accentColor = StudioGold,
                    modifier = Modifier.weight(1f)
                )
                HudTelemetryCell(
                    label = "ISO GAIN",
                    value = telemetry.iso,
                    subtext = "Sensitivity",
                    accentColor = StudioEmerald,
                    modifier = Modifier.weight(1f)
                )
                HudTelemetryCell(
                    label = "FOCAL / EV",
                    value = telemetry.focalLength,
                    subtext = telemetry.exposureBias.substringBefore(" "),
                    accentColor = Color(0xFFF472B6),
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Mini Histogram + Resolution & Location Telemetry Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(96.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    val bins = histogram.ifEmpty { List(16) { 0.35f } }.take(16)
                    val barW = size.width / (bins.size * 1.3f)
                    val gap = barW * 0.3f
                    bins.forEachIndexed { i, v ->
                        val h = (v * size.height).coerceAtLeast(2f)
                        drawRoundRect(
                            color = StudioGold,
                            topLeft = Offset(i * (barW + gap), size.height - h),
                            size = Size(barW, h),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${photo.megapixels} (${photo.resolutionText}) • ${telemetry.aspectRatioLabel} • ${photo.formattedSize}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${telemetry.meteringMode} • ${telemetry.whiteBalance} • ${photo.locationTag}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.68f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HudTelemetryCell(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.07f),
        modifier = modifier.border(0.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LightboxDockItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 5.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFFE4E4E7),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Detailed EXIF Metadata Bottom Sheet that can be opened from the Lightbox,
 * directly from any photo card in the Gallery Timeline, or from the Multi-Select bar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExifMetadataBottomSheet(
    photo: PhotoEntity,
    histogram: List<Float>,
    onDismiss: () -> Unit,
    onAnalyzeWithAi: () -> Unit = {},
    onStripExifAndShare: () -> Unit = {},
    onSaveMetadata: (
        title: String,
        album: String,
        tags: String,
        notes: String,
        location: String,
        cameraModel: String,
        aperture: String,
        shutterSpeed: String,
        iso: String,
        focalLength: String
    ) -> Unit
) {
    val activeTheme = LocalCompleteUiTheme.current
    val telemetry = rememberDetailedExifTelemetry(photo)

    var isEditing by remember { mutableStateOf(false) }
    var titleText by remember(photo) { mutableStateOf(photo.title) }
    var albumText by remember(photo) { mutableStateOf(photo.albumName) }
    var tagsText by remember(photo) { mutableStateOf(photo.tags) }
    var locationText by remember(photo) { mutableStateOf(photo.locationTag) }
    var notesText by remember(photo) { mutableStateOf(photo.notes) }
    var cameraModelText by remember(photo) { mutableStateOf(photo.cameraModel) }
    var apertureText by remember(photo) { mutableStateOf(photo.aperture) }
    var shutterSpeedText by remember(photo) { mutableStateOf(photo.shutterSpeed) }
    var isoText by remember(photo) { mutableStateOf(photo.iso) }
    var focalLengthText by remember(photo) { mutableStateOf(photo.focalLength) }

    val bins = remember(histogram) {
        histogram.ifEmpty {
            listOf(
                0.15f, 0.22f, 0.30f, 0.42f, 0.55f, 0.68f, 0.76f, 0.85f,
                0.92f, 0.88f, 0.79f, 0.72f, 0.64f, 0.58f, 0.52f, 0.46f,
                0.38f, 0.31f, 0.25f, 0.20f, 0.16f, 0.12f, 0.09f, 0.06f
            )
        }
    }
    val totalWeight = remember(bins) { bins.sum().coerceAtLeast(0.01f) }
    val shadowsPct = remember(bins, totalWeight) {
        ((bins.take(8).sum() / totalWeight) * 100f).roundToInt()
    }
    val midtonesPct = remember(bins, totalWeight) {
        ((bins.drop(8).take(8).sum() / totalWeight) * 100f).roundToInt()
    }
    val highlightsPct = remember(shadowsPct, midtonesPct) {
        (100 - shadowsPct - midtonesPct).coerceAtLeast(0)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface,
        modifier = Modifier.testTag("exif_metadata_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Sheet Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = activeTheme.primaryAccent
                        ) {
                            Text(
                                text = "EXIF 2.32",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = activeTheme.onPrimaryAccent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = photo.mimeType.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Photo EXIF & Metadata",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = photo.fullTimestampText,
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTheme.textSecondary
                    )
                }

                OutlinedButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.testTag("toggle_edit_metadata_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEditing) "Cancel" else "Edit EXIF")
                }
            }

            // 2. Photo Thumbnail & File Summary Banner Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = activeTheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeTheme.backgroundSecondary)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = activeTheme.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${photo.megapixels} • ${photo.resolutionText} (${telemetry.aspectRatioLabel}) • ${photo.formattedSize}",
                            style = MaterialTheme.typography.labelMedium,
                            color = activeTheme.primaryAccent
                        )
                        Text(
                            text = "${telemetry.orientationLabel} • ${telemetry.colorSpace}",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }
                }
            }

            // 3. Primary Camera & Exposure Triangle Grid
            Text(
                text = "CAMERA & EXPOSURE SETTINGS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                ),
                color = activeTheme.primaryAccent
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExifDetailedMetricCard(
                        icon = Icons.Default.CameraAlt,
                        label = "CAMERA MODEL",
                        primaryValue = telemetry.cameraModel,
                        secondaryDetail = "Make: ${telemetry.cameraMake}",
                        modifier = Modifier.weight(1f)
                    )
                    ExifDetailedMetricCard(
                        icon = Icons.Default.Camera,
                        label = "APERTURE",
                        primaryValue = telemetry.aperture,
                        secondaryDetail = telemetry.apertureDescription,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExifDetailedMetricCard(
                        icon = Icons.Default.ShutterSpeed,
                        label = "EXPOSURE TIME",
                        primaryValue = telemetry.shutterSpeed,
                        secondaryDetail = telemetry.shutterDescription,
                        modifier = Modifier.weight(1f)
                    )
                    ExifDetailedMetricCard(
                        icon = Icons.Default.Iso,
                        label = "ISO SENSITIVITY",
                        primaryValue = telemetry.iso,
                        secondaryDetail = telemetry.isoDescription,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExifDetailedMetricCard(
                        icon = Icons.Default.Exposure,
                        label = "FOCAL LENGTH",
                        primaryValue = telemetry.focalLength,
                        secondaryDetail = telemetry.focalEquivalent35mm,
                        modifier = Modifier.weight(1f)
                    )
                    ExifDetailedMetricCard(
                        icon = Icons.Default.WbSunny,
                        label = "METERING & BIAS",
                        primaryValue = telemetry.exposureBias,
                        secondaryDetail = "${telemetry.meteringMode} • ${telemetry.whiteBalance}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Live Luminance Histogram & Tonal Distribution Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = activeTheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LUMINANCE HISTOGRAM & TONAL CURVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = activeTheme.primaryAccent
                        )
                        Text(
                            text = telemetry.softwarePipeline,
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }

                    val barColor = activeTheme.primaryAccent
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        val barWidth = size.width / (bins.size * 1.35f)
                        val gap = barWidth * 0.35f
                        bins.forEachIndexed { idx, norm ->
                            val barH = (norm * size.height).coerceAtLeast(4f)
                            val x = idx * (barWidth + gap)
                            drawRoundRect(
                                color = barColor.copy(alpha = 0.88f),
                                topLeft = Offset(x, size.height - barH),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Shadows ($shadowsPct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                        Text(
                            text = "Midtones ($midtonesPct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                        Text(
                            text = "Highlights ($highlightsPct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeTheme.textSecondary
                        )
                    }
                }
            }

            // 5. Additional Optical & Archival Badges + AI Scene Analyzer Trigger
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExifSpecBadge("Flash", telemetry.flashStatus)
                ExifSpecBadge("Color Space", telemetry.colorSpace)
                ExifSpecBadge("Filter", photo.appliedFilter)
                ExifSpecBadge("Album", photo.albumName)
                ExifSpecBadge("Location", photo.locationTag)
            }

            if (isEditing) {
                HorizontalDivider(color = activeTheme.borderStroke)
                Text(
                    text = "EDIT EXIF & ARCHIVAL METADATA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = activeTheme.primaryAccent
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Photo Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cameraModelText,
                        onValueChange = { cameraModelText = it },
                        label = { Text("Camera Model") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = focalLengthText,
                        onValueChange = { focalLengthText = it },
                        label = { Text("Focal Length") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = apertureText,
                        onValueChange = { apertureText = it },
                        label = { Text("Aperture (f-stop)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = shutterSpeedText,
                        onValueChange = { shutterSpeedText = it },
                        label = { Text("Shutter Speed") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = isoText,
                        onValueChange = { isoText = it },
                        label = { Text("ISO") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = albumText,
                    onValueChange = { albumText = it },
                    label = { Text("Album / Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Comma-separated Tags") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Location Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Archival Caption / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onSaveMetadata(
                            titleText,
                            albumText,
                            tagsText,
                            notesText,
                            locationText,
                            cameraModelText,
                            apertureText,
                            shutterSpeedText,
                            isoText,
                            focalLengthText
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryAccent,
                        contentColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_metadata_button")
                ) {
                    Text("Save EXIF & Metadata Changes")
                }
            } else {
                // Tags & AI Auto-Tag Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TAGS & AI SCENE LABELS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = activeTheme.textSecondary
                        )
                        Text(
                            text = photo.tagList.joinToString(" • ").ifEmpty { "Untagged" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = activeTheme.textPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onAnalyzeWithAi,
                        modifier = Modifier.testTag("exif_sheet_ai_analyze_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = activeTheme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Tag")
                    }
                }

                if (photo.notes.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = activeTheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ARCHIVAL CAPTION / STORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = activeTheme.primaryAccent
                            )
                            Text(
                                text = photo.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTheme.textPrimary
                            )
                        }
                    }
                }

                Button(
                    onClick = onStripExifAndShare,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryAccent,
                        contentColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("strip_exif_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Strip EXIF / Location & Share",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = activeTheme.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Local Path: ${photo.filePath} (${photo.sizeBytes} bytes)",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeTheme.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExifDetailedMetricCard(
    icon: ImageVector,
    label: String,
    primaryValue: String,
    secondaryDetail: String,
    modifier: Modifier = Modifier
) {
    val activeTheme = LocalCompleteUiTheme.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = activeTheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.border(1.dp, activeTheme.borderStroke, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = activeTheme.primaryAccent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = activeTheme.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = activeTheme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondaryDetail,
                style = MaterialTheme.typography.labelSmall,
                color = activeTheme.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExifSpecBadge(label: String, value: String) {
    val activeTheme = LocalCompleteUiTheme.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = activeTheme.surfaceVariant,
        modifier = Modifier.border(0.5.dp, activeTheme.borderStroke, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = activeTheme.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = activeTheme.textPrimary
            )
        }
    }
}

@Composable
fun MoveOrCopyAlbumDialog(
    currentAlbum: String,
    albums: List<AlbumSummary>,
    onDismiss: () -> Unit,
    onConfirm: (targetAlbum: String, copyInsteadOfMove: Boolean) -> Unit
) {
    val albumNames = remember(albums) {
        (albums.map { it.name } + listOf(
            "Camera Roll",
            "Food",
            "Travel",
            "Portraits",
            "Landscapes",
            "Architecture",
            "Nature & Macro",
            "Street & Night"
        )).distinct()
    }
    var selectedAlbum by remember {
        mutableStateOf(albumNames.firstOrNull { it != currentAlbum } ?: albumNames.first())
    }
    var customAlbumInput by remember { mutableStateOf("") }
    var copyInsteadOfMove by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Organize into Album") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (copyInsteadOfMove) "Keep original (Copy)" else "Move to selected album",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = copyInsteadOfMove,
                        onCheckedChange = { copyInsteadOfMove = it }
                    )
                }

                albumNames.forEach { name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedAlbum = name
                                customAlbumInput = ""
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = customAlbumInput.isBlank() && selectedAlbum == name,
                            onClick = {
                                selectedAlbum = name
                                customAlbumInput = ""
                            }
                        )
                        Text(
                            text = if (name == currentAlbum) "$name (Current)" else name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = customAlbumInput,
                    onValueChange = { customAlbumInput = it },
                    label = { Text("Or type a new album name…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = customAlbumInput.trim().ifEmpty { selectedAlbum }
                    onConfirm(target, copyInsteadOfMove)
                }
            ) {
                Text(if (copyInsteadOfMove) "Copy Here" else "Move Here")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
