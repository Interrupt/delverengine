package com.interrupt.math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

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
        if (forward.epsilonEquals(Vector3.Y)) {
            return lookRotation(forward, Vector3.Z, rotation);
        }

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

    public static Quaternion setEuler(Quaternion quaternion, float yaw, float pitch, float roll) {
        double halfYaw = Math.toRadians(yaw) / 2;
        double halfPitch = Math.toRadians(pitch) / 2;
        double halfRoll = Math.toRadians(roll) / 2;

        quaternion.x = (float)(cos(halfYaw) * sin(halfPitch) * cos(halfRoll) + sin(halfYaw) * cos(halfPitch) * sin(halfRoll));
        quaternion.y = (float)(sin(halfYaw) * cos(halfPitch) * cos(halfRoll) - cos(halfYaw) * sin(halfPitch) * sin(halfRoll));
        quaternion.z = (float)(cos(halfYaw) * cos(halfPitch) * sin(halfRoll) - sin(halfYaw) * sin(halfPitch) * cos(halfRoll));
        quaternion.w = (float)(cos(halfYaw) * cos(halfPitch) * cos(halfRoll) + sin(halfYaw) * sin(halfPitch) * sin(halfRoll));

        return quaternion;
    }

    /** Get the yaw euler angle in degrees, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * Higher precision than libGDX implementation.
     * @param quaternion The quaternion
     * @return the rotation around the y axis in radians (between -180 and +180) */
    public static float getYaw(Quaternion quaternion) {
        return (float)Math.toDegrees(quaternion.getGimbalPole() == 0 ? Math.atan2(2.0 * (quaternion.y * quaternion.w + quaternion.x * quaternion.z), 1.0 - 2.0 * (quaternion.y * quaternion.y + quaternion.x * quaternion.x)) : 0.0);
    }

    /** Get the pitch euler angle in degrees, which is the rotation around the x axis. Requires that this quaternion is normalized.
     * Higher precision than libGDX implementation.
     * @param quaternion The quaternion
     * @return the rotation around the x axis in degrees (between -90 and +90) */
    public static float getPitch(Quaternion quaternion) {
        final double pole = quaternion.getGimbalPole();
        return (float)Math.toDegrees(pole == 0 ? Math.asin(com.badlogic.gdx.math.MathUtils.clamp(2.0 * (quaternion.w * quaternion.x - quaternion.z * quaternion.y), -1.0, 1.0)) : pole * Math.PI * 0.5);
    }

    /** Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
     * Higher precision than libGDX implementation.
     * @param quaternion The quaternion
     * @return the rotation around the z axis in degrees (between -180 and +180) */
    public static float getRoll(Quaternion quaternion) {
        final double pole = quaternion.getGimbalPole();
        return (float)Math.toDegrees(pole == 0 ? Math.atan2(2.0 * (quaternion.w * quaternion.z + quaternion.y * quaternion.x), 1.0 - 2.0 * (quaternion.x * quaternion.x + quaternion.z * quaternion.z)) : pole * 2.0 * com.badlogic.gdx.math.MathUtils.atan2(quaternion.y, quaternion.w));
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
