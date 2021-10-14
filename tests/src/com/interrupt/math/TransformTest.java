package com.interrupt.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.GdxTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class TransformTest {
    @Test
    public void TestPosition() {
        Vector3 position = new Vector3(0.5f, -0.5f, 0);

        Transform t = new Transform();
        t.setPosition(position);

        Vector3 expected = new Vector3(position);
        Vector3 actual = t.getPosition();

        assertEquals(expected, actual);
    }

    @Test
    public void TestWorldToLocalRoot() {
        Vector3 rootWorldPosition = new Vector3(0.5f, -0.5f, 0);

        Transform t = new Transform();
        t.setPosition(rootWorldPosition);

        Vector3 expected = new Vector3(rootWorldPosition);
        Vector3 actual = new Vector3(rootWorldPosition);
        t.worldToLocal(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void TestWorldToLocalChild() {
        Transform t = new Transform();
        Vector3 rootWorldPosition = new Vector3(0.5f, -0.5f, 0);
        t.setPosition(rootWorldPosition);

        Transform c = new Transform();
        Vector3 childWorldPosition = new Vector3(1.5f, 0.5f, 1);
        Vector3 childLocalPosition = new Vector3(1, 1, 1);

        t.addChild(c);
        c.setPosition(childWorldPosition);

        Vector3 expected = childLocalPosition;
        Vector3 actual = new Vector3(childWorldPosition);

        c.worldToLocal(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void TestLocalToWorldRoot() {
        Transform t = new Transform();
        Vector3 rootWorldPosition = new Vector3(0.5f, -0.5f, 0);
        t.setPosition(rootWorldPosition);

        Vector3 expected = new Vector3(rootWorldPosition);
        Vector3 actual = new Vector3(rootWorldPosition);
        t.localToWorld(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void TestLocalToWorldChild() {
        Transform t = new Transform();
        Vector3 rootWorldPosition = new Vector3(0.5f, -0.5f, 0);
        t.setPosition(rootWorldPosition);

        Transform c = new Transform();
        Vector3 childWorldPosition = new Vector3(1.5f, 0.5f, 1);
        Vector3 childLocalPosition = new Vector3(1, 1, 1);

        t.addChild(c);
        c.setPosition(childWorldPosition);

        Vector3 expected = childWorldPosition;
        Vector3 actual = new Vector3(childLocalPosition);
        c.localToWorld(actual);

        assertEquals(expected, actual);
    }

    public boolean epsilonEquals(Quaternion a, Quaternion b) {
        if (a == null || b == null) return false;

        float epsilon = MathUtils.FLOAT_ROUNDING_ERROR;

		if (Math.abs(a.x - b.x) > epsilon) return false;
		if (Math.abs(a.y - b.y) > epsilon) return false;
		if (Math.abs(a.z - b.z) > epsilon) return false;
		if (Math.abs(a.w - b.w) > epsilon) return false;

		return true;
    }

    @Test
    public void TestRotation() {
        Quaternion rotation = new Quaternion().setEulerAngles(0, 90, 45);

        Transform t = new Transform();
        t.setRotation(rotation);

        Quaternion expected = new Quaternion(rotation);
        Quaternion actual = t.getRotation();

        assertTrue(epsilonEquals(expected, actual));
    }

    @Test
    public void TestNestedRotation() {
        Quaternion rotation = new Quaternion().setEulerAngles(0, 90, 45);
        Transform t = new Transform();
        t.setRotation(rotation);

        Quaternion childRotation = new Quaternion().setEulerAngles(0, 0, 45);
        Transform c = new Transform();
        c.setRotation(childRotation);

        t.addChild(c);

        Quaternion expected = new Quaternion().setEulerAngles(0, 90, 90);
        Quaternion actual = c.getRotation();

        assertTrue(epsilonEquals(expected, actual));
    }

    @Test
    public void TestScale() {
        Vector3 scale = new Vector3(1, 0.5f, -2.5f);

        Transform t = new Transform();
        t.setScale(scale);

        Vector3 expected = new Vector3(scale);
        Vector3 actual = t.getScale();

        assertEquals(expected, actual);
    }

    @Test
    public void TestNestedScale() {
        Vector3 scale = new Vector3(1, 0.5f, -2.5f);
        Transform t = new Transform();
        t.setScale(scale);

        Vector3 childScale = new Vector3(2, 4, -0.8f);
        Transform c = new Transform();
        c.setScale(childScale);

        t.addChild(c);

        Vector3 expected = new Vector3(2, 2, 2);
        Vector3 actual = c.getScale();

        assertEquals(expected, actual);
    }
}
