/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.domain

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class IrPatternTest {
    @Test
    fun convertsCarrierPeriodsToMicroseconds() {
        val pattern = IrPattern(
            carrierHz = 38_000,
            periods = intArrayOf(1, 2, 3),
            sourceUrl = "test://vector",
            license = "test",
        )

        assertArrayEquals(intArrayOf(26, 52, 78), pattern.toMicroseconds())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidCarrierFrequency() {
        IrPattern(
            carrierHz = 0,
            periods = intArrayOf(1),
            sourceUrl = "test://vector",
            license = "test",
        ).toMicroseconds()
    }
}
