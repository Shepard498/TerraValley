package com.terraforged.engine.world.climate;

import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class Moisture implements Module {
    private final Module source;
    private final float falloff;

    public Moisture(int seed, int scale, float falloff) {
        this.source = Source.simplex(seed, Math.max(1, scale), 4).map(0, 1);
        this.falloff = Math.max(0.01F, falloff);
    }

    @Override
    public float getValue(int seed, float x, float y) {
        float value = source.getValue(seed, x, y);
        return clamp((float) Math.pow(value, falloff));
    }

    @Override
    public float minValue() {
        return 0F;
    }

    @Override
    public float maxValue() {
        return 1F;
    }

    private static float clamp(float value) {
        return value < 0F ? 0F : value > 1F ? 1F : value;
    }
}
