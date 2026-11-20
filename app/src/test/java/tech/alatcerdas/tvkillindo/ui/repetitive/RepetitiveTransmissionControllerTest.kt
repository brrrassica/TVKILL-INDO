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
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction
import tech.alatcerdas.tvkillindo.transport.TvTransport

class RepetitiveTransmissionControllerTest {

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
    fun cancelStopsAfterTheRoundInProgress() {
        val transport = FakeTransport()
        val controller = RepetitiveTransmissionController(transport) {
            // cancel after the first full round has completed
            transport.sent.size >= 2
        }

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff()), brand("sony", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(RepetitiveOutcome.Stopped(1), outcome)
        assertEquals(2, transport.sent.size)
    }

    @Test
    fun explicitCancelHookStopsTheLoop() {
        val transport = FakeTransport()
        val controller = RepetitiveTransmissionController(transport)
        // Stop right after the first pattern of the first round
        val rounds = mutableListOf<Int>()

        Thread {
            // let the loop start, then cancel mid-round
            while (transport.sent.isEmpty()) Thread.yield()
            controller.cancel()
        }.start()

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff()), brand("sony", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
            onRound = { rounds += it },
        )

        assertTrue(outcome is RepetitiveOutcome.Stopped)
        assertEquals(1, transport.sent.size)
        assertTrue(rounds.isEmpty())
    }

    @Test
    fun unavailableTransportShortCircuits() {
        val transport = FakeTransport(available = false)
        val controller = RepetitiveTransmissionController(transport)

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(RepetitiveOutcome.TransportUnavailable, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun transportFailureMidRoundReportsErrorWithRoundCount() {
        val failurePattern = powerOff(periods = intArrayOf(9, 9, 9, 9))
        val transport = FakeTransport(failureOn = failurePattern)
        val controller = RepetitiveTransmissionController(transport)

        val outcome = controller.transmit(
            listOf(brand("samsung", powerOff()), brand("sony", failurePattern)),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertTrue(outcome is RepetitiveOutcome.Error)
        assertEquals("boom", (outcome as RepetitiveOutcome.Error).message)
        assertEquals(0, outcome.rounds)
    }

    @Test
    fun emptySelectionYieldsNoPatterns() {
        val transport = FakeTransport()
        val controller = RepetitiveTransmissionController(transport)

        val outcome = controller.transmit(
            emptyList(),
            additionalPatterns = true,
            delayMillis = 0,
        )

        assertEquals(RepetitiveOutcome.NoPatterns, outcome)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun mutePatternsAreNeverSent() {
        val transport = FakeTransport()
        val controller = RepetitiveTransmissionController(transport)

        // single-brand selection with only a mute pattern
        val outcome = controller.transmit(
            listOf(brand("samsung", mute())),
            additionalPatterns = false,
            delayMillis = 0,
        )

        assertEquals(RepetitiveOutcome.NoPatterns, outcome)
        assertTrue(transport.sent.isEmpty())
    }
}
