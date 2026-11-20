# Contributing to TVKILL-INDO

Thanks for helping keep this open-source TV power-off app alive on modern Android and useful in Indonesia.

Please read [plans/milestones.md](plans/milestones.md) first to see what is in scope for `2.0-indo`. Wi-Fi control, full translations and smart-TV protocols are deferred to later milestones.

## Issues

Use the fork's issue tracker at <https://github.com/brrrassica/TVKILL-INDO/issues>.

- Search for duplicates first.
- Use a descriptive title.
- For IR failures, include: TV brand and model, approximate year, phone model and Android version, which app mode you used, and whether the physical remote works.
- Do not report Play Store policy issues here; this fork is F-Droid/sideload only.

## Pull requests

- One feature or fix per PR.
- Open an issue first for large changes (Compose rewrite, catalog format, new transport).
- Keep commits small with imperative messages.
- Run `./gradlew clean assembleDebug testDebugUnitTest lintDebug` before opening a PR.
- Prefer tests for changes to pattern parsing, timing conversion or settings.

## Adding IR patterns

Pattern data is moving out of hardcoded Kotlin into a versioned, attributed catalog (milestone M4). Until that lands:

1. Find the power-off pattern and carrier frequency using a learning IR receiver or a documented database.
2. Include the **source URL** and its **license** in the code comment or catalog entry. Check [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) first.
3. Prefer discrete `POWER OFF` codes over toggles.
4. Put more reliable patterns earlier in a brand's list.
5. State the TV model you verified against, or clearly mark the pattern as unverified/best-effort.
6. If you cannot test on hardware, still open an issue with the source — someone with the TV may be able to confirm it.

Useful public sources: [LIRC remotes database](https://lirc-remotes.sourceforge.net/), [Tasmota IR codes](https://tasmota.github.io/docs/Codes-for-IR-Remotes), the [Flipper Zero universal TV list](https://github.com/flipperdevices/flipperzero-firmware/blob/dev/applications/main/infrared/resources/infrared/assets/tv.ir) (GPL-3.0, compatible), and manufacturer IR documentation such as the [Hisense discrete command list](https://assets.hisense-usa.com/assets/ProductDownloads/16/c995389f20/Hisense-Discrete-IR-Commands-for-copy-paste_0.pdf). Check each source's redistribution terms before bundling. Note: IRDB (probonopd/irdb) is deliberately excluded — do not import from it.

## Translations

UI strings are still English-only in 2.0-indo, but the resource scaffold exists. See [docs/TRANSLATING.md](docs/TRANSLATING.md). Please do not machine-translate strings; open a PR with a native-speaker review.

## Code style

- Kotlin: official code style (`kotlin.code.style=official`).
- Java: 4-space indentation, keep existing GPL headers.
- No Google Play Services, analytics, ads or proprietary SDKs.

By contributing you agree that your changes are licensed under GPLv3, the same license as the project.
