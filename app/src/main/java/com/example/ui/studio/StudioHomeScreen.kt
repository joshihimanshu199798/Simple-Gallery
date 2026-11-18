package com.example.ui.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.CompleteUiThemeSpec
import com.example.ui.theme.LocalCompleteUiTheme

// Theme-Adaptive Studio Palette
private val DarkGraphiteBackground: Color
    @Composable get() = LocalCompleteUiTheme.current.background
private val DarkGraphiteCard: Color
    @Composable get() = LocalCompleteUiTheme.current.surface
private val DarkGraphiteBorder: Color
    @Composable get() = LocalCompleteUiTheme.current.borderStroke
private val NeonGreenAccent: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent
private val NeonGreenDim: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent.copy(alpha = 0.14f)
private val TextWhite: Color
    @Composable get() = LocalCompleteUiTheme.current.textPrimary
private val TextMuted: Color
    @Composable get() = LocalCompleteUiTheme.current.textSecondary
private val GoldAccent: Color
    @Composable get() = LocalCompleteUiTheme.current.secondaryAccent
private val CyanAccent: Color
    @Composable get() = LocalCompleteUiTheme.current.tertiaryAccent

@Composable
fun StudioHomeScreen(
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    selectedPhoto: PhotoEntity? = null,
    onSelectPhoto: (PhotoEntity) -> Unit = {},
    onLaunchEditor: (photo: PhotoEntity?, initialTab: EditorToolTab) -> Unit,
    onLaunchCollage: () -> Unit,
    onLaunchCompression: (photo: PhotoEntity?) -> Unit,
    onLaunchBatch: (mode: BatchActionMode) -> Unit = {},
    onLaunchRawEditor: (photo: PhotoEntity?) -> Unit,
    onPickFromStorage: () -> Unit,
    onCaptureCamera: () -> Unit,
    onOpenCompare: () -> Unit = {},
    onOpenWatermark: () -> Unit = {},
    onOpenExifFrameStudio: (PhotoEntity?) -> Unit = {},
    onLaunchStorageAnalysis: () -> Unit = {},
    vaultPhotosCount: Int = 0,
    trashedPhotosCount: Int = 0,
    onLaunchVault: () -> Unit = {},
    onLaunchRecycleBin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedPhotoId by remember {
        mutableStateOf(selectedPhoto?.id ?: photos.firstOrNull()?.id)
    }
    LaunchedEffect(selectedPhoto?.id) {
        if (selectedPhoto != null) {
            selectedPhotoId = selectedPhoto.id
        }
    }
    LaunchedEffect(photos.size) {
        if (selectedPhotoId == null || photos.none { it.id == selectedPhotoId }) {
            selectedPhotoId = photos.firstOrNull()?.id
        }
    }
    val activePhoto = photos.firstOrNull { it.id == selectedPhotoId } ?: selectedPhoto ?: photos.firstOrNull()
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

    val recentEdits = remember(photos) {
        photos.filter {
            it.appliedFilter != "Original" ||
                it.albumName.contains("Edited", ignoreCase = true) ||
                it.albumName.contains("Collage", ignoreCase = true) ||
                it.albumName.contains("Studio", ignoreCase = true) ||
                it.tags.contains("Edited", ignoreCase = true)
        }.take(8).ifEmpty { photos.take(8) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkGraphiteBackground,
                        LocalCompleteUiTheme.current.backgroundSecondary
                    )
                )
            )
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 148.dp)
            .testTag("studio_home_screen")
    ) {
        // 1. Clean Studio Header (Title + Ready pill + Library summary only)
        StudioHomeHeaderBanner(
            totalPhotosCount = photos.size,
            editedCount = recentEdits.size
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Target Photo Selection Card (compact single-row empty state + 3-button action bar)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGraphiteCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, NeonGreenAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .testTag("studio_active_photo_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (photos.isNotEmpty()) {
                                showPhotoPickerSheet = true
                            } else {
                                onPickFromStorage()
                            }
                        }
                ) {
                    if (activePhoto != null) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalCompleteUiTheme.current.surfaceVariant)
                                .border(1.5.dp, NeonGreenAccent, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val thumbReq = remember(activePhoto.id, activePhoto.filePath) {
                                PhotoImageProcessor.buildThumbnailRequest(context, activePhoto.filePath, 200, 200)
                            }
                            AsyncImage(
                                model = thumbReq,
                                contentDescription = activePhoto.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonGreenDim
                                ) {
                                    Text(
                                        text = "SELECTED PHOTO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = NeonGreenAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "• ${photos.size} in library",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activePhoto.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${activePhoto.albumName} • ${activePhoto.resolutionText} • ${activePhoto.formattedSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(NeonGreenDim)
                                .border(1.dp, NeonGreenAccent.copy(alpha = 0.5f), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "No photo selected",
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELECTED PHOTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = NeonGreenAccent,
                                maxLines = 1
                            )
                            Text(
                                text = "No Photo Selected Yet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Clean 3-button Action Row (exclusively inside the Selected Photo card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (photos.isNotEmpty()) {
                                showPhotoPickerSheet = true
                            } else {
                                onPickFromStorage()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreenAccent,
                            contentColor = LocalCompleteUiTheme.current.onPrimaryAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("studio_open_photo_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (photos.isNotEmpty()) "Select Photo (${photos.size})" else "Select Photo",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onPickFromStorage,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkGraphiteBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("studio_pick_device_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = NeonGreenAccent,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Device",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextWhite
                        )
                    }

                    OutlinedButton(
                        onClick = onCaptureCamera,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkGraphiteBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("studio_camera_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            tint = NeonGreenAccent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Quick Horizontal Strip of Library Photos
                if (photos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_target_photo_selector")
                    ) {
                        item(key = "browse_all_tile") {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonGreenDim)
                                    .border(1.dp, NeonGreenAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .clickable { showPhotoPickerSheet = true }
                                    .testTag("studio_browse_all_photos_tile"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Browse All Photos",
                                        tint = NeonGreenAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "All (${photos.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = NeonGreenAccent
                                    )
                                }
                            }
                        }

                        items(photos.take(60), key = { it.id }) { item ->
                            val isSelected = item.id == activePhoto?.id
                            val thumbReq = remember(item.id, item.filePath) {
                                PhotoImageProcessor.buildThumbnailRequest(context, item.filePath, 180, 180)
                            }
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) NeonGreenAccent else DarkGraphiteBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedPhotoId = item.id
                                        onSelectPhoto(item)
                                    }
                                    .testTag("studio_select_photo_${item.id}")
                            ) {
                                AsyncImage(
                                    model = thumbReq,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        color = NeonGreenAccent,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = LocalCompleteUiTheme.current.onPrimaryAccent,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPhotoPickerSheet) {
            StudioPhotoPickerSheet(
                photos = photos,
                selectedIds = setOfNotNull(activePhoto?.id),
                isMultiSelect = false,
                title = "Select Photo for Studio",
                onDismiss = { showPhotoPickerSheet = false },
                onSelectSinglePhoto = { chosen ->
                    selectedPhotoId = chosen.id
                    onSelectPhoto(chosen)
                    showPhotoPickerSheet = false
                },
                onPickFromDevice = {
                    showPhotoPickerSheet = false
                    onPickFromStorage()
                },
                onCaptureCamera = {
                    showPhotoPickerSheet = false
                    onCaptureCamera()
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Section: Darkroom & Color (Uniform 3x2 Equal-Height Grid)
        StudioSectionTitle(title = "Darkroom & Color", icon = Icons.Default.Palette)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolCardDetailed(
                    title = "Exposure",
                    description = "Brightness & contrast",
                    icon = Icons.Default.Tune,
                    accentColor = NeonGreenAccent,
                    onClick = { onLaunchEditor(activePhoto, EditorToolTab.LIGHT) },
                    testTag = "studio_card_edit_photo",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                StudioToolCardDetailed(
                    title = "Crop & Rotate",
                    description = "Aspect ratio & flip",
                    icon = Icons.Default.Crop,
                    accentColor = GoldAccent,
                    onClick = { onLaunchEditor(activePhoto, EditorToolTab.CROP) },
                    testTag = "studio_card_crop",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolCardDetailed(
                    title = "Color Balance",
                    description = "Warmth, tint & color",
                    icon = Icons.Default.Palette,
                    accentColor = CyanAccent,
                    onClick = { onLaunchEditor(activePhoto, EditorToolTab.COLOR) },
                    testTag = "studio_card_color",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                StudioToolCardDetailed(
                    title = "Film LUTs",
                    description = "43 analog & cinema",
                    icon = Icons.Default.Movie,
                    accentColor = Color(0xFFEC4899),
                    onClick = { onLaunchEditor(activePhoto, EditorToolTab.FILTERS) },
                    testTag = "studio_card_filters",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolCardDetailed(
                    title = "Vignette & Grain",
                    description = "Shading & film grain",
                    icon = Icons.Default.FilterVintage,
                    accentColor = Color(0xFF22C55E),
                    onClick = { onLaunchEditor(activePhoto, EditorToolTab.DETAIL) },
                    testTag = "studio_card_creative_fx",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                StudioToolCardDetailed(
                    title = "White Balance",
                    description = "Kelvin & shadows",
                    icon = Icons.Default.Memory,
                    accentColor = Color(0xFFF43F5E),
                    onClick = { onLaunchRawEditor(activePhoto) },
                    testTag = "studio_card_raw",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Section: Creative & Batch Studio (Balanced 2x2 Equal-Height Grid)
        StudioSectionTitle(title = "Creative & Batch", icon = Icons.Default.AutoAwesomeMosaic)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolCardDetailed(
                    title = "Collage",
                    description = "Grid & bento layouts",
                    icon = Icons.Default.AutoAwesomeMosaic,
                    accentColor = NeonGreenAccent,
                    onClick = onLaunchCollage,
                    testTag = "studio_card_collage",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                StudioToolCardDetailed(
                    title = "Batch Filters",
                    description = "Bulk LUTs & resize",
                    icon = Icons.Default.Layers,
                    accentColor = CyanAccent,
                    onClick = { onLaunchBatch(BatchActionMode.BATCH_EDIT) },
                    testTag = "studio_card_batch",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioToolCardDetailed(
                    title = "EXIF & Camera Frame",
                    description = "Gallery mat & stamps",
                    icon = Icons.Default.BrandingWatermark,
                    accentColor = GoldAccent,
                    onClick = { onOpenExifFrameStudio(activePhoto) },
                    testTag = "studio_card_exif_frame",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                StudioToolCardDetailed(
                    title = "Before / After",
                    description = "Compare original edit",
                    icon = Icons.Default.Compare,
                    accentColor = Color(0xFF38BDF8),
                    onClick = onOpenCompare,
                    testTag = "studio_card_compare",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Section: Storage & Format Utilities
        StudioSectionTitle(title = "Storage & Format Utilities", icon = Icons.Default.Compress)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioAiToolRow(
                title = "Smart Image Compression",
                subtitle = "Reduce photo file size up to 80% with adjustable quality & scale",
                badge = "JPEG • WEBP • PNG",
                isOnline = false,
                icon = Icons.Default.Compress,
                testTag = "studio_card_compress",
                onClick = { onLaunchCompression(activePhoto) }
            )
            StudioAiToolRow(
                title = "Batch Resize Studio",
                subtitle = "Scale resolution by custom percentage (10%-100%) or 720p/1080p/2K presets",
                badge = "RESIZE",
                isOnline = false,
                icon = Icons.Default.Crop,
                testTag = "studio_storage_resize",
                onClick = { onLaunchBatch(BatchActionMode.RESIZE_50) }
            )
            StudioAiToolRow(
                title = "Batch Format Converter",
                subtitle = "Convert multiple photos into JPEG, WebP, or lossless PNG with custom quality",
                badge = "CONVERT",
                isOnline = false,
                icon = Icons.Default.Transform,
                testTag = "studio_storage_convert",
                onClick = { onLaunchBatch(BatchActionMode.CONVERT_JPEG) }
            )
            StudioAiToolRow(
                title = "Storage & Duplicate Cleaner",
                subtitle = "Scan similar bursts, duplicate files, and heavy (> 4 MB) masters",
                badge = "CLEANER",
                isOnline = false,
                icon = Icons.Default.PieChart,
                testTag = "studio_storage_analysis",
                onClick = onLaunchStorageAnalysis
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Section: Privacy & Recycle Bin
        StudioSectionTitle(title = "Privacy & Recycle Bin", icon = Icons.Default.Lock)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StudioToolCardDetailed(
                title = "Private Vault",
                description = if (vaultPhotosCount > 0) "$vaultPhotosCount locked" else "PIN protected gallery",
                icon = Icons.Default.Lock,
                accentColor = GoldAccent,
                onClick = onLaunchVault,
                testTag = "studio_card_vault",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StudioToolCardDetailed(
                title = "Recycle Bin",
                description = if (trashedPhotosCount > 0) "$trashedPhotosCount deleted" else "Empty recycle bin",
                icon = Icons.Default.DeleteOutline,
                accentColor = Color(0xFFEF4444),
                onClick = onLaunchRecycleBin,
                testTag = "studio_card_recycle_bin",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Section: Recently Edited Photos
        if (recentEdits.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NeonGreenAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Recent Studio Edits",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextWhite
                    )
                }
                Text(
                    text = "${recentEdits.size} Photos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("studio_recent_edits_row")
            ) {
                items(recentEdits, key = { it.id }) { photo ->
                    RecentEditItemCard(
                        photo = photo,
                        onClick = { onLaunchEditor(photo, EditorToolTab.LIGHT) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioHomeHeaderBanner(
    totalPhotosCount: Int,
    editedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Studio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = TextWhite
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF22C55E).copy(alpha = 0.14f),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF22C55E).copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Text(
                        text = "Ready",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }

        Text(
            text = "$totalPhotosCount Library Photos · $editedCount Studio Edits",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun StudioSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonGreenAccent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextWhite
        )
    }
}

@Composable
private fun StudioToolCardDetailed(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraphiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .heightIn(min = 68.dp)
            .border(1.dp, DarkGraphiteBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = accentColor.copy(alpha = 0.14f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = TextWhite,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StudioAiToolRow(
    title: String,
    subtitle: String,
    badge: String,
    isOnline: Boolean,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraphiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkGraphiteBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOnline) CyanAccent.copy(alpha = 0.16f) else NeonGreenDim,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isOnline) CyanAccent else NeonGreenAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isOnline) CyanAccent.copy(alpha = 0.18f) else NeonGreenDim
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isOnline) CyanAccent else NeonGreenAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentEditItemCard(
    photo: PhotoEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val thumbReq = remember(photo.id, photo.filePath) {
        PhotoImageProcessor.buildThumbnailRequest(context, photo.filePath, 220, 220)
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraphiteCard),
        modifier = Modifier
            .width(114.dp)
            .border(1.dp, DarkGraphiteBorder, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("recent_edit_${photo.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = thumbReq,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    color = Color.Black.copy(alpha = 0.72f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = photo.appliedFilter.take(8),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NeonGreenAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = photo.resolutionText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Reusable Full-Library Photo Picker Bottom Sheet for the Studio tab.
 * Supports both single-photo selection (Editor, RAW, EXIF Frame) and multi-photo selection
 * (Collage, Batch Filters, Batch Resize/Convert, Compression) with search, album filter chips,
 * and direct device storage / camera import buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioPhotoPickerSheet(
    photos: List<PhotoEntity>,
    selectedIds: Set<Long>,
    isMultiSelect: Boolean = false,
    maxSelection: Int = 500,
    title: String = "Select Photo",
    onDismiss: () -> Unit,
    onSelectSinglePhoto: (PhotoEntity) -> Unit = {},
    onConfirmMultiSelect: (List<PhotoEntity>) -> Unit = {},
    onPickFromDevice: (() -> Unit)? = null,
    onCaptureCamera: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedAlbumFilter by remember { mutableStateOf("All") }

    val multiSelectedIds = remember(selectedIds) {
        mutableStateListOf<Long>().apply { addAll(selectedIds) }
    }

    val albumFilters = remember(photos) {
        val albums = photos.map { it.albumName }.filter { it.isNotBlank() }.distinct()
        listOf("All", "Favorites") + albums
    }

    val filteredPhotos = remember(photos, searchQuery, selectedAlbumFilter) {
        val q = searchQuery.trim().lowercase()
        photos.filter { photo ->
            val matchesAlbum = when (selectedAlbumFilter) {
                "All" -> true
                "Favorites" -> photo.isFavorite
                else -> photo.albumName.equals(selectedAlbumFilter, ignoreCase = true)
            }
            val matchesQuery = q.isEmpty() ||
                photo.title.lowercase().contains(q) ||
                photo.fileName.lowercase().contains(q) ||
                photo.albumName.lowercase().contains(q) ||
                photo.dateHeader.lowercase().contains(q) ||
                photo.tags.lowercase().contains(q)
            matchesAlbum && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkGraphiteCard,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
                .testTag("studio_photo_picker_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextWhite
                    )
                    Text(
                        text = if (isMultiSelect) {
                            "${multiSelectedIds.size} selected • ${filteredPhotos.size} available"
                        } else {
                            "Tap any photo to load in Studio (${filteredPhotos.size} photos)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreenAccent
                    )
                }

                if (isMultiSelect) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LocalCompleteUiTheme.current.surfaceVariant,
                            border = BorderStroke(1.dp, DarkGraphiteBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    multiSelectedIds.clear()
                                    multiSelectedIds.addAll(filteredPhotos.take(maxSelection).map { it.id })
                                }
                        ) {
                            Text(
                                text = "Select All",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreenAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LocalCompleteUiTheme.current.surfaceVariant,
                            border = BorderStroke(1.dp, DarkGraphiteBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { multiSelectedIds.clear() }
                        ) {
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                    }
                }
            }

            // Device Import & Camera Row (if callbacks provided)
            if (onPickFromDevice != null || onCaptureCamera != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onPickFromDevice != null) {
                        OutlinedButton(
                            onClick = onPickFromDevice,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NeonGreenAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import from Device",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                    }
                    if (onCaptureCamera != null) {
                        OutlinedButton(
                            onClick = onCaptureCamera,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkGraphiteBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = NeonGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhite
                            )
                        }
                    }
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search by name, album, date, or tag...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NeonGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreenAccent,
                    unfocusedBorderColor = DarkGraphiteBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Album Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(albumFilters, key = { it }) { album ->
                    val isSelected = selectedAlbumFilter == album
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) NeonGreenAccent else LocalCompleteUiTheme.current.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) NeonGreenAccent else DarkGraphiteBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedAlbumFilter = album }
                    ) {
                        Text(
                            text = album,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) LocalCompleteUiTheme.current.onPrimaryAccent else TextWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3-Column Fast Thumbnail Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = filteredPhotos,
                    key = { it.id },
                    contentType = { "studio_picker_thumb" }
                ) { item ->
                    val isSelected = if (isMultiSelect) {
                        item.id in multiSelectedIds
                    } else {
                        item.id in selectedIds
                    }
                    val thumbReq = remember(item.id, item.filePath) {
                        PhotoImageProcessor.buildThumbnailRequest(context, item.filePath, 260, 260)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) NeonGreenAccent else DarkGraphiteBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isMultiSelect) {
                                    if (item.id in multiSelectedIds) {
                                        multiSelectedIds.remove(item.id)
                                    } else if (multiSelectedIds.size < maxSelection) {
                                        multiSelectedIds.add(item.id)
                                    }
                                } else {
                                    onSelectSinglePhoto(item)
                                }
                            }
                            .testTag("studio_picker_item_${item.id}")
                    ) {
                        AsyncImage(
                            model = thumbReq,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom title gradient
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .background(NeonGreenAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = LocalCompleteUiTheme.current.onPrimaryAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Confirm Multi-Select Footer Button
            if (isMultiSelect) {
                Button(
                    onClick = {
                        val chosen = photos.filter { it.id in multiSelectedIds }
                        onConfirmMultiSelect(chosen)
                    },
                    enabled = multiSelectedIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreenAccent,
                        contentColor = LocalCompleteUiTheme.current.onPrimaryAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("studio_picker_confirm_multi_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Use Selected Photos (${multiSelectedIds.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

