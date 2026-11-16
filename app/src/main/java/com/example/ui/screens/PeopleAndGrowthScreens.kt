package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.GrowthAgeUtils
import com.example.data.PersonClusterEntity
import com.example.data.PhotoEntity
import com.example.data.PhotoFaceTagEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.CompleteUiThemeSpec
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RichSearchOverlay(
    initialQuery: String,
    allPhotos: List<PhotoEntity>,
    personClusters: List<PersonClusterEntity>,
    faceTags: List<PhotoFaceTagEntity>,
    activeTheme: CompleteUiThemeSpec,
    onQueryChange: (String) -> Unit,
    onOpenPersonDetail: (PersonClusterEntity) -> Unit,
    onSelectQuickChipAndClose: (String) -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedQuickChip by remember { mutableStateOf<String?>(null) }

    val quickChips = remember {
        listOf("Screenshots", "Selfies", "1st Birthday", "Videos", "Last Month")
    }

    val matchingPhotos = remember(query, selectedQuickChip, allPhotos) {
        val trimmed = query.trim().lowercase()
        allPhotos.filter { photo ->
            val chipMatch = selectedQuickChip == null || photo.matchesCategory(selectedQuickChip!!)
            val queryMatch = trimmed.isEmpty() ||
                photo.title.lowercase().contains(trimmed) ||
                photo.albumName.lowercase().contains(trimmed) ||
                photo.tags.lowercase().contains(trimmed) ||
                photo.milestoneSticker.lowercase().contains(trimmed) ||
                photo.notes.lowercase().contains(trimmed) ||
                photo.shortDateText.lowercase().contains(trimmed)
            chipMatch && queryMatch
        }.sortedByDescending { it.dateTaken }
    }

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("rich_search_overlay")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("search_overlay_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = activeTheme.textPrimary
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        onQueryChange(it)
                    },
                    placeholder = {
                        Text(
                            "Search people, milestones, dates…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = activeTheme.textSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = activeTheme.primaryAccent
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    query = ""
                                    onQueryChange("")
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = activeTheme.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = activeTheme.surface,
                        unfocusedContainerColor = activeTheme.surface,
                        focusedBorderColor = activeTheme.primaryAccent,
                        unfocusedBorderColor = activeTheme.borderStroke
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_overlay_text_field")
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // People & Pets Horizontal Strip
                if (personClusters.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "People & Pets",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = activeTheme.textPrimary
                                )
                                Text(
                                    text = "${personClusters.size} clusters",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.primaryAccent
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(personClusters, key = { it.id }) { person ->
                                    val count = faceTags.count { it.personId == person.id }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(74.dp)
                                            .clickable { onOpenPersonDetail(person) }
                                            .testTag("search_person_avatar_${person.id}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(66.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (person.hasGrowthTimeline) 2.5.dp else 1.5.dp,
                                                    color = if (person.hasGrowthTimeline) {
                                                        activeTheme.primaryAccent
                                                    } else {
                                                        activeTheme.borderStroke
                                                    },
                                                    shape = CircleShape
                                                )
                                                .background(activeTheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (person.avatarPhotoPath.isNotEmpty()) {
                                                AsyncImage(
                                                    model = PhotoImageProcessor.resolveCoilModel(person.avatarPhotoPath),
                                                    contentDescription = person.displayTitle,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Text(
                                                    text = person.displayTitle.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = activeTheme.primaryAccent
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = if (person.isNamed) person.name else "Add name",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (person.isNamed) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = if (person.isNamed) activeTheme.textPrimary else activeTheme.primaryAccent,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$count photos",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                            color = activeTheme.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Smart-Filter Chips Row
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Filters",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = activeTheme.textPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickChips) { chip ->
                                val selected = selectedQuickChip == chip
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) activeTheme.primaryAccent else activeTheme.borderStroke
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedQuickChip = if (selected) null else chip
                                        }
                                        .testTag("search_quick_chip_${chip.lowercase().replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = chip,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) activeTheme.onPrimaryAccent else activeTheme.textPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Result Header
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${matchingPhotos.size} Matching Photos",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = activeTheme.textSecondary
                        )
                        if (selectedQuickChip != null) {
                            Text(
                                text = "Apply in Photos →",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = activeTheme.primaryAccent,
                                modifier = Modifier.clickable {
                                    onSelectQuickChipAndClose(selectedQuickChip!!)
                                }
                            )
                        }
                    }
                }

                items(matchingPhotos, key = { it.id }) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, activeTheme.borderStroke, RoundedCornerShape(12.dp))
                            .clickable { onPhotoClick(photo) }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (photo.milestoneSticker.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = activeTheme.primaryAccent,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = photo.milestoneSticker,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                                    color = activeTheme.onPrimaryAccent,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeopleDirectoryScreen(
    personClusters: List<PersonClusterEntity>,
    faceTags: List<PhotoFaceTagEntity>,
    activeTheme: CompleteUiThemeSpec,
    onOpenPersonDetail: (PersonClusterEntity) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("people_directory_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeTheme.textPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "People & Pets",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "${personClusters.size} detected profiles • Tap to view or start a Growth Timeline",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTheme.textSecondary
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(personClusters, key = { it.id }) { person ->
                    val count = faceTags.count { it.personId == person.id }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenPersonDetail(person) }
                            .testTag("directory_person_card_${person.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.5.dp,
                                        if (person.hasGrowthTimeline) activeTheme.primaryAccent else activeTheme.borderStroke,
                                        CircleShape
                                    )
                                    .background(activeTheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(person.avatarPhotoPath),
                                    contentDescription = person.displayTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = person.displayTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (person.isNamed) activeTheme.textPrimary else activeTheme.primaryAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (person.hasGrowthTimeline) "Growth Timeline • $count" else "$count Photos",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeTheme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonDetailScreen(
    person: PersonClusterEntity,
    allPersons: List<PersonClusterEntity>,
    personPhotos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onBack: () -> Unit,
    onRenamePerson: (Long, String) -> Unit,
    onMergePerson: (Long, Long, String) -> Unit,
    onHidePerson: (Long) -> Unit,
    onOpenOrSetupGrowthTimeline: () -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit
) {
    BackHandler { onBack() }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("person_detail_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("person_detail_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeTheme.textPrimary)
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("person_detail_more_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Person options", tint = activeTheme.textPrimary)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(activeTheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Merge Person", color = activeTheme.textPrimary) },
                            onClick = {
                                menuExpanded = false
                                showMergeDialog = true
                            },
                            modifier = Modifier.testTag("menu_merge_person")
                        )
                        DropdownMenuItem(
                            text = { Text("Hide Face", color = activeTheme.textPrimary) },
                            onClick = {
                                menuExpanded = false
                                onHidePerson(person.id)
                                onBack()
                            },
                            modifier = Modifier.testTag("menu_hide_face")
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero Profile Header Card
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, activeTheme.primaryAccent, CircleShape)
                                    .background(activeTheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(person.avatarPhotoPath),
                                    contentDescription = person.displayTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Text(
                                text = person.displayTitle,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = activeTheme.textPrimary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { showRenameDialog = true },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("person_edit_name_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (person.isNamed) "Edit Name" else "Add Name")
                                }

                                Button(
                                    onClick = onOpenOrSetupGrowthTimeline,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeTheme.primaryAccent,
                                        contentColor = activeTheme.onPrimaryAccent
                                    ),
                                    modifier = Modifier.testTag("setup_growth_timeline_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (person.hasGrowthTimeline) "Open Growth Timeline" else "Set up Growth Timeline",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "${personPhotos.size} Tagged Photos",
                                style = MaterialTheme.typography.labelMedium,
                                color = activeTheme.textSecondary
                            )
                        }
                    }
                }

                items(personPhotos, key = { it.id }) { photo ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(0.5.dp, activeTheme.borderStroke, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPhotoClick(photo) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                contentDescription = photo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(topEnd = 10.dp),
                                color = Color.Black.copy(alpha = 0.68f),
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                val badgeLabel = if (person.hasGrowthTimeline && person.childDobMs != null) {
                                    GrowthAgeUtils.calculateAgeLabel(person.childDobMs, photo.dateTaken)
                                } else {
                                    photo.shortDateText
                                }
                                Text(
                                    text = badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var nameInput by remember(person.id) { mutableStateOf(person.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = activeTheme.surface,
            title = { Text(if (person.isNamed) "Edit Name" else "Name This Person", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = nameInput.isNotBlank(),
                    onClick = {
                        onRenamePerson(person.id, nameInput.trim())
                        showRenameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMergeDialog) {
        val mergeCandidates = allPersons.filter { it.id != person.id }
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            containerColor = activeTheme.surface,
            title = { Text("Merge Into Person", fontWeight = FontWeight.Bold) },
            text = {
                if (mergeCandidates.isEmpty()) {
                    Text("No other visible person clusters to merge with.", color = activeTheme.textSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mergeCandidates.forEach { target ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = activeTheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onMergePerson(person.id, target.id, target.displayTitle)
                                        showMergeDialog = false
                                        onBack()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AsyncImage(
                                        model = PhotoImageProcessor.resolveCoilModel(target.avatarPhotoPath),
                                        contentDescription = target.displayTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = target.displayTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = activeTheme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMergeDialog = false }) { Text("Close") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthTimelineSetupBottomSheet(
    person: PersonClusterEntity,
    activeTheme: CompleteUiThemeSpec,
    onDismiss: () -> Unit,
    onConfirmSetup: (childName: String, birthDateMs: Long) -> Unit
) {
    var childName by remember(person.id) { mutableStateOf(person.name.ifBlank { "Maya" }) }
    val defaultDob = remember(person.id) {
        person.childDobMs ?: (System.currentTimeMillis() - 395L * 86_400_000L)
    }
    var selectedDobMs by remember { mutableLongStateOf(defaultDob) }
    val dobFormatted = remember(selectedDobMs) {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(selectedDobMs))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = activeTheme.surface,
        modifier = Modifier.testTag("growth_timeline_setup_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Set up Growth Timeline",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = activeTheme.textPrimary
            )
            Text(
                text = "Automatically calculate exact baby & toddler ages across every photo and organize chapters chronologically.",
                style = MaterialTheme.typography.bodySmall,
                color = activeTheme.textSecondary
            )

            OutlinedTextField(
                value = childName,
                onValueChange = { childName = it },
                label = { Text("Child's Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("growth_setup_name_input")
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = activeTheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = activeTheme.primaryAccent, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Date of Birth: $dobFormatted",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = activeTheme.textPrimary
                            )
                        }
                        Text(
                            text = GrowthAgeUtils.calculateHeaderAge(selectedDobMs),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = activeTheme.primaryAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "6 Months" to 185L,
                            "1 Year" to 365L,
                            "13 Months" to 395L,
                            "2 Years" to 730L
                        ).forEach { (label, daysAgo) ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = activeTheme.surface,
                                border = BorderStroke(1.dp, activeTheme.borderStroke),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        selectedDobMs = System.currentTimeMillis() - daysAgo * 86_400_000L
                                    }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = activeTheme.textPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Fine Year / Month adjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selectedDobMs
                                    add(Calendar.MONTH, -1)
                                }
                                selectedDobMs = cal.timeInMillis
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("-1 Month") }
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selectedDobMs
                                    add(Calendar.MONTH, 1)
                                }
                                if (cal.timeInMillis <= System.currentTimeMillis()) {
                                    selectedDobMs = cal.timeInMillis
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("+1 Month") }
                    }
                }
            }

            Button(
                enabled = childName.isNotBlank(),
                onClick = { onConfirmSetup(childName.trim(), selectedDobMs) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeTheme.primaryAccent,
                    contentColor = activeTheme.onPrimaryAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_growth_setup_button")
            ) {
                Text("Activate Growth Timeline", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrowthTimelineScreen(
    person: PersonClusterEntity,
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onBack: () -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onToggleMilestoneSticker: (PhotoEntity, String) -> Unit
) {
    BackHandler { onBack() }

    val dobMs = person.childDobMs ?: (System.currentTimeMillis() - 395L * 86_400_000L)
    val sortedPhotos = remember(photos) { photos.sortedBy { it.dateTaken } }
    val groupedChapters = remember(sortedPhotos, dobMs) {
        val grouped = sortedPhotos.groupBy { photo ->
            GrowthAgeUtils.resolveAgeChapter(dobMs, photo.dateTaken)
        }
        GrowthAgeUtils.AGE_CHAPTER_ORDER.mapNotNull { chapter ->
            val items = grouped[chapter]
            if (items.isNullOrEmpty()) null else chapter to items
        }
    }

    var showReelModal by remember { mutableStateOf(false) }
    var showThenNowModal by remember { mutableStateOf(false) }
    var stickerTargetPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("growth_timeline_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("growth_timeline_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = activeTheme.textPrimary)
                }
                Text(
                    text = "${person.displayTitle}'s Storybook",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = activeTheme.textPrimary
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Warm Editorial Child Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, activeTheme.primaryAccent, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = PhotoImageProcessor.resolveCoilModel(person.avatarPhotoPath),
                                        contentDescription = person.displayTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.displayTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = activeTheme.textPrimary
                                    )
                                    Text(
                                        text = GrowthAgeUtils.calculateHeaderAge(dobMs),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = activeTheme.primaryAccent
                                    )
                                    Text(
                                        text = "${sortedPhotos.size} milestone memories captured",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = activeTheme.textSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    enabled = sortedPhotos.isNotEmpty(),
                                    onClick = { showReelModal = true },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeTheme.primaryAccent,
                                        contentColor = activeTheme.onPrimaryAccent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("play_growth_reel_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play Growth Reel", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(
                                    enabled = sortedPhotos.size >= 2,
                                    onClick = { showThenNowModal = true },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("then_now_compare_button")
                                ) {
                                    Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Then & Now", fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Vertical Terracotta Timeline Rail Chapters
                groupedChapters.forEach { (chapterTitle, chapterPhotos) ->
                    item(key = "chapter_$chapterTitle") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Terracotta Vertical Rail Column
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(activeTheme.primaryAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height((chapterPhotos.size * 220).dp)
                                        .background(activeTheme.primaryAccent.copy(alpha = 0.35f))
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = activeTheme.primaryAccent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = chapterTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = activeTheme.primaryAccent,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                chapterPhotos.forEach { photo ->
                                    val ageBadge = GrowthAgeUtils.calculateAgeLabel(dobMs, photo.dateTaken)
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(20.dp))
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { onPhotoClick(photo) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(190.dp)
                                        ) {
                                            AsyncImage(
                                                model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                                contentDescription = photo.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Subtle Bottom-Left Age Badge
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(0xFF1F1C1A).copy(alpha = 0.85f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = ageBadge,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Tappable Milestone Sticker Pill (Bottom Right)
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (photo.milestoneSticker.isNotEmpty()) {
                                                    activeTheme.primaryAccent
                                                } else {
                                                    Color.Black.copy(alpha = 0.56f)
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(10.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { stickerTargetPhoto = photo }
                                            ) {
                                                Text(
                                                    text = if (photo.milestoneSticker.isNotEmpty()) photo.milestoneSticker else "+ Milestone",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
        }
    }

    // Milestone Sticker Selector Dialog
    val targetStickerPhoto = stickerTargetPhoto
    if (targetStickerPhoto != null) {
        AlertDialog(
            onDismissRequest = { stickerTargetPhoto = null },
            containerColor = activeTheme.surface,
            title = { Text("Choose Milestone Sticker", fontWeight = FontWeight.Bold) },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GrowthAgeUtils.MILESTONE_STICKER_OPTIONS.forEach { sticker ->
                        val isChosen = targetStickerPhoto.milestoneSticker == sticker
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isChosen) activeTheme.primaryAccent else activeTheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    onToggleMilestoneSticker(targetStickerPhoto, sticker)
                                    stickerTargetPhoto = null
                                }
                        ) {
                            Text(
                                text = sticker,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isChosen) activeTheme.onPrimaryAccent else activeTheme.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { stickerTargetPhoto = null }) { Text("Done") }
            }
        )
    }

    if (showReelModal && sortedPhotos.isNotEmpty()) {
        GrowthReelModal(
            childName = person.displayTitle,
            dobMs = dobMs,
            photos = sortedPhotos,
            activeTheme = activeTheme,
            onDismiss = { showReelModal = false }
        )
    }

    if (showThenNowModal && sortedPhotos.size >= 2) {
        ThenNowCompareModal(
            childName = person.displayTitle,
            dobMs = dobMs,
            photos = sortedPhotos,
            activeTheme = activeTheme,
            onDismiss = { showThenNowModal = false }
        )
    }
}

@Composable
private fun GrowthReelModal(
    childName: String,
    dobMs: Long,
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onDismiss: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying, photos.size) {
        while (isPlaying && photos.size > 1) {
            delay(2200L)
            index = (index + 1) % photos.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "growth_reel_zoom")
    val kenBurnsScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reel_scale"
    )

    val currentPhoto = photos[index.coerceIn(0, photos.lastIndex)]
    val ageLabel = GrowthAgeUtils.calculateAgeLabel(dobMs, currentPhoto.dateTaken)

    Surface(
        color = Color(0xFF141210),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("growth_reel_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$childName • Growth Reel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "Memory ${index + 1} of ${photos.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
                Row {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Reel", tint = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, activeTheme.primaryAccent, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = currentPhoto, animationSpec = tween(550), label = "reel_crossfade") { photo ->
                    AsyncImage(
                        model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = kenBurnsScale, scaleY = kenBurnsScale)
                    )
                }

                // Bottom Overlay Age & Milestone
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = activeTheme.primaryAccent
                        ) {
                            Text(
                                text = ageLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = currentPhoto.milestoneSticker.ifEmpty { currentPhoto.title },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos.indices.toList()) { i ->
                    val active = i == index
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (active) 2.5.dp else 1.dp, if (active) activeTheme.primaryAccent else Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable { index = i }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photos[i].filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThenNowCompareModal(
    childName: String,
    dobMs: Long,
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onDismiss: () -> Unit
) {
    var thenIdx by remember { mutableIntStateOf(0) }
    var nowIdx by remember { mutableIntStateOf(photos.lastIndex) }

    val thenPhoto = photos[thenIdx.coerceIn(0, photos.lastIndex)]
    val nowPhoto = photos[nowIdx.coerceIn(0, photos.lastIndex)]

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("then_now_compare_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$childName • Then & Now",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = activeTheme.textPrimary
                    )
                    Text(
                        text = "Compare two ages side-by-side",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTheme.textSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = activeTheme.textPrimary)
                }
            }

            // Side-by-Side Comparison Frame
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompareAgeSlotCard(
                    tagLabel = "THEN",
                    ageLabel = GrowthAgeUtils.calculateAgeLabel(dobMs, thenPhoto.dateTaken),
                    photo = thenPhoto,
                    accentColor = activeTheme.secondaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier.weight(1f)
                )
                CompareAgeSlotCard(
                    tagLabel = "NOW",
                    ageLabel = GrowthAgeUtils.calculateAgeLabel(dobMs, nowPhoto.dateTaken),
                    photo = nowPhoto,
                    accentColor = activeTheme.primaryAccent,
                    activeTheme = activeTheme,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Pick 'Then' Photo",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = activeTheme.textSecondary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos.indices.toList()) { i ->
                    val selected = i == thenIdx
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (selected) 2.5.dp else 1.dp, if (selected) activeTheme.secondaryAccent else activeTheme.borderStroke, RoundedCornerShape(10.dp))
                            .clickable { thenIdx = i }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photos[i].filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Text(
                text = "Pick 'Now' Photo",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = activeTheme.textSecondary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos.indices.toList()) { i ->
                    val selected = i == nowIdx
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(if (selected) 2.5.dp else 1.dp, if (selected) activeTheme.primaryAccent else activeTheme.borderStroke, RoundedCornerShape(10.dp))
                            .clickable { nowIdx = i }
                    ) {
                        AsyncImage(
                            model = PhotoImageProcessor.resolveCoilModel(photos[i].filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareAgeSlotCard(
    tagLabel: String,
    ageLabel: String,
    photo: PhotoEntity,
    accentColor: Color,
    activeTheme: CompleteUiThemeSpec,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
        modifier = modifier
            .fillMaxHeight()
            .border(1.5.dp, accentColor, RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = tagLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF1F1C1A).copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
            ) {
                Text(
                    text = ageLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}
