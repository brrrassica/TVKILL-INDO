/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.ui.brandlist

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern

class BrandListStateTest {

    private fun pattern() = IrPattern(
        carrierHz = 38000,
        periods = intArrayOf(1, 1, 10, 10),
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
    )

    private fun brand(
        id: String,
        name: String,
        region: Brand.Region,
    ) = Brand(id = id, displayName = name, region = region, patterns = listOf(pattern()))

    private val brands = listOf(
        brand("samsung", "Samsung", Brand.Region.PREMIUM),
        brand("lg", "LG", Brand.Region.PREMIUM),
        brand("tcl", "TCL", Brand.Region.MAINSTREAM),
        brand("polytron", "Polytron", Brand.Region.LOCAL_ID),
        brand("akari", "Akari", Brand.Region.LOCAL_ID),
        brand("mitsubishi", "Mitsubishi", Brand.Region.LEGACY),
    )

    @Test
    fun emptyStateShowsEverythingInCatalogOrder() {
        val result = BrandListState().filter(brands)
        assertEquals(brands, result)
    }

    @Test
    fun queryMatchesDisplayNameCaseInsensitive() {
        val result = BrandListState(query = "POLY").filter(brands)
        assertEquals(listOf("polytron"), result.map { it.id })
    }

    @Test
    fun queryTrimsWhitespace() {
        val result = BrandListState(query = "  tcl  ").filter(brands)
        assertEquals(listOf("tcl"), result.map { it.id })
    }

    @Test
    fun queryWithoutMatchReturnsEmpty() {
        assertEquals(emptyList<Brand>(), BrandListState(query = "nokia").filter(brands))
    }

    @Test
    fun regionFilterSelectsOnlyThatRegion() {
        val result = BrandListState(selectedRegion = RegionFilter.LOCAL_ID).filter(brands)
        assertEquals(listOf("polytron", "akari"), result.map { it.id })
    }

    @Test
    fun queryAndRegionCombine() {
        val result = BrandListState(
            query = "a",
            selectedRegion = RegionFilter.PREMIUM,
        ).filter(brands)
        // "a" matches Samsung and Akari by name, but region narrows to Samsung.
        assertEquals(listOf("samsung"), result.map { it.id })
    }

    @Test
    fun catalogOrderIsNeverReshuffled() {
        val shuffled = listOf(brands[3], brands[0], brands[5], brands[2], brands[4], brands[1])
        val result = BrandListState().filter(shuffled)
        assertEquals(shuffled, result)
    }
}
