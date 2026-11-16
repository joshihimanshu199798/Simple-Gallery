package com.example.ui.screens

import android.graphics.RectF
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.data.formatBytes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.clip
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.service.AiOrganizerUiState
import com.example.ui.studio.BatchActionMode
import com.example.ui.studio.CollageLayoutMode
import com.example.ui.studio.EditorToolTab
import com.example.ui.studio.StudioBatchWorkspace
import com.example.ui.studio.StudioCollageWorkspace
import com.example.ui.studio.StudioCompressionWorkspace
import com.example.ui.studio.StudioEditorWorkspace
import com.example.ui.studio.StudioHomeScreen
import com.example.ui.studio.StudioRawWorkspace
import com.example.ui.studio.StudioSubScreen
import com.example.ui.theme.CompleteUiThemeSpec

@Composable
fun StudioTabScreen(
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onAdvancedSaveEdits: (
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
    onExecuteAiCutout: (PhotoEntity) -> Unit,
    onExecuteAiInpaint: ((PhotoEntity, List<RectF>) -> Unit)? = null,
    onExecuteAiBgReplace: ((PhotoEntity, String) -> Unit)? = null,
    onExecuteAiRestore: ((PhotoEntity) -> Unit)? = null,
    onExecuteAiSuperRes: ((PhotoEntity) -> Unit)? = null,
    onCompressBatch: (
        targets: List<PhotoEntity>,
        format: String,
        quality: Int,
        scalePercent: Int,
        preserveExif: Boolean
    ) -> Unit,
    onCreateCollage: (
        photoPaths: List<String>,
        layoutMode: CollageLayoutMode,
        aspectRatio: CropAspectRatio,
        spacingDp: Float,
        cornerRadiusDp: Float,
        backgroundColorHex: String
    ) -> Unit,
    onExecuteBatch: (
        targets: List<PhotoEntity>,
        mode: BatchActionMode,
        presetId: String?,
        cropRatio: CropAspectRatio,
        filterIntensity: Float,
        rotationDegrees: Int
    ) -> Unit,
    onDevelopRaw: (
        photo: PhotoEntity,
        kelvinTemp: Int,
        tint: Float,
        exposureEv: Float,
        highlightRecovery: Float,
        shadowRecovery: Float,
        chromaDenoise: Float
    ) -> Unit,
    onPickCustomPhoto: () -> Unit,
    onTakeCameraPhoto: () -> Unit,
    onWorkspaceActiveChanged: (Boolean) -> Unit = {},
    // Merged Tools Capabilities
    vaultPhotos: List<PhotoEntity> = emptyList(),
    trashedPhotos: List<PhotoEntity> = emptyList(),
    isVaultUnlocked: Boolean = false,
    vaultPinHint: String = "1234",
    vaultBiometricEnabled: Boolean = true,
    onToggleVaultBiometric: () -> Unit = {},
    onAttemptUnlockVault: (String) -> Boolean = { false },
    onLockVault: () -> Unit = {},
    onChangeVaultPin: (String) -> Unit = {},
    onUnhideVaultPhoto: (PhotoEntity) -> Unit = {},
    onRestoreTrashPhoto: (PhotoEntity) -> Unit = {},
    onDeletePermanently: (PhotoEntity) -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    onOpenCompare: () -> Unit = {},
    onOpenWatermark: () -> Unit = {},
    onCreateFramedPhoto: (PhotoEntity, String, Boolean, String) -> Unit = { _, _, _, _ -> },
    onStartAiOrganizeAll: (Boolean) -> Unit = {},
    aiOrganizerUiState: AiOrganizerUiState = AiOrganizerUiState.Idle,
    onPhotoClick: (PhotoEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubScreen by remember { mutableStateOf(StudioSubScreen.DASHBOARD) }
    var activeTargetPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var activeEditorTab by remember { mutableStateOf(EditorToolTab.LIGHT) }
    var activeBatchMode by remember { mutableStateOf(BatchActionMode.BATCH_EDIT) }
    var showStorageAnalysisDialog by remember { mutableStateOf(false) }
    var exifFrameTargetPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    LaunchedEffect(currentSubScreen) {
        onWorkspaceActiveChanged(currentSubScreen != StudioSubScreen.DASHBOARD)
    }

    // Ensure back press returns to Studio Dashboard if in a sub-screen
    if (currentSubScreen != StudioSubScreen.DASHBOARD) {
        BackHandler {
            currentSubScreen = StudioSubScreen.DASHBOARD
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activeTheme.background)
    ) {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                val enteringWorkspace = targetState != StudioSubScreen.DASHBOARD
                if (enteringWorkspace) {
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { it / 5 } + fadeIn(tween(220)) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(220)
                    )) togetherWith (slideOutHorizontally(tween(180)) { -it / 8 } + fadeOut(tween(160)))
                } else {
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { -it / 6 } + fadeIn(tween(220)) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(220)
                    )) togetherWith (slideOutHorizontally(tween(180)) { it / 5 } + fadeOut(tween(160)) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(160)
                    ))
                }
            },
            label = "studio_screen_transition"
        ) { subScreen ->
            when (subScreen) {
                StudioSubScreen.DASHBOARD -> {
                    StudioHomeScreen(
                        photos = photos,
                        activeTheme = activeTheme,
                        onLaunchEditor = { photo, initialTab ->
                            activeTargetPhoto = photo ?: photos.firstOrNull()
                            activeEditorTab = initialTab
                            if (activeTargetPhoto != null) {
                                currentSubScreen = StudioSubScreen.PHOTO_EDITOR
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onLaunchCollage = {
                            if (photos.size >= 2) {
                                currentSubScreen = StudioSubScreen.COLLAGE_MAKER
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onLaunchCompression = { photo ->
                            activeTargetPhoto = photo ?: photos.firstOrNull()
                            if (photos.isNotEmpty()) {
                                currentSubScreen = StudioSubScreen.COMPRESSION_STUDIO
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onLaunchBatch = { mode ->
                            activeBatchMode = mode
                            if (photos.isNotEmpty()) {
                                currentSubScreen = StudioSubScreen.BATCH_EDITOR
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onLaunchRawEditor = { photo ->
                            activeTargetPhoto = photo ?: photos.firstOrNull()
                            if (photos.isNotEmpty()) {
                                currentSubScreen = StudioSubScreen.RAW_EDITOR
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onPickFromStorage = onPickCustomPhoto,
                        onCaptureCamera = onTakeCameraPhoto,
                        onOpenCompare = onOpenCompare,
                        onOpenWatermark = onOpenWatermark,
                        onOpenExifFrameStudio = { target ->
                            val resolved = target ?: photos.firstOrNull()
                            if (resolved != null) {
                                exifFrameTargetPhoto = resolved
                            } else {
                                onPickCustomPhoto()
                            }
                        },
                        onLaunchStorageAnalysis = {
                            showStorageAnalysisDialog = true
                        },
                        vaultPhotosCount = vaultPhotos.size,
                        trashedPhotosCount = trashedPhotos.size,
                        onLaunchVault = { currentSubScreen = StudioSubScreen.PRIVATE_VAULT },
                        onLaunchRecycleBin = { currentSubScreen = StudioSubScreen.RECYCLE_BIN }
                    )
                }

                StudioSubScreen.PHOTO_EDITOR -> {
                    val target = activeTargetPhoto ?: photos.firstOrNull()
                    if (target != null) {
                        StudioEditorWorkspace(
                            photo = target,
                            initialTab = activeEditorTab,
                            onClose = { currentSubScreen = StudioSubScreen.DASHBOARD },
                            onSaveEdits = { savedPhoto, cropRatio, customCropRect, rot, flipH, flipV, exp, bright, cont, sat, high, shad, temp, tint, vig, gr, fMat, fInt, smooth, saveCopy ->
                                onAdvancedSaveEdits(
                                    savedPhoto,
                                    cropRatio,
                                    customCropRect,
                                    rot,
                                    flipH,
                                    flipV,
                                    exp,
                                    bright,
                                    cont,
                                    sat,
                                    high,
                                    shad,
                                    temp,
                                    tint,
                                    vig,
                                    gr,
                                    fMat,
                                    fInt,
                                    smooth,
                                    saveCopy
                                )
                                currentSubScreen = StudioSubScreen.DASHBOARD
                            }
                        )
                    } else {
                        currentSubScreen = StudioSubScreen.DASHBOARD
                    }
                }

                StudioSubScreen.COLLAGE_MAKER -> {
                    StudioCollageWorkspace(
                        allPhotos = photos,
                        onClose = { currentSubScreen = StudioSubScreen.DASHBOARD },
                        onExportCollage = { photoPaths, layoutMode, aspectRatio, spacingDp, cornerRadiusDp, backgroundColorHex ->
                            onCreateCollage(
                                photoPaths,
                                layoutMode,
                                aspectRatio,
                                spacingDp,
                                cornerRadiusDp,
                                backgroundColorHex
                            )
                            currentSubScreen = StudioSubScreen.DASHBOARD
                        }
                    )
                }

                StudioSubScreen.COMPRESSION_STUDIO -> {
                    StudioCompressionWorkspace(
                        initialPhoto = activeTargetPhoto ?: photos.firstOrNull(),
                        allPhotos = photos,
                        onClose = { currentSubScreen = StudioSubScreen.DASHBOARD },
                        onCompressPhotos = { targets, format, quality, scalePercent, preserveExif ->
                            onCompressBatch(
                                targets,
                                format,
                                quality,
                                scalePercent,
                                preserveExif
                            )
                            currentSubScreen = StudioSubScreen.DASHBOARD
                        }
                    )
                }

                StudioSubScreen.BATCH_EDITOR -> {
                    StudioBatchWorkspace(
                        allPhotos = photos,
                        initialMode = activeBatchMode,
                        onClose = { currentSubScreen = StudioSubScreen.DASHBOARD },
                        onExecuteBatch = { targets, mode, presetId, cropRatio, filterIntensity, rotationDegrees ->
                            onExecuteBatch(targets, mode, presetId, cropRatio, filterIntensity, rotationDegrees)
                            currentSubScreen = StudioSubScreen.DASHBOARD
                        },
                        onCompressBatch = { targets, format, quality, scalePercent, preserveExif ->
                            onCompressBatch(targets, format, quality, scalePercent, preserveExif)
                            currentSubScreen = StudioSubScreen.DASHBOARD
                        }
                    )
                }

                StudioSubScreen.RAW_EDITOR -> {
                    StudioRawWorkspace(
                        initialPhoto = activeTargetPhoto ?: photos.firstOrNull(),
                        allPhotos = photos,
                        onClose = { currentSubScreen = StudioSubScreen.DASHBOARD },
                        onExportRawAsJpeg = { targetPhoto, kelvin, tint, exposure, high, shadow, chroma ->
                            onDevelopRaw(
                                targetPhoto,
                                kelvin,
                                tint,
                                exposure,
                                high,
                                shadow,
                                chroma
                            )
                            currentSubScreen = StudioSubScreen.DASHBOARD
                        }
                    )
                }

                StudioSubScreen.PRIVATE_VAULT -> {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                        StudioSubScreenBackHeader(
                            title = "PRIVATE PIN VAULT",
                            activeTheme = activeTheme,
                            onBack = { currentSubScreen = StudioSubScreen.DASHBOARD }
                        )
                        VaultScreen(
                            isUnlocked = isVaultUnlocked,
                            currentPinHint = vaultPinHint,
                            vaultPhotos = vaultPhotos,
                            biometricEnabled = vaultBiometricEnabled,
                            onToggleBiometric = onToggleVaultBiometric,
                            onAttemptUnlock = onAttemptUnlockVault,
                            onLockVault = onLockVault,
                            onChangePin = onChangeVaultPin,
                            onPhotoClick = onPhotoClick,
                            onUnhidePhoto = onUnhideVaultPhoto
                        )
                    }
                }

                StudioSubScreen.RECYCLE_BIN -> {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                        StudioSubScreenBackHeader(
                            title = "RECYCLE BIN & RECOVERY",
                            activeTheme = activeTheme,
                            onBack = { currentSubScreen = StudioSubScreen.DASHBOARD }
                        )
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

        if (showStorageAnalysisDialog) {
            val totalBytes = photos.sumOf { it.sizeBytes }
            val largePhotos = photos.filter { it.sizeBytes > 4_000_000L }
            val largeBytes = largePhotos.sumOf { it.sizeBytes }
            val editedCount = photos.count {
                it.appliedFilter != "Original" ||
                    it.albumName.contains("Edited", ignoreCase = true) ||
                    it.albumName.contains("Studio", ignoreCase = true)
            }
            // Detect duplicate / similar burst candidates (matching normalized base title or identical dimensions + similar byte size)
            val duplicateCandidates = remember(photos) {
                val bySignature = photos.groupBy { photo ->
                    val normalizedTitle = photo.title
                        .lowercase()
                        .replace(Regex("\\(\\d+\\)|_copy|_dup|-\\d+$"), "")
                        .trim()
                    "${normalizedTitle}_${photo.width}x${photo.height}"
                }
                bySignature.values
                    .filter { it.size > 1 }
                    .flatMap { group -> group.drop(1) }
            }
            val duplicateBytes = duplicateCandidates.sumOf { it.sizeBytes }

            AlertDialog(
                onDismissRequest = { showStorageAnalysisDialog = false },
                title = {
                    Text(
                        text = "Storage & Duplicate Cleaner",
                        fontWeight = FontWeight.Bold,
                        color = activeTheme.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Total Gallery Size: ${formatBytes(totalBytes)} (${photos.size} photos)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = activeTheme.textPrimary
                        )
                        HorizontalDivider(color = activeTheme.borderStroke)
                        Text(
                            text = "• Duplicate / Similar Candidates: ${duplicateCandidates.size} photos (${formatBytes(duplicateBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (duplicateCandidates.isNotEmpty()) activeTheme.primaryAccent else activeTheme.textSecondary
                        )
                        Text(
                            text = "• Large Masters (> 4 MB): ${largePhotos.size} photos (${formatBytes(largeBytes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeTheme.textSecondary
                        )
                        Text(
                            text = "• Studio & Edited Prints: $editedCount photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeTheme.textSecondary
                        )
                        Text(
                            text = "• Recycle Bin: ${trashedPhotos.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeTheme.textSecondary
                        )
                        if (duplicateCandidates.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    showStorageAnalysisDialog = false
                                    activeTargetPhoto = duplicateCandidates.firstOrNull()
                                    currentSubScreen = StudioSubScreen.COMPRESSION_STUDIO
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clean_duplicates_button")
                            ) {
                                Text("Inspect ${duplicateCandidates.size} Duplicate Candidates")
                            }
                        }
                        Text(
                            text = "Tip: Use Smart Image Compression export profiles (Web 1080p, Social Ready, Storage Saver) to save up to 80% space.",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeTheme.primaryAccent
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStorageAnalysisDialog = false
                            activeTargetPhoto = largePhotos.firstOrNull() ?: photos.firstOrNull()
                            if (photos.isNotEmpty()) {
                                currentSubScreen = StudioSubScreen.COMPRESSION_STUDIO
                            }
                        }
                    ) {
                        Text("Compress Heavy Photos")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStorageAnalysisDialog = false }) {
                        Text("Close", color = activeTheme.textSecondary)
                    }
                },
                containerColor = activeTheme.surface,
                textContentColor = activeTheme.textPrimary
            )
        }

        val frameTarget = exifFrameTargetPhoto
        if (frameTarget != null) {
            var selectedFrameStyle by remember(frameTarget.id) { mutableStateOf("Editorial Cream") }
            var stampExif by remember(frameTarget.id) { mutableStateOf(true) }
            var ageBadgeText by remember(frameTarget.id) {
                mutableStateOf(frameTarget.milestoneSticker.ifBlank { "" })
            }
            val frameStyles = listOf(
                "Editorial Cream",
                "Museum White Mat",
                "35mm Film",
                "Gold Gallery",
                "Obsidian Mat"
            )

            AlertDialog(
                onDismissRequest = { exifFrameTargetPhoto = null },
                containerColor = activeTheme.surface,
                titleContentColor = activeTheme.textPrimary,
                textContentColor = activeTheme.textPrimary,
                title = {
                    Text(
                        text = "EXIF & Camera Frame",
                        fontWeight = FontWeight.ExtraBold,
                        color = activeTheme.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Frame \"${frameTarget.title}\" with a clean gallery mat and optional camera EXIF or child age stamp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeTheme.textSecondary
                        )
                        frameStyles.forEach { style ->
                            val selected = selectedFrameStyle == style
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedFrameStyle = style }
                            ) {
                                Text(
                                    text = style,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) activeTheme.onPrimaryAccent else activeTheme.textPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stamp Camera EXIF Metadata",
                                style = MaterialTheme.typography.labelLarge,
                                color = activeTheme.textPrimary
                            )
                            Switch(
                                checked = stampExif,
                                onCheckedChange = { stampExif = it }
                            )
                        }
                        OutlinedTextField(
                            value = ageBadgeText,
                            onValueChange = { ageBadgeText = it },
                            label = { Text("Optional Child Age / Keepsake Stamp") },
                            placeholder = { Text("e.g., 6m 12d • First Smile") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCreateFramedPhoto(
                                frameTarget,
                                selectedFrameStyle,
                                stampExif,
                                ageBadgeText.trim()
                            )
                            exifFrameTargetPhoto = null
                        }
                    ) {
                        Text("Export Framed Print")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { exifFrameTargetPhoto = null }) {
                        Text("Cancel", color = activeTheme.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun StudioSubScreenBackHeader(
    title: String,
    activeTheme: CompleteUiThemeSpec,
    onBack: () -> Unit
) {
    Surface(
        color = activeTheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(activeTheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("studio_subscreen_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Studio",
                    tint = activeTheme.textPrimary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = activeTheme.textPrimary
            )
        }
    }
}
