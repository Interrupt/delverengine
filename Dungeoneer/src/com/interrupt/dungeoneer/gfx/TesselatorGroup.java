package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Triangle;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.dungeoneer.partitioning.TriangleSpatialHash;

public class TesselatorGroup {
    private ArrayMap<String, Tesselator> tesselators = new ArrayMap<String, Tesselator>();
    private BoundingBox bounds = new BoundingBox();
    private ShaderInfo shader;

    public TesselatorGroup(ShaderInfo shader) {
        this.shader = shader;
    }

    public boolean needsTesselation() {
        if(tesselators.size == 0)
            return true;
        
        for(int i = 0; i < tesselators.size; i++) {
            Tesselator tesselator = tesselators.getValueAt(i);
            if(!tesselator.HasBuilt())
                return true;
        }

        return false;
    }

    public boolean isEmpty() {
        boolean isEmpty = true;
        for(int i = 0; i < tesselators.size; i++) {
            if(!tesselators.getValueAt(i).empty())
                isEmpty = false;
        }
        return isEmpty;
    }

    public void clear() {
        for(int i = 0; i < tesselators.size; i++) {
            tesselators.getValueAt(i).clear();
        }
    }

    public void render() {
        for(int i = 0; i < tesselators.size; i++) {
            TextureAtlas.bindRepeatingTextureAtlasByIndex(tesselators.getKeyAt(i));
            tesselators.getValueAt(i).renderMesh(shader);
        }
    }

    public void build() {
        for(int i = 0; i < tesselators.size; i++) {
            tesselators.getValueAt(i).build();
        }
    }

    public void refresh() {
        for(int i = 0; i < tesselators.size; i++) {
            tesselators.getValueAt(i).refresh();
        }
    }

    public BoundingBox calculateBoundingBox() {
        bounds = new BoundingBox();

        for(int i = 0; i < tesselators.size; i++) {
            BoundingBox b = tesselators.getValueAt(i).calculateBoundingBox();
            if(b != null) {
                bounds.min.y = Math.min(bounds.min.y, b.min.y);
                bounds.max.y = Math.max(bounds.max.y, b.max.y);
            }
        }

        return bounds;
    }

    public BoundingBox getBoundingBox() {
        return bounds;
    }

    public Tesselator getTesselatorByAtlas(String atlas) {
        if(!tesselators.containsKey(atlas)) createTesselator(atlas);
        return tesselators.get(atlas);
    }

    public Tesselator createTesselator(String atlas) {
        Tesselator t = new Tesselator();
        tesselators.put(atlas, t);
        return t;
    }

    public void addCollisionTriangles(TriangleSpatialHash triangleSpatialHash) {
        for(int ti = 0; ti < tesselators.size; ti++) {
            Tesselator tesselator = tesselators.getValueAt(ti);
            for (int i = 0; i < tesselator.collisionTriangles.size - 2; i += 3) {
                triangleSpatialHash.AddTriangle(
                        new Triangle(tesselator.collisionTriangles.get(i),
                                tesselator.collisionTriangles.get(i + 1),
                                tesselator.collisionTriangles.get(i + 2)));
            }
        }
    }
}
