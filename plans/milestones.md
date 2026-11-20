# TVKILL-INDO — Milestone Plan to `2.0-indo`

**Status:** draft for approval  
**Repository:** `~/projects/TVKILL-INDO` (fork of `42SK/TVKILL`, currently byte-identical to upstream `b2117e2`, 2019-02-25)  
**Target release:** `2.0-indo` (`versionName = "2.0-indo"`)  
**Target application ID:** `tech.alatcerdas.tvkillindo`

---

## 1. Purpose of `2.0-indo`

Two headline goals, in priority order:

1. **Modern Android compatibility** — eliminate the "app was built for an older version of Android" install warning, restore a working build on current tooling, and make the app behave correctly on Android 7 → 16.
2. **Indonesian-market TV coverage** — become the go-to open-source "turn off the TV" app for TVs actually sold in Indonesia, with a much broader, source-attributed pattern catalog and an architecture that can later control smart TVs over Wi-Fi.

Non-goals for `2.0-indo`: Wi-Fi/network remote control, Play Store publication, full UI translation (see §4 and §9).

---

## 2. Baseline (what exists today)

Evidence from the current tree:

| Area | Current state |
|---|---|
| Build | Gradle 4.4 wrapper, AGP 3.1.2, Kotlin 1.2.41, `jcenter()`, `kotlin-android-extensions`, `kapt` data-binding compiler; `compileSdk 27`, `targetSdk 27`, `minSdk 19`, Java 8 |
| Build health | `./gradlew` fails on the installed JDK 25 (`Could not determine java version from '25.0.5'`); no CI, no tests except a removed-API `ApplicationTest` stub |
| Manifest | `TRANSMIT_IR`, `WAKE_LOCK`, `uses-feature android.hardware.consumerir`; no `android:exported`; no `POST_NOTIFICATIONS` |
| App | Java + a little Kotlin; `MainActivity` + 3 tabs, `TransmitService` foreground service, `Preferences`, 1 home-screen widget; XML layouts, Support Library 27.1.1, arch-lifecycle 1.1.1 |
| IR data | 16 brands hardcoded in `app/src/main/java/com/redirectapps/tvkill/BrandContainer.kt` (samsung, sony, lg, panasonic, philips, nec, sharp, jvc, toshiba, mitsubishi, vizio, rca, pioneer, hisense, akai, aoc); device-specific timing conversion in `Pattern.java` |
| Known fragile areas | device-specific IR conversion (`Pattern.java:68-105`), upstream issues #43 (Huawei IR), #46 (64 KB method limit when growing brand data), #52 (settings not applied instantly), #54 (Android 14 install warning/crash) |
| Localization | `values-{cs,de,es,fr,ru}` only; no `values-id/`, no Indonesian strings |
| Docs | `README.md` still describes upstream, links upstream Play/F-Droid listings and upstream issue tracker; no F-Droid build metadata in repo |
| Test hardware available | **LG V20 (Android 7.x, API 24/25)** and **Vivo Y29 4G** (modern Android, IR blaster) |

---

## 3. Research notes informing this plan

### 3.1 Indonesian TV market — brand coverage targets

Local buyer guides and market reports (Carisinyal 2026, IndexBox 2025, Pricebook tiering, Indonesia Smart TV Market report, retailer listings) consistently show these brands in Indonesian homes:

| Tier | Brands | Current app |
|---|---|---|
| Premium / global | Samsung, LG, Sony, Panasonic, Philips, Sharp, Toshiba | mostly present |
| Mainstream value / Chinese | **TCL**, **Hisense**, **Coocaa (Skyworth)**, **Changhong / CHiQ**, **Konka**, **Haier**, **Xiaomi/Redmi**, Skyworth, AOC | Hisense + AOC only |
| Local / Indonesia-specific | **Polytron**, **Ikon**, **Akari**, **Mito**, **SPC**, **AQUA**, Sansui, Kolin | none |
| Adjacent | DVB-T2 set-top boxes (very common after the analog shutdown), projectors | none |

TCL, Hisense, Coocaa and Polytron are repeatedly called out in the IDR 3–7 juta sweet spot (43–55"), i.e. exactly the households most likely to use this app. Sharp and Polytron also appear among the most-purchased brands in local surveys. `Hisense` and `Akai` already exist in the catalog but their patterns need re-validation against Indonesian models.

**Plan implication:** target the broadest practical list. Priority order for pattern work: Polytron, Changhong/CHiQ, TCL, Coocaa, Ikon, Hisense, Sharp, Akari, Konka, Haier, Xiaomi/Redmi, AQUA, Mito, SPC, Sansui, plus existing brands' Indonesian models.

### 3.2 Pattern data sources

- [LIRC remotes database](https://lirc-remotes.sourceforge.net/) and the [probonopd/lirc-remotes](https://github.com/probonopd/lirc-remotes) Pronto-Hex conversion.
- [Tasmota IR code list](https://tasmota.github.io/docs/Codes-for-IR-Remotes) — convenient NEC/RC5-style power codes.
- [Flipper Zero universal TV list](https://github.com/flipperdevices/flipperzero-firmware/blob/dev/applications/main/infrared/resources/infrared/assets/tv.ir) — GPL-3.0, license-compatible; used for the Coocaa (Skyworth) NEC patterns.
- [Hisense discrete IR commands PDF](https://assets.hisense-usa.com/assets/ProductDownloads/16/c995389f20/Hisense-Discrete-IR-Commands-for-copy-paste_0.pdf) — discrete ON/OFF (not just toggle), valuable for reliability.
- Upstream unmerged PRs #49/#50 (JSON pattern DB, TV-B-Gone NA/EU codes) are a design reference, not code to copy blindly.

**Plan implication:** move patterns out of Kotlin source into a versioned data file; each record carries source URL, license/attribution, carrier frequency, protocol/timings, confidence and target brand/model. A license audit is an explicit gate before bundling.

### 3.3 Platform constraints

- Google Play now requires target API 36 (Android 16) for updates (since 2026-08-31). We are **not** shipping on Play, but raising `targetSdk` is still the direct fix for the install warning and for Android 14+ crashes.
- Raising `targetSdk` forces: `android:exported` on manifest components, explicit `PendingIntent` mutability, notification channel + `POST_NOTIFICATIONS` handling, foreground-service type declarations, no background `startService`, edge-to-edge/WindowInsets handling on Android 15/16, and removal of deprecated `PreferenceActivity`/`ProgressDialog`/`android.app.Fragment`.
- `minSdk 24` keeps the LG V20 usable; it drops only API 19–23 devices. It also lets us delete the pre-Lollipop Samsung timing hacks in `Pattern.java`.
- Modern phones increasingly lack IR blasters; 2.0 keeps IR as the only transport but introduces the abstraction that Wi-Fi control will plug into in 2.x.

---

## 4. Decisions locked for `2.0-indo`

| # | Decision | Choice |
|---|---|---|
| D1 | Distribution | **F-Droid / direct APK only.** No Play Store submission, AAB, or Play policy work. Keep the build fully FOSS and offline-capable. |
| D2 | Transport scope | **IR first.** 2.0 hardens IR only but introduces a transport interface so Wi-Fi control can be added in 2.1+. |
| D3 | UI stack | **Kotlin + Jetpack Compose (Material 3).** Rewrite the three tabs, settings and status flows; remove Java/XML UI and `kotlin-android-extensions`. |
| D4 | Minimum SDK | **API 24 (Android 7.0)** — raised from 19 so the LG V20 remains supported; delete pre-Lollipop workarounds. |
| D5 | Target/compile SDK | **36** (Android 16), to remove install warnings and adopt current behavior. |
| D6 | Pattern catalog | **Broadest possible list**, bulk-imported from the sources in §3.2, with source attribution, confidence metadata and a community feedback path. Unverified codes are labeled best-effort. |
| D7 | App identity | **New application ID `tech.alatcerdas.tvkillindo`**; new AGP `namespace`, new signing identity, treated as a new app on F-Droid. No upgrade path from the upstream package. |
| D8 | Documentation | **README.md rewritten in English now.** Add localization *scaffolds* (`values-id/strings.xml` stub, translation-contribution doc) but no translated UI yet; translations come in later milestones. |
| D9 | Test devices | LG V20 (API 24/25) is the minimum-SDK reference device; Vivo Y29 4G is the modern-device reference. Both will be used for sign-off. |

---

## 5. Overall definition of done for `2.0-indo`

- [ ] `./gradlew clean assembleRelease` succeeds from a clean checkout on a pinned, documented JDK/Gradle/AGP combination; unsigned build is reproducible (identical bytes across two clean builds).
- [ ] `compileSdk 36`, `targetSdk 36`, `minSdk 24`, `versionName "2.0-indo"`, `applicationId tech.alatcerdas.tvkillindo`.
- [ ] Installs and runs on both reference devices **without** the "built for an older version of Android" warning; no crash on Android 14–16 emulators.
- [ ] All 16 legacy brands still work; the expanded Indonesian-priority catalog ships as external attributed data, not source code.
- [ ] All UI is Compose; no Support Library, no `kotlin-android-extensions`, no deprecation-warning blockers, no Google Play Services or other proprietary dependency.
- [ ] Unit tests for timing conversion, pattern parsing/ordering and catalog integrity; instrumented smoke tests on both reference devices.
- [ ] F-Droid build metadata and a signed, reproducible release APK artifact exist; release tag `2.0-indo`.
- [ ] `README.md` refreshed (English) and i18n scaffolds present.

---

## 6. Milestones

Effort key: **S** ≈ days, **M** ≈ 1–2 weeks, **L** ≈ multiple weeks. Order is sequential unless noted.

### M0 — Fork identity, repo hygiene and documentation scaffold (S)

**Goal:** make this repo clearly the Indonesian fork before touching code.

- Set `applicationId`/AGP `namespace` to `tech.alatcerdas.tvkillindo`; pick `versionCode` scheme (e.g. `20000`) and confirm `versionName "2.0-indo"`.
- Rewrite `README.md` (English) around the fork: purpose, new ID, install from F-Droid/APK, supported brands + data sources, build prerequisites, device requirements, known limitations (IR-only in 2.0), how to test on the two reference devices, how to contribute patterns with attribution, GPLv3 + third-party data credits, migration note from `com.redirectapps.tvkill`.
- Replace upstream-only links in `README.md`/`CONTRIBUTING.md` with fork links; keep upstream attribution and GPL notices.
- Add `.gitignore`/CI skeleton, branch protection and a `plans/` convention; add F-Droid metadata skeleton (`tech.alatcerdas.tvkillindo.yml`) with fields left to fill in M7.
- Add localization scaffolds only: empty `app/src/main/res/values-id/strings.xml` with translator comments and a `docs/TRANSLATING.md` stub. Do **not** translate strings yet.
- Remove stale `proguard-rules.pro` developer path and the obsolete generated `ApplicationTest`.

**Exit:** clean clone builds conceptually on paper (build fix is M1); README accurately describes the fork; identity strings consistent everywhere.

---

### M1 — Toolchain and SDK modernization (M) — *blocks everything else*

**Goal:** a buildable project on current tooling, with the SDK levels that remove the install warning.

- Replace Gradle wrapper with a current Gradle + AGP pair; migrate from Groovy `compileSdkVersion` to the `android { namespace, compileSdk, minSdk, targetSdk }` DSL.
- Upgrade Kotlin to a current stable release; remove `kotlin-android-extensions`, remove the `kapt` data-binding compiler, and drop `dataBinding` if no layout uses `<layout>`.
- Replace `jcenter()` with `mavenCentral()` + `google()`; pin all dependency versions.
- Pin a supported JDK (JDK 21 LTS recommended; AGP may not support the machine's JDK 25 — document and enforce via Gradle toolchains/CI) and prove `./gradlew` works from a clean checkout.
- Set `compileSdk 36`, `targetSdk 36`, `minSdk 24`; add Android SDK 36 to CI/dev setup docs.
- Migrate Support Library → AndroidX (appcompat, material, lifecycle, preference) and delete `cardview` if unused.
- Establish reproducible-build basics: no timestamps/network-dependent build inputs, pinned build-tools, deterministic signing flow, CI script that runs two clean builds and compares outputs.

**Exit:** `./gradlew clean assembleDebug` green on the pinned JDK; install warning gone; no `jcenter`, no removed Kotlin plugin, no Support Library.

---

### M2 — Platform-behavior migration (M)

**Goal:** make the app behave correctly under `targetSdk 36`.

- Manifest: add explicit `android:exported` to activities/receiver; declare foreground-service type(s); add `POST_NOTIFICATIONS` handling; remove obsolete `WAKE_LOCK` if the partial wake lock can be replaced by modern APIs.
- Replace `context.startService` with `startForegroundService` + correct service type; use `getForegroundService` PendingIntents with `FLAG_IMMUTABLE` where appropriate (widget and notification).
- Notification channel for all supported versions; runtime permission request with graceful in-app status when notifications are denied.
- Edge-to-edge: handle WindowInsets/status/nav bars in Compose; verify on Android 15/16 (target 36 makes edge-to-edge non-optional) and on API 24.
- Replace deprecated APIs: `PreferenceActivity` → Compose settings + DataStore/SharedPreferences; `ProgressDialog` → Compose progress UI; `android.app.Fragment` → Compose navigation.
- Remove pre-API-21 device-timing special cases in `Pattern.java` (minSdk 24); keep and test the remaining manufacturer-specific emission behavior.

**Exit:** smoke matrix passes on API 24 (LG V20), Vivo Y29 4G, and emulators for API 28/33/34/36; no crash on fresh install or on foreground-service start from widget.

---

### M3 — Kotlin/Compose architecture rewrite (L)

**Goal:** one clean, testable codebase with a seam for future transports.

- Define domain model: `Brand`, `IrPattern` (frequency, timings/protocol, source, confidence), `TransmissionMode` (single brand, universal, repetitive, mute), `TransmissionResult`.
- Define a `TvTransport` interface (e.g. `supports(): Boolean`, `send(pattern): Result`, `observeStatus()`) with the initial `ConsumerIrManagerTransport` implementation. Wi-Fi transport will implement the same interface in 2.1 — **do not implement it now**.
- Replace `TransmitService` with a modern foreground-service/worker design that keeps cancellation, progress, partial wake lock, widget updates and repetitive mode; keep behavior equivalent to today (including extra-pattern depth and delay settings).
- Build Compose UI: universal screen, brands screen (searchable/filterable list), repetitive screen, settings screen, no-IR/unsupported-hardware state, transmission progress and result feedback.
- Migrate settings to a typed repository with migration from the old preference keys (new package means no persisted data to migrate, but keep key names consistent).
- Home-screen widget: keep as `RemoteViews`/XML or move to Glance — record the choice; it must still toggle OFF/CANCEL and show state.
- Remove old Java classes, XML layouts and unused resources once feature parity is verified.

**Exit:** all legacy UI removed; feature parity with 1.3.2 verified on both reference devices; transport seam covered by unit tests.

---

### M4 — Pattern database v2 and import pipeline (M)

**Goal:** escape the 64 KB method limit and make bulk catalog growth safe.

- Move all patterns out of `BrandContainer.kt` into a versioned, app-bundled data file (JSON or CSV — decide in this milestone; must be parsed at runtime without network access).
- Schema per record at minimum: `brand`, `model/family (optional)`, `protocol`, `carrierHz`, `timings` (or code words), `function` (power_off / mute), `sourceUrl`, `license`, `confidence` (verified / community / imported), `addedOn`.
- Write an offline import/conversion tool for the §3.2 sources: LIRC, Tasmota NEC codes, Flipper Zero universal TV list, Hisense discrete-code PDF; convert Pronto/NEC representations into the app's timing format with unit tests against known vectors. IRDB is excluded (license rejected).
- Deduplicate and order patterns by reliability (best first, as the upstream wiki recommends); cap per-transmission count and keep universal-mode duration reasonable.
- **License/attribution gate:** review each source's terms, add `THIRD_PARTY_NOTICES`/`LICENSES` file, and drop any source whose terms conflict with GPLv3 redistribution.
- Preserve the existing 16 brands' patterns in the new format and verify no regressions against the LG V20/Vivo Y29 4G.

**Exit:** catalog loads from data file; 16 legacy brands pass regression; 100% of records have source + license + confidence; unit tests prove conversion and ordering.

---

### M5 — Indonesian brand expansion and feedback loop (M–L)

**Goal:** ship the broadest practical catalog for TVs sold in Indonesia.

- Add priority brands from §3.1: Polytron, Changhong/CHiQ, TCL, Coocaa, Ikon, Akari, Konka, Haier, Xiaomi/Redmi, AQUA, Mito, SPC, Sansui, Kolin; re-check Hisense, Sharp and other existing brands against Indonesian models.
- For each brand: import candidate codes, label confidence honestly, and test what can be tested on the two reference phones with whatever TVs/remote codes are available. Unverified codes are shipped as best-effort and visibly labeled in the UI.
- Add brand grouping/filtering in the brands screen (local, mainstream, premium, legacy) and make universal mode use the expanded catalog without becoming unusably slow.
- Add a no-internet feedback path: "did this work?" in-app prompt that creates a share/exportable report (brand, model, pattern ID, phone model/OS, result) users can send by email or issue tracker; document it in `CONTRIBUTING.md`.
- Document the pattern-contribution workflow fork-side, with attribution requirements and a template for new brands.

**Exit:** every §3.1 priority brand either present with ≥1 sourced pattern or explicitly listed as a gap with reason; feedback path works offline; README brand table matches the catalog.

---

### M6 — Quality, device validation and release hardening (M)

**Goal:** evidence that 2.0-indo works where it claims to.

- Unit tests: timing conversion, catalog parsing/validation, receiver/ordering logic, settings repository, transport selection.
- Instrumented tests on **LG V20 (API 24/25)** and **Vivo Y29 4G**: install, no-IR/IR detection, single-brand OFF, universal OFF, repetitive start/stop, mute, settings changes applied immediately (upstream issue #52), widget, foreground notification/STOP, permission denial paths.
- Emulator matrix for API 26, 33, 34, 35, 36 (edge-to-edge, foreground-service restrictions, no-IR graceful degradation).
- Performance checks: transmission duration, cancellation latency, battery/CPU for repetitive mode, APK size after catalog expansion.
- Accessibility and usability pass: large tap targets, clear progress/result states, Bahasa Indonesia-friendly brand names and layout lengths even though strings remain English.
- Fix all crash- and install-blocking lint issues; record accepted warnings.

**Exit:** all P0/P1 defects closed; signed-off test report referencing both physical phones.

---

### M7 — Release `2.0-indo` (S–M)

**Goal:** publishable F-Droid-ready artifact.

- Finalize `README.md` (English), `CONTRIBUTING.md`, `docs/TRANSLATING.md` scaffold and third-party notices; tag `2.0-indo`.
- Complete F-Droid metadata: application ID, current version code/name, source URL, license, build commands, API/SDK levels, `AllowedAPKSigningKeys` if shipping developer-signed APKs.
- Produce signed release APK twice from clean builds and verify byte-for-byte reproducibility; attach SHA-256 sums and release notes.
- Document install instructions for sideloading and the F-Droid submission, plus the "new package ID = install side-by-side with upstream, then uninstall old app" migration note.
- Publish the GitHub release and submit the F-Droid packaging request.

**Exit:** `2.0-indo` tag, reproducible signed APK, F-Droid metadata committed, release notes published.

---

## 7. Deferred to later milestones (2.x)

These are explicitly **out of scope** for 2.0-indo; the design work in M3/M4 keeps the door open.

- **2.1 — Wi-Fi transport:** Android TV/Google TV (TCL, Sony, Xiaomi, Polytron smart models), Hisense VIDAA, Samsung/LG protocols; discovery, permissions (`NEARBY_WIFI_DEVICES`, local-network access), pairing, security review, transport picker UI. Plug into the `TvTransport` interface.
- **2.2 — Localization:** fill `values-id/` and other locales, localized store/F-Droid descriptions and screenshots.
- **2.3 — Extensible hardware:** external USB/IR adapters (upstream PR #51), learn-IR-from-existing-remote flow, projectors/set-top boxes as first-class categories.
- Community-hosted pattern database with runtime updates (must stay FOSS/offline-friendly and reproducible — F-Droid builds cannot depend on a mutable remote dataset).

---

## 8. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Bulk-imported codes are inaccurate or toggles instead of discrete OFF | High — app appears broken on the exact brands we add | Confidence labels, source attribution, prefer discrete OFF codes (e.g. Hisense PDF), in-app "did it work?" feedback, prioritize real-device testing |
| Third-party database licenses conflict with GPLv3 redistribution | High — release blocked | License audit gate in M4; drop non-compatible sources; ship attribution file |
| `targetSdk 36` on Android 16 changes behavior unpredictably | High | Emulator matrix in M6, edge-to-edge from day one in Compose, staged milestone M2 |
| minSdk 24 excludes API 19–23 phones with IR | Medium — smaller audience, deliberate tradeoff | Decision D4 accepted to keep LG V20; document supported devices clearly |
| LG V20/Vivo Y29 4G timing conversion differs from legacy assumptions | Medium | Keep and test device-specific emission behavior; instrument tests on both phones; upstream #43 as a known example |
| Compose rewrite + pattern DB expansion together is a large change | High — schedule slip | Strict milestone exits; ship feature parity first (M3), then data (M4/M5); no Wi-Fi work in 2.0 |
| F-Droid reproducibility fails | Medium — publishing delayed | Pin toolchain, avoid network-dependent build steps, two-build byte comparison in M1/M7 |
| New package ID means no upgrade path | Medium — user confusion | Explicit README note, side-by-side install guidance, distinct app name/icon in M0/M7 |

---

## 9. Open decisions for later milestones (not blocking 2.0-indo)

- Catalog file format: JSON vs CSV vs generated Kotlin — decide in M4 based on APK size and parser testability.
- Widget implementation: classic `RemoteViews` vs Jetpack Glance — decide in M3.
- Foreground-service type for repetitive mode (`specialUse` vs `connectedDevice` vs restructuring) — decide in M2; F-Droid is more permissive than Play here, but pick the least surprising option.
- Signing model: F-Droid-signed vs developer-signed APK (`AllowedAPKSigningKeys`) — decide in M7.
- Whether to keep MUTE as an experimental feature in the new UI — decide in M3.
- Which local brands get priority for real-hardware verification first, based on TVs the community can provide — revisit during M5.

---

## 10. Implementation notes (M0/M1 scaffolds)

Added while building the scaffolds; these do not change the locked decisions above.

### 10.0 Hard rule: never build unsigned APKs

**Do not produce unsigned APK artifacts. Ever.** `app/build.gradle` only configures release signing when `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD` are all set; without them `assembleRelease` silently emits `app-release-unsigned.apk`, which is a useless artifact and must not be created.

- Routine verification is `./gradlew testDebugUnitTest assembleDebug lintDebug --max-workers=1`. Release tasks are **not** part of routine verification.
- `assembleRelease` (or any task producing release artifacts) may only run when all four signing environment variables are present, so the output is a signed APK.
- If an unsigned release APK is produced by mistake, delete `app/build/outputs/apk/release/` immediately and report it; never distribute or commit it.
- `assembleDebug` is allowed: the debug keystore signs it automatically.

- **Verified toolchain:** Gradle 9.6.0 wrapper, AGP 9.5.1 (built-in Kotlin 2.2.10), Compose compiler plugin 2.2.10, compileSdk/targetSdk 36, build-tools 36.0.0, minSdk 24. Local verification used JDK 25; CI is pinned to JDK 21 LTS.
- **Dependency ceiling:** the newest AndroidX/Compose artifacts (core-ktx 1.19.1, lifecycle 2.11.0, navigation-compose 2.10.x, Compose BOM 2026.09.00) declare `minCompileSdk 37`. Platform 37 is not yet published as a stable SDK package, so the catalog pins the newest versions compatible with compileSdk 36: core-ktx 1.18.0, lifecycle 2.10.0, navigation-compose 2.9.1, Compose BOM 2026.01.00, activity-compose 1.13.0, appcompat 1.8.0, Material 1.14.0. Revisit when platform 37 ships (2.x).
- **AGP 9 built-in Kotlin:** the `kotlin-android` plugin is gone; Kotlin is compiled by AGP. The Compose compiler plugin is kept at the same version as AGP's bundled Kotlin to avoid classpath conflicts.
- **Verified on the scaffold:** `clean assembleDebug testDebugUnitTest lintDebug` all pass on both JDK 21 (the CI pin) and JDK 25. The generated manifest reports package `tech.alatcerdas.tvkillindo`, minSdk 24, targetSdk 36. (Release reproducibility was confirmed historically with two clean builds on the **same JDK**; see §10.0 — such builds require proper signing config and are not part of routine verification.)
- **JDK is part of the reproducibility contract:** builds on JDK 21 and JDK 25 are each internally reproducible but differ from each other (`classes3.dex` and `assets/dexopt/baseline.prof` differ). CI and F-Droid must therefore keep one pinned JDK for all comparison builds; do not compare APKs produced by different JDKs.
- **Expected remaining warnings:** the legacy UI still uses `PreferenceManager`, `android.app.Fragment`, `ProgressDialog` and `getSerializableExtra`; these are intentional until M2/M3 replaces them. The two app-widget layouts suppress `UseAppTint` because `RemoteViews` cannot inflate AppCompat widgets.

### 10.1 M3 progress notes

- **Compose shell screens:** `ComposeShellActivity` hosts the universal, brands, repetitive and settings screens with bottom navigation. `UniversalScreen`, `BrandListScreen`, `RepetitiveScreen` and `SettingsScreen` each own a dedicated controller (`UniversalTransmissionController`, `BrandTransmissionController`, `RepetitiveTransmissionController`, `SettingsController`) that is unit-testable without Android.
- **Launcher flipped, legacy UI removed (M3 slice):** `ComposeShellActivity` is now the `MAIN`/`LAUNCHER` entry point. Deleted the legacy tab UI entirely: `MainActivity`, the three fragments (`IndividualremoteFragment`, `UniversalmodeFragment`, `RepetitiveModeFragment`), `CustomAdapter`, the `Preferences` activity, their XML layouts (`activity_main`, `individualremote`, `universal`, `repetitive_mode`, `list_item`, `progress_dialog`), `res/xml/preferences.xml`, and the interim per-screen `BrandListActivity`/`UniversalActivity` hosts (superseded by the shell).
- **Mute parity preserved:** the legacy mute behavior is reproduced in Compose, gated by the `show_mute` preference (default off). Universal tab sends one MUTE pattern per brand via `UniversalSequence.muteSequence`; the brands tab shows a per-brand MUTE action via `BrandTransmissionController.transmitMute` and `selectMutePattern`; the repetitive tab keeps no mute (parity with legacy). Pattern selection was generalized to `selectPattern(brand, function)` with `selectPowerOffPattern`/`selectMutePattern` wrappers. All new paths covered by unit tests (controllers, sequence, selection).
- **Legacy runtime retained:** `TransmitService` (with the removed `MainActivity.progressDialog` hook), `BrandContainer`, legacy `Brand`/`Pattern` and the power-off widget stay until M4+ replaces the pattern data pipeline and adds a Compose-native background path.
- **Verification:** `./gradlew testDebugUnitTest assembleDebug lintDebug --max-workers=1` green after the flip.
