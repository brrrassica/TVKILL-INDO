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
| probonopd/irdb | <https://github.com/probonopd/irdb> | custom irdb license — <https://github.com/probonopd/irdb/blob/master/LICENSE.md> | Must comply before bundling |
| Tasmota IR codes | <https://tasmota.github.io/docs/Codes-for-IR-Remotes> | to verify | NEC/RC5-style power codes |
| Hisense discrete IR commands | <https://assets.hisense-usa.com/assets/ProductDownloads/16/c995389f20/Hisense-Discrete-IR-Commands-for-copy-paste_0.pdf> | manufacturer documentation | Discrete ON/OFF codes |

No records from these sources are bundled yet in 2.0-indo. M4 will record the
exact source URL, license, confidence and model for every pattern in the
catalog and remove any source whose terms conflict with GPLv3.
