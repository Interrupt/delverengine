package com.interrupt.dungeoneer.game.terrain;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Prefab;

import java.util.Random;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.serializers.v2.ThreadSafeLevelSerializer;

public class OverworldTerrainGenerator {
    Array<TerrainNoise> noiseFunctions;
    Array<FoliageGenerator> foliageGenerators;
    Material baseTexture = new Material("t1", (byte)27);

    public OverworldTerrainGenerator() {
        noiseFunctions = new Array<TerrainNoise>();
        foliageGenerators = new Array<FoliageGenerator>();

        // default terrain generator, big hills and small bumps
        TerrainNoise hillsPerlin = new TerrainNoise(1, 1f, 0.5f, 9f, 2, true);
        TerrainNoise detailperlin = new TerrainNoise(1, 1f, 6f, 0.5f, 1, true);

        // add some forest generators
        Array<Entity> deciduous = new Array<Entity>();
        deciduous.add(new Prefab("Trees", "Tree1"));
        deciduous.add(new Prefab("Breakables", "Bush"));

        Array<Entity> pines = new Array<Entity>();
        pines.add(new Prefab("Trees", "Pine1"));
        pines.add(new Prefab("Breakables", "Bush"));

        foliageGenerators.add(new FoliageGenerator(deciduous, 1, 1, 6, 1));
        foliageGenerators.add(new FoliageGenerator(pines, 10, 0.15f, 2f, 1.5f));

        noiseFunctions.add(detailperlin);
        noiseFunctions.add(hillsPerlin);
    }

    public float getHeightAt(float x, float y, Material outMaterial) {
        float height = 0;

        TerrainNoise maxNoise = null;
        float maxHeight = 0;

        for(int i = 0; i < noiseFunctions.size; i++) {
            float noiseHeight = noiseFunctions.get(i).getHeightAt(x, y);
            height += noiseHeight;

            // keep track of who is applying the most height to the ground for texturing
            if(outMaterial != null) {
                float noiseHeightAbs = Math.abs(noiseHeight);
                if (noiseHeightAbs > maxHeight) {
                    maxHeight = noiseHeightAbs;
                    maxNoise = noiseFunctions.get(i);
                }
            }
        }

        // send out a texture, if asked for one
        if(outMaterial != null) {
            if(maxNoise != null && maxNoise.texture != null) {
                outMaterial.texAtlas = maxNoise.texture.texAtlas;
                outMaterial.tex = maxNoise.texture.tex;
            }
            else {
                outMaterial.texAtlas = baseTexture.texAtlas;
                outMaterial.tex = baseTexture.tex;
            }
        }

        return height;
    }

    public Entity findEntityFor(float x, float y, float z, Random r, ThreadSafeLevelSerializer serializer) {
        Entity found = null;
        for(int i = 0; i < foliageGenerators.size && found == null; i++) {
            found = foliageGenerators.get(i).getEntityFor(x, y, z, r, serializer);
        }

        // TODO: Maybe generate IDs to make sure there are no duplicates?
        // found.id = Integer.toString(r.nextInt());

        return found;
    }
}