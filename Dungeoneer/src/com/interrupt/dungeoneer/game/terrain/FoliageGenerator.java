package com.interrupt.dungeoneer.game.terrain;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.serializers.v2.ThreadSafeLevelSerializer;
import com.interrupt.managers.EntityManager;

import java.util.Random;

public class FoliageGenerator {
    TerrainNoise placementNoise = new TerrainNoise(3, 1f, 3f, 6f, 2, false);

    public boolean invert = false;
    public float threshold = 1f;
    public float minElevation = -100;
    public float maxElevation = 100;
    public float spawnPercentage = 0.1f;

    public Array<Entity> entities = new Array<Entity>();

    public FoliageGenerator() { }

    public FoliageGenerator(Array<Entity> entities, int seed, float frequency, float amplitude, float threshold) {
        placementNoise.seed = seed;
        placementNoise.frequency = frequency;
        placementNoise.amplitude = amplitude;
        this.threshold = threshold;
        if(entities != null) {
            this.entities.addAll(entities);
        }
    }

    public float getSpawnChanceAtFiltered(float x, float y, float z) {
        float val = 0;

        for(int xx = -1; xx <= 1; xx++) {
            for(int yy = -1; yy <= 1; yy++) {
                val += getSpawnChanceAt(x, y, z) / 8f;
            }
        }

        return val;
    }

    public float getSpawnChanceAt(float x, float y, float z) {
        if(z > maxElevation || z < minElevation) return 0;
        float val = placementNoise.getHeightAt(x, y);

        // TODO: Filter this?
        if(val + (placementNoise.amplitude / 2) < threshold) return 0;
        else return 1;
    }

    public boolean canSpawnAt(float x, float y, float z, Random r) {
        float spawnChance = getSpawnChanceAt(x, y , z);
        if(invert) spawnChance *= -1;
        return r.nextFloat() < spawnChance * spawnPercentage;
    }

    public Entity getEntityFor(float x, float y, float z, Random r, ThreadSafeLevelSerializer serializer) {
        if(entities.size > 0 && canSpawnAt(x, y, z, r)) {
            Entity e = entities.get(r.nextInt(entities.size));

            if(e instanceof Prefab) {
                e = EntityManager.instance.getEntityWithSerializer(((Prefab) e).category, ((Prefab) e).name, serializer);
            }

            if(e != null) {
                Entity copy = (Entity)serializer.copyObject(e);
                copy.x = x + 0.5f;
                copy.y = y + 0.5f;
                copy.z = z;

                //copy.x += Game.rand.nextFloat() * 0.1f;
                //copy.y += Game.rand.nextFloat() * 0.1f;

                return copy;
            }
        }
        return null;
    }
}
