package com.terraforged.engine.world.terrain;

import java.util.Objects;

public class Terrain {
    private final int id;
    private final String name;
    private final Terrain delegate;
    private final boolean river;
    private final boolean lake;
    private final boolean overground;

    Terrain(int id, String name, Terrain delegate, boolean river, boolean lake, boolean overground) {
        this.id = id;
        this.name = name;
        this.delegate = delegate;
        this.river = river;
        this.lake = lake;
        this.overground = overground;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Terrain getDelegate() {
        return delegate;
    }

    public boolean isRiver() {
        return river || (delegate != null && delegate.isRiver());
    }

    public boolean isLake() {
        return lake || (delegate != null && delegate.isLake());
    }

    public boolean isOverground() {
        return overground || (delegate != null && delegate.isOverground());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Terrain terrain && name.equals(terrain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
