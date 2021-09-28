package com.interrupt.dungeoneer.entities;

public class EntityIntersector {
    public static boolean intersects(Entity e1, Entity e2) {
        if(e2.x > e1.x - e1.collision.x - e2.collision.x) {
            if(e2.x < e1.x + e1.collision.x + e2.collision.x) {
                if(e2.y > e1.y - e1.collision.y - e2.collision.y) {
                    if(e2.y < e1.y + e1.collision.y + e2.collision.y) {
                        if(e2.z > e1.z - e2.collision.z && e2.z < e1.z + e1.collision.z) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean intersects(float nextX, float nextY, float nextZ, Entity e1, Entity e2) {
        return intersects(nextX, nextY, nextZ, e2.collision.x, e2.collision.y, e2.collision.z, e1);
    }

    public static boolean intersects(float nextX, float nextY, float nextZ, float collisionX, float collisionY, float collisionZ, Entity e) {
        if(nextX > e.x - e.collision.x - collisionX) {
            if(nextX < e.x + e.collision.x + collisionX) {
                if(nextY > e.y - e.collision.y - collisionY) {
                    if(nextY < e.y + e.collision.y + collisionY) {
                        if(nextZ > e.z - collisionZ && nextZ < e.z + e.collision.z) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean intersects2d(float nextX, float nextY, float collisionX, float collisionY, Entity e) {
        if(nextX > e.x - e.collision.x - collisionX) {
            if(nextX < e.x + e.collision.x + collisionX) {
                if(nextY > e.y - e.collision.y - collisionY) {
                    if(nextY < e.y + e.collision.y + collisionY) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
