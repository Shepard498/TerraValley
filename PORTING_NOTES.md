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
- `./gradlew compileJava --stacktrace` now succeeds under JDK 21.
- `./gradlew jar --stacktrace` builds `build/libs/TerraForged-1.21.1-0.4.0-alpha-1.jar`.
- `./gradlew build --stacktrace` succeeds. The old visual/manual test classes compile, and Gradle is configured not to fail when no automated tests are discovered.
- `./gradlew runData --stacktrace` now starts NeoForge datagen and completes successfully.
- The TerraForged default datapack is now exposed through NeoForge's pack finder as an always-active built-in server datapack.

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
- Obsolete registry/datapack hooks and client world-creation mixins are excluded from source compilation while the NeoForge lifecycle replacement is rebuilt.
- `ChunkGenerator` and `BiomeSource` implementations have been moved to the 1.21 `MapCodec` and async generation method contracts.
- Biome-source and chunk-generator codecs are now registered through NeoForge `DeferredRegister` on the mod event bus instead of late direct registration during datagen.
- Datagen has a compile-first 1.21 provider path that writes world preset/dimension JSON explicitly and exports TerraForged's in-memory data registries directly.
- The generated default pack now also overrides `minecraft:normal` so normal world creation can resolve to the TerraForged overworld generator without the removed create-world UI mixins.
- Tag loading, light queuing, world-dimensions detection, and `RandomState` seed access currently use compile-first placeholders.

Current blockers:

- Runtime behavior is not ready: world seed propagation is temporarily hardcoded for paths that only receive `RandomState` or `ChunkGeneratorStructureState`.
- The excluded hooks/mixins need proper NeoForge 1.21 replacements before calling this a usable mod.
- Tag/resource pack loading needs updating for the 1.21 pack APIs.
- Custom biome bootstrap and climate JSON/defaults need a proper 1.21 dynamic-registry pass.
- The datagen provider should eventually be replaced with a proper 1.21 `RegistrySetBuilder` / `DatapackBuiltinEntriesProvider` flow.

Possible next paths:

1. Recover or recreate the missing Engine/Noise/Cereal libraries and include them locally.
2. Vendor MIT-compatible source from a modern continuation, preserving notices, then adapt old TerraForged code to it.
3. Treat ReTerraForged as the practical modern base and port TerraValley features/identity onto that architecture instead of trying to compile the archived 1.19 code directly.
