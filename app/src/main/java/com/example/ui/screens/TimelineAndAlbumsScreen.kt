package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.PersonClusterEntity
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AlbumSortOption
import com.example.data.AlbumSummary
import com.example.data.GridDensity
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.SortOption
import com.example.data.formatBytes
import com.example.service.AiOrganizerUiState
import com.example.ui.GalleryFilterState
import com.example.ui.theme.AppThemeCatalog
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.GalleryLayoutStyle
import com.example.ui.theme.LocalCompleteUiTheme
import java.io.File
import kotlinx.coroutines.delay

data class SmartPhotoCategorySpec(
    val id: String,
    val label: String,
    val subtitle: String,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    themeFilterCategory: Int,
    isThemeSwitcherExpanded: Boolean,
    isSearchExpanded: Boolean,
    filterState: GalleryFilterState,
    availableTags: List<String>,
    selectedPhotoIds: Set<Long> = emptySet(),
    onSelectCompleteTheme: (CompleteUiThemeSpec) -> Unit,
    onSelectThemeCategory: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectTag: (String) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onPhotoLongClick: (PhotoEntity) -> Unit = {},
    onToggleFavorite: (PhotoEntity) -> Unit,
    onStartSlideshow: () -> Unit,
    onImportClick: () -> Unit,
    onClearAlbumFilter: () -> Unit,
    onCycleLayoutStyle: () -> Unit = {},
    onSelectLayoutStyle: (GalleryLayoutStyle) -> Unit = {},
    onSelectGridDensity: (GridDensity) -> Unit = {},
    onSelectSquareGridMode: (Boolean) -> Unit = {},
    onPinchGridStyle: (Boolean) -> Unit = {},
    onOpenCollageStudio: () -> Unit = {},
    allPhotosForCategories: List<PhotoEntity> = photos,
    aiOrganizerUiState: AiOrganizerUiState = AiOrganizerUiState.Idle,
    onStartAiOrganize: () -> Unit = {},
    onCancelAiOrganize: () -> Unit = {},
    onDismissAiBanner: () -> Unit = {},
    onInspectExif: (PhotoEntity) -> Unit = {},
    onOpenDarkroom: (PhotoEntity) -> Unit = {},
    onOpenCropStudio: (PhotoEntity) -> Unit = onOpenDarkroom,
    onAssignCategory: (PhotoEntity, String) -> Unit = { _, _ -> },
    onQuickRotate90: (PhotoEntity) -> Unit = {},
    onDuplicatePhoto: (PhotoEntity) -> Unit = {},
    onMoveToVault: (PhotoEntity) -> Unit = {},
    onMoveToTrash: (PhotoEntity) -> Unit = {},
    onFavoriteDateGroup: (List<PhotoEntity>, String) -> Unit = { _, _ -> },
    onToggleSelectDateGroup: (List<PhotoEntity>, String) -> Unit = onFavoriteDateGroup,
    onToggleHideScreenshots: () -> Unit = {},
    onToggleGroupByDate: () -> Unit = {},
    onOpenWatermarkStudio: () -> Unit = {},
    onOpenCreateAlbum: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleSearchBar: () -> Unit = {},
    hasMediaPermission: Boolean = true,
    shouldShowPermissionRationale: Boolean = false,
    onRequestMediaPermission: () -> Unit = {},
    onOpenAppSettings: () -> Unit = {},
    isRefreshing: Boolean = false,
    onSyncDeviceGallery: () -> Unit = {},
    onBrowseFilesClick: () -> Unit = onImportClick,
    onTakeCameraPhotoClick: () -> Unit = {},
    onClearSamplePhotos: () -> Unit = {},
    onLoadSamplePhotos: () -> Unit = {},
    isSelectionMode: Boolean = selectedPhotoIds.isNotEmpty(),
    onToggleSelectionMode: () -> Unit = {},
    onToggleSelectPhoto: (PhotoEntity) -> Unit = onPhotoLongClick,
    onSelectAllPhotos: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onRequestDeletePhotos: (List<PhotoEntity>) -> Unit = {},
    personClusters: List<PersonClusterEntity> = emptyList(),
    onOpenQuickClean: () -> Unit = {},
    onOpenSearchOverlay: () -> Unit = {},
    onOpenPersonDetail: (PersonClusterEntity) -> Unit = {}
) {
    if (filterState.selectedAlbumName != null) {
        BackHandler { onClearAlbumFilter() }
    }

    val timelineContext = LocalContext.current
    val timelinePrefs = remember(timelineContext) {
        timelineContext.getSharedPreferences("simple_gallery_prefs", android.content.Context.MODE_PRIVATE)
    }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var homeMoreMenuExpanded by remember { mutableStateOf(false) }
    var homeSortSubMenuExpanded by remember { mutableStateOf(false) }
    // Retractable Grid Pod: remembers user preference across app reloads
    var showGridStyleBar by remember {
        mutableStateOf(timelinePrefs.getBoolean("preferred_grid_bar_expanded", false))
    }
    val groupByRelativeDate = filterState.groupByRelativeDate
    // Toggleable direct MediaStore PhotoGalleryScreen view
    var showMediaStorePhotoGallery by remember { mutableStateOf(false) }

    if (showMediaStorePhotoGallery) {
        BackHandler { showMediaStorePhotoGallery = false }
        PhotoGalleryScreen(
            activeTheme = activeTheme,
            fallbackPhotos = allPhotosForCategories,
            initialColumns = filterState.gridDensity.columns.coerceIn(2, 4),
            onBackClick = { showMediaStorePhotoGallery = false },
            onOpenInGalleryLightbox = onPhotoClick,
            onImportPhotosClick = onImportClick,
            onTakeCameraPhotoClick = onTakeCameraPhotoClick,
            onDeleteLinkedGalleryPhotos = { deletedPaths ->
                val matching = allPhotosForCategories.filter { it.filePath in deletedPaths }
                if (matching.isNotEmpty()) {
                    onRequestDeletePhotos(matching)
                }
            },
            onRescanTriggered = onSyncDeviceGallery
        )
        return
    }

    var photoPendingCategoryAssignment by remember { mutableStateOf<PhotoEntity?>(null) }

    var pinchHudTimestamp by remember { mutableLongStateOf(0L) }
    var showPinchHud by remember { mutableStateOf(false) }

    LaunchedEffect(pinchHudTimestamp) {
        if (pinchHudTimestamp > 0L) {
            showPinchHud = true
            delay(1400L)
            showPinchHud = false
        }
    }

    val gridColumns = filterState.gridDensity.columns.coerceIn(1, 5)

    // Smart Filter Pills below the Search Bar: All Photos, Camera, Favorites, Screenshots, Videos (+ Edited)
    val smartCategorySpecs = remember {
        listOf(
            SmartPhotoCategorySpec("All", "All Photos", "Complete Archive", Color(0xFFE06D53)),
            SmartPhotoCategorySpec("Camera", "Camera", "Camera Roll", Color(0xFFE06D53)),
            SmartPhotoCategorySpec("Favorites", "Favorites", "Hearted Prints", Color(0xFFE06D53)),
            SmartPhotoCategorySpec("Screenshots", "Screenshots", "Screen Captures", Color(0xFFD9822B)),
            SmartPhotoCategorySpec("Videos", "Videos", "Motion Clips", Color(0xFF4A7C59)),
            SmartPhotoCategorySpec("Edited", "Edited", "Darkroom & Cropped", Color(0xFFE06D53))
        )
    }

    // Compute relative date groups ("Today", "Yesterday", "This Week", "Earlier This Month", etc.) only when enabled
    val nowMs = remember(photos, groupByRelativeDate) { System.currentTimeMillis() }
    val groupedPhotosByDate = remember(photos, nowMs, groupByRelativeDate) {
        if (groupByRelativeDate) {
            photos.groupBy { photo -> photo.relativeDateGroup(nowMs) }
        } else {
            emptyMap()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(filterState.gridDensity) {
                awaitEachGesture {
                    var cumulativeZoom = 1f
                    var triggeredInThisGesture = false
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size >= 2 && !triggeredInThisGesture) {
                            val zoomChange = event.calculateZoom()
                            if (zoomChange != 1f) {
                                cumulativeZoom *= zoomChange
                                event.changes.forEach { it.consume() }
                                if (cumulativeZoom > 1.24f) {
                                    triggeredInThisGesture = true
                                    pinchHudTimestamp = System.currentTimeMillis()
                                    onPinchGridStyle(true)
                                } else if (cumulativeZoom < 0.78f) {
                                    triggeredInThisGesture = true
                                    pinchHudTimestamp = System.currentTimeMillis()
                                    onPinchGridStyle(false)
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        val timelineGridState = rememberLazyGridState()
        val timelineScope = rememberCoroutineScope()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onSyncDeviceGallery,
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = activeTheme.surface,
                    color = activeTheme.primaryAccent,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("photo_grid_pull_to_refresh")
        ) {
            LazyVerticalGrid(
                state = timelineGridState,
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                activeTheme.background,
                                activeTheme.backgroundSecondary
                            )
                        )
                    )
                    .testTag("timeline_photo_grid"),
                contentPadding = PaddingValues(
                    start = if (gridColumns >= 4) 4.dp else 8.dp,
                    end = if (gridColumns >= 4) 4.dp else 8.dp,
                    top = 6.dp,
                    bottom = 112.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(if (gridColumns >= 4) 3.dp else if (gridColumns == 3) 4.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (gridColumns >= 4) 3.dp else if (gridColumns == 3) 4.dp else 8.dp)
            ) {
            // 1. Standardized Photos Header Bar + Search Input + Smart Filter Pills
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (gridColumns >= 4) 8.dp else 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Standardized Top App Bar with Headline "Photos" & Consolidated ••• More Options Menu
                    HomeTopHeaderBar(
                        photoCount = photos.size,
                        albumName = filterState.selectedAlbumName,
                        activeTheme = activeTheme,
                        gridColumns = gridColumns,
                        currentGridDensity = filterState.gridDensity,
                        onSelectGridDensity = onSelectGridDensity,
                        showGridStyleBar = showGridStyleBar,
                        hideScreenshotsFromMainFeed = filterState.hideScreenshotsFromMainFeed,
                        onToggleHideScreenshots = onToggleHideScreenshots,
                        isFavoritesFilterActive = filterState.selectedTag.equals("Favorites", ignoreCase = true),
                        currentSortOption = filterState.sortOption,
                        onSelectSort = onSelectSort,
                        onToggleSearchBar = onToggleSearchBar,
                        onToggleGridStyleBar = {
                            val next = !showGridStyleBar
                            showGridStyleBar = next
                            timelinePrefs.edit().putBoolean("preferred_grid_bar_expanded", next).commit()
                        },
                        onToggleFavoritesFilter = {
                            if (filterState.selectedTag.equals("Favorites", ignoreCase = true)) {
                                onSelectTag("All")
                            } else {
                                onSelectTag("Favorites")
                            }
                        },
                        onCreateNewCategory = onOpenCreateAlbum,
                        onOpenMediaStoreGallery = { showMediaStorePhotoGallery = true },
                        onOpenCropForFirstPhoto = {
                            photos.firstOrNull()?.let { onOpenCropStudio(it) }
                        },
                        onOpenCollageStudio = onOpenCollageStudio,
                        onOpenSettings = onOpenSettings,
                        onStartSlideshow = onStartSlideshow,
                        onStartAiOrganize = onStartAiOrganize,
                        onSyncDeviceGallery = onSyncDeviceGallery,
                        onToggleGroupByDate = onToggleGroupByDate,
                        groupByRelativeDate = groupByRelativeDate,
                        onToggleSelectionMode = onToggleSelectionMode,
                        isSelectionMode = isSelectionMode || selectedPhotoIds.isNotEmpty(),
                        selectedCount = selectedPhotoIds.size,
                        onRequestDeleteSelected = {
                            val selectedList = allPhotosForCategories.filter { it.id in selectedPhotoIds }
                            if (selectedList.isNotEmpty()) {
                                onRequestDeletePhotos(selectedList)
                            }
                        },
                        onClearAlbumFilter = onClearAlbumFilter,
                        onOpenQuickClean = onOpenQuickClean,
                        onLoadSamplePhotos = onLoadSamplePhotos
                    )

                    // Rich Google Photos-style Search Bar (tapping opens Search & People Overlay)
                    val searchInteractionSource = remember { MutableInteractionSource() }
                    LaunchedEffect(searchInteractionSource) {
                        searchInteractionSource.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                onOpenSearchOverlay()
                            }
                        }
                    }
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        interactionSource = searchInteractionSource,
                        readOnly = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gallery_search_input"),
                        placeholder = {
                            Text(
                                "Search people, dates, or tags…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTheme.textSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search photos and people",
                                tint = activeTheme.primaryAccent,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = activeTheme.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else if (personClusters.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = activeTheme.primaryAccent.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onOpenSearchOverlay() }
                                ) {
                                    Text(
                                        text = "${personClusters.size} People",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = activeTheme.primaryAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = activeTheme.surface,
                            unfocusedContainerColor = activeTheme.surface,
                            focusedBorderColor = activeTheme.primaryAccent,
                            unfocusedBorderColor = activeTheme.borderStroke,
                            focusedTextColor = activeTheme.textPrimary,
                            unfocusedTextColor = activeTheme.textPrimary
                        )
                    )

                    // "On This Day / Flashback Memories" Story Carousel
                    val flashbackMoments = remember(allPhotosForCategories.size, allPhotosForCategories.firstOrNull()?.id) {
                        val currentCal = java.util.Calendar.getInstance()
                        val currentYear = currentCal.get(java.util.Calendar.YEAR)
                        val currentMonth = currentCal.get(java.util.Calendar.MONTH)
                        val currentDay = currentCal.get(java.util.Calendar.DAY_OF_MONTH)
                        val photoCal = java.util.Calendar.getInstance()
                        allPhotosForCategories.asSequence().filter { photo ->
                            photoCal.timeInMillis = photo.dateTaken
                            photoCal.get(java.util.Calendar.MONTH) == currentMonth &&
                                photoCal.get(java.util.Calendar.DAY_OF_MONTH) == currentDay &&
                                photoCal.get(java.util.Calendar.YEAR) < currentYear
                        }.take(8).toList().ifEmpty {
                            // If no exact anniversary photo, showcase top favorite highlights as Flashbacks
                            allPhotosForCategories.asSequence().filter { it.isFavorite }.take(6).toList()
                        }
                    }

                    if (flashbackMoments.isNotEmpty() && filterState.searchQuery.isBlank() && filterState.selectedTag == "All") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = activeTheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, activeTheme.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                .padding(vertical = 4.dp)
                                .testTag("timeline_memories_carousel")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = activeTheme.primaryAccent,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = "On This Day • Memories",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = activeTheme.textPrimary
                                        )
                                    }
                                    Text(
                                        text = "${flashbackMoments.size} Moments",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = activeTheme.primaryAccent
                                    )
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(flashbackMoments, key = { "memory_${it.id}" }) { memoryPhoto ->
                                        val memoryContext = LocalContext.current
                                        val memoryThumbModel = remember(memoryPhoto.id, memoryPhoto.filePath) {
                                            PhotoImageProcessor.buildThumbnailRequest(memoryContext, memoryPhoto.filePath, 240, 240)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = activeTheme.surfaceVariant,
                                            modifier = Modifier
                                                .width(115.dp)
                                                .height(135.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { onPhotoClick(memoryPhoto) }
                                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(16.dp))
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = memoryThumbModel,
                                                    contentDescription = memoryPhoto.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                // Dark gradient scrim & title
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.BottomCenter)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                            )
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 5.dp)
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = memoryPhoto.shortDateText.ifBlank { "Flashback" },
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.sp
                                                            ),
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = memoryPhoto.title,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontSize = 8.5.sp
                                                            ),
                                                            color = Color.White.copy(alpha = 0.82f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // De-cluttered Light-Purple / Neutral Category Chips Strip (No action clutter row or dark gradient boxes)
                    VisualPhotoCategoriesStrip(
                        specs = smartCategorySpecs,
                        allPhotos = allPhotosForCategories,
                        selectedTag = filterState.selectedTag,
                        activeTheme = activeTheme,
                        onSelectCategory = onSelectTag
                    )

                    // Contextual Multi-Select & Local Storage Delete Banner
                    AnimatedVisibility(
                        visible = isSelectionMode || selectedPhotoIds.isNotEmpty(),
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(tween(180)) + scaleIn(
                            initialScale = 0.95f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                        exit = shrinkVertically(tween(160)) + fadeOut(tween(150)) + scaleOut(targetScale = 0.95f)
                    ) {
                        val selectedPhotoObjects = remember(allPhotosForCategories, selectedPhotoIds) {
                            allPhotosForCategories.filter { it.id in selectedPhotoIds }
                        }
                        val selectedTotalBytes = remember(selectedPhotoObjects) {
                            selectedPhotoObjects.sumOf { it.sizeBytes }
                        }
                        val allVisibleSelected = photos.isNotEmpty() && photos.all { it.id in selectedPhotoIds }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = activeTheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, activeTheme.primaryAccent, RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("multi_select_action_banner")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedPhotoIds.isEmpty()) {
                                            "Select Photos to Delete or Edit"
                                        } else {
                                            "${selectedPhotoIds.size} Selected · ${formatBytes(selectedTotalBytes)}"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = activeTheme.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (selectedPhotoIds.isEmpty()) {
                                            "Tap or long-press any photo card in the grid"
                                        } else {
                                            "Ready to delete from local storage or batch edit"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = activeTheme.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = activeTheme.surfaceVariant,
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onSelectAllPhotos() }
                                            .testTag("multi_select_all_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SelectAll,
                                                contentDescription = if (allVisibleSelected) "Deselect All" else "Select All",
                                                tint = activeTheme.primaryAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (allVisibleSelected) "None" else "All",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = activeTheme.textPrimary
                                            )
                                        }
                                    }

                                    Button(
                                        enabled = selectedPhotoIds.isNotEmpty(),
                                        onClick = {
                                            if (selectedPhotoObjects.isNotEmpty()) {
                                                onRequestDeletePhotos(selectedPhotoObjects)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE11D48),
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("delete_selected_photos_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "Delete selected photos from local storage",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (selectedPhotoIds.isNotEmpty()) {
                                                "Delete (${selectedPhotoIds.size})"
                                            } else {
                                                "Delete"
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = activeTheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .clickable { onClearSelection() }
                                            .testTag("cancel_selection_mode_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Exit selection mode",
                                                tint = activeTheme.textPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Retractable Photo Grid Pod (Expanded with larger, easy-to-tap controls)
                    AnimatedVisibility(
                        visible = showGridStyleBar,
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(tween(180)) + scaleIn(initialScale = 0.95f),
                        exit = shrinkVertically(tween(160)) + fadeOut(tween(140)) + scaleOut(targetScale = 0.95f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = activeTheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("photo_grid_selector_section")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1× / 2× / 3× / 4× / 5× Column Pills
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    listOf(
                                        GridDensity.THREE,
                                        GridDensity.FOUR,
                                        GridDensity.FIVE,
                                        GridDensity.TWO,
                                        GridDensity.LIST
                                    ).forEach { density ->
                                        val isSelected = filterState.gridDensity == density
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.surfaceVariant.copy(alpha = 0.72f)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    pinchHudTimestamp = System.currentTimeMillis()
                                                    onSelectGridDensity(density)
                                                }
                                                .testTag("grid_density_chip_${density.columns}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${density.columns}×",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.5.sp
                                                    ),
                                                    color = if (isSelected) {
                                                        activeTheme.onPrimaryAccent
                                                    } else {
                                                        activeTheme.textPrimary
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 1:1 Square vs Adaptive Crop Toggle Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (filterState.isSquareGrid) {
                                        activeTheme.primaryAccent.copy(alpha = 0.18f)
                                    } else {
                                        activeTheme.surfaceVariant.copy(alpha = 0.72f)
                                    },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (filterState.isSquareGrid) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.borderStroke
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onSelectSquareGridMode(!filterState.isSquareGrid)
                                        }
                                        .testTag("grid_crop_mode_toggle")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = if (filterState.isSquareGrid) "1:1" else "FIT",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            ),
                                            color = if (filterState.isSquareGrid) {
                                                activeTheme.primaryAccent
                                            } else {
                                                activeTheme.textPrimary
                                            }
                                        )
                                    }
                                }

                                // Retract / Close Button
                                Surface(
                                    shape = CircleShape,
                                    color = activeTheme.surfaceVariant.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            showGridStyleBar = false
                                            timelinePrefs.edit().putBoolean("preferred_grid_bar_expanded", false).commit()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Retract Grid Bar",
                                            tint = activeTheme.textPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Live AI Progress Banner (Only visible when AI scan is actively running/completed)
                    AnimatedVisibility(
                        visible = aiOrganizerUiState !is AiOrganizerUiState.Idle,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        AiCompactStatusBanner(
                            aiState = aiOrganizerUiState,
                            activeTheme = activeTheme,
                            onCancelAiOrganize = onCancelAiOrganize,
                            onDismissBanner = onDismissAiBanner
                        )
                    }

                    // Prominent Device Media Permission Banner when permission is not granted (visible even when sample or imported photos exist)
                    AnimatedVisibility(visible = !hasMediaPermission && photos.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.2.dp, activeTheme.primaryAccent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                                .testTag("timeline_permission_banner")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = activeTheme.primaryAccent.copy(alpha = 0.16f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = activeTheme.primaryAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Scan Local Device Photos",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = activeTheme.textPrimary
                                        )
                                        Text(
                                            text = if (shouldShowPermissionRationale) {
                                                "Photo access is required to scan and display images from Camera, Screenshots, and Pictures."
                                            } else {
                                                "Allow photo access so the gallery can automatically scan and display your phone's local photos."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = activeTheme.textSecondary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onRequestMediaPermission,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeTheme.primaryAccent,
                                            contentColor = activeTheme.onPrimaryAccent
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("banner_grant_media_permission_button")
                                    ) {
                                        Text(
                                            text = "Grant Photo Access",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onOpenAppSettings,
                                        modifier = Modifier
                                            .height(40.dp)
                                            .testTag("banner_open_settings_button")
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Settings",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. High-Density Futuristic Photo Grid Grouped by "Today", "Yesterday", "This Week", etc.
            if (photos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val isFilteredView = !filterState.selectedTag.equals("All", ignoreCase = true) ||
                        filterState.searchQuery.isNotBlank() ||
                        filterState.selectedAlbumName != null
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
                            .testTag("personal_photos_empty_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = activeTheme.primaryAccent.copy(alpha = 0.12f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = activeTheme.primaryAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    !hasMediaPermission -> "Allow Gallery Access"
                                    isFilteredView -> "No photos in '${filterState.selectedAlbumName ?: filterState.selectedTag}'"
                                    else -> "Your Gallery is Empty"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = when {
                                    !hasMediaPermission ->
                                        "Grant photo access to automatically scan your device library, or select photos directly."
                                    isFilteredView ->
                                        "No photos matched this filter. Switch back to All Photos to view your library."
                                    else ->
                                        "Select photos from your device or scan local folders to populate your gallery."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTheme.textSecondary
                            )
                            if (!hasMediaPermission) {
                                Button(
                                    onClick = onRequestMediaPermission,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeTheme.primaryAccent,
                                        contentColor = activeTheme.onPrimaryAccent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("grant_media_permission_button")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan Device", fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = onImportClick,
                                        modifier = Modifier.testTag("empty_state_pick_photos_button")
                                    ) {
                                        Text("Select Photos", color = activeTheme.primaryAccent, fontWeight = FontWeight.SemiBold)
                                    }
                                    TextButton(
                                        onClick = onOpenAppSettings,
                                        modifier = Modifier.testTag("empty_state_open_settings_button")
                                    ) {
                                        Text("App Settings", color = activeTheme.textSecondary)
                                    }
                                }
                            } else if (isFilteredView) {
                                Button(
                                    onClick = {
                                        onSearchQueryChange("")
                                        onSelectTag("All")
                                        onClearAlbumFilter()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeTheme.primaryAccent,
                                        contentColor = activeTheme.onPrimaryAccent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Show All Photos", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Prominent Primary Terracotta Pill CTA
                                Button(
                                    onClick = onImportClick,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeTheme.primaryAccent,
                                        contentColor = activeTheme.onPrimaryAccent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("empty_state_pick_photos_button")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Select Photos", fontWeight = FontWeight.Bold)
                                }

                                // Outlined Tonal Pill Buttons beneath Select Photos
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = onSyncDeviceGallery,
                                        shape = RoundedCornerShape(50),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = activeTheme.surfaceVariant.copy(alpha = 0.72f),
                                            contentColor = activeTheme.textPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("rescan_device_gallery_button")
                                    ) {
                                        Text(
                                            "Scan Device",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onTakeCameraPhotoClick,
                                        shape = RoundedCornerShape(50),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = activeTheme.surfaceVariant.copy(alpha = 0.72f),
                                            contentColor = activeTheme.textPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("empty_state_camera_button")
                                    ) {
                                        Text(
                                            "Take Photo",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (groupByRelativeDate) {
                groupedPhotosByDate.forEach { (dateGroupTitle, groupPhotos) ->
                    // Date Section Header ("TODAY", "YESTERDAY", "THIS WEEK", "EARLIER THIS MONTH", etc.)
                    item(
                        key = "date_header_$dateGroupTitle",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        RelativeDateSectionHeader(
                            dateGroupTitle = dateGroupTitle,
                            subtitleDate = groupPhotos.firstOrNull()?.shortDateText.orEmpty(),
                            groupPhotos = groupPhotos,
                            selectedPhotoIds = selectedPhotoIds,
                            activeTheme = activeTheme,
                            onToggleSelectGroup = {
                                onToggleSelectDateGroup(groupPhotos, dateGroupTitle)
                            }
                        )
                    }

                    // Photos inside this date section
                    items(
                        items = groupPhotos,
                        key = { photo -> photo.id },
                        contentType = { "photo_card" },
                        span = { GridItemSpan(1) }
                    ) { photo ->
                        ThemeAdaptiveShowcaseCard(
                            photo = photo,
                            activeTheme = activeTheme,
                            gridColumns = gridColumns,
                            isHeroFullSpan = gridColumns == 1,
                            isCompactGrid = gridColumns >= 3,
                            isSquareGrid = filterState.isSquareGrid,
                            isSelected = photo.id in selectedPhotoIds,
                            isSelectionMode = isSelectionMode || selectedPhotoIds.isNotEmpty(),
                            onClick = { onPhotoClick(photo) },
                            onToggleSelect = { onToggleSelectPhoto(photo) },
                            onToggleFavorite = { onToggleFavorite(photo) },
                            onInspectExif = { onInspectExif(photo) },
                            onOpenDarkroom = { onOpenDarkroom(photo) },
                            onOpenCropStudio = { onOpenCropStudio(photo) },
                            onAssignCategoryClick = { photoPendingCategoryAssignment = photo },
                            onQuickRotate90 = { onQuickRotate90(photo) },
                            onDuplicatePhoto = { onDuplicatePhoto(photo) },
                            onMoveToVault = { onMoveToVault(photo) },
                            onMoveToTrash = { onMoveToTrash(photo) },
                            onRequestDeleteFromStorage = { onRequestDeletePhotos(listOf(photo)) }
                        )
                    }
                }
            } else {
                items(
                    items = photos,
                    key = { photo -> photo.id },
                    contentType = { "photo_card" },
                    span = { GridItemSpan(1) }
                ) { photo ->
                    ThemeAdaptiveShowcaseCard(
                        photo = photo,
                        activeTheme = activeTheme,
                        gridColumns = gridColumns,
                        isHeroFullSpan = gridColumns == 1,
                        isCompactGrid = gridColumns >= 3,
                        isSquareGrid = filterState.isSquareGrid,
                        isSelected = photo.id in selectedPhotoIds,
                        isSelectionMode = isSelectionMode || selectedPhotoIds.isNotEmpty(),
                        onClick = { onPhotoClick(photo) },
                        onToggleSelect = { onToggleSelectPhoto(photo) },
                        onToggleFavorite = { onToggleFavorite(photo) },
                        onInspectExif = { onInspectExif(photo) },
                        onOpenDarkroom = { onOpenDarkroom(photo) },
                        onOpenCropStudio = { onOpenCropStudio(photo) },
                        onAssignCategoryClick = { photoPendingCategoryAssignment = photo },
                        onQuickRotate90 = { onQuickRotate90(photo) },
                        onDuplicatePhoto = { onDuplicatePhoto(photo) },
                        onMoveToVault = { onMoveToVault(photo) },
                        onMoveToTrash = { onMoveToTrash(photo) },
                        onRequestDeleteFromStorage = { onRequestDeletePhotos(listOf(photo)) }
                    )
                }
            }
        }

        // Interactive Right-Edge Draggable Timeline Date Scrubber with Floating Date Tooltip Badge ("Nov 2026")
        var isDraggingScrubber by remember { mutableStateOf(false) }
        var scrubberFraction by remember { mutableFloatStateOf(0f) }
        val firstVisibleIndex by remember {
            derivedStateOf { timelineGridState.firstVisibleItemIndex }
        }
        val totalGridItems by remember {
            derivedStateOf { timelineGridState.layoutInfo.totalItemsCount.coerceAtLeast(1) }
        }
        val scrollProgressFraction by remember {
            derivedStateOf {
                val maxIdx = (totalGridItems - timelineGridState.layoutInfo.visibleItemsInfo.size).coerceAtLeast(1)
                (firstVisibleIndex.toFloat() / maxIdx.toFloat()).coerceIn(0f, 1f)
            }
        }
        val displayFraction = if (isDraggingScrubber) scrubberFraction else scrollProgressFraction

        val showScrubber = photos.size >= 4 || firstVisibleIndex > 1
        AnimatedVisibility(
            visible = showScrubber && photos.isNotEmpty(),
            enter = fadeIn(tween(180)) + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut(tween(160)) + slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(top = 88.dp, bottom = 104.dp, end = 6.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                val density = LocalDensity.current
                val thumbHeightDp = 50.dp
                val maxTravelPx = with(density) {
                    (maxHeight - thumbHeightDp).coerceAtLeast(1.dp).toPx()
                }
                val thumbOffsetY = (displayFraction * maxTravelPx).roundToInt()

                val activeIdx = if (isDraggingScrubber) {
                    (scrubberFraction * (photos.size - 1)).roundToInt().coerceIn(0, (photos.size - 1).coerceAtLeast(0))
                } else {
                    (displayFraction * (photos.size - 1)).roundToInt().coerceIn(0, (photos.size - 1).coerceAtLeast(0))
                }
                val activePhoto = photos.getOrNull(activeIdx)
                val tooltipDateText = activePhoto?.shortMonthYearText?.ifBlank { activePhoto.shortDateText }
                    ?: "Top"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.offset { IntOffset(0, thumbOffsetY) }
                ) {
                    // Floating Date Tooltip Badge ("Nov 2026") next to the right-edge thumb while dragging
                    AnimatedVisibility(
                        visible = isDraggingScrubber,
                        enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.86f),
                        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.86f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = activeTheme.primaryAccent,
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.28f)
                            ),
                            modifier = Modifier.testTag("timeline_fast_scroll_top_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tooltipDateText,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    ),
                                    color = activeTheme.onPrimaryAccent
                                )
                            }
                        }
                    }

                    // Right-edge draggable thumb handle
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDraggingScrubber) activeTheme.primaryAccent else activeTheme.surface.copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDraggingScrubber) Color.White.copy(alpha = 0.4f) else activeTheme.borderStroke
                        ),
                        shadowElevation = if (isDraggingScrubber) 8.dp else 4.dp,
                        modifier = Modifier
                            .width(24.dp)
                            .height(thumbHeightDp)
                            .pointerInput(photos.size, maxTravelPx) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        scrubberFraction = scrollProgressFraction
                                        isDraggingScrubber = true
                                    },
                                    onDragEnd = { isDraggingScrubber = false },
                                    onDragCancel = { isDraggingScrubber = false },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        val next = (scrubberFraction + (dragAmount / maxTravelPx)).coerceIn(0f, 1f)
                                        scrubberFraction = next
                                        val targetGridIdx = (next * (totalGridItems - 1).coerceAtLeast(0))
                                            .roundToInt()
                                            .coerceIn(0, (totalGridItems - 1).coerceAtLeast(0))
                                        timelineScope.launch {
                                            timelineGridState.scrollToItem(targetGridIdx)
                                        }
                                    }
                                )
                            }
                            .testTag("timeline_edge_scrubber_pill")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 1.5.dp)
                                        .width(10.dp)
                                        .height(2.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isDraggingScrubber) Color.White else activeTheme.primaryAccent)
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // Floating HUD Indicator Pill when Pinching or Switching Photo Grid
        AnimatedVisibility(
            visible = showPinchHud,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetY = { -it }
            ) + fadeIn(tween(180)) + scaleIn(initialScale = 0.88f),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(160)) + scaleOut(targetScale = 0.88f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF141416).copy(alpha = 0.92f),
                shadowElevation = 8.dp,
                modifier = Modifier.border(
                    1.dp,
                    activeTheme.primaryAccent,
                    RoundedCornerShape(50)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = activeTheme.primaryAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "GRID · ${filterState.gridDensity.label.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }

    // Assign Photo Category Dialog (Triggered from any photo's 3-dot menu)
    val targetCategoryPhoto = photoPendingCategoryAssignment
    if (targetCategoryPhoto != null) {
        var customCategoryText by remember(targetCategoryPhoto.id) {
            mutableStateOf(targetCategoryPhoto.albumName)
        }
        AlertDialog(
            onDismissRequest = { photoPendingCategoryAssignment = null },
            title = {
                Text(
                    text = "Assign Photo Category",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select a smart category or enter a custom category for \"${targetCategoryPhoto.title}\":",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeTheme.textSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "Nature",
                            "Travel",
                            "Family",
                            "Portraits",
                            "Architecture",
                            "Night & Street",
                            "Food"
                        ).forEach { catName ->
                            val isChosen = customCategoryText.equals(catName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isChosen) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { customCategoryText = catName }
                            ) {
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Medium
                                    ),
                                    color = if (isChosen) activeTheme.onPrimaryAccent else activeTheme.textPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text("Category / Album Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = customCategoryText.isNotBlank(),
                    onClick = {
                        onAssignCategory(targetCategoryPhoto, customCategoryText.trim())
                        photoPendingCategoryAssignment = null
                    }
                ) {
                    Text("Save Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoPendingCategoryAssignment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HomeTopHeaderBar(
    photoCount: Int,
    albumName: String?,
    activeTheme: CompleteUiThemeSpec,
    gridColumns: Int,
    currentGridDensity: GridDensity,
    onSelectGridDensity: (GridDensity) -> Unit,
    showGridStyleBar: Boolean,
    hideScreenshotsFromMainFeed: Boolean,
    onToggleHideScreenshots: () -> Unit,
    isFavoritesFilterActive: Boolean,
    currentSortOption: SortOption,
    onSelectSort: (SortOption) -> Unit,
    onToggleSearchBar: () -> Unit,
    onToggleGridStyleBar: () -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onCreateNewCategory: () -> Unit,
    onOpenMediaStoreGallery: () -> Unit,
    onOpenCropForFirstPhoto: () -> Unit,
    onOpenCollageStudio: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartSlideshow: () -> Unit,
    onStartAiOrganize: () -> Unit,
    onSyncDeviceGallery: () -> Unit,
    onToggleGroupByDate: () -> Unit,
    groupByRelativeDate: Boolean,
    onToggleSelectionMode: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onRequestDeleteSelected: () -> Unit,
    onClearAlbumFilter: () -> Unit,
    onOpenQuickClean: () -> Unit = {},
    onLoadSamplePhotos: () -> Unit = {}
) {
    var topMenuExpanded by remember { mutableStateOf(false) }
    var sortSubMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Standardized "Photos" Title or Active Album Back Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (albumName != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = activeTheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onClearAlbumFilter() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "All Albums",
                            tint = activeTheme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = activeTheme.primaryAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Column {
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "$photoCount Photos · ${currentSortOption.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTheme.textSecondary
                    )
                }
            }
        }

        // Right: Quick Clean + Theme Button + Consolidated ••• (More Options) Dropdown Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = activeTheme.surfaceVariant.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onOpenQuickClean() }
                    .testTag("home_quick_clean_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Quick Clean Swipe Triage",
                        tint = activeTheme.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = activeTheme.surfaceVariant.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onOpenSettings() }
                    .testTag("home_top_theme_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes & Appearance Settings",
                        tint = activeTheme.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Box {
                Surface(
                    shape = CircleShape,
                    color = if (topMenuExpanded) activeTheme.primaryAccent else activeTheme.surfaceVariant.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { topMenuExpanded = true }
                        .testTag("home_three_dot_menu_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = if (topMenuExpanded) activeTheme.onPrimaryAccent else activeTheme.textPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = topMenuExpanded,
                    onDismissRequest = {
                        topMenuExpanded = false
                        sortSubMenuExpanded = false
                    },
                    modifier = Modifier
                        .width(256.dp)
                        .background(activeTheme.surface)
                        .testTag("home_three_dot_dropdown_menu")
                ) {
                    Text(
                        text = "GALLERY OPTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = activeTheme.primaryAccent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

                    // 1. Sort Order
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sort: ${currentSortOption.label}",
                                fontWeight = FontWeight.SemiBold,
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            sortSubMenuExpanded = !sortSubMenuExpanded
                        }
                    )

                    if (sortSubMenuExpanded) {
                        SortOption.entries.forEach { option ->
                            val isCurrentSort = currentSortOption == option
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "   • ${option.label}",
                                        fontWeight = if (isCurrentSort) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isCurrentSort) activeTheme.primaryAccent else activeTheme.textPrimary
                                    )
                                },
                                onClick = {
                                    onSelectSort(option)
                                    sortSubMenuExpanded = false
                                    topMenuExpanded = false
                                }
                            )
                        }
                    }

                    // 2. Quick Grid Column Switcher (3× / 4× / 5×) + Full Grid Controls Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grid Columns",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = if (showGridStyleBar) "Hide bar" else "More",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = activeTheme.primaryAccent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        topMenuExpanded = false
                                        onToggleGridStyleBar()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("toggle_grid_style_bar_button")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                GridDensity.THREE,
                                GridDensity.FOUR,
                                GridDensity.FIVE
                            ).forEach { density ->
                                val isCurrentCols = currentGridDensity == density
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCurrentCols) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isCurrentCols) activeTheme.primaryAccent else activeTheme.borderStroke
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onSelectGridDensity(density)
                                            topMenuExpanded = false
                                        }
                                        .testTag("menu_grid_cols_${density.columns}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${density.columns}×",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isCurrentCols) activeTheme.onPrimaryAccent else activeTheme.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2b. Hide Screenshots from Main Feed Toggle
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Hide Screenshots from Main Feed",
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeTheme.textPrimary
                                )
                                Text(
                                    text = if (hideScreenshotsFromMainFeed) "Hidden from All Photos" else "Visible in Main Feed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.textSecondary
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (hideScreenshotsFromMainFeed) activeTheme.primaryAccent else activeTheme.textSecondary
                            )
                        },
                        trailingIcon = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (hideScreenshotsFromMainFeed) activeTheme.primaryAccent else activeTheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (hideScreenshotsFromMainFeed) "ON" else "OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (hideScreenshotsFromMainFeed) activeTheme.onPrimaryAccent else activeTheme.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        },
                        onClick = {
                            onToggleHideScreenshots()
                            topMenuExpanded = false
                        },
                        modifier = Modifier.testTag("menu_item_hide_screenshots")
                    )

                    // 3. Folder Management (MediaStore Folders)
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Device Folders",
                                fontWeight = FontWeight.SemiBold,
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onOpenMediaStoreGallery()
                        },
                        modifier = Modifier.testTag("menu_item_mediastore_photo_gallery")
                    )

                    // 4. Create New Album / Category
                    DropdownMenuItem(
                        text = {
                            Text(
                                "New Album / Category",
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onCreateNewCategory()
                        },
                        modifier = Modifier.testTag("new_photo_category_button")
                    )

                    // 5. Filter Favorites
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isFavoritesFilterActive) "Show All Photos" else "Favorites Only",
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isFavoritesFilterActive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = Color(0xFFFF4D6D)
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onToggleFavoritesFilter()
                        },
                        modifier = Modifier.testTag("menu_item_favorites_only")
                    )

                    // 6. Select Photos
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isSelectionMode) "Exit Selection Mode" else "Select Photos",
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Checklist,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onToggleSelectionMode()
                        },
                        modifier = Modifier.testTag("menu_item_select_photos")
                    )

                    if (selectedCount > 0) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Selected ($selectedCount)…",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48)
                                )
                            },
                            onClick = {
                                topMenuExpanded = false
                                onRequestDeleteSelected()
                            },
                            modifier = Modifier.testTag("menu_item_delete_selected_photos")
                        )
                    }

                    // 7. Date Grouping
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (groupByRelativeDate) "Date Grouping: ON" else "Date Grouping: OFF",
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onToggleGroupByDate()
                        },
                        modifier = Modifier.testTag("menu_item_toggle_date_grouping")
                    )

                    HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

                    // 8. Crop Studio, Collage & Slideshow
                    DropdownMenuItem(
                        text = { Text("Crop & Rotate Studio", color = activeTheme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Crop,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onOpenCropForFirstPhoto()
                        },
                        modifier = Modifier.testTag("menu_item_crop_studio")
                    )

                    DropdownMenuItem(
                        text = { Text("Collage Maker", color = activeTheme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AutoAwesomeMosaic,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onOpenCollageStudio()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Start Slideshow", color = activeTheme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onStartSlideshow()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Scan Device Storage", color = activeTheme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onSyncDeviceGallery()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Demo Photos",
                                fontWeight = FontWeight.SemiBold,
                                color = activeTheme.primaryAccent
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = activeTheme.primaryAccent
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onLoadSamplePhotos()
                        },
                        modifier = Modifier.testTag("menu_item_load_demo_photos")
                    )

                    HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Themes & Appearance",
                                fontWeight = FontWeight.SemiBold,
                                color = activeTheme.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = activeTheme.textSecondary
                            )
                        },
                        onClick = {
                            topMenuExpanded = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualPhotoCategoriesStrip(
    specs: List<SmartPhotoCategorySpec>,
    allPhotos: List<PhotoEntity>,
    selectedTag: String,
    activeTheme: CompleteUiThemeSpec,
    onSelectCategory: (String) -> Unit
) {
    // Warm Editorial Terracotta & Cream category chip palette
    val unselectedBg = activeTheme.surfaceVariant
    val unselectedBorder = activeTheme.borderStroke
    val unselectedText = activeTheme.textPrimary
    val unselectedCountBg = activeTheme.backgroundSecondary
    val unselectedCountText = activeTheme.textSecondary
    val selectedAccent = activeTheme.primaryAccent

    val categoryCounts = remember(allPhotos.size, allPhotos.firstOrNull()?.id, specs) {
        specs.associate { spec ->
            spec.id to if (spec.id.equals("All", ignoreCase = true)) {
                allPhotos.size
            } else {
                allPhotos.count { it.matchesCategory(spec.id) }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visual_photo_categories_strip")
            .testTag("ai_smart_categories_section"),
        contentPadding = PaddingValues(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(specs, key = { "visual_cat_${it.id}" }) { spec ->
            val matchingCount = categoryCounts[spec.id] ?: 0
            val isSelected = selectedTag.equals(spec.id, ignoreCase = true)

            val chipBg by animateColorAsState(
                targetValue = if (isSelected) selectedAccent else unselectedBg,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "category_chip_bg"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isSelected) selectedAccent else unselectedBorder,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "category_chip_border"
            )
            val chipTextColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else unselectedText,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "category_chip_text"
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = chipBg,
                modifier = Modifier
                    .height(38.dp)
                    .border(
                        width = 1.dp,
                        color = chipBorder,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable { onSelectCategory(spec.id) }
                    .testTag("smart_category_chip_${spec.id.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = spec.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = chipTextColor,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color.White.copy(alpha = 0.22f) else unselectedCountBg
                    ) {
                        Text(
                            text = "$matchingCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) Color.White else unselectedCountText,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelativeDateSectionHeader(
    dateGroupTitle: String,
    subtitleDate: String,
    groupPhotos: List<PhotoEntity>,
    selectedPhotoIds: Set<Long>,
    activeTheme: CompleteUiThemeSpec,
    onToggleSelectGroup: () -> Unit
) {
    val allSelectedInGroup = remember(groupPhotos, selectedPhotoIds) {
        groupPhotos.isNotEmpty() && groupPhotos.all { it.id in selectedPhotoIds }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
            .testTag("date_group_header_${dateGroupTitle.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accent Dot
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        when (dateGroupTitle) {
                            "Today" -> Color(0xFF10B981)
                            "Yesterday" -> activeTheme.primaryAccent
                            else -> activeTheme.secondaryAccent
                        }
                    )
            )
            Text(
                text = dateGroupTitle.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    letterSpacing = 0.7.sp
                ),
                color = activeTheme.textPrimary
            )
            if (subtitleDate.isNotBlank() && (dateGroupTitle == "Today" || dateGroupTitle == "Yesterday")) {
                Text(
                    text = "· $subtitleDate",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = activeTheme.textSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = activeTheme.surfaceVariant.copy(alpha = 0.78f)
            ) {
                Text(
                    text = "${groupPhotos.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = activeTheme.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Clean Date-Group Multi-Select Toggle ("Select" / "Selected")
        Surface(
            shape = RoundedCornerShape(50),
            color = if (allSelectedInGroup) {
                activeTheme.primaryAccent.copy(alpha = 0.22f)
            } else {
                activeTheme.surfaceVariant.copy(alpha = 0.65f)
            },
            border = if (allSelectedInGroup) {
                androidx.compose.foundation.BorderStroke(1.dp, activeTheme.primaryAccent)
            } else null,
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(50))
                .clickable { onToggleSelectGroup() }
                .testTag("select_group_${dateGroupTitle.lowercase().replace(" ", "_")}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (allSelectedInGroup) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select all in $dateGroupTitle",
                    tint = if (allSelectedInGroup) activeTheme.primaryAccent else activeTheme.textSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (allSelectedInGroup) "Selected" else "Select",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (allSelectedInGroup) activeTheme.primaryAccent else activeTheme.textSecondary
                )
            }
        }
    }
}

@Composable
private fun AiCompactStatusBanner(
    aiState: AiOrganizerUiState,
    activeTheme: CompleteUiThemeSpec,
    onCancelAiOrganize: () -> Unit,
    onDismissBanner: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = activeTheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeTheme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("ai_service_status_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statusText = when (aiState) {
                is AiOrganizerUiState.Running -> "AI organizing ${aiState.processedCount}/${aiState.totalCount}…"
                is AiOrganizerUiState.Completed -> "AI organized ${aiState.organizedCount} photos"
                is AiOrganizerUiState.Error -> aiState.message
                else -> ""
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = activeTheme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (aiState is AiOrganizerUiState.Running) onCancelAiOrganize() else onDismissBanner()
                },
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = activeTheme.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CompleteUiDesignSwitcherCard(
    activeTheme: CompleteUiThemeSpec,
    selectedCategory: Int,
    onSelectCategory: (Int) -> Unit,
    onSelectTheme: (CompleteUiThemeSpec) -> Unit
) {
    val displayedThemes = remember(selectedCategory) {
        when (selectedCategory) {
            1 -> AppThemeCatalog.lightThemes
            2 -> AppThemeCatalog.darkThemes
            else -> AppThemeCatalog.allThemes
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
            .testTag("complete_ui_switcher_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = activeTheme.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Theme Palette",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = activeTheme.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = activeTheme.primaryAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${displayedThemes.size} Available",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = activeTheme.primaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Category Filter Pills: All / Light / Dark (Full width segmented bar)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activeTheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeCategoryFilterPill(
                        label = "All (${AppThemeCatalog.allThemes.size})",
                        isSelected = selectedCategory == 0,
                        activeColor = activeTheme.primaryAccent,
                        textColor = activeTheme.textSecondary,
                        onClick = { onSelectCategory(0) },
                        tag = "theme_cat_all",
                        modifier = Modifier.weight(1f)
                    )
                    ThemeCategoryFilterPill(
                        label = "☀ Light (${AppThemeCatalog.lightThemes.size})",
                        isSelected = selectedCategory == 1,
                        activeColor = activeTheme.primaryAccent,
                        textColor = activeTheme.textSecondary,
                        onClick = { onSelectCategory(1) },
                        tag = "theme_cat_light",
                        modifier = Modifier.weight(1f)
                    )
                    ThemeCategoryFilterPill(
                        label = "☾ Dark (${AppThemeCatalog.darkThemes.size})",
                        isSelected = selectedCategory == 2,
                        activeColor = activeTheme.primaryAccent,
                        textColor = activeTheme.textSecondary,
                        onClick = { onSelectCategory(2) },
                        tag = "theme_cat_dark",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Themes List (Vertical list with clear 3-dot color swatches, non-truncating names, mode badges & active checkmark)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedThemes.forEach { themeItem ->
                    val isCurrent = themeItem.id == activeTheme.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) {
                            activeTheme.primaryAccent.copy(alpha = 0.10f)
                        } else {
                            activeTheme.surfaceVariant.copy(alpha = 0.55f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) activeTheme.primaryAccent else activeTheme.borderStroke
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectTheme(themeItem) }
                            .testTag("theme_option_${themeItem.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 3-color overlapping palette circles
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-6).dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.background)
                                        .border(1.dp, themeItem.borderStroke, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.primaryAccent)
                                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.secondaryAccent)
                                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                )
                            }

                            // Theme Name & Mode/Badge Label
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = themeItem.name.replace(" (Light)", "").replace(" (Dark)", ""),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isCurrent) activeTheme.primaryAccent else activeTheme.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = if (themeItem.isLight) {
                                            Icons.Default.LightMode
                                        } else {
                                            Icons.Default.DarkMode
                                        },
                                        contentDescription = null,
                                        tint = if (themeItem.isLight) Color(0xFFF59E0B) else activeTheme.primaryAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = "${themeItem.modeLabel} • ${themeItem.shortBadge}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp
                                    ),
                                    color = activeTheme.textSecondary
                                )
                            }

                            // Active Selection Indicator
                            if (isCurrent) {
                                Surface(
                                    shape = CircleShape,
                                    color = activeTheme.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected theme",
                                            tint = activeTheme.onPrimaryAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, activeTheme.borderStroke),
                                    modifier = Modifier.size(22.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCategoryFilterPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isSelected) LocalCompleteUiTheme.current.onPrimaryAccent else textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeAdaptiveShowcaseCard(
    photo: PhotoEntity,
    activeTheme: CompleteUiThemeSpec,
    gridColumns: Int = 3,
    isHeroFullSpan: Boolean,
    isCompactGrid: Boolean = false,
    isSquareGrid: Boolean = false,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onInspectExif: () -> Unit = {},
    onOpenDarkroom: () -> Unit = {},
    onOpenCropStudio: () -> Unit = {},
    onAssignCategoryClick: () -> Unit = {},
    onQuickRotate90: () -> Unit = {},
    onDuplicatePhoto: () -> Unit = {},
    onMoveToVault: () -> Unit = {},
    onMoveToTrash: () -> Unit = {},
    onRequestDeleteFromStorage: () -> Unit = {}
) {
    val cornerRadius = when {
        isSelected -> 16.dp
        gridColumns >= 5 -> 6.dp
        gridColumns == 4 -> 8.dp
        isCompactGrid -> 10.dp
        else -> 14.dp
    }
    val cardScale = if (isSelected) 0.94f else 1f
    val borderWidth = if (isSelected) 2.5.dp else 0.dp
    val borderColor = if (isSelected) activeTheme.primaryAccent else Color.Transparent
    val cardAspectRatio = when {
        isSquareGrid -> 1f
        isHeroFullSpan -> {
            if (photo.width > 0 && photo.height > 0) {
                (photo.width.toFloat() / photo.height.toFloat()).coerceIn(0.72f, 1.85f)
            } else {
                1.56f
            }
        }
        gridColumns <= 2 -> 0.85f
        else -> 0.82f
    }

    val context = LocalContext.current
    val thumbSizePx = remember(gridColumns) {
        when {
            gridColumns >= 5 -> 220
            gridColumns == 4 -> 260
            else -> 360
        }
    }
    val thumbnailRequest = remember(photo.id, photo.filePath, photo.sizeBytes, thumbSizePx) {
        PhotoImageProcessor.buildThumbnailRequest(context, photo.filePath, thumbSizePx, thumbSizePx)
    }
    var thumbnailDecodeFailed by remember(photo.id, photo.filePath) {
        mutableStateOf(false)
    }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompactGrid) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .aspectRatio(cardAspectRatio)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onClick()
                    }
                },
                onLongClick = { onToggleSelect() }
            )
            .testTag("photo_card_${photo.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (photo.filePath.isNotBlank() && !thumbnailDecodeFailed) {
                AsyncImage(
                    model = thumbnailRequest,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Crop,
                    onError = { thumbnailDecodeFailed = true },
                    onSuccess = { thumbnailDecodeFailed = false },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Clean fallback media placeholder whenever an image fails to decode or path is missing
            if (photo.filePath.isBlank() || thumbnailDecodeFailed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(activeTheme.surfaceVariant)
                        .testTag("fallback_media_icon_${photo.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (photo.isVideo) {
                                Icons.Default.Movie
                            } else {
                                Icons.Default.Image
                            },
                            contentDescription = "Media preview unavailable",
                            tint = activeTheme.textSecondary.copy(alpha = 0.72f),
                            modifier = Modifier.size(if (gridColumns >= 5) 20.dp else 26.dp)
                        )
                        if (gridColumns <= 3) {
                            Text(
                                text = if (photo.isVideo) "Video" else "Unavailable",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = activeTheme.textSecondary.copy(alpha = 0.65f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Subtle Top-Start Video Duration Badge (only for videos)
            if (photo.isVideo) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (gridColumns >= 5) 4.dp else 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (gridColumns >= 5) 5.dp else 7.dp,
                            vertical = if (gridColumns >= 5) 2.dp else 3.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(if (gridColumns >= 5) 10.dp else 12.dp)
                        )
                        Text(
                            text = photo.formattedDuration,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (gridColumns >= 5) 8.5.sp else 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Subtle Bottom-End Favorite Heart Badge (only when favorited)
            if (photo.isFavorite) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.48f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(if (gridColumns >= 5) 4.dp else 6.dp)
                        .size(if (gridColumns >= 5) 20.dp else 24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorited",
                            tint = Color(0xFFFF4D6D),
                            modifier = Modifier.size(if (gridColumns >= 5) 11.dp else 13.dp)
                        )
                    }
                }
            }

            // Selection Highlight Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(160)),
                exit = fadeOut(tween(160)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(activeTheme.primaryAccent.copy(alpha = 0.24f))
                )
            }

            // Clean selection check circle (only shown when selecting or in selection mode)
            if (isSelectionMode || isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (gridColumns >= 5) 4.dp else if (isCompactGrid) 5.dp else 8.dp)
                ) {
                    val checkColor = if (isSelected) activeTheme.primaryAccent else Color.Black.copy(alpha = 0.65f)
                    Surface(
                        shape = CircleShape,
                        color = checkColor,
                        modifier = Modifier
                            .size(if (gridColumns >= 5) 22.dp else if (isCompactGrid) 26.dp else 30.dp)
                            .border(
                                width = 1.2.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { onToggleSelect() }
                            .testTag("select_toggle_${photo.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSelected) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = if (isSelected) "Deselect photo" else "Select photo",
                                tint = if (isSelected) activeTheme.onPrimaryAccent else Color.White,
                                modifier = Modifier.size(if (gridColumns >= 5) 14.dp else if (isCompactGrid) 16.dp else 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumsGridScreen(
    albums: List<AlbumSummary>,
    allPhotos: List<PhotoEntity> = emptyList(),
    personClusters: List<PersonClusterEntity> = emptyList(),
    albumSortOption: AlbumSortOption = AlbumSortOption.DATE_DESC,
    onSelectAlbumSort: (AlbumSortOption) -> Unit = {},
    onAlbumClick: (String) -> Unit,
    onCreateAlbumClick: () -> Unit,
    onOpenPeopleDirectory: () -> Unit = {},
    onOpenGrowthTimeline: (PersonClusterEntity) -> Unit = {},
    onOpenPersonDetail: (PersonClusterEntity) -> Unit = {}
) {
    val activeTheme = LocalCompleteUiTheme.current
    val context = LocalContext.current
    val albumPrefs = remember(context) {
        context.getSharedPreferences("simple_gallery_prefs", android.content.Context.MODE_PRIVATE)
    }
    var pinnedAlbums by remember {
        mutableStateOf(com.example.ui.studio.StudioPreferenceStore.loadPinnedAlbums(context))
    }
    var isListView by remember {
        mutableStateOf(albumPrefs.getBoolean("albums_grid_list_view", false))
    }
    val orderedAlbums = remember(albums, pinnedAlbums) {
        albums.sortedWith(
            compareByDescending<AlbumSummary> { album ->
                pinnedAlbums.any { it.equals(album.name, ignoreCase = true) }
            }
        )
    }
    val previewPathsByAlbum = remember(allPhotos.size, allPhotos.firstOrNull()?.id) {
        val map = HashMap<String, MutableList<String>>()
        for (photo in allPhotos) {
            if (photo.filePath.isBlank()) continue
            val key = photo.albumName.lowercase()
            val list = map.getOrPut(key) { ArrayList(4) }
            if (list.size < 4 && !list.contains(photo.filePath)) {
                list.add(photo.filePath)
            }
        }
        map
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isListView) 1 else 2),
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.background)
            .testTag("albums_grid"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Albums",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = activeTheme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onCreateAlbumClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeTheme.primaryAccent,
                            contentColor = activeTheme.onPrimaryAccent
                        ),
                        modifier = Modifier.testTag("create_album_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Album")
                    }
                }

                // Interactive Albums Sort Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (albums.size == 1) "1 Album" else "${albums.size} Albums",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = activeTheme.textSecondary
                    )

                    var showAlbumSortMenu by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 2-Col Grid vs 1-Col Compact List View Toggle
                        Surface(
                            shape = CircleShape,
                            color = activeTheme.surfaceVariant.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val next = !isListView
                                    isListView = next
                                    albumPrefs.edit().putBoolean("albums_grid_list_view", next).commit()
                                }
                                .testTag("albums_view_mode_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListView) Icons.Default.GridView else Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = if (isListView) "Switch to 2-column grid" else "Switch to wide cards",
                                    tint = activeTheme.primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Box {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (showAlbumSortMenu) activeTheme.primaryAccent else activeTheme.surfaceVariant.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, activeTheme.borderStroke),
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showAlbumSortMenu = true }
                                    .testTag("albums_sort_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Sort albums",
                                        tint = if (showAlbumSortMenu) activeTheme.onPrimaryAccent else activeTheme.primaryAccent,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = albumSortOption.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (showAlbumSortMenu) activeTheme.onPrimaryAccent else activeTheme.textPrimary
                                    )
                                }
                            }

                            AlbumSortDropdownMenu(
                                expanded = showAlbumSortMenu,
                                onDismiss = { showAlbumSortMenu = false },
                                currentSort = albumSortOption,
                                onSelectSort = {
                                    onSelectAlbumSort(it)
                                    showAlbumSortMenu = false
                                },
                                activeTheme = activeTheme
                            )
                        }
                    }
                }

                // Featured Smart Cards (People & Pets + Growth Timelines)
                val growthPeople = remember(personClusters) {
                    personClusters.filter { it.hasGrowthTimeline }
                }
                val featuredChild = growthPeople.firstOrNull()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // People & Pets Smart Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenPeopleDirectory() }
                            .testTag("albums_smart_card_people")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-10).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatars = personClusters.take(3)
                                if (avatars.isNotEmpty()) {
                                    avatars.forEach { person ->
                                        AsyncImage(
                                            model = remember(person.avatarPhotoPath) {
                                                PhotoImageProcessor.buildThumbnailRequest(context, person.avatarPhotoPath, 128, 128)
                                            },
                                            contentDescription = person.displayTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, activeTheme.surface, CircleShape)
                                                .background(activeTheme.surfaceVariant)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = activeTheme.primaryAccent.copy(alpha = 0.14f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = activeTheme.primaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "People & Pets",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = activeTheme.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (personClusters.isEmpty()) "Tap Demo Photos to preview" else "${personClusters.size} face clusters",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.primaryAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Growth Timelines Smart Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (featuredChild != null) {
                                    onOpenGrowthTimeline(featuredChild)
                                } else {
                                    val fallback = personClusters.firstOrNull()
                                    if (fallback != null) {
                                        onOpenPersonDetail(fallback)
                                    } else {
                                        onOpenPeopleDirectory()
                                    }
                                }
                            }
                            .testTag("albums_smart_card_growth")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (featuredChild != null && featuredChild.avatarPhotoPath.isNotEmpty()) {
                                    AsyncImage(
                                        model = remember(featuredChild.avatarPhotoPath) {
                                            PhotoImageProcessor.buildThumbnailRequest(context, featuredChild.avatarPhotoPath, 128, 128)
                                        },
                                        contentDescription = featuredChild.displayTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, activeTheme.primaryAccent, CircleShape)
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = activeTheme.secondaryAccent.copy(alpha = 0.16f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = activeTheme.secondaryAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = activeTheme.primaryAccent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = if (featuredChild != null) "${growthPeople.size} Active" else "Storybook",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        ),
                                        color = activeTheme.primaryAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Growth Timelines",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = activeTheme.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (featuredChild != null) "${featuredChild.displayTitle} • Milestones" else "Age chapters & reels",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.secondaryAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        items(orderedAlbums, key = { it.name }) { album ->
            val isPinned = remember(album.name, pinnedAlbums) {
                pinnedAlbums.any { it.equals(album.name, ignoreCase = true) }
            }
            val albumPreviewPaths = remember(album.name, album.coverFilePath, previewPathsByAlbum) {
                val matching = previewPathsByAlbum[album.name.lowercase()].orEmpty()
                if (matching.isNotEmpty()) {
                    matching
                } else if (!album.coverFilePath.isNullOrBlank()) {
                    listOf(album.coverFilePath)
                } else {
                    emptyList()
                }
            }

            AlbumCard(
                album = album,
                previewPaths = albumPreviewPaths,
                isPinned = isPinned,
                isListView = isListView,
                activeTheme = activeTheme,
                onTogglePin = {
                    pinnedAlbums = com.example.ui.studio.StudioPreferenceStore.togglePinnedAlbum(context, album.name)
                },
                onClick = { onAlbumClick(album.name) }
            )
        }
    }
}

@Composable
private fun AlbumCard(
    album: AlbumSummary,
    previewPaths: List<String>,
    isPinned: Boolean = false,
    isListView: Boolean = false,
    activeTheme: CompleteUiThemeSpec,
    onTogglePin: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val cornerRadius = activeTheme.layoutStyle.cardCornerDp.dp
    val itemCountLabel = if (album.photoCount == 1) "1 Photo" else "${album.photoCount} Photos"

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .testTag("album_card_${album.name}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isListView) 2.2f else 1.18f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeTheme.surfaceVariant)
            ) {
                when {
                    previewPaths.size >= 2 -> {
                        // Rounded 2x2 photo collage grid preview
                        val collageSlots = remember(previewPaths) {
                            List(4) { idx -> previewPaths.getOrNull(idx) ?: previewPaths[idx % previewPaths.size] }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                AsyncImage(
                                    model = remember(collageSlots[0]) {
                                        PhotoImageProcessor.buildThumbnailRequest(context, collageSlots[0], 240, 240)
                                    },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                AsyncImage(
                                    model = remember(collageSlots[1]) {
                                        PhotoImageProcessor.buildThumbnailRequest(context, collageSlots[1], 240, 240)
                                    },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                AsyncImage(
                                    model = remember(collageSlots[2]) {
                                        PhotoImageProcessor.buildThumbnailRequest(context, collageSlots[2], 240, 240)
                                    },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                AsyncImage(
                                    model = remember(collageSlots[3]) {
                                        PhotoImageProcessor.buildThumbnailRequest(context, collageSlots[3], 240, 240)
                                    },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }

                    previewPaths.size == 1 -> {
                        // Latest photo thumbnail
                        val singleCoverPath = previewPaths.first()
                        AsyncImage(
                            model = remember(singleCoverPath) {
                                PhotoImageProcessor.buildThumbnailRequest(context, singleCoverPath, 360, 360)
                            },
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        // Single clean centered category icon (zero 2x2 clutter for empty albums)
                        val lowerName = album.name.lowercase()
                        val iconTint = activeTheme.primaryAccent
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = activeTheme.primaryAccent.copy(alpha = 0.12f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        lowerName.contains("landscape") || lowerName.contains("nature") -> {
                                            Canvas(modifier = Modifier.size(32.dp)) {
                                                val mountainPath = Path().apply {
                                                    moveTo(size.width * 0.08f, size.height * 0.82f)
                                                    lineTo(size.width * 0.42f, size.height * 0.24f)
                                                    lineTo(size.width * 0.62f, size.height * 0.54f)
                                                    lineTo(size.width * 0.76f, size.height * 0.36f)
                                                    lineTo(size.width * 0.94f, size.height * 0.82f)
                                                    close()
                                                }
                                                drawPath(mountainPath, color = iconTint, style = Stroke(width = 5f))
                                            }
                                        }
                                        lowerName.contains("travel") -> {
                                            Canvas(modifier = Modifier.size(32.dp)) {
                                                val cx = size.width / 2f
                                                val cy = size.height * 0.38f
                                                drawCircle(color = iconTint, radius = size.width * 0.22f, center = Offset(cx, cy), style = Stroke(width = 5f))
                                                drawCircle(color = iconTint, radius = size.width * 0.08f, center = Offset(cx, cy))
                                                val pinTail = Path().apply {
                                                    moveTo(cx - size.width * 0.18f, cy + size.height * 0.12f)
                                                    lineTo(cx, size.height * 0.88f)
                                                    lineTo(cx + size.width * 0.18f, cy + size.height * 0.12f)
                                                }
                                                drawPath(pinTail, color = iconTint, style = Stroke(width = 5f))
                                            }
                                        }
                                        lowerName.contains("keepsake") || lowerName.contains("family") -> {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                        lowerName.contains("camera") -> {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 1-Tap Pin / Favorite Album Button (top-left)
                Surface(
                    shape = CircleShape,
                    color = if (isPinned) activeTheme.primaryAccent else Color(0xFF1F1C1A).copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePin)
                        .testTag("album_pin_toggle_${album.name}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (isPinned) "Unpin album" else "Pin album to top",
                            tint = if (isPinned) Color.White else Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Subtle overlay count badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1F1C1A).copy(alpha = 0.76f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${album.photoCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = activeTheme.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = itemCountLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = activeTheme.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PhotoSortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentSort: SortOption,
    onSelectSort: (SortOption) -> Unit,
    activeTheme: CompleteUiThemeSpec
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(activeTheme.surface)
    ) {
        Text(
            text = "ORDER PHOTOS BY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = activeTheme.primaryAccent,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "DATE ADDED",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Newest First", fontWeight = if (currentSort == SortOption.DATE_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = if (currentSort == SortOption.DATE_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.DATE_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.DATE_DESC) }
        )
        DropdownMenuItem(
            text = { Text("Oldest First", fontWeight = if (currentSort == SortOption.DATE_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = if (currentSort == SortOption.DATE_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.DATE_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.DATE_ASC) }
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "NAME",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Name (A to Z)", fontWeight = if (currentSort == SortOption.NAME_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = if (currentSort == SortOption.NAME_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.NAME_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.NAME_ASC) }
        )
        DropdownMenuItem(
            text = { Text("Name (Z to A)", fontWeight = if (currentSort == SortOption.NAME_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = if (currentSort == SortOption.NAME_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.NAME_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.NAME_DESC) }
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "FILE SIZE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Largest Size", fontWeight = if (currentSort == SortOption.SIZE_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.DataUsage, contentDescription = null, tint = if (currentSort == SortOption.SIZE_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.SIZE_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.SIZE_DESC) }
        )
        DropdownMenuItem(
            text = { Text("Smallest Size", fontWeight = if (currentSort == SortOption.SIZE_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.DataUsage, contentDescription = null, tint = if (currentSort == SortOption.SIZE_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == SortOption.SIZE_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(SortOption.SIZE_ASC) }
        )
    }
}

@Composable
fun AlbumSortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentSort: AlbumSortOption,
    onSelectSort: (AlbumSortOption) -> Unit,
    activeTheme: CompleteUiThemeSpec
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(activeTheme.surface)
    ) {
        Text(
            text = "ORDER ALBUMS BY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = activeTheme.primaryAccent,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "DATE ADDED",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Newest Date Added", fontWeight = if (currentSort == AlbumSortOption.DATE_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = if (currentSort == AlbumSortOption.DATE_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.DATE_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.DATE_DESC) }
        )
        DropdownMenuItem(
            text = { Text("Oldest Date Added", fontWeight = if (currentSort == AlbumSortOption.DATE_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = if (currentSort == AlbumSortOption.DATE_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.DATE_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.DATE_ASC) }
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "NAME",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Name (A to Z)", fontWeight = if (currentSort == AlbumSortOption.NAME_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = if (currentSort == AlbumSortOption.NAME_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.NAME_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.NAME_ASC) }
        )
        DropdownMenuItem(
            text = { Text("Name (Z to A)", fontWeight = if (currentSort == AlbumSortOption.NAME_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = if (currentSort == AlbumSortOption.NAME_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.NAME_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.NAME_DESC) }
        )

        HorizontalDivider(color = activeTheme.borderStroke.copy(alpha = 0.5f))

        Text(
            text = "SIZE & COUNT",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = activeTheme.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("Largest Total Size", fontWeight = if (currentSort == AlbumSortOption.SIZE_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.DataUsage, contentDescription = null, tint = if (currentSort == AlbumSortOption.SIZE_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.SIZE_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.SIZE_DESC) }
        )
        DropdownMenuItem(
            text = { Text("Smallest Total Size", fontWeight = if (currentSort == AlbumSortOption.SIZE_ASC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.DataUsage, contentDescription = null, tint = if (currentSort == AlbumSortOption.SIZE_ASC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.SIZE_ASC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.SIZE_ASC) }
        )
        DropdownMenuItem(
            text = { Text("Most Photos", fontWeight = if (currentSort == AlbumSortOption.COUNT_DESC) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = if (currentSort == AlbumSortOption.COUNT_DESC) activeTheme.primaryAccent else activeTheme.textSecondary) },
            trailingIcon = if (currentSort == AlbumSortOption.COUNT_DESC) { { Icon(Icons.Default.Check, contentDescription = null, tint = activeTheme.primaryAccent) } } else null,
            onClick = { onSelectSort(AlbumSortOption.COUNT_DESC) }
        )
    }
}
