# Golden apple conversion — local validation, 2026-09-11

Candidate: TotemEnchanting 0.1.12, Minecraft 26.2, Fabric API 0.154.2+26.2, TotemCore 0.7.18, Java 25.

- Build and all five dedicated-server GameTests passed.
- Golden apple regression covers power 53/54/64, third-option exclusivity, insufficient XP/lapis, exact one-item output, three-level/three-lapis cost, repeated packets, and stale bookshelf power.
- Production client probe `GoldenAppleVisualGameTest` passed: native `EnchantmentScreen` receives the 54-level offer; the real inventory-button packet yields an enchanted golden apple, empty lapis slot and XP level 61 from 64.
- Native screenshots: `screenshots/golden-apple/golden-apple-native-enchanting-54.png` and `golden-apple-native-enchanting-result.png`.
- Independent read-only review `/root/observer_publisher_review` passed server authority, costs, repeat rejection and native Observer menu compatibility. There is no new production Screen or provider protocol.
- English and Traditional Chinese manual instructions include the power range, third option and survival cost.

Commands used from TotemEnchanting with Java 25:

```sh
../TotemCore/gradlew build runGameTest --no-daemon
../TotemCore/gradlew -I /tmp/enchanting-apple-production.init.gradle runAppleProductionClientGameTest --no-daemon
```

Logs: `/tmp/enchanting-golden-apple-final.log`, `/tmp/enchanting-apple-production.log`.
JAR SHA-256: `0700b4f7b3947076f4417e6b6753909eb0e16b46b720ad32f5d3a24c444baecc`.
Validation preceded publication. Verified release details are recorded in `.github/staging/modrinth-published-0.1.12.json`.
