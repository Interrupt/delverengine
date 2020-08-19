package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.interfaces.Directional;

public class DirectionalEntity extends Entity implements Directional {
	/** Entity rotation. */
    @EditorProperty( group = "Visual" )
	public Vector3 rotation = new Vector3();
	
	public transient Vector3 dirWork = new Vector3();
	
	public DirectionalEntity() { }
	
	public DirectionalEntity(float x, float y, int tex, boolean isDynamic)
	{
		super(x, y, tex, false);
	}
	
	@Override
	public void rotate90() {
		super.rotate90();
		rotation.z -= 90f;
	}

	@Override
    public void rotate90Reversed() {
        super.rotate90Reversed();
        rotation.z += 90f;
    }
	
	@Override
	public void setRotation(float rotX, float rotY, float rotZ) {
		rotation.x = rotX;
		rotation.y = rotY;
		rotation.z = rotZ;
	}

    @Override
    public void rotate(float rotX, float rotY, float rotZ) {
        rotation.x += rotX;
        rotation.y += rotY;
        rotation.z += rotZ;
    }

	@Override
	public Vector3 getRotation() {
		return rotation;
	}

	public Vector3 getDirection() {
		Vector3 dir = dirWork.set(1,0,0);
		dir.rotate(Vector3.Y, -rotation.y);
		dir.rotate(Vector3.X, -rotation.x);
		dir.rotate(Vector3.Z, -rotation.z);
		return dir;
	}
}
