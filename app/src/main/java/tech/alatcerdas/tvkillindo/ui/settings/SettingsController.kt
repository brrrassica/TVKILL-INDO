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
 * Persistence seam for the Compose settings screen, kept Android-free so the
 * state logic stays unit-testable. The production implementation writes
 * through [android.content.SharedPreferences] using the **legacy preference
 * keys** (`depth`, `show_mute`, `delay`) so the old UI, the widget path and
 * [tech.alatcerdas.tvkillindo.Settings] keep reading the same values.
 */
interface SettingsStore {
    fun load(): SettingsState
    fun save(state: SettingsState)
}

/**
 * Applies user edits to a [SettingsState] and persists them immediately.
 * Each `apply*` call both updates the state and writes it to the
 * [SettingsStore], so transmission screens observe fresh values the moment
 * the user leaves (or while still on) the settings tab — the immediate-apply
 * behavior required by upstream issue #52.
 */
class SettingsController(
    private val store: SettingsStore,
) {

    /** The current settings; starts from what the store has persisted. */
    var state: SettingsState = store.load()
        private set

    /** Sets the additional-patterns (legacy `depth`) preference. */
    fun applyAdditionalPatterns(enabled: Boolean) {
        state = state.copy(additionalPatterns = enabled)
        store.save(state)
    }

    /** Sets the experimental mute visibility (legacy `show_mute`) preference. */
    fun applyShowMute(enabled: Boolean) {
        state = state.copy(showMute = enabled)
        store.save(state)
    }

    /**
     * Sets the inter-pattern delay (legacy `delay` preference) from raw user
     * input. The input is normalized by [SettingsState.withDelayInput] before
     * persisting, and only normalized values reach the store.
     */
    fun applyDelayInput(raw: String) {
        state = state.withDelayInput(raw)
        store.save(state)
    }
}
