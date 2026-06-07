# TerraValley Porting Notes

## 2026-06-07 Build Spike

The repository has been moved from the archived TerraForged ForgeGradle 1.19 setup to a NeoForge 1.21.1 Gradle baseline.

Current baseline:

- Minecraft: `1.21.1`
- NeoForge: `21.1.231`
- Gradle wrapper: `9.2.1`
- Java toolchain: `21`
- NeoGradle userdev: `7.1.36`
- Mixin annotation processing is temporarily disabled because the old `org.spongepowered:mixin:0.8.5:processor` crashes while parsing modern descriptor selectors under the current toolchain. Runtime mixin/refmap handling still needs a proper NeoForge-compatible pass.
- Active branch: `neoforge/1.21.1-port`

What works:

- `./gradlew tasks` configures successfully under JDK 21.
- NeoForge run tasks are available: `runClient`, `runServer`, and `runData`.

Resolved blocker:

- The archived TerraForged branch depended on `com.terraforged:Engine:0.3.0` from `https://io.terraforged.com/maven/`.
- That host no longer resolves, so the old dependency cannot be fetched.
- The old `.gitmodules` mentions `https://github.com/TerraForged/Engine.git`, but the current `0.3.x` tree does not contain a registered `Engine` submodule path.
- Historical TerraForged commit `704a1ad9e51d4007789b18c2bc64eed5628d794f` referenced Engine submodule commit `df2e6e0be9d31b8bc80288a5002c0d0683c0839a`, but the public `TerraForged/Engine` repository is currently unavailable.
- An initial source compatibility layer now exists under `src/main/java/com/terraforged/engine` to replace the old Engine dependency surface used by this codebase. It is intentionally incomplete and should be treated as a porting bridge, not a recovered full Engine implementation.

Recovered local dependencies:

- `libs/Noise2D` is added as a submodule from `https://github.com/TerraForged/Noise2D.git`.
- `libs/Cereal` is added as a submodule from `https://github.com/TerraForged/Cereal.git`.
- Their Java sources are compiled directly by the root project to avoid relying on JitPack or the unavailable TerraForged Maven host.

Observed first compile categories after removing the dead Maven dependency:

- Minecraft 1.21.1 API changes, including registry keys, `ResourceLocation` factories, `DataResult.error` suppliers, `Holder` methods, and command feedback suppliers.
- Forge-to-NeoForge package migration in `Forge/main/java`.
- Mixin targets and refmap generation still need to be reviewed after source compilation is restored.

Checkpoint progress:

- `com.terraforged.engine.*` compile imports have been replaced by local compatibility sources.
- `Forge/main/java` platform entrypoint imports have been moved to NeoForge/FML packages and now use the injected mod event bus.
- Datagen provider return type has been updated to the 1.21 `CompletableFuture<?>` contract.
- Several low-risk 1.21 API substitutions are applied: `ResourceLocation.parse/fromNamespaceAndPath`, `Registries.*`, `BuiltInRegistries.*` codec registration, supplier-based `sendSuccess`, and `RegistrationInfo.BUILT_IN`.
- Custom biome bootstrap and climate defaults currently contain compile-first placeholders. They need a proper 1.21 dynamic-registry/bootstrap rewrite.

Current compile blockers:

- Old registry/datapack hooks depend on removed types such as `RegistryAccess.Writable`, `RegistryLoader`, and `RegistryResourceAccess`.
- Client world-creation mixins target removed/renamed screens such as `WorldGenSettingsComponent`.
- `ChunkGenerator` and `BiomeSource` contracts changed to `MapCodec` and new async generation methods.
- Seed access via `RandomState.legacyLevelSeed()` is gone and needs a new seed source.
- Tag/resource pack loading needs updating for the 1.21 pack APIs.

Possible next paths:

1. Recover or recreate the missing Engine/Noise/Cereal libraries and include them locally.
2. Vendor MIT-compatible source from a modern continuation, preserving notices, then adapt old TerraForged code to it.
3. Treat ReTerraForged as the practical modern base and port TerraValley features/identity onto that architecture instead of trying to compile the archived 1.19 code directly.
