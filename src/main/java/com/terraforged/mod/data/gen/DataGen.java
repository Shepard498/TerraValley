/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.data.gen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.data.codec.Codecs;
import com.terraforged.mod.data.util.JsonFormatter;
import com.terraforged.mod.registry.DataRegistry;
import com.terraforged.mod.util.FileUtil;
import com.terraforged.mod.util.TagLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataGen {
    private final CachedOutput cache;
    private final List<CompletableFuture<?>> tasks = new ObjectArrayList<>();

    public DataGen(CachedOutput cache) {
        this.cache = cache;
    }

    protected CompletableFuture<?> doExport(Path dir) {
        FileUtil.delete(dir);

        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var writeOps = RegistryOps.create(JsonOps.INSTANCE, registries);

        TagLoader.bindTags(registries);

        genPreset(dir, registries, writeOps);
        genBuiltin(dir, registries, writeOps);
        genDimensionType(dir, registries, writeOps);

        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    public static CompletableFuture<?> export(Path dir, CachedOutput cache) {
        return new DataGen(cache).doExport(dir);
    }

    private void genPreset(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var json = new JsonObject();
        var dimensions = new JsonObject();
        dimensions.add("minecraft:overworld", createOverworldPreset(writeOps));
        dimensions.add("minecraft:the_nether", createNetherPreset());
        dimensions.add("minecraft:the_end", createEndPreset());
        json.add("dimensions", dimensions);
        export(dir, Registries.WORLD_PRESET, TerraForged.WORLD_PRESET, json);
        export(dir, Registries.WORLD_PRESET, ResourceLocation.withDefaultNamespace("normal"), json.deepCopy());
    }

    private void genDimensionType(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var json = new JsonObject();
        json.addProperty("effects", TerraForged.DIMENSION_EFFECTS.toString());
        json.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
        json.addProperty("ambient_light", 0);
        json.addProperty("bed_works", true);
        json.addProperty("coordinate_scale", 1);
        json.addProperty("has_ceiling", false);
        json.addProperty("has_raids", true);
        json.addProperty("has_skylight", true);
        json.addProperty("height", 1024);
        json.addProperty("logical_height", 1024);
        json.addProperty("min_y", -64);
        json.addProperty("monster_spawn_block_light_limit", 0);
        json.addProperty("natural", true);
        json.addProperty("piglin_safe", false);
        json.addProperty("respawn_anchor_works", false);
        json.addProperty("ultrawarm", false);

        var light = new JsonObject();
        light.addProperty("type", "minecraft:uniform");

        var lightValue = new JsonObject();
        lightValue.addProperty("max_inclusive", 7);
        lightValue.addProperty("min_inclusive", 0);
        light.add("value", lightValue);
        json.add("monster_spawn_light_level", light);

        export(dir, Registries.DIMENSION_TYPE, ResourceLocation.withDefaultNamespace("overworld"), json);
    }

    private void genBuiltin(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        for (var registry : CommonAPI.get().getRegistryManager().getRegistries()) {
            export(dir, registry, registries, writeOps);
        }
    }

    private void genTags(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {

    }

    private <T> void export(Path dir, DataRegistry<T> builtin, RegistryAccess access, DynamicOps<JsonElement> ops) {
        var registry = builtin.key().get();

        TerraForged.LOG.info("Exporting registry: {}", registry);
        for (var entry : builtin) {
            try {
                var json = builtin.codec().encodeStart(ops, entry.getValue())
                        .mapError(s -> {
                            logError(s);
                            return s;
                        })
                        .result()
                        .orElseThrow();

                export(dir, registry, entry.getKey().location(), json);
            } catch (Throwable t) {
                new EncodingException(entry.getKey(), t).printStackTrace();
            }
        }
    }

    private JsonObject createOverworldPreset(DynamicOps<JsonElement> ops) {
        var dimension = new JsonObject();
        dimension.addProperty("type", "minecraft:overworld");

        var generator = new JsonObject();
        generator.addProperty("type", TerraForged.location("generator").toString());
        generator.add("levels", Codecs.encode(TerrainLevels.DEFAULT.get(), TerrainLevels.CODEC, ops));
        dimension.add("generator", generator);

        return dimension;
    }

    private JsonObject createNetherPreset() {
        var dimension = new JsonObject();
        dimension.addProperty("type", "minecraft:the_nether");

        var biomeSource = new JsonObject();
        biomeSource.addProperty("preset", "minecraft:nether");
        biomeSource.addProperty("type", "minecraft:multi_noise");

        var generator = new JsonObject();
        generator.addProperty("settings", "minecraft:nether");
        generator.addProperty("type", "minecraft:noise");
        generator.add("biome_source", biomeSource);
        dimension.add("generator", generator);

        return dimension;
    }

    private JsonObject createEndPreset() {
        var dimension = new JsonObject();
        dimension.addProperty("type", "minecraft:the_end");

        var biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:the_end");

        var generator = new JsonObject();
        generator.addProperty("settings", "minecraft:end");
        generator.addProperty("type", "minecraft:noise");
        generator.add("biome_source", biomeSource);
        dimension.add("generator", generator);

        return dimension;
    }

    private void export(Path dir, ResourceKey<?> registry, ResourceLocation name, JsonElement json) {
        var file = dir.resolve("data")
                .resolve(name.getNamespace())
                .resolve(registry.location().getPath())
                .resolve(name.getPath() + ".json");

        if (cache != null) {
            writeCached(file, json);
        } else {
            tasks.add(CompletableFuture.runAsync(() -> writeDirect(file, json)));
        }
    }

    protected void writeDirect(Path path, JsonElement json) {
        var parent = path.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (var out = Files.newBufferedWriter(path)) {
            JsonFormatter.format(json, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    protected void writeCached(Path path, JsonElement json) {
        try {
            var byteOut = new ByteArrayOutputStream();
            var hashOut = new HashingOutputStream(Hashing.sha1(), byteOut);

            JsonFormatter.format(json, hashOut);

            cache.writeIfNeeded(path, byteOut.toByteArray(), hashOut.hash());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String logError(String s) {
        TerraForged.LOG.warn(s);
        return s;
    }
}
