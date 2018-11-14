package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class DynamicLight {
	public Vector3 position = new Vector3();
	public Vector3 color = new Vector3(1f,1f,1f);
	public Vector3 workPos = new Vector3();
	public float range = 3f;
	
	public Vector3 getRenderPosition() {
		return workPos.set(position.x, position.z, position.y);
	}
}
