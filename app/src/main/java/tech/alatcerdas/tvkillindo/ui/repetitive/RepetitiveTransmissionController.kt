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

import java.util.concurrent.atomic.AtomicBoolean
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.UniversalSequence
import tech.alatcerdas.tvkillindo.transport.TvTransport

/**
 * Outcome of a repetitive-mode run from the Compose screen. Repetitive mode
 * has no natural completion: it always ends stopped, failed or unavailable.
 */
sealed interface RepetitiveOutcome {
    /** The user stopped the mode after [rounds] completed rounds. */
    data class Stopped(val rounds: Int) : RepetitiveOutcome

    /** The selection produced no eligible patterns (should not happen). */
    data object NoPatterns : RepetitiveOutcome

    /** The transport cannot work on this device (no IR blaster). */
    data object TransportUnavailable : RepetitiveOutcome

    /** [TvTransport.send] failed on a pattern. */
    data class Error(val message: String, val rounds: Int) : RepetitiveOutcome
}

/**
 * Pure, testable glue between the Compose repetitive screen and a
 * [TvTransport]: builds the per-round sequence via [UniversalSequence.build]
 * and keeps sending rounds until cancelled. One "round" sends one power-off
 * pattern of every selected brand, mirroring the legacy [tech.alatcerdas.tvkillindo.TransmitService]
 * `forever` loop semantics (including the additional-patterns depth and the
 * inter-pattern delay).
 *
 * No Android classes — the logic stays unit-testable
 * ([tech.alatcerdas.tvkillindo.ui.repetitive.RepetitiveTransmissionControllerTest]).
 *
 * Note: this runs the loop synchronously; the Compose screen invokes it from a
 * background coroutine. Unlike the legacy foreground service, the loop only
 * lives as long as the caller's coroutine — hooking the loop up to
 * [tech.alatcerdas.tvkillindo.TransmitService] for background continuation is
 * a later M3 slice.
 */
class RepetitiveTransmissionController(
    private val transport: TvTransport,
    private val isCancelled: () -> Boolean = { false },
) {

    /** Thread-safe cancellation hook, driven by the screen's STOP button. */
    private val cancelFlag = AtomicBoolean(false)

    /** Requests cancellation of the currently running loop (if any). */
    fun cancel() {
        cancelFlag.set(true)
    }

    /**
     * Sends rounds until cancelled. [onRound] is invoked with the completed
     * round count after every round, so the UI can show progress.
     */
    fun transmit(
        brands: List<Brand>,
        additionalPatterns: Boolean,
        delayMillis: Long,
        onRound: (rounds: Int) -> Unit = {},
    ): RepetitiveOutcome {
        val steps = UniversalSequence.build(brands, additionalPatterns)
        if (steps.isEmpty()) return RepetitiveOutcome.NoPatterns
        if (!transport.isAvailable()) return RepetitiveOutcome.TransportUnavailable

        var rounds = 0
        cancelFlag.set(false)
        while (true) {
            for (step in steps) {
                if (cancelFlag.get() || isCancelled()) return RepetitiveOutcome.Stopped(rounds)
                transport.send(step.pattern).fold(
                    onSuccess = {},
                    onFailure = {
                        return RepetitiveOutcome.Error(
                            it.message ?: it.javaClass.simpleName,
                            rounds,
                        )
                    },
                )
                if (delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return RepetitiveOutcome.Stopped(rounds)
                    }
                }
            }
            rounds++
            onRound(rounds)
        }
    }
}
