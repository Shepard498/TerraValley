package com.terraforged.engine.world.heightmap;

public class Levels {
    public final int waterLevel;
    public final int worldHeight;
    public final float scale;

    public Levels(int waterLevel, int worldHeight) {
        this.waterLevel = waterLevel;
        this.worldHeight = worldHeight;
        this.scale = 1F / Math.max(1, worldHeight - waterLevel);
    }
}
