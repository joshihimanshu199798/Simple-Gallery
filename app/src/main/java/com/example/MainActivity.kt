package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AlbumSortOption
import com.example.data.GridDensity
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.formatBytes
import com.example.service.configureAppCheck
import com.example.ui.GalleryTab
import com.example.ui.GalleryViewModel
import com.example.ui.screens.AlbumsGridScreen
import com.example.ui.screens.BentoCollageMakerSheet
import com.example.ui.screens.CompleteUiDesignSwitcherCard
import com.example.ui.screens.ExifMetadataBottomSheet
import com.example.ui.screens.LittleMemoriesScreen
import com.example.ui.screens.MoveOrCopyAlbumDialog
import com.example.ui.screens.PhotoCompareBottomSheet
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.PhotoLightboxScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.screens.WatermarkStudioSheet
import com.example.ui.theme.AppThemeCatalog
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.GalleryLayoutStyle
import com.example.ui.theme.SimpleGalleryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File

import androidx.compose.material.icons.filled.Tune
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.ui.screens.GrowthTimelineScreen
import com.example.ui.screens.GrowthTimelineSetupBottomSheet
import com.example.ui.screens.PeopleDirectoryScreen
import com.example.ui.screens.PersonDetailScreen
import com.example.ui.screens.QuickCleanTriageOverlay
import com.example.ui.screens.RichSearchOverlay
import com.example.ui.screens.StudioTabScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optimized Coil ImageLoader with 35% memory cache and 200MB disk cache for instant photo browsing
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.35)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val galleryViewModel: GalleryViewModel = viewModel(
                factory = GalleryViewModel.Factory(context)
            )
            val activeTheme by galleryViewModel.activeTheme.collectAsStateWithLifecycle()

            SimpleGalleryTheme(themeSpec = activeTheme) {
                SimpleGalleryApp(viewModel = galleryViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SimpleGalleryApp(viewModel: GalleryViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    val themeFilterCategory by viewModel.themeFilterCategory.collectAsStateWithLifecycle()
    val isThemeSwitcherExpanded by viewModel.isThemeSwitcherExpanded.collectAsStateWithLifecycle()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsStateWithLifecycle()
    val isCompareModalOpen by viewModel.isCompareModalOpen.collectAsStateWithLifecycle()
    val isCollageModalOpen by viewModel.isCollageModalOpen.collectAsStateWithLifecycle()
    val isWatermarkModalOpen by viewModel.isWatermarkModalOpen.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val allVisiblePhotos by viewModel.allVisiblePhotos.collectAsStateWithLifecycle()
    val filteredPhotos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val vaultPhotos by viewModel.vaultPhotos.collectAsStateWithLifecycle()
    val trashedPhotos by viewModel.trashedPhotos.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val albumSortOption by viewModel.albumSortOption.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedPhotoIds.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val activeLightboxId by viewModel.activeLightboxPhotoId.collectAsStateWithLifecycle()
    val inspectedExifPhotoId by viewModel.inspectedExifPhotoId.collectAsStateWithLifecycle()
    val editingPhoto by viewModel.editingPhoto.collectAsStateWithLifecycle()
    val editorInitialTab by viewModel.editorInitialTab.collectAsStateWithLifecycle()
    val photoForMemoryComposer by viewModel.photoForMemoryComposer.collectAsStateWithLifecycle()
    val isSlideshowPlaying by viewModel.isSlideshowPlaying.collectAsStateWithLifecycle()
    val slideshowIntervalSec by viewModel.slideshowIntervalSec.collectAsStateWithLifecycle()
    val activeHistogram by viewModel.activeHistogram.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val vaultPin by viewModel.vaultPin.collectAsStateWithLifecycle()
    val vaultBiometricEnabled by viewModel.vaultBiometricEnabled.collectAsStateWithLifecycle()
    val personClusters by viewModel.personClusters.collectAsStateWithLifecycle()
    val photoFaceTags by viewModel.photoFaceTags.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val aiOrganizerUiState by viewModel.aiOrganizerUiState.collectAsStateWithLifecycle()
    val aiRecentResults by viewModel.aiRecentResults.collectAsStateWithLifecycle()
    val aiMoveIntoCategoryAlbums by viewModel.aiMoveIntoCategoryAlbums.collectAsStateWithLifecycle()
    val aiAutoTagOnImport by viewModel.aiAutoTagOnImport.collectAsStateWithLifecycle()
    val isSyncingDeviceGallery by viewModel.isSyncingDeviceGallery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var showBatchAlbumDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var photosPendingPermanentDelete by remember { mutableStateOf<List<PhotoEntity>>(emptyList()) }
    var isStudioWorkspaceActive by remember { mutableStateOf(false) }
    var isSearchOverlayOpen by remember { mutableStateOf(false) }
    var isQuickCleanOpen by remember { mutableStateOf(false) }
    var isPeopleDirectoryOpen by remember { mutableStateOf(false) }
    var activePersonId by remember { mutableStateOf<Long?>(null) }
    var activeGrowthPersonId by remember { mutableStateOf<Long?>(null) }
    var showGrowthSetupForPersonId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbarMessage()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    fun isMediaPermissionDeclaredInManifest(ctx: Context): Boolean {
        return try {
            val pkgInfo = ctx.packageManager.getPackageInfo(
                ctx.packageName,
                PackageManager.GET_PERMISSIONS
            )
            val perms = pkgInfo.requestedPermissions ?: emptyArray()
            Manifest.permission.READ_MEDIA_IMAGES in perms ||
                Manifest.permission.READ_EXTERNAL_STORAGE in perms
        } catch (_: Exception) {
            false
        }
    }

    fun checkFullMediaReadPermission(ctx: Context): Boolean {
        if (!isMediaPermissionDeclaredInManifest(ctx)) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun checkMediaReadPermission(ctx: Context): Boolean {
        if (!isMediaPermissionDeclaredInManifest(ctx)) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val fullGranted = checkFullMediaReadPermission(ctx)
            val partialGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
            fullGranted || partialGranted
        } else {
            checkFullMediaReadPermission(ctx)
        }
    }

    val primaryReadMediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val mediaPermissionState = rememberPermissionState(
        permission = primaryReadMediaPermission
    ) { isGranted ->
        if (isGranted || checkMediaReadPermission(context)) {
            viewModel.syncDeviceGalleryAutomatically(silent = false)
        }
    }

    val mediaPermissionsList = remember {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
            else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val multipleMediaPermissionsState = rememberMultiplePermissionsState(
        permissions = mediaPermissionsList
    ) { permissionsMap ->
        if (permissionsMap.values.any { it } || checkMediaReadPermission(context)) {
            viewModel.syncDeviceGalleryAutomatically(silent = false)
        }
    }

    var systemPermissionGranted by remember { mutableStateOf(checkMediaReadPermission(context)) }
    val hasMediaPermission = mediaPermissionState.status.isGranted ||
        multipleMediaPermissionsState.permissions.any { it.status.isGranted } ||
        systemPermissionGranted
    val shouldShowPermissionRationale = mediaPermissionState.status.shouldShowRationale ||
        multipleMediaPermissionsState.shouldShowRationale

    val requestMediaPermissions: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            multipleMediaPermissionsState.launchMultiplePermissionRequest()
        } else {
            mediaPermissionState.launchPermissionRequest()
        }
    }

    val openAppPermissionSettings: () -> Unit = {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            viewModel.postMessage("Unable to open app permission settings")
        }
    }

    val triggerRescanDeviceGallery: () -> Unit = {
        if (!mediaPermissionState.status.isGranted && !checkFullMediaReadPermission(context)) {
            requestMediaPermissions()
        }
        viewModel.syncDeviceGalleryAutomatically(silent = false)
    }

    // Observe Accompanist permission state transitions and scan local photos as soon as permission is granted
    LaunchedEffect(mediaPermissionState.status.isGranted, hasMediaPermission) {
        if (mediaPermissionState.status.isGranted || hasMediaPermission) {
            systemPermissionGranted = true
            viewModel.syncDeviceGalleryAutomatically(silent = true)
        }
    }

    // Automatically request READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE on launch if not yet granted
    LaunchedEffect(Unit) {
        if (hasMediaPermission || checkMediaReadPermission(context)) {
            systemPermissionGranted = true
            viewModel.syncDeviceGalleryAutomatically(silent = true)
        } else {
            requestMediaPermissions()
        }
    }

    // Automatically sync device photos whenever the user returns to the app (e.g. after granting in Settings or taking a photo)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = checkMediaReadPermission(context)
                systemPermissionGranted = granted
                if (granted || mediaPermissionState.status.isGranted) {
                    viewModel.syncDeviceGalleryAutomatically(silent = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Zero-permission Android Photo Picker Launcher (Up to 50 personal photos at once)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importDevicePhotos(uris)
        }
    }

    // System Document / File Browser Launcher for personal image files in Downloads/Folders
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importDevicePhotos(uris)
        }
    }

    // Quick Camera Capture Launcher for testing with a live camera shot
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.saveCameraCapture(bitmap)
        }
    }

    val launchPhotoPicker = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val launchFileBrowser = {
        documentPickerLauncher.launch(arrayOf("image/*"))
    }

    val launchCameraCapture = {
        cameraPreviewLauncher.launch(null)
    }

    // Clear multi-select on Back press
    if (selectedIds.isNotEmpty() || isSelectionMode) {
        BackHandler { viewModel.clearSelection() }
    } else if (currentTab != GalleryTab.PICS) {
        BackHandler { viewModel.selectTab(GalleryTab.PICS) }
    }

    // 1. Full-screen Darkroom Photo Editor & Interactive Crop Studio
    val currentEditingPhoto = editingPhoto
    if (currentEditingPhoto != null) {
        PhotoEditorScreen(
            photo = currentEditingPhoto,
            initialTab = editorInitialTab,
            onCancel = { viewModel.closePhotoEditor() },
            onSaveEdits = { preset, brightness, contrast, saturation, rot, flipH, flipV, crop, copy, customCropBounds ->
                viewModel.applyPhotoEdits(
                    original = currentEditingPhoto,
                    preset = preset,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    rotationDegrees = rot,
                    flipHorizontal = flipH,
                    flipVertical = flipV,
                    cropRatio = crop,
                    customCropBounds = customCropBounds,
                    saveAsCopy = copy
                )
            }
        )
        return
    }

    // 2. Full-screen Lightbox Viewer & Slideshow
    val activePhoto = remember(activeLightboxId, allVisiblePhotos, vaultPhotos) {
        if (activeLightboxId == null) null
        else (allVisiblePhotos + vaultPhotos).firstOrNull { it.id == activeLightboxId }
    }

    if (activePhoto != null) {
        val lightboxList = remember(
            activePhoto.id,
            activePhoto.isVaultLocked,
            activeGrowthPersonId,
            activePersonId,
            photoFaceTags,
            filteredPhotos,
            allVisiblePhotos,
            vaultPhotos
        ) {
            when {
                activePhoto.isVaultLocked -> vaultPhotos.ifEmpty { listOf(activePhoto) }
                activeGrowthPersonId != null -> {
                    val taggedIds = photoFaceTags
                        .filter { it.personId == activeGrowthPersonId }
                        .map { it.photoId }
                        .toSet()
                    val personList = allVisiblePhotos.filter { it.id in taggedIds }
                    if (personList.any { it.id == activePhoto.id }) personList else listOf(activePhoto)
                }
                activePersonId != null -> {
                    val taggedIds = photoFaceTags
                        .filter { it.personId == activePersonId }
                        .map { it.photoId }
                        .toSet()
                    val personList = allVisiblePhotos.filter { it.id in taggedIds }
                    if (personList.any { it.id == activePhoto.id }) personList else listOf(activePhoto)
                }
                filteredPhotos.any { it.id == activePhoto.id } -> filteredPhotos
                allVisiblePhotos.any { it.id == activePhoto.id } -> allVisiblePhotos
                else -> listOf(activePhoto)
            }
        }
        PhotoLightboxScreen(
            photo = activePhoto,
            currentList = lightboxList,
            albums = albums,
            histogram = activeHistogram,
            isSlideshowPlaying = isSlideshowPlaying,
            slideshowIntervalSec = slideshowIntervalSec,
            onClose = { viewModel.closeLightbox() },
            onSelectPhoto = { nextPhoto -> viewModel.openLightbox(nextPhoto, isSlideshowPlaying) },
            onToggleSlideshow = { viewModel.toggleSlideshow() },
            onCycleSlideshowInterval = { viewModel.cycleSlideshowInterval() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenEditor = { viewModel.openPhotoEditor(it) },
            onCreateMemory = { viewModel.openMemoryComposerWithPhoto(it) },
            onSharePhoto = { shareLocalPhoto(context, it, viewModel::postMessage) },
            onMoveOrCopyAlbum = { targetPhoto, targetAlbum, copy ->
                viewModel.movePhotosToAlbum(listOf(targetPhoto.id), targetAlbum, copy)
            },
            onToggleVaultLock = { targetPhoto ->
                viewModel.setVaultLocked(listOf(targetPhoto.id), !targetPhoto.isVaultLocked)
            },
            onMoveToTrash = { targetPhoto ->
                viewModel.moveToTrash(listOf(targetPhoto.id))
            },
            onUpdateMetadata = { targetPhoto, title, album, tags, notes, loc, cam, aper, shut, iso, focal ->
                viewModel.updatePhotoMetadata(
                    photo = targetPhoto,
                    title = title,
                    album = album,
                    tags = tags,
                    notes = notes,
                    location = loc,
                    cameraModel = cam,
                    aperture = aper,
                    shutterSpeed = shut,
                    iso = iso,
                    focalLength = focal
                )
            },
            onAnalyzeWithAi = { targetPhoto ->
                viewModel.startAiAnalyzeSinglePhoto(targetPhoto)
            },
            onOpenCollageStudio = {
                viewModel.closeLightbox()
                viewModel.openCollageModal()
            },
            onApplyQuickLut = { targetPhoto, preset, saveCopy ->
                viewModel.applyQuickLutPreset(targetPhoto, preset, saveCopy)
            },
            onShrinkPhoto = { targetPhoto, scaleFactor, quality, replaceOriginal ->
                viewModel.shrinkAndOptimizePhoto(targetPhoto, scaleFactor, quality, replaceOriginal)
            },
            onCreateFramedPhoto = { targetPhoto, frameStyle, includeExif ->
                viewModel.createFramedPhoto(targetPhoto, frameStyle, includeExif)
            },
            onStampWatermark = { targetPhoto, watermarkText, includeExif ->
                viewModel.createWatermarkedPhoto(targetPhoto, watermarkText, includeExif)
            },
            onQuickRotate90 = { targetPhoto ->
                viewModel.quickRotatePhoto90(targetPhoto)
            },
            onDuplicatePhoto = { targetPhoto ->
                viewModel.duplicatePhoto(targetPhoto)
            },
            onOpenCompare = {
                viewModel.closeLightbox()
                viewModel.openCompareModal()
            },
            onOpenCropStudio = { targetPhoto ->
                viewModel.openPhotoCropStudio(targetPhoto)
            },
            onDeletePermanently = { targetPhoto ->
                viewModel.permanentlyDelete(listOf(targetPhoto.id))
            },
            faceTags = photoFaceTags,
            personClusters = personClusters,
            onStripExifAndShare = { targetPhoto ->
                viewModel.prepareExifStrippedShare(targetPhoto) { strippedFile ->
                    if (strippedFile != null) {
                        shareStrippedFile(context, strippedFile, targetPhoto.title, viewModel::postMessage)
                    } else {
                        viewModel.postMessage("Unable to strip EXIF metadata for sharing")
                    }
                }
            },
            onOpenPersonDetail = { person ->
                activePersonId = person.id
                viewModel.closeLightbox()
            }
        )
        return
    }

    // 3. Main Gallery Archive Scaffold with Floating Pill Bottom Dock (No duplicate upper bar)
    Scaffold(
        containerColor = activeTheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    val slideDirection = if (forward) 1 else -1
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth -> (fullWidth * 0.14f * slideDirection).toInt() }
                    ) + fadeIn(animationSpec = tween(220)) + scaleIn(
                        initialScale = 0.97f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )) togetherWith (slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetX = { fullWidth -> -(fullWidth * 0.10f * slideDirection).toInt() }
                    ) + fadeOut(animationSpec = tween(160)) + scaleOut(
                        targetScale = 0.97f,
                        animationSpec = tween(160)
                    ))
                },
                label = "main_gallery_tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    GalleryTab.PICS -> {
                        TimelineScreen(
                            photos = filteredPhotos,
                            activeTheme = activeTheme,
                            themeFilterCategory = themeFilterCategory,
                            isThemeSwitcherExpanded = isThemeSwitcherExpanded,
                            isSearchExpanded = isSearchExpanded,
                            filterState = filterState,
                            availableTags = availableTags,
                            selectedPhotoIds = selectedIds,
                            onSelectCompleteTheme = viewModel::selectCompleteTheme,
                            onSelectThemeCategory = viewModel::setThemeFilterCategory,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectTag = viewModel::selectTagFilter,
                            onSelectSort = viewModel::updateSortOption,
                            onPhotoClick = { photo ->
                                if (isSelectionMode || selectedIds.isNotEmpty()) {
                                    viewModel.togglePhotoSelection(photo.id)
                                } else {
                                    viewModel.openLightbox(photo)
                                }
                            },
                            onPhotoLongClick = { photo ->
                                viewModel.togglePhotoSelection(photo.id)
                            },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onStartSlideshow = {
                                filteredPhotos.firstOrNull()?.let { first ->
                                    viewModel.openLightbox(first, startSlideshow = true)
                                }
                            },
                            onImportClick = launchPhotoPicker,
                            onClearAlbumFilter = { viewModel.openAlbumDetail(null) },
                            onCycleLayoutStyle = { viewModel.pinchAdjustGridStyle(zoomIn = false) },
                            onSelectLayoutStyle = { viewModel.setLayoutStyleOverride(it) },
                            onSelectGridDensity = { viewModel.setGridDensity(it) },
                            onSelectSquareGridMode = { viewModel.setSquareGridMode(it) },
                            onPinchGridStyle = { zoomIn -> viewModel.pinchAdjustGridStyle(zoomIn) },
                            onOpenCollageStudio = { viewModel.openCollageModal() },
                            allPhotosForCategories = allVisiblePhotos,
                            aiOrganizerUiState = aiOrganizerUiState,
                            onStartAiOrganize = { viewModel.startAiOrganizeAllPhotos(onlyUntagged = false) },
                            onCancelAiOrganize = { viewModel.cancelAiOrganizerService() },
                            onDismissAiBanner = { viewModel.dismissAiOrganizerBanner() },
                            onInspectExif = { viewModel.openExifSheetForPhoto(it) },
                            onOpenDarkroom = { viewModel.openPhotoEditor(it) },
                            onOpenCropStudio = { viewModel.openPhotoCropStudio(it) },
                            onAssignCategory = viewModel::assignPhotoCategory,
                            onQuickRotate90 = viewModel::quickRotatePhoto90,
                            onDuplicatePhoto = viewModel::duplicatePhoto,
                            onMoveToVault = { viewModel.setVaultLocked(listOf(it.id), true) },
                            onMoveToTrash = { viewModel.moveToTrash(listOf(it.id)) },
                            onFavoriteDateGroup = viewModel::favoriteAllInList,
                            onOpenWatermarkStudio = { viewModel.openWatermarkModal() },
                            onOpenCreateAlbum = { showCreateAlbumDialog = true },
                            onOpenSettings = { showSettingsDialog = true },
                            onToggleSearchBar = { viewModel.toggleSearchExpanded() },
                            hasMediaPermission = hasMediaPermission,
                            shouldShowPermissionRationale = shouldShowPermissionRationale,
                            onRequestMediaPermission = requestMediaPermissions,
                            onOpenAppSettings = openAppPermissionSettings,
                            isRefreshing = isSyncingDeviceGallery,
                            onSyncDeviceGallery = triggerRescanDeviceGallery,
                            onBrowseFilesClick = launchFileBrowser,
                            onTakeCameraPhotoClick = launchCameraCapture,
                            onClearSamplePhotos = viewModel::clearDefaultSamplePhotos,
                            onLoadSamplePhotos = viewModel::loadSampleDemoPhotos,
                            isSelectionMode = isSelectionMode || selectedIds.isNotEmpty(),
                            onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                            onToggleSelectPhoto = { photo -> viewModel.togglePhotoSelection(photo.id) },
                            onSelectAllPhotos = {
                                if (selectedIds.size == filteredPhotos.size && filteredPhotos.isNotEmpty()) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectAllCurrentPhotos(filteredPhotos)
                                }
                            },
                            onClearSelection = { viewModel.clearSelection() },
                            onRequestDeletePhotos = { targetPhotos ->
                                photosPendingPermanentDelete = targetPhotos
                            },
                            personClusters = personClusters,
                            onOpenQuickClean = {
                                if (filteredPhotos.isNotEmpty()) {
                                    isQuickCleanOpen = true
                                } else {
                                    viewModel.postMessage("Load photos first to start Quick Clean")
                                }
                            },
                            onOpenSearchOverlay = { isSearchOverlayOpen = true },
                            onOpenPersonDetail = { person -> activePersonId = person.id }
                        )
                    }
                    GalleryTab.ALBUMS -> {
                        AnimatedContent(
                            targetState = filterState.selectedAlbumName,
                            transitionSpec = {
                                if (targetState != null) {
                                    (scaleIn(
                                        initialScale = 0.94f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ) + fadeIn(tween(200))) togetherWith (scaleOut(
                                        targetScale = 1.04f,
                                        animationSpec = tween(160)
                                    ) + fadeOut(tween(160)))
                                } else {
                                    (scaleIn(
                                        initialScale = 1.04f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ) + fadeIn(tween(200))) togetherWith (scaleOut(
                                        targetScale = 0.94f,
                                        animationSpec = tween(160)
                                    ) + fadeOut(tween(160)))
                                }
                            },
                            label = "album_detail_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { selectedAlbum ->
                            if (selectedAlbum != null) {
                                TimelineScreen(
                                    photos = filteredPhotos,
                                    activeTheme = activeTheme,
                                    themeFilterCategory = themeFilterCategory,
                                    isThemeSwitcherExpanded = false,
                                    isSearchExpanded = isSearchExpanded,
                                    filterState = filterState,
                                    availableTags = availableTags,
                                    selectedPhotoIds = selectedIds,
                                    onSelectCompleteTheme = viewModel::selectCompleteTheme,
                                    onSelectThemeCategory = viewModel::setThemeFilterCategory,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onSelectTag = viewModel::selectTagFilter,
                                    onSelectSort = viewModel::updateSortOption,
                                    onPhotoClick = { photo ->
                                        if (isSelectionMode || selectedIds.isNotEmpty()) {
                                            viewModel.togglePhotoSelection(photo.id)
                                        } else {
                                            viewModel.openLightbox(photo)
                                        }
                                    },
                                    onPhotoLongClick = { photo ->
                                        viewModel.togglePhotoSelection(photo.id)
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onStartSlideshow = {
                                        filteredPhotos.firstOrNull()?.let { first ->
                                            viewModel.openLightbox(first, startSlideshow = true)
                                        }
                                    },
                                    onImportClick = launchPhotoPicker,
                                    onClearAlbumFilter = { viewModel.openAlbumDetail(null) },
                                    onCycleLayoutStyle = { viewModel.pinchAdjustGridStyle(zoomIn = false) },
                                    onSelectLayoutStyle = { viewModel.setLayoutStyleOverride(it) },
                                    onSelectGridDensity = { viewModel.setGridDensity(it) },
                                    onSelectSquareGridMode = { viewModel.setSquareGridMode(it) },
                                    onPinchGridStyle = { zoomIn -> viewModel.pinchAdjustGridStyle(zoomIn) },
                                    onOpenCollageStudio = { viewModel.openCollageModal() },
                                    allPhotosForCategories = allVisiblePhotos,
                                    aiOrganizerUiState = aiOrganizerUiState,
                                    onStartAiOrganize = { viewModel.startAiOrganizeAllPhotos(onlyUntagged = false) },
                                    onCancelAiOrganize = { viewModel.cancelAiOrganizerService() },
                                    onDismissAiBanner = { viewModel.dismissAiOrganizerBanner() },
                                    onInspectExif = { viewModel.openExifSheetForPhoto(it) },
                                    onOpenDarkroom = { viewModel.openPhotoEditor(it) },
                                    onOpenCropStudio = { viewModel.openPhotoCropStudio(it) },
                                    onAssignCategory = viewModel::assignPhotoCategory,
                                    onQuickRotate90 = viewModel::quickRotatePhoto90,
                                    onDuplicatePhoto = viewModel::duplicatePhoto,
                                    onMoveToVault = { viewModel.setVaultLocked(listOf(it.id), true) },
                                    onMoveToTrash = { viewModel.moveToTrash(listOf(it.id)) },
                                    onFavoriteDateGroup = viewModel::favoriteAllInList,
                                    onOpenWatermarkStudio = { viewModel.openWatermarkModal() },
                                    onOpenCreateAlbum = { showCreateAlbumDialog = true },
                                    onOpenSettings = { showSettingsDialog = true },
                                    onToggleSearchBar = { viewModel.toggleSearchExpanded() },
                                    hasMediaPermission = hasMediaPermission,
                                    shouldShowPermissionRationale = shouldShowPermissionRationale,
                                    onRequestMediaPermission = requestMediaPermissions,
                                    onOpenAppSettings = openAppPermissionSettings,
                                    isRefreshing = isSyncingDeviceGallery,
                                    onSyncDeviceGallery = triggerRescanDeviceGallery,
                                    onBrowseFilesClick = launchFileBrowser,
                                    onTakeCameraPhotoClick = launchCameraCapture,
                                    onClearSamplePhotos = viewModel::clearDefaultSamplePhotos,
                                    onLoadSamplePhotos = viewModel::loadSampleDemoPhotos,
                                    isSelectionMode = isSelectionMode || selectedIds.isNotEmpty(),
                                    onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                                    onToggleSelectPhoto = { photo -> viewModel.togglePhotoSelection(photo.id) },
                                    onSelectAllPhotos = {
                                        if (selectedIds.size == filteredPhotos.size && filteredPhotos.isNotEmpty()) {
                                            viewModel.clearSelection()
                                        } else {
                                            viewModel.selectAllCurrentPhotos(filteredPhotos)
                                        }
                                    },
                                    onClearSelection = { viewModel.clearSelection() },
                                    onRequestDeletePhotos = { targetPhotos ->
                                        photosPendingPermanentDelete = targetPhotos
                                    },
                                    personClusters = personClusters,
                                    onOpenQuickClean = {
                                        if (filteredPhotos.isNotEmpty()) {
                                            isQuickCleanOpen = true
                                        } else {
                                            viewModel.postMessage("No photos in this album yet")
                                        }
                                    },
                                    onOpenSearchOverlay = { isSearchOverlayOpen = true },
                                    onOpenPersonDetail = { person -> activePersonId = person.id }
                                )
                            } else {
                                AlbumsGridScreen(
                                    albums = albums,
                                    allPhotos = allVisiblePhotos,
                                    personClusters = personClusters,
                                    albumSortOption = albumSortOption,
                                    onSelectAlbumSort = viewModel::updateAlbumSortOption,
                                    onAlbumClick = { albumName ->
                                        viewModel.openAlbumDetail(albumName)
                                    },
                                    onCreateAlbumClick = { showCreateAlbumDialog = true },
                                    onOpenPeopleDirectory = { isPeopleDirectoryOpen = true },
                                    onOpenGrowthTimeline = { person -> activeGrowthPersonId = person.id },
                                    onOpenPersonDetail = { person -> activePersonId = person.id }
                                )
                            }
                        }
                    }
                    GalleryTab.STUDIO -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            StudioTabScreen(
                                photos = allVisiblePhotos,
                                activeTheme = activeTheme,
                                onAdvancedSaveEdits = { photo, cropRatio, customCropRect, rot, flipH, flipV, exp, bright, cont, sat, high, shad, temp, tint, vig, gr, fMat, fInt, smooth, saveCopy ->
                                    viewModel.applyAdvancedStudioEdits(
                                        original = photo,
                                        cropRatio = cropRatio,
                                        customCropRect = customCropRect,
                                        rotationDegrees = rot,
                                        flipH = flipH,
                                        flipV = flipV,
                                        exposure = exp,
                                        brightness = bright,
                                        contrast = cont,
                                        saturation = sat,
                                        highlights = high,
                                        shadows = shad,
                                        temperature = temp,
                                        tint = tint,
                                        vignette = vig,
                                        grain = gr,
                                        filterMatrix = fMat,
                                        filterIntensity = fInt,
                                        skinSmoothing = smooth,
                                        saveAsCopy = saveCopy
                                    )
                                },
                                onExecuteAiCutout = { targetPhoto ->
                                    viewModel.exportAiCutout(targetPhoto)
                                },
                                onExecuteAiInpaint = { targetPhoto, rects ->
                                    viewModel.exportAiInpaint(targetPhoto, rects)
                                },
                                onExecuteAiBgReplace = { targetPhoto, style ->
                                    viewModel.exportAiBgReplace(targetPhoto, style)
                                },
                                onExecuteAiRestore = { targetPhoto ->
                                    viewModel.exportAiRestoration(targetPhoto)
                                },
                                onExecuteAiSuperRes = { targetPhoto ->
                                    viewModel.exportAiSuperRes(targetPhoto)
                                },
                                onCompressBatch = { targets, format, quality, scalePercent, preserveExif ->
                                    viewModel.compressPhotosBatch(
                                        targets = targets,
                                        format = format,
                                        quality = quality,
                                        scalePercent = scalePercent,
                                        preserveExif = preserveExif
                                    )
                                },
                                onCreateCollage = { paths, mode, ratio, spacing, cornerRadius, bgHex ->
                                    viewModel.createAdvancedCollage(
                                        photoPaths = paths,
                                        layoutMode = mode,
                                        aspectRatio = ratio,
                                        spacingDp = spacing,
                                        cornerRadiusDp = cornerRadius,
                                        backgroundColorHex = bgHex
                                    )
                                },
                                onExecuteBatch = { targets, mode, presetId, cropRatio, filterIntensity, rotationDegrees ->
                                    viewModel.executeBatchStudioAction(
                                        targets = targets,
                                        mode = mode,
                                        presetId = presetId,
                                        cropRatio = cropRatio,
                                        filterIntensity = filterIntensity,
                                        rotationDegrees = rotationDegrees
                                    )
                                },
                                onDevelopRaw = { photo, kelvin, tint, exposure, high, shadow, chroma ->
                                    viewModel.exportRawDevelopedPhoto(
                                        photo = photo,
                                        kelvinTemp = kelvin,
                                        tint = tint,
                                        exposureEv = exposure,
                                        highlightRecovery = high,
                                        shadowRecovery = shadow,
                                        chromaDenoise = chroma
                                    )
                                },
                                onPickCustomPhoto = launchPhotoPicker,
                                onTakeCameraPhoto = launchCameraCapture,
                                onWorkspaceActiveChanged = { isStudioWorkspaceActive = it },
                                vaultPhotos = vaultPhotos,
                                trashedPhotos = trashedPhotos,
                                isVaultUnlocked = isVaultUnlocked,
                                vaultPinHint = vaultPin,
                                vaultBiometricEnabled = vaultBiometricEnabled,
                                onToggleVaultBiometric = viewModel::toggleVaultBiometric,
                                onAttemptUnlockVault = viewModel::attemptUnlockVault,
                                onLockVault = viewModel::lockVault,
                                onChangeVaultPin = viewModel::updateVaultPin,
                                onUnhideVaultPhoto = { photo ->
                                    viewModel.setVaultLocked(listOf(photo.id), false)
                                },
                                onRestoreTrashPhoto = { photo ->
                                    viewModel.restoreFromTrash(listOf(photo.id))
                                },
                                onDeletePermanently = { photo ->
                                    viewModel.permanentlyDelete(listOf(photo.id))
                                },
                                onEmptyTrash = { viewModel.emptyRecycleBin() },
                                onOpenCompare = { viewModel.openCompareModal() },
                                onOpenWatermark = { viewModel.openWatermarkModal() },
                                onCreateFramedPhoto = { photo, frameStyle, showExif, badgeText ->
                                    viewModel.createFramedPhoto(photo, frameStyle, showExif, badgeText)
                                },
                                onStartAiOrganizeAll = { onlyUntagged ->
                                    viewModel.startAiOrganizeAllPhotos(onlyUntagged)
                                },
                                aiOrganizerUiState = aiOrganizerUiState,
                                onPhotoClick = { photo -> viewModel.openLightbox(photo) }
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedIds.isNotEmpty(),
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it }
                    ) + fadeIn(tween(180)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )) togetherWith (slideOutVertically(
                        animationSpec = tween(180),
                        targetOffsetY = { it }
                    ) + fadeOut(tween(150)) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(150)
                    ))
                },
                label = "bottom_dock_mode_transition",
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { hasSelection ->
                if (hasSelection) {
                    val selectedPhotosList = remember(allVisiblePhotos, vaultPhotos, selectedIds) {
                        (allVisiblePhotos + vaultPhotos).filter { it.id in selectedIds }
                    }
                    SelectedPhotosBottomActionDock(
                        selectedCount = selectedIds.size,
                        activeTheme = activeTheme,
                        onFavoriteSelected = { viewModel.batchFavoriteSelected(true) },
                        onAiTagSelected = { viewModel.startAiAnalyzePhotosByIds(selectedIds.toList()) },
                        onCollageSelected = { viewModel.openCollageModal() },
                        onCompareSelected = { viewModel.openCompareModal() },
                        onShrinkSelected = { viewModel.batchShrinkPhotos(selectedIds.toList()) },
                        onWatermarkSelected = { viewModel.openWatermarkModal() },
                        onRotateSelected = {
                            selectedPhotosList.firstOrNull()?.let { viewModel.quickRotatePhoto90(it) }
                        },
                        onDuplicateSelected = {
                            selectedPhotosList.firstOrNull()?.let { viewModel.duplicatePhoto(it) }
                        },
                        onInspectExifSelected = {
                            selectedPhotosList.firstOrNull()?.let { viewModel.openExifSheetForPhoto(it) }
                        },
                        onMoveAlbumSelected = { showBatchAlbumDialog = true },
                        onShareSelected = {
                            selectedPhotosList.firstOrNull()?.let {
                                shareLocalPhoto(context, it, viewModel::postMessage)
                            }
                        },
                        onVaultSelected = { viewModel.setVaultLocked(selectedIds.toList(), true) },
                        onTrashSelected = { viewModel.moveToTrash(selectedIds.toList()) },
                        onDeleteSelectedPermanently = {
                            if (selectedPhotosList.isNotEmpty()) {
                                photosPendingPermanentDelete = selectedPhotosList
                            }
                        },
                        onClearSelection = { viewModel.clearSelection() }
                    )
                } else if (!isStudioWorkspaceActive || currentTab != GalleryTab.STUDIO) {
                    FloatingGalleryBottomDock(
                        currentTab = currentTab,
                        activeTheme = activeTheme,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Deleting Selected Photos from Local Storage
    if (photosPendingPermanentDelete.isNotEmpty()) {
        val targetPhotos = photosPendingPermanentDelete
        val totalSelectedBytes = remember(targetPhotos) {
            targetPhotos.sumOf { it.sizeBytes }
        }
        AlertDialog(
            onDismissRequest = { photosPendingPermanentDelete = emptyList() },
            modifier = Modifier.testTag("delete_confirmation_dialog"),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = if (targetPhotos.size == 1) {
                        "Delete Photo from Local Storage?"
                    } else {
                        "Delete ${targetPhotos.size} Selected Photos?"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (targetPhotos.size == 1) {
                            "Are you sure you want to permanently delete \"${targetPhotos.first().title}\" (${formatBytes(totalSelectedBytes)}) from your device's local storage? This action cannot be undone."
                        } else {
                            "Are you sure you want to permanently delete these ${targetPhotos.size} selected photos (${formatBytes(totalSelectedBytes)} total) from your device's local storage? This action cannot be undone."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = activeTheme.textSecondary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_dialog_preview_strip")
                    ) {
                        items(targetPhotos.take(8), key = { it.id }) { photoItem ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(photoItem.filePath),
                                    contentDescription = photoItem.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val ids = targetPhotos.map { it.id }
                            photosPendingPermanentDelete = emptyList()
                            viewModel.moveToTrash(ids)
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
                        val ids = targetPhotos.map { it.id }
                        photosPendingPermanentDelete = emptyList()
                        viewModel.permanentlyDelete(ids)
                    },
                    modifier = Modifier.testTag("confirm_delete_photos_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (targetPhotos.size == 1) {
                            "Delete from Device"
                        } else {
                            "Delete ${targetPhotos.size} Photos"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { photosPendingPermanentDelete = emptyList() },
                    modifier = Modifier.testTag("cancel_delete_photos_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive Bento Collage Maker Sheet
    if (isCollageModalOpen) {
        BentoCollageMakerSheet(
            photos = allVisiblePhotos,
            onDismiss = { viewModel.closeCollageModal() },
            onCreateCollage = { paths, title, layoutMode, bgHex ->
                viewModel.createBentoCollage(paths, title, layoutMode, bgHex)
            }
        )
    }

    // Settings & Complete UI Theme Chooser Modal (Houses all 11 Light/White & Dark Themes + Photo Grid Options)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = activeTheme.surface,
            titleContentColor = activeTheme.textPrimary,
            textContentColor = activeTheme.textSecondary,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Appearance & Themes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = activeTheme.textPrimary
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} • Build ${BuildConfig.VERSION_CODE}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = activeTheme.primaryAccent
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = activeTheme.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = activeTheme.primaryAccent.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.primaryAccent.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Active Theme: ${activeTheme.name.replace(" (Light)", "").replace(" (Dark)", "")}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = activeTheme.primaryAccent
                            )
                            Text(
                                text = "Photo Grid: ${filterState.gridDensity.label} • ${if (filterState.isSquareGrid) "1:1 Square" else "Adaptive Ratio"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                    }

                    // Quick Default Minimalist Pure White & Dark UI Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectCompleteTheme(
                                    AppThemeCatalog.findById("warm_editorial_light")
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTheme.id == "warm_editorial_light") activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                contentColor = if (activeTheme.id == "warm_editorial_light") activeTheme.onPrimaryAccent else activeTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_pure_white_button")
                        ) {
                            Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Warm Cream", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                viewModel.selectCompleteTheme(AppThemeCatalog.darkThemes.first())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!activeTheme.isLight) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                contentColor = if (!activeTheme.isLight) activeTheme.onPrimaryAccent else activeTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_dark_ui_button")
                        ) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dark UI", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Complete UI Design Switcher (All 11 Light/White & Dark Themes)
                    CompleteUiDesignSwitcherCard(
                        activeTheme = activeTheme,
                        selectedCategory = themeFilterCategory,
                        onSelectCategory = viewModel::setThemeFilterCategory,
                        onSelectTheme = viewModel::selectCompleteTheme
                    )

                    // Photo Grid Columns Selector
                    Text(
                        text = "DEFAULT PHOTO GRID COLUMNS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = activeTheme.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GridDensity.entries.forEach { density ->
                            val isSelected = filterState.gridDensity == density
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { viewModel.setGridDensity(density) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "${density.columns} Col",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) activeTheme.onPrimaryAccent else activeTheme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryAccent,
                        contentColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Create New Album Dialog
    if (showCreateAlbumDialog) {
        var albumName by remember { mutableStateOf("") }
        var albumDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateAlbumDialog = false },
            title = { Text("Create New Local Album") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = albumName,
                        onValueChange = { albumName = it },
                        label = { Text("Album Name (e.g., Summer Roadtrip)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_album_name_input")
                    )
                    OutlinedTextField(
                        value = albumDesc,
                        onValueChange = { albumDesc = it },
                        label = { Text("Description (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = albumName.isNotBlank(),
                    onClick = {
                        viewModel.createNewAlbum(albumName, albumDesc)
                        showCreateAlbumDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_album_button")
                ) {
                    Text("Create Album")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch Move / Copy Album Dialog
    if (showBatchAlbumDialog && selectedIds.isNotEmpty()) {
        MoveOrCopyAlbumDialog(
            currentAlbum = "",
            albums = albums,
            onDismiss = { showBatchAlbumDialog = false },
            onConfirm = { targetAlbum, copy ->
                viewModel.movePhotosToAlbum(selectedIds.toList(), targetAlbum, copy)
                showBatchAlbumDialog = false
            }
        )
    }

    // Rich Search Overlay (People & Pets + Quick Filter Chips + Live Matching Grid)
    if (isSearchOverlayOpen) {
        RichSearchOverlay(
            initialQuery = filterState.searchQuery,
            allPhotos = allVisiblePhotos,
            personClusters = personClusters,
            faceTags = photoFaceTags,
            activeTheme = activeTheme,
            onQueryChange = viewModel::updateSearchQuery,
            onOpenPersonDetail = { person ->
                activePersonId = person.id
            },
            onSelectQuickChipAndClose = { chip ->
                viewModel.selectTagFilter(chip)
                isSearchOverlayOpen = false
            },
            onPhotoClick = { photo ->
                viewModel.openLightbox(photo)
            },
            onClose = { isSearchOverlayOpen = false }
        )
    }

    // People & Pets Directory Overlay
    if (isPeopleDirectoryOpen) {
        PeopleDirectoryScreen(
            personClusters = personClusters,
            faceTags = photoFaceTags,
            activeTheme = activeTheme,
            onOpenPersonDetail = { person ->
                activePersonId = person.id
            },
            onClose = { isPeopleDirectoryOpen = false }
        )
    }

    // Person Detail Overlay
    val currentPerson = remember(activePersonId, personClusters) {
        activePersonId?.let { id -> personClusters.firstOrNull { it.id == id } }
    }
    if (currentPerson != null) {
        val personPhotos = remember(currentPerson.id, photoFaceTags, allVisiblePhotos) {
            val taggedPhotoIds = photoFaceTags
                .filter { it.personId == currentPerson.id }
                .map { it.photoId }
                .toSet()
            allVisiblePhotos.filter { it.id in taggedPhotoIds }
        }
        PersonDetailScreen(
            person = currentPerson,
            allPersons = personClusters,
            personPhotos = personPhotos,
            activeTheme = activeTheme,
            onBack = { activePersonId = null },
            onRenamePerson = viewModel::renamePerson,
            onMergePerson = viewModel::mergePersons,
            onHidePerson = viewModel::hidePerson,
            onOpenOrSetupGrowthTimeline = {
                if (currentPerson.hasGrowthTimeline) {
                    activeGrowthPersonId = currentPerson.id
                } else {
                    showGrowthSetupForPersonId = currentPerson.id
                }
            },
            onPhotoClick = { photo ->
                viewModel.openLightbox(photo)
            }
        )
    }

    // Child Growth & Milestone Storybook Timeline Overlay
    val growthPerson = remember(activeGrowthPersonId, personClusters) {
        activeGrowthPersonId?.let { id -> personClusters.firstOrNull { it.id == id } }
    }
    if (growthPerson != null) {
        val growthPhotos = remember(growthPerson.id, photoFaceTags, allVisiblePhotos) {
            val taggedIds = photoFaceTags
                .filter { it.personId == growthPerson.id }
                .map { it.photoId }
                .toSet()
            allVisiblePhotos.filter { it.id in taggedIds }
        }
        GrowthTimelineScreen(
            person = growthPerson,
            photos = growthPhotos,
            activeTheme = activeTheme,
            onBack = { activeGrowthPersonId = null },
            onPhotoClick = { photo ->
                viewModel.openLightbox(photo)
            },
            onToggleMilestoneSticker = viewModel::togglePhotoMilestoneSticker
        )
    }

    // 1-Tap Growth Timeline Setup Bottom Sheet
    val setupPerson = remember(showGrowthSetupForPersonId, personClusters) {
        showGrowthSetupForPersonId?.let { id -> personClusters.firstOrNull { it.id == id } }
    }
    if (setupPerson != null) {
        GrowthTimelineSetupBottomSheet(
            person = setupPerson,
            activeTheme = activeTheme,
            onDismiss = { showGrowthSetupForPersonId = null },
            onConfirmSetup = { childName, birthDateMs ->
                viewModel.setupGrowthTimeline(setupPerson.id, childName, birthDateMs)
                showGrowthSetupForPersonId = null
                activeGrowthPersonId = setupPerson.id
            }
        )
    }

    // Tinder-style Quick Clean Swipe Triage Overlay
    if (isQuickCleanOpen) {
        QuickCleanTriageOverlay(
            photos = filteredPhotos,
            activeTheme = activeTheme,
            onFavoritePhoto = { photo ->
                if (!photo.isFavorite) {
                    viewModel.toggleFavorite(photo)
                }
            },
            onTrashPhoto = { photo ->
                viewModel.moveToTrash(listOf(photo.id))
            },
            onRestoreTrashPhoto = { photo ->
                viewModel.restoreFromTrash(listOf(photo.id))
            },
            onClose = { isQuickCleanOpen = false }
        )
    }
}



@Composable
private fun FloatingGalleryBottomDock(
    currentTab: GalleryTab,
    activeTheme: CompleteUiThemeSpec,
    onSelectTab: (GalleryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("bottom_navigation_bar"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = activeTheme.dockBackground,
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(50))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingDockTabItem(
                    tab = GalleryTab.PICS,
                    selected = currentTab == GalleryTab.PICS,
                    icon = Icons.Default.PhotoLibrary,
                    activeTheme = activeTheme,
                    onClick = { onSelectTab(GalleryTab.PICS) },
                    modifier = Modifier.weight(1f)
                )
                FloatingDockTabItem(
                    tab = GalleryTab.ALBUMS,
                    selected = currentTab == GalleryTab.ALBUMS,
                    icon = Icons.Default.Collections,
                    activeTheme = activeTheme,
                    onClick = { onSelectTab(GalleryTab.ALBUMS) },
                    modifier = Modifier.weight(1f)
                )
                FloatingDockTabItem(
                    tab = GalleryTab.STUDIO,
                    selected = currentTab == GalleryTab.STUDIO,
                    icon = Icons.Default.AutoAwesome,
                    activeTheme = activeTheme,
                    onClick = { onSelectTab(GalleryTab.STUDIO) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SelectedPhotosBottomActionDock(
    selectedCount: Int,
    activeTheme: CompleteUiThemeSpec,
    onFavoriteSelected: () -> Unit,
    onAiTagSelected: () -> Unit,
    onCollageSelected: () -> Unit,
    onCompareSelected: () -> Unit,
    onShrinkSelected: () -> Unit,
    onWatermarkSelected: () -> Unit,
    onRotateSelected: () -> Unit,
    onDuplicateSelected: () -> Unit,
    onInspectExifSelected: () -> Unit,
    onMoveAlbumSelected: () -> Unit,
    onShareSelected: () -> Unit,
    onVaultSelected: () -> Unit,
    onTrashSelected: () -> Unit,
    onDeleteSelectedPermanently: () -> Unit = onTrashSelected,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goldAccent = Color(0xFFF5B700)
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF141416),
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(26.dp))
            .testTag("selected_photos_bottom_dock")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Selected Count Badge + Quick Delete & Vault Pills + Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = goldAccent,
                        modifier = Modifier.animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        AnimatedContent(
                            targetState = selectedCount,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "selected_count_badge_anim"
                        ) { count ->
                            Text(
                                text = "$count SELECTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE11D48),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onDeleteSelectedPermanently() }
                            .testTag("selected_dock_header_delete_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete selected photos from local storage",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.10f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onVaultSelected() }
                            .testTag("selected_dock_vault")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Move to Vault",
                                tint = goldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Vault",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onClearSelection() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Row 1: Favorite | Collage | Shrink | Rotate | Duplicate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectedDockActionItem(
                    icon = Icons.Default.Favorite,
                    label = "Favorite",
                    tint = Color(0xFFFF4D6D),
                    onClick = onFavoriteSelected,
                    testTag = "selected_dock_favorite",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.AutoAwesomeMosaic,
                    label = "Collage",
                    tint = goldAccent,
                    onClick = onCollageSelected,
                    testTag = "selected_dock_collage",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.Compress,
                    label = "Shrink",
                    tint = Color(0xFF10B981),
                    onClick = onShrinkSelected,
                    testTag = "selected_dock_shrink",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.RotateRight,
                    label = "Rotate",
                    tint = Color.White,
                    onClick = onRotateSelected,
                    testTag = "selected_dock_rotate",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Duplicate",
                    tint = Color.White,
                    onClick = onDuplicateSelected,
                    testTag = "selected_dock_duplicate",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Album | Share | Trash | Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectedDockActionItem(
                    icon = Icons.Default.DriveFileMove,
                    label = "Album",
                    tint = Color.White,
                    onClick = onMoveAlbumSelected,
                    testTag = "selected_dock_album",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    tint = Color.White,
                    onClick = onShareSelected,
                    testTag = "selected_dock_share",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.DeleteOutline,
                    label = "Trash",
                    tint = Color(0xFFFB7185),
                    onClick = onTrashSelected,
                    testTag = "selected_dock_trash",
                    modifier = Modifier.weight(1f)
                )
                SelectedDockActionItem(
                    icon = Icons.Default.DeleteForever,
                    label = "Delete",
                    tint = Color(0xFFE11D48),
                    onClick = onDeleteSelectedPermanently,
                    testTag = "selected_dock_delete_local",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SelectedDockActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 5.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp
            ),
            color = Color(0xFFE4E4E7),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FloatingDockTabItem(
    tab: GalleryTab,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeTheme: CompleteUiThemeSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dock_tab_scale"
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) activeTheme.primaryAccent else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_tab_pill_color"
    )
    val contentTint by animateColorAsState(
        targetValue = if (selected) activeTheme.onPrimaryAccent else activeTheme.textSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_tab_content_tint"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = pillColor,
        modifier = modifier
            .height(48.dp)
            .scale(itemScale)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("nav_tab_${tab.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tab.label,
                tint = contentTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                ),
                color = contentTint,
                maxLines = 1
            )
        }
    }
}

private fun shareLocalPhoto(
    context: Context,
    photo: PhotoEntity,
    onMessage: (String) -> Unit
) {
    try {
        val uri = when {
            photo.filePath.startsWith("content://") -> android.net.Uri.parse(photo.filePath)
            photo.filePath.startsWith("android.resource://") -> {
                onMessage("Cannot share bundled resource URI directly")
                return
            }
            else -> {
                val rawPath = if (photo.filePath.startsWith("file://")) {
                    android.net.Uri.parse(photo.filePath).path ?: photo.filePath
                } else {
                    photo.filePath
                }
                val file = File(rawPath)
                if (!file.exists()) {
                    onMessage("File not found on local storage")
                    return
                }
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (_: IllegalArgumentException) {
                    val shareCacheFile = File(context.cacheDir, "share_${file.name}")
                    file.copyTo(shareCacheFile, overwrite = true)
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        shareCacheFile
                    )
                }
            }
        }
        val resolvedMimeType = photo.mimeType
            .takeIf { it.contains("/") && it.isNotBlank() }
            ?: if (photo.isVideo) "video/mp4" else "image/jpeg"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = resolvedMimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, photo.title)
            putExtra(Intent.EXTRA_TEXT, "${photo.title} (${photo.exifSummary})")
            clipData = android.content.ClipData.newRawUri(photo.title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Photo").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(chooser)
    } catch (_: Exception) {
        onMessage("Unable to launch share sheet on this device")
    }
}

private fun shareStrippedFile(
    context: Context,
    file: File,
    title: String,
    onMessage: (String) -> Unit
) {
    try {
        if (!file.exists()) {
            onMessage("Cleaned share file not found")
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title (Privacy Share • EXIF & Location Removed)")
            clipData = android.content.ClipData.newRawUri(title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Privacy-Cleaned Photo").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(chooser)
    } catch (_: Exception) {
        onMessage("Unable to launch share sheet on this device")
    }
}
