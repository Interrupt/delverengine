package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Vector3;

public class Triangle {
	public Vector3 v1;
	public Vector3 v2;
	public Vector3 v3;
	
	public static Vector3 workVector = new Vector3();
	
	public Triangle( Vector3 v1, Vector3 v2, Vector3 v3 ) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
	
	public Vector3 midPoint() {
		workVector.set((v1.x + v2.x + v3.x) / 3f, (v1.y + v2.y + v3.y) / 3f, (v1.z + v2.z + v3.z) / 3f );
		return workVector;
	}
}