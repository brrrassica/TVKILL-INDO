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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern

class RepetitiveStateTest {

    private fun brand(id: String) = Brand(
        id = id,
        displayName = id.replaceFirstChar { it.uppercase() },
        patterns = listOf(
            IrPattern(
                carrierHz = 38000,
                periods = intArrayOf(1, 1, 10, 10),
                sourceUrl = "https://example.com",
                license = "GPL-3.0-or-later",
            ),
        ),
    )

    private val brands = listOf(brand("samsung"), brand("sony"), brand("lg"))

    @Test
    fun defaultSelectionIsAllBrands() {
        val state = RepetitiveState(brands = brands)

        assertEquals(0, state.selectedPosition)
        assertEquals(brands, state.selectedBrands())
        assertTrue(state.hasSelection)
    }

    @Test
    fun choicesStartWithAllBrandsLabel() {
        val state = RepetitiveState(brands = brands)

        assertEquals("ALL BRANDS", state.choices.first())
        assertEquals(listOf("SAMSUNG", "SONY", "LG"), state.choices.drop(1))
    }

    @Test
    fun singleBrandSelectionSelectsExactlyThatBrand() {
        val state = RepetitiveState(brands = brands, selectedPosition = 2)

        assertEquals(listOf(brand("sony")), state.selectedBrands())
        assertTrue(state.hasSelection)
    }

    @Test
    fun outOfRangeSelectionYieldsNoBrands() {
        val state = RepetitiveState(brands = brands, selectedPosition = 99)

        assertTrue(state.selectedBrands().isEmpty())
        assertFalse(state.hasSelection)
    }

    @Test
    fun emptyCatalogYieldsNoBrands() {
        val state = RepetitiveState()

        assertTrue(state.selectedBrands().isEmpty())
        assertFalse(state.hasSelection)
    }
}
