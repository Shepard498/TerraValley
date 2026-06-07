package com.terraforged.engine.world.biome.type;

public enum BiomeType {
    TROPICAL_RAINFOREST(0x2D7D32),
    SAVANNA(0xB9A84A),
    DESERT(0xD8C05F),
    TEMPERATE_RAINFOREST(0x3D7C59),
    TEMPERATE_FOREST(0x4F8F46),
    GRASSLAND(0x88A84B),
    COLD_STEPPE(0x8EA58C),
    STEPPE(0xA7A96A),
    TAIGA(0x54745B),
    TUNDRA(0xB7C5BE),
    ALPINE(0xAEB8B3);

    private final int color;

    BiomeType(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public static BiomeType get(float temperature, float moisture) {
        temperature = clamp(temperature);
        moisture = clamp(moisture);

        if (temperature < 0.16F) {
            return moisture < 0.45F ? TUNDRA : TAIGA;
        }
        if (temperature < 0.32F) {
            return moisture < 0.35F ? COLD_STEPPE : TAIGA;
        }
        if (temperature > 0.82F) {
            if (moisture < 0.25F) return DESERT;
            if (moisture < 0.55F) return SAVANNA;
            return TROPICAL_RAINFOREST;
        }
        if (temperature > 0.62F) {
            if (moisture < 0.25F) return STEPPE;
            if (moisture < 0.55F) return GRASSLAND;
            return TEMPERATE_RAINFOREST;
        }
        if (moisture < 0.28F) return STEPPE;
        if (moisture > 0.72F) return TEMPERATE_RAINFOREST;
        return TEMPERATE_FOREST;
    }

    private static float clamp(float value) {
        return value < 0F ? 0F : value > 1F ? 1F : value;
    }
}
