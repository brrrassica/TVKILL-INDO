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
import tech.alatcerdas.tvkillindo.domain.IrPattern
import tech.alatcerdas.tvkillindo.domain.IrPattern.Confidence
import tech.alatcerdas.tvkillindo.domain.IrPattern.PatternFunction

/**
 * The 16 legacy brands inherited from upstream TVKILL (b2117e2), expressed in
 * the versioned catalog data model ([IrPattern]).
 *
 * Every raw timing array is byte-identical to the values that previously lived
 * in BrandContainer.kt, so no emission behavior changes in this round. The
 * arrays are alternating on/off durations in carrier periods, matching the
 * upstream TV-B-Gone derived data format. Power-off comes first, mute second.
 *
 * Attribution: the timings derive from the upstream TVKILL project, which in
 * turn credits TV-B-Gone (per its source comments). Upstream TVKILL is GPLv3,
 * so redistribution here under GPL-3.0-or-later is valid.
 */
object LegacyCatalog {

    private const val SOURCE_URL = "https://github.com/42SK/TVKILL"
    private const val LICENSE = "GPL-3.0-or-later"

    /**
     * Source for project-generated COMMUNITY patterns: the universal TV code
     * list bundled with Flipper Zero firmware (GPL-3.0).
     */
    private const val COMMUNITY_SOURCE_URL =
        "https://github.com/flipperdevices/flipperzero-firmware/blob/dev/applications/main/infrared/resources/infrared/assets/tv.ir"

    private fun powerOff(carrierHz: Int, periods: IntArray, model: String? = null) = IrPattern(
        carrierHz = carrierHz,
        periods = periods,
        function = PatternFunction.POWER_OFF,
        sourceUrl = SOURCE_URL,
        license = LICENSE,
        confidence = Confidence.IMPORTED,
        model = model,
    )

    private fun mute(carrierHz: Int, periods: IntArray, model: String? = null) = IrPattern(
        carrierHz = carrierHz,
        periods = periods,
        function = PatternFunction.MUTE,
        sourceUrl = SOURCE_URL,
        license = LICENSE,
        confidence = Confidence.IMPORTED,
        model = model,
    )

    /**
     * COMMUNITY patterns are project-generated from a properly-licensed public
     * code list (see [COMMUNITY_SOURCE_URL]) and are best-effort until verified
     * on real hardware.
     */
    private fun communityPowerOff(carrierHz: Int, periods: IntArray, model: String? = null) = IrPattern(
        carrierHz = carrierHz,
        periods = periods,
        function = PatternFunction.POWER_OFF,
        sourceUrl = COMMUNITY_SOURCE_URL,
        license = LICENSE,
        confidence = Confidence.COMMUNITY,
        model = model,
    )

    private fun communityMute(carrierHz: Int, periods: IntArray, model: String? = null) = IrPattern(
        carrierHz = carrierHz,
        periods = periods,
        function = PatternFunction.MUTE,
        sourceUrl = COMMUNITY_SOURCE_URL,
        license = LICENSE,
        confidence = Confidence.COMMUNITY,
        model = model,
    )

    private val samsung = Brand(
        id = "samsung",
        displayName = "Samsung",
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            powerOff(38343, intArrayOf(1, 1, 170, 172, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 1788, 170, 172, 21, 22, 21, 4907)),
            mute(38343, intArrayOf(1, 1, 170, 172, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 1788, 170, 172, 21, 22, 21, 4907)),
        ),
    )

    private val sony = Brand(
        id = "sony",
        displayName = "Sony",
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            powerOff(40064, intArrayOf(1, 1, 96, 24, 48, 24, 48, 24, 48, 24, 48, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 985, 96, 24, 48, 24, 48, 24, 48, 24, 48, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 985, 96, 24, 48, 24, 48, 24, 48, 24, 48, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 5128)),
            mute(40192, intArrayOf(1, 1, 96, 24, 24, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 1060, 96, 24, 24, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 1060, 96, 24, 24, 24, 24, 24, 48, 24, 24, 24, 48, 24, 24, 24, 24, 24, 48, 24, 24, 24, 24, 24, 24, 24, 24, 5144)),
        ),
    )

    private val lg = Brand(
        id = "lg",
        displayName = "LG",
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            powerOff(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 4892)),
            mute(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4892)),
        ),
    )

    private val panasonic = Brand(
        id = "panasonic",
        displayName = "Panasonic",
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            powerOff(36873, intArrayOf(1, 1, 128, 64, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 48, 16, 48, 16, 48, 16, 48, 16, 48, 16, 16, 16, 16, 16, 48, 16, 48, 16, 48, 16, 48, 16, 48, 16, 48, 16, 16, 16, 48, 16, 4719)),
            mute(36873, intArrayOf(1, 1, 128, 64, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 48, 16, 48, 16, 16, 16, 16, 16, 16, 16, 48, 16, 16, 16, 16, 16, 48, 16, 48, 16, 16, 16, 48, 16, 4719)),
        ),
    )

    private val philips = Brand(
        id = "philips",
        displayName = "Philips",
        region = Brand.Region.PREMIUM,
        patterns = listOf(
            powerOff(36231, intArrayOf(1, 1, 32, 32, 64, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 63, 32, 32, 64, 32, 32, 4637)),
            mute(36231, intArrayOf(1, 1, 32, 32, 32, 32, 64, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 63, 32, 32, 64, 64, 32, 4637)),
        ),
    )

    private val nec = Brand(
        id = "nec",
        displayName = "NEC",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 4907)),
            powerOff(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4892), model = "NEC[30]"),
            mute(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4892)),
        ),
    )

    private val sharp = Brand(
        id = "sharp",
        displayName = "Sharp",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38226, intArrayOf(1, 1, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 70, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 30, 10, 1663, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 70, 10, 30, 10, 70, 10, 30, 10, 70, 10, 1663, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 70, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 30, 10, 4892)),
            mute(38109, intArrayOf(1, 1, 10, 70, 10, 30, 10, 30, 10, 30, 10, 30, 10, 70, 10, 70, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 1698, 10, 70, 10, 30, 10, 30, 10, 30, 10, 30, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 70, 10, 70, 10, 30, 10, 70, 10, 1698, 10, 70, 10, 30, 10, 30, 10, 30, 10, 30, 10, 70, 10, 70, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 4878)),
        ),
    )

    private val jvc = Brand(
        id = "jvc",
        displayName = "JVC",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38226, intArrayOf(1, 1, 321, 161, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 60, 20, 20, 20, 20, 20, 20, 20, 859, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 60, 20, 20, 20, 20, 20, 20, 20, 833, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 60, 20, 20, 20, 20, 20, 20, 20, 4892)),
            mute(38226, intArrayOf(1, 1, 321, 161, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 899, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 873, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 60, 20, 60, 20, 60, 20, 20, 20, 20, 20, 20, 20, 4892)),
        ),
    )

    private val toshiba = Brand(
        id = "toshiba",
        displayName = "Toshiba",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 4892)),
            mute(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 4907)),
        ),
    )

    private val mitsubishi = Brand(
        id = "mitsubishi",
        displayName = "Mitsubishi",
        region = Brand.Region.LEGACY,
        patterns = listOf(
            powerOff(32894, intArrayOf(1, 1, 10, 70, 10, 70, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 834, 10, 70, 10, 70, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 4210)),
            mute(32981, intArrayOf(1, 1, 10, 70, 10, 70, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 70, 10, 30, 10, 30, 10, 836, 10, 70, 10, 70, 10, 70, 10, 30, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 30, 10, 30, 10, 70, 10, 70, 10, 30, 10, 30, 10, 4221)),
        ),
    )

    private val vizio = Brand(
        id = "vizio",
        displayName = "Vizio",
        region = Brand.Region.LEGACY,
        patterns = listOf(
            powerOff(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4892)),
            mute(38226, intArrayOf(1, 1, 343, 171, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4892)),
        ),
    )

    private val rca = Brand(
        id = "rca",
        displayName = "RCA",
        region = Brand.Region.LEGACY,
        patterns = listOf(
            powerOff(57603, intArrayOf(1, 1, 228, 230, 28, 115, 28, 115, 28, 115, 28, 115, 28, 58, 28, 58, 28, 115, 28, 115, 28, 115, 28, 58, 28, 115, 28, 115, 28, 58, 28, 58, 28, 58, 28, 58, 28, 115, 28, 115, 28, 58, 28, 58, 28, 58, 28, 115, 28, 58, 28, 58, 28, 7373)),
            mute(57603, intArrayOf(1, 1, 228, 230, 28, 115, 28, 115, 28, 115, 28, 115, 28, 115, 28, 58, 28, 58, 28, 115, 28, 115, 28, 115, 28, 115, 28, 115, 28, 115, 28, 58, 28, 58, 28, 58, 28, 58, 28, 115, 28, 115, 28, 58, 28, 58, 28, 58, 28, 58, 28, 58, 28, 58, 28, 7373)),
        ),
    )

    private val pioneer = Brand(
        id = "pioneer",
        displayName = "Pioneer",
        region = Brand.Region.LEGACY,
        patterns = listOf(
            powerOff(40192, intArrayOf(1, 1, 342, 170, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 63, 22, 63, 22, 1042, 342, 170, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 63, 22, 63, 22, 5144)),
            mute(40064, intArrayOf(1, 1, 342, 169, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 1039, 342, 169, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 21, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 63, 22, 21, 22, 63, 22, 5128)),
        ),
    )

    private val hisense = Brand(
        id = "hisense",
        displayName = "Hisense",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38343, intArrayOf(1, 1, 341, 173, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 872, 341, 173, 21, 4907)),
            mute(38226, intArrayOf(1, 1, 341, 173, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 64, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 64, 21, 65, 21, 64, 21, 22, 21, 64, 21, 64, 21, 22, 21, 64, 21, 22, 21, 22, 21, 22, 21, 64, 21, 22, 21, 22, 21, 65, 21, 22, 21, 64, 21, 65, 21, 65, 21, 869, 341, 173, 21, 4892)),
        ),
    )

    private val akai = Brand(
        id = "akai",
        displayName = "Akai",
        region = Brand.Region.LEGACY,
        patterns = listOf(
            powerOff(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4907)),
            mute(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 4907)),
        ),
    )

    /**
     * Coocaa is Skyworth's smart-TV brand (confirmed: rebranded Skyworth).
     * No Skyworth/Coocaa power code exists in TV-B-Gone, LIRC or Tasmota, so
     * these NEC patterns were generated from the most common NEC TV power
     * commands in the Flipper Zero universal TV list (GPL-3.0, from
     * https://github.com/flipperdevices/flipperzero-firmware, file
     * applications/main/infrared/resources/infrared/assets/tv.ir).
     * They are COMMUNITY confidence / best-effort until verified on real
     * Coocaa hardware (M5).
     */
    private val coocaa = Brand(
        id = "coocaa",
        displayName = "Coocaa (Skyworth)",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            communityPowerOff(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 1022), model = "NEC addr 0x08 cmd 0x0F"),
            communityPowerOff(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 21, 21, 64, 21, 1022), model = "NEC addr 0x08 cmd 0x12"),
            communityPowerOff(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 21, 21, 64, 21, 1022), model = "NEC addr 0x40 cmd 0x12"),
            communityPowerOff(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 21, 21, 64, 21, 1022), model = "NEC addr 0x38 cmd 0x12"),
            communityPowerOff(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 1022), model = "NEC addr 0x16 cmd 0x0F"),
            communityMute(38461, intArrayOf(342, 171, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 1022), model = "NEC addr 0x40 cmd 0x10"),
            communityMute(38461, intArrayOf(342, 171, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 64, 21, 1022), model = "NEC addr 0x38 cmd 0x04"),
            communityMute(38461, intArrayOf(342, 171, 21, 21, 21, 64, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21, 21, 21, 21, 21, 21, 21, 64, 21, 21, 21, 64, 21, 21, 21, 21, 21, 64, 21, 64, 21, 64, 21, 21, 21, 64, 21, 21, 21, 64, 21, 64, 21, 1022), model = "NEC addr 0x40 cmd 0x14"),
        ),
    )

    private val aoc = Brand(
        id = "aoc",
        displayName = "AOC",
        region = Brand.Region.MAINSTREAM,
        patterns = listOf(
            powerOff(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4907)),
            mute(38343, intArrayOf(1, 1, 343, 172, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 22, 21, 65, 21, 22, 21, 22, 21, 65, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 65, 21, 65, 21, 22, 21, 65, 21, 65, 21, 65, 21, 65, 21, 65, 21, 4907)),
        ),
    )

    /**
     * Catalog order mirrors the upstream universal-mode ordering
     * (BrandContainer.allBrands), which the app already relies on.
     */
    val brands: List<Brand> = listOf(
        samsung, sony, lg, panasonic, philips, nec, sharp, jvc, toshiba,
        mitsubishi, vizio, rca, pioneer, hisense, akai, aoc, coocaa,
    )
}
