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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.alatcerdas.tvkillindo.R
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/**
 * Settings Compose screen (M3 slice): mirrors the legacy `preferences.xml`
 * surface with the toggles and delay the Compose shell actually consumes.
 * Edits go through [SettingsController], which persists immediately — the
 * Compose-side fix for upstream issue #52.
 *
 * The screen is stateless apart from the delay text field's local editing
 * buffer; everything else is driven by [SettingsController.state].
 */
@Composable
fun SettingsScreen(
    controller: SettingsController,
    modifier: Modifier = Modifier,
    onChanged: () -> Unit = {},
) {
    var state by remember { mutableStateOf(controller.state) }
    // Local buffer so the user can clear/edit the field without persisting
    // intermediate empty values on every keystroke.
    var delayEditing by remember(state.delayText) { mutableStateOf(state.delayText) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsHeader(stringResource(R.string.category_transmission))

        SettingsSwitchRow(
            title = stringResource(R.string.depth),
            summary = stringResource(R.string.depth_summary),
            checked = state.additionalPatterns,
            onCheckedChange = {
                controller.applyAdditionalPatterns(it)
                state = controller.state
                onChanged()
            },
        )

        DelayRow(
            title = stringResource(R.string.delay),
            summary = stringResource(R.string.delay_summary),
            value = delayEditing,
            onValueChange = { raw ->
                delayEditing = raw
                controller.applyDelayInput(raw)
                state = controller.state
                delayEditing = state.delayText
                onChanged()
            },
        )

        HorizontalDivider()

        SettingsHeader(stringResource(R.string.category_appearance))

        SettingsSwitchRow(
            title = stringResource(R.string.show_mute),
            summary = null,
            checked = state.showMute,
            onCheckedChange = {
                controller.applyShowMute(it)
                state = controller.state
                onChanged()
            },
        )
    }
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = title },
        )
    }
}

@Composable
private fun DelayRow(
    title: String,
    summary: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .width(96.dp)
                .semantics { contentDescription = title },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TvkillIndoTheme {
        SettingsScreen(controller = SettingsController(InMemorySettingsStore(SettingsState())))
    }
}

/** Simple in-memory store for previews. */
class InMemorySettingsStore(initial: SettingsState) : SettingsStore {
    private var saved: SettingsState = initial
    override fun load(): SettingsState = saved
    override fun save(state: SettingsState) {
        saved = state
    }
}
