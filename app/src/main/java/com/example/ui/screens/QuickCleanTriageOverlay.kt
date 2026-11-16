package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.CompleteUiThemeSpec
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class TriageAction {
    KEEP,
    FAVORITE,
    TRASH
}

private data class TriageHistoryEntry(
    val photo: PhotoEntity,
    val action: TriageAction
)

@Composable
fun QuickCleanTriageOverlay(
    photos: List<PhotoEntity>,
    activeTheme: CompleteUiThemeSpec,
    onFavoritePhoto: (PhotoEntity) -> Unit,
    onTrashPhoto: (PhotoEntity) -> Unit,
    onRestoreTrashPhoto: (PhotoEntity) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val queue = remember { photos.toList() }
    var currentIndex by remember { mutableIntStateOf(0) }
    val history = remember { mutableStateListOf<TriageHistoryEntry>() }
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val activePhoto = queue.getOrNull(currentIndex)
    val nextPhoto = queue.getOrNull(currentIndex + 1)

    val sageGreen = Color(0xFF4A7C59)
    val terracotta = activeTheme.primaryAccent
    val trashRed = Color(0xFFDC2626)

    fun commitAction(action: TriageAction) {
        val target = queue.getOrNull(currentIndex) ?: return
        scope.launch {
            val targetX = when (action) {
                TriageAction.KEEP -> 1200f
                TriageAction.TRASH -> -1200f
                TriageAction.FAVORITE -> 0f
            }
            val targetY = if (action == TriageAction.FAVORITE) -1300f else 120f
            launch { offsetX.animateTo(targetX, tween(210)) }
            launch { offsetY.animateTo(targetY, tween(210)) }.join()

            when (action) {
                TriageAction.FAVORITE -> if (!target.isFavorite) onFavoritePhoto(target)
                TriageAction.TRASH -> onTrashPhoto(target)
                TriageAction.KEEP -> Unit
            }
            history.add(TriageHistoryEntry(target, action))
            currentIndex++
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
        }
    }

    fun undoLast() {
        val last = history.removeLastOrNull() ?: return
        if (currentIndex > 0) {
            currentIndex--
        }
        when (last.action) {
            TriageAction.TRASH -> onRestoreTrashPhoto(last.photo)
            TriageAction.FAVORITE -> if (!last.photo.isFavorite) onFavoritePhoto(last.photo)
            TriageAction.KEEP -> Unit
        }
        scope.launch {
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
        }
    }

    Surface(
        color = activeTheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("quick_clean_triage_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar + Progress
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = terracotta.copy(alpha = 0.14f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = terracotta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Quick Clean",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = if (queue.isEmpty()) "No photos to review" else "${minOf(currentIndex + 1, queue.size)} of ${queue.size} • Swipe up, left, or right",
                                style = MaterialTheme.typography.labelMedium,
                                color = activeTheme.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("quick_clean_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Quick Clean",
                            tint = activeTheme.textPrimary
                        )
                    }
                }

                val progress = if (queue.isEmpty()) 1f else (currentIndex.toFloat() / queue.size.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    color = terracotta,
                    trackColor = activeTheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }

            // Center Swipe Card Stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activePhoto == null) {
                    val keptCount = history.count { it.action == TriageAction.KEEP }
                    val favCount = history.count { it.action == TriageAction.FAVORITE }
                    val trashCount = history.count { it.action == TriageAction.TRASH }

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
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = sageGreen.copy(alpha = 0.14f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = sageGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Triage Complete!",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = activeTheme.textPrimary
                            )
                            Text(
                                text = "$keptCount kept · $favCount favorited · $trashCount moved to Recycle Bin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTheme.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = onClose,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = terracotta,
                                    contentColor = activeTheme.onPrimaryAccent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Photos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Underlying Next Card Preview
                    if (nextPhoto != null) {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(0.94f)
                                .alpha(0.7f)
                                .border(1.dp, activeTheme.borderStroke, RoundedCornerShape(26.dp))
                        ) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(nextPhoto.filePath),
                                contentDescription = nextPhoto.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Active Draggable Top Card
                    val dragX = offsetX.value
                    val dragY = offsetY.value
                    val rotation = (dragX / 35f).coerceIn(-16f, 16f)

                    val isFavHover = dragY < -90f && abs(dragY) > abs(dragX)
                    val isKeepHover = dragX > 90f && !isFavHover
                    val isTrashHover = dragX < -90f && !isFavHover

                    Card(
                        shape = RoundedCornerShape(26.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = activeTheme.surface),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
                            .rotate(rotation)
                            .border(
                                width = 2.dp,
                                color = when {
                                    isFavHover -> terracotta
                                    isKeepHover -> sageGreen
                                    isTrashHover -> trashRed
                                    else -> activeTheme.borderStroke
                                },
                                shape = RoundedCornerShape(26.dp)
                            )
                            .pointerInput(activePhoto.id) {
                                detectDragGestures(
                                    onDragEnd = {
                                        val curX = offsetX.value
                                        val curY = offsetY.value
                                        when {
                                            curY < -190f && abs(curY) > abs(curX) -> commitAction(TriageAction.FAVORITE)
                                            curX > 220f -> commitAction(TriageAction.KEEP)
                                            curX < -220f -> commitAction(TriageAction.TRASH)
                                            else -> {
                                                scope.launch {
                                                    launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                                    launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                                }
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch {
                                            launch { offsetX.animateTo(0f, spring()) }
                                            launch { offsetY.animateTo(0f, spring()) }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount.x)
                                            offsetY.snapTo(offsetY.value + dragAmount.y)
                                        }
                                    }
                                )
                            }
                            .testTag("quick_clean_active_card")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(activePhoto.filePath),
                                contentDescription = activePhoto.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Swipe Direction Stamp Overlays
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isKeepHover || isTrashHover || isFavHover,
                                enter = fadeIn(tween(120)),
                                exit = fadeOut(tween(120)),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp)
                            ) {
                                val (stampText, stampColor) = when {
                                    isFavHover -> "FAVORITE ♥" to terracotta
                                    isKeepHover -> "KEEP ✓" to sageGreen
                                    else -> "RECYCLE BIN" to trashRed
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = stampColor.copy(alpha = 0.92f),
                                    border = BorderStroke(2.dp, Color.White)
                                ) {
                                    Text(
                                        text = stampText,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            // Bottom Photo Info Scrim
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = activePhoto.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${activePhoto.shortDateText} · ${activePhoto.albumName} · ${activePhoto.formattedSize}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Accessible Triage Buttons
            if (activePhoto != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trash (Left)
                    TriageActionCircleButton(
                        icon = Icons.Default.DeleteOutline,
                        label = "Trash",
                        containerColor = trashRed.copy(alpha = 0.14f),
                        contentColor = trashRed,
                        sizeDp = 58,
                        tag = "quick_clean_trash_btn",
                        onClick = { commitAction(TriageAction.TRASH) }
                    )

                    // Undo
                    TriageActionCircleButton(
                        icon = Icons.Default.History,
                        label = "Undo",
                        containerColor = activeTheme.surfaceVariant,
                        contentColor = activeTheme.textPrimary,
                        sizeDp = 48,
                        enabled = history.isNotEmpty(),
                        tag = "quick_clean_undo_btn",
                        onClick = { undoLast() }
                    )

                    // Favorite (Up)
                    TriageActionCircleButton(
                        icon = Icons.Default.Favorite,
                        label = "Favorite",
                        containerColor = terracotta,
                        contentColor = Color.White,
                        sizeDp = 64,
                        tag = "quick_clean_favorite_btn",
                        onClick = { commitAction(TriageAction.FAVORITE) }
                    )

                    // Keep (Right)
                    TriageActionCircleButton(
                        icon = Icons.Default.Check,
                        label = "Keep",
                        containerColor = sageGreen,
                        contentColor = Color.White,
                        sizeDp = 58,
                        tag = "quick_clean_keep_btn",
                        onClick = { commitAction(TriageAction.KEEP) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TriageActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    sizeDp: Int,
    enabled: Boolean = true,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.alpha(if (enabled) 1f else 0.45f)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            shadowElevation = if (enabled) 4.dp else 0.dp,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onClick)
                .testTag(tag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size((sizeDp * 0.44f).dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}
