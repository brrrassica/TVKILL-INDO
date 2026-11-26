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
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.IrPattern

/**
 * Guards against transcription and encoding regressions in the catalog
 * timing arrays (issues #15, #16, #18).
 *
 * Decoding convention (validated against the legacy LG frame, which decodes
 * to the known LG address `0x20DF`): header pair, then mark 21 + space
 * 21/22 = 0 and mark 21 + space 64/65 = 1, 32 bits MSB-first as
 * addr, ~addr, cmd, ~cmd.
 *
 * Only strict-NEC frames are asserted against the inverse-byte rule: the
 * upstream TV-B-Gone derived catalog also contains Samsung/NEC-variant and
 * other protocol frames that legitimately do not use inverted bytes, so a
 * catalog-wide assertion would be wrong. A pattern counts as strict NEC
 * when it carries the "NEC addr 0xXX cmd 0xYY" label used by the
 * project-generated Coocaa entries.
 */
class NecCatalogIntegrityTest {

    companion object {
        // NEC bit shape in carrier periods, as generated into the catalogs:
        // header 342/171, data mark 21, space 21/22 (0) vs 64/65 (1),
        // final mark + trailer gap.
        private const val MARK_LIMIT = 100    // mark > 100 periods means header/trailer
        private const val SPACE_ZERO_HIGH = 40 // space >= 40 periods means bit 1
        private const val SPACE_REPEAT = 1000 // space > 1000 periods means inter-frame gap
    }

    private fun List<Int>.toBytesMsbFirst(): List<Int> {
        require(size % 8 == 0) { "bit count not byte aligned: $size" }
        return chunked(8) { chunk -> chunk.fold(0) { acc, bit -> (acc shl 1) or bit } }
    }

    /**
     * Decodes strict-NEC data bits from carrier periods: finds the header
     * (first mark above [MARK_LIMIT], tolerating the legacy leading "1, 1"
     * prefix), skips the header pair, then reads mark/space pairs until the
     * trailing mark or inter-frame gap.
     */
    private fun decodeNecBits(periods: IntArray): List<Int> {
        var header = 0
        while (header + 1 < periods.size && periods[header] <= MARK_LIMIT) header += 2
        var i = header + 2 // skip header pair
        val bits = mutableListOf<Int>()
        while (i + 1 < periods.size) {
            val mark = periods[i]
            val space = periods[i + 1]
            if (mark > MARK_LIMIT) break       // trailing mark
            if (space > SPACE_REPEAT) break    // trailing inter-frame gap
            bits.add(if (space >= SPACE_ZERO_HIGH) 1 else 0)
            i += 2
        }
        return bits
    }

    private fun checkNecFrame(where: String, pattern: IrPattern) {
        val label = pattern.model ?: return
        val match = Regex("NEC addr 0x([0-9A-Fa-f]{2}) cmd 0x([0-9A-Fa-f]{2})").find(label)
            ?: return
        val bits = decodeNecBits(pattern.periods)
        assertEquals(
            "$where (\"$label\"): strict NEC frame must carry exactly 32 data bits",
            32, bits.size,
        )
        val (addr, addrInv, cmd, cmdInv) = bits.toBytesMsbFirst()
        assertEquals(
            "$where (\"$label\"): NEC address inverse invalid " +
                "(addr=0x${addr.toString(16)}, ~addr=0x${addrInv.toString(16)})",
            addr.inv() and 0xFF, addrInv,
        )
        assertEquals(
            "$where (\"$label\"): NEC command inverse invalid " +
                "(cmd=0x${cmd.toString(16)}, ~cmd=0x${cmdInv.toString(16)})",
            cmd.inv() and 0xFF, cmdInv,
        )
        assertEquals("$where: decoded address disagrees with label \"$label\"",
            match.groupValues[1].toInt(16), addr)
        assertEquals("$where: decoded command disagrees with label \"$label\"",
            match.groupValues[2].toInt(16), cmd)
    }

    @Test
    fun labeledNecFramesInLegacyCatalogAreValidAndMatchLabels() {
        LegacyCatalog.brands.forEach { brand ->
            brand.patterns.forEachIndexed { index, pattern ->
                checkNecFrame("LegacyCatalog/${brand.id}[$index]", pattern)
            }
        }
    }

    @Test
    fun sharedBrandArraysStayByteIdenticalBetweenCatalogs() {
        // The Compose app transmits LegacyCatalog while the legacy
        // TransmitService still transmits BrandContainer; both must carry the
        // same frames (LegacyCatalog.kt "byte-identical" invariant).
        val legacy = LegacyCatalog.brands.associateBy { it.id }
        tech.alatcerdas.tvkillindo.BrandContainer.allBrands.forEach { brand ->
            val mirror = legacy[brand.designation]
                ?: return@forEach // brand only exists in one catalog yet
            assertTrue(
                "Brand ${brand.designation}: LegacyCatalog lost patterns present in BrandContainer",
                mirror.patterns.size >= brand.patterns.size,
            )
            brand.patterns.forEachIndexed { index, pattern ->
                // BrandContainer's mute arrays embed the frequency as the
                // first element; the power arrays carry it outside the array.
                val rawPeriods = pattern.rawPattern()
                val containerPeriods = if (rawPeriods.size > 3 && rawPeriods[0] > 30000) {
                    rawPeriods.copyOfRange(1, rawPeriods.size)
                } else {
                    rawPeriods
                }
                assertTrue(
                    "Brand ${brand.designation} pattern[$index] diverged between " +
                        "LegacyCatalog and BrandContainer",
                    mirror.patterns[index].periods.contentEquals(containerPeriods),
                )
            }
        }
    }

    @Test
    fun brandContainerNecFramesAreValidAndMatchComments() {
        // BrandContainer carries the Coocaa labels as // comments; the frames
        // themselves must be strict NEC with valid inverse bytes and must
        // match the six expected (addr, cmd) pairs.
        val expectedLabels = listOf(
            0x08 to 0x0F,
            0x08 to 0x12,
            0x40 to 0x12,
            0x38 to 0x12,
            0x16 to 0x0F,
            0x40 to 0x10,
        )
        val coocaa = tech.alatcerdas.tvkillindo.BrandContainer.allBrands
            .first { it.designation == "coocaa" }
        val frames = coocaa.patterns + coocaa.mute
        assertEquals(expectedLabels.size, frames.size)
        frames.forEachIndexed { index, pattern ->
            val (labeledAddr, labeledCmd) = expectedLabels[index]
            // The mute array embeds the frequency first.
            val rawPeriods = pattern.rawPattern()
            val periods = if (rawPeriods.size > 3 && rawPeriods[0] > 30000) {
                rawPeriods.copyOfRange(1, rawPeriods.size)
            } else {
                rawPeriods
            }
            val bits = decodeNecBits(periods)
            assertEquals("coocaa frame $index must carry exactly 32 data bits", 32, bits.size)
            val (addr, addrInv, cmd, cmdInv) = bits.toBytesMsbFirst()
            assertEquals("coocaa frame $index: address inverse invalid",
                addr.inv() and 0xFF, addrInv)
            assertEquals("coocaa frame $index: command inverse invalid",
                cmd.inv() and 0xFF, cmdInv)
            assertEquals("coocaa frame $index: address disagrees with comment", labeledAddr, addr)
            assertEquals("coocaa frame $index: command disagrees with comment", labeledCmd, cmd)
        }
    }

    @Test
    fun lgPowerFrameDecodesToKnownLgAddress() {
        // Decoding-convention sanity check: the LG power frame must decode to
        // the well-known LG address bytes 0x20DF.
        val lg = LegacyCatalog.brands.first { it.id == "lg" }
        val pattern = lg.patterns.first()
        val periods = pattern.periods.let { it.copyOfRange(2, it.size) } // strip legacy 1, 1 prefix
        val bytes = decodeNecBits(periods).toBytesMsbFirst()
        assertEquals(listOf(0x20, 0xDF), bytes.take(2))
    }
}
