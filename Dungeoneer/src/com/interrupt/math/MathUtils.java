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
        final double halfYaw = Math.toRadians(yaw) / 2;
        final double halfPitch = Math.toRadians(pitch) / 2;
        final double halfRoll = Math.toRadians(roll) / 2;

        final double cosHalfYaw = cos(halfYaw);
        final double sinHalfYaw = sin(halfYaw);
        final double cosHalfPitch = cos(halfPitch);
        final double sinHalfPitch = sin(halfPitch);
        final double cosHalfRoll = cos(halfRoll);
        final double sinHalfRoll = sin(halfRoll);

        quaternion.x = (float)(cosHalfYaw * sinHalfPitch * cosHalfRoll + sinHalfYaw * cosHalfPitch * sinHalfRoll);
        quaternion.y = (float)(sinHalfYaw * cosHalfPitch * cosHalfRoll - cosHalfYaw * sinHalfPitch * sinHalfRoll);
        quaternion.z = (float)(cosHalfYaw * cosHalfPitch * sinHalfRoll - sinHalfYaw * sinHalfPitch * cosHalfRoll);
        quaternion.w = (float)(cosHalfYaw * cosHalfPitch * cosHalfRoll + sinHalfYaw * sinHalfPitch * sinHalfRoll);

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

    /** Get Euler angles for given Quaternion. Handles gimbal lock singularities.
     * @param quaternion A Quaternion to convert. Assumed to be normalized.
     * @param vector Vector3 to represent euler angles.
     * @return v for chaining.
     */
    public static Vector3 toEuler(Quaternion quaternion, Vector3 vector) {
        final float x = quaternion.x;
        final float y = quaternion.y;
        final float z = quaternion.z;
        final float w = quaternion.w;

        // Handle gimbal lock.
        float test = x * w - y * z;
        if (Math.abs(test) > 0.4995f) {
            final float sign = Math.signum(test);

            vector.x = 90f * sign;
            vector.y = (float)Math.toDegrees(2f * Math.atan2(y, x)) * sign;
            vector.z = 0;

            return vector;
        }

        vector.x = (float)Math.toDegrees(Math.asin(2f * (x * w - y * z)));
        vector.y = (float)Math.toDegrees(Math.atan2(2f * (y * w + x * z), 1 - 2f * (x * x + y * y)));
        vector.z = (float)Math.toDegrees(Math.atan2(2f * (z * w + x * y), 1 - 2f * (x * x + z * z)));

        return vector;
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
