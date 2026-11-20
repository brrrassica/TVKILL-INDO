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
package tech.alatcerdas.tvkillindo.ui

import android.content.Context
import android.content.Intent
import android.hardware.ConsumerIrManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tech.alatcerdas.tvkillindo.R
import tech.alatcerdas.tvkillindo.Settings
import tech.alatcerdas.tvkillindo.data.CatalogRepository
import tech.alatcerdas.tvkillindo.transport.ConsumerIrTvTransport
import tech.alatcerdas.tvkillindo.ui.brandlist.BrandListScreen
import tech.alatcerdas.tvkillindo.ui.brandlist.BrandTransmissionController
import tech.alatcerdas.tvkillindo.ui.repetitive.RepetitiveScreen
import tech.alatcerdas.tvkillindo.ui.repetitive.RepetitiveTransmissionController
import tech.alatcerdas.tvkillindo.ui.theme.TvkillIndoTheme
import tech.alatcerdas.tvkillindo.ui.universal.UniversalScreen
import tech.alatcerdas.tvkillindo.ui.universal.UniversalTransmissionController

/**
 * Shared per-activity state for the Compose shell: the catalog, the IR
 * availability, the delay setting, and the transmission controllers. The
 * screens stay stateless and previewable; everything Android-specific lives
 * here.
 */
class ShellViewModel : ViewModel() {

    private val repository = CatalogRepository()

    val brands: List<tech.alatcerdas.tvkillindo.domain.Brand> = repository.brands()

    /** Additional-patterns preference (legacy "depth" key). Loaded once. */
    var additionalPatterns: Boolean = false
        private set

    /** Delay between patterns in milliseconds (legacy "delay" key). Loaded once. */
    var delayMillis: Long = 0L
        private set

    fun loadSettings(settings: Settings) {
        additionalPatterns = settings.additionalPatterns.value ?: false
        delayMillis = settings.preferences.getString("delay", "0")?.toLongOrNull() ?: 0L
    }

    private var repetitiveControllerRef: RepetitiveTransmissionController? = null

    fun repetitiveController(transport: ConsumerIrTvTransport): RepetitiveTransmissionController =
        repetitiveControllerRef ?: RepetitiveTransmissionController(transport).also {
            repetitiveControllerRef = it
        }

    override fun onCleared() {
        super.onCleared()
        repetitiveControllerRef?.cancel()
    }
}

/**
 * Compose shell for M3: a single activity with bottom navigation hosting the
 * universal, brands and repetitive screens. Deliberately **not** the launcher
 * and not exported: the legacy tab-based MainActivity stays user-facing until
 * the Compose rewrite reaches full feature parity (next M3 slice flips the
 * launcher and removes the legacy UI).
 */
class ComposeShellActivity : ComponentActivity() {

    private val shellViewModel: ShellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val hasIrEmitter = irManager != null && irManager.hasIrEmitter()
        val transport = ConsumerIrTvTransport(this)
        val settings = Settings.with(this)

        shellViewModel.loadSettings(settings)

        setContent {
            TvkillIndoTheme {
                ComposeShell(
                    brands = shellViewModel.brands,
                    hasIrEmitter = hasIrEmitter,
                    additionalPatterns = shellViewModel.additionalPatterns,
                    delayMillis = shellViewModel.delayMillis,
                    transport = transport,
                    repetitiveController = shellViewModel.repetitiveController(transport),
                )
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ComposeShellActivity::class.java))
        }
    }
}

private enum class ShellTab(val route: String, val labelRes: Int, val iconRes: Int) {
    UNIVERSAL("universal", R.string.menu_universal_mode, R.drawable.ic_power_settings_new_black_48dp),
    BRANDS("brands", R.string.menu_specific_devices, R.drawable.ic_settings_black_48dp),
    REPETITIVE("repetitive", R.string.menu_repetitive_mode, R.drawable.ic_play_arrow_black_48dp),
}

/**
 * The full shell UI: [NavHost] over the three M3 screens plus a
 * [NavigationBar] mirroring the legacy TabLayout tabs.
 */
@Composable
fun ComposeShell(
    brands: List<tech.alatcerdas.tvkillindo.domain.Brand>,
    hasIrEmitter: Boolean,
    additionalPatterns: Boolean,
    delayMillis: Long,
    transport: ConsumerIrTvTransport,
    repetitiveController: RepetitiveTransmissionController,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                ShellTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = stringResource(tab.labelRes),
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShellTab.UNIVERSAL.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ShellTab.UNIVERSAL.route) {
                UniversalScreen(
                    brands = brands,
                    additionalPatternsDefault = additionalPatterns,
                    delayMillis = delayMillis,
                    hasIrEmitter = hasIrEmitter,
                    controller = UniversalTransmissionController(transport),
                )
            }
            composable(ShellTab.BRANDS.route) {
                BrandListScreen(
                    brands = brands,
                    hasIrEmitter = hasIrEmitter,
                    controller = BrandTransmissionController(transport),
                )
            }
            composable(ShellTab.REPETITIVE.route) {
                RepetitiveScreen(
                    brands = brands,
                    additionalPatternsDefault = additionalPatterns,
                    delayMillis = delayMillis,
                    hasIrEmitter = hasIrEmitter,
                    controller = repetitiveController,
                )
            }
        }
    }
}
