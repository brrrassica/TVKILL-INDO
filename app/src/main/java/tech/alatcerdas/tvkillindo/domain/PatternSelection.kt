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
package tech.alatcerdas.tvkillindo.domain

import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence

private val CONFIDENCE_RANK = mapOf(
    Confidence.VERIFIED to 0,
    Confidence.COMMUNITY to 1,
    Confidence.IMPORTED to 2,
)

/**
 * Single-pattern transmission (brands screen): one tap sends exactly one,
 * best-qualified pattern for the brand — not the full sequence the legacy
 * service transmits.
 *
 * Selection rules (unit-tested in [tech.alatcerdas.tvkillindo.domain.PatternSelectionTest]):
 *  - only patterns of the requested [IrPattern.PatternFunction] are eligible;
 *  - higher confidence wins: VERIFIED > COMMUNITY > IMPORTED;
 *  - among equal confidence, the catalog order decides (earliest first),
 *    because M4 orders patterns best-first within a brand.
 */
fun selectPattern(brand: Brand, function: IrPattern.PatternFunction): IrPattern? =
    brand.patterns
        .filter { it.function == function }
        .minWithOrNull(
            compareBy(
                { CONFIDENCE_RANK[it.confidence] ?: Int.MAX_VALUE },
                { brand.patterns.indexOf(it) },
            ),
        )

/** Single-pattern POWER_OFF transmission. */
fun selectPowerOffPattern(brand: Brand): IrPattern? =
    selectPattern(brand, IrPattern.PatternFunction.POWER_OFF)

/**
 * Single-pattern MUTE transmission (experimental, gated by the legacy
 * `show_mute` setting in the UI).
 */
fun selectMutePattern(brand: Brand): IrPattern? =
    selectPattern(brand, IrPattern.PatternFunction.MUTE)
