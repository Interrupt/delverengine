package com.interrupt.dungeoneer.collision;

import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Triangle;

public class Collidor {
	
	private static Vector3 tmp = new Vector3();
    private static Vector3 tempRayVec = new Vector3();

	private static Vector3 workVector1 = new Vector3();
	private static Vector3 workVector2 = new Vector3();
	private static Vector3 workVector3 = new Vector3();
	private static Vector3 workVector4 = new Vector3();
	private static Vector3 workVector5 = new Vector3();
	
	private static Vector3 best = new Vector3();
	
	private static Vector3 bestTriangleV1 = new Vector3();
	private static Vector3 bestTriangleV2 = new Vector3();
	private static Vector3 bestTriangleV3 = new Vector3();
	
	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection, and the normal
	 * 
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangleList(Ray ray, Array<Vector3> triangles, Vector3 intersection, Vector3 normal) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if (triangles.size % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.size - 2; i += 3) {
			boolean result = Intersector.intersectRayTriangle(ray, triangles.get(i), triangles.get(i + 1), triangles.get(i + 2), tmp);

			if (result == true) {
                tempRayVec.set(ray.origin);
				float dist = tempRayVec.sub(tmp).len2();
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
					
					bestTriangleV1.set(triangles.get(i));
					bestTriangleV2.set(triangles.get(i + 1));
					bestTriangleV3.set(triangles.get(i + 2));
					
					hit = true;
				}
			}
		}

		if (!hit)
			return false;
		else {
			if(intersection != null) {
				intersection.set(best);
			}
			
			if(normal != null) {
				Vector3 p1 = workVector1.set(bestTriangleV1);
				Vector3 p2 = workVector2.set(bestTriangleV2);
				Vector3 p3 = workVector3.set(bestTriangleV3);
				
				Vector3 c1 = workVector4.set(p2).sub(p1);
				Vector3 c2 = workVector5.set(p3).sub(p1);
				Vector3 nor = c2.crs(c1).nor();
				
				normal.set(nor);
			}
			
			return true;
		}
	}
	
	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection, and the normal
	 * 
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles(Ray ray, Array<CollisionTriangle> triangles, Vector3 intersection, Vector3 normal) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		for (int i = 0; i < triangles.size; i++) {
			Triangle t = triangles.get(i);
			
			boolean result = Intersector.intersectRayTriangle(ray, t.v1, t.v2, t.v3, tmp);

			if (result == true) {
                tempRayVec.set(ray.origin);
				float dist = tempRayVec.sub(tmp).len2();
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
					
					bestTriangleV1.set(t.v1);
					bestTriangleV2.set(t.v2);
					bestTriangleV3.set(t.v3);
					
					hit = true;
				}
			}
		}

		if (!hit)
			return false;
		else {
			if(intersection != null) {
				intersection.set(best);
			}
			
			if(normal != null) {
				Vector3 p1 = workVector1.set(bestTriangleV1);
				Vector3 p2 = workVector2.set(bestTriangleV2);
				Vector3 p3 = workVector3.set(bestTriangleV3);
				
				Vector3 c1 = workVector4.set(p2).sub(p1);
				Vector3 c2 = workVector5.set(p3).sub(p1);
				Vector3 nor = c2.crs(c1).nor();
				
				normal.set(nor);
			}
			
			return true;
		}
	}

	public static boolean intersectRayForwardFacingTriangles(Ray ray, Camera camera, Array<CollisionTriangle> triangles, Vector3 intersection, Vector3 normal) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		for (int i = 0; i < triangles.size; i++) {
			Triangle t = triangles.get(i);

			boolean result = Intersector.intersectRayTriangle(ray, t.v1, t.v2, t.v3, tmp);

			if (result == true) {
				tempRayVec.set(ray.origin);
				float dist = tempRayVec.sub(tmp).len2();
				if (dist < min_dist) {
					boolean frontFacing = false;
					if(normal != null) {
						Vector3 p1 = workVector1.set(t.v1);
						Vector3 p2 = workVector2.set(t.v2);
						Vector3 p3 = workVector3.set(t.v3);

						Vector3 c1 = workVector4.set(p2).sub(p1);
						Vector3 c2 = workVector5.set(p3).sub(p1);
						Vector3 nor = c2.crs(c1).nor();
						normal.set(nor);

						// if this is backfacing, skip!
						frontFacing = normal.dot(ray.direction) <= 0f;
						normal.set(nor);
					}

					if(frontFacing) {
						min_dist = dist;
						best.set(tmp);

						bestTriangleV1.set(t.v1);
						bestTriangleV2.set(t.v2);
						bestTriangleV3.set(t.v3);

						hit = true;
					}
				}
			}
		}

		if (!hit)
			return false;
		else {
			if(intersection != null) {
				intersection.set(best);
			}
			return true;
		}
	}
}
