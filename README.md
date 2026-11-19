# TVKILL-INDO

**TVKILL-INDO** is an open-source Android app that turns off TVs using a phone's built-in infrared (IR) blaster. It is a maintained fork of [42SK/TVKILL](https://github.com/42SK/TVKILL) ([GPLv3](LICENSE)), adapted for current Android versions and for TVs commonly sold in Indonesia.

> **2.0-indo is in development.** The build system, package identity and documentation are being modernized first; the Compose UI rewrite and the expanded IR pattern catalog land in later milestones. See [plans/milestones.md](plans/milestones.md) for the roadmap.

## What it does

- Points your phone at a TV and transmits IR "power off" patterns.
- Modes: universal (all known patterns), brand-specific OFF/MUTE, and repetitive mode.
- Optional home-screen widget with OFF / STOP.
- Works fully offline: no account, no analytics, no ads, no network permission.

## Requirements

- Android 7.0 (API 24) or newer.
- A phone with an IR blaster (`ConsumerIrManager.hasIrEmitter()`).
  - Reference devices used by this fork: **LG V20** (Android 7.x) and **Vivo Y29 4G**.
  - Most flagship phones no longer include an IR blaster; the app shows an explanation on those devices.
- `compileSdk`/`targetSdk` 36; the install warning shown by older builds is gone in this release line.

## Install

This fork is **not published on Google Play**.

- **F-Droid:** repository metadata lives in [`fdroid/`](fdroid/) and will be submitted with the 2.0-indo release.
- **Direct APK:** download a signed release from the [GitHub Releases](../../releases) page and enable installation from unknown sources for your file manager/browser.

The application ID is `tech.alatcerdas.tvkillindo`. It is **different** from upstream `com.redirectapps.tvkill`, so both can be installed side by side. After verifying 2.0-indo, uninstall the old app yourself; settings are not migrated between package IDs.

## TV brand coverage

Upstream ships 16 brands (Samsung, Sony, LG, Panasonic, Philips, NEC, Sharp, JVC, Toshiba, Mitsubishi, Vizio, RCA, Pioneer, Hisense, Akai, AOC).

The 2.0-indo catalog adds TVs commonly found in the Indonesian market, including **Polytron, Changhong / CHiQ, TCL, Coocaa (Skyworth), Ikon, Hisense, Sharp, Akari, Konka, Haier, Xiaomi/Redmi, AQUA, Mito, SPC and Sansui**. Patterns are imported from attributed public databases (LIRC, IRDB, Tasmota code lists and manufacturer documentation); see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Imported codes may be marked *best-effort* until verified on real hardware. Wi-Fi control of smart TVs is planned for a later 2.x milestone, not 2.0-indo.

## Build from source

Prerequisites:

- JDK 21 LTS (or newer; Gradle 9.6+ runs on JDK 25 as well). Set `JAVA_HOME` accordingly.
- Android SDK Platform 36 and Build Tools 36.0.0 (`sdkmanager "platforms;android-36" "build-tools;36.0.0"`).
- Set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.

```sh
./gradlew clean assembleDebug          # debug APK
./gradlew clean assembleRelease        # unsigned release APK unless signing env vars are set
./gradlew testDebugUnitTest            # unit tests
./gradlew lintDebug                    # lint
```

Optional signing for release builds (used by CI/F-Droid metadata):

```sh
export SIGNING_STORE_FILE=/path/to/keystore.jks
export SIGNING_STORE_PASSWORD=...
export SIGNING_KEY_ALIAS=...
export SIGNING_KEY_PASSWORD=...
```

Builds are intended to be reproducible: pinned toolchain in [`gradle/libs.versions.toml`](gradle/libs.versions.toml), no network-dependent build steps, and no minification in the release build. CI verifies two clean builds produce the same APK bytes.

## Testing on a device

1. Install the debug APK on a phone with an IR blaster.
2. Grant the notification permission when prompted (Android 13+); without it the foreground-service notification is hidden.
3. Check: IR-blaster detection, universal OFF, a brand-specific OFF, repetitive start/stop, settings changes applied immediately, widget OFF/STOP.
4. Verify on both Android 7.x (LG V20) and a modern Android device (Vivo Y29 4G) before release.

## Contributing

Bug reports, brand/pattern contributions and translations are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/TRANSLATING.md](docs/TRANSLATING.md). Always include the source and license of any IR pattern you add.

## License and attribution

GPLv3 — see [LICENSE](LICENSE).

Original work: Copyright 2015–2018 Sebastian Kappes, Copyright 2018 Jonas Lochmann.
Fork and 2.0-indo changes: Copyright 2026 TVKILL-INDO contributors.

Third-party IR pattern data credits are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
