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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsStateTest {

    @Test
    fun defaultsMatchLegacyPreferenceDefaults() {
        val state = SettingsState()

        assertFalse(state.additionalPatterns)
        assertFalse(state.showMute)
        assertEquals("", state.delayText)
    }

    @Test
    fun emptyDelayTextYieldsDefaultDelay() {
        assertEquals(SettingsState.DEFAULT_DELAY_MILLIS, SettingsState(delayText = "").delayMillis)
    }

    @Test
    fun validDelayTextIsParsed() {
        assertEquals(150L, SettingsState(delayText = "150").delayMillis)
    }

    @Test
    fun invalidDelayTextYieldsDefaultDelay() {
        assertEquals(
            SettingsState.DEFAULT_DELAY_MILLIS,
            SettingsState(delayText = "abc").delayMillis,
        )
    }

    @Test
    fun negativeDelayTextYieldsDefaultDelay() {
        // "-" never survives input normalization, but the parse must stay safe.
        assertEquals(
            SettingsState.DEFAULT_DELAY_MILLIS,
            SettingsState(delayText = "-5").delayMillis,
        )
    }

    @Test
    fun delayAboveMaximumIsClamped() {
        assertEquals(
            SettingsState.MAX_DELAY_MILLIS,
            SettingsState(delayText = "5000").delayMillis,
        )
    }

    @Test
    fun withDelayInputKeepsDigitsOnly() {
        assertEquals("250", SettingsState().withDelayInput("2a5b0").delayText)
    }

    @Test
    fun withDelayInputCollapsesLeadingZeros() {
        assertEquals("42", SettingsState().withDelayInput("0042").delayText)
    }

    @Test
    fun withDelayInputAllowsClearingTheField() {
        val cleared = SettingsState(delayText = "300").withDelayInput("")

        assertEquals("", cleared.delayText)
        assertEquals(SettingsState.DEFAULT_DELAY_MILLIS, cleared.delayMillis)
    }

    @Test
    fun withDelayInputCapsLength() {
        val state = SettingsState().withDelayInput("12345678")

        assertEquals(SettingsState.MAX_DELAY_DIGITS, state.delayText.length)
        assertTrue(state.delayMillis <= SettingsState.MAX_DELAY_MILLIS)
    }
}
