package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

public class Block extends DirectionalEntity {
    @EditorProperty
    public float width = 0.5f;

    @EditorProperty
    public float height = 1.0f;

    @EditorProperty
    public float depth = 0.5f;

    /** Path to texture file for Model. */
    @EditorProperty(group = "Visual", type = "FILE_PICKER", params = "")
    public String textureFile = "meshes.png";

    /** Can decals project onto this Entity? */
    @EditorProperty(group = "Visual")
    public boolean receiveDecals = true;

    private transient Mesh mesh;

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        drawable = null;
        updateDrawable();
        if (drawable == null) return;

        if (source != Level.Source.EDITOR) {
            if (drawable instanceof DrawableMesh) {
                DrawableMesh drawableMesh = (DrawableMesh) drawable;

                if (drawableMesh.loadedMesh == null) {
                    generateMesh();
                    updateDrawable();
                }

                textureFile = drawableMesh.textureFile;
                drawableMesh.loadedMesh = mesh;
                drawableMesh.isStaticMesh = !isDynamic;
                drawableMesh.addCollisionTriangles = receiveDecals;
            }
        }
    }

    @Override
    public void updateDrawable() {
        if (drawable == null) {
            DrawableMesh drawableMesh = new DrawableMesh();
            drawable = drawableMesh;
            generateMesh();
        }

        if (drawable != null && drawable instanceof DrawableMesh) {
            DrawableMesh drawableMesh = (DrawableMesh)drawable;
            drawableMesh.rotX = rotation.x;
			drawableMesh.rotY = rotation.y;
			drawableMesh.rotZ = rotation.z;
            drawableMesh.loadedMesh = mesh;
            drawableMesh.meshFile = "";
            drawableMesh.textureFile = textureFile;
            drawable.update(this);
        }
    }

    private void generateMesh() {
        mesh = new Mesh(
            false,
            24,
            36,
            new VertexAttribute(
                VertexAttributes.Usage.Position,
                3,
                "a_position"
            ),
            new VertexAttribute(
                VertexAttributes.Usage.TextureCoordinates,
                2,
                "a_texCoord0"
            )
        );

        float x = width / 2f;
        float y = depth / 2f;
        float z = height;

        float[] vertices = new float[]{
            // Bottom face
            -x, 0, -y, getUVMinU(width), getUVMinV(depth), // 0
            -x, 0, +y, getUVMinU(width), getUVMaxV(depth), // 1
            +x, 0, +y, getUVMaxU(width), getUVMaxV(depth), // 2
            +x, 0, -y, getUVMaxU(width), getUVMinV(depth), // 3

            // Top face
            -x, z, -y, getUVMaxU(width), getUVMaxV(depth), // 4
            -x, z, +y, getUVMaxU(width), getUVMinV(depth), // 5
            +x, z, +y, getUVMinU(width), getUVMinV(depth), // 6
            +x, z, -y, getUVMinU(width), getUVMaxV(depth), // 7

            // South face
            -x, 0, -y, getUVMaxU(width), getUVMinV(height), // 8
            -x, z, -y, getUVMaxU(width), getUVMaxV(height), // 9
            +x, z, -y, getUVMinU(width), getUVMaxV(height), // 10
            +x, 0, -y, getUVMinU(width), getUVMinV(height), // 11

            // North face
            -x, 0, +y, getUVMinU(width), getUVMinV(height), // 12
            -x, z, +y, getUVMinU(width), getUVMaxV(height), // 13
            +x, z, +y, getUVMaxU(width), getUVMaxV(height), // 14
            +x, 0, +y, getUVMaxU(width), getUVMinV(height), // 15

            // East face
            -x, 0, -y, getUVMinU(depth), getUVMinV(height), // 16
            -x, 0, +y, getUVMaxU(depth), getUVMinV(height), // 17
            -x, z, +y, getUVMaxU(depth), getUVMaxV(height), // 18
            -x, z, -y, getUVMinU(depth), getUVMaxV(height), // 19

            // West face
            +x, 0, -y, getUVMaxU(depth), getUVMinV(height), // 20
            +x, 0, +y, getUVMinU(depth), getUVMinV(height), // 21
            +x, z, +y, getUVMinU(depth), getUVMaxV(height), // 22
            +x, z, -y, getUVMaxU(depth), getUVMaxV(height), // 23
        };

        short[] indices = new short[]{
            // Bottom face
            0, 2, 1,
            0, 3, 2,

            // Top face
            4, 5, 6,
            4, 6, 7,

            // South face
            8, 9, 10,
            8, 10, 11,

            // North face
            12, 15, 14,
            12, 14, 13,

            // East face
            16, 17, 18,
            16, 18, 19,

            // West face
            20, 23, 22,
            20, 22, 21
        };

        mesh.setVertices(vertices);
        mesh.setIndices(indices);
    }

    float cachedWidth;
    float cachedDepth;
    float cachedHeight;

    @Override
    public void editorTick(Level level, float delta) {
        super.editorTick(level, delta);

        DrawableMesh drawableMesh = (DrawableMesh) drawable;
        if (drawableMesh == null) return;

        if (width != cachedWidth ||
            depth != cachedDepth ||
            height != cachedHeight ||
            !drawableMesh.textureFile.equals(textureFile)) {

            collision.x = width / 2f;
            collision.y = depth / 2f;
            collision.z = height;

            cachedWidth = width;
            cachedDepth = depth;
            cachedHeight = height;

            drawable = null;
        }
    }

    private float getUVMaxU(float width) {
        if (width <= 0.25 + 0.125) {
            return 31f / 32f;
        }
        if (width <= 0.5 + 0.25) {
            return 15f / 16f;
        }
        else if (width <= 1 + 0.5) {
            return 7f / 8f;
        }
        else if (width <= 2 + 1) {
            return 3f / 4f;
        }

        return 1f / 2f;
    }

    private float getUVMinU(float width) {
        if (width <= 0.25 + 0.125) {
            return 15f / 16f;
        }
        if (width <= 0.5 + 0.25) {
            return 7f / 8f;
        }
        else if (width <= 1 + 0.5) {
            return 3f / 4f;
        }
        else if (width <= 2 + 1) {
            return 1f / 2f;
        }

        return 0;
    }

    private float getUVMaxV(float height) {
        if (height <= 0.25 + 0.125) {
            return 1 - (31f / 32f);
        }
        if (height <= 0.5 + 0.25) {
            return 1 - (15f / 16f);
        }
        else if (height <= 1 + 0.5) {
            return 1 - (7f / 8f);
        }
        else if (height <= 2 + 1) {
            return 1 - (3f / 4f);
        }

        return 1 - (1f / 2f);
    }

    private float getUVMinV(float height) {
        if (height <= 0.25 + 0.125) {
            return 1 - (15f / 16f);
        }
        if (height <= 0.5 + 0.25) {
            return 1 - (7f / 8f);
        }
        else if (height <= 1 + 0.5) {
            return 1 - (3f / 4f);
        }
        else if (height <= 2 + 1) {
            return 1 - (1f / 2f);
        }

        return 1;
    }
}
