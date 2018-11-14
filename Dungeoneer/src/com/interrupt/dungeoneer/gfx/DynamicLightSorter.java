package com.interrupt.dungeoneer.gfx;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;

public class DynamicLightSorter implements Comparator<DynamicLight> {
	
	Camera camera;
	
	public DynamicLightSorter(Camera camera) {
		this.camera = camera;
	}
	
	public int compare (DynamicLight o1, DynamicLight o2) {
		float dist1 = camera.position.dst(o2.position);
		float dist2 = camera.position.dst(o1.position);
		return (int)Math.signum(dist2 - dist1);
	}
}
