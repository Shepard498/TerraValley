package com.terraforged.engine.settings;

public class TerrainSettings {
    public final General general = new General();

    public static class General {
        public float globalVerticalScale = 1F;
        public boolean fancyMountains = true;
    }
}
