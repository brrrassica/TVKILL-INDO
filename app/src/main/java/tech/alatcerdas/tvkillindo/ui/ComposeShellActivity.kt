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
import tech.alatcerdas.tvkillindo.ui.settings.SettingsController
import tech.alatcerdas.tvkillindo.ui.settings.SettingsScreen
import tech.alatcerdas.tvkillindo.ui.settings.SharedPreferencesSettingsStore
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

    /** Experimental mute visibility (legacy "show_mute" key). Loaded once. */
    var showMute: Boolean = false
        private set

    fun loadSettings(settings: Settings) {
        additionalPatterns = settings.additionalPatterns.value ?: false
        delayMillis = settings.preferences.getString("delay", "0")?.toLongOrNull() ?: 0L
        showMute = settings.preferences.getBoolean("show_mute", false)
    }

    // Controllers are cached per ViewModel instance: their cancellation flags
    // are instance-local, so recreating them per recomposition would leave
    // running transmissions unstoppable (issue #3).
    private var repetitiveControllerRef: RepetitiveTransmissionController? = null
    private var universalControllerRef: UniversalTransmissionController? = null
    private var brandControllerRef: BrandTransmissionController? = null

    fun repetitiveController(transport: ConsumerIrTvTransport): RepetitiveTransmissionController =
        repetitiveControllerRef ?: RepetitiveTransmissionController(transport).also {
            repetitiveControllerRef = it
        }

    fun universalController(transport: ConsumerIrTvTransport): UniversalTransmissionController =
        universalControllerRef ?: UniversalTransmissionController(transport).also {
            universalControllerRef = it
        }

    fun brandController(transport: ConsumerIrTvTransport): BrandTransmissionController =
        brandControllerRef ?: BrandTransmissionController(transport).also {
            brandControllerRef = it
        }

    override fun onCleared() {
        super.onCleared()
        repetitiveControllerRef?.cancel()
        universalControllerRef?.cancel()
        // BrandTransmissionController is synchronous (no long-running run),
        // so it needs no cancellation on teardown.
    }
}

/**
 * Compose shell for M3: a single activity with bottom navigation hosting the
 * universal, brands, repetitive and settings screens. This is the app's
 * launcher entry point; the legacy tab-based UI was removed in the M3
 * "flip launcher" slice.
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

        // The settings tab persists through the legacy preference keys; the
        // shell re-reads them via onSettingsChanged so transmission screens
        // always use the latest values (upstream issue #52).
        val settingsController = SettingsController(SharedPreferencesSettingsStore(this))

        val universalController = shellViewModel.universalController(transport)
        val brandController = shellViewModel.brandController(transport)

        setContent {
            TvkillIndoTheme {
                ComposeShell(
                    brands = shellViewModel.brands,
                    hasIrEmitter = hasIrEmitter,
                    additionalPatterns = shellViewModel.additionalPatterns,
                    delayMillis = shellViewModel.delayMillis,
                    showMute = shellViewModel.showMute,
                    transport = transport,
                    universalController = universalController,
                    brandController = brandController,
                    repetitiveController = shellViewModel.repetitiveController(transport),
                    settingsController = settingsController,
                    onSettingsChanged = {
                        shellViewModel.loadSettings(Settings.with(this))
                    },
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
    SETTINGS("settings", R.string.menu_settings, R.drawable.ic_clear_black_48dp),
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
    showMute: Boolean,
    transport: ConsumerIrTvTransport,
    universalController: UniversalTransmissionController,
    brandController: BrandTransmissionController,
    repetitiveController: RepetitiveTransmissionController,
    settingsController: SettingsController,
    onSettingsChanged: () -> Unit,
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
                    showMute = showMute,
                    controller = universalController,
                )
            }
            composable(ShellTab.BRANDS.route) {
                BrandListScreen(
                    brands = brands,
                    hasIrEmitter = hasIrEmitter,
                    showMute = showMute,
                    controller = brandController,
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
            composable(ShellTab.SETTINGS.route) {
                SettingsScreen(
                    controller = settingsController,
                    onChanged = onSettingsChanged,
                )
            }
        }
    }
}
