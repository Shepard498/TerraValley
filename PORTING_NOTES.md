# TerraValley Porting Notes

## 2026-06-07 Build Spike

The repository has been moved from the archived TerraForged ForgeGradle 1.19 setup to a NeoForge 1.21.1 Gradle baseline.

Current baseline:

- Minecraft: `1.21.1`
- NeoForge: `21.1.231`
- Gradle wrapper: `9.2.1`
- Java toolchain: `21`
- NeoGradle userdev: `7.1.36`
- Active branch: `neoforge/1.21.1-port`

What works:

- `./gradlew tasks` configures successfully under JDK 21.
- NeoForge run tasks are available: `runClient`, `runServer`, and `runData`.

Current compile blocker:

- The archived TerraForged branch depended on `com.terraforged:Engine:0.3.0` from `https://io.terraforged.com/maven/`.
- That host no longer resolves, so the old dependency cannot be fetched.
- The old `.gitmodules` mentions `https://github.com/TerraForged/Engine.git`, but the current `0.3.x` tree does not contain a registered `Engine` submodule path.
- Historical TerraForged commit `704a1ad9e51d4007789b18c2bc64eed5628d794f` referenced Engine submodule commit `df2e6e0be9d31b8bc80288a5002c0d0683c0839a`, but the public `TerraForged/Engine` repository is currently unavailable.

Observed first compile categories after removing the dead Maven dependency:

- Missing old TerraForged libraries:
  - `com.terraforged.engine.*`
  - `com.terraforged.noise.*`
  - `com.terraforged.cereal.*`
- Minecraft 1.21.1 API changes, for example `net.minecraft.network.chat.contents.LiteralContents`.
- Forge-to-NeoForge package migration still needed in `Forge/main/java`.

Possible next paths:

1. Recover or recreate the missing Engine/Noise/Cereal libraries and include them locally.
2. Vendor MIT-compatible source from a modern continuation, preserving notices, then adapt old TerraForged code to it.
3. Treat ReTerraForged as the practical modern base and port TerraValley features/identity onto that architecture instead of trying to compile the archived 1.19 code directly.
