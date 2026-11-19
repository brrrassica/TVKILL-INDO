/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.domain

sealed interface TransmissionMode {
    data object Universal : TransmissionMode
    data class SingleBrand(val brandId: String) : TransmissionMode
    data class Repetitive(val brandId: String?) : TransmissionMode
    data class Mute(val brandId: String?) : TransmissionMode
}
