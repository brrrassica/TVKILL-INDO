/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.ui.brandlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.alatcerdas.tvkillindo.R
import tech.alatcerdas.tvkillindo.domain.Brand
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.transport.TvTransport
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/**
 * First real Compose screen (M3 slice): a searchable, region-filterable list
 * of TV brands backed by [tech.alatcerdas.tvkillindo.data.PatternRepository].
 *
 * Tapping a brand transmits its best POWER_OFF pattern through
 * [controller]; the outcome is reported via the screen-level snackbar.
 * The legacy tab UI keeps working unchanged until full feature parity is
 * verified.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandListScreen(
    brands: List<Brand>,
    modifier: Modifier = Modifier,
    hasIrEmitter: Boolean = true,
    controller: BrandTransmissionController? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf(RegionFilter.ALL) }
    val state = BrandListState(query = query, selectedRegion = region)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Latest outcome is held in state so the resource is resolved via
    // stringResource() (composition-safe, lint-approved), then shown once
    // in a LaunchedEffect.
    var pendingOutcome by remember { mutableStateOf<TransmissionOutcome?>(null) }
    pendingOutcome?.let { outcome ->
        val message = stringResource(outcome.messageRes())
        LaunchedEffect(outcome) {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                // TODO(M5): open a "did it work?" feedback report.
            }
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
                .padding(innerPadding),
        ) {
            if (!hasIrEmitter) {
                NoIrEmitterNotice(modifier = Modifier.padding(16.dp))
                return@Column
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.brand_list_search_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            RegionFilterChips(
                selected = region,
                onSelect = { region = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            val visible = state.filter(brands)
            if (visible.isEmpty()) {
                Text(
                    text = stringResource(R.string.brand_list_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(24.dp),
                )
            } else {
                LazyColumn {
                    items(visible, key = { it.id }) { brand ->
                        BrandRow(
                            brand = brand,
                            onBrandClick = { brand ->
                                pendingOutcome = controller?.transmit(brand)
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionFilterChips(
    selected: RegionFilter,
    onSelect: (RegionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RegionFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(filter.labelRes())) },
            )
        }
    }
}

private fun RegionFilter.labelRes(): Int = when (this) {
    RegionFilter.ALL -> R.string.region_filter_all
    RegionFilter.PREMIUM -> R.string.region_filter_premium
    RegionFilter.MAINSTREAM -> R.string.region_filter_mainstream
    RegionFilter.LOCAL_ID -> R.string.region_filter_local
    RegionFilter.LEGACY -> R.string.region_filter_legacy
}

private fun Brand.Region.labelRes(): Int = when (this) {
    Brand.Region.PREMIUM -> R.string.region_filter_premium
    Brand.Region.MAINSTREAM -> R.string.region_filter_mainstream
    Brand.Region.LOCAL_ID -> R.string.region_filter_local
    Brand.Region.LEGACY -> R.string.region_filter_legacy
    Brand.Region.GLOBAL -> R.string.region_filter_all
}

@Composable
private fun BrandRow(
    brand: Brand,
    onBrandClick: (Brand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val powerOffCount = brand.patterns.count { it.function == IrPattern.PatternFunction.POWER_OFF }
    ListItem(
        headlineContent = { Text(brand.displayName) },
        supportingContent = {
            Text(
                stringResource(R.string.brand_list_pattern_count, powerOffCount),
            )
        },
        trailingContent = {
            Text(
                text = stringResource(brand.region.labelRes()),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        modifier = modifier.clickable(onClick = { onBrandClick(brand) }),
    )
}

private fun TransmissionOutcome.messageRes(): Int = when (this) {
    TransmissionOutcome.Sent -> R.string.brand_list_sent
    TransmissionOutcome.NoPattern -> R.string.brand_list_no_pattern
    TransmissionOutcome.TransportUnavailable -> R.string.brand_list_transport_unavailable
    is TransmissionOutcome.Error -> R.string.brand_list_send_failed
}

@Composable
private fun NoIrEmitterNotice(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
private fun BrandListScreenPreview() {
    TvkillIndoTheme {
        BrandListScreen(
            brands = previewBrands(),
            controller = BrandTransmissionController(FakePreviewTransport),
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
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            IrPattern(
                carrierHz = 38343,
                periods = intArrayOf(1, 1, 170, 172),
                sourceUrl = "https://example.com",
                license = "GPL-3.0-or-later",
            ),
        ),
    ),
    Brand(
        id = "polytron",
        displayName = "Polytron",
        region = Brand.Region.LOCAL_ID,
        patterns = emptyList(),
    ),
)
