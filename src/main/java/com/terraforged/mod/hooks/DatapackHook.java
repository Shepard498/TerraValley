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

package com.terraforged.mod.hooks;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.nio.file.Path;
import java.util.function.Predicate;

public class DatapackHook {
    private static final String PACK_FILE_ID = "file/" + DataPackExporter.PACK_FILE_NAME;
    private static final DirectoryValidator ALLOW_ALL_VALIDATOR = new DirectoryValidator(path -> true);

    public static boolean injectDatapack(PackRepository repository, Path dir) {
        var changed = false;

        if (!repository.isAvailable(PACK_FILE_ID)) {
            DataPackExporter.createWorldDatapack(dir);
            repository.addPackFinder(new FolderRepositorySource(dir, PackType.SERVER_DATA, PackSource.WORLD, ALLOW_ALL_VALIDATOR));
            repository.reload();

            TerraForged.LOG.info("Injected datapack {}", PACK_FILE_ID);
            changed = true;
        }

        if (repository.addPack(PACK_FILE_ID)) {
            TerraForged.LOG.info("Selected datapack {}", PACK_FILE_ID);
            changed = true;
        }

        return changed;
    }

    public static void selectPreset(Object object) {
        if (object instanceof CreateWorldScreen screen) {
            for (var listener : screen.children()) {
                if (listener instanceof CycleButton<?> button && isPreset(button)) {

                    // Only log if the preset changed from a non-TF to the TF preset
                    if (selectPreset(button, id -> id.getNamespace().equals(TerraForged.MODID))) {
                        TerraForged.LOG.info("Selected terraforged world_preset");
                    }

                    return;
                }
            }
        }
    }

    private static boolean selectPreset(CycleButton<?> button, Predicate<ResourceLocation> predicate) {
        final var key = getKey(button);
        
        // Return early if preset already selected
        if (predicate.test(key.location())) {
            return false;
        }

        // Loop through options and test
        var next = nextKey(button);
        while (next != key) {

            if (predicate.test(next.location())) {
                return true;
            }

            next = nextKey(button);
        }

        return false;
    }

    private static ResourceKey<?> getKey(CycleButton<?> button) {
        return ((Holder<?>) button.getValue()).unwrapKey().orElseThrow();
    }

    private static ResourceKey<?> nextKey(CycleButton<?> button) {
        button.onPress();
        return getKey(button);
    }

    private static boolean isPreset(CycleButton<?> button) {
        return button.getValue() instanceof Holder<?> holder && holder.unwrap().map(
                key -> key.registry().equals(Registries.WORLD_PRESET.location()),
                value -> value instanceof WorldPreset
        );
    }
}
