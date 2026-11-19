/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.domain

sealed interface TransmissionResult {
    data class Sent(val pattern: IrPattern, val index: Int, val total: Int) : TransmissionResult
    data object Completed : TransmissionResult
    data object Cancelled : TransmissionResult
    data class Failed(val reason: String) : TransmissionResult
}
