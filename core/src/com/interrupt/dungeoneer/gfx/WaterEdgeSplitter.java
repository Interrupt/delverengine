package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Triangle;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.WaterEdges;

public class WaterEdgeSplitter {
    private static transient Intersector.SplitTriangle split = new Intersector.SplitTriangle(3);

    private static Plane northPlane = new Plane(new Vector3(0,0,-1), new Vector3(0.25f, 0, 0.25f));
    private static Plane southPlane = new Plane(new Vector3(0,0,1), new Vector3(0.75f, 0, 0.75f));
    private static Plane eastPlane = new Plane(new Vector3(-1,0,0), new Vector3(0.25f, 0, 0.25f));
    private static Plane westPlane = new Plane(new Vector3(1,0,0), new Vector3(0.75f, 0, 0.75f));

    private static Plane getPlaneForWaterEdge(WaterEdges edge) {
        if(edge == WaterEdges.North) {
            return northPlane;
        }
        else if(edge == WaterEdges.South) {
            return southPlane;
        }
        else if(edge == WaterEdges.East) {
            return eastPlane;
        }
        else if(edge == WaterEdges.West) {
            return westPlane;
        }

        return null;
    }

    public static Array<Triangle> split(Tile t, WaterEdges edge) {

        float heightOffset = 0.0025f;

        Plane plane = getPlaneForWaterEdge(edge);

        if(plane == null) {
            plane = new Plane(new Vector3(0,1,0), new Vector3(0.25f, 1, 0.25f));
        }

        float[] triangle1 = new float[] { 0, t.getSEFloorHeight() + heightOffset, 1, 1, t.getNWFloorHeight() + heightOffset, 0, 0, t.getNEFloorHeight() + heightOffset, 0 };
        float[] triangle2 = new float[] { 0, t.getSEFloorHeight() + heightOffset, 1, 1, t.getSWFloorHeight() + heightOffset, 1, 1, t.getNWFloorHeight() + heightOffset, 0 };

        Array<Triangle> triangles = new Array<Triangle>();

        Intersector.splitTriangle(triangle1, plane, split);
        for(int v = 0; v < split.numFront * 9; v += 9) {
            Vector3 v1 = new Vector3(split.front[v], split.front[v + 1], split.front[v + 2]);
            Vector3 v2 = new Vector3(split.front[v + 3], split.front[v + 4], split.front[v + 5]);
            Vector3 v3 = new Vector3(split.front[v + 6], split.front[v + 7], split.front[v + 8]);
            if(((plane.testPoint(v1) == Plane.PlaneSide.Back || plane.testPoint(v1) == Plane.PlaneSide.OnPlane) || (plane.testPoint(v2) == Plane.PlaneSide.Back || plane.testPoint(v2) == Plane.PlaneSide.OnPlane) || (plane.testPoint(v3) == Plane.PlaneSide.Back || plane.testPoint(v3) == Plane.PlaneSide.OnPlane))) {
                triangles.add(new Triangle(v1, v2, v3));
            }
        }

        Intersector.splitTriangle(triangle2, plane, split);
        for(int v = 0; v < split.numFront * 9; v += 9) {
            Vector3 v1 = new Vector3(split.front[v], split.front[v + 1], split.front[v + 2]);
            Vector3 v2 = new Vector3(split.front[v + 3], split.front[v + 4], split.front[v + 5]);
            Vector3 v3 = new Vector3(split.front[v + 6], split.front[v + 7], split.front[v + 8]);
            if(((plane.testPoint(v1) == Plane.PlaneSide.Back || plane.testPoint(v1) == Plane.PlaneSide.OnPlane) || (plane.testPoint(v2) == Plane.PlaneSide.Back || plane.testPoint(v2) == Plane.PlaneSide.OnPlane) || (plane.testPoint(v3) == Plane.PlaneSide.Back || plane.testPoint(v3) == Plane.PlaneSide.OnPlane))) {
                triangles.add(new Triangle(v1, v2, v3));
            }
        }

        return triangles;
    }

    public static float getTextureRotationForEdge(WaterEdges edge) {
        if(edge == WaterEdges.East) {
            return 180f;
        }
        else if(edge == WaterEdges.West) {
            return 0f;
        }
        else if(edge == WaterEdges.South) {
            return 90f;
        }
        else if(edge == WaterEdges.North) {
            return 270f;
        }

        return 0f;
    }

    public static float getTexU(Vector3 inVector, WaterEdges edge, TextureRegion region) {
        Vector3 v = new Vector3(inVector);
        v.add(-0.5f, 0, -0.5f);
        v.rotate(Vector3.Y, getTextureRotationForEdge(edge));
        v.add(0.5f, 0, 0.5f);

        float diff = region.getU2() - region.getU();
        return v.z * diff + region.getU();
    }

    public static float getTexV(Vector3 inVector, WaterEdges edge, TextureRegion region) {
        Vector3 v = new Vector3(inVector);
        v.add(-0.5f, 0, -0.5f);
        v.rotate(Vector3.Y, getTextureRotationForEdge(edge));
        v.add(0.5f, 0, 0.5f);

        float diff = region.getV2() - region.getV();
        return v.x * diff + region.getV();
    }
}