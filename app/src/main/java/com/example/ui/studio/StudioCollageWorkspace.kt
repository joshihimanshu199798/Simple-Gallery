package com.example.ui.studio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.CropAspectRatio
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.LocalCompleteUiTheme

private val GraphiteBg: Color
    @Composable get() = LocalCompleteUiTheme.current.background
private val PanelBg: Color
    @Composable get() = LocalCompleteUiTheme.current.surface
private val CardBg: Color
    @Composable get() = LocalCompleteUiTheme.current.surfaceVariant
private val BorderColor: Color
    @Composable get() = LocalCompleteUiTheme.current.borderStroke
private val NeonGreen: Color
    @Composable get() = LocalCompleteUiTheme.current.primaryAccent
private val OnNeonGreen: Color
    @Composable get() = LocalCompleteUiTheme.current.onPrimaryAccent
private val TextWhite: Color
    @Composable get() = LocalCompleteUiTheme.current.textPrimary
private val TextMuted: Color
    @Composable get() = LocalCompleteUiTheme.current.textSecondary

@Composable
fun StudioCollageWorkspace(
    allPhotos: List<PhotoEntity>,
    onClose: () -> Unit,
    onExportCollage: (
        photoPaths: List<String>,
        layoutMode: CollageLayoutMode,
        aspectRatio: CropAspectRatio,
        spacingDp: Float,
        cornerRadiusDp: Float,
        backgroundColorHex: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val selectedPaths = remember {
        mutableStateListOf<String>().apply {
            addAll(allPhotos.take(4).map { it.filePath })
        }
    }

    var layoutMode by remember { mutableStateOf(CollageLayoutMode.BENTO_HERO) }
    var aspectRatio by remember { mutableStateOf(CropAspectRatio.SQUARE) }
    var spacingDp by remember { mutableFloatStateOf(8f) }
    var cornerRadiusDp by remember { mutableFloatStateOf(12f) }
    var selectedBgHex by remember { mutableStateOf("#141418") }

    val bgColors = listOf(
        "#141418" to Color(0xFF141418),
        "#0A0A0B" to Color(0xFF0A0A0B),
        "#F8FAFC" to Color(0xFFF8FAFC),
        "#FEF3C7" to Color(0xFFFEF3C7),
        "#DC2626" to Color(0xFFDC2626),
        "#0284C7" to Color(0xFF0284C7)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("studio_collage_workspace")
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COLLAGE STUDIO",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = TextWhite
                )
                Text(
                    text = "${selectedPaths.size} of 20 Photos Selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen
                )
            }

            Button(
                onClick = {
                    if (selectedPaths.size >= 2) {
                        onExportCollage(
                            selectedPaths.toList(),
                            layoutMode,
                            aspectRatio,
                            spacingDp,
                            cornerRadiusDp,
                            selectedBgHex
                        )
                    }
                },
                enabled = selectedPaths.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = OnNeonGreen
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = OnNeonGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("EXPORT", fontWeight = FontWeight.ExtraBold, color = OnNeonGreen)
            }
        }

        // CENTER COLLAGE CANVAS PREVIEW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val aspect = when (aspectRatio) {
                CropAspectRatio.PORTRAIT_4_5 -> 4f / 5f
                CropAspectRatio.CINEMA_16_9 -> 16f / 9f
                CropAspectRatio.STORY_9_16 -> 9f / 16f
                else -> 1f
            }

            val animatedAspect by animateFloatAsState(
                targetValue = aspect,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "collage_aspect_anim"
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(android.graphics.Color.parseColor(selectedBgHex)),
                modifier = Modifier
                    .aspectRatio(animatedAspect)
                    .clip(RoundedCornerShape(14.dp))
                    .padding((spacingDp * 0.8f).dp)
            ) {
                AnimatedContent(
                    targetState = layoutMode,
                    transitionSpec = {
                        (fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(220))) togetherWith
                            (fadeOut(tween(150)) + scaleOut(targetScale = 0.96f, animationSpec = tween(150)))
                    },
                    label = "collage_layout_transition"
                ) { activeMode ->
                    when (activeMode) {
                        CollageLayoutMode.THEN_NOW_SPLIT -> {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(spacingDp.dp)
                            ) {
                                listOf("THEN", "NOW").forEachIndexed { idx, label ->
                                    val path = selectedPaths.getOrNull(idx) ?: selectedPaths.firstOrNull()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(cornerRadiusDp.dp))
                                    ) {
                                        if (path != null) {
                                            AsyncImage(
                                                model = PhotoImageProcessor.resolveCoilModel(path),
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color.Black.copy(alpha = 0.72f),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        CollageLayoutMode.BENTO_HERO -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(spacingDp.dp)
                            ) {
                                if (selectedPaths.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.58f)
                                            .clip(RoundedCornerShape(cornerRadiusDp.dp))
                                    ) {
                                        AsyncImage(
                                            model = PhotoImageProcessor.resolveCoilModel(selectedPaths[0]),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                if (selectedPaths.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.42f),
                                        horizontalArrangement = Arrangement.spacedBy(spacingDp.dp)
                                    ) {
                                        for (i in 1 until selectedPaths.size.coerceAtMost(5)) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(cornerRadiusDp.dp))
                                            ) {
                                                AsyncImage(
                                                    model = PhotoImageProcessor.resolveCoilModel(selectedPaths[i]),
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
                        else -> {
                            val cols = if (selectedPaths.size <= 4) 2 else 3
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(cols),
                                verticalArrangement = Arrangement.spacedBy(spacingDp.dp),
                                horizontalArrangement = Arrangement.spacedBy(spacingDp.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(selectedPaths) { path ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(cornerRadiusDp.dp))
                                    ) {
                                        AsyncImage(
                                            model = PhotoImageProcessor.resolveCoilModel(path),
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
            }
        }

        // BOTTOM CONTROLS
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = PanelBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Layout Templates
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CollageLayoutMode.entries) { mode ->
                        val isSelected = layoutMode == mode
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) NeonGreen else CardBg,
                            animationSpec = tween(180),
                            label = "collage_mode_bg"
                        )
                        val pillFg by animateColorAsState(
                            targetValue = if (isSelected) OnNeonGreen else TextWhite,
                            animationSpec = tween(180),
                            label = "collage_mode_fg"
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = pillBg,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonGreen else BorderColor,
                                    shape = RoundedCornerShape(50)
                                )
                                .clip(RoundedCornerShape(50))
                                .clickable { layoutMode = mode }
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                ),
                                color = pillFg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Aspect Ratios & Background Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            CropAspectRatio.SQUARE,
                            CropAspectRatio.PORTRAIT_4_5,
                            CropAspectRatio.CINEMA_16_9,
                            CropAspectRatio.STORY_9_16
                        ).forEach { r ->
                            val isSelected = aspectRatio == r
                            val ratioBg by animateColorAsState(
                                targetValue = if (isSelected) NeonGreen else CardBg,
                                animationSpec = tween(180),
                                label = "ratio_bg"
                            )
                            val ratioFg by animateColorAsState(
                                targetValue = if (isSelected) OnNeonGreen else TextWhite,
                                animationSpec = tween(180),
                                label = "ratio_fg"
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ratioBg,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NeonGreen else BorderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { aspectRatio = r }
                            ) {
                                Text(
                                    text = r.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ratioFg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Background color dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        bgColors.forEach { (hex, col) ->
                            val isSelected = selectedBgHex == hex
                            val dotScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "bg_dot_scale"
                            )
                            Surface(
                                shape = CircleShape,
                                color = col,
                                modifier = Modifier
                                    .scale(dotScale)
                                    .size(22.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonGreen else BorderColor,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable { selectedBgHex = hex }
                            ) {}
                        }
                    }
                }

                // Spacing & Corner Radius Sliders
                PremiumStudioSlider(
                    value = spacingDp,
                    onValueChange = { spacingDp = it },
                    valueRange = 0f..24f,
                    label = "Grid Spacing",
                    valueText = "${spacingDp.toInt()} dp"
                )

                PremiumStudioSlider(
                    value = cornerRadiusDp,
                    onValueChange = { cornerRadiusDp = it },
                    valueRange = 0f..32f,
                    label = "Corner Roundness",
                    valueText = "${cornerRadiusDp.toInt()} dp"
                )

                // Photo selector strip (toggle inclusion)
                Text(
                    text = "TAP PHOTOS TO INCLUDE (2–20):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allPhotos, key = { it.id }) { photo ->
                        val isSelected = photo.filePath in selectedPaths
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) NeonGreen else BorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected && selectedPaths.size > 2) {
                                        selectedPaths.remove(photo.filePath)
                                    } else if (!isSelected && selectedPaths.size < 20) {
                                        selectedPaths.add(photo.filePath)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(NeonGreen.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NeonGreen,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = OnNeonGreen,
                                                modifier = Modifier.size(13.dp)
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
