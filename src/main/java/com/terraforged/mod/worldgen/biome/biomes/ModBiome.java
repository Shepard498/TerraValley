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

package com.terraforged.mod.worldgen.biome.biomes;

import com.terraforged.mod.TerraForged;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModBiome(ResourceKey<Biome> key, Supplier<Biome> factory) {
    public Biome create() {
        return factory.get();
    }

    public static ModBiome of(String name, ResourceKey<Biome> parent, Consumer<Biome.BiomeBuilder> modifier) {
        var key = ResourceKey.create(Registries.BIOME, TerraForged.location(name));
        var factory = copyFactory(parent, modifier);
        return new ModBiome(key, factory);
    }

    private static Supplier<Biome> copyFactory(ResourceKey<Biome> parent, Consumer<Biome.BiomeBuilder> modifier) {
        return () -> {
            var builder = builderOf(parent);
            modifier.accept(builder);
            return builder.build();
        };
    }

    private static Biome.BiomeBuilder builderOf(ResourceKey<Biome> parent) {
        // TODO 1.21: copy the parent biome from dynamic registry bootstrap context.
        var builder = new Biome.BiomeBuilder();
        builder.downfall(0.4F);
        builder.temperature(0.8F);
        builder.hasPrecipitation(true);
        builder.mobSpawnSettings(MobSpawnSettings.EMPTY);
        builder.specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(7907327)
                .build());
        builder.generationSettings(BiomeGenerationSettings.EMPTY);
        return builder;
    }

    public static Biome create(ResourceKey<Biome> parent, Consumer<Biome.BiomeBuilder> modifier) {
        var builder = builderOf(parent);
        modifier.accept(builder);
        return builder.build();
    }
}
