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

import android.content.Context
import android.content.Intent
import android.hardware.ConsumerIrManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tech.alatcerdas.tvkillindo.Settings
import tech.alatcerdas.tvkillindo.data.CatalogRepository
import tech.alatcerdas.tvkillindo.transport.ConsumerIrTvTransport
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme

/**
 * Activity host for the Compose universal-mode screen (M3 slice). Mirrors
 * [tech.alatcerdas.tvkillindo.ui.brandlist.BrandListActivity]; the legacy
 * tab UI keeps working until full feature parity is verified.
 */
class UniversalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val hasIrEmitter = irManager != null && irManager.hasIrEmitter()
        val transport = ConsumerIrTvTransport(this)
        val controller = UniversalTransmissionController(transport)
        val settings = Settings.with(this)
        val delayMillis = settings.preferences.getString("delay", "0")?.toLongOrNull() ?: 0L

        setContent {
            TvkillIndoTheme {
                UniversalScreen(
                    brands = CatalogRepository().brands(),
                    additionalPatternsDefault = settings.additionalPatterns.value ?: false,
                    delayMillis = delayMillis,
                    hasIrEmitter = hasIrEmitter,
                    controller = controller,
                )
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, UniversalActivity::class.java))
        }
    }
}
