/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.domain

/**
 * An IR power-off/mute signal, stored as alternating on/off durations in
 * carrier periods, matching the upstream TV-B-Gone derived data format.
 *
 * Attribution is mandatory: every imported pattern must carry its source URL
 * and license so GPLv3 redistribution is valid.
 */
data class IrPattern(
    val carrierHz: Int,
    val periods: IntArray,
    val function: PatternFunction = PatternFunction.POWER_OFF,
    val sourceUrl: String,
    val license: String,
    val confidence: Confidence = Confidence.IMPORTED,
    val model: String? = null,
) {
    /** Converts carrier periods to the microseconds expected by ConsumerIrManager. */
    fun toMicroseconds(): IntArray {
        require(carrierHz > 0) { "carrierHz must be positive" }
        val periodMicros = 1_000_000 / carrierHz
        return IntArray(periods.size) { periods[it] * periodMicros }
    }

    enum class PatternFunction { POWER_OFF, MUTE }

    enum class Confidence { VERIFIED, COMMUNITY, IMPORTED }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrPattern) return false
        return carrierHz == other.carrierHz &&
            function == other.function &&
            periods.contentEquals(other.periods)
    }

    override fun hashCode(): Int {
        var result = carrierHz
        result = 31 * result + periods.contentHashCode()
        result = 31 * result + function.hashCode()
        return result
    }
}
