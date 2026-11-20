/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.ui.brandlist

import android.content.Context
import android.content.Intent
import android.hardware.ConsumerIrManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tech.alatcerdas.tvkillindo.data.CatalogRepository
import tech.alatcerdas.tvkillindo.transport.ConsumerIrTvTransport
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/**
 * Entry point for the first Compose screen (M3 slice). Deliberately **not**
 * exported and not the launcher: the legacy tab-based MainActivity remains
 * the user-facing UI until the Compose rewrite reaches feature parity.
 * Launch via [start] (e.g. from a debug tool or the next M3 navigation slice).
 */
class BrandListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val hasIrEmitter = irManager != null && irManager.hasIrEmitter()

        val transport = ConsumerIrTvTransport(this)
        val controller = BrandTransmissionController(transport)

        setContent {
            TvkillIndoTheme {
                BrandListScreen(
                    brands = CatalogRepository().brands(),
                    hasIrEmitter = hasIrEmitter,
                    controller = controller,
                )
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, BrandListActivity::class.java))
        }
    }
}
