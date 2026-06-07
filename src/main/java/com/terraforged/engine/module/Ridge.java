package com.terraforged.engine.module;

import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public class Ridge implements Module {
    private final Module source;

    public Ridge(Module source) {
        this.source = source;
    }

    @Override
    public String getSpecName() {
        return "Ridge";
    }

    @Override
    public float getValue(int seed, float x, float y) {
        float min = source.minValue();
        float max = source.maxValue();
        float range = max - min;
        float value = range == 0F ? 0F : (source.getValue(seed, x, y) - min) / range;
        return 1F - Math.abs(value * 2F - 1F);
    }

    @Override
    public float minValue() {
        return 0F;
    }

    @Override
    public float maxValue() {
        return 1F;
    }

    public static DataSpec<Ridge> spec() {
        return DataSpec.builder("Ridge", Ridge.class, Ridge::deserialize)
                .addObj("source", Module.class, ridge -> ridge.source)
                .build();
    }

    private static Ridge deserialize(DataObject data, DataSpec<Ridge> spec, Context context) {
        Module source = data.get("source").isNonNull() ? spec.get("source", data, Module.class, context) : Source.ZERO;
        return new Ridge(source);
    }
}
