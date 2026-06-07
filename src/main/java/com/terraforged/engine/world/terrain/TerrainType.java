package com.terraforged.engine.world.terrain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TerrainType {
    private static final Map<String, Terrain> TYPES = new LinkedHashMap<>();

    public static final Terrain NONE = create("none", null, false, false, false);
    public static final Terrain DEEP_OCEAN = create("deep_ocean", NONE, false, false, false);
    public static final Terrain SHALLOW_OCEAN = create("shallow_ocean", NONE, false, false, false);
    public static final Terrain COAST = create("coast", NONE, false, false, false);
    public static final Terrain RIVER = create("river", NONE, true, false, false);
    public static final Terrain LAKE = create("lake", NONE, false, true, false);
    public static final Terrain FLATS = create("flats", NONE, false, false, true);
    public static final Terrain HILLS = create("hills", FLATS, false, false, true);
    public static final Terrain PLATEAU = create("plateau", HILLS, false, false, true);
    public static final Terrain BADLANDS = create("badlands", HILLS, false, false, true);
    public static final Terrain MOUNTAINS = create("mountains", HILLS, false, false, true);

    private TerrainType() {
    }

    public static Terrain get(String name) {
        return TYPES.getOrDefault(name, NONE);
    }

    public static Terrain getOrCreate(String name, Terrain delegate) {
        Terrain terrain = TYPES.get(name);
        if (terrain == null) {
            terrain = create(name, delegate, false, false, delegate != null && delegate.isOverground());
        }
        return terrain;
    }

    public static Collection<Terrain> values() {
        return TYPES.values();
    }

    public static void forEach(Consumer<Terrain> consumer) {
        TYPES.values().forEach(consumer);
    }

    private static Terrain create(String name, Terrain delegate, boolean river, boolean lake, boolean overground) {
        Terrain terrain = new Terrain(TYPES.size(), name, delegate, river, lake, overground);
        TYPES.put(name, terrain);
        return terrain;
    }
}
