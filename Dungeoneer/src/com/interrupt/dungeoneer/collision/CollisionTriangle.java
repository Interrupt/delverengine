package com.interrupt.dungeoneer.collision;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Triangle;

public class CollisionTriangle extends Triangle {

    public enum TriangleCollisionType { WORLD, WATER };

    public TriangleCollisionType collisionType = TriangleCollisionType.WORLD;

    public CollisionTriangle(Vector3 v1, Vector3 v2, Vector3 v3 ) {
        super(v1, v2, v3);
    }

    public CollisionTriangle(Vector3 v1, Vector3 v2, Vector3 v3, TriangleCollisionType colType) {
        super(v1, v2, v3);
        collisionType = colType;
    }
}
