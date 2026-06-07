package com.terraforged.engine.settings;

public class WorldSettings {
    public int seed = 0;
    public final Properties properties = new Properties();
    public final Continent continent = new Continent();
    public final ControlPoints controlPoints = new ControlPoints();

    public static class Properties {
        public int seaLevel = 63;
        public int worldHeight = 255;
    }

    public static class Continent {
        public int continentScale = 400;
    }

    public static class ControlPoints {
        public float deepOcean = 0.05F;
        public float shallowOcean = 0.30F;
        public float beach = 0.45F;
        public float coast = 0.75F;
        public float inland = 0.80F;
    }
}
