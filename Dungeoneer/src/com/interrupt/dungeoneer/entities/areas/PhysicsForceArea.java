package com.interrupt.dungeoneer.entities.areas;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.interfaces.Directional;

public class PhysicsForceArea extends Area implements Directional {
    public PhysicsForceArea() { hidden = true; spriteAtlas = "editor"; tex = 11; isStatic = false; isDynamic = true; collision.set(0.25f, 0.25f, 1f); }

    /** Amount of impulse force to apply to entities. */
    @EditorProperty
    public float forceAmount = 0.01f;

    /** Whether the force should be applied instantly, or incrementally. */
    @EditorProperty
    public boolean instantForce = false;

    /** Apply the physics force? If disabled, a trigger can enable it. */
    @EditorProperty
    public boolean enabled = true;

    public Vector3 rotation = new Vector3();

    public transient Vector3 dirWork = new Vector3();

    @Override
    public void tick(Level level, float delta) {
        if(!enabled)
            return;

        Array<Entity> touching = level.getEntitiesEncroaching(this);
        for(int i = 0; i < touching.size; i++) {
            Entity e = touching.get(i);
            if(!e.isStatic) {
                Vector3 dir = getDirection();

                if(instantForce) {
                    e.xa = dir.x * forceAmount;
                    e.ya = dir.y * forceAmount;
                    e.za = dir.z * forceAmount;
                } else {
                    e.xa += dir.x * forceAmount * delta;
                    e.ya += dir.y * forceAmount * delta;
                    e.za += dir.z * forceAmount * delta;
                }
            }
        }
    }

    @Override
    public void onTrigger(Entity instigator, String value) {
        enabled = !enabled;
    }

    @Override
    public void rotate90() {
        super.rotate90();
        rotation.z -= 90f;
    }

    @Override
    public void rotate90Reversed() {
        super.rotate90Reversed();
        rotation.z += 90f;
    }

    @Override
    public void setRotation(float rotX, float rotY, float rotZ) {
        rotation.x = rotX;
        rotation.y = rotY;
        rotation.z = rotZ;
    }

    @Override
    public void rotate(float rotX, float rotY, float rotZ) {
        rotation.x += rotX;
        rotation.y += rotY;
        rotation.z += rotZ;
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    public Vector3 getDirection() {
        Vector3 dir = dirWork.set(1,0,0);
        dir.rotate(Vector3.Y, -rotation.y);
        dir.rotate(Vector3.X, -rotation.x);
        dir.rotate(Vector3.Z, -rotation.z);
        return dir;
    }
}
