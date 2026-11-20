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
package tech.alatcerdas.tvkillindo.ui.brandlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.transport.TvTransport

class BrandTransmissionControllerTest {

    private fun powerOffPattern(confidence: Confidence) = IrPattern(
        carrierHz = 38000,
        periods = intArrayOf(1, 1, 10, 10),
        function = IrPattern.PatternFunction.POWER_OFF,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
        confidence = confidence,
    )

    private fun brand(vararg patterns: IrPattern) = Brand(
        id = "samsung",
        displayName = "Samsung",
        region = Brand.Region.PREMIUM,
        patterns = patterns.toList(),
    )

    private class FakeTransport(
        private val available: Boolean = true,
        private val result: Result<Unit> = Result.success(Unit),
    ) : TvTransport {
        var sent: IrPattern? = null

        override val id: String = "fake"
        override val displayName: String = "Fake"

        override fun isAvailable(): Boolean = available

        override fun send(pattern: IrPattern): Result<Unit> {
            sent = pattern
            return result
        }
    }

    @Test
    fun sendsHighestConfidencePatternThroughTransport() {
        val verified = powerOffPattern(Confidence.VERIFIED)
        val imported = powerOffPattern(Confidence.IMPORTED)
        val transport = FakeTransport()
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmit(brand(imported, verified))

        assertEquals(TransmissionOutcome.Sent, outcome)
        assertEquals(verified, transport.sent)
    }

    @Test
    fun brandWithoutPatternsYieldsNoPatternWithoutTouchingTransport() {
        val transport = FakeTransport()
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmit(brand())

        assertEquals(TransmissionOutcome.NoPattern, outcome)
        assertEquals(null, transport.sent)
    }

    @Test
    fun unavailableTransportYieldsTransportUnavailableWithoutSending() {
        val transport = FakeTransport(available = false)
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmit(brand(powerOffPattern(Confidence.VERIFIED)))

        assertEquals(TransmissionOutcome.TransportUnavailable, outcome)
        assertEquals(null, transport.sent)
    }

    @Test
    fun transportFailureIsWrappedAsError() {
        val transport = FakeTransport(result = Result.failure(IllegalStateException("boom")))
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmit(brand(powerOffPattern(Confidence.VERIFIED)))

        assertTrue(outcome is TransmissionOutcome.Error)
        assertEquals("boom", (outcome as TransmissionOutcome.Error).message)
        assertEquals(powerOffPattern(Confidence.VERIFIED), transport.sent)
    }

    private fun mutePattern(confidence: Confidence) = IrPattern(
        carrierHz = 38000,
        periods = intArrayOf(1, 1, 10, 10),
        function = IrPattern.PatternFunction.MUTE,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
        confidence = confidence,
    )

    @Test
    fun muteTapSendsBestMutePattern() {
        val communityMute = mutePattern(Confidence.COMMUNITY)
        val importedMute = mutePattern(Confidence.IMPORTED)
        val transport = FakeTransport()
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmitMute(brand(importedMute, communityMute))

        assertEquals(TransmissionOutcome.Sent, outcome)
        assertEquals(communityMute, transport.sent)
    }

    @Test
    fun muteTapOnBrandWithoutMuteYieldsNoPattern() {
        val transport = FakeTransport()
        val controller = BrandTransmissionController(transport)

        val outcome = controller.transmitMute(brand(powerOffPattern(Confidence.VERIFIED)))

        assertEquals(TransmissionOutcome.NoPattern, outcome)
        assertEquals(null, transport.sent)
    }
}
