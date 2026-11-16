package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.MemoryEntryEntity
import com.example.data.MemoryMood
import com.example.data.MilestoneReminderEntity
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.PolaroidFrameStyle

@Composable
fun LittleMemoriesScreen(
    memories: List<MemoryEntryEntity>,
    milestones: List<MilestoneReminderEntity>,
    availablePhotos: List<PhotoEntity>,
    initialComposerPhoto: PhotoEntity?,
    onConsumeInitialComposerPhoto: () -> Unit,
    onSaveMemory: (MemoryEntryEntity) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onTogglePinMemory: (MemoryEntryEntity) -> Unit,
    onUnlockTimeCapsule: (MemoryEntryEntity) -> Unit,
    onExportPolaroid: (MemoryEntryEntity) -> Unit,
    onSaveMilestone: (MilestoneReminderEntity) -> Unit,
    onToggleMilestoneCompleted: (MilestoneReminderEntity) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
    onOpenPhotoInLightbox: (String) -> Unit
) {
    // 0 = Scrapbook Diary, 1 = Time Capsules, 2 = Milestones & Growth, 3 = Mood & Insights
    var activeSubTab by remember { mutableIntStateOf(0) }
    var selectedMoodFilter by remember { mutableStateOf("All") }
    var showMemoryComposer by remember { mutableStateOf(false) }
    var composerPreselectedPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var composerDefaultTimeCapsule by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialComposerPhoto) {
        if (initialComposerPhoto != null) {
            composerPreselectedPhoto = initialComposerPhoto
            composerDefaultTimeCapsule = false
            showMemoryComposer = true
            onConsumeInitialComposerPhoto()
        }
    }

    val filteredMemories = remember(memories, selectedMoodFilter) {
        memories.filter { entry ->
            selectedMoodFilter == "All" || entry.mood.equals(selectedMoodFilter, ignoreCase = true)
        }
    }

    val pinnedFlashbacks = remember(memories) {
        memories.filter { !it.isCurrentlyLockedCapsule && (it.isPinnedHighlight || memories.size <= 4) }
    }

    val timeCapsules = remember(memories) {
        memories.filter { it.isTimeCapsule }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("little_memories_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header Banner for Little Memories
        item {
            LittleMemoriesHeroBanner(
                memoriesCount = memories.size,
                capsulesCount = timeCapsules.size,
                milestonesCount = milestones.size,
                onNewMemoryClick = {
                    composerPreselectedPhoto = availablePhotos.firstOrNull()
                    composerDefaultTimeCapsule = false
                    showMemoryComposer = true
                },
                onNewCapsuleClick = {
                    composerPreselectedPhoto = availablePhotos.firstOrNull()
                    composerDefaultTimeCapsule = true
                    showMemoryComposer = true
                },
                onNewMilestoneClick = {
                    showMilestoneDialog = true
                }
            )
        }

        // 2. "On This Day & Pinned Flashbacks" Carousel
        if (pinnedFlashbacks.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Flashbacks & Cherished Highlights",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "${pinnedFlashbacks.size} keepsakes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pinnedFlashbacks, key = { "flashback_${it.id}" }) { memory ->
                            Box(modifier = Modifier.animateItem()) {
                                FlashbackSpotlightCard(
                                    memory = memory,
                                    onClick = { onOpenPhotoInLightbox(memory.photoFilePath) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Sub-Navigation Chips (Scrapbook Diary | Time Capsules | Milestones & Growth | Mood Insights)
        item {
            val tabs = listOf(
                Triple(0, "Scrapbook (${memories.size})", Icons.Default.MenuBook),
                Triple(1, "Time Capsules (${timeCapsules.size})", Icons.Default.LockClock),
                Triple(2, "Milestones & Growth (${milestones.size})", Icons.Default.EmojiEvents),
                Triple(3, "Mood & Calendar", Icons.Default.CalendarMonth)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { (idx, label, icon) ->
                    FilterChip(
                        selected = activeSubTab == idx,
                        onClick = { activeSubTab = idx },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("memories_subtab_$idx")
                    )
                }
            }
        }

        // 4. Content for Active Sub-Tab
        when (activeSubTab) {
            0 -> {
                // Mood filter strip for Scrapbook Diary
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedMoodFilter == "All",
                                onClick = { selectedMoodFilter = "All" },
                                label = { Text("All Moods") }
                            )
                        }
                        items(MemoryMood.entries) { mood ->
                            FilterChip(
                                selected = selectedMoodFilter.equals(mood.label, ignoreCase = true),
                                onClick = { selectedMoodFilter = mood.label },
                                label = { Text("${mood.emoji} ${mood.label}") }
                            )
                        }
                    }
                }

                items(filteredMemories, key = { "diary_${it.id}" }) { memory ->
                    Box(modifier = Modifier.animateItem()) {
                        PolaroidMemoryCard(
                            memory = memory,
                            onOpenPhoto = { onOpenPhotoInLightbox(memory.photoFilePath) },
                            onTogglePin = { onTogglePinMemory(memory) },
                            onExportPolaroid = { onExportPolaroid(memory) },
                            onUnlockCapsule = { onUnlockTimeCapsule(memory) },
                            onDelete = { onDeleteMemory(memory.id) }
                        )
                    }
                }
            }
            1 -> {
                // Time Capsules Vault
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Memory Time Capsules",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Seal a photograph and personal letter until a future milestone date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    composerPreselectedPhoto = availablePhotos.firstOrNull()
                                    composerDefaultTimeCapsule = true
                                    showMemoryComposer = true
                                },
                                modifier = Modifier.testTag("create_capsule_button")
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Seal Capsule")
                            }
                        }
                    }
                }

                if (timeCapsules.isEmpty()) {
                    item {
                        Text(
                            text = "No Time Capsules sealed yet. Tap 'Seal Capsule' above to lock a memory for future you!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(timeCapsules, key = { "capsule_${it.id}" }) { capsule ->
                        Box(modifier = Modifier.animateItem()) {
                            PolaroidMemoryCard(
                                memory = capsule,
                                onOpenPhoto = { onOpenPhotoInLightbox(capsule.photoFilePath) },
                                onTogglePin = { onTogglePinMemory(capsule) },
                                onExportPolaroid = { onExportPolaroid(capsule) },
                                onUnlockCapsule = { onUnlockTimeCapsule(capsule) },
                                onDelete = { onDeleteMemory(capsule.id) }
                            )
                        }
                    }
                }
            }
            2 -> {
                // Milestones, Growth & Immunization/Event Tracker
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Milestones, Growth & Reminders",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Track first moments, height/weight logs, wellness checkups & anniversaries.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showMilestoneDialog = true },
                                modifier = Modifier.testTag("add_milestone_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Log")
                            }
                        }
                    }
                }

                items(milestones, key = { "milestone_${it.id}" }) { item ->
                    MilestoneReminderCard(
                        milestone = item,
                        onToggleCompleted = { onToggleMilestoneCompleted(item) },
                        onDelete = { onDeleteMilestone(item.id) }
                    )
                }
            }
            3 -> {
                // Mood Breakdown & Chronological Memory Calendar
                item {
                    MoodAndCalendarInsightsCard(
                        memories = memories,
                        milestones = milestones
                    )
                }
            }
        }
    }

    if (showMemoryComposer) {
        MemoryComposerBottomSheet(
            availablePhotos = availablePhotos,
            preselectedPhoto = composerPreselectedPhoto,
            defaultTimeCapsule = composerDefaultTimeCapsule,
            onDismiss = { showMemoryComposer = false },
            onSave = { newEntry ->
                onSaveMemory(newEntry)
                showMemoryComposer = false
            }
        )
    }

    if (showMilestoneDialog) {
        AddMilestoneDialog(
            availablePhotos = availablePhotos,
            onDismiss = { showMilestoneDialog = false },
            onSave = { newMilestone ->
                onSaveMilestone(newMilestone)
                showMilestoneDialog = false
            }
        )
    }
}

@Composable
private fun LittleMemoriesHeroBanner(
    memoriesCount: Int,
    capsulesCount: Int,
    milestonesCount: Int,
    onNewMemoryClick: () -> Unit,
    onNewCapsuleClick: () -> Unit,
    onNewMilestoneClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keepsake & Moments Studio",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$memoriesCount stories • $capsulesCount time capsules • $milestonesCount milestones & growth logs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNewMemoryClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("new_memory_story_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoAlbum,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Memory")
                    }

                    OutlinedButton(
                        onClick = onNewCapsuleClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_time_capsule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capsule")
                    }

                    OutlinedButton(
                        onClick = onNewMilestoneClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_milestone_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Milestone")
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashbackSpotlightCard(
    memory: MemoryEntryEntity,
    onClick: () -> Unit
) {
    val moodEmoji = MemoryMood.entries.firstOrNull {
        it.label.equals(memory.mood, ignoreCase = true)
    }?.emoji ?: "✨"

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(230.dp)
            .height(155.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = PhotoImageProcessor.resolveCoilModel(memory.photoFilePath),
                contentDescription = memory.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "$moodEmoji ${memory.milestoneBadge}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${memory.formattedDate} • ${memory.locationName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PolaroidMemoryCard(
    memory: MemoryEntryEntity,
    onOpenPhoto: () -> Unit,
    onTogglePin: () -> Unit,
    onExportPolaroid: () -> Unit,
    onUnlockCapsule: () -> Unit,
    onDelete: () -> Unit
) {
    val frame = PolaroidFrameStyle.entries.firstOrNull {
        it.label.equals(memory.frameStyle, ignoreCase = true)
    } ?: PolaroidFrameStyle.CLASSIC_POLAROID

    val moodObj = MemoryMood.entries.firstOrNull {
        it.label.equals(memory.mood, ignoreCase = true)
    } ?: MemoryMood.JOYFUL

    val frameBgColor = Color(android.graphics.Color.parseColor(frame.borderHex))
    val frameTextColor = Color(android.graphics.Color.parseColor(frame.textHex))
    val frameAccentColor = Color(android.graphics.Color.parseColor(frame.accentHex))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = frameBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = frameAccentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("memory_card_${memory.id}")
    ) {
        if (memory.isCurrentlyLockedCapsule) {
            // Sealed Time Capsule Presentation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = frameAccentColor.copy(alpha = 0.18f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = "Sealed Time Capsule",
                            tint = frameAccentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = "Sealed Time Capsule: ${memory.title}",
                    style = MaterialTheme.typography.titleLarge,
                    color = frameTextColor
                )
                Text(
                    text = "${memory.capsuleCountdownText} • Created ${memory.formattedDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = frameAccentColor
                )
                Text(
                    text = "This keepsake photograph and story are sealed until the target date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = frameTextColor.copy(alpha = 0.75f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onUnlockCapsule,
                        modifier = Modifier.testTag("unlock_capsule_${memory.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Capsule Early")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete capsule",
                            tint = frameTextColor.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        } else {
            // Unsealed Polaroid Scrapbook Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.32f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenPhoto() }
                ) {
                    AsyncImage(
                        model = PhotoImageProcessor.resolveCoilModel(memory.photoFilePath),
                        contentDescription = memory.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top badges on photo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "${moodObj.emoji} ${memory.mood} • ${memory.milestoneBadge}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onTogglePin() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (memory.isPinnedHighlight) {
                                        Icons.Default.Bookmark
                                    } else {
                                        Icons.Default.BookmarkBorder
                                    },
                                    contentDescription = "Pin memory highlight",
                                    tint = if (memory.isPinnedHighlight) Color(0xFFF4A261) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Polaroid Caption Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = frameTextColor
                        )
                        Text(
                            text = "${memory.formattedDate} • ${memory.frameStyle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = frameAccentColor
                        )
                    }

                    // Date Stamp Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = frameAccentColor.copy(alpha = 0.14f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = memory.dayOfMonthText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = frameAccentColor
                            )
                            Text(
                                text = memory.monthShortText,
                                style = MaterialTheme.typography.labelSmall,
                                color = frameTextColor
                            )
                        }
                    }
                }

                Text(
                    text = memory.story,
                    style = MaterialTheme.typography.bodyMedium,
                    color = frameTextColor.copy(alpha = 0.9f)
                )

                // People & Location Metadata Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (memory.locationName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = frameAccentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = memory.locationName,
                                style = MaterialTheme.typography.labelMedium,
                                color = frameTextColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    if (memory.peopleTagged.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = frameAccentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = memory.peopleTagged,
                                style = MaterialTheme.typography.labelMedium,
                                color = frameTextColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Bottom Polaroid Actions: Export Printable Postcard to Gallery & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = onExportPolaroid,
                        label = {
                            Text(
                                "Save Polaroid Card to Gallery",
                                color = frameTextColor
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = frameAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("export_polaroid_${memory.id}")
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete memory",
                            tint = frameTextColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneReminderCard(
    milestone: MilestoneReminderEntity,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_card_${milestone.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onToggleCompleted,
                modifier = Modifier.testTag("toggle_milestone_${milestone.id}")
            ) {
                Icon(
                    imageVector = if (milestone.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = "Toggle milestone completed",
                    tint = if (milestone.isCompleted) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            if (milestone.linkedPhotoPath != null && PhotoImageProcessor.canDisplayPhotoPath(milestone.linkedPhotoPath)) {
                AsyncImage(
                    model = PhotoImageProcessor.resolveCoilModel(milestone.linkedPhotoPath),
                    contentDescription = milestone.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = milestone.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• ${milestone.relativeDaysText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (milestone.measurementNote.isNotBlank()) {
                    Text(
                        text = milestone.measurementNote,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (milestone.notes.isNotBlank()) {
                    Text(
                        text = milestone.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete milestone",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoodAndCalendarInsightsCard(
    memories: List<MemoryEntryEntity>,
    milestones: List<MilestoneReminderEntity>
) {
    val total = memories.size.coerceAtLeast(1)
    val moodCounts = remember(memories) {
        MemoryMood.entries.map { mood ->
            val count = memories.count { it.mood.equals(mood.label, ignoreCase = true) }
            mood to count
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Mood & Keepsake Analytics",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Emotional tone & milestone distribution across your local keepsakes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            moodCounts.forEach { (mood, count) ->
                val fraction = count.toFloat() / total.toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${mood.emoji} ${mood.label}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "$count memories",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Completed Milestones: ${milestones.count { it.isCompleted }} of ${milestones.size}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryComposerBottomSheet(
    availablePhotos: List<PhotoEntity>,
    preselectedPhoto: PhotoEntity?,
    defaultTimeCapsule: Boolean,
    onDismiss: () -> Unit,
    onSave: (MemoryEntryEntity) -> Unit
) {
    var selectedPhotoPath by remember {
        mutableStateOf(
            preselectedPhoto?.filePath ?: availablePhotos.firstOrNull()?.filePath.orEmpty()
        )
    }
    var title by remember {
        mutableStateOf(preselectedPhoto?.let { "${it.title} Keepsake" } ?: "")
    }
    var story by remember {
        mutableStateOf(preselectedPhoto?.notes ?: "")
    }
    var selectedMood by remember { mutableStateOf(MemoryMood.JOYFUL.label) }
    var selectedBadge by remember { mutableStateOf("Everyday Magic") }
    var peopleTagged by remember { mutableStateOf("") }
    var locationName by remember {
        mutableStateOf(preselectedPhoto?.locationTag ?: "Home Studio")
    }
    var selectedFrame by remember {
        mutableStateOf(PolaroidFrameStyle.CLASSIC_POLAROID.label)
    }
    var isTimeCapsule by remember { mutableStateOf(defaultTimeCapsule) }
    var capsuleDays by remember { mutableIntStateOf(30) }

    val badgeOptions = listOf(
        "Everyday Magic",
        "First Moment",
        "Celebration",
        "Travel Keepsake",
        "Family & Friends",
        "Growth Log"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isTimeCapsule) "Seal a Memory Time Capsule" else "Create Keepsake Story",
                style = MaterialTheme.typography.headlineMedium
            )

            // Photo selector strip
            Text(
                text = "Choose Photograph",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(availablePhotos, key = { "pick_${it.id}" }) { photo ->
                    val isPicked = photo.filePath == selectedPhotoPath
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isPicked) 3.dp else 0.dp,
                                color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPhotoPath = photo.filePath
                                if (title.isBlank()) title = "${photo.title} Keepsake"
                            }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isPicked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(4.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Memory Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_title_input")
            )

            OutlinedTextField(
                value = story,
                onValueChange = { story = it },
                label = { Text("Heartfelt Story / Journal Note") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_story_input")
            )

            // Mood Selector
            Text(
                text = "Mood & Feeling",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MemoryMood.entries) { mood ->
                    FilterChip(
                        selected = selectedMood == mood.label,
                        onClick = { selectedMood = mood.label },
                        label = { Text("${mood.emoji} ${mood.label}") }
                    )
                }
            }

            // Milestone Badge Selector
            Text(
                text = "Milestone Tag",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(badgeOptions) { badge ->
                    FilterChip(
                        selected = selectedBadge == badge,
                        onClick = { selectedBadge = badge },
                        label = { Text(badge) }
                    )
                }
            }

            // Polaroid Frame Selector
            Text(
                text = "Polaroid Keepsake Frame",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PolaroidFrameStyle.entries) { style ->
                    FilterChip(
                        selected = selectedFrame == style.label,
                        onClick = { selectedFrame = style.label },
                        label = { Text(style.label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = peopleTagged,
                    onValueChange = { peopleTagged = it },
                    label = { Text("With (People)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Time Capsule Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Seal as Time Capsule",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Lock this memory until a future unlock countdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isTimeCapsule,
                    onCheckedChange = { isTimeCapsule = it }
                )
            }

            AnimatedVisibility(visible = isTimeCapsule) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(7 to "7 Days", 30 to "30 Days", 180 to "6 Months", 365 to "1 Year")) { (days, label) ->
                        FilterChip(
                            selected = capsuleDays == days,
                            onClick = { capsuleDays = days },
                            label = { Text("Unlock in $label") }
                        )
                    }
                }
            }

            Button(
                enabled = selectedPhotoPath.isNotEmpty(),
                onClick = {
                    val now = System.currentTimeMillis()
                    val unlockAt = if (isTimeCapsule) {
                        now + capsuleDays * 86_400_000L
                    } else null
                    onSave(
                        MemoryEntryEntity(
                            photoFilePath = selectedPhotoPath,
                            title = title.trim().ifEmpty { "Cherished Moment" },
                            story = story.trim().ifEmpty { "Captured a special memory to treasure." },
                            mood = selectedMood,
                            milestoneBadge = selectedBadge,
                            peopleTagged = peopleTagged.trim(),
                            locationName = locationName.trim().ifEmpty { "Local Keepsake" },
                            memoryDate = now,
                            isTimeCapsule = isTimeCapsule,
                            unlockTimestamp = unlockAt,
                            isCapsuleOpened = false,
                            frameStyle = selectedFrame,
                            isPinnedHighlight = !isTimeCapsule
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_memory_button")
            ) {
                Text(if (isTimeCapsule) "Seal Time Capsule" else "Save Keepsake")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AddMilestoneDialog(
    availablePhotos: List<PhotoEntity>,
    onDismiss: () -> Unit,
    onSave: (MilestoneReminderEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Milestone") }
    var measurement by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val categories = listOf(
        "Milestone",
        "Birthday & Anniversary",
        "Growth & Health",
        "Trip & Event"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone or Growth Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., First Bike Ride, 1-Year Checkup)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("milestone_title_input")
                )
                OutlinedTextField(
                    value = measurement,
                    onValueChange = { measurement = it },
                    label = { Text("Growth / Measurement / Note Badge (optional)") },
                    placeholder = { Text("e.g., Height: 85 cm • Weight: 11.8 kg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details & Memories") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        MilestoneReminderEntity(
                            title = title.trim(),
                            category = category,
                            targetDate = System.currentTimeMillis() + 7 * 86_400_000L,
                            notes = notes.trim(),
                            measurementNote = measurement.trim(),
                            isCompleted = false,
                            linkedPhotoPath = availablePhotos.firstOrNull()?.filePath
                        )
                    )
                },
                modifier = Modifier.testTag("confirm_save_milestone_button")
            ) {
                Text("Save Milestone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
