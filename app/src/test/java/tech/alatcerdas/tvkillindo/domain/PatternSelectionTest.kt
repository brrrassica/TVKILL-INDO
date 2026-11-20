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
import org.junit.Assert.assertNull
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction

class PatternSelectionTest {

    private fun pattern(
        function: PatternFunction = PatternFunction.POWER_OFF,
        confidence: Confidence = Confidence.IMPORTED,
        periods: IntArray = intArrayOf(1, 1, 10, 10),
    ) = IrPattern(
        carrierHz = 38000,
        periods = periods,
        function = function,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
        confidence = confidence,
    )

    private fun brand(vararg patterns: IrPattern) = Brand(
        id = "test",
        displayName = "Test",
        region = Brand.Region.PREMIUM,
        patterns = patterns.toList(),
    )

    @Test
    fun emptyBrandYieldsNull() {
        assertNull(selectPowerOffPattern(brand()))
    }

    @Test
    fun singlePowerOffPatternIsSelected() {
        val p = pattern()
        assertEquals(p, selectPowerOffPattern(brand(p)))
    }

    @Test
    fun muteOnlyBrandYieldsNull() {
        assertNull(selectPowerOffPattern(brand(pattern(function = PatternFunction.MUTE))))
    }

    @Test
    fun mutePatternsAreNeverSelected() {
        val mute = pattern(function = PatternFunction.MUTE)
        val off = pattern()
        assertEquals(off, selectPowerOffPattern(brand(mute, off)))
    }

    @Test
    fun verifiedBeatsImported() {
        val imported = pattern(confidence = Confidence.IMPORTED)
        val verified = pattern(confidence = Confidence.VERIFIED)
        assertEquals(verified, selectPowerOffPattern(brand(imported, verified)))
    }

    @Test
    fun communitySitsBetweenVerifiedAndImported() {
        val imported = pattern(confidence = Confidence.IMPORTED)
        val community = pattern(confidence = Confidence.COMMUNITY)
        val verified = pattern(confidence = Confidence.VERIFIED)
        assertEquals(verified, selectPowerOffPattern(brand(imported, community, verified)))
    }

    @Test
    fun equalConfidenceFallsBackToCatalogOrder() {
        val first = pattern()
        val second = pattern()
        assertEquals(first, selectPowerOffPattern(brand(first, second)))
        assertEquals(first, selectPowerOffPattern(brand(second, first)))
    }

    @Test
    fun verifiedBeatsEarlierImportedRegardlessOfOrder() {
        val imported = pattern(confidence = Confidence.IMPORTED)
        val verified = pattern(confidence = Confidence.VERIFIED)
        assertEquals(verified, selectPowerOffPattern(brand(verified, imported)))
    }

    @Test
    fun selectMutePicksOnlyMutePatterns() {
        val mute = pattern(function = PatternFunction.MUTE)
        val off = pattern()
        assertEquals(mute, selectMutePattern(brand(off, mute)))
    }

    @Test
    fun selectMuteYieldsNullWithoutMutePatterns() {
        assertNull(selectMutePattern(brand(pattern())))
    }

    @Test
    fun selectMutePrefersHigherConfidence() {
        val imported = pattern(function = PatternFunction.MUTE, confidence = Confidence.IMPORTED)
        val verified = pattern(function = PatternFunction.MUTE, confidence = Confidence.VERIFIED)
        assertEquals(verified, selectMutePattern(brand(imported, verified)))
    }
}
