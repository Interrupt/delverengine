package com.interrupt.math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class MathUtils {
    public static final float PI = 3.1415927f;
    public static final float TAU = 6.2831854f;

    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();

    /** Creates a rotation with the given forward direction and default up direction (+y axis).
     * @param forward Forward direction
     * @param rotation The Quaternion to set
     * @return The given Quaternion for chaining */
    public static Quaternion lookRotation(Vector3 forward, Quaternion rotation) {
        return lookRotation(forward, Vector3.Y, rotation);
    }

    /** Creates a rotation with the given forward and up directions.
     * @param forward Forward direction
     * @param up Up direction
     * @param rotation The Quaternion to set
     * @return The given Quaternion for chaining */
    public static Quaternion lookRotation(Vector3 forward, Vector3 up, Quaternion rotation) {
        tmp.set(up).crs(forward).nor();
        tmp2.set(forward).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, forward.x, tmp.y, tmp2.y, forward.y, tmp.z, tmp2.z, forward.z);

        return rotation;
    }

    private static final Quaternion x = new Quaternion();
    public static Quaternion inverse(Quaternion quaternion) {
        float rcp = 1f / quaternion.dot(quaternion);
        x.set(-1 ,-1, -1, 1);
        quaternion.mul(rcp).mul(x);

        return quaternion;
    }

    /** Swaps y and z components of the given vector.
     * @param vector The vector for swizzling
     * @return vector for chaining */
    public static Vector3 swizzleXZY(Vector3 vector) {
        float swap = vector.z;
        vector.z = vector.y;
        vector.y = swap;

        return vector;
    }

    /** Projects given vector on onto vector.
     * @param vector The vector to project
     * @param direction The vector to project onto
     * @return vector for chaining */
    public static Vector3 project(Vector3 vector, Vector3 direction) {
        float magnitude = vector.dot(direction);
        return vector.set(direction).scl(magnitude);
    }
}
