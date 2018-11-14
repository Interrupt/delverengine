package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.badlogic.gdx.graphics.PerspectiveCamera;

public class LookAtTrigger extends BasicTrigger {

	@EditorProperty
	public float lookTime = 10;

	@EditorProperty
	public float lookDistanceThreshold = 6;

	float hasLookedAtTime = 0f;

	private transient Vector3 t_checkDistance = new Vector3();

	public LookAtTrigger() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) { }

	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);

		if(isActive) {
			BoundingBox bb = CachePools.getAABB(this);
			if (Game.camera.frustum.boundsInFrustum(bb)) {
				if(getDistanceToCamera(Game.camera) < lookDistanceThreshold) {
					hasLookedAtTime += delta;
				}
				else {
					hasLookedAtTime = 0;
				}
			} else {
				hasLookedAtTime = 0;
			}

			// Have we looked at this long enough?
			if (hasLookedAtTime > lookTime) {
				super.doTriggerEvent(null);
			}
		}
	}

	public float getDistanceToCamera(PerspectiveCamera camera) {
		return t_checkDistance.set(camera.position.x, camera.position.z, camera.position.y).sub(x, y, z).len();
	}
}
