/*
 * Copyright (C) 2026 TVKILL-INDO contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package tech.alatcerdas.tvkillindo.domain

/** A TV brand and its ordered power-off patterns. */
data class Brand(
    val id: String,
    val displayName: String,
    val region: Region = Region.GLOBAL,
    val patterns: List<IrPattern>,
) {
    enum class Region { LOCAL_ID, MAINSTREAM, PREMIUM, LEGACY, GLOBAL }
}
