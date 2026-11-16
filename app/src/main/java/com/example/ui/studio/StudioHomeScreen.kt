package com.example.ui.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioHomeScreen(
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
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
    val scrollState = rememberScrollState()
    var selectedPhotoId by remember(photos) {
        mutableStateOf(photos.firstOrNull()?.id)
    }
    val activePhoto = photos.firstOrNull { it.id == selectedPhotoId } ?: photos.firstOrNull()

    val recentEdits = photos.filter {
        it.appliedFilter != "Original" ||
            it.albumName.contains("Edited", ignoreCase = true) ||
            it.albumName.contains("Collage", ignoreCase = true) ||
            it.albumName.contains("Studio", ignoreCase = true) ||
            it.tags.contains("Edited", ignoreCase = true)
    }.take(8).ifEmpty { photos.take(8) }

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
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp)
            .testTag("studio_home_screen")
    ) {
        // 1. Standardized Studio Header with subtle Ready badge & Import Actions
        StudioHomeHeaderBanner(
            totalPhotosCount = photos.size,
            editedCount = recentEdits.size,
            onPickClick = onPickFromStorage,
            onCameraClick = onCaptureCamera
        )

        // 2. Active Photo Selector Strip (choose which photo to edit)
        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = NeonGreenAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Active Photo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextWhite
                    )
                }
                Text(
                    text = activePhoto?.title ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = NeonGreenAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_target_photo_selector")
            ) {
                items(photos.take(30), key = { it.id }) { item ->
                    val isSelected = item.id == activePhoto?.id
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) NeonGreenAccent else DarkGraphiteBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPhotoId = item.id }
                            .testTag("studio_select_photo_${item.id}")
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(item.filePath),
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

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Section: Photo Editing Suite (Concise, Non-Truncating Titles)
        StudioSectionTitle(title = "Darkroom & Color", icon = Icons.Default.Palette)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            StudioToolCardDetailed(
                title = "Exposure",
                description = "Brightness, contrast & highlights",
                icon = Icons.Default.Tune,
                accentColor = NeonGreenAccent,
                onClick = { onLaunchEditor(activePhoto, EditorToolTab.LIGHT) },
                testTag = "studio_card_edit_photo",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Crop & Rotate",
                description = "Aspect ratios, 90° turn & flip",
                icon = Icons.Default.Crop,
                accentColor = GoldAccent,
                onClick = { onLaunchEditor(activePhoto, EditorToolTab.CROP) },
                testTag = "studio_card_crop",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Color Balance",
                description = "Warmth, tint & saturation",
                icon = Icons.Default.Palette,
                accentColor = CyanAccent,
                onClick = { onLaunchEditor(activePhoto, EditorToolTab.COLOR) },
                testTag = "studio_card_color",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Film LUTs",
                description = "42 analog & cinema grades",
                icon = Icons.Default.Movie,
                accentColor = Color(0xFFEC4899),
                onClick = { onLaunchEditor(activePhoto, EditorToolTab.FILTERS) },
                testTag = "studio_card_filters",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Vignette & Grain",
                description = "Optical shading & film texture",
                icon = Icons.Default.FilterVintage,
                accentColor = Color(0xFF10B981),
                onClick = { onLaunchEditor(activePhoto, EditorToolTab.DETAIL) },
                testTag = "studio_card_creative_fx",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "White Balance",
                description = "Kelvin temp & shadow recovery",
                icon = Icons.Default.Memory,
                accentColor = Color(0xFFF43F5E),
                onClick = { onLaunchRawEditor(activePhoto) },
                testTag = "studio_card_raw",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Section: Creative & Batch Studio
        StudioSectionTitle(title = "Creative & Batch", icon = Icons.Default.AutoAwesomeMosaic)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            StudioToolCardDetailed(
                title = "Collage",
                description = "Grid, bento & filmstrip layouts",
                icon = Icons.Default.AutoAwesomeMosaic,
                accentColor = NeonGreenAccent,
                onClick = onLaunchCollage,
                testTag = "studio_card_collage",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Batch Filters",
                description = "Bulk LUT grades, crop & rotate",
                icon = Icons.Default.Layers,
                accentColor = CyanAccent,
                onClick = { onLaunchBatch(BatchActionMode.BATCH_EDIT) },
                testTag = "studio_card_batch",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "EXIF & Camera Frame",
                description = "Gallery borders stamped with camera or child age",
                icon = Icons.Default.BrandingWatermark,
                accentColor = GoldAccent,
                onClick = { onOpenExifFrameStudio(activePhoto) },
                testTag = "studio_card_exif_frame",
                modifier = Modifier.weight(1f)
            )
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
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            StudioToolCardDetailed(
                title = "Private Vault",
                description = if (vaultPhotosCount > 0) "$vaultPhotosCount photos locked" else "PIN protected gallery",
                icon = Icons.Default.Lock,
                accentColor = GoldAccent,
                onClick = onLaunchVault,
                testTag = "studio_card_vault",
                modifier = Modifier.weight(1f)
            )
            StudioToolCardDetailed(
                title = "Recycle Bin",
                description = if (trashedPhotosCount > 0) "$trashedPhotosCount deleted photos" else "Empty recycle bin",
                icon = Icons.Default.DeleteOutline,
                accentColor = Color(0xFFEF4444),
                onClick = onLaunchRecycleBin,
                testTag = "studio_card_recycle_bin",
                modifier = Modifier.weight(1f)
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
    editedCount: Int,
    onPickClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
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
                    color = NeonGreenDim,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        NeonGreenAccent.copy(alpha = 0.35f)
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
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonGreenAccent
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = LocalCompleteUiTheme.current.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGraphiteBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPickClick)
                        .testTag("studio_pick_storage_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Pick Photo",
                            tint = NeonGreenAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = LocalCompleteUiTheme.current.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGraphiteBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCameraClick)
                        .testTag("studio_quick_camera_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Photo",
                            tint = NeonGreenAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
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
            .border(1.dp, DarkGraphiteBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp),
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
                    softWrap = false
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted,
                    maxLines = 2,
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
                    model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
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
