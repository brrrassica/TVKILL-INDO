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

import android.content.Context

/**
 * [SettingsStore] backed by the app's default [android.content.SharedPreferences],
 * using the **legacy preference keys** so the old `Preferences` activity,
 * [tech.alatcerdas.tvkillindo.Settings] listeners and any future consumer keep
 * seeing the same values. Edits are `apply()`ed, so they are visible to readers
 * in the same process immediately (upstream issue #52 semantics).
 */
class SharedPreferencesSettingsStore(context: Context) : SettingsStore {

    private val preferences =
        tech.alatcerdas.tvkillindo.Settings.with(context).preferences

    override fun load(): SettingsState = SettingsState(
        additionalPatterns = preferences.getBoolean(KEY_ADDITIONAL_PATTERNS, false),
        showMute = preferences.getBoolean(KEY_SHOW_MUTE, false),
        delayText = preferences.getString(KEY_DELAY, null) ?: "",
    )

    override fun save(state: SettingsState) {
        preferences.edit()
            .putBoolean(KEY_ADDITIONAL_PATTERNS, state.additionalPatterns)
            .putBoolean(KEY_SHOW_MUTE, state.showMute)
            .putString(KEY_DELAY, state.delayText)
            .apply()
    }

    companion object {
        /** Legacy keys, shared with [tech.alatcerdas.tvkillindo.Settings] and preferences.xml. */
        const val KEY_ADDITIONAL_PATTERNS = "depth"
        const val KEY_SHOW_MUTE = "show_mute"
        const val KEY_DELAY = "delay"
    }
}
