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
package tech.alatcerdas.tvkillindo.ui.repetitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.alatcerdas.tvkillindo.R
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.transport.TvTransport
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/** Progress sample pushed from the transmitting thread to the UI. */
private data class RoundProgress(val rounds: Int)

/**
 * Repetitive-mode Compose screen (M3 slice): pick a brand (or all brands),
 * then start/stop a loop that keeps sending power-off patterns. Mirrors the
 * legacy repetitive tab, which drove the foreground-service `forever` loop;
 * this slice runs the loop only while the screen is alive, and reports each
 * completed round.
 *
 * The loop runs on [Dispatchers.IO]; round progress reaches the UI through a
 * conflated channel so no Compose state is mutated off the main thread.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepetitiveScreen(
    brands: List<Brand>,
    additionalPatternsDefault: Boolean,
    delayMillis: Long,
    modifier: Modifier = Modifier,
    hasIrEmitter: Boolean = true,
    controller: RepetitiveTransmissionController? = null,
) {
    // RepetitiveState holds List<Brand>, which is not Bundle-storable; the
    // whole state is derivable from `brands`, so plain remember is correct
    // (issue #2).
    var state by remember(brands) { mutableStateOf(RepetitiveState(brands = brands)) }
    var running by remember { mutableStateOf(false) }
    var stopping by remember { mutableStateOf(false) }
    var additionalPatterns by rememberSaveable { mutableStateOf(additionalPatternsDefault) }
    LaunchedEffect(additionalPatternsDefault) { additionalPatterns = additionalPatternsDefault }
    var rounds by remember { mutableStateOf(0) }
    var pendingOutcome by remember { mutableStateOf<RepetitiveOutcome?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val progressChannel = remember { Channel<RoundProgress>(Channel.CONFLATED) }

    DisposableEffect(controller) {
        onDispose { controller?.cancel() }
    }

    LaunchedEffect(progressChannel) {
        for (progress in progressChannel) {
            rounds = progress.rounds
        }
    }

    pendingOutcome?.let { outcome ->
        val message = stringResource(outcome.messageRes(), outcome.rounds())
        LaunchedEffect(outcome) {
            running = false
            stopping = false
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
            pendingOutcome = null
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!hasIrEmitter) {
                NoIrEmitterNotice()
                return@Column
            }

            if (running) {
                Text(
                    text = stringResource(R.string.mode_running),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.repetitive_rounds, rounds),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        stopping = true
                        controller?.cancel()
                    },
                    enabled = !stopping,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(stringResource(R.string.stop))
                }
            } else {
                BrandDropdown(
                    choices = state.choices,
                    selectedPosition = state.selectedPosition,
                    enabled = true,
                    onSelected = { state = state.copy(selectedPosition = it) },
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = additionalPatterns,
                        onCheckedChange = { additionalPatterns = it },
                    )
                    Text(
                        text = stringResource(R.string.depth),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }

                Button(
                    onClick = {
                        controller?.reset()
                        running = true
                        stopping = false
                        rounds = 0
                        scope.launch {
                            val outcome = withContext(Dispatchers.IO) {
                                controller?.transmit(
                                    brands = state.selectedBrands(),
                                    additionalPatterns = additionalPatterns,
                                    delayMillis = delayMillis,
                                    onRound = { done ->
                                        progressChannel.trySend(RoundProgress(done))
                                    },
                                ) ?: RepetitiveOutcome.Stopped(0)
                            }
                            pendingOutcome = outcome
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.size(width = 220.dp, height = 220.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow_black_48dp),
                            contentDescription = stringResource(R.string.menu_repetitive_mode),
                            modifier = Modifier.size(96.dp),
                        )
                        Text(
                            stringResource(R.string.menu_repetitive_mode),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dropdown replacing the legacy spinner. Uses [ExposedDropdownMenuBox] so the
 * choices stay on screen; selection is stored as an index into
 * [RepetitiveState.choices].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandDropdown(
    choices: List<String>,
    selectedPosition: Int,
    enabled: Boolean,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = choices.getOrElse(selectedPosition) { choices.first() }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.repetitive_brand_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            choices.forEachIndexed { index, choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun RepetitiveOutcome.rounds(): Int = when (this) {
    is RepetitiveOutcome.Stopped -> rounds
    is RepetitiveOutcome.Error -> rounds
    else -> 0
}

private fun RepetitiveOutcome.messageRes(): Int = when (this) {
    is RepetitiveOutcome.Stopped -> R.string.toast_mode_stopped
    is RepetitiveOutcome.NoPatterns -> R.string.brand_list_no_pattern
    is RepetitiveOutcome.TransportUnavailable -> R.string.brand_list_transport_unavailable
    is RepetitiveOutcome.Error -> R.string.brand_list_send_failed
}

@Composable
private fun NoIrEmitterNotice(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.blaster_dialog_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.blaster_dialog_body),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun previewBrand(id: String) = Brand(
    id = id,
    displayName = id.replaceFirstChar { it.uppercase() },
    patterns = listOf(
        IrPattern(
            carrierHz = 38000,
            periods = intArrayOf(1, 1, 10, 10),
            sourceUrl = "https://example.com",
            license = "GPL-3.0-or-later",
        ),
    ),
)

@Preview(showBackground = true)
@Composable
private fun RepetitiveScreenPreview() {
    TvkillIndoTheme {
        RepetitiveScreen(
            brands = listOf(previewBrand("samsung"), previewBrand("sony")),
            additionalPatternsDefault = false,
            delayMillis = 0,
            controller = RepetitiveTransmissionController(FakePreviewTransport),
        )
    }
}

private object FakePreviewTransport : TvTransport {
    override val id: String = "preview"
    override val displayName: String = "Preview"
    override fun isAvailable(): Boolean = true
    override fun send(pattern: IrPattern): Result<Unit> = Result.success(Unit)
}
