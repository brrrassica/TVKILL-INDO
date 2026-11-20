/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.ui.brandlist

import tech.alatcerdas.tvkillindo.domain.Brand

/**
 * Pure, Android-free state and filtering logic for the Compose brand-list
 * screen (M3). Kept free of Compose/Android types so the filtering contract
 * is unit-testable on the JVM and reusable by later screens (repetitive mode
 * brand chooser, universal mode).
 */
data class BrandListState(
    val query: String = "",
    val selectedRegion: RegionFilter = RegionFilter.ALL,
) {
    /**
     * Filter the given brands by search query (display name, case-insensitive)
     * and region. The catalog's own ordering is preserved — it encodes pattern
     * reliability priority (best first), which must not be reshuffled here.
     */
    fun filter(brands: List<Brand>): List<Brand> =
        brands.filter { brand ->
            selectedRegion.matches(brand) && brand.displayName.contains(
                query.trim(),
                ignoreCase = true,
            )
        }
}

/** Filter chips shown above the brand list. */
enum class RegionFilter {
    ALL,
    PREMIUM,
    MAINSTREAM,
    LOCAL_ID,
    LEGACY;

    /** Whether a brand from the catalog passes this filter. */
    fun matches(brand: Brand): Boolean = when (this) {
        ALL -> true
        PREMIUM -> brand.region == Brand.Region.PREMIUM
        MAINSTREAM -> brand.region == Brand.Region.MAINSTREAM
        LOCAL_ID -> brand.region == Brand.Region.LOCAL_ID
        LEGACY -> brand.region == Brand.Region.LEGACY
    }
}
