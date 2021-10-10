package com.interrupt.math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class MathUtils {
    public static final float PI = 3.1415927f;
    public static final float TAU = 6.2831854f;

    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();

    /** Creates a rotation with the given forward direction and default up direction (+y axis). */
    public static Quaternion lookRotation(Vector3 forward, Quaternion rotation) {
        return lookRotation(forward, Vector3.Y, rotation);
    }

    /** Creates a rotation with the given forward and up directions. */
    public static Quaternion lookRotation(Vector3 forward, Vector3 up, Quaternion rotation) {
        tmp.set(up).crs(forward).nor();
        tmp2.set(forward).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, forward.x, tmp.y, tmp2.y, forward.y, tmp.z, tmp2.z, forward.z);

        return rotation;
    }
}
