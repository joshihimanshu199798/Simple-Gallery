package com.example.ui.studio

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCompleteUiTheme

enum class StudioSubScreen {
    DASHBOARD,
    PHOTO_EDITOR,
    COLLAGE_MAKER,
    COMPRESSION_STUDIO,
    BATCH_EDITOR,
    RAW_EDITOR,
    PRIVATE_VAULT,
    RECYCLE_BIN
}

enum class EditorToolTab(val label: String) {
    CROP("Crop & Rotate"),
    LIGHT("Light & Tone"),
    COLOR("Color & Warmth"),
    FILTERS("Filters"),
    DETAIL("Effects & Detail")
}

enum class FilterCategory(val label: String) {
    CINEMATIC("Cinematic"),
    FILM("Film"),
    VINTAGE("Vintage"),
    PORTRAIT("Portrait"),
    NATURE("Nature"),
    BW("Black & White"),
    NIGHT("Night")
}

data class StudioFilterItem(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val description: String,
    val matrixArray: FloatArray,
    val previewGradient: List<Color>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StudioFilterItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class HslColorBand(val label: String, val centerHue: Float, val bandColor: Color) {
    RED("Red", 0f, Color(0xFFEF4444)),
    ORANGE("Orange", 30f, Color(0xFFF97316)),
    YELLOW("Yellow", 60f, Color(0xFFEAB308)),
    GREEN("Green", 120f, Color(0xFF22C55E)),
    CYAN("Cyan", 180f, Color(0xFF06B6D4)),
    BLUE("Blue", 240f, Color(0xFF3B82F6)),
    PURPLE("Purple", 280f, Color(0xFFA855F7)),
    MAGENTA("Magenta", 320f, Color(0xFFEC4899))
}

data class HslBandSetting(
    val hueShift: Float = 0f, // -30..+30
    val satShift: Float = 0f, // -100..+100
    val lumShift: Float = 0f  // -100..+100
)

enum class CurveChannel(val label: String, val tint: Color) {
    RGB("RGB", Color.White),
    RED("Red", Color(0xFFEF4444)),
    GREEN("Green", Color(0xFF22C55E)),
    BLUE("Blue", Color(0xFF3B82F6))
}

data class ColorGradingTarget(
    val hue: Float = 0f,        // 0..360
    val saturation: Float = 0f, // 0..100
    val luminance: Float = 0f   // -100..+100
)

data class SelectiveStroke(
    val points: List<androidx.compose.ui.geometry.Offset>,
    val brushSize: Float,
    val isEraser: Boolean
)

data class CompressionPreset(
    val format: String, // JPEG, WebP, PNG
    val quality: Int,   // 10..100
    val scalePercent: Int, // 25..100
    val preserveExif: Boolean
)

enum class CollageLayoutMode(val label: String, val minPhotos: Int, val maxPhotos: Int) {
    GRID_EQUAL("Equal Grid", 2, 9),
    THEN_NOW_SPLIT("Then & Now Split", 2, 2),
    BENTO_HERO("Bento Hero", 3, 6),
    MOSAIC_SPLIT("Split Mosaic", 3, 8),
    FILMSTRIP_ROW("Filmstrip", 2, 10),
    MAGAZINE_COVER("Magazine", 2, 5),
    FREEFORM_POLAROID("Polaroid Wall", 2, 12)
}

enum class FrameBorderPreset(val label: String, val padRatio: Float, val frameColor: Color, val bottomWeight: Float = 1f) {
    NONE("No Border", 0f, Color.Transparent),
    MUSEUM_WHITE("Museum Mat", 0.06f, Color(0xFFFAF8F5), 1f),
    POLAROID_CLASSIC("Polaroid", 0.055f, Color(0xFFFFFDF9), 2.8f),
    GALLERY_SLATE("Obsidian Mat", 0.06f, Color(0xFF14161D), 1f),
    CINEMA_LETTERBOX("Letterbox", 0.07f, Color(0xFF08090C), 1.4f),
    WARM_CREAM("Editorial Cream", 0.055f, Color(0xFFF3ECE1), 1.3f)
}

data class StudioCustomRecipe(
    val id: String,
    val name: String,
    val exposure: Float = 0f,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vignette: Float = 0f,
    val grain: Float = 0f,
    val skinSmoothing: Float = 0f,
    val filterId: String = "",
    val filterIntensity: Float = 1f,
    val keystoneVertical: Float = 0f,
    val keystoneHorizontal: Float = 0f,
    val frameName: String = FrameBorderPreset.NONE.name
) {
    fun serialize(): String {
        val safeName = name.replace("|", " ").replace(";", " ").trim().ifEmpty { "My Preset" }
        return listOf(
            id,
            safeName,
            exposure,
            brightness,
            contrast,
            saturation,
            highlights,
            shadows,
            temperature,
            tint,
            vignette,
            grain,
            skinSmoothing,
            filterId,
            filterIntensity,
            keystoneVertical,
            keystoneHorizontal,
            frameName
        ).joinToString("|")
    }

    companion object {
        fun deserialize(raw: String): StudioCustomRecipe? {
            val parts = raw.split("|")
            if (parts.size < 15) return null
            return runCatching {
                StudioCustomRecipe(
                    id = parts[0],
                    name = parts[1],
                    exposure = parts[2].toFloat(),
                    brightness = parts[3].toFloat(),
                    contrast = parts[4].toFloat(),
                    saturation = parts[5].toFloat(),
                    highlights = parts[6].toFloat(),
                    shadows = parts[7].toFloat(),
                    temperature = parts[8].toFloat(),
                    tint = parts[9].toFloat(),
                    vignette = parts[10].toFloat(),
                    grain = parts[11].toFloat(),
                    skinSmoothing = parts[12].toFloat(),
                    filterId = parts[13],
                    filterIntensity = parts[14].toFloat(),
                    keystoneVertical = parts.getOrNull(15)?.toFloatOrNull() ?: 0f,
                    keystoneHorizontal = parts.getOrNull(16)?.toFloatOrNull() ?: 0f,
                    frameName = parts.getOrNull(17) ?: FrameBorderPreset.NONE.name
                )
            }.getOrNull()
        }

        val BUILT_IN_RECIPES = listOf(
            StudioCustomRecipe(
                id = "recipe_golden_film",
                name = "Golden Hour 35mm",
                exposure = 0.25f,
                brightness = 6f,
                contrast = 1.12f,
                saturation = 1.18f,
                highlights = -14f,
                shadows = 20f,
                temperature = 16f,
                tint = 4f,
                vignette = 22f,
                grain = 18f
            ),
            StudioCustomRecipe(
                id = "recipe_cinema_matte",
                name = "Cinema Matte",
                exposure = 0.05f,
                brightness = 2f,
                contrast = 1.22f,
                saturation = 0.90f,
                highlights = -22f,
                shadows = 28f,
                temperature = -10f,
                tint = -2f,
                vignette = 32f,
                grain = 24f,
                frameName = FrameBorderPreset.CINEMA_LETTERBOX.name
            ),
            StudioCustomRecipe(
                id = "recipe_fine_art_bw",
                name = "Leica Silver B&W",
                exposure = 0.15f,
                brightness = 4f,
                contrast = 1.32f,
                saturation = 0f,
                highlights = -10f,
                shadows = 15f,
                vignette = 28f,
                grain = 30f,
                frameName = FrameBorderPreset.MUSEUM_WHITE.name
            )
        )
    }
}

data class ExportProfilePreset(
    val id: String,
    val name: String,
    val format: String,
    val quality: Int,
    val scalePercent: Int
) {
    companion object {
        val DEFAULT_PROFILES = listOf(
            ExportProfilePreset("web_fast", "Web 1080p • 80% WEBP", "WEBP", 80, 75),
            ExportProfilePreset("social_hd", "Social Ready • 85% JPEG", "JPEG", 85, 85),
            ExportProfilePreset("archive_saver", "Storage Saver • 65% WEBP", "WEBP", 65, 60),
            ExportProfilePreset("print_master", "Print Master • 100% PNG", "PNG", 100, 100)
        )
    }
}

object StudioPreferenceStore {
    private const val PREF_FILE = "simple_gallery_studio_store"
    private const val KEY_RECIPES = "user_custom_recipes_v1"
    private const val KEY_PINNED_ALBUMS = "pinned_album_names_v1"

    fun loadUserRecipes(context: android.content.Context): List<StudioCustomRecipe> {
        val prefs = context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECIPES, null)
        if (raw.isNullOrBlank()) return StudioCustomRecipe.BUILT_IN_RECIPES
        val parsed = raw.split(";").mapNotNull { StudioCustomRecipe.deserialize(it) }
        return if (parsed.isEmpty()) StudioCustomRecipe.BUILT_IN_RECIPES else parsed
    }

    fun saveUserRecipe(context: android.content.Context, recipe: StudioCustomRecipe): List<StudioCustomRecipe> {
        val current = loadUserRecipes(context).toMutableList()
        current.removeAll { it.id == recipe.id || it.name.equals(recipe.name, ignoreCase = true) }
        current.add(0, recipe)
        val trimmed = current.take(15)
        context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECIPES, trimmed.joinToString(";") { it.serialize() })
            .apply()
        return trimmed
    }

    fun deleteUserRecipe(context: android.content.Context, recipeId: String): List<StudioCustomRecipe> {
        val current = loadUserRecipes(context).filterNot { it.id == recipeId }
        context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECIPES, current.joinToString(";") { it.serialize() })
            .apply()
        return current
    }

    fun savePhotoEditHistory(context: android.content.Context, photoId: Long, recipe: StudioCustomRecipe) {
        context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("photo_recipe_$photoId", recipe.serialize())
            .apply()
    }

    fun loadPhotoEditHistory(context: android.content.Context, photoId: Long): StudioCustomRecipe? {
        val raw = context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .getString("photo_recipe_$photoId", null) ?: return null
        return StudioCustomRecipe.deserialize(raw)
    }

    fun loadPinnedAlbums(context: android.content.Context): Set<String> {
        return context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .getStringSet(KEY_PINNED_ALBUMS, emptySet()) ?: emptySet()
    }

    fun togglePinnedAlbum(context: android.content.Context, albumName: String): Set<String> {
        val current = loadPinnedAlbums(context).toMutableSet()
        if (current.contains(albumName)) {
            current.remove(albumName)
        } else {
            current.add(albumName)
        }
        context.getSharedPreferences(PREF_FILE, android.content.Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_PINNED_ALBUMS, current)
            .apply()
        return current
    }
}

/**
 * Native M3-powered studio slider with silky-smooth gesture handling inside scroll containers,
 * bi-directional zero-anchor visualization, animated thumb feedback, and tap-on-badge reset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStudioSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String? = null,
    valueText: String? = null,
    defaultValue: Float? = null,
    testTag: String? = null
) {
    val theme = LocalCompleteUiTheme.current
    val accentColor = theme.primaryAccent
    val onAccentColor = theme.onPrimaryAccent
    val trackInactiveColor = theme.borderStroke
    val textPrimary = theme.textPrimary

    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isDragged || isPressed

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val clampedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val rawFraction = ((clampedValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = if (isActive) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "slider_fraction"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (isActive) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slider_thumb_scale"
    )

    val isBiDirectional = valueRange.start < 0f && valueRange.endInclusive > 0f
    val resolvedDefault = defaultValue ?: if (isBiDirectional) 0f else valueRange.start
    val zeroFraction = if (isBiDirectional) {
        ((0f - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (label != null || valueText != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                }
                if (valueText != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.32f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onValueChange(resolvedDefault) }
                    ) {
                        Text(
                            text = valueText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Slider(
            value = clampedValue,
            onValueChange = { newVal ->
                onValueChange(newVal.coerceIn(valueRange.start, valueRange.endInclusive))
            },
            valueRange = valueRange,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = trackInactiveColor
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(thumbScale)
                        .shadow(4.dp, CircleShape, clip = false)
                        .background(accentColor, CircleShape)
                        .border(2.5.dp, onAccentColor, CircleShape)
                )
            },
            track = {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                ) {
                    val trackWidth = size.width.coerceAtLeast(1f)
                    val trackHeight = 6.dp.toPx()
                    val centerY = size.height / 2f
                    val trackRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)

                    // 1. Inactive Track Pill
                    drawRoundRect(
                        color = trackInactiveColor,
                        topLeft = Offset(0f, centerY - trackHeight / 2f),
                        size = Size(trackWidth, trackHeight),
                        cornerRadius = trackRadius
                    )

                    // 2. Active Track Pill
                    val thumbX = trackWidth * animatedFraction
                    if (isBiDirectional) {
                        val zeroX = trackWidth * zeroFraction
                        val leftX = minOf(zeroX, thumbX)
                        val rightX = maxOf(zeroX, thumbX)
                        val activeW = (rightX - leftX).coerceAtLeast(2.dp.toPx())
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(leftX, centerY - trackHeight / 2f),
                            size = Size(activeW, trackHeight),
                            cornerRadius = trackRadius
                        )
                        // Center zero tick mark
                        drawCircle(
                            color = textPrimary.copy(alpha = 0.45f),
                            radius = 2.dp.toPx(),
                            center = Offset(zeroX, centerY)
                        )
                    } else {
                        val activeW = thumbX.coerceAtLeast(trackHeight)
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(0f, centerY - trackHeight / 2f),
                            size = Size(activeW, trackHeight),
                            cornerRadius = trackRadius
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        )
    }
}
