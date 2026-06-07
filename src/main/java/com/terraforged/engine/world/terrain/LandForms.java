package com.terraforged.engine.world.terrain;

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.TerrainSettings;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;

public class LandForms {
    private final TerrainSettings settings;
    private final Levels levels;
    private final Module ground;

    public LandForms(TerrainSettings settings, Levels levels, Module ground) {
        this.settings = settings;
        this.levels = levels;
        this.ground = ground;
    }

    public Module steppe(Seed seed) {
        return rolling(seed, 360, 3, 0.18);
    }

    public Module plains(Seed seed) {
        return rolling(seed, 440, 3, 0.12);
    }

    public Module hills1(Seed seed) {
        return rolling(seed, 300, 4, 0.36);
    }

    public Module hills2(Seed seed) {
        return rolling(seed, 260, 4, 0.46).max(detail(seed, 95, 2, 0.08));
    }

    public Module dales(Seed seed) {
        Module valleys = Source.simplex(seed.next(), 340, 4).abs().invert().map(0, 1);
        return rolling(seed, 320, 3, 0.35).mult(valleys.alpha(0.65));
    }

    public Module plateau(Seed seed) {
        Module shelf = Source.simplex(seed.next(), 420, 4).map(0, 1).terrace(0.45, 0.75, 0.2, 4, 0.2);
        return shelf.scale(vertical(0.5)).add(detail(seed, 90, 2, 0.05));
    }

    public Module badlands(Seed seed) {
        Module ridges = Source.simplexRidge(seed.next(), 240, 4).map(0, 1).steps(8, 0.1, 0.35);
        return ridges.scale(vertical(0.5)).warp(seed.next(), 130, 3, 36);
    }

    public Module torridonian(Seed seed) {
        Module base = plateau(seed).scale(0.65);
        Module ridges = Source.simplexRidge(seed.next(), 520, 5).map(0, 1).scale(vertical(0.4));
        return base.max(ridges).warp(seed.next(), 480, 3, 160);
    }

    public Module mountains(Seed seed) {
        return mountain(seed, 580, 5, 0.78);
    }

    public Module mountains2(Seed seed) {
        return mountain(seed, 460, 5, settings.general.fancyMountains ? 0.92 : 0.74);
    }

    public Module mountains3(Seed seed) {
        return mountain(seed, 380, 6, settings.general.fancyMountains ? 1.0 : 0.78);
    }

    private Module mountain(Seed seed, int scale, int octaves, double strength) {
        Module peaks = Source.build(seed.next(), scale, octaves)
                .lacunarity(2.5)
                .gain(0.55)
                .simplexRidge()
                .map(0, 1)
                .pow(1.35)
                .scale(vertical(strength));

        Module slopes = Source.simplex(seed.next(), scale / 2, 4)
                .map(0, 1)
                .scale(vertical(strength * 0.35));

        return peaks.max(slopes)
                .warp(Domain.warp(Source.SIMPLEX, seed.next(), Math.max(40, scale / 8), 3, scale * 0.12));
    }

    private Module rolling(Seed seed, int scale, int octaves, double strength) {
        return Source.simplex(seed.next(), scale, octaves)
                .map(0, 1)
                .scale(vertical(strength))
                .add(detail(seed, Math.max(45, scale / 4), 2, strength * 0.12))
                .max(ground.scale(0.05));
    }

    private Module detail(Seed seed, int scale, int octaves, double strength) {
        return Source.simplex(seed.next(), scale, octaves)
                .map(0, 1)
                .scale(vertical(strength));
    }

    private double vertical(double value) {
        return value * settings.general.globalVerticalScale * (levels.worldHeight / 255D);
    }
}
