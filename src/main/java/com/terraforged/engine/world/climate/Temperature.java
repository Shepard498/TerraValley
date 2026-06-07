package com.terraforged.engine.world.climate;

import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class Temperature implements Module {
    private final float frequency;
    private final float falloff;
    private final Module variation;

    public Temperature(float frequency, float falloff) {
        this.frequency = frequency;
        this.falloff = Math.max(0.01F, falloff);
        this.variation = Source.simplex(15689, Math.max(1, Math.round(1F / Math.max(0.001F, frequency))), 3).map(0, 1);
    }

    @Override
    public float getValue(int seed, float x, float y) {
        float latitude = 1F - Math.min(1F, Math.abs((float) Math.sin(y * frequency * 0.5F)));
        float noise = variation.getValue(seed, x, y);
        return clamp((float) Math.pow(latitude * 0.7F + noise * 0.3F, falloff));
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
