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
package tech.alatcerdas.tvkillindo.ui.universal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction
import tech.alatcerdas.tvkillindo.transport.TvTransport

class UniversalTransmissionControllerTest {

    private fun powerOff(periods: IntArray = intArrayOf(1, 1, 10, 10)) = IrPattern(
        carrierHz = 38000,
        periods = periods,
        function = PatternFunction.POWER_OFF,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
        confidence = Confidence.IMPORTED,
    )

    private fun mute() = IrPattern(
        carrierHz = 38000,
        periods = intArrayOf(1, 1, 10, 10),
        function = PatternFunction.MUTE,
        sourceUrl = "https://example.com",
        license = "GPL-3.0-or-later",
    )

    private fun brand(id: String, vararg patterns: IrPattern) = Brand(
        id = id,
        displayName = id.replaceFirstChar { it.uppercase() },
        patterns = patterns.toList(),
    )

    private class FakeTransport(
        private val available: Boolean = true,
        private val failureOn: IrPattern? = null,
    ) : TvTransport {
        val sent = mutableListOf<IrPattern>()

        override val id: String = "fake"
        override val displayName: String = "Fake"

        override fun isAvailable(): Boolean = available

        override fun send(pattern: IrPattern): Result<Unit> =
            if (pattern == failureOn) {
                Result.failure(IllegalStateException("boom"))
            } else {
                sent += pattern
                Result.success(Unit)
            }
    }

    @Test
    fun completedRunSendsEveryPatternAndReportsCount() {
        val transport = FakeTransport()
        val brands = listOf(
            brand("samsung", powerOff(), mute()),
            brand("sony", powerOff()),
        )
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmit(brands, additionalPatterns = false, delayMillis = 0)

        assertEquals(UniversalOutcome.Completed(2), outcome)
        assertEquals(2, transport.sent.size)
        assertTrue(transport.sent.none { it.function == PatternFunction.MUTE })
    }

    @Test
    fun unavailableTransportShortCircuits() {
        val transport = FakeTransport(available = false)
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.TransportUnavailable, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun cancellationAfterFirstPatternReportsCancelled() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport) {
            transport.sent.isNotEmpty() // cancel right after the first send
        }

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff()), brand("sony", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Cancelled(1), outcome)
        assertEquals(1, transport.sent.size)
    }

    @Test
    fun transportFailureMidSequenceReportsErrorWithProgress() {
        val failurePattern = powerOff(periods = intArrayOf(9, 9, 9, 9))
        val transport = FakeTransport(failureOn = failurePattern)
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff()), brand("sony", failurePattern)),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertTrue(outcome is UniversalOutcome.Error)
        assertEquals("boom", (outcome as UniversalOutcome.Error).message)
        assertEquals(1, outcome.steps)
    }

    @Test
    fun emptySequenceYieldsNoPatterns() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmit(emptyList(), additionalPatterns = true, delayMillis = 0)

        assertEquals(UniversalOutcome.NoPatterns, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun muteRunSendsOnlyMutePatterns() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmitMute(
            listOf(brand("samsung", powerOff(), mute()), brand("sony", powerOff())),
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Completed(1), outcome)
        assertEquals(1, transport.sent.size)
        assertTrue(transport.sent.all { it.function == PatternFunction.MUTE })
    }

    @Test
    fun muteRunWithoutMutePatternsYieldsNoPatterns() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmitMute(
            listOf(brand("samsung", powerOff())),
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.NoPatterns, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun muteRunUnavailableTransportShortCircuits() {
        val transport = FakeTransport(available = false)
        val controller = UniversalTransmissionController(transport)

        val outcome = controller.transmitMute(
            listOf(brand("samsung", mute())),
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.TransportUnavailable, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun muteRunCancellationReportsCancelled() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport) {
            transport.sent.isNotEmpty() // cancel right after the first send
        }

        val outcome = controller.transmitMute(
            listOf(brand("samsung", mute()), brand("sony", mute())),
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Cancelled(1), outcome)
        assertEquals(1, transport.sent.size)
    }

    @Test
    fun cancelBeforeTransmitIsHonored() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)
        controller.cancel()

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Cancelled(0), outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun cancelBeforeMuteTransmitIsHonored() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)
        controller.cancel()

        val outcome = controller.transmitMute(
            listOf(brand("samsung", mute())),
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Cancelled(0), outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun resetClearsPreviousCancel() {
        val transport = FakeTransport()
        val controller = UniversalTransmissionController(transport)
        controller.cancel()
        controller.reset()

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(UniversalOutcome.Completed(1), outcome)
        assertEquals(1, transport.sent.size)
    }
}
