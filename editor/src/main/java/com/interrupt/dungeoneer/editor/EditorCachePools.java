package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Triangle;
import com.interrupt.dungeoneer.gfx.StaticMeshPool;

public class EditorCachePools {

    // Triangle cache
    protected static Pool<Triangle> trianglePool = new Pool<Triangle>(1000) {
        @Override
        protected Triangle newObject () {
            return new Triangle(new Vector3(), new Vector3(), new Vector3());
        }
    };
    protected static Array<Triangle> usedTriangles = new Array<Triangle>();

    public static Triangle getTriangle() {
        return trianglePool.obtain();
    }

    public static void freeTriangles() {
        trianglePool.freeAll(usedTriangles);
        usedTriangles.clear();
    }

    // Collision Vector3 cache
    protected static Pool<Vector3> collisionVector3Pool = new Pool<Vector3>(1000) {
        @Override
        protected Vector3 newObject () {
            return new Vector3();
        }
    };
    protected static Array<Vector3> usedCollisionVectors = new Array<Vector3>();

    public static Vector3 getCollisionVector(float v1, float v2, float v3) {
        return collisionVector3Pool.obtain().set(v1, v2, v3);
    }

    public static void freeCollisionVectors() {
        collisionVector3Pool.freeAll(usedCollisionVectors);
        usedCollisionVectors.clear();
    }

    // Static mesh cache
    private static StaticMeshPool staticMeshPool = new StaticMeshPool();
    protected static Array<Mesh> usedStaticMeshes = new Array<Mesh>();

    public static Mesh getStaticMesh(VertexAttributes vertexAttributes, int vertexCount, int indexCount, boolean createMax) {
        Mesh m = staticMeshPool.obtain(vertexAttributes, vertexCount, indexCount, createMax);
        usedStaticMeshes.add(m);
        return m;
    }

    public static void freeStaticMeshes() {
        staticMeshPool.freeMeshes(usedStaticMeshes);
        usedStaticMeshes.clear();
    }

    // clear all
    public static void freeAllCaches() {
        freeTriangles();
        freeCollisionVectors();
        freeStaticMeshes();
    }
}
