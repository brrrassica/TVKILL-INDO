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
package tech.alatcerdas.tvkillindo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction

class UniversalSequenceTest {

    private fun powerOff(periods: IntArray = intArrayOf(1, 1, 10, 10)) = IrPattern(
        carrierHz = 38000,
        periods = periods,
        function = PatternFunction.POWER_OFF,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
    )

    private fun mute() = IrPattern(
        carrierHz = 38000,
        periods = intArrayOf(1, 1, 10, 10),
        function = PatternFunction.MUTE,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
    )

    private fun brand(
        id: String,
        vararg patterns: IrPattern,
    ) = Brand(
        id = id,
        displayName = id.replaceFirstChar { it.uppercase() },
        patterns = patterns.toList(),
    )

    @Test
    fun singleRoundSendsOnePowerOffPerBrandInCatalogOrder() {
        val brands = listOf(
            brand("samsung", powerOff(), mute()),
            brand("sony", powerOff()),
            brand("lg", powerOff(), powerOff(periods = intArrayOf(2, 2, 20, 20))),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = false)

        assertEquals(listOf("samsung", "sony", "lg"), steps.map { it.brandId })
        steps.forEach { assertEquals(PatternFunction.POWER_OFF, it.pattern.function) }
    }

    @Test
    fun additionalPatternsAppendDeeperRounds() {
        val deep = powerOff(periods = intArrayOf(2, 2, 20, 20))
        val brands = listOf(
            brand("samsung", powerOff(), deep),
            brand("sony", powerOff()),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = true)

        assertEquals(listOf("samsung", "sony", "samsung"), steps.map { it.brandId })
        assertEquals(deep, steps.last().pattern)
    }

    @Test
    fun additionalPatternsRespectMaxDepth() {
        val brands = listOf(
            brand("samsung", powerOff(), powerOff(), powerOff()),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = true, maxDepth = 2)

        assertEquals(2, steps.size)
    }

    @Test
    fun mutePatternsAreNeverIncluded() {
        val brands = listOf(
            brand("samsung", powerOff()),
            brand("akai", mute()),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = true)

        assertTrue(steps.none { it.pattern.function == PatternFunction.MUTE })
        assertEquals(listOf("samsung"), steps.map { it.brandId })
    }

    @Test
    fun stopsWhenNoBrandHasPatternsLeftAtDepth() {
        val brands = listOf(
            brand("samsung", powerOff(), powerOff()),
            brand("sony", powerOff()),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = true)

        // Round 2 (index) has no patterns for any brand → stop after round 1.
        assertEquals(3, steps.size)
        assertEquals(listOf("samsung", "sony", "samsung"), steps.map { it.brandId })
    }

    @Test
    fun emptyCatalogYieldsEmptySequence() {
        assertTrue(UniversalSequence.build(emptyList(), additionalPatterns = true).isEmpty())
    }

    @Test
    fun brandsWithoutAnyPowerOffPatternAreSkipped() {
        val brands = listOf(
            brand("polytron", mute()),
            brand("lg", powerOff()),
        )

        val steps = UniversalSequence.build(brands, additionalPatterns = false)

        assertEquals(listOf("lg"), steps.map { it.brandId })
    }
}
