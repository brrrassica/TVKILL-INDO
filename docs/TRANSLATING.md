# Translating TVKILL-INDO (scaffold)

UI translations are **not part of 2.0-indo**. This document and `app/src/main/res/values-id/strings.xml` are scaffolds so contributors can start work without blocking the release.

## What exists today

- `app/src/main/res/values/strings.xml` — the canonical English strings.
- `values-cs`, `values-de`, `values-es`, `values-fr`, `values-ru` — upstream translations (may be stale after the 2.0 rewrite).
- `values-id/strings.xml` — placeholder for Bahasa Indonesia. It currently contains only a comment; untranslated keys fall back to English.

## Adding a locale

1. Copy `values/strings.xml` to `values-<locale>/strings.xml`.
2. Translate only the `text` of each `<string>`; keep names, `translatable="false"` entries, formatting arguments (`%1$s`) and XML escaping intact.
3. Do not machine-translate without a native-speaker review.
4. Run `./gradlew lintDebug` and check `MissingTranslation` is still intentionally disabled for 2.0-indo.
5. Open a PR with the locale in the title, e.g. `Add Bahasa Indonesia strings`.

## Later milestones

- 2.2: fill `values-id/`, refresh other locales, add localized F-Droid descriptions and screenshots.
- When the Compose UI lands, Compose string resources keep using the same `res/values` files, so translations carry forward.
