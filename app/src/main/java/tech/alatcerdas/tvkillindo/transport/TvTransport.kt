/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.transport

import tech.alatcerdas.tvkillindo.domain.IrPattern

/**
 * Hardware/network seam. IR is the only implementation in 2.0-indo; Wi-Fi
 * control for smart TVs will implement this interface in 2.1 without changing
 * the UI layer.
 */
interface TvTransport {
    val id: String
    val displayName: String
    fun isAvailable(): Boolean
    fun send(pattern: IrPattern): Result<Unit>
}
