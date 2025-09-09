package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Triangle;
import com.interrupt.dungeoneer.collision.CollisionTriangle;
import com.interrupt.dungeoneer.gfx.decals.DDecal;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.dungeoneer.partitioning.TriangleSpatialHash;

public class TesselatorGroup {
    private ArrayMap<String, Tesselator> tesselators = new ArrayMap<String, Tesselator>();
    private BoundingBox bounds = new BoundingBox();
    private ShaderInfo defaultShader;

    public TesselatorGroup(ShaderInfo defaultShader) {
        this.defaultShader = defaultShader;
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
        if(tesselators.size == 0)
            return;

        ShaderInfo lastShader = null;
        TextureAtlas lastAtlas = null;

        for(int i = 0; i < tesselators.size; i++) {
            ShaderInfo shader = null;

            // Bind the atlas for drawing
            TextureAtlas atlas = TextureAtlas.bindRepeatingTextureAtlasByIndex(tesselators.getKeyAt(i));
            if(atlas != null)
                shader = atlas.getShader();

            // If there's no custom shader set, use the default one
            if(shader == null)
                shader = defaultShader;

            if(shader != lastShader || atlas != lastAtlas) {
                // End the last shader before starting the new one
                if(lastShader != null)
                    lastShader.end();

                shader.begin();

                // Set some shader properties based on the atlas
                if(atlas != null) {
                    Texture tex = atlas.texture;
                    if (tex != null) {
                        shader.setAttribute("u_tex_width", (float) atlas.spriteSize / tex.getWidth());
                        shader.setAttribute("u_tex_height", 1f / tex.getHeight());
                    }

                    shader.setAttribute("u_sprite_columns", atlas.columns);
                    shader.setAttribute("u_sprite_rows", atlas.rows);
                }
            }

            tesselators.getValueAt(i).renderMesh(shader);
            lastShader = shader;
            lastAtlas = atlas;
        }

        // Finish any shaders before continuing
        if(lastShader != null)
            lastShader.end();
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
        addCollisionTriangles(triangleSpatialHash, CollisionTriangle.TriangleCollisionType.WORLD);
    }

    public void addCollisionTriangles(TriangleSpatialHash triangleSpatialHash, CollisionTriangle.TriangleCollisionType collisionType) {
        for(int ti = 0; ti < tesselators.size; ti++) {
            Tesselator tesselator = tesselators.getValueAt(ti);
            for (int i = 0; i < tesselator.collisionTriangles.size - 2; i += 3) {
                triangleSpatialHash.AddTriangle(
                        new CollisionTriangle(tesselator.collisionTriangles.get(i),
                                tesselator.collisionTriangles.get(i + 1),
                                tesselator.collisionTriangles.get(i + 2),
                                collisionType));
            }
        }
    }
}
