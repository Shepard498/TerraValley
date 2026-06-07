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

package com.terraforged.mod.platform.forge;

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.data.gen.TerraForgedDataProvider;
import com.terraforged.mod.lifecycle.CommonSetup;
import com.terraforged.mod.lifecycle.DataGenSetup;
import com.terraforged.mod.lifecycle.Stage;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TFData extends Stage {
    public static final TFData STAGE = new TFData();
    private IEventBus eventBus;

    public boolean run(IEventBus eventBus) {
        this.eventBus = eventBus;
        return run();
    }

    @Override
    protected void doInit() {
        eventBus.addListener(this::onGenerateData);

        var register = DeferredRegister.create(Registries.BIOME, TerraForged.MODID);
        register.register(eventBus);

        DataGenSetup.STAGE.run();

        for (var entry : CommonAPI.get().getRegistryManager().getRegistry(TerraForged.BIOMES)) {
            register.register(entry.getKey().location().getPath(), entry::getValue);
        }
    }

    void onGenerateData(GatherDataEvent event) {
        CommonSetup.STAGE.run();

        var generator = event.getGenerator().getVanillaPack(event.includeServer());

        generator.addProvider(output -> new TerraForgedDataProvider(output.getOutputFolder().resolve("resources/default")));
    }
}
