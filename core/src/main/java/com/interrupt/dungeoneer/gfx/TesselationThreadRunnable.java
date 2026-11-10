package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.OverworldChunk;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TesselationThreadRunnable implements Runnable {

    WorldChunk wc = null;
    Level level = null;
    GlRenderer renderer = null;

    Array<Vector3> staticMeshCollisionTriangles = new Array<Vector3>();

    boolean makeFloors = true;
    boolean makeCeilings = true;
    boolean makeWalls = true;

    final Tesselator t = new Tesselator();

    @Override
    public void run() {
        // Worker thread: pure CPU vertex generation. No GL calls here - the GL
        // context only exists on the render thread (issue #2).
        final TesselatorGroups groups = new TesselatorGroups();
        t.Tesselate(level, renderer, wc, wc.xOffset, wc.yOffset, wc.width, wc.height, groups, makeFloors, makeCeilings, makeWalls, true, false);

        final Array<Entity> chunkEntities = (wc.overworldChunk != null) ? wc.overworldChunk.static_entities : wc.entities;

        // sort the mesh entities into buckets, determined by their texture (CPU, reads DrawableMesh metadata only)
        final HashMap<String, Array<Entity>> meshesByTexture = new HashMap<String, Array<Entity>>();
        for(Entity e : chunkEntities) {
            if(e.drawable != null && e.drawable instanceof DrawableMesh) {
                DrawableMesh drbl = (DrawableMesh)e.drawable;
                if(drbl.isStaticMesh) {
                    if(!meshesByTexture.containsKey(drbl.textureFile))  {
                        meshesByTexture.put(drbl.textureFile, new Array<Entity>());
                    }
                    meshesByTexture.get(drbl.textureFile).add(e);
                }
            }
        }

        // base bounds from the chunk footprint (CPU, no mesh)
        final BoundingBox boundingBox = new BoundingBox();
        boundingBox.min.x = wc.xOffset;
        boundingBox.max.x = wc.xOffset + wc.width;

        boundingBox.min.z = wc.yOffset;
        boundingBox.max.z = wc.yOffset + wc.height;

        // mesh building, merging, bounds and publish all need the GL context,
        // so they run on the render thread.
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                groups.build();   // GL: pool obtain + setVertices + setIndices

                final List<Vector3> tempCollisionTriangles = new ArrayList<Vector3>();
                final ArrayMap<String,Array<Mesh>> newStaticMeshBatch = new ArrayMap<String, Array<Mesh>>();

                for(String key : meshesByTexture.keySet()) {
                    Array<Mesh> m = GlRenderer.mergeStaticMeshes(level, meshesByTexture.get(key), tempCollisionTriangles);
                    if(m != null) {
                        newStaticMeshBatch.put(key, m);

                        for(Mesh mesh : m) {
                            BoundingBox meshBounds = mesh.calculateBoundingBox();
                            boundingBox.min.x = Math.min(boundingBox.min.x, meshBounds.min.x);
                            boundingBox.min.y = Math.min(boundingBox.min.y, meshBounds.min.y);
                            boundingBox.min.z = Math.min(boundingBox.min.z, meshBounds.min.z);
                            boundingBox.max.x = Math.max(boundingBox.max.x, meshBounds.max.x);
                            boundingBox.max.y = Math.max(boundingBox.max.y, meshBounds.max.y);
                            boundingBox.max.z = Math.max(boundingBox.max.z, meshBounds.max.z);
                        }
                    }
                }

                // tesselator bounds read the built meshes, so they are GL too
                Array<BoundingBox> calcedBounds = new Array<BoundingBox>();
                calcedBounds.add(groups.world.calculateBoundingBox());
                calcedBounds.add(groups.water.calculateBoundingBox());
                calcedBounds.add(groups.waterfall.calculateBoundingBox());

                for(BoundingBox b : calcedBounds)
                {
                    if(b != null) {
                        boundingBox.min.y = Math.min(boundingBox.min.y, b.min.y);
                        boundingBox.max.y = Math.max(boundingBox.max.y, b.max.y);
                    }
                }

                // reverse the collision triangles list!
                staticMeshCollisionTriangles.clear();
                for(int i = tempCollisionTriangles.size() - 1; i >= 0; i--) {
                    staticMeshCollisionTriangles.add(tempCollisionTriangles.get(i));
                }

                // cleanup, don't need the mesh buckets anymore
                meshesByTexture.clear();

                // publish on the render thread
                wc.entities = chunkEntities;
                wc.hasBuilt = true;
                wc.tesselators = groups;
                wc.position.set(wc.xOffset + (wc.width / 2f), 0, wc.yOffset + (wc.height / 2f));
                wc.bounds = boundingBox;
                wc.staticMeshBatch = newStaticMeshBatch;

                if(!renderer.chunks.contains(wc, true)) {
                    renderer.chunks.add(wc);
                }
            }
        });
    }

    public void init(OverworldChunk oc, Level level, GlRenderer renderer) {
        this.wc = oc.getWorldChunk();
        this.wc.overworldChunk = oc;
        this.level = level;
        this.renderer = renderer;
    }
}
