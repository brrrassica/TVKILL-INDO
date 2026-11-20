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

import java.util.concurrent.atomic.AtomicBoolean
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.UniversalSequence
import tech.alatcerdas.tvkillindo.transport.TvTransport

/**
 * Outcome of one universal-mode transmission run from the Compose screen.
 */
sealed interface UniversalOutcome {
    /** The whole sequence was transmitted. */
    data class Completed(val steps: Int) : UniversalOutcome

    /** The catalog produced no eligible patterns (should not happen). */
    data object NoPatterns : UniversalOutcome

    /** The transport cannot work on this device (no IR blaster). */
    data object TransportUnavailable : UniversalOutcome

    /** The user cancelled mid-sequence; [steps] patterns were already sent. */
    data class Cancelled(val steps: Int) : UniversalOutcome

    /** [TvTransport.send] failed on a pattern. */
    data class Error(val message: String, val steps: Int) : UniversalOutcome
}

/**
 * Pure, testable glue between the Compose universal screen and a
 * [TvTransport]: builds the sequence via [UniversalSequence.build], sends each
 * pattern through the transport honoring the inter-pattern delay, and reports
 * cancellation via [isCancelled]. No Android classes — unit-testable.
 *
 * Note: this runs the sequence synchronously; the Compose screen invokes it
 * from a background coroutine and shows progress. Replacing the legacy
 * foreground-service behavior (repetitive mode, widget) is a later M3 slice.
 */
class UniversalTransmissionController(
    private val transport: TvTransport,
    private val isCancelled: () -> Boolean = { false },
) {

    /** Thread-safe cancellation hook, driven by the screen's CANCEL button. */
    private val cancelFlag = AtomicBoolean(false)

    /** Requests cancellation of the currently running sequence (if any). */
    fun cancel() {
        cancelFlag.set(true)
    }

    /**
     * @param delayMillis pause between patterns, mirroring the legacy
     *   "delay between patterns" setting.
     */
    fun transmit(
        brands: List<Brand>,
        additionalPatterns: Boolean,
        delayMillis: Long,
        onProgress: (sent: Int, total: Int) -> Unit = { _, _ -> },
    ): UniversalOutcome {
        val steps = UniversalSequence.build(brands, additionalPatterns)
        if (steps.isEmpty()) return UniversalOutcome.NoPatterns
        if (!transport.isAvailable()) return UniversalOutcome.TransportUnavailable

        var sent = 0
        cancelFlag.set(false)
        onProgress(0, steps.size)
        for (step in steps) {
            if (cancelFlag.get() || isCancelled()) return UniversalOutcome.Cancelled(sent)
            transport.send(step.pattern).fold(
                onSuccess = {
                    sent++
                    onProgress(sent, steps.size)
                },
                onFailure = { return UniversalOutcome.Error(it.message ?: it.javaClass.simpleName, sent) },
            )
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return UniversalOutcome.Cancelled(sent)
                }
            }
        }
        return UniversalOutcome.Completed(sent)
    }

}
