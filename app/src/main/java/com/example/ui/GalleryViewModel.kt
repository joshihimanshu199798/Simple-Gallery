package com.example.ui

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumSortOption
import com.example.data.AlbumSummary
import com.example.data.CropAspectRatio
import com.example.data.GalleryDatabase
import com.example.data.GalleryRepository
import com.example.data.GridDensity
import com.example.data.MemoryEntryEntity
import com.example.data.MilestoneReminderEntity
import com.example.data.PersonClusterEntity
import com.example.data.PhotoEntity
import com.example.data.PhotoFaceTagEntity
import com.example.data.PhotoFilterPreset
import com.example.data.PhotoImageProcessor
import com.example.data.SortOption
import com.example.service.AiOrganizerStatusBus
import com.example.service.AiOrganizerUiState
import com.example.service.AiPhotoAnalysisResult
import com.example.service.AiPhotoOrganizerService
import com.example.ui.theme.AppThemeCatalog
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.GalleryLayoutStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GalleryTab(val label: String) {
    PICS("Photos"),
    ALBUMS("Albums"),
    STUDIO("Studio")
}

data class GalleryFilterState(
    val searchQuery: String = "",
    val selectedTag: String = "All",
    val selectedAlbumName: String? = null,
    val sortOption: SortOption = SortOption.DATE_DESC,
    val gridDensity: GridDensity = GridDensity.TWO,
    val isSquareGrid: Boolean = false
)

class GalleryViewModel(
    private val repository: GalleryRepository,
    context: Context
) : ViewModel() {

    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("simple_gallery_prefs", Context.MODE_PRIVATE)

    val aiOrganizerUiState: StateFlow<AiOrganizerUiState> = AiOrganizerStatusBus.uiState
    val aiRecentResults: StateFlow<List<AiPhotoAnalysisResult>> = AiOrganizerStatusBus.recentResults

    private val _aiMoveIntoCategoryAlbums = MutableStateFlow(
        prefs.getBoolean("ai_move_into_albums", true)
    )
    val aiMoveIntoCategoryAlbums: StateFlow<Boolean> = _aiMoveIntoCategoryAlbums.asStateFlow()

    private val _aiAutoTagOnImport = MutableStateFlow(
        prefs.getBoolean("ai_auto_tag_on_import", true)
    )
    val aiAutoTagOnImport: StateFlow<Boolean> = _aiAutoTagOnImport.asStateFlow()

    private val _currentTab = MutableStateFlow(GalleryTab.PICS)
    val currentTab: StateFlow<GalleryTab> = _currentTab.asStateFlow()

    private val _baseTheme = MutableStateFlow(
        AppThemeCatalog.findById(
            prefs.getString("active_theme_id_v3", "warm_editorial_light") ?: "warm_editorial_light"
        )
    )

    private val _layoutStyleOverride = MutableStateFlow<GalleryLayoutStyle?>(null)
    val layoutStyleOverride: StateFlow<GalleryLayoutStyle?> = _layoutStyleOverride.asStateFlow()

    val activeTheme: StateFlow<CompleteUiThemeSpec> = combine(
        _baseTheme,
        _layoutStyleOverride
    ) { theme, layoutOverride ->
        if (layoutOverride != null) {
            theme.copy(layoutStyle = layoutOverride)
        } else {
            theme
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        _baseTheme.value
    )

    // 0 = All (11), 1 = Light (5), 2 = Dark (6)
    private val _themeFilterCategory = MutableStateFlow(0)
    val themeFilterCategory: StateFlow<Int> = _themeFilterCategory.asStateFlow()

    private val _isThemeSwitcherExpanded = MutableStateFlow(false)
    val isThemeSwitcherExpanded: StateFlow<Boolean> = _isThemeSwitcherExpanded.asStateFlow()

    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()

    private val _isCompareModalOpen = MutableStateFlow(false)
    val isCompareModalOpen: StateFlow<Boolean> = _isCompareModalOpen.asStateFlow()

    private val _isCollageModalOpen = MutableStateFlow(false)
    val isCollageModalOpen: StateFlow<Boolean> = _isCollageModalOpen.asStateFlow()

    private val _isWatermarkModalOpen = MutableStateFlow(false)
    val isWatermarkModalOpen: StateFlow<Boolean> = _isWatermarkModalOpen.asStateFlow()

    private val _filterState = MutableStateFlow(GalleryFilterState())
    val filterState: StateFlow<GalleryFilterState> = _filterState.asStateFlow()

    private val _selectedPhotoIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotoIds: StateFlow<Set<Long>> = _selectedPhotoIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _activeLightboxPhotoId = MutableStateFlow<Long?>(null)
    val activeLightboxPhotoId: StateFlow<Long?> = _activeLightboxPhotoId.asStateFlow()

    private val _inspectedExifPhotoId = MutableStateFlow<Long?>(null)
    val inspectedExifPhotoId: StateFlow<Long?> = _inspectedExifPhotoId.asStateFlow()

    private val _editingPhoto = MutableStateFlow<PhotoEntity?>(null)
    val editingPhoto: StateFlow<PhotoEntity?> = _editingPhoto.asStateFlow()

    private val _editorInitialTab = MutableStateFlow(0)
    val editorInitialTab: StateFlow<Int> = _editorInitialTab.asStateFlow()

    private val _photoForMemoryComposer = MutableStateFlow<PhotoEntity?>(null)
    val photoForMemoryComposer: StateFlow<PhotoEntity?> = _photoForMemoryComposer.asStateFlow()

    private val _isSlideshowPlaying = MutableStateFlow(false)
    val isSlideshowPlaying: StateFlow<Boolean> = _isSlideshowPlaying.asStateFlow()

    private val _slideshowIntervalSec = MutableStateFlow(3)
    val slideshowIntervalSec: StateFlow<Int> = _slideshowIntervalSec.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultPin = MutableStateFlow(prefs.getString("vault_pin", "1234") ?: "1234")
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    private val _vaultBiometricEnabled = MutableStateFlow(
        prefs.getBoolean("vault_biometric_enabled", true)
    )
    val vaultBiometricEnabled: StateFlow<Boolean> = _vaultBiometricEnabled.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _activeHistogram = MutableStateFlow<List<Float>>(emptyList())
    val activeHistogram: StateFlow<List<Float>> = _activeHistogram.asStateFlow()

    val personClusters: StateFlow<List<PersonClusterEntity>> = repository.visiblePersonsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photoFaceTags: StateFlow<List<PhotoFaceTagEntity>> = repository.faceTagsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisiblePhotos: StateFlow<List<PhotoEntity>> = repository.visiblePhotosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePhotos: StateFlow<List<PhotoEntity>> = repository.favoritePhotosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultPhotos: StateFlow<List<PhotoEntity>> = repository.vaultPhotosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedPhotos: StateFlow<List<PhotoEntity>> = repository.trashedPhotosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _albumSortOption = MutableStateFlow(AlbumSortOption.DATE_DESC)
    val albumSortOption: StateFlow<AlbumSortOption> = _albumSortOption.asStateFlow()

    val albums: StateFlow<List<AlbumSummary>> = combine(
        repository.albumsSummaryFlow,
        _albumSortOption
    ) { albumList, sort ->
        when (sort) {
            AlbumSortOption.DATE_DESC -> albumList.sortedByDescending { it.latestTimestamp }
            AlbumSortOption.DATE_ASC -> albumList.sortedBy { it.latestTimestamp }
            AlbumSortOption.NAME_ASC -> albumList.sortedBy { it.name.lowercase() }
            AlbumSortOption.NAME_DESC -> albumList.sortedByDescending { it.name.lowercase() }
            AlbumSortOption.SIZE_DESC -> albumList.sortedByDescending { it.totalBytes }
            AlbumSortOption.SIZE_ASC -> albumList.sortedBy { it.totalBytes }
            AlbumSortOption.COUNT_DESC -> albumList.sortedByDescending { it.photoCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateAlbumSortOption(option: AlbumSortOption) {
        _albumSortOption.value = option
    }

    val memories: StateFlow<List<MemoryEntryEntity>> = repository.memoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<MilestoneReminderEntity>> = repository.milestonesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableTags: StateFlow<List<String>> = combine(allVisiblePhotos) { (photos) ->
        val extracted = photos.flatMap { it.tagList }.distinct().sorted()
        listOf("All", "Favorites", "Edited") + extracted.filter {
            it != "Edited" && it != "Favorites"
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf("All", "Favorites", "Edited")
    )

    val filteredPhotos: StateFlow<List<PhotoEntity>> = combine(
        allVisiblePhotos,
        _filterState
    ) { photos, filter ->
        val query = filter.searchQuery.trim().lowercase()
        val distinctPhotos = photos.distinctBy { photo ->
            val fileName = photo.filePath.substringAfterLast('/')
            "$fileName:${photo.sizeBytes}"
        }
        val filtered = distinctPhotos.filter { photo ->
            val matchesAlbum = filter.selectedAlbumName == null ||
                photo.albumName.equals(filter.selectedAlbumName, ignoreCase = true)

            val matchesTag = photo.matchesCategory(filter.selectedTag)

            val matchesSearch = query.isEmpty() ||
                photo.title.lowercase().contains(query) ||
                photo.fileName.lowercase().contains(query) ||
                photo.dateHeader.lowercase().contains(query) ||
                photo.shortDateText.lowercase().contains(query) ||
                photo.isoDateText.lowercase().contains(query) ||
                photo.fullTimestampText.lowercase().contains(query) ||
                photo.relativeDateGroup().lowercase().contains(query) ||
                photo.albumName.lowercase().contains(query) ||
                photo.tags.lowercase().contains(query) ||
                photo.locationTag.lowercase().contains(query) ||
                photo.cameraModel.lowercase().contains(query) ||
                photo.notes.lowercase().contains(query)

            matchesAlbum && matchesTag && matchesSearch
        }

        when (filter.sortOption) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateTaken }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateTaken }
            SortOption.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> filtered.sortedBy { it.sizeBytes }
            SortOption.RESOLUTION_DESC -> filtered.sortedByDescending { it.width.toLong() * it.height.toLong() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val mediaStoreObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            syncDeviceGalleryAutomatically(silent = true)
        }
    }

    init {
        try {
            appContext.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                mediaStoreObserver
            )
        } catch (_: Exception) {
        }
        viewModelScope.launch {
            repository.ensureSeeded()
            repository.syncDeviceMediaStorePhotos()
        }
    }

    private val _isSyncingDeviceGallery = MutableStateFlow(false)
    val isSyncingDeviceGallery: StateFlow<Boolean> = _isSyncingDeviceGallery.asStateFlow()
    private var lastSyncTimestamp = 0L

    override fun onCleared() {
        super.onCleared()
        try {
            appContext.contentResolver.unregisterContentObserver(mediaStoreObserver)
        } catch (_: Exception) {
        }
    }

    fun syncDeviceGalleryAutomatically(silent: Boolean = true) {
        val now = System.currentTimeMillis()
        if (silent && allVisiblePhotos.value.isNotEmpty() && (now - lastSyncTimestamp < 2500L)) {
            // Debounce rapid repeated silent scans only when photos are already loaded
            return
        }
        lastSyncTimestamp = now

        viewModelScope.launch {
            if (!silent) {
                _isSyncingDeviceGallery.value = true
                _filterState.update {
                    it.copy(
                        selectedTag = "All",
                        selectedAlbumName = null,
                        searchQuery = ""
                    )
                }
            }
            try {
                val startMs = System.currentTimeMillis()
                val added = repository.syncDeviceMediaStorePhotos(forceRescan = !silent)
                if (!silent) {
                    val elapsedMs = System.currentTimeMillis() - startMs
                    if (elapsedMs < 450L) {
                        delay(450L - elapsedMs)
                    }
                    if (added > 0) {
                        postMessage("Synced $added personal photo${if (added > 1) "s" else ""} from device storage")
                    } else {
                        val currentTotal = allVisiblePhotos.value.size
                        if (currentTotal > 0) {
                            postMessage("Gallery is up to date ($currentTotal photos loaded)")
                        } else {
                            postMessage("No photos found in MediaStore — allow full photo access or select photos from device")
                        }
                    }
                }
            } finally {
                if (!silent) {
                    _isSyncingDeviceGallery.value = false
                }
            }
        }
    }

    fun selectTab(tab: GalleryTab) {
        _currentTab.value = tab
        clearSelection()
        if (tab != GalleryTab.ALBUMS) {
            _filterState.update { it.copy(selectedAlbumName = null) }
        }
    }

    fun selectCompleteTheme(theme: CompleteUiThemeSpec) {
        _baseTheme.value = theme
        _layoutStyleOverride.value = null
        prefs.edit().putString("active_theme_id_v3", theme.id).apply()
        postMessage("Applied UI Theme: ${theme.name}")
    }

    fun toggleLightDarkThemeQuick() {
        val current = _baseTheme.value
        val next = if (current.isLight) {
            AppThemeCatalog.darkThemes.first()
        } else {
            AppThemeCatalog.lightThemes.first()
        }
        selectCompleteTheme(next)
    }

    fun cycleLayoutStyleOverride() {
        val styles = GalleryLayoutStyle.entries
        val currentStyle = activeTheme.value.layoutStyle
        val nextIdx = (styles.indexOf(currentStyle) + 1) % styles.size
        val nextStyle = styles[nextIdx]
        _layoutStyleOverride.value = nextStyle
        postMessage("Switched Gallery Layout: ${nextStyle.displayName}")
    }

    fun setLayoutStyleOverride(style: GalleryLayoutStyle) {
        _layoutStyleOverride.value = style
        postMessage("Layout Style: ${style.displayName}")
    }

    fun setGridDensity(density: GridDensity) {
        _filterState.update { it.copy(gridDensity = density) }
        postMessage("Photo Grid: ${density.label}")
    }

    fun setSquareGridMode(isSquare: Boolean) {
        _filterState.update { it.copy(isSquareGrid = isSquare) }
        postMessage(if (isSquare) "Grid Ratio: 1:1 Square" else "Grid Ratio: Adaptive")
    }

    /**
     * Pinch-out (zoomIn = true) -> fewer columns / larger cards (4 -> 3 -> 2 -> 1)
     * Pinch-in (zoomIn = false) -> more columns / compact grid (1 -> 2 -> 3 -> 4)
     */
    fun pinchAdjustGridStyle(zoomIn: Boolean) {
        val orderedDensities = listOf(
            GridDensity.LIST,   // 1 col
            GridDensity.TWO,    // 2 cols
            GridDensity.THREE,  // 3 cols
            GridDensity.FOUR    // 4 cols
        )
        val currentDensity = _filterState.value.gridDensity
        val currentIdx = orderedDensities.indexOf(currentDensity).coerceAtLeast(1)
        if (zoomIn) {
            // Pinching outward (magnifying cards -> fewer columns)
            if (currentIdx > 0) {
                val nextDensity = orderedDensities[currentIdx - 1]
                _filterState.update { it.copy(gridDensity = nextDensity) }
                postMessage("Photo Grid: ${nextDensity.label}")
            }
        } else {
            // Pinching inward (shrinking cards -> more columns)
            if (currentIdx < orderedDensities.lastIndex) {
                val nextDensity = orderedDensities[currentIdx + 1]
                _filterState.update { it.copy(gridDensity = nextDensity) }
                postMessage("Photo Grid: ${nextDensity.label}")
            }
        }
    }

    fun setThemeFilterCategory(categoryIndex: Int) {
        _themeFilterCategory.value = categoryIndex.coerceIn(0, 2)
    }

    fun toggleThemeSwitcherExpanded() {
        _isThemeSwitcherExpanded.update { !it }
    }

    fun toggleSearchExpanded() {
        _isSearchExpanded.update { !it }
    }

    fun openCompareModal() {
        _isCompareModalOpen.value = true
    }

    fun closeCompareModal() {
        _isCompareModalOpen.value = false
    }

    fun openCollageModal() {
        _isCollageModalOpen.value = true
    }

    fun closeCollageModal() {
        _isCollageModalOpen.value = false
    }

    fun openWatermarkModal() {
        _isWatermarkModalOpen.value = true
    }

    fun closeWatermarkModal() {
        _isWatermarkModalOpen.value = false
    }

    fun createBentoCollage(
        photoPaths: List<String>,
        collageTitle: String,
        layoutMode: Int,
        backgroundColorHex: String
    ) {
        if (photoPaths.size < 2) {
            postMessage("Select at least 2 photos to create a Bento Collage")
            return
        }
        viewModelScope.launch {
            val saved = repository.createBentoCollage(
                photoPaths = photoPaths,
                collageTitle = collageTitle,
                layoutMode = layoutMode,
                backgroundColorHex = backgroundColorHex
            )
            _isCollageModalOpen.value = false
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Created '${saved.title}' in 'Collage Studio' album!")
            } else {
                postMessage("Unable to generate Bento Collage")
            }
        }
    }

    fun createWatermarkedPhoto(
        photo: PhotoEntity,
        watermarkText: String,
        includeDateAndExif: Boolean
    ) {
        viewModelScope.launch {
            val saved = repository.createWatermarkedPhoto(
                original = photo,
                watermarkText = watermarkText,
                includeDateAndExif = includeDateAndExif
            )
            _isWatermarkModalOpen.value = false
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Saved stamped print '${saved.title}' to 'Edited Studio'!")
            } else {
                postMessage("Unable to stamp watermark onto photo")
            }
        }
    }

    fun shrinkAndOptimizePhoto(
        photo: PhotoEntity,
        scaleFactor: Float,
        jpegQuality: Int,
        replaceOriginal: Boolean
    ) {
        viewModelScope.launch {
            val saved = repository.shrinkPhoto(
                original = photo,
                scaleFactor = scaleFactor,
                jpegQuality = jpegQuality,
                replaceOriginal = replaceOriginal
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Shrunk '${saved.title}' → ${saved.formattedSize} (${saved.resolutionText})")
            } else {
                postMessage("Unable to optimize photo size")
            }
        }
    }

    fun applyAdvancedStudioEdits(
        original: PhotoEntity,
        cropRatio: CropAspectRatio,
        customCropRect: android.graphics.RectF?,
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
    ) {
        viewModelScope.launch {
            val saved = repository.applyAdvancedStudioEdits(
                original = original,
                cropRatio = cropRatio,
                customCropRect = customCropRect,
                rotationDegrees = rotationDegrees,
                flipH = flipH,
                flipV = flipV,
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
                filterMatrix = filterMatrix,
                filterIntensity = filterIntensity,
                skinSmoothing = skinSmoothing,
                saveAsCopy = saveAsCopy
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Exported master print '${saved.title}' (${saved.formattedSize})")
            } else {
                postMessage("Unable to export edited photo")
            }
        }
    }

    fun compressPhotosBatch(
        targets: List<PhotoEntity>,
        format: String,
        quality: Int,
        scalePercent: Int,
        preserveExif: Boolean
    ) {
        viewModelScope.launch {
            val results = repository.compressPhotoBatch(targets, format, quality, scalePercent, preserveExif)
            if (results.isNotEmpty()) {
                val totalSavedBytes = targets.sumOf { it.sizeBytes } - results.sumOf { it.sizeBytes }
                postMessage("Compressed ${results.size} photo(s) into 'Compressed' album! Saved ${com.example.data.formatBytes(totalSavedBytes.coerceAtLeast(0L))}")
            } else {
                postMessage("Unable to compress photos")
            }
        }
    }

    fun exportAiCutout(photo: PhotoEntity) {
        viewModelScope.launch {
            val saved = repository.exportAiCutout(photo)
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Created transparent PNG cutout '${saved.title}'")
            } else {
                postMessage("Unable to generate subject cutout")
            }
        }
    }

    fun exportAiInpaint(photo: PhotoEntity, maskRects: List<RectF> = listOf(RectF(0.35f, 0.35f, 0.65f, 0.65f))) {
        viewModelScope.launch {
            val saved = repository.exportAiInpaint(photo, maskRects)
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Removed object! Saved clean photo to 'AI Object Removal' album")
            } else {
                postMessage("Unable to remove object")
            }
        }
    }

    fun exportAiBgReplace(photo: PhotoEntity, bgStyle: String = "Studio Gradient") {
        viewModelScope.launch {
            val saved = repository.exportAiBgReplace(photo, bgStyle)
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Replaced background! Saved to 'AI Backgrounds' album")
            } else {
                postMessage("Unable to replace background")
            }
        }
    }

    fun exportAiRestoration(photo: PhotoEntity) {
        viewModelScope.launch {
            val saved = repository.exportAiRestoration(photo)
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Restored vintage photo! Saved to 'Restored Photos' album")
            } else {
                postMessage("Unable to restore photo")
            }
        }
    }

    fun exportAiSuperRes(photo: PhotoEntity) {
        viewModelScope.launch {
            val saved = repository.exportAiSuperRes(photo)
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Upscaled 2x with crisp neural detail! Saved to 'AI Upscaled' album")
            } else {
                postMessage("Unable to upscale photo")
            }
        }
    }

    fun createAdvancedCollage(
        photoPaths: List<String>,
        layoutMode: com.example.ui.studio.CollageLayoutMode,
        aspectRatio: CropAspectRatio,
        spacingDp: Float,
        cornerRadiusDp: Float,
        backgroundColorHex: String
    ) {
        if (photoPaths.size < 2) {
            postMessage("Select at least 2 photos to create a collage")
            return
        }
        viewModelScope.launch {
            val saved = repository.createAdvancedCollage(
                photoPaths = photoPaths,
                layoutMode = layoutMode,
                aspectRatio = aspectRatio,
                spacingDp = spacingDp,
                cornerRadiusDp = cornerRadiusDp,
                backgroundColorHex = backgroundColorHex
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Saved high-res collage to 'Collages' album!")
            } else {
                postMessage("Unable to generate collage")
            }
        }
    }

    fun exportRawDevelopedPhoto(
        photo: PhotoEntity,
        kelvinTemp: Int,
        tint: Float,
        exposureEv: Float,
        highlightRecovery: Float,
        shadowRecovery: Float,
        chromaDenoise: Float
    ) {
        viewModelScope.launch {
            val saved = repository.exportRawDevelopedPhoto(
                photo = photo,
                kelvinTemp = kelvinTemp,
                tint = tint,
                exposureEv = exposureEv,
                highlightRecovery = highlightRecovery,
                shadowRecovery = shadowRecovery,
                chromaDenoise = chromaDenoise
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                postMessage("Developed master photograph to 'RAW Develop' album!")
            } else {
                postMessage("Unable to process and develop RAW photo")
            }
        }
    }

    fun executeBatchStudioAction(
        targets: List<PhotoEntity>,
        mode: com.example.ui.studio.BatchActionMode,
        presetId: String?,
        cropRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
        filterIntensity: Float = 1f,
        rotationDegrees: Int = 0
    ) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            val count = repository.executeBatchStudioAction(
                targets = targets,
                mode = mode,
                presetId = presetId,
                cropRatio = cropRatio,
                filterIntensity = filterIntensity,
                rotationDegrees = rotationDegrees
            )
            postMessage("Batch processed $count photo(s) into 'Batch Edits' album!")
        }
    }

    fun batchShrinkPhotos(
        ids: List<Long>,
        scaleFactor: Float = 0.75f,
        jpegQuality: Int = 80
    ) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val count = repository.batchShrinkPhotos(ids, scaleFactor, jpegQuality)
            clearSelection()
            postMessage("Optimized $count photo(s) into 'Optimized Archive' album!")
        }
    }

    fun quickRotatePhoto90(photo: PhotoEntity) {
        viewModelScope.launch {
            val saved = repository.applyStudioEdits(
                original = photo,
                preset = PhotoFilterPreset.ORIGINAL,
                brightness = 0f,
                contrast = 1f,
                saturation = 1f,
                rotationDegrees = 90,
                flipHorizontal = false,
                flipVertical = false,
                cropRatio = CropAspectRatio.ORIGINAL,
                saveAsCopy = false
            )
            if (saved != null) {
                loadHistogramForPhoto(saved)
                postMessage("Rotated '${saved.title}' 90° clockwise")
            }
        }
    }

    fun duplicatePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            val copy = repository.duplicatePhoto(photo)
            if (copy != null) {
                postMessage("Duplicated '${copy.title}' in '${copy.albumName}'")
            }
        }
    }

    fun startAiAnalyzePhotosByIds(ids: List<Long>) {
        if (ids.isEmpty()) return
        AiPhotoOrganizerService.startOrganizePhotosByIds(
            context = appContext,
            photoIds = ids,
            moveIntoCategoryAlbums = _aiMoveIntoCategoryAlbums.value
        )
        clearSelection()
        postMessage("Analyzing ${ids.size} selected photo(s) with Gemini AI...")
    }

    fun createFramedPhoto(
        photo: PhotoEntity,
        frameStyleName: String,
        showExifSignature: Boolean,
        customBadgeText: String = ""
    ) {
        viewModelScope.launch {
            val saved = repository.createFramedPhoto(
                original = photo,
                frameStyleName = frameStyleName,
                showExifSignature = showExifSignature,
                customBadgeText = customBadgeText
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Saved framed gallery print '${saved.title}'!")
            } else {
                postMessage("Unable to render framed print")
            }
        }
    }

    fun renamePerson(personId: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePerson(personId, newName)
            val label = newName.trim().ifEmpty { "Unnamed" }
            postMessage("Updated profile name to '$label'")
        }
    }

    fun hidePerson(personId: Long) {
        viewModelScope.launch {
            repository.hidePerson(personId)
            postMessage("Hid person cluster from People & Pets")
        }
    }

    fun mergePersons(sourcePersonId: Long, targetPersonId: Long, targetName: String) {
        viewModelScope.launch {
            repository.mergePersons(sourcePersonId, targetPersonId)
            postMessage("Merged face cluster into '$targetName'")
        }
    }

    fun setupGrowthTimeline(personId: Long, childName: String, dobMs: Long) {
        viewModelScope.launch {
            repository.setupGrowthTimeline(personId, childName, dobMs)
            postMessage("Activated Growth Timeline for '${childName.trim().ifEmpty { "Little One" }}'")
        }
    }

    fun togglePhotoMilestoneSticker(photo: PhotoEntity, sticker: String) {
        viewModelScope.launch {
            val next = if (photo.milestoneSticker == sticker) "" else sticker
            repository.updatePhotoMilestoneSticker(photo.id, next)
            postMessage(
                if (next.isNotEmpty()) "Added '$next' milestone sticker"
                else "Removed milestone sticker"
            )
        }
    }

    fun prepareExifStrippedShare(photo: PhotoEntity, onFileReady: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            val file = repository.createExifStrippedShareFile(photo)
            onFileReady(file)
        }
    }

    fun applyQuickLutPreset(
        photo: PhotoEntity,
        preset: PhotoFilterPreset,
        saveAsCopy: Boolean = false
    ) {
        viewModelScope.launch {
            val saved = repository.applyStudioEdits(
                original = photo,
                preset = preset,
                brightness = 0f,
                contrast = 1f,
                saturation = 1f,
                rotationDegrees = 0,
                flipHorizontal = false,
                flipVertical = false,
                cropRatio = CropAspectRatio.ORIGINAL,
                saveAsCopy = saveAsCopy
            )
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage("Applied '${preset.displayName}' LUT grade to '${saved.title}'")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectTagFilter(tag: String) {
        _filterState.update { current ->
            val nextTag = if (current.selectedTag.equals(tag, ignoreCase = true) &&
                !tag.equals("All", ignoreCase = true)
            ) {
                "All"
            } else {
                tag
            }
            current.copy(selectedTag = nextTag)
        }
    }

    fun openAlbumDetail(albumName: String?) {
        _filterState.update { it.copy(selectedAlbumName = albumName) }
        clearSelection()
    }

    fun updateSortOption(option: SortOption) {
        _filterState.update { it.copy(sortOption = option) }
    }

    fun cycleGridDensity() {
        val values = GridDensity.entries
        val nextIndex = (values.indexOf(_filterState.value.gridDensity) + 1) % values.size
        _filterState.update { it.copy(gridDensity = values[nextIndex]) }
    }

    fun toggleSelectionMode() {
        val next = !_isSelectionMode.value
        _isSelectionMode.value = next
        if (!next) {
            _selectedPhotoIds.value = emptySet()
        } else {
            postMessage("Multi-Select Mode active: tap photos to select")
        }
    }

    fun togglePhotoSelection(photoId: Long) {
        _isSelectionMode.value = true
        _selectedPhotoIds.update { current ->
            if (current.contains(photoId)) current - photoId else current + photoId
        }
    }

    fun selectAllCurrentPhotos(photos: List<PhotoEntity>) {
        _isSelectionMode.value = true
        _selectedPhotoIds.value = photos.map { it.id }.toSet()
        postMessage("Selected all ${photos.size} photos")
    }

    fun selectFavoritePhotos(photos: List<PhotoEntity>) {
        _isSelectionMode.value = true
        val favIds = photos.filter { it.isFavorite }.map { it.id }.toSet()
        _selectedPhotoIds.value = favIds
        postMessage("Selected ${favIds.size} favorite photos")
    }

    fun invertPhotoSelection(photos: List<PhotoEntity>) {
        _isSelectionMode.value = true
        val current = _selectedPhotoIds.value
        val inverted = photos.map { it.id }.filter { it !in current }.toSet()
        _selectedPhotoIds.value = inverted
        postMessage("Inverted selection (${inverted.size} selected)")
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedPhotoIds.value = emptySet()
    }

    fun openLightbox(photo: PhotoEntity, startSlideshow: Boolean = false) {
        _activeLightboxPhotoId.value = photo.id
        _isSlideshowPlaying.value = startSlideshow
        loadHistogramForPhoto(photo)
    }

    fun closeLightbox() {
        _activeLightboxPhotoId.value = null
        _isSlideshowPlaying.value = false
    }

    fun openExifSheetForPhoto(photo: PhotoEntity) {
        _editingPhoto.value = null
        _inspectedExifPhotoId.value = photo.id
        loadHistogramForPhoto(photo)
    }

    fun closeExifSheet() {
        _inspectedExifPhotoId.value = null
    }

    fun toggleSlideshow() {
        _isSlideshowPlaying.update { !it }
    }

    fun cycleSlideshowInterval() {
        val next = when (_slideshowIntervalSec.value) {
            2 -> 4
            4 -> 6
            else -> 2
        }
        _slideshowIntervalSec.value = next
    }

    fun loadHistogramForPhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            _activeHistogram.value = PhotoImageProcessor.computeLuminanceHistogram(
                filePath = photo.filePath,
                context = appContext
            )
        }
    }

    fun openPhotoEditor(photo: PhotoEntity, initialTab: Int = 0) {
        _inspectedExifPhotoId.value = null
        _editorInitialTab.value = initialTab.coerceIn(0, 2)
        _editingPhoto.value = photo
        _isSlideshowPlaying.value = false
        loadHistogramForPhoto(photo)
    }

    fun openPhotoCropStudio(photo: PhotoEntity) {
        openPhotoEditor(photo, initialTab = 2)
    }

    fun closePhotoEditor() {
        _editingPhoto.value = null
    }

    fun openMemoryComposerWithPhoto(photo: PhotoEntity) {
        _photoForMemoryComposer.value = photo
        _activeLightboxPhotoId.value = null
        _currentTab.value = GalleryTab.STUDIO
    }

    fun consumePhotoForMemoryComposer() {
        _photoForMemoryComposer.value = null
    }

    fun saveLittleMemory(memory: MemoryEntryEntity) {
        viewModelScope.launch {
            repository.saveMemoryEntry(memory)
            postMessage(
                if (memory.isTimeCapsule) "Sealed '${memory.title}' in a Time Capsule!"
                else "Saved '${memory.title}' to Keepsakes journal"
            )
        }
    }

    fun deleteLittleMemory(memoryId: Long) {
        viewModelScope.launch {
            repository.deleteMemoryEntry(memoryId)
            postMessage("Removed memory entry from journal")
        }
    }

    fun toggleMemoryPinned(memory: MemoryEntryEntity) {
        viewModelScope.launch {
            repository.toggleMemoryPinned(memory)
            postMessage(
                if (!memory.isPinnedHighlight) "Pinned to Curated Highlights"
                else "Unpinned from Highlights"
            )
        }
    }

    fun unlockTimeCapsule(memory: MemoryEntryEntity) {
        viewModelScope.launch {
            repository.unlockTimeCapsule(memory.id)
            postMessage("Unlocked Time Capsule: '${memory.title}'!")
        }
    }

    fun exportPolaroidKeepsake(memory: MemoryEntryEntity) {
        viewModelScope.launch {
            val savedPhoto = repository.exportMemoryAsPolaroidToGallery(memory)
            if (savedPhoto != null) {
                postMessage("Saved printed Polaroid card to 'Keepsakes' album!")
            } else {
                postMessage("Unable to generate Polaroid keepsake card")
            }
        }
    }

    fun saveMilestoneReminder(milestone: MilestoneReminderEntity) {
        viewModelScope.launch {
            repository.saveMilestoneReminder(milestone)
            postMessage("Saved milestone '${milestone.title}'")
        }
    }

    fun toggleMilestoneCompleted(milestone: MilestoneReminderEntity) {
        viewModelScope.launch {
            repository.toggleMilestoneCompleted(milestone)
            postMessage(
                if (!milestone.isCompleted) "Marked '${milestone.title}' as completed! 🎉"
                else "Reopened milestone '${milestone.title}'"
            )
        }
    }

    fun deleteMilestone(id: Long) {
        viewModelScope.launch {
            repository.deleteMilestone(id)
            postMessage("Deleted milestone entry")
        }
    }

    fun applyPhotoEdits(
        original: PhotoEntity,
        preset: PhotoFilterPreset,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        rotationDegrees: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRatio: CropAspectRatio,
        saveAsCopy: Boolean,
        customCropBounds: android.graphics.RectF? = null
    ) {
        viewModelScope.launch {
            val saved = repository.applyStudioEdits(
                original = original,
                preset = preset,
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                rotationDegrees = rotationDegrees,
                flipHorizontal = flipHorizontal,
                flipVertical = flipVertical,
                cropRatio = cropRatio,
                saveAsCopy = saveAsCopy,
                customCropBounds = customCropBounds
            )
            _editingPhoto.value = null
            if (saved != null) {
                _activeLightboxPhotoId.value = saved.id
                loadHistogramForPhoto(saved)
                postMessage(
                    if (saveAsCopy) "Saved edited print '${saved.title}' (${saved.resolutionText})"
                    else "Updated '${saved.title}' (${saved.resolutionText})"
                )
            } else {
                postMessage("Unable to process image file")
            }
        }
    }

    fun createNewAlbum(name: String, description: String) {
        viewModelScope.launch {
            repository.createCustomAlbum(name, description)
            postMessage("Created album '${name.trim()}'")
        }
    }

    fun importDevicePhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val targetAlbum = _filterState.value.selectedAlbumName ?: "Camera Roll"
        viewModelScope.launch {
            val importedIds = repository.importPhotosFromUris(uris, targetAlbum)
            val count = importedIds.size
            if (count > 0) {
                if (_aiAutoTagOnImport.value) {
                    AiPhotoOrganizerService.startOrganizePhotosByIds(
                        context = appContext,
                        photoIds = importedIds,
                        moveIntoCategoryAlbums = _aiMoveIntoCategoryAlbums.value
                    )
                    postMessage("Imported $count photo${if (count > 1) "s" else ""} • Background AI Tagger started!")
                } else {
                    postMessage("Imported $count photo${if (count > 1) "s" else ""} into $targetAlbum")
                }
            } else {
                postMessage("Could not import selected media")
            }
        }
    }

    fun saveCameraCapture(bitmap: Bitmap?) {
        if (bitmap == null) return
        val targetAlbum = _filterState.value.selectedAlbumName ?: "Camera Roll"
        viewModelScope.launch {
            val photo = repository.saveCapturedBitmap(bitmap, targetAlbum)
            if (_aiAutoTagOnImport.value) {
                AiPhotoOrganizerService.startOrganizePhotosByIds(
                    context = appContext,
                    photoIds = listOf(photo.id),
                    moveIntoCategoryAlbums = _aiMoveIntoCategoryAlbums.value
                )
                postMessage("Saved '${photo.title}' • Background AI Tagger analyzing...")
            } else {
                postMessage("Saved '${photo.title}' to $targetAlbum")
            }
        }
    }

    fun clearDefaultSamplePhotos() {
        viewModelScope.launch {
            val removed = repository.clearDefaultSamplePhotos()
            postMessage(
                if (removed > 0) "Removed $removed default sample photos"
                else "Default sample photos already cleared"
            )
        }
    }

    fun loadSampleDemoPhotos() {
        viewModelScope.launch {
            val added = repository.loadSampleDemoPhotos()
            postMessage(
                if (added > 0) "Loaded $added sample demo photos"
                else "Sample demo photos are already loaded"
            )
        }
    }

    fun startAiOrganizeAllPhotos(onlyUntagged: Boolean = false) {
        AiPhotoOrganizerService.startOrganizeLibrary(
            context = appContext,
            moveIntoCategoryAlbums = _aiMoveIntoCategoryAlbums.value,
            onlyUntagged = onlyUntagged
        )
        postMessage("Started AI Background Photo Organizer (Food, Travel, Portraits & more)")
    }

    fun startAiAnalyzeSinglePhoto(photo: PhotoEntity) {
        AiPhotoOrganizerService.startOrganizePhotosByIds(
            context = appContext,
            photoIds = listOf(photo.id),
            moveIntoCategoryAlbums = _aiMoveIntoCategoryAlbums.value
        )
        postMessage("Analyzing '${photo.title}' with Gemini AI in background...")
    }

    fun cancelAiOrganizerService() {
        AiPhotoOrganizerService.cancelRunningService(appContext)
        postMessage("Cancelled background AI photo organizer")
    }

    fun toggleAiMoveIntoCategoryAlbums() {
        val next = !_aiMoveIntoCategoryAlbums.value
        _aiMoveIntoCategoryAlbums.value = next
        prefs.edit().putBoolean("ai_move_into_albums", next).apply()
        postMessage(
            if (next) "AI Organizer will move photos into Category Albums (Food, Travel, Portraits…)"
            else "AI Organizer set to Tag-Only mode (keeps existing albums)"
        )
    }

    fun toggleAiAutoTagOnImport() {
        val next = !_aiAutoTagOnImport.value
        _aiAutoTagOnImport.value = next
        prefs.edit().putBoolean("ai_auto_tag_on_import", next).apply()
        postMessage(
            if (next) "Background AI Auto-Tagging enabled for new imports & captures"
            else "Background AI Auto-Tagging on import paused"
        )
    }

    fun dismissAiOrganizerBanner() {
        AiOrganizerStatusBus.resetToIdle()
    }

    fun toggleFavorite(photo: PhotoEntity) {
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(photo)
            val msg = if (isNowFavorite) {
                "Added '${photo.title}' to Favorites ♥"
            } else {
                "Removed '${photo.title}' from Favorites"
            }
            postMessage(msg)
        }
    }

    fun assignPhotoCategory(photo: PhotoEntity, categoryName: String) {
        viewModelScope.launch {
            repository.assignPhotoCategory(photo, categoryName)
            postMessage("Categorized '${photo.title}' under '$categoryName'")
        }
    }

    fun favoriteAllInList(photos: List<PhotoEntity>, label: String = "Section") {
        if (photos.isEmpty()) return
        viewModelScope.launch {
            val allAlreadyFav = photos.all { it.isFavorite }
            val nextFav = !allAlreadyFav
            repository.setFavoriteBatch(photos.map { it.id }, nextFav)
            postMessage(
                if (nextFav) "Favorited ${photos.size} photo(s) in $label ♥"
                else "Unfavorited ${photos.size} photo(s) in $label"
            )
        }
    }

    fun updatePhotoMetadata(
        photo: PhotoEntity,
        title: String,
        album: String,
        tags: String,
        notes: String,
        location: String,
        cameraModel: String = photo.cameraModel,
        aperture: String = photo.aperture,
        shutterSpeed: String = photo.shutterSpeed,
        iso: String = photo.iso,
        focalLength: String = photo.focalLength
    ) {
        viewModelScope.launch {
            repository.updateMetadata(
                photo = photo,
                newTitle = title,
                newAlbum = album,
                newTags = tags,
                newNotes = notes,
                newLocation = location,
                newCameraModel = cameraModel,
                newAperture = aperture,
                newShutterSpeed = shutterSpeed,
                newIso = iso,
                newFocalLength = focalLength
            )
            postMessage("Updated EXIF & archival metadata for '${title.trim().ifEmpty { photo.title }}'")
        }
    }

    fun batchFavoriteSelected(favorite: Boolean) {
        val ids = _selectedPhotoIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.setFavoriteBatch(ids, favorite)
            clearSelection()
            postMessage("${ids.size} photo(s) ${if (favorite) "favorited" else "unfavorited"}")
        }
    }

    fun movePhotosToAlbum(ids: List<Long>, targetAlbum: String, copyInsteadOfMove: Boolean) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            if (copyInsteadOfMove) {
                repository.copyPhotosToAlbum(ids, targetAlbum)
                postMessage("Copied ${ids.size} photo(s) to '$targetAlbum'")
            } else {
                repository.movePhotosToAlbum(ids, targetAlbum)
                postMessage("Moved ${ids.size} photo(s) to '$targetAlbum'")
            }
            clearSelection()
        }
    }

    fun setVaultLocked(ids: List<Long>, locked: Boolean) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.setVaultLockedBatch(ids, locked)
            clearSelection()
            if (_activeLightboxPhotoId.value in ids) {
                closeLightbox()
            }
            postMessage(
                if (locked) "Moved ${ids.size} photo(s) to Private Vault"
                else "Restored ${ids.size} photo(s) to Public Gallery"
            )
        }
    }

    fun moveToTrash(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.moveToTrashBatch(ids)
            clearSelection()
            if (_activeLightboxPhotoId.value in ids) {
                closeLightbox()
            }
            postMessage("Moved ${ids.size} photo(s) to Recycle Bin")
        }
    }

    fun restoreFromTrash(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.restoreFromTrashBatch(ids)
            clearSelection()
            postMessage("Restored ${ids.size} photo(s) from Recycle Bin")
        }
    }

    fun permanentlyDelete(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val count = repository.permanentlyDeleteBatch(ids)
            clearSelection()
            if (_activeLightboxPhotoId.value in ids) {
                closeLightbox()
            }
            postMessage("Permanently deleted $count photo${if (count > 1) "s" else ""} from local storage")
        }
    }

    fun deleteSelectedPhotosFromLocalStorage() {
        val ids = _selectedPhotoIds.value.toList()
        if (ids.isEmpty()) return
        permanentlyDelete(ids)
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            val count = repository.emptyTrash()
            if (count > 0) {
                postMessage("Emptied Recycle Bin ($count file${if (count > 1) "s" else ""} removed)")
            }
        }
    }

    fun attemptUnlockVault(enteredPin: String): Boolean {
        return if (enteredPin == _vaultPin.value) {
            _isVaultUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun updateVaultPin(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            _vaultPin.value = newPin
            prefs.edit().putString("vault_pin", newPin).apply()
            postMessage("Updated Private Vault 4-digit PIN")
        }
    }

    fun toggleVaultBiometric() {
        val next = !_vaultBiometricEnabled.value
        _vaultBiometricEnabled.value = next
        prefs.edit().putBoolean("vault_biometric_enabled", next).apply()
        postMessage(
            if (next) "Biometric & Passkey unlock enabled for Private Vault"
            else "Biometric quick-unlock disabled (PIN required)"
        )
    }

    fun postMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = GalleryDatabase.getInstance(context.applicationContext)
            val repo = GalleryRepository(context.applicationContext, db.photoDao())
            return GalleryViewModel(repo, context.applicationContext) as T
        }
    }
}
