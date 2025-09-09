package com.interrupt.dungeoneer.generator.rooms.themes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class RoomPrefab {
    public String group = "";
    public String name = "";
    public int space = 1;
    public int size = 1;
    public float chance = 1f;
    public Vector2 placedAt = null;

    public Vector3 rotateAmount = new Vector3(0, 0, 20);
    public boolean placeOnCeiling = false;
    public boolean placeCentered = false;
    public float heightOffset = 0f;

    public float minFreeHeight = 0.7f;
    public float maxFreeHeight = Float.MAX_VALUE;

    public RoomPrefab() { }

    public RoomPrefab(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public RoomPrefab(String group, String name, float chance) {
        this.group = group;
        this.name = name;
        this.chance = chance;
    }

    public RoomPrefab(String group, String name, float chance, float minFreeHeight) {
        this.group = group;
        this.name = name;
        this.chance = chance;
        this.minFreeHeight = minFreeHeight;
    }

    public RoomPrefab(String group, String name, float chance, float minFreeHeight, float maxFreeHeight) {
        this.group = group;
        this.name = name;
        this.chance = chance;
        this.minFreeHeight = minFreeHeight;
        this.maxFreeHeight = maxFreeHeight;
    }

    public RoomPrefab(String group, String name, Vector2 location) {
        this.group = group;
        this.name = name;
        this.placedAt = location;
    }

    public RoomPrefab rotation(float rotX, float rotY, float rotZ) {
        rotateAmount.set(rotX, rotY, rotZ);
        return this;
    }

    public RoomPrefab minFreeHeight(float min) {
        this.minFreeHeight = min;
        return this;
    }

    public RoomPrefab maxFreeHeight(float max) {
        this.maxFreeHeight = max;
        return this;
    }
}
