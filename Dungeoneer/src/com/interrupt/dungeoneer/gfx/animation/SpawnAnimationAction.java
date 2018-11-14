package com.interrupt.dungeoneer.gfx.animation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;

public class SpawnAnimationAction extends AnimationAction {
	
	public String spawnCategory;
	public String spawnName;
	private Vector3 spawnOffset = new Vector3();
	
	public SpawnAnimationAction() { }
	
	public SpawnAnimationAction(String spawnCategory, String spawnName) {
		this.spawnCategory = spawnCategory;
		this.spawnName = spawnName;
	}

	@Override
	public void doAction(Entity instigator) {
		Entity spawns = Game.instance.entityManager.getEntity(spawnCategory, spawnName);
		if(spawns != null) {
			Vector2 dir = new Vector2(Game.instance.player.x, Game.instance.player.y).sub(new Vector2(instigator.x - 0.5f, instigator.y - 0.5f));
			dir = dir.nor();
			
			Vector2 rotDir = new Vector2(spawnOffset.x, spawnOffset.y);
			rotDir.rotate(dir.angle());
			
			spawns.x = instigator.x + rotDir.x;
			spawns.y = instigator.y + rotDir.y;
			spawns.z = instigator.z + spawnOffset.z + 0.2f;
			
			Game.instance.level.SpawnEntity(spawns);
		}
	}
}
