/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tech.alatcerdas.tvkillindo.ui.repetitive

import tech.alatcerdas.tvkillindo.domain.Brand

/**
 * Pure state holder for the Compose repetitive screen, unit-testable
 * ([tech.alatcerdas.tvkillindo.ui.repetitive.RepetitiveStateTest]).
 *
 * Selection mirrors the legacy spinner semantics: index 0 means "all brands",
 * any other index selects exactly one brand.
 */
data class RepetitiveState(
    val brands: List<Brand> = emptyList(),
    /** Index into `[allBrands] + brands`; 0 = all brands, else brands[index-1]. */
    val selectedPosition: Int = 0,
) {

    /** The brand choices offered by the screen, "All brands" first. */
    val choices: List<String>
        get() = listOf(ALL_BRANDS_LABEL) + brands.map { it.displayName.uppercase() }

    /** Whether the current selection covers at least one brand. */
    val hasSelection: Boolean
        get() = selectedBrands().isNotEmpty()

    /**
     * The brands covered by the current selection: all catalog brands for
     * position 0, otherwise the single selected brand.
     */
    fun selectedBrands(): List<Brand> = when {
        brands.isEmpty() -> emptyList()
        selectedPosition <= 0 -> brands
        selectedPosition - 1 < brands.size -> listOf(brands[selectedPosition - 1])
        else -> emptyList()
    }

    companion object {
        const val ALL_BRANDS_LABEL = "ALL BRANDS"
    }
}
