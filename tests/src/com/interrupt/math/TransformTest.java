package com.interrupt.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.GdxTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(GdxTestRunner.class)
public class TransformTest {
    @Test
    public void TestPosition() {
        Vector3 v = new Vector3();

        Transform parent = new Transform();
        Transform child = new Transform();
        parent.addChild(child);

        parent.setPosition(v.set(1, 2, 0));
        child.setPosition(v.set(-1, -1, 0));

        // Check world position
        Vector3 position = parent.getPosition();
        assertEquals(new Vector3(1, 2, 0), position);

        // Check local position
        position = parent.getLocalPosition();
        assertEquals(new Vector3(1, 2, 0), position);

        // Check world position
        position = child.getPosition();
        assertEquals(new Vector3(-1, -1, 0), position);

        // Check local position
        position = child.getLocalPosition();
        assertEquals(new Vector3(-2, -3, 0), position);
    }

    @Test
    public void TestRepeatedSetPositionsOnRoot() {
        Transform parent = new Transform();

        parent.setPosition(1, 1, 0);
        Vector3 position = parent.getPosition();
        assertEquals(new Vector3(1, 1, 0), position);

        parent.setPosition(1, 1, 0);
        position = parent.getPosition();
        assertEquals(new Vector3(1, 1, 0), position);
    }

    @Test
    public void TestRotation() {
        Quaternion q = new Quaternion();
        Quaternion rotation;
        Transform parent = new Transform();
        Transform child = new Transform();
        parent.addChild(child);

        parent.setRotation(q.setEulerAngles(45, 0, 0));
        child.setRotation(q.setEulerAngles(90, 0, 0));

        // Check world rotation
        rotation = parent.getRotation();
        assertEquals(q.setEulerAngles(45, 0, 0), rotation);

        // Check local rotation
        rotation = parent.getLocalRotation();
        assertEquals(q.setEulerAngles(45, 0, 0), rotation);

        // Check world rotation
        rotation = child.getRotation();
        q.setEulerAngles(90, 0, 0);
        assertEquals(q.x, rotation.x, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.y, rotation.y, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.z, rotation.z, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.w, rotation.w, MathUtils.FLOAT_ROUNDING_ERROR);

        // Check local rotation
        rotation = child.getLocalRotation();
        q.setEulerAngles(45, 0, 0);
        assertEquals(q.x, rotation.x, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.y, rotation.y, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.z, rotation.z, MathUtils.FLOAT_ROUNDING_ERROR);
        assertEquals(q.w, rotation.w, MathUtils.FLOAT_ROUNDING_ERROR);
    }
}
