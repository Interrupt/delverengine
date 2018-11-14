package com.interrupt.dungeoneer.collision;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Collision {
	public enum CollisionType { solidTile, floor, ceiling, angledWall };
	
	public Vector2 colPos = new Vector2();
	public Vector3 colNormal = new Vector3();
	public CollisionType colType;
	
	public Collision() { }
	
	public Collision(float x, float y, CollisionType colType) {
		colPos.x = x;
		colPos.y = y;
		this.colType = colType;
	}
	
	public void set(float x, float y, CollisionType colType) {
		colPos.x = x;
		colPos.y = y;
		this.colType = colType;
	}
	
	public void setHitNormal(Vector3 normal) {
		colNormal.set(normal.x, normal.y, normal.z);
	}
}
