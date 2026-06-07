package com.terraforged.engine.world.heightmap;

import com.terraforged.engine.settings.WorldSettings;

public class ControlPoints {
    public float deepOcean;
    public float shallowOcean;
    public float beach;
    public float coast;
    public float inland;

    public ControlPoints() {
        this(new WorldSettings.ControlPoints());
    }

    public ControlPoints(WorldSettings.ControlPoints controlPoints) {
        this.deepOcean = controlPoints.deepOcean;
        this.shallowOcean = controlPoints.shallowOcean;
        this.beach = controlPoints.beach;
        this.coast = controlPoints.coast;
        this.inland = controlPoints.inland;
    }
}
