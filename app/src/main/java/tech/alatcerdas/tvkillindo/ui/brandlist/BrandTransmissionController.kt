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

import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.domain.selectPowerOffPattern
import tech.alatcerdas.tvkillindo.transport.TvTransport

/**
 * Result of attempting a single-brand transmission from the brands screen.
 * The UI maps each outcome to a user-facing message.
 */
sealed interface TransmissionOutcome {
    /** The best pattern was sent over the transport. */
    data object Sent : TransmissionOutcome

    /** The brand exists in the catalog but carries no usable pattern. */
    data object NoPattern : TransmissionOutcome

    /** The transport cannot work on this device (no IR blaster). */
    data object TransportUnavailable : TransmissionOutcome

    /** [TvTransport.send] failed. */
    data class Error(val message: String) : TransmissionOutcome
}

/**
 * Pure, testable glue between the Compose brands screen and a [TvTransport]:
 * picks the best POWER_OFF pattern via [selectPowerOffPattern] and hands it to
 * the transport. No Android classes — the screen stays previewable and the
 * logic stays unit-testable.
 */
class BrandTransmissionController(private val transport: TvTransport) {

    fun transmit(brand: Brand): TransmissionOutcome {
        val pattern = selectPowerOffPattern(brand) ?: return TransmissionOutcome.NoPattern
        if (!transport.isAvailable()) return TransmissionOutcome.TransportUnavailable

        return transport.send(pattern).fold(
            onSuccess = { TransmissionOutcome.Sent },
            onFailure = { TransmissionOutcome.Error(it.message ?: it.javaClass.simpleName) },
        )
    }
}
