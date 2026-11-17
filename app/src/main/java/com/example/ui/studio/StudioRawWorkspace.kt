package com.example.ui.studio

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor
import com.example.ui.theme.LocalCompleteUiTheme
import java.util.Locale
import kotlin.math.pow

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

private enum class RawParamDial(val label: String) {
    KELVIN("Kelvin WB"),
    TINT("Tint"),
    EXPOSURE("Exposure EV"),
    HIGHLIGHTS("Highlights"),
    SHADOWS("Shadows"),
    DENOISE("Denoise")
}

@Composable
fun StudioRawWorkspace(
    initialPhoto: PhotoEntity?,
    allPhotos: List<PhotoEntity>,
    onClose: () -> Unit,
    onExportRawAsJpeg: (
        photo: PhotoEntity,
        kelvinTemp: Int,
        tint: Float,
        exposureEv: Float,
        highlightRecovery: Float,
        shadowRecovery: Float,
        chromaDenoise: Float
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }
    val context = LocalContext.current

    var selectedPhoto by remember { mutableStateOf(initialPhoto ?: allPhotos.firstOrNull()) }
    var activeDial by remember { mutableStateOf(RawParamDial.KELVIN) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

    var kelvinTemp by remember { mutableIntStateOf(5500) } // 2500K..9500K
    var tint by remember { mutableFloatStateOf(0f) } // -50..+50
    var exposureEv by remember { mutableFloatStateOf(0f) } // -3.0..+3.0 EV
    var highlightRecovery by remember { mutableFloatStateOf(30f) } // 0..100
    var shadowRecovery by remember { mutableFloatStateOf(40f) } // 0..100
    var chromaDenoise by remember { mutableFloatStateOf(20f) } // 0..100

    val liveMatrix = remember(kelvinTemp, tint, exposureEv, highlightRecovery, shadowRecovery) {
        val tempNorm = (kelvinTemp - 5500) / 4000f
        val rShift = tempNorm * 28f + tint * 0.4f
        val gShift = -tint * 0.6f
        val bShift = -tempNorm * 28f

        val expScale = 2.0.pow(exposureEv.toDouble()).toFloat()
        ColorMatrix(
            floatArrayOf(
                expScale, 0f, 0f, 0f, rShift + (shadowRecovery * 0.2f),
                0f, expScale, 0f, 0f, gShift + (shadowRecovery * 0.2f),
                0f, 0f, expScale, 0f, bShift + (shadowRecovery * 0.2f),
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("studio_raw_workspace")
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
                    text = "RAW & Kelvin Develop",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextWhite
                )
                Text(
                    text = "${kelvinTemp}K • ${String.format(Locale.US, "%+.1f EV", exposureEv)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = NeonGreen
                )
            }

            Button(
                onClick = {
                    if (selectedPhoto != null) {
                        onExportRawAsJpeg(
                            selectedPhoto!!,
                            kelvinTemp,
                            tint,
                            exposureEv,
                            highlightRecovery,
                            shadowRecovery,
                            chromaDenoise
                        )
                    }
                },
                enabled = selectedPhoto != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = OnNeonGreen
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = OnNeonGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Develop", fontWeight = FontWeight.Bold, color = OnNeonGreen, fontSize = 12.sp)
            }
        }

        // MAIN VIEWPORT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF090A0D))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedPhoto != null) {
                val rawPreviewModel = remember(selectedPhoto!!.id, selectedPhoto!!.filePath) {
                    PhotoImageProcessor.buildPreviewRequest(context, selectedPhoto!!.filePath, 1600)
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = rawPreviewModel,
                        contentDescription = selectedPhoto!!.title,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(liveMatrix),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // MODERN DIAL-DRIVEN RAW CONTROL DECK
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = PanelBg,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Kelvin Illuminant Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "3200K Tungsten" to 3200,
                        "5500K Daylight" to 5500,
                        "6500K Overcast" to 6500,
                        "7500K Golden" to 7500
                    ).forEach { (label, tempVal) ->
                        val isSelected = kelvinTemp == tempVal
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) NeonGreen.copy(alpha = 0.16f) else CardBg,
                            border = BorderStroke(1.dp, if (isSelected) NeonGreen else BorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    kelvinTemp = tempVal
                                    activeDial = RawParamDial.KELVIN
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) NeonGreen else TextWhite,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardBg,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                kelvinTemp = 5500
                                tint = 0f
                                exposureEv = 0f
                                highlightRecovery = 30f
                                shadowRecovery = 40f
                                chromaDenoise = 20f
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text("Reset", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }

                // Parameter Dial Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RawParamDial.entries.forEach { dial ->
                        val isSelected = activeDial == dial
                        val valLabel = when (dial) {
                            RawParamDial.KELVIN -> "${kelvinTemp}K"
                            RawParamDial.TINT -> "${tint.toInt()}"
                            RawParamDial.EXPOSURE -> String.format(Locale.US, "%+.1f", exposureEv)
                            RawParamDial.HIGHLIGHTS -> "${highlightRecovery.toInt()}%"
                            RawParamDial.SHADOWS -> "${shadowRecovery.toInt()}%"
                            RawParamDial.DENOISE -> "${chromaDenoise.toInt()}%"
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) NeonGreen else CardBg,
                            border = BorderStroke(1.dp, if (isSelected) NeonGreen else BorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { activeDial = dial }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dial.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.5.sp
                                    ),
                                    color = if (isSelected) OnNeonGreen else TextWhite
                                )
                                Text(
                                    text = valLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isSelected) OnNeonGreen else NeonGreen
                                )
                            }
                        }
                    }
                }

                // Active Parameter Precision Slider
                when (activeDial) {
                    RawParamDial.KELVIN -> PremiumStudioSlider(
                        value = kelvinTemp.toFloat(),
                        onValueChange = { kelvinTemp = it.toInt() },
                        valueRange = 2500f..9500f,
                        label = "White Balance Temperature",
                        valueText = "${kelvinTemp}K"
                    )
                    RawParamDial.TINT -> PremiumStudioSlider(
                        value = tint,
                        onValueChange = { tint = it },
                        valueRange = -50f..50f,
                        label = "Green / Magenta Tint",
                        valueText = "${if (tint > 0) "+" else ""}${tint.toInt()}"
                    )
                    RawParamDial.EXPOSURE -> PremiumStudioSlider(
                        value = exposureEv,
                        onValueChange = { exposureEv = it },
                        valueRange = -3f..3f,
                        label = "Exposure Compensation",
                        valueText = String.format(Locale.US, "%+.1f EV", exposureEv)
                    )
                    RawParamDial.HIGHLIGHTS -> PremiumStudioSlider(
                        value = highlightRecovery,
                        onValueChange = { highlightRecovery = it },
                        valueRange = 0f..100f,
                        label = "Highlight Dynamic Recovery",
                        valueText = "${highlightRecovery.toInt()}%"
                    )
                    RawParamDial.SHADOWS -> PremiumStudioSlider(
                        value = shadowRecovery,
                        onValueChange = { shadowRecovery = it },
                        valueRange = 0f..100f,
                        label = "Shadow Fill & Detail",
                        valueText = "${shadowRecovery.toInt()}%"
                    )
                    RawParamDial.DENOISE -> PremiumStudioSlider(
                        value = chromaDenoise,
                        onValueChange = { chromaDenoise = it },
                        valueRange = 0f..100f,
                        label = "Chroma Luminance Denoise",
                        valueText = "${chromaDenoise.toInt()}%"
                    )
                }

                // Photo Switcher Strip + Browse Grid Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPhoto?.title ?: "Select Photo",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NeonGreen.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, NeonGreen),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showPhotoPickerSheet = true }
                            .testTag("raw_browse_photos_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Select Photo (${allPhotos.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allPhotos.take(80), key = { it.id }) { photo ->
                        val isSelected = selectedPhoto?.id == photo.id
                        val thumbReq = remember(photo.id, photo.filePath) {
                            PhotoImageProcessor.buildThumbnailRequest(context, photo.filePath, 160, 160)
                        }
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonGreen else BorderColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPhoto = photo }
                        ) {
                            AsyncImage(
                                model = thumbReq,
                                contentDescription = photo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        if (showPhotoPickerSheet) {
            StudioPhotoPickerSheet(
                photos = allPhotos,
                selectedIds = setOfNotNull(selectedPhoto?.id),
                isMultiSelect = false,
                title = "Select Photo for White Balance & RAW",
                onDismiss = { showPhotoPickerSheet = false },
                onSelectSinglePhoto = { chosen ->
                    selectedPhoto = chosen
                    showPhotoPickerSheet = false
                }
            )
        }
    }
}
