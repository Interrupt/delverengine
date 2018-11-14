package com.interrupt.dungeoneer.interfaces;

import com.badlogic.gdx.math.Vector3;

public interface Directional {
	public void rotate90();

    public void rotate90Reversed();

	public void setRotation(float rotX, float rotY, float rotZ);

    public void rotate(float rotX, float rotY, float rotZ);

	public Vector3 getRotation();

	public Vector3 getDirection();
}
