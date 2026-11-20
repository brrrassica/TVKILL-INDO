/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.data

import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern

/**
 * First real implementation of [PatternRepository], serving the 16 legacy
 * brands from [LegacyCatalog]. Milestone M4 replaces the backing data with a
 * bundled, attributed catalog file; the interface stays network-free.
 */
class CatalogRepository : PatternRepository {

    override fun brands(): List<Brand> = LegacyCatalog.brands

    override fun brand(id: String): Brand? =
        LegacyCatalog.brands.firstOrNull { it.id == id }

    /**
     * Universal mode interleaves the best pattern of each brand first, then
     * (with "additional patterns" enabled) deeper per-brand patterns. Patterns
     * are ordered per brand: power-off variants first, then mute.
     */
    override fun universalPatterns(): List<Brand> {
        val maxDepth = LegacyCatalog.brands.maxOf { it.patterns.size }
        val result = mutableListOf<Brand>()
        for (depth in 0 until maxDepth) {
            val patterns = LegacyCatalog.brands.mapNotNull { brand ->
                brand.patterns.getOrNull(depth)
            }
            if (patterns.isNotEmpty()) {
                result.add(
                    Brand(
                        id = "universal_$depth",
                        displayName = "Universal round ${depth + 1}",
                        region = Brand.Region.GLOBAL,
                        patterns = patterns,
                    )
                )
            }
        }
        return result
    }
}

/** One-shot parsed view over a single pattern, used by transmission code. */
fun IrPattern.isPowerOff(): Boolean =
    function == IrPattern.PatternFunction.POWER_OFF
