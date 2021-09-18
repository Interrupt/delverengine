package com.interrupt.math;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RangeTest {
    @Test
    public void testContains() {
        Range range = new Range(0, 1);

        assertTrue(range.contains(0));
        assertTrue(range.contains(0.5f));
        assertTrue(range.contains(1));

        assertFalse(range.contains(-1));
        assertFalse(range.contains(2));
    }

    @Test
    public void testIntersects() {
        Range a = new Range(0, 1);
        Range b = new Range(-0.5f, 1.5f);
        Range c = new Range(1, 2);
        Range d = new Range(1.1f, 2f);

        assertTrue(a.intersects(b), "B contains A and should intersect");
        assertTrue(a.intersects(c), "A and C touch and should intersect");
        assertFalse(a.intersects(d), "A and D should not intersect");
    }
}
