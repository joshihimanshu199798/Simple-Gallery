package com.example.ui.studio

import androidx.compose.ui.graphics.Color

object CinematicFilterCatalog {

    val identityMatrix = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    fun interpolateMatrix(target: FloatArray, intensity: Float): FloatArray {
        val clamped = intensity.coerceIn(0f, 1f)
        val result = FloatArray(20)
        for (i in 0 until 20) {
            val base = identityMatrix[i]
            val dest = target[i]
            result[i] = base + (dest - base) * clamped
        }
        return result
    }

    val ALL_PRESETS: List<StudioFilterItem> = listOf(
        // === 1. FILM ===
        StudioFilterItem(
            id = "film_kodak_gold100",
            name = "Kodak Gold 100",
            category = FilterCategory.FILM,
            description = "Iconic sun-drenched golden warmth, nostalgic amber highlights & rich skin",
            matrixArray = floatArrayOf(
                1.18f, 0.06f, 0.01f, 0f, 14f,
                0.04f, 1.08f, 0.02f, 0f, 8f,
                -0.04f, 0.02f, 0.88f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF59E0B), Color(0xFFE57A5C))
        ),
        StudioFilterItem(
            id = "film_velvia50",
            name = "Fuji Velvia 50",
            category = FilterCategory.FILM,
            description = "Legendary slide film high saturation, vivid skies & lush greens",
            matrixArray = floatArrayOf(
                1.22f, 0.05f, 0.02f, 0f, 10f,
                0.02f, 1.24f, 0.04f, 0f, 8f,
                0.04f, 0.02f, 1.25f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF84CC16), Color(0xFF0EA5E9))
        ),
        StudioFilterItem(
            id = "film_cinestill800",
            name = "CineStill 800T",
            category = FilterCategory.FILM,
            description = "Tungsten balance, vibrant red halation & deep blue undertones",
            matrixArray = floatArrayOf(
                1.15f, 0.02f, 0.05f, 0f, 12f,
                -0.02f, 0.96f, 0.08f, 0f, -4f,
                0.04f, 0.04f, 1.28f, 0f, 18f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFEF4444), Color(0xFF2563EB))
        ),
        StudioFilterItem(
            id = "film_portra400",
            name = "Portra 400",
            category = FilterCategory.FILM,
            description = "Smooth natural skin tones, soft pastels & gentle latitude",
            matrixArray = floatArrayOf(
                1.12f, 0.03f, 0.02f, 0f, 8f,
                0.03f, 1.04f, 0.03f, 0f, 6f,
                0.02f, 0.04f, 0.94f, 0f, -2f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFDBA74), Color(0xFFFED7AA))
        ),
        StudioFilterItem(
            id = "film_kodachrome64",
            name = "Kodachrome 64",
            category = FilterCategory.FILM,
            description = "Rich classic 1960s warmth, punchy reds and yellow-greens",
            matrixArray = floatArrayOf(
                1.20f, 0.06f, 0.02f, 0f, 14f,
                0.04f, 1.10f, 0.02f, 0f, 6f,
                0.02f, 0.02f, 0.88f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFDC2626), Color(0xFFFBBF24))
        ),
        StudioFilterItem(
            id = "film_ektar100",
            name = "Ektar 100",
            category = FilterCategory.FILM,
            description = "Ultra-fine grain emulsion with high contrast & crisp clarity",
            matrixArray = floatArrayOf(
                1.16f, 0.02f, 0.02f, 0f, 6f,
                0.02f, 1.14f, 0.02f, 0f, 6f,
                0.02f, 0.02f, 1.16f, 0f, 8f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF3B82F6), Color(0xFFEF4444))
        ),
        StudioFilterItem(
            id = "film_tri_x",
            name = "Tri-X 400 Grain",
            category = FilterCategory.FILM,
            description = "Gritty silver-rich analog black and white emulsion",
            matrixArray = floatArrayOf(
                0.33f, 0.59f, 0.11f, 0f, -5f,
                0.33f, 0.59f, 0.11f, 0f, -5f,
                0.33f, 0.59f, 0.11f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFE2E8F0), Color(0xFF0F172A))
        ),

        // === 2. CINEMATIC ===
        StudioFilterItem(
            id = "cine_blockbuster",
            name = "Teal & Orange",
            category = FilterCategory.CINEMATIC,
            description = "Hollywood blockbuster warm skin & cyan shadows",
            matrixArray = floatArrayOf(
                1.18f, 0.04f, 0.02f, 0f, 10f,
                0.02f, 1.05f, 0.05f, 0f, 0f,
                -0.08f, 0.02f, 1.25f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF97316), Color(0xFF06B6D4))
        ),
        StudioFilterItem(
            id = "cine_blade_runner",
            name = "Neo Noir 2049",
            category = FilterCategory.CINEMATIC,
            description = "Smoky amber highlights with dark dystopian violet shadows",
            matrixArray = floatArrayOf(
                1.25f, 0.05f, 0f, 0f, 15f,
                0.04f, 0.92f, 0.02f, 0f, -5f,
                0.12f, 0.03f, 1.15f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFE11D48), Color(0xFF8B5CF6))
        ),
        StudioFilterItem(
            id = "cine_scifi_cyan",
            name = "Deep Odyssey",
            category = FilterCategory.CINEMATIC,
            description = "Subtle desaturated cold sci-fi atmosphere",
            matrixArray = floatArrayOf(
                0.88f, 0.08f, 0.04f, 0f, -8f,
                0.04f, 1.08f, 0.08f, 0f, 2f,
                0.02f, 0.10f, 1.28f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF0284C7), Color(0xFF0F172A))
        ),
        StudioFilterItem(
            id = "cine_golden_dusk",
            name = "Sunset Cinema",
            category = FilterCategory.CINEMATIC,
            description = "Rich golden hour flares and warm cinematic glow",
            matrixArray = floatArrayOf(
                1.24f, 0.08f, 0f, 0f, 20f,
                0.06f, 1.08f, 0.02f, 0f, 8f,
                -0.05f, 0.02f, 0.85f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF59E0B), Color(0xFFDC2626))
        ),
        StudioFilterItem(
            id = "cine_matrix_code",
            name = "Matrix Wash",
            category = FilterCategory.CINEMATIC,
            description = "Stylized darkroom phosphor green tint in shadows",
            matrixArray = floatArrayOf(
                0.85f, 0.08f, 0.02f, 0f, -10f,
                0.04f, 1.25f, 0.04f, 0f, 18f,
                0.02f, 0.08f, 0.88f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF10B981), Color(0xFF064E3B))
        ),
        StudioFilterItem(
            id = "cine_monolith",
            name = "Monolith Steel",
            category = FilterCategory.CINEMATIC,
            description = "High dynamic range cold metallic architectural grade",
            matrixArray = floatArrayOf(
                0.95f, 0.05f, 0.05f, 0f, 0f,
                0.04f, 1.02f, 0.08f, 0f, 4f,
                0.04f, 0.08f, 1.22f, 0f, 14f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF94A3B8), Color(0xFF334155))
        ),

        // === 3. VINTAGE ===
        StudioFilterItem(
            id = "vint_polaroid600",
            name = "Polaroid 600",
            category = FilterCategory.VINTAGE,
            description = "Instant film chemical haze, warm cyan cast & faded blacks",
            matrixArray = floatArrayOf(
                1.08f, 0.04f, 0.02f, 0f, 16f,
                0.02f, 1.02f, 0.04f, 0f, 14f,
                0.06f, 0.04f, 0.92f, 0f, 22f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFEF3C7), Color(0xFF67E8F9))
        ),
        StudioFilterItem(
            id = "vint_1970_daze",
            name = "1970s Gold Daze",
            category = FilterCategory.VINTAGE,
            description = "Super 8 nostalgic warm haze, amber glow and soft edges",
            matrixArray = floatArrayOf(
                1.26f, 0.08f, 0f, 0f, 24f,
                0.08f, 1.12f, 0.02f, 0f, 12f,
                -0.08f, -0.02f, 0.82f, 0f, -18f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF59E0B), Color(0xFFB45309))
        ),
        StudioFilterItem(
            id = "vint_technicolor",
            name = "Technicolor 3-Strip",
            category = FilterCategory.VINTAGE,
            description = "1950s golden age saturated reds, deep blues and rich dye transfer",
            matrixArray = floatArrayOf(
                1.30f, 0.02f, 0.02f, 0f, 8f,
                0.02f, 1.25f, 0.02f, 0f, 4f,
                0.02f, 0.02f, 1.28f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFB91C1C), Color(0xFF1D4ED8))
        ),
        StudioFilterItem(
            id = "vint_daguerreotype",
            name = "Daguerreotype 1880",
            category = FilterCategory.VINTAGE,
            description = "Early silver-plated antique brown monochrome patina",
            matrixArray = floatArrayOf(
                0.36f, 0.54f, 0.12f, 0f, 28f,
                0.32f, 0.52f, 0.10f, 0f, 18f,
                0.24f, 0.44f, 0.08f, 0f, -6f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF78350F), Color(0xFFD97706))
        ),
        StudioFilterItem(
            id = "vint_vhs_tape",
            name = "VHS 1984",
            category = FilterCategory.VINTAGE,
            description = "Analog magnetic tape bloom, slight green shift and lifted baseline",
            matrixArray = floatArrayOf(
                0.96f, 0.04f, 0.06f, 0f, 14f,
                0.02f, 1.12f, 0.02f, 0f, 16f,
                0.08f, 0.02f, 1.05f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFA855F7), Color(0xFF10B981))
        ),
        StudioFilterItem(
            id = "vint_archival_sepia",
            name = "Darkroom Sepia",
            category = FilterCategory.VINTAGE,
            description = "Rich chocolate archival paper print toning",
            matrixArray = floatArrayOf(
                0.39f, 0.76f, 0.18f, 0f, 30f,
                0.34f, 0.68f, 0.16f, 0f, 16f,
                0.27f, 0.53f, 0.13f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF92400E), Color(0xFFFDE68A))
        ),

        // === 4. PORTRAIT ===
        StudioFilterItem(
            id = "port_porcelain",
            name = "Porcelain Glow",
            category = FilterCategory.PORTRAIT,
            description = "Flattering skin illumination, lowered micro-contrast & soft highlights",
            matrixArray = floatArrayOf(
                1.08f, 0.02f, 0.02f, 0f, 12f,
                0.02f, 1.06f, 0.02f, 0f, 10f,
                0.02f, 0.02f, 1.04f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFFEDD5), Color(0xFFF472B6))
        ),
        StudioFilterItem(
            id = "port_honey_warm",
            name = "Honey Bronze",
            category = FilterCategory.PORTRAIT,
            description = "Warm golden undertones, tanned skin accents and rich iris pop",
            matrixArray = floatArrayOf(
                1.16f, 0.04f, 0f, 0f, 14f,
                0.04f, 1.08f, 0.02f, 0f, 8f,
                -0.04f, 0.02f, 0.94f, 0f, -4f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFEA580C), Color(0xFFFBBF24))
        ),
        StudioFilterItem(
            id = "port_editorial_matte",
            name = "Vogue Matte",
            category = FilterCategory.PORTRAIT,
            description = "Modern fashion magazine matte finish with clean neutral tones",
            matrixArray = floatArrayOf(
                1.04f, 0.02f, 0.02f, 0f, 8f,
                0.02f, 1.02f, 0.02f, 0f, 8f,
                0.02f, 0.02f, 1.02f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFE2E8F0), Color(0xFF64748B))
        ),
        StudioFilterItem(
            id = "port_studio_flash",
            name = "Studio Strobe",
            category = FilterCategory.PORTRAIT,
            description = "Crisp directional studio lighting pop with deep black isolation",
            matrixArray = floatArrayOf(
                1.18f, 0f, 0f, 0f, 2f,
                0f, 1.18f, 0f, 0f, 2f,
                0f, 0f, 1.18f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFFFFFF), Color(0xFF1E293B))
        ),
        StudioFilterItem(
            id = "port_rose_petal",
            name = "Rose Petal",
            category = FilterCategory.PORTRAIT,
            description = "Delicate blush pink undertones and romantic soft focus glow",
            matrixArray = floatArrayOf(
                1.16f, 0.02f, 0.04f, 0f, 16f,
                0.02f, 1.02f, 0.02f, 0f, 6f,
                0.06f, 0.02f, 1.08f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFB7185), Color(0xFFFCE7F3))
        ),
        StudioFilterItem(
            id = "port_dramatic_shadows",
            name = "Rembrandt Mood",
            category = FilterCategory.PORTRAIT,
            description = "Classical master chiaroscuro with velvety shadows and piercing catchlights",
            matrixArray = floatArrayOf(
                1.25f, 0.02f, 0f, 0f, -8f,
                0.02f, 1.15f, 0f, 0f, -12f,
                0f, 0f, 0.98f, 0f, -16f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF713F12), Color(0xFF000000))
        ),

        // === 5. NATURE ===
        StudioFilterItem(
            id = "nat_emerald_canopy",
            name = "Emerald Canopy",
            category = FilterCategory.NATURE,
            description = "Deep lush rainforest foliage, boosted greens and filtered dappled sunlight",
            matrixArray = floatArrayOf(
                0.94f, 0.04f, 0.02f, 0f, -4f,
                0.04f, 1.32f, 0.04f, 0f, 14f,
                0.02f, 0.04f, 0.96f, 0f, -2f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF059669), Color(0xFF10B981))
        ),
        StudioFilterItem(
            id = "nat_alpine_clarity",
            name = "Alpine Peak",
            category = FilterCategory.NATURE,
            description = "Crisp mountain air, punchy skies and brilliant glacial white snow",
            matrixArray = floatArrayOf(
                1.08f, 0.02f, 0.04f, 0f, 2f,
                0.02f, 1.12f, 0.04f, 0f, 4f,
                0.04f, 0.04f, 1.28f, 0f, 16f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF38BDF8), Color(0xFFF8FAFC))
        ),
        StudioFilterItem(
            id = "nat_autumn_amber",
            name = "Autumn Foliage",
            category = FilterCategory.NATURE,
            description = "Vibrant maple crimson, burnt orange leaves and rich warm earth",
            matrixArray = floatArrayOf(
                1.30f, 0.06f, 0f, 0f, 16f,
                0.06f, 1.10f, 0.02f, 0f, 6f,
                -0.08f, -0.02f, 0.82f, 0f, -14f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFDC2626), Color(0xFFD97706))
        ),
        StudioFilterItem(
            id = "nat_ocean_abyss",
            name = "Azure Reef",
            category = FilterCategory.NATURE,
            description = "Vibrant turquoise shallows, sapphire ocean depths and bright sands",
            matrixArray = floatArrayOf(
                0.86f, 0.04f, 0.06f, 0f, -10f,
                0.04f, 1.16f, 0.12f, 0f, 8f,
                0.04f, 0.10f, 1.35f, 0f, 22f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF06B6D4), Color(0xFF1D4ED8))
        ),
        StudioFilterItem(
            id = "nat_sahara_dune",
            name = "Desert Mirage",
            category = FilterCategory.NATURE,
            description = "Sun-bleached sandstone, terracotta warmth and heat-haze gradient",
            matrixArray = floatArrayOf(
                1.26f, 0.08f, 0f, 0f, 22f,
                0.06f, 1.12f, 0.02f, 0f, 12f,
                -0.06f, 0f, 0.86f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFCA8A04), Color(0xFFF97316))
        ),
        StudioFilterItem(
            id = "nat_botanical_macro",
            name = "Botanical Macro",
            category = FilterCategory.NATURE,
            description = "Vivid petal saturation and tactile organic micro-contrast",
            matrixArray = floatArrayOf(
                1.15f, 0.04f, 0.02f, 0f, 6f,
                0.02f, 1.25f, 0.04f, 0f, 8f,
                0.04f, 0.02f, 1.18f, 0f, 8f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF10B981), Color(0xFFA855F7))
        ),

        // === 6. BLACK & WHITE ===
        StudioFilterItem(
            id = "bw_ansel_silver",
            name = "Ansel Zone V",
            category = FilterCategory.BW,
            description = "Legendary Zone System tonal balance with pristine white to deep black",
            matrixArray = floatArrayOf(
                0.30f, 0.59f, 0.11f, 0f, 0f,
                0.30f, 0.59f, 0.11f, 0f, 0f,
                0.30f, 0.59f, 0.11f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFE2E8F0), Color(0xFF475569))
        ),
        StudioFilterItem(
            id = "bw_high_contrast",
            name = "Noir Silhouette",
            category = FilterCategory.BW,
            description = "Crushed blacks, searing brights and stark graphic silhouettes",
            matrixArray = floatArrayOf(
                0.40f, 0.70f, 0.14f, 0f, -25f,
                0.40f, 0.70f, 0.14f, 0f, -25f,
                0.40f, 0.70f, 0.14f, 0f, -25f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFFFFFF), Color(0xFF000000))
        ),
        StudioFilterItem(
            id = "bw_soft_charcoal",
            name = "Velvet Charcoal",
            category = FilterCategory.BW,
            description = "Silky low-contrast midtones with lifted shadows and soft highlights",
            matrixArray = floatArrayOf(
                0.28f, 0.52f, 0.10f, 0f, 20f,
                0.28f, 0.52f, 0.10f, 0f, 20f,
                0.28f, 0.52f, 0.10f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFCBD5E1), Color(0xFF64748B))
        ),
        StudioFilterItem(
            id = "bw_infrared",
            name = "Infrared Wood Effect",
            category = FilterCategory.BW,
            description = "Ethereal luminous white foliage and inky black skies",
            matrixArray = floatArrayOf(
                0.15f, 0.75f, 0.35f, 0f, 10f,
                0.15f, 0.75f, 0.35f, 0f, 10f,
                0.15f, 0.75f, 0.35f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF1F5F9), Color(0xFF1E293B))
        ),
        StudioFilterItem(
            id = "bw_newspaper",
            name = "Press Print 1930",
            category = FilterCategory.BW,
            description = "Halftone newsprint aesthetic with punchy micro-tonality",
            matrixArray = floatArrayOf(
                0.32f, 0.62f, 0.12f, 0f, -12f,
                0.32f, 0.62f, 0.12f, 0f, -12f,
                0.32f, 0.62f, 0.12f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFE2E8F0), Color(0xFF0F172A))
        ),
        StudioFilterItem(
            id = "bw_platinum_print",
            name = "Platinum Palladium",
            category = FilterCategory.BW,
            description = "Exquisite archival platinum print with delicate warm cream tint",
            matrixArray = floatArrayOf(
                0.32f, 0.58f, 0.11f, 0f, 12f,
                0.30f, 0.56f, 0.10f, 0f, 8f,
                0.26f, 0.50f, 0.08f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFEF3C7), Color(0xFF334155))
        ),

        // === 7. NIGHT PHOTOGRAPHY ===
        StudioFilterItem(
            id = "night_tokyo_cyber",
            name = "Tokyo Cyberpunk",
            category = FilterCategory.NIGHT,
            description = "Vibrant neon magenta reflections, laser cyan signage & rainy asphalt",
            matrixArray = floatArrayOf(
                1.25f, 0.05f, 0.15f, 0f, 10f,
                0.02f, 0.98f, 0.10f, 0f, -5f,
                0.15f, 0.08f, 1.45f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFEC4899), Color(0xFF06B6D4))
        ),
        StudioFilterItem(
            id = "night_sodium_vapor",
            name = "Sodium Amber",
            category = FilterCategory.NIGHT,
            description = "Atmospheric vintage orange street lamp haze and moody alleys",
            matrixArray = floatArrayOf(
                1.35f, 0.10f, 0f, 0f, 25f,
                0.08f, 1.15f, 0f, 0f, 12f,
                -0.12f, -0.04f, 0.70f, 0f, -25f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFF59E0B), Color(0xFF78350F))
        ),
        StudioFilterItem(
            id = "night_aurora_green",
            name = "Aurora Borealis",
            category = FilterCategory.NIGHT,
            description = "Ethereal arctic green ionization glow across night skies",
            matrixArray = floatArrayOf(
                0.85f, 0.05f, 0.05f, 0f, -8f,
                0.05f, 1.40f, 0.10f, 0f, 20f,
                0.05f, 0.15f, 1.25f, 0f, 16f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF10B981), Color(0xFF38BDF8))
        ),
        StudioFilterItem(
            id = "night_midnight_blue",
            name = "Midnight Inky Blue",
            category = FilterCategory.NIGHT,
            description = "Clean cool blue hour grading, crisp starpoints & deep skies",
            matrixArray = floatArrayOf(
                0.90f, 0.02f, 0.08f, 0f, -6f,
                0.02f, 0.98f, 0.12f, 0f, -2f,
                0.04f, 0.12f, 1.38f, 0f, 24f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF1E3A8A), Color(0xFF60A5FA))
        ),
        StudioFilterItem(
            id = "night_afterhours_neon",
            name = "After Hours Club",
            category = FilterCategory.NIGHT,
            description = "Deep violet mood with punchy chartreuse and neon electric sparks",
            matrixArray = floatArrayOf(
                1.20f, 0.02f, 0.18f, 0f, 12f,
                -0.04f, 0.90f, 0.04f, 0f, -10f,
                0.12f, 0.04f, 1.40f, 0f, 22f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFF8B5CF6), Color(0xFFF43F5E))
        ),
        StudioFilterItem(
            id = "night_city_lights_bokeh",
            name = "City Bokeh Glow",
            category = FilterCategory.NIGHT,
            description = "Warm dreamlike specular highlights and dark velvety backdrop",
            matrixArray = floatArrayOf(
                1.22f, 0.08f, 0.04f, 0f, 18f,
                0.04f, 1.14f, 0.06f, 0f, 10f,
                0.02f, 0.06f, 1.15f, 0f, 14f,
                0f, 0f, 0f, 1f, 0f
            ),
            previewGradient = listOf(Color(0xFFFBBF24), Color(0xFF4F46E5))
        )
    )

    fun findFilter(id: String): StudioFilterItem? = ALL_PRESETS.firstOrNull { it.id == id }
}
