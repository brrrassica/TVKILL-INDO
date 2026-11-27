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
package tech.alatcerdas.tvkillindo.ui.universal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import tech.alatcerdas.tvkillindo.domain.UniversalSequence
import tech.alatcerdas.tvkillindo.transport.TvTransport
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/** Progress sample pushed from the transmitting thread to the UI. */
private data class SendProgress(val sent: Int, val total: Int)

/**
 * Universal-mode Compose screen (M3 slice): one big button transmits a
 * power-off pattern of every brand in sequence, mirroring the legacy
 * universal tab. Additional (deeper) patterns are opt-in, matching the
 * legacy "additional patterns" setting; the inter-pattern delay follows the
 * legacy "delay" setting.
 *
 * Transmission runs on [Dispatchers.IO]; progress reaches the UI through a
 * conflated channel so no Compose state is mutated off the main thread.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalScreen(
    brands: List<Brand>,
    additionalPatternsDefault: Boolean,
    delayMillis: Long,
    modifier: Modifier = Modifier,
    hasIrEmitter: Boolean = true,
    showMute: Boolean = false,
    controller: UniversalTransmissionController? = null,
) {
    var running by remember { mutableStateOf(false) }
    var cancelling by remember { mutableStateOf(false) }
    var additionalPatterns by rememberSaveable { mutableStateOf(additionalPatternsDefault) }
    LaunchedEffect(additionalPatternsDefault) { additionalPatterns = additionalPatternsDefault }
    var sentCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }
    var pendingOutcome by remember { mutableStateOf<UniversalOutcome?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val progressChannel = remember { Channel<SendProgress>(Channel.CONFLATED) }

    DisposableEffect(controller) {
        onDispose { controller?.cancel() }
    }

    LaunchedEffect(progressChannel) {
        for (progress in progressChannel) {
            sentCount = progress.sent
            totalCount = progress.total
        }
    }

    pendingOutcome?.let { outcome ->
        val message = stringResource(outcome.messageRes(), outcome.stepsSent())
        LaunchedEffect(outcome) {
            running = false
            cancelling = false
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!hasIrEmitter) {
                NoIrEmitterNotice()
                return@Column
            }

            if (running) {
                Text(
                    text = stringResource(R.string.pd_transmission_in_progress),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (totalCount > 0) {
                    LinearProgressIndicator(
                        progress = { sentCount.toFloat() / totalCount },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.universal_progress, sentCount, totalCount),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = {
                        cancelling = true
                        controller?.cancel()
                    },
                    enabled = !cancelling,
                ) {
                    Text(stringResource(R.string.cancel))
                }
            } else {
                Button(
                    onClick = {
                        controller?.reset()
                        running = true
                        cancelling = false
                        sentCount = 0
                        totalCount = UniversalSequence.build(brands, additionalPatterns).size
                        scope.launch {
                            val outcome = withContext(Dispatchers.IO) {
                                controller?.transmit(
                                    brands = brands,
                                    additionalPatterns = additionalPatterns,
                                    delayMillis = delayMillis,
                                    onProgress = { sent, total ->
                                        progressChannel.trySend(SendProgress(sent, total))
                                    },
                                ) ?: UniversalOutcome.Completed(0)
                            }
                            pendingOutcome = outcome
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.size(width = 220.dp, height = 220.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_power_settings_new_black_48dp),
                            contentDescription = stringResource(R.string.off),
                            modifier = Modifier.size(96.dp),
                        )
                        Text(stringResource(R.string.off), style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (showMute) Button(
                    onClick = {
                        controller?.reset()
                        running = true
                        cancelling = false
                        sentCount = 0
                        totalCount = UniversalSequence.muteSequence(brands).size
                        scope.launch {
                            val outcome = withContext(Dispatchers.IO) {
                                controller?.transmitMute(
                                    brands = brands,
                                    delayMillis = delayMillis,
                                    onProgress = { sent, total ->
                                        progressChannel.trySend(SendProgress(sent, total))
                                    },
                                ) ?: UniversalOutcome.Completed(0)
                            }
                            pendingOutcome = outcome
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    modifier = Modifier.size(width = 132.dp, height = 56.dp),
                ) {
                    Text(stringResource(R.string.mute))
                }

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
            }
        }
    }
}

private fun UniversalOutcome.stepsSent(): Int = when (this) {
    is UniversalOutcome.Completed -> steps
    is UniversalOutcome.Cancelled -> steps
    is UniversalOutcome.Error -> steps
    else -> 0
}

private fun UniversalOutcome.messageRes(): Int = when (this) {
    is UniversalOutcome.Completed -> R.string.universal_completed
    is UniversalOutcome.NoPatterns -> R.string.brand_list_no_pattern
    is UniversalOutcome.TransportUnavailable -> R.string.brand_list_transport_unavailable
    is UniversalOutcome.Cancelled -> R.string.universal_cancelled
    is UniversalOutcome.Error -> R.string.brand_list_send_failed
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

@Preview(showBackground = true)
@Composable
private fun UniversalScreenPreview() {
    TvkillIndoTheme {
        UniversalScreen(
            brands = previewBrands(),
            additionalPatternsDefault = false,
            delayMillis = 0,
            controller = UniversalTransmissionController(FakePreviewTransport),
        )
    }
}

private object FakePreviewTransport : TvTransport {
    override val id: String = "preview"
    override val displayName: String = "Preview"
    override fun isAvailable(): Boolean = true
    override fun send(pattern: IrPattern): Result<Unit> = Result.success(Unit)
}

private fun previewBrands(): List<Brand> = listOf(
    Brand(
        id = "samsung",
        displayName = "Samsung",
        patterns = listOf(
            IrPattern(
                carrierHz = 38343,
                periods = intArrayOf(1, 1, 170, 172),
                sourceUrl = "https://example.com",
                license = "GPL-3.0-or-later",
            ),
        ),
    ),
)
