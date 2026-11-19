/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.transport

import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.core.content.ContextCompat
import tech.alatcerdas.tvkillindo.domain.IrPattern

/** IR transport backed by the platform ConsumerIrManager (API 19+). */
class ConsumerIrTvTransport(private val context: Context) : TvTransport {
    override val id: String = "ir"
    override val displayName: String = "Infrared (IR blaster)"

    override fun isAvailable(): Boolean =
        ContextCompat.getSystemService(context, ConsumerIrManager::class.java)?.hasIrEmitter() == true

    override fun send(pattern: IrPattern): Result<Unit> = runCatching {
        val manager = ContextCompat.getSystemService(context, ConsumerIrManager::class.java)
            ?: error("ConsumerIrManager is not available on this device")
        check(manager.hasIrEmitter()) { "This device has no IR blaster" }
        manager.transmit(pattern.carrierHz, pattern.toMicroseconds())
    }
}
