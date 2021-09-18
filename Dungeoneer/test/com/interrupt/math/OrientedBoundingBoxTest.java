package com.interrupt.math;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrientedBoundingBoxTest {
    private final Vector3 temp = new Vector3();

    private final float epsilon2 = 0.0001f * 0.0001f;
    private boolean isClose(Vector3 a, Vector3 b)
    {
        temp.set(a);
        return Vector3.dst2(a.x, a.y, a.z, b.x, b.y, b.z) < epsilon2;
    }

    @Test
    public void TestRotation() {
        OrientedBoundingBox obb = new OrientedBoundingBox(
            new Vector3(-0.5f, -0.5f, 0f),
            new Vector3(0.5f, 0.5f, 1)
        );

        Vector3 corner = new Vector3();
        obb.getCorner000(corner);

        assertTrue(
            isClose(new Vector3(-0.5f, -0.5f, 0), corner),
            "Untransformed min corner is incorrect"
        );

        obb.setEulerAngles(45, 0, 0);
        obb.getCorner000(corner);

        assertTrue(
            isClose(new Vector3(-0.35355335f,-0.5f,0.3535534f), corner),
            "Rotated min corner incorrect"
        );

        obb.setOrigin(new Vector3(1, 1, 1));
        obb.getCorner000(corner);

        assertTrue(
            isClose(new Vector3(1 + -0.35355335f,0.5f,1.3535534f), corner),
            "Rotated + translated min corner incorrect"
        );
    }

    @Test
    public void testContainsPoint() {
        OrientedBoundingBox obb = new OrientedBoundingBox(
            new Vector3(-0.5f, -0.5f, 0f),
            new Vector3(0.5f, 0.5f, 1)
        );

        Vector3 point0 = new Vector3(0, 0, 0.5f);
        Vector3 point1 = new Vector3(0.4f, 0, -0.1f);
        Vector3 point2 = new Vector3(1, 1, 1);

        // Untransformed case
        assertTrue(obb.contains(point0), "Given point should be inside OOB");
        assertTrue(obb.contains(obb.getOrigin(temp)), "Given point should be inside OOB");
        assertFalse(obb.contains(point1));

        // Rotated case
        obb.setEulerAngles(45, 0, 0);

        assertTrue(obb.contains(point1), "Contained point should be inside OOB");
        assertTrue(obb.contains(obb.getOrigin(temp)),"Boundary point should be inside OOB");
        assertFalse(obb.contains(point2), "Given point should be outside OOB");

        // Rotated + translated case
        obb.setOrigin(point2);

        assertTrue(obb.contains(point2), "Contained point should be inside OOB");
        assertTrue(obb.contains(obb.getOrigin(temp)), "Boundary point should be inside OOB");
        assertFalse(obb.contains(point0), "Given point should be outside OOB");
    }

    @Test
    public void testIntersectsOBB() {
        OrientedBoundingBox obb0 = new OrientedBoundingBox(
            new Vector3(-0.5f, -0.5f, 0f),
            new Vector3(0.5f, 0.5f, 1)
        );

        OrientedBoundingBox obb1 = new OrientedBoundingBox(
            new Vector3(-0.5f, -0.5f, 0f),
            new Vector3(0.5f, 0.5f, 1)
        );

        // Completely overlapping OBBs
        assertTrue(obb0.intersects(obb1));

        // Arbitrary orientation, separated
        obb0.setEulerAngles(-23, 38, -37);
        obb0.setOrigin(0.43f, -0.4f, -0.3f);
        obb1.setEulerAngles(26, 280, 35);
        obb1.setOrigin(0, 0, 1);
        assertFalse(obb0.intersects(obb1));

        // Arbitrary orientation, intersecting
        obb0.setEulerAngles(-23, 38, -37);
        obb0.setOrigin(0.43f, -0.4f, -0.3f);
        obb1.setEulerAngles(26, 280, 35);
        obb1.setOrigin(0, -0.3f, 1);
        assertTrue(obb0.intersects(obb1));
    }

    @Test
    public void testIntersectsAABB() {
        BoundingBox aabb = new BoundingBox(
            new Vector3(-0.5f, -0.5f, 1.5f),
            new Vector3(0.5f, 0.5f, 2.5f)
        );

        OrientedBoundingBox obb = new OrientedBoundingBox(
            new Vector3(-0.5f, -0.5f, 0f),
            new Vector3(0.5f, 0.5f, 1)
        );
        obb.setEulerAngles(45, 0, 0);
        obb.setOrigin(0, 0, 0);

        // Separated case
        assertFalse(obb.intersects(aabb));

        // Intersecting case
        aabb.set(
            new Vector3(-0.5f, -0.5f, 0.5f),
            new Vector3(0.5f, 0.5f, 1.5f)
        );

        assertTrue(obb.intersects(aabb));
    }
}
