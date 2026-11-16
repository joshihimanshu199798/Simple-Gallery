package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.service.AI_GALLERY_CATEGORIES
import com.example.service.AiOrganizerUiState
import com.example.service.AiPhotoAnalysisResult
import com.example.ui.theme.CompleteUiThemeSpec

/**
 * Compact AI Smart Categories & Background Organizer status bar shown on the main Gallery Timeline.
 */
@Composable
fun AiSmartCategoriesStripAndBanner(
    allPhotos: List<PhotoEntity>,
    selectedTag: String,
    aiState: AiOrganizerUiState,
    activeTheme: CompleteUiThemeSpec,
    onSelectCategoryTag: (String) -> Unit,
    onStartAiOrganize: () -> Unit,
    onCancelAiOrganize: () -> Unit,
    onDismissBanner: () -> Unit
) {
    // Count how many photos match each smart category (either via albumName or tagList)
    val categoryCounts = remember(allPhotos) {
        val map = mutableMapOf<String, Int>()
        for (cat in AI_GALLERY_CATEGORIES) {
            val count = allPhotos.count { photo ->
                photo.albumName.equals(cat, ignoreCase = true) ||
                    photo.tagList.any { it.equals(cat, ignoreCase = true) }
            }
            map[cat] = count
        }
        map
    }

    val primaryDisplayCategories = remember {
        listOf("All", "Food", "Travel", "Portraits", "Nature", "Family", "Architecture")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_smart_categories_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Smart Categories Filter Strip + "✨ AI Organize" Action Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = activeTheme.primaryAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "AI SMART CATEGORIES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    ),
                    color = activeTheme.textSecondary
                )
            }

            val isRunning = aiState is AiOrganizerUiState.Running
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isRunning) activeTheme.secondaryAccent else activeTheme.primaryAccent.copy(alpha = 0.14f),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isRunning) activeTheme.secondaryAccent else activeTheme.primaryAccent,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (isRunning) onCancelAiOrganize() else onStartAiOrganize()
                    }
                    .testTag("timeline_ai_organize_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = activeTheme.onSecondaryAccent
                        )
                        Text(
                            text = "STOP AI SCAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = activeTheme.onSecondaryAccent
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Auto-Tag & Organize with AI",
                            tint = activeTheme.primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AI AUTO-TAG & ORGANIZE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = activeTheme.primaryAccent
                        )
                    }
                }
            }
        }

        // Category Filter Chips ('All', 'Food', 'Travel', 'Portraits', 'Nature', 'Family', 'Architecture')
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(primaryDisplayCategories, key = { "ai_cat_$it" }) { category ->
                val isSelected = selectedTag.equals(category, ignoreCase = true)
                val count = if (category == "All") allPhotos.size else (categoryCounts[category] ?: 0)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategoryTag(category) },
                    label = {
                        Text(
                            text = if (count > 0) "$category ($count)" else category,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeTheme.primaryAccent,
                        selectedLabelColor = activeTheme.onPrimaryAccent
                    ),
                    modifier = Modifier.testTag("ai_category_chip_${category.lowercase()}")
                )
            }
        }

        // 2. Live Background Service Progress / Completion / Error Status Card
        AnimatedVisibility(visible = aiState !is AiOrganizerUiState.Idle) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = when (aiState) {
                            is AiOrganizerUiState.Error -> MaterialTheme.colorScheme.error
                            is AiOrganizerUiState.Completed -> activeTheme.secondaryAccent
                            else -> activeTheme.primaryAccent
                        },
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("ai_service_status_banner")
            ) {
                when (aiState) {
                    is AiOrganizerUiState.Running -> {
                        val fraction = if (aiState.totalCount > 0) {
                            aiState.processedCount.toFloat() / aiState.totalCount.toFloat()
                        } else 0f
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.2.dp,
                                        color = activeTheme.primaryAccent
                                    )
                                    Column {
                                        Text(
                                            text = "AI Background Service Analyzing (${aiState.processedCount}/${aiState.totalCount})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = activeTheme.textPrimary
                                        )
                                        Text(
                                            text = "Inspecting: ${aiState.currentPhotoTitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = activeTheme.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = onCancelAiOrganize,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Stop", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            LinearProgressIndicator(
                                progress = { fraction.coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = activeTheme.primaryAccent,
                                trackColor = activeTheme.surfaceVariant
                            )
                            aiState.latestResult?.let { latest ->
                                Text(
                                    text = "Latest: '${latest.photoTitle}' → ${latest.primaryCategory} (${latest.tags.joinToString(", ")})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.primaryAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    is AiOrganizerUiState.Completed -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = activeTheme.secondaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "AI Organized ${aiState.organizedCount} Photo(s)!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = activeTheme.textPrimary
                                    )
                                    val summary = aiState.categoryCounts.entries
                                        .joinToString(" • ") { "${it.key}: ${it.value}" }
                                        .ifEmpty { "All photos tagged & categorized" }
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = activeTheme.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = onDismissBanner) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = activeTheme.textSecondary
                                )
                            }
                        }
                    }

                    is AiOrganizerUiState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "AI Background Organizer Error",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = aiState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = activeTheme.textPrimary
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onStartAiOrganize) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry AI Scan",
                                        tint = activeTheme.primaryAccent
                                    )
                                }
                                IconButton(onClick = onDismissBanner) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = activeTheme.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    AiOrganizerUiState.Idle -> Unit
                }
            }
        }
    }
}

/**
 * Full AI Photo Categorizer & Background Service management view inside Studio Tools.
 */
@Composable
fun AiSmartOrganizerHubSection(
    allPhotos: List<PhotoEntity>,
    aiState: AiOrganizerUiState,
    recentResults: List<AiPhotoAnalysisResult>,
    moveIntoCategoryAlbums: Boolean,
    autoTagOnImport: Boolean,
    activeTheme: CompleteUiThemeSpec,
    onStartOrganizeAll: (onlyUntagged: Boolean) -> Unit,
    onAnalyzeSinglePhoto: (PhotoEntity) -> Unit,
    onCancelService: () -> Unit,
    onToggleMoveIntoAlbums: () -> Unit,
    onToggleAutoTagOnImport: () -> Unit,
    onOpenLightbox: (PhotoEntity) -> Unit
) {
    val isRunning = aiState is AiOrganizerUiState.Running
    val aiTaggedCount = remember(allPhotos) {
        allPhotos.count { photo ->
            photo.tagList.any { it.equals("AI Tagged", ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_organizer_hub_section"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Main AI Background Service Control Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = activeTheme.primaryAccent.copy(alpha = 0.16f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = activeTheme.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Background Photo Organizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = "Powered by Gemini 3.8 Flash • $aiTaggedCount/${allPhotos.size} photos AI-tagged",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "Automatically analyzes local photos in a background Android Service and organizes them into categories like 'Food', 'Travel', 'Portraits', 'Nature', 'Family', and 'Architecture' with rich visual tags and captions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeTheme.textSecondary
                    )

                    // Toggles for Background Service behavior
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Move into Category Albums",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = "Organize photos directly into 'Food', 'Travel', 'Portraits' albums",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                        Switch(
                            checked = moveIntoCategoryAlbums,
                            onCheckedChange = { onToggleMoveIntoAlbums() },
                            modifier = Modifier.testTag("switch_ai_move_albums")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Auto-Tag on Import",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = "Automatically run AI Service whenever new photos are added",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeTheme.textSecondary
                            )
                        }
                        Switch(
                            checked = autoTagOnImport,
                            onCheckedChange = { onToggleAutoTagOnImport() },
                            modifier = Modifier.testTag("switch_ai_auto_import")
                        )
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRunning) {
                            Button(
                                onClick = onCancelService,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cancel_ai_service_button")
                            ) {
                                Text("Stop Background AI Service")
                            }
                        } else {
                            Button(
                                onClick = { onStartOrganizeAll(false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeTheme.primaryAccent,
                                    contentColor = activeTheme.onPrimaryAccent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_ai_organize_all_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Organize All (${allPhotos.size})")
                            }
                            OutlinedButton(
                                onClick = { onStartOrganizeAll(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_ai_organize_untagged_button")
                            ) {
                                Text("Scan Untagged")
                            }
                        }
                    }
                }
            }
        }

        // 2. Single-Photo Quick AI Inspector Strip
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tap Any Photo to Tag & Categorize with Gemini AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeTheme.textPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allPhotos, key = { "ai_single_${it.id}" }) { photo ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onAnalyzeSinglePhoto(photo) }
                            ) {
                                Box(modifier = Modifier.size(96.dp)) {
                                    AsyncImage(
                                        model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                        contentDescription = photo.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.72f),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = photo.albumName.take(11),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = photo.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Recent AI Categorization Results Log
        if (recentResults.isNotEmpty()) {
            item {
                Text(
                    text = "Recent AI Categorization Log (${recentResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeTheme.textPrimary
                )
            }
            items(recentResults, key = { "ai_res_${it.photoId}" }) { result ->
                val matchedPhoto = allPhotos.firstOrNull { it.id == result.photoId }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(18.dp))
                        .clickable {
                            if (matchedPhoto != null) onOpenLightbox(matchedPhoto)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (matchedPhoto != null) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(matchedPhoto.filePath),
                                contentDescription = result.photoTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = activeTheme.primaryAccent
                                ) {
                                    Text(
                                        text = result.primaryCategory.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.5.sp
                                        ),
                                        color = activeTheme.onPrimaryAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = activeTheme.secondaryAccent.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = result.detectedSceneMood,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = activeTheme.secondaryAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = result.smartCaption,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = activeTheme.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Tags: ${result.tags.joinToString(" • ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeTheme.textSecondary,
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
