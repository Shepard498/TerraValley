package com.terraforged.engine.world;

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.Settings;

public class GeneratorContext {
    public final Settings settings;
    public final Seed seed;

    public GeneratorContext(Settings settings) {
        this.settings = settings;
        this.seed = new Seed(settings.world.seed);
    }
}
