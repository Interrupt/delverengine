package com.interrupt.dungeoneer.game.terrain;

import com.noise.PerlinNoise;
import com.interrupt.dungeoneer.gfx.Material;

public class TerrainNoise {
    private transient PerlinNoise noise = null;
    public String comment = null;

    public int seed = 1;
    public double persistence = 1;
    public double frequency = 0.5;
    public double amplitude = 9;
    public int octaves = 1;
    public boolean filter = true;
    public Float threshold = null;

    public boolean abs = false;
    public Float min = null;
    public Float max = null;

    public Material texture = new Material("t1", (byte)27);

    public TerrainNoise() { }

    public TerrainNoise(int seed, double persistence, double frequency, double amplitude, int octaves, boolean filter) {
        this.seed = seed;
        this.persistence = persistence;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.octaves = octaves;
        this.filter = filter;
    }

    public float getHeightAt(float x, float y) {
        if(filter) {
            return getFilteredHeightAt(x, y);
        }
        else {
            return getBaseHeightAt(x, y);
        }
    }

    private float getFilteredHeightAt(float x, float y) {
        float xLookup = x;
        float yLookup = y;

        float height = 0;

        for(int xx = -1; xx <= 1; xx++) {
            for(int yy = -1; yy <= 1; yy++) {
                height += getBaseHeightAt(xLookup + (xx), yLookup + (yy)) / 8f;
            }
        }

        return height;
    }

    private float getBaseHeightAt(float x, float y) {
        if(noise == null) {
            noise = new PerlinNoise(seed, persistence, frequency, amplitude, octaves);
        }

        float height = (float)noise.getHeight(x * 0.1f + 400, y * 0.1f + 400);

        if(threshold != null) {
            if(height < threshold) height = 0;
        }

        // might need to muck with the values
        if(min != null) height = Math.min(height, min);
        if(max != null) height = Math.max(height, max);
        if(abs) height = Math.abs(height);

        return height;
    }
}
