package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.interrupt.math.MathUtils;

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
        return disc(0.5f, 32);
    }

    /** Generates a disc mesh. */
    public static Mesh disc(float radius, int segments) {
        return arc(radius, 0, 360, segments);
    }

    /** Generates a unit cone mesh. */
    public static Mesh cone() {
        return cone(0.5f, 1, 32);
    }

    /** Generates a cone mesh. */
    public static Mesh cone(float radius, float height, int segments) {
        int componentsPerVertex = 6;
        int indicesPerSegment = 6;

        Mesh mesh = new Mesh(
            true,
            segments + 2,
            segments * indicesPerSegment,
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

        float[] vertices = new float[(segments + 2) * componentsPerVertex];
        short[] indices = new short[segments * indicesPerSegment];

        float step = MathUtils.TAU / segments;

        // Base center vertex
        vertices[0] = 0;
        vertices[1] = 0;
        vertices[2] = 0;
        vertices[3] = Color.WHITE_FLOAT_BITS;
        vertices[4] = 0;
        vertices[5] = 0;

        // Top center vertex
        vertices[ 6] = 0;
        vertices[ 7] = height;
        vertices[ 8] = 0;
        vertices[ 9] = Color.WHITE_FLOAT_BITS;
        vertices[10] = 0;
        vertices[11] = 0;

        // Generate vertices
        for (int i = 0; i < segments; i++) {
            int offset = (i + 2) * componentsPerVertex;

            vertices[offset + 0] = (float)Math.cos((i + 1) * step) * radius;
            vertices[offset + 1] = 0;
            vertices[offset + 2] = (float)Math.sin((i + 1) * step) * radius;
            vertices[offset + 3] = Color.WHITE_FLOAT_BITS;
            vertices[offset + 4] = 0;
            vertices[offset + 5] = 0;
        }

        // Generate indices
        for (short i = 0; i < segments; i++) {
            int offset = i * indicesPerSegment;
            short current = (short)(i + 2);
            short next = (short)((i + 1) % segments + 2);

            indices[offset + 0] = 0;
            indices[offset + 1] = next;
            indices[offset + 2] = current;
            indices[offset + 3] = 1;
            indices[offset + 4] = next;
            indices[offset + 5] = current;
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    /** Generates a unit arc mesh. */
    public static Mesh arc(float start, float end) {
        return arc(0.5f, start, end, 32);
    }

    /** Generates an arc mesh. */
    public static Mesh arc(float radius, float start, float end, int segments) {
        int componentsPerVertex = 6;
        int indicesPerSegment = 3;

        Mesh mesh = new Mesh(
            true,
            segments + 2,
            segments * indicesPerSegment,
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

        float[] vertices = new float[(segments + 2) * componentsPerVertex];
        short[] indices = new short[segments * indicesPerSegment];

        float begin = (float)Math.toRadians(start);
        float stop = (float)Math.toRadians(end);
        float step = (stop - begin) / segments;

        // Center vertex
        vertices[0] = 0;
        vertices[1] = 0;
        vertices[2] = 0;
        vertices[3] = Color.WHITE_FLOAT_BITS;
        vertices[4] = 0;
        vertices[5] = 0;

        // Generate vertices
        for (int i = 0; i <= segments; i++) {
            int offset = (i + 1) * componentsPerVertex;

            vertices[offset + 0] = (float)Math.cos(i * step + begin) * radius;
            vertices[offset + 1] = 0;
            vertices[offset + 2] = (float)Math.sin(i * step + begin) * radius;
            vertices[offset + 3] = Color.WHITE_FLOAT_BITS;
            vertices[offset + 4] = 0;
            vertices[offset + 5] = 0;
        }

        // Generate indices
        for (short i = 0; i < segments; i++) {
            int offset = i * indicesPerSegment;
            short current = (short)(i + 1);
            short next = (short)(i + 2);

            indices[offset + 0] = 0;
            indices[offset + 1] = next;
            indices[offset + 2] = current;
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    /** Generates a unit ring mesh. */
    public static Mesh ring() {
        return ring(1 - (1 / 8f), 1, 32);
    }

    /** Generates a ring mesh. */
    public static Mesh ring(float innerRadius, float outerRadius, int segments) {
        return ringArc(innerRadius, outerRadius, 0, 360, segments);
    }

    /** Generates a unit ring arc mesh. */
    public static Mesh ringArc(float start, float end) {
        return ringArc(1 - (1 / 8f), 1, start, end, 32);
    }

    /** Generates an ring arc mesh. */
    public static Mesh ringArc(float innerRadius, float outerRadius, float start, float end, int segments) {
        int componentsPerVertex = 6;
        int indicesPerSegment = 6;

        Mesh mesh = new Mesh(
            true,
            (segments + 1) * 2,
            segments * indicesPerSegment,
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

        float[] vertices = new float[(segments + 1) * 2 * componentsPerVertex];
        short[] indices = new short[segments * indicesPerSegment];

        float begin = (float)Math.toRadians(start);
        float stop = (float)Math.toRadians(end);
        float step = (stop - begin) / segments;

        // Generate vertices
        for (int i = 0; i <= segments; i++) {
            int offset = i * 2 * componentsPerVertex;

            float cos = (float)Math.cos(i * step + begin);
            float sin = (float)Math.sin(i * step + begin);

            // Inner vertex
            vertices[offset + 0] = cos * innerRadius;
            vertices[offset + 1] = 0;
            vertices[offset + 2] = sin * innerRadius;
            vertices[offset + 3] = Color.WHITE_FLOAT_BITS;
            vertices[offset + 4] = 0;
            vertices[offset + 5] = 0;

            // Outer vertex
            vertices[offset +  6] = cos * outerRadius;
            vertices[offset +  7] = 0;
            vertices[offset +  8] = sin * outerRadius;
            vertices[offset +  9] = Color.WHITE_FLOAT_BITS;
            vertices[offset + 10] = 0;
            vertices[offset + 11] = 0;
        }

        // Generate indices
        for (short i = 0; i < segments; i++) {
            int offset = i * indicesPerSegment;
            short innerCurrent = (short)(i * 2 + 0);
            short outerCurrent = (short)(i * 2 + 1);
            short innerNext = (short)(i * 2 + 2);
            short outerNext = (short)(i * 2 + 3);

            // Inner index
            indices[offset + 0] = innerCurrent;
            indices[offset + 1] = outerNext;
            indices[offset + 2] = outerCurrent;

            // Outer index
            indices[offset + 3] = innerCurrent;
            indices[offset + 4] = innerNext;
            indices[offset + 5] = outerNext;
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    /** Combines several meshes into one.
     * Adapated from: https://stackoverflow.com/questions/20170758/slow-model-batch-rendering-in-libgdx/20937752 */
    public static Mesh combine(Mesh... meshes) {
        if (meshes.length == 0) return null;

        int combinedVertexCount = 0;
        int combinedIndexCount = 0;
        VertexAttributes attributes = meshes[0].getVertexAttributes();
        int componentsPerVertex = meshes[0].getVertexSize() / 4;

        for (Mesh mesh : meshes) {
            if (mesh.getVertexAttributes().size() != attributes.size()) {
                throw new RuntimeException("Can only combine meshes with matching vertex attributes");
            }

            combinedVertexCount += mesh.getNumVertices();
            combinedIndexCount += mesh.getNumIndices();
        }

        float[] vertices = new float[combinedVertexCount * componentsPerVertex];
        short[] indices = new short[combinedIndexCount];

        int indexOffset = 0;
        int vertexOffset = 0;
        int componentsOffset = 0;

        for (Mesh mesh : meshes) {
            int indexCount = mesh.getNumIndices();
            int vertexCount = mesh.getNumVertices();
            int componentsCount = vertexCount * componentsPerVertex;

            // Copy indices
            mesh.getIndices(indices, indexOffset);

            // Adjust index offsets
            for (int i = indexOffset; i < indexOffset + indexCount; i++) {
                indices[i] += vertexOffset;
            }

            indexOffset += indexCount;

            // Copy vertices
            mesh.getVertices(
                0,
                componentsCount,
                vertices,
                componentsOffset
            );

            vertexOffset += vertexCount;
            componentsOffset += componentsCount;
        }

        Mesh mesh = new Mesh(
            true,
            combinedVertexCount,
            combinedIndexCount,
            attributes
        );

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }
}
