/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.data

import tech.alatcerdas.tvkillindo.domain.Brand

/**
 * Source of brands and patterns. The 2.0 implementation will read a bundled,
 * versioned catalog file (M4); keep this interface network-free for F-Droid.
 */
interface PatternRepository {
    fun brands(): List<Brand>
    fun brand(id: String): Brand?
    fun universalPatterns(): List<Brand>
}
