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
package tech.alatcerdas.tvkillindo.ui.settings

/**
 * Pure state holder for the Compose settings screen, unit-testable
 * ([tech.alatcerdas.tvkillindo.ui.settings.SettingsStateTest]).
 *
 * It mirrors the legacy preference surface ([tech.alatcerdas.tvkillindo.Settings]
 * plus `preferences.xml`): the toggles the Compose shell actually consumes,
 * and the inter-pattern delay. The shell reloads these values whenever the
 * screen changes them, so every edit applies **immediately** — this is the
 * Compose-side fix for upstream issue #52 ("settings not applied instantly").
 *
 * The delay is kept as the raw string typed by the user so the text field can
 * be edited freely; it is only interpreted through [delayMillis] when read by
 * the transmission screens.
 */
data class SettingsState(
    /** Legacy `depth` preference: send additional (longer) pattern variants. */
    val additionalPatterns: Boolean = false,
    /** Legacy `show_mute` preference: whether the experimental mute control is visible. */
    val showMute: Boolean = false,
    /** Legacy `delay` preference, as typed by the user. Empty means unset. */
    val delayText: String = "",
) {

    /** The parsed delay in milliseconds, or [DEFAULT_DELAY_MILLIS] if unset/invalid. */
    val delayMillis: Long
        get() = delayText.toLongOrNull()?.coerceIn(MIN_DELAY_MILLIS, MAX_DELAY_MILLIS)
            ?: DEFAULT_DELAY_MILLIS

    /**
     * Returns a copy with the delay set from user input, normalized: only
     * digits are kept, leading zeros collapse, and the empty result is
     * preserved so the field can be cleared.
     */
    fun withDelayInput(raw: String): SettingsState {
        val digitsOnly = raw.filter { it.isDigit() }.take(MAX_DELAY_DIGITS)
        val normalized = when {
            digitsOnly.isEmpty() -> ""
            else -> digitsOnly.toLong().toString()
        }
        return copy(delayText = normalized)
    }

    companion object {
        const val DEFAULT_DELAY_MILLIS = 0L
        const val MIN_DELAY_MILLIS = 0L
        /** Upstream caps the delay at 1000 ms via `android:maxLength`-style validation. */
        const val MAX_DELAY_MILLIS = 1000L
        const val MAX_DELAY_DIGITS = 4
    }
}
