package com.terraforged.engine.settings;

import com.terraforged.noise.Module;

public class Settings {
    public final WorldSettings world = new WorldSettings();
    public final Climate climate = new Climate();

    public static class Climate {
        public final BiomeShape biomeShape = new BiomeShape();
        public final ClimateModule temperature = new ClimateModule();
        public final ClimateModule moisture = new ClimateModule();
    }

    public static class BiomeShape {
        public int biomeSize = 220;
        public int biomeWarpScale = 400;
        public float biomeWarpStrength = 150F;
    }

    public static class ClimateModule {
        public int seedOffset = 0;
        public float scale = 1F;
        public float falloff = 1F;
        public float bias = 0F;

        public Module apply(Module module) {
            Module result = module;
            if (falloff != 1F) {
                result = result.pow(falloff);
            }
            if (scale != 1F) {
                result = result.scale(scale);
            }
            if (bias != 0F) {
                result = result.bias(bias);
            }
            return result.clamp(0, 1);
        }
    }
}
