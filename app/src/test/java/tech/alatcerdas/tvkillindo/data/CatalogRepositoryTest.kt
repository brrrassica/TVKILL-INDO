/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction

class CatalogRepositoryTest {

    private val repository = CatalogRepository()

    @Test
    fun exposesAllSeventeenLegacyBrands() {
        assertEquals(17, repository.brands().size)
    }

    @Test
    fun brandsAreUnique() {
        val ids = repository.brands().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun legacyBrandIdsArePreserved() {
        val expected = setOf(
            "samsung", "sony", "lg", "panasonic", "philips", "nec", "sharp",
            "jvc", "toshiba", "mitsubishi", "vizio", "rca", "pioneer",
            "hisense", "akai", "aoc", "coocaa",
        )
        assertEquals(expected, repository.brands().map { it.id }.toSet())
    }

    @Test
    fun everyBrandHasAtLeastOnePowerOffPattern() {
        repository.brands().forEach { brand ->
            assertTrue(
                "Brand ${brand.id} has no power-off pattern",
                brand.patterns.any { it.function == PatternFunction.POWER_OFF },
            )
        }
    }

    @Test
    fun everyPatternHasAttribution() {
        repository.brands().flatMap { it.patterns }.forEach { pattern ->
            assertTrue(pattern.sourceUrl.isNotBlank())
            assertTrue(pattern.license.isNotBlank())
        }
    }

    @Test
    fun patternsHaveExpectedConfidence() {
        // Upstream TV-B-Gone-derived patterns are IMPORTED; the project-generated
        // Coocaa (Skyworth) patterns from the Flipper Zero list are COMMUNITY
        // until verified on real hardware.
        repository.brands().forEach { brand ->
            brand.patterns.forEach { pattern ->
                val isExpected = pattern.confidence == Confidence.IMPORTED ||
                    (pattern.confidence == Confidence.COMMUNITY && brand.id == "coocaa")
                assertTrue(
                    "Unexpected confidence ${pattern.confidence} in brand ${brand.id}",
                    isExpected,
                )
            }
        }
    }

    @Test
    fun coocaaPatternsAreCommunityConfidence() {
        val coocaa = repository.brand("coocaa")
        assertNotNull(coocaa)
        assertTrue(coocaa!!.patterns.isNotEmpty())
        coocaa.patterns.forEach { pattern ->
            assertEquals(Confidence.COMMUNITY, pattern.confidence)
        }
    }

    @Test
    fun timingsArePositiveCarrierPeriods() {
        repository.brands().flatMap { it.patterns }.forEach { pattern ->
            assertTrue("carrierHz must be positive: ${pattern.carrierHz}", pattern.carrierHz > 0)
            assertTrue("timings must be non-empty", pattern.periods.isNotEmpty())
            pattern.periods.forEach { period ->
                assertTrue("timing must be positive, got $period", period > 0)
            }
        }
    }

    @Test
    fun universalModeStartsWithOnePowerOffPerBrand() {
        val rounds = repository.universalPatterns()
        assertTrue(rounds.isNotEmpty())

        val firstRound = rounds.first()
        assertEquals(repository.brands().size, firstRound.patterns.size)
        firstRound.patterns.forEach { pattern ->
            assertEquals(PatternFunction.POWER_OFF, pattern.function)
        }
    }

    @Test
    fun brandLookupByIdWorks() {
        assertNotNull(repository.brand("samsung"))
        assertEquals("Samsung", repository.brand("samsung")?.displayName)
        assertNotNull(repository.brand("coocaa"))
        assertEquals("Coocaa (Skyworth)", repository.brand("coocaa")?.displayName)
        assertEquals(null, repository.brand("does-not-exist"))
    }
}
