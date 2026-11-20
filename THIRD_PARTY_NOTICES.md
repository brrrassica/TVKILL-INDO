# Third-party notices

TVKILL-INDO is GPLv3. Any bundled IR pattern data, artwork or code from third
parties must be compatible with GPLv3 redistribution and credited here.

## Upstream application

- **TV KILL** — Copyright 2015–2018 Sebastian Kappes, Copyright 2018 Jonas Lochmann.
  Licensed GPLv3. <https://github.com/42SK/TVKILL>

## IR pattern sources (license audit pending in milestone M4)

| Source | URL | License status | Notes |
|---|---|---|---|
| LIRC remotes database | <https://lirc-remotes.sourceforge.net/> | to verify | Converted Pronto Hex data |
| probonopd/lirc-remotes | <https://github.com/probonopd/lirc-remotes> | to verify | Pronto Hex + protocol conversions |
| Tasmota IR codes | <https://tasmota.github.io/docs/Codes-for-IR-Remotes> | to verify | NEC/RC5-style power codes |
| Flipper Zero firmware universal TV list | <https://github.com/flipperdevices/flipperzero-firmware/blob/dev/applications/main/infrared/resources/infrared/assets/tv.ir> | GPL-3.0 (compatible) | Source of the project-generated Coocaa (Skyworth) NEC patterns |
| Hisense discrete IR commands | <https://assets.hisense-usa.com/assets/ProductDownloads/16/c995389f20/Hisense-Discrete-IR-Commands-for-copy-paste_0.pdf> | manufacturer documentation | Discrete ON/OFF codes |

IRDB (probonopd/irdb) was evaluated and deliberately **excluded**: its custom
license terms are not acceptable for this project, and no data from it is
bundled or referenced.

No records from these sources are bundled yet in 2.0-indo. M4 will record the
exact source URL, license, confidence and model for every pattern in the
catalog and remove any source whose terms conflict with GPLv3.
