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

class SettingsControllerTest {

    /** Test double recording every save, mirroring the SharedPreferences store. */
    private class RecordingStore(initial: SettingsState) : SettingsStore {
        var persisted: SettingsState = initial
        var saveCount = 0
        override fun load(): SettingsState = persisted
        override fun save(state: SettingsState) {
            persisted = state
            saveCount++
        }
    }

    @Test
    fun stateStartsFromStore() {
        val initial = SettingsState(additionalPatterns = true, delayText = "120")
        val controller = SettingsController(RecordingStore(initial))

        assertEquals(initial, controller.state)
    }

    @Test
    fun applyingAdditionalPatternsPersistsImmediately() {
        val store = RecordingStore(SettingsState())
        val controller = SettingsController(store)

        controller.applyAdditionalPatterns(true)

        assertTrue(controller.state.additionalPatterns)
        assertTrue(store.persisted.additionalPatterns)
        assertEquals(1, store.saveCount)
    }

    @Test
    fun applyingShowMutePersistsImmediately() {
        val store = RecordingStore(SettingsState())
        val controller = SettingsController(store)

        controller.applyShowMute(true)

        assertTrue(controller.state.showMute)
        assertTrue(store.persisted.showMute)
        assertEquals(1, store.saveCount)
    }

    @Test
    fun applyingDelayNormalizesThenPersists() {
        val store = RecordingStore(SettingsState())
        val controller = SettingsController(store)

        controller.applyDelayInput("0400")

        assertEquals("400", controller.state.delayText)
        assertEquals(400L, controller.state.delayMillis)
        assertEquals("400", store.persisted.delayText)
    }

    @Test
    fun editsPreserveOtherSettings() {
        val controller = SettingsController(
            RecordingStore(SettingsState(additionalPatterns = true, delayText = "50")),
        )

        controller.applyShowMute(true)
        controller.applyDelayInput("80")

        assertTrue(controller.state.additionalPatterns)
        assertTrue(controller.state.showMute)
        assertEquals("80", controller.state.delayText)
    }

    @Test
    fun reloadingUsesStoreSnapshot() {
        val store = RecordingStore(SettingsState())
        val controller = SettingsController(store)

        controller.applyDelayInput("100")

        // A second controller over the same store reads the persisted value.
        assertEquals("100", SettingsController(store).state.delayText)
    }
}
