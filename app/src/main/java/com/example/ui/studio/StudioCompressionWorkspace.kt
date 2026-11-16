package com.example.ui.studio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.data.formatBytes
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
fun StudioCompressionWorkspace(
    initialPhoto: PhotoEntity?,
    allPhotos: List<PhotoEntity>,
    onClose: () -> Unit,
    onCompressPhotos: (
        targetPhotos: List<PhotoEntity>,
        format: String,
        quality: Int,
        scalePercent: Int,
        preserveExif: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    var selectedPhoto by remember { mutableStateOf(initialPhoto ?: allPhotos.firstOrNull()) }
    var format by remember { mutableStateOf("WEBP") } // JPEG, WEBP, PNG
    var quality by remember { mutableIntStateOf(80) }
    var scalePercent by remember { mutableIntStateOf(80) }

    val batchSelectedIds = remember {
        mutableStateListOf<Long>().apply {
            (initialPhoto ?: allPhotos.firstOrNull())?.id?.let { add(it) }
        }
    }

    val activeTargets = remember(batchSelectedIds.toList(), selectedPhoto, allPhotos) {
        if (batchSelectedIds.isNotEmpty()) {
            allPhotos.filter { it.id in batchSelectedIds }
        } else {
            listOfNotNull(selectedPhoto)
        }
    }

    val originalBytes = remember(activeTargets) {
        activeTargets.sumOf { it.sizeBytes.coerceAtLeast(350_000L) }.coerceAtLeast(500_000L)
    }
    val estRatio = (quality / 100f) * (scalePercent / 100f) * (if (format == "PNG") 1.1f else if (format == "WEBP") 0.62f else 0.78f)
    val estimatedBytes = (originalBytes * estRatio).toLong().coerceAtLeast(25_000L)
    val savedPercent = (((originalBytes - estimatedBytes).toFloat() / originalBytes.toFloat()) * 100).toInt().coerceIn(5, 92)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("studio_compression_workspace")
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Smart Image Compression",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextWhite
                )
                Text(
                    text = "${activeTargets.size} photo(s) • -$savedPercent% space saved",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = NeonGreen
                )
            }

            Button(
                onClick = {
                    if (activeTargets.isNotEmpty()) {
                        onCompressPhotos(activeTargets, format, quality, scalePercent, false)
                    }
                },
                enabled = activeTargets.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = OnNeonGreen
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Compress, contentDescription = null, tint = OnNeonGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Compress", fontWeight = FontWeight.Bold, color = OnNeonGreen, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Live Storage Savings & Preview Hero Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PanelBg,
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedPhoto != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A0B0E)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = PhotoImageProcessor.resolveCoilModel(selectedPhoto!!.filePath),
                                contentDescription = selectedPhoto!!.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = formatBytes(originalBytes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "~${formatBytes(estimatedBytes)} (-$savedPercent%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }

                    // Format Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "WEBP" to "WebP • Smallest",
                            "JPEG" to "JPEG • Universal",
                            "PNG" to "PNG • Lossless"
                        ).forEach { (fmt, desc) ->
                            val isSelected = format == fmt
                            val fmtBg by animateColorAsState(
                                targetValue = if (isSelected) NeonGreen else CardBg,
                                animationSpec = tween(180),
                                label = "fmt_bg_$fmt"
                            )
                            val fmtFg by animateColorAsState(
                                targetValue = if (isSelected) OnNeonGreen else TextWhite,
                                animationSpec = tween(180),
                                label = "fmt_fg_$fmt"
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = fmtBg,
                                border = BorderStroke(1.dp, if (isSelected) NeonGreen else BorderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { format = fmt }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = fmt,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = fmtFg
                                    )
                                    Text(
                                        text = desc.substringAfter("• "),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = if (isSelected) OnNeonGreen.copy(alpha = 0.85f) else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Compression & Scale Sliders + 1-Tap Export Profiles Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PanelBg,
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Export Profiles",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ExportProfilePreset.DEFAULT_PROFILES, key = { it.id }) { profile ->
                            val isProfileActive = format == profile.format &&
                                quality == profile.quality &&
                                scalePercent == profile.scalePercent
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isProfileActive) NeonGreen else CardBg,
                                border = BorderStroke(1.dp, if (isProfileActive) NeonGreen else BorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        format = profile.format
                                        quality = profile.quality
                                        scalePercent = profile.scalePercent
                                    }
                                    .testTag("export_profile_${profile.id}")
                            ) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isProfileActive) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isProfileActive) OnNeonGreen else TextWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    PremiumStudioSlider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 15f..100f,
                        label = "Encoding Quality",
                        valueText = "$quality%"
                    )

                    PremiumStudioSlider(
                        value = scalePercent.toFloat(),
                        onValueChange = { scalePercent = it.toInt() },
                        valueRange = 25f..100f,
                        label = "Resolution Scale",
                        valueText = "$scalePercent%"
                    )
                }
            }

            // 3. Multi-Photo Selector Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PanelBg,
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
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
                        Text(
                            text = "Select Photos to Compress (${activeTargets.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CardBg,
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        batchSelectedIds.clear()
                                        batchSelectedIds.addAll(allPhotos.map { it.id })
                                    }
                            ) {
                                Text(
                                    text = "Select All",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeonGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allPhotos, key = { it.id }) { photo ->
                            val isSelected = photo.id in batchSelectedIds || (batchSelectedIds.isEmpty() && selectedPhoto?.id == photo.id)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonGreen else BorderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedPhoto = photo
                                        if (batchSelectedIds.contains(photo.id)) {
                                            if (batchSelectedIds.size > 1) batchSelectedIds.remove(photo.id)
                                        } else {
                                            batchSelectedIds.add(photo.id)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = PhotoImageProcessor.resolveCoilModel(photo.filePath),
                                    contentDescription = photo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(18.dp)
                                            .background(NeonGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = OnNeonGreen,
                                            modifier = Modifier.size(11.dp)
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
