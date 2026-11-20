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

import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction

/** One step of the universal-mode transmission: a single brand's pattern. */
data class UniversalStep(
    val brandId: String,
    val brandName: String,
    val pattern: IrPattern,
)

/**
 * Builds the universal-mode transmission sequence — pure and unit-tested
 * ([tech.alatcerdas.tvkillindo.domain.UniversalSequenceTest]).
 *
 * Rules mirror the legacy [tech.alatcerdas.tvkillindo.TransmitService]
 * behavior and the M4 ordering intent:
 *  - only POWER_OFF patterns are eligible (MUTE is never sent in universal
 *    mode);
 *  - round 0 sends one power-off pattern of every brand, in catalog order;
 *  - with [UniversalSequence.build]'s `additionalPatterns` enabled, deeper
 *    rounds append each brand's next power-off pattern, up to `maxDepth`
 *    rounds (the "additional patterns" setting);
 *  - rounds stop as soon as no brand has a pattern left at that depth.
 */
object UniversalSequence {

    fun build(
        brands: List<Brand>,
        additionalPatterns: Boolean,
        maxDepth: Int = Int.MAX_VALUE,
    ): List<UniversalStep> {
        require(maxDepth >= 1) { "maxDepth must be at least 1" }
        val depth = if (additionalPatterns) maxDepth else 1

        val steps = mutableListOf<UniversalStep>()
        for (round in 0 until depth) {
            var addedInRound = 0
            brands.forEach { brand ->
                val pattern = brand.patterns
                    .filter { it.function == PatternFunction.POWER_OFF }
                    .getOrNull(round)
                    ?: return@forEach
                steps += UniversalStep(brand.id, brand.displayName, pattern)
                addedInRound++
            }
            if (addedInRound == 0) break
        }
        return steps
    }
}
