# Architecture (2.0-indo scaffold)

This document describes the target structure. During M0/M1 the legacy Java/XML UI still exists under `app/src/main/java/tech/alatcerdas/tvkillindo/`; the new packages below are being introduced alongside it and become authoritative when the Compose UI lands in M3.

## Packages

| Package | Responsibility | Status |
|---|---|---|
| `tech.alatcerdas.tvkillindo` | Legacy activities, service, settings, widget (1.3.2 behavior) | Present, being modernized |
| `domain` | `Brand`, `IrPattern`, `TransmissionMode`, `TransmissionResult` | Scaffold |
| `data` | Pattern catalog repository, settings repository, source/license metadata | Scaffold |
| `transport` | `TvTransport` interface + `ConsumerIrTvTransport` implementation | Scaffold |
| `ui` | Compose screens, ViewModels, navigation, theme | M3 in progress: brands + universal screens landed, legacy tabs still the launcher |

## Key rules

1. **UI never talks to `ConsumerIrManager` directly.** It calls a `TransmissionController` that resolves a `TvTransport`.
2. **`TvTransport` is the seam for 2.1 Wi-Fi control.** Android TV/Google TV/VIDAA transports will implement the same interface; no UI changes should be needed beyond a picker.
3. **Pattern data is data, not code.** Patterns live in a versioned, attributed catalog parsed at runtime. This avoids the upstream 64 KB method limit when expanding Indonesian brands. Every entry has `sourceUrl`, `license`, `confidence` and, where possible, `model`.
4. **No network dependency at build or run time** (2.0-indo). F-Droid builds must be reproducible and the app must work offline.
5. **No proprietary dependencies**: no Google Play Services, no ads, no analytics.

## Compose screens landed so far (M3)

| Screen | Entry point | Logic layer | Tests |
|---|---|---|---|
| Brands | `ui/brandlist/BrandListActivity` | `BrandTransmissionController` + `selectPowerOffPattern` | `BrandTransmissionControllerTest`, `BrandListStateTest` |
| Universal | `ui/universal/UniversalActivity` | `UniversalTransmissionController` + `UniversalSequence` | `UniversalTransmissionControllerTest`, `UniversalSequenceTest` |

Neither activity is the launcher yet: `MainActivity` keeps the legacy tab UI
until feature parity is verified. Universal mode sends only `POWER_OFF`
patterns, one per brand per round, with the "additional patterns" switch
adding deeper rounds — byte-identical pattern data to the legacy service.

## Current transmission flow (legacy, for reference)

`MainActivity` → `TransmitService` (foreground service, progress LiveData, cancellation) → `Brand`/`Pattern` → `Transmitter` → `ConsumerIrManager.transmit(frequency, timings)`.

The 2.0 flow will be: Compose screen → ViewModel → `TransmissionController` → `TvTransport` → `ConsumerIrManager`, with the same cancellation/progress/widget behavior preserved.
