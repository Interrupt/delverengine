package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Meshes {
    /** Generates a unit cube mesh. */
    public static Mesh cube() {
        return cube(1, 1, 1);
    }

    /** Generates cube mesh. */
    public static Mesh cube(float width, float depth, float height) {
        Mesh mesh = new Mesh(
            true,
            24,
            36,
            new VertexAttribute(
                VertexAttributes.Usage.Position,
                3,
                ShaderProgram.POSITION_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.ColorPacked,
                4,
                ShaderProgram.COLOR_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.TextureCoordinates,
                2,
                ShaderProgram.TEXCOORD_ATTRIBUTE + "0"
            )
        );

        float x = width / 2f;
        float y = depth / 2f;
        float z = height;

        float[] vertices = new float[]{
            // Bottom face
            -x, 0, -y, Color.WHITE_FLOAT_BITS, 0, 0, // 0
            -x, 0, +y, Color.WHITE_FLOAT_BITS, 0, 1, // 1
            +x, 0, +y, Color.WHITE_FLOAT_BITS, 1, 1, // 2
            +x, 0, -y, Color.WHITE_FLOAT_BITS, 1, 0, // 3

            // Top face
            -x, z, -y, Color.WHITE_FLOAT_BITS, 1, 1, // 4
            -x, z, +y, Color.WHITE_FLOAT_BITS, 1, 0, // 5
            +x, z, +y, Color.WHITE_FLOAT_BITS, 0, 0, // 6
            +x, z, -y, Color.WHITE_FLOAT_BITS, 0, 1, // 7

            // South face
            -x, 0, -y, Color.WHITE_FLOAT_BITS, 1, 0, // 8
            -x, z, -y, Color.WHITE_FLOAT_BITS, 1, 1, // 9
            +x, z, -y, Color.WHITE_FLOAT_BITS, 0, 1, // 10
            +x, 0, -y, Color.WHITE_FLOAT_BITS, 0, 0, // 11

            // North face
            -x, 0, +y, Color.WHITE_FLOAT_BITS, 0, 0, // 12
            -x, z, +y, Color.WHITE_FLOAT_BITS, 0, 1, // 13
            +x, z, +y, Color.WHITE_FLOAT_BITS, 1, 1, // 14
            +x, 0, +y, Color.WHITE_FLOAT_BITS, 1, 0, // 15

            // East face
            -x, 0, -y, Color.WHITE_FLOAT_BITS, 0, 0, // 16
            -x, 0, +y, Color.WHITE_FLOAT_BITS, 1, 0, // 17
            -x, z, +y, Color.WHITE_FLOAT_BITS, 1, 1, // 18
            -x, z, -y, Color.WHITE_FLOAT_BITS, 0, 1, // 19

            // West face
            +x, 0, -y, Color.WHITE_FLOAT_BITS, 1, 0, // 20
            +x, 0, +y, Color.WHITE_FLOAT_BITS, 0, 0, // 21
            +x, z, +y, Color.WHITE_FLOAT_BITS, 0, 1, // 22
            +x, z, -y, Color.WHITE_FLOAT_BITS, 1, 1, // 23
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

        return mesh;
    }

    /** Generates a unit quad mesh. */
    public static Mesh quad() {
        return quad(1, 1);
    }

    /** Generates a two-sided quad mesh. */
    public static Mesh quad(float width, float depth) {
        Mesh mesh = new Mesh(
            true,
            8,
            12,
            new VertexAttribute(
                VertexAttributes.Usage.Position,
                3,
                ShaderProgram.POSITION_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.ColorPacked,
                4,
                ShaderProgram.COLOR_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.TextureCoordinates,
                2,
                ShaderProgram.TEXCOORD_ATTRIBUTE + "0"
            )
        );

        float x = width / 2f;
        float y = depth / 2f;

        float[] vertices = new float[]{
            // Bottom face
            -x, 0, -y, Color.WHITE_FLOAT_BITS, 0, 0, // 0
            -x, 0, +y, Color.WHITE_FLOAT_BITS, 0, 1, // 1
            +x, 0, +y, Color.WHITE_FLOAT_BITS, 1, 1, // 2
            +x, 0, -y, Color.WHITE_FLOAT_BITS, 1, 0, // 3

            // Top face
            -x, 0, -y, Color.WHITE_FLOAT_BITS, 1, 1, // 4
            -x, 0, +y, Color.WHITE_FLOAT_BITS, 1, 0, // 5
            +x, 0, +y, Color.WHITE_FLOAT_BITS, 0, 0, // 6
            +x, 0, -y, Color.WHITE_FLOAT_BITS, 0, 1, // 7
        };

        short[] indices = new short[]{
            // Bottom face
            0, 2, 1,
            0, 3, 2,

            // Top face
            4, 5, 6,
            4, 6, 7,
        };

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    /** Generates a unit disc mesh. */
    public static Mesh disc() {
        return disc(1, 32);
    }

    /** Generates a disc mesh. */
    public static Mesh disc(float radius, int segments) {
        Mesh mesh = new Mesh(
            true,
            segments + 1,
            segments * 3,
            new VertexAttribute(
                VertexAttributes.Usage.Position,
                3,
                ShaderProgram.POSITION_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.ColorPacked,
                4,
                ShaderProgram.COLOR_ATTRIBUTE
            ),
            new VertexAttribute(
                VertexAttributes.Usage.TextureCoordinates,
                2,
                ShaderProgram.TEXCOORD_ATTRIBUTE + "0"
            )
        );

        float[] vertices = new float[(segments + 1) * 6];
        short[] indices = new short[segments * 3];

        float tau = (float)Math.PI * 2;
        float step = tau / segments;

        // Center vertex
        vertices[0] = 0;
        vertices[1] = 0;
        vertices[2] = 0;
        vertices[3] = Color.WHITE_FLOAT_BITS;
        vertices[4] = 0;
        vertices[5] = 0;

        // First vertex
        vertices[ 6] = 1 * radius;
        vertices[ 7] = 0;
        vertices[ 8] = 0 * radius;
        vertices[ 9] = Color.WHITE_FLOAT_BITS;
        vertices[10] = 0;
        vertices[11] = 0;

        for (int i = 0; i < segments - 1; i++) {
            // Vertex array offset
            int vx = (i * 6) + 12;
            // Index array offset
            int ix = i * 3;

            // Vertex
            vertices[vx + 0] = (float)Math.cos((i + 1) * step) * radius;
            vertices[vx + 1] = 0;
            vertices[vx + 2] = (float)Math.sin((i + 1) * step) * radius;
            vertices[vx + 3] = Color.WHITE_FLOAT_BITS;
            vertices[vx + 4] = 0;
            vertices[vx + 5] = 0;

            // Indices
            indices[ix + 0] = 0;
            indices[ix + 1] = (short)(i + 2);
            indices[ix + 2] = (short)(i + 1);
        }

        // Last index
        int last = (segments - 1) * 3;
        indices[last + 0] = 0;
        indices[last + 1] = (short)segments;
        indices[last + 2] = 1;

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }
}
