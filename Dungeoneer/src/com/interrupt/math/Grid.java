package com.interrupt.math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Grid {
    public final Transform transform;
    public final float size;

    public Grid(float size) {
        this(size, Vector3.Zero, new Quaternion(), new Vector3(1, 1, 1));
    }

    public Grid(float size, Vector3 position, Quaternion rotation, Vector3 scale) {
        this.size = size;
        transform = new Transform();
        transform.set(
            position,
            rotation,
            scale
        );
    }

    public Vector3 snap(Vector3 vector) {
        transform.worldToLocalPosition(vector);

        vector.x = (float)Math.round(vector.x / size) * size;
        vector.y = (float)Math.round(vector.y / size) * size;
        vector.z = (float)Math.round(vector.z / size) * size;

        transform.localToWorldPosition(vector);

        return vector;
    }
}
